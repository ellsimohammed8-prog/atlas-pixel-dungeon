# Arabic Rendering Fix — Report (agent/arabic-fix)

Branch: `agent/arabic-fix` (created from `main` @ v1.3.1, c5d7399). Nothing was
merged, pushed or released from this branch.

## TL;DR

Arabic has been shaped per *token*, not per *line*. `splitforTextBlock` split
strings **between every Arabic character**, so every token became its own
`RenderedText`, and a one-character `RenderedText` can only produce the
**isolated** glyph form — the letter's joins and even the word-level bidi were
decided without context. On top of that, a block-level hack *mirrored the word
positions* of Arabic lines, which correctly reordered the isolated letters but
fought the shaper once whole words were shaped. A correct set of font
generation knobs (true size, hairline border, hinting) was already in place
from v1.1.2/v1.1.3 and remains.

---

## 1. Suspect-by-suspect diagnosis

All rendering evidence below was produced with a harness that runs the
production pipeline pieces (`ArabicSupport.shape`,
`DesktopPlatformSupport.splitforTextBlock`, `PlatformSupport.getFont`,
`RenderedTextBlock.build/layout`) and saves a PNG at a phone-like pixel size
(glyphs generated at 36 device px, defaultZoom 4 — what a 1440×2560-class
device draws for a size-9 text block). Run with
`gradlew :desktop:arabicFixHarness` (available in this branch only).
Chrome renders of the *same bundled font* (`core/assets/fonts/arabic.ttf`,
36 px HTML, real HarfBuzz shaping + UAX#9 bidi) are used as ground truth.

### A. Glyph advances — CLEARED as root cause; the ~6% tightening hack removed anyway
- Evidence: before/after line widths at gen 36 differ by only 0–4% for all
  probes (`before…` vs `after…` PNG "1line w=…" labels, e.g. HP probe 243→227px,
  slam 620→603px, digits 301→315px), while legibility improved completely —
  so the previous attempts' failures were not an advance-amplitude problem.
- The font's designed advances are correct at true size (the v1.1.2 fix already
  removed the 2× supersampling that doubled them via incremental loading).
- Action: the ~6% advance-tightening post-pass in `PlatformSupport.getFont`
  was **removed**. Pulling each advance in by up to 6% only *helped* joins that
  were wrongly isolated, and over-tightens correctly-shaped text. With shaping
  fixed, font-metric advances are correct by themselves.
- The full Arabic glyph repertoire is now preloaded at font creation (below),
  which also removes the old "late incremental loads got different metrics"
  issue — every glyph of a font now has the font's exact metrics.

### B. Font coverage — CLEARED (no missing glyphs)
- `generateFont` used to load only `'\uFFFD'` and rely on incremental loading.
  Now the Arabic font is generated with `U+FFFD + " 0123456789" + U+0600..06FF +
  FE70..FEFC` — the complete set `ArabicSupport` can emit (isolated/initial/
  medial/final forms FE80–FEFC incl. lam-alef ligatures FEF5–FEFC, base
  letters, harakat U+064B–0652, U+0670, tatweel, digits).
- Harness runs report missing glyphs through `RenderedText.measure`'s built-in
  "could not render" check; no such error appears in any run.

### C. Diacritics — CLEARED as a join-breaker (positioning is a known limitation)
- Probe `مُهَرِّبٌ شؤون الـبَطْلُ ثُمَّ عَادَ` renders connected at gen 36 with
  marks overlapping the letters (`zoom_harakat_after.png`). Harakat are
  transparent for joining in the shaper and are assigned no pushing advance,
  so word joins hold.
- Limitation (unchanged, upstream-tooling limit): marks are drawn as
  zero-advance glyphs at the pen position, not HarfBuzz-attached to their
  glyph; positions are approximately right, not font-crafted. See GT.

### D. Word segmentation / two reorder layers — CONFIRMED (main root cause)
- **Before:** `InArabic`/`InArabic_Presentation_Forms_B` split clauses in the
  Desktop/Android splitter patterns made every Arabic character its own token.
  A token is shaped alone → always isolated form → *no amount of advance or
  border tuning could connect them* (exactly why v1.1.1–v1.1.3 failed).
  Visible in `before_desktop_gen36_arabic.png`: spaced-out letters.
- The block-level mirror (`RenderedTextBlock.layout`) then mirrored word
  positions per line. With isolated per-char tokens that produced correct
  reading order; with whole-word tokens it would put visually-reordered chunks
  back in logical order — the two reorder layers fight. Removed.
- **After:** `RenderedTextBlock.text()` shapes the whole string once
  (context-sensitive joins + bidi per line), *then* tokenizes (`splitforTextBlock`
  of the shaped text; splits now only on newlines, spaces, `_`/`**` and CJK).
  Layout lays out tokens left-to-right; no positional mirroring. Arabic
  paragraphs are right-aligned within the block (the alignment part the old
  mirror used to provide).
- `shape()` is made idempotent: strings that already contain presentation
  forms are returned unchanged, so the per-token `RenderedText.shape()` call
  cannot re-run the bidi on shaped input (which would scramble order again).
- Evidence: `(12 مركب)` rendered `"(مركب)12"` before (paren inside-out, split
  letters), now `"(مركب 12)"` matching Chrome (`zoom_combreq_before.png` vs
  `zoom_combreq_after.png` vs `gt_chrome_zoom_combreq.png`).

### E. True-size generation — CONFIRMED necessary, kept
- Arabic is generated at its true pixel size (no supersampling) — required
  because glyphs loaded incrementally after generation keep the
  generation-scale xadvance. The sweep PNGs at gen 36 show joins are fine at
  true size; the avatar-size legibility gap is closed by the v1.1.3 hairline
  border, not by none.

### F. Shaper edge cases — CONFIRMED & FIXED (brackets, digit runs, placeholders, Latin runs)
Evidence from probes at gen 36 (after screenshots):
- **ASCII brackets**: a bracket group around Latin/number content used to
  render inside-out (`(مركب)12`, `+10%(`). Each ASCII bracket is now its own
  bidi unit so RTL reversal moves it to the group edge, and the final pass
  re-mirrors the glyph. `"(مكافأة +10%)"` now displays as `"(+10% مكافأة)"`.
  Against Chrome, the sign runs display as LTR islands (`+10%`, not Chrome's
  `%10+`); that is a deliberate simplification that keeps `%1$d` and numeric
  tokens intact and game-readable — the paren *group* layout matches Chrome.
- **Arabic-Indic digits**: a maximal run `٠–٩` is one unit → `٠١٢٣٤٥٦٧٨٩` reads
  in numeric order (`zoom_digits_after.png`; matches Chrome).
- **Placeholders**: `%1$d` etc. are single LTR islands; never reordered.
- **Latin words inside Arabic**: word-order is no longer reversed inside a
  maximal run of consecutive non-Arabic words, so "Atlas Pixel Dungeon" keeps
  its name order (`zoom_mixed_after.png`; matches Chrome).
- **Teh marbuta / alef maksura / tatweel**: handled by the existing form
  tables; visible in probes.

### Knob sweep (border / hinting), gen 36
Production knobs (hairline same-colour border `max(0.5, size*0.03)`,
`Hinting.Full`, true size, negative spaceX) were compared against three
alternatives with everything else equal (`sweep_gen36_*.png`,
side-by-side `sweep_cmp_hp_zoom.png`):
- no border + full hint: strokes fade at 36px, joins read weakly;
- hairline border + no hinting: comparable boldness, slightly softer edges;
- no border + no hinting: thin strokes, weakest readability.
**Production knobs kept** — no change needed. (Hinting choice makes less
difference than the border at these sizes.)

---

## 2. What changed (code)

| File | Change |
|---|---|
| `SPD-classes/.../utils/ArabicSupport.java` | bracket peel into RTL; Arabic-Indic digit runs as units; chunk-based bidi that does not reverse Latin word runs; shape() idempotence guard; `containsArabicText()` (logical + presentation forms); removed dead helpers (`mirrorBrackets`, `isNeutral`, `isFormatToken`, `joinsForward`) |
| `SPD-classes/.../utils/PlatformSupport.java` | Arabic fonts generated with the full Arabic repertoire preloaded (replaces `"\uFFFD"`-only + incremental discovery); removed the ~6% advance-tightening post-pass |
| `SPD-classes/.../noosa/` – (none) | `RenderedText` unchanged (shaper call is idempotent now) |
| `core/.../ui/RenderedTextBlock.java` | shape whole text before tokenizing; removed the block-level line mirroring; Arabic lines right-aligned instead (only when language == Arabic and block is left-aligned) |
| `desktop/.../DesktopPlatformSupport.java`, `android/.../AndroidPlatformSupport.java` | removed `InArabic`/`InArabic_Presentation_Forms_B` per-character splits from the text-block splitter patterns |
| `desktop/build.gradle`, `desktop/src/test/.../ArabicFixHarness.java` | test-only evidence harness + gradle task (not shipped game code) |

Performance: no per-frame cost added on the draw path. The only runtime
additions are text-set-time (an extra context scan during `text()` rebuilds,
same order as the existing tokenizing) and build-time glyph preload per font
(one-time, ~600 glyphs). English and other languages take the exact same code
paths as before (their strings never enter the Arabic branches; splitter edit
only removes patterns that can never match them).

`translations`: untouched, still logical order, shaped at render time.

## 3. Screenshots (this branch, `ARABIC-EVIDENCE/`)

All at gen 36 device px (phone-like), production pipeline, Arabic language.

| Probe | Before | After | Chrome GT |
|---|---|---|---|
| full page all probes | `before_desktop_gen36_arabic.png` | `after_desktop_gen36_arabic.png` | `gt_chrome.png` (+`gt_chrome.html` to re-render) |
| mission HP probe `يستعيد…(مكافأة +10%)` | `zoom_hp_before_vs_after.png` | `zoom_hp_after.png` | `gt_chrome_zoom_probe.png` |
| `(12 مركب)` | `zoom_combreq_before.png` | `zoom_combreq_after.png` | `gt_chrome_zoom_combreq.png` |
| digits `٠١٢٣٤٥٦٧٨٩ وا 918 بعد` | — (same page, before) | `zoom_digits_after.png` | (gt page) |
| harakat `مُهَرِّبٌ…` | — (same page, before) | `zoom_harakat_after.png` | (gt page) |
| mixed `مرحبا بك في Atlas Pixel Dungeon يا بطل` | — (same page, before) | `zoom_mixed_after.png` | (gt page) |
| English no-regression | n/a | `after_desktop_gen36_english.png` | n/a |
| knob sweep | — | `sweep_gen36_*.png`, `sweep_cmp_hp_zoom.png` | n/a |

Note: the long trinket/slam probes wrap and are right-aligned in the after
page; the before page shows the same strings broken letter-by-letter.

## 4. What these screenshots do and do not prove

- They prove: the *desktop harness* (a 1:1 walk of the same code the Android
  build runs: shaper, splitter, font generation, block layout) produces
  connected cursive, correct RTL word order, working wraps/alignment/highlights,
  correct digits/brackets/Placeholders at phone-like pixel sizes.
- They do NOT prove device rendering: different GPU/GL driver, density
  bucketing, DIP scaling and the actual `android` launcher can still shift
  rasterization. On-device verification belongs to the owner's screenshots, as
  the previous three attempts were judged there.
- The Android module could NOT be compiled in this environment: no Android
  SDK is installed and no `local.properties` exists, so `:android` is not
  buildable here. Its fix (`AndroidPlatformSupport.java`) is the same edit,
  character-for-character, to the same regex-string whose twin in
  `DesktopPlatformSupport.java` was verified compiling; the device owner
  should still see it build on their machine.
- The iOS platform has no Arabic font generator (unchanged, out of scope).
- Floating combat text still uses the pixel bitmap font with no Arabic glyphs
  (out of scope, per mission).
- Harakat positioning is sequential-approximate, not attached (see C).
