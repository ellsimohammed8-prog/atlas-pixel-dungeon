# Liquid Gold — Ring Recycling Report

Branch: `agent/liquid-gold` (created from `main` @ `964507a`, "v1.3.2: news entry")

Liquid Gold is an alchemy reagent that lets a player destroy a spare ring to
produce gold, then spend that gold to raise another ring by one upgrade level.
It is the ring answer to the existing Arcane Resin (wand) pattern, and it is
implemented natively on top of the game's own `Recipe` / `AlchemyScene` /
`WndBag.ItemSelector` plumbing.

> Note: `ATLAS-AGENT-CONTEXT.md` was referenced by the mission but does not exist
> in the working tree, in git history, or in the user profile. Work proceeded
> using the guardrails restated in the mission itself and the repository's
> observed conventions (feature branches named `agent/*`, deliverable report at
> repo root, no commit signatures or co-author lines).

---

## Step 0 — Code map (mapped before building)

Everything below was read and understood before any new code was written.

| Area | Location | What it does / how it was used |
| --- | --- | --- |
| Recipe base | `items/Recipe.java` | Abstract `Recipe` with `testIngredients`, `cost`, `brew`, `sampleOutput`; static arrays `oneIngredientRecipes` / `twoIngredientRecipes`; `usableInRecipe(Item)` gates which items can be dropped in the pot. |
| Registration | `Recipe.java` (`findRecipe`, static arrays) | Recipes are matched by `Item.isSimilar`; new recipes are added to the one/two-ingredient arrays. |
| Blueprints pattern | `items/.../*Blueprint*`, `items/Recipe.java` | Precedent for adding a new, self-contained recipe class and appending it to the static array without disturbing ordering. |
| Alchemy pot UI | `scenes/AlchemyScene.java` | Handles the pot slots, auto-detach of stackables (`LiquidMetal`, missile weapons), recipe preview and confirm. Three detach branches had to learn about the new stackable reagent. |
| Ring model | `items/rings/Ring.java` | `level()`, `cursed`, `cursedKnown`, `levelKnown`, `duplicate()`, `isSimilar`. `Ring.upgrade()` exists but has a random curse-removal chance, so it was **not** reused. |
| Buff scaling | `items/rings/Ring.java` + subclass `buff()` | Ring power is derived from `level()`, so a plain `level(level()+1)` correctly strengthens a ring. |
| Arcane Resin (production + apply) | `items/ArcaneResin.java` | The closest existing analogue: a reagent produced by melting wands and spent to raise wands. Used as the behavioural template (not copied). |
| Liquid Metal | `items/LiquidMetal.java` | Precedent for a stackable reagent with a custom `apply` action, value, and `.desc`, plus special stackable handling in `AlchemyScene`. |
| Item selector | `ui/WndBag.java` (`ItemSelector`) | Interface used for "select an item" prompts; `itemSelectable` filters choices, `onSelect` acts. Used for the in-inventory APPLY flow. |
| Guide / quick recipes | `ui/QuickRecipe.java` | Supplies the example rows shown in the alchemy guide; entries are indexed by recipe count. |
| Journal | `journal/Catalog.java` | Enum of catalogued items grouped by category; new items must be listed to appear in the journal. |
| Sprites | `sprites/ItemSpriteSheet.java`, `assets/sprites/items.png` | Sprite atlas cells and per-cell pixel rectangles. |
| Strings | `assets/messages/items/items.properties`, `items_ar.properties` | EN/AR item strings; every new string must exist in both files. |

---

## What was built

Two recipes in the alchemy pot, plus a direct APPLY action:

1. **Melt** (1 ingredient): any ring whose curse state is known + **3 energy** →
   Liquid Gold. The ring is destroyed permanently.
2. **Apply (pot)** (2 ingredients): one ring + a stack of Liquid Gold → the same
   ring at +1, consuming `target level` Liquid Gold. No energy cost.
3. **APPLY (inventory)**: using Liquid Gold from the bag opens a ring picker
   that excludes rings it cannot improve; the same cost and cap rules apply.

The picker *and* the recipe each independently refuse rings that are unknown or
already at +3 or higher.

---

## Files changed (one line each)

| File | Change |
| --- | --- |
| `items/LiquidGold.java` (**new**) | New stackable reagent `LiquidGold` with `MAX_RING_LEVEL=3`, static `upgradeCost`/`canUpgrade`/`meltYield`/`raiseRing`, an APPLY `WndBag.ItemSelector`, and the nested `MeltRecipe` and `ApplyRingRecipe`. |
| `items/Recipe.java` | Registered `LiquidGold.MeltRecipe` (one-ingredient) and `LiquidGold.ApplyRingRecipe` (two-ingredient); `usableInRecipe` now allows a `Ring` when `cursedKnown`; added the `Ring` import. |
| `items/rings/Ring.java` | Added nested `public static class PlaceHolder extends Ring` (`RING_HOLDER` sprite, `isSimilar` matches any ring) for the guide/quick-recipe rows. |
| `scenes/AlchemyScene.java` | `LiquidGold` treated as a stackable reagent in the three pot detach branches (auto-detach-all instead of detach-one). |
| `sprites/ItemSpriteSheet.java` | Added `LIQUID_GOLD = POTIONS+14` and `assignItemRect(LIQUID_GOLD, 8, 15)`. |
| `ui/QuickRecipe.java` | Added the two Liquid Gold guide rows (melt with a ring placeholder; apply with a +1 ring placeholder). |
| `journal/Catalog.java` | Added `LiquidGold.class` to the `MISC_CONSUMABLES` catalog group. |
| `assets/messages/items/items.properties` | Added the `items.liquidgold.*` English strings (name, ac_apply, prompt, not_enough, level_too_high, apply, desc, discover_hint). |
| `assets/messages/items/items_ar.properties` | Added the same `items.liquidgold.*` keys in Arabic. |
| `assets/sprites/items.png` | Added a distinct Liquid Gold vial: the Liquid Metal vial tinted gold, written into a previously unused atlas cell `(14,22)`. Only that one cell changed. |

No changes were made to ring balance, drop rates, or any other system. No file
outside this feature footprint (plus the shared EN/AR string files and the sprite
atlas) was touched.

---

## Yield and cost table (exact)

### Melting — Liquid Gold produced

| Ring level | Normal ring | Cursed ring |
| --- | --- | --- |
| +0 | 1 | 0 |
| +1 | 2 | 1 |
| +2 | 3 | 1 |
| +3 | 4 | 2 |

Formula: `yield = max(0, level + 1)`, then halved (integer division, floored)
if the ring is cursed. The yield is shown in the pot's output preview
(`sampleOutput`) before the player confirms.

### Applying — Liquid Gold spent

| Raising to | Cost (Liquid Gold) | Allowed? |
| --- | --- | --- |
| +1 | 1 | yes |
| +2 | 2 | yes |
| +3 | 3 | yes |
| +4 or higher | — | **refused** (hard cap +3) |

Formula: `cost = ring.level() + 1`. A ring may be raised only while
`0 <= level < 3`. The recipe refuses a ring at +3 or higher, and also refuses
when the Liquid Gold stack is smaller than the cost; the inventory picker hides
such rings for the same reason.

**State rules:** raising preserves the ring's curse state and its identification
state (level is set directly, never via `Ring.upgrade()`, which can remove a
curse). Melting destroys the ring permanently. Applying consumes the Liquid Gold
and replaces the input ring with the upgraded one.

---

## Save compatibility

- Old saves load unchanged: no existing class, field, item ID, or serialization
  path was modified.
- The new item uses standard serialization only (`Item` defaults; `stackable`,
  `bones`, no custom `store`/`restore` and no bundle keys).
- The new sprite is an extra atlas cell and does not shift any existing cell
  coordinates, so existing sprite references remain valid.

---

## EN / AR parity

Every new player-facing string was added to **both**
`assets/messages/items/items.properties` and
`assets/messages/items/items_ar.properties` in the same commit:

`items.liquidgold.name`, `.ac_apply`, `.prompt`, `.not_enough`,
`.level_too_high`, `.apply`, `.desc`, `.discover_hint` (8 keys × 2 languages).

No English string was added without its Arabic counterpart.

---

## Recipes appear ungated

Both recipes are registered in `Recipe`'s static arrays, so they are matched by
the pot like any other recipe and appear in the alchemy guide (`QuickRecipe`)
without any unlock flag or progression gate.

---

## Verification

### Compile

Command:

```
gradlew :core:compileJava :desktop:compileJava :android:compileDebugJavaWithJavac --console=plain
```

Result: `BUILD SUCCESSFUL`, exit 0 — **0 errors**. (PowerShell may print
"Exited with code 1" because Gradle writes Notes to stderr even on success; the
authoritative line is `BUILD SUCCESSFUL`.) Toolchain: JDK 21 (Android Studio
JBR), Android SDK via `ANDROID_HOME`, compileSdk 36.

### Logic

A temporary headless harness exercised the pure static logic
(`meltYield` / `upgradeCost` / `canUpgrade`) across the full level × curse
matrix and the refusal cases. **All 30 checks passed**, covering: melt yields
1/2/3/4 for +0..+3, cursed halving (0/1/1/2), apply cost 1/2/3, `canUpgrade`
true at +0/+1/+2 and false at +3, and refusal when the stack is short. The
harness and its temporary Gradle task were removed afterwards so they do not
remain in the feature footprint.

### Not verified

- No verification was performed on a physical phone or emulator. This report
  does **not** claim phone verification. Only compilation (desktop + core +
  Android) and the logic harness above were run.

---

## Known limits

- The APPLY inventory flow requires the ring's curse state **and** level to be
  known to be selectable; an unidentified ring will not appear in the picker
  (identify it first). The pot's Apply recipe uses the same requirement.
- Melting requires the ring's curse state to be known (so the yield can be shown
  before confirming); an unknown-curse ring is not offered for melting.
- Liquid Gold cannot be used on anything but rings, and cannot raise a ring past
  +3 by any path (pot recipe or inventory action).
- The Liquid Gold icon is a gold tint of the existing Liquid Metal vial placed in
  a new atlas cell; it is visually distinct but shares the vial silhouette.
