# Chain Warden — Chain Snare build report

Branch: `agent/chain-warden` (created from `main` = `964507a`).
Spec: `CHAIN-WARDEN-DESIGN.md`, as amended by MISSION 1.2-BUILD (mission wins on conflict).

## 1. Files changed (one line each)

| File | Change |
| --- | --- |
| `CHAIN-WARDEN-DESIGN.md` | First commit on the branch: an unchanged copy of the approved design note, so the spec travels with the code. |
| `core/.../actors/mobs/Tengu.java` | The mechanic: `CHAIN_ABILITY = 3`, new `chainCooldown` / `chainUsed` / `pendingJump` state, `chainUsable()`, the deterministic branch at the top of `useAbility()`, `telegraphChain()`, `throwChain()`, the inner `ChainAbility` wind-up buff and its `HangingChain` visual, plus bundle store/restore for the new state. |
| `core/.../actors/buffs/Chained.java` | New 1-turn bind buff, modelled on `Roots`: NEGATIVE, announced, `BuffIndicator.ROOTS`, refuses flying, set-it/clear-it guard on `Char.rooted`, detaches the moment the target flies. |
| `core/src/main/assets/messages/actors/actors.properties` | The English strings from design §5 (`actors.mobs.tengu$chainability.*`, `actors.buffs.chained.*`). |
| `core/src/main/assets/messages/actors/actors_ar.properties` | The Arabic partners for every one of those keys, in the same commit. |

Nothing else is touched: `effects/Chains.java`, `Effects.java`,
`levels/PrisonBossLevel.java`, the traps, the other three abilities' code and
numbers, drops, Badges and rankings are all byte-for-byte unchanged
(`git diff --stat 964507a..HEAD` lists exactly the four files above plus this report).

## 2. Decision-compliance checklist (owner decisions, 2026-10-18)

| Rule | Status | How a reviewer confirms it in the diff |
| --- | --- | --- |
| **Bind only — no reel, no displacement** | Honoured | `git diff -- core/.../mobs/Tengu.java` contains no `Pushing`, no `move(`, no `occupyCell`, no `Ballistica`-path walk in the chain code. The only mention of the reel is the word "reel" in prose comments and this report. The string `actors.mobs.tengu$chainability.reeled` is deliberately **absent** from both message files (grep finds zero hits) because with the reel deferred it would be unreachable dead text. |
| **No damage, ever** | Honoured | The chain path calls no `damage(`, no `Buff.affect` other than `Chained`, no `interrupt()`. Grep the `Chain Ability` section of `Tengu.java` for `damage` — zero hits. `BombAbility`'s own `damage(...)`/`Statistics` lines are untouched (same file, different class, unchanged lines). |
| **No badge / score contact** | Honoured | Grep the whole diff for `qualifiedForBossChallengeBadge` and `bossScores` — zero hits. (Both still exist in `BombAbility`, unmodified, as before.) |
| **One teaching use in phase 1** | Honoured | In `useAbility()`, `boolean phase1 = ... state() == FIGHT_START`; the teaching use increments `abilitiesUsed` only when `!phase1`. `chainUsable()` allows `FIGHT_START` only while `chainUsed == false` and `HP <= 3*HT/4`. Since phase 1 ends at `HP <= HT/2` (`damage()`), the phase-1 window is `HT/2 < HP <= 3*HT/4`. |
| **Nothing else about the fight changes** | Honoured | `BOMB_ABILITY`, `FIRE_ABILITY`, `SHOCKER_ABILITY` values, their rolls, `targetAbilityUses()`, the warp brackets and the phase transitions are unchanged. The chain refuses to act in arena until `abilitiesUsed >= 2`, so the scripted first bomb and second shocker still happen exactly as before; the phase-1 teaching use consumes no `abilitiesUsed` slot, so the arena's opening sequence is untouched. `spendAbilityTime()` is only a pure extraction of the block that was already there (same conditions, same `spend()` calls). |
| **Footprint** | Honoured | Four files, all named by the design's implementation sketch. |
| **EN + AR parity in the same commit** | Honoured | All 9 new keys exist in both files with identical key names; a compare of the key lists reports no difference. |
| **No agent files, no signatures** | Honoured | Commit messages are plain one-liners; no `AGENTS.md`/plan/trace files; no co-author lines. |
| **Never touch `main`** | Honoured | All commits are on `agent/chain-warden` only. |

## 3. Trigger matrix (verified by code inspection, not by a running build)

No headless harness was run — see §5. The matrix below is read directly off
`chainUsable()`; every row is a single early-return in that method.

Eligible only when **all** of these hold:

| Condition | Test in code | Eligible when |
| --- | --- | --- |
| Level is the prison | `Dungeon.level instanceof PrisonBossLevel` | true |
| Fight state | `state()` | `FIGHT_START` **or** `FIGHT_ARENA` (`START`/`FIGHT_PAUSE`/`WON` → false) |
| Phase-1 permission | `state == FIGHT_START` | only while `!chainUsed` (teaching) |
| Phase-2 gate | `state == FIGHT_ARENA` | only when `abilitiesUsed >= 2` (never preempts the scripted bomb/shocker) |
| Not mid-warp | `!pendingJump` | true (set in `damage()` while a bracket warp is scheduled, cleared when the warp VFX actor runs) |
| Past the notice beat | `BossHealthBar.isAssigned()` | true (assigned by `notice()`) |
| Cooldown | `chainCooldown > 0` | false (0 = ready) |
| Not twice in a row | `lastAbility == CHAIN_ABILITY` | false |
| HP gate | `HP` | teaching: `HP <= 3*HT/4`; regular: `HP <= HT/2` |
| Ability budget | `abilitiesUsed >= targetAbilityUses()` (regular uses) | false, i.e. the Snare shares the fight's ability budget |
| Hero valid | `hero != null && hero.isAlive()` | true |
| Not already bound | `hero.buff(Chained.class) != null` | false |
| Not immovable | `hero.properties().contains(IMMOVABLE)` | false |
| Distance | `Dungeon.level.distance(pos, hero.pos)` | `2 <= d <= 5` |
| Line of sight | `new Ballistica(pos, hero.pos, PROJECTILE).collisionPos == hero.pos` | true |

Representative ineligible cases, all reached by exactly one of the rows above:
hero adjacent (distance 1) → refused; hero 6+ tiles away → refused; a wall
between them → refused; the boss just took a bracket hit and a warp is queued
→ refused for that action; the hero is already `Chained` → refused; the hero is
levitating (`IMMOVABLE` is *not* the test for that — a flying hero is instead
refused at resolution time, see §4) → the throw is allowed but the bind is
refused and the `no_anchor` line plays.

## 4. A save made DURING the warning turn

What is written to the save: the boss's own new fields
(`chain_cooldown`, `chain_used`, `chain_pending_jump`) alongside the existing
ones, **and** the in-flight wind-up — `ChainAbility` is a buff on the Warden, so
it is serialised with his buff list, carrying its `target_pos` and its Actor
`time` (the standard `Actor` bundle). `Chained` itself serialises normally if it
is already on the hero.

What the player sees after loading:

1. The fight resumes in the same state, with the Warden's chain cooldown and
   teaching flag intact — he will not get a second teaching line and will not
   re-throw early.
2. The warning turn is **not** skipped and **not** replayed: no warning line is
   printed again, and the chain is **not** drawn again — the hanging chain
   (`HangingChain`) and the `"!"` marker are transient screen objects that are
   deliberately not part of the save, so after loading there is **no visible
   chain on screen** even though the Snare is still pending.
3. The pending Snare still resolves, on the same scheduled moment it would have
   without the save: on the wind-up buff's next `act()` it re-verifies live
   state, distance, line of sight and hero state. If everything still holds the
   hero is bound for one turn (`chained` buff line, one turn of lost movement);
   if anything changed — the hero stepped out of range, broke line of sight, or
   is now flying — the chain snaps (effect, sound, `snapped` line) and nothing
   happens to the hero.

In short: the *mechanics* of the pending Snare survive the load; the *visual*
of the warning turn does not come back. This is the one deliberate
save/visual gap in the mechanic and it is listed again under §6.

## 5. Build result

Command actually run, in the repo root:

```
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat :core:compileJava --offline --console=plain
```

Result: **BUILD SUCCESSFUL**, 0 errors (`:core:compileJava`, with
`:services:compileJava` and `:SPD-classes:compileJava` up-to-date). JDK 21
(Android Studio's bundled JBR). Only the pre-existing "unchecked or unsafe
operations" note from `Char.java` is printed.

Scope note, stated plainly: the **core** module — which contains all four
changed Java files — was compiled. The Android application module was **not**
compiled (no Android SDK/AGP toolchain run in this environment). So: *build
run for `core`; Android module build not run.*

**No headless logic harness was run.** The trigger matrix in §3 is a
code-inspection matrix, not the output of an executed test. It is not claimed
to be a test result.

**No phone verification of any kind was performed.** Nothing below has been
seen on a device; the phone-test checklist in §7 is handed over for that.

## 6. Known limits and deliberate choices

- **Hanging-chain visual is not restored on load** (see §4). Everything else
  about the pending Snare is.
- **`reeled` string omitted** from both message files. The reel is deferred, so
  the line would be unreachable. It can be added in the same commit that builds
  the reel.
- **`pendingJump` / `notice()` checks are behavioural proxies.** `pendingJump`
  is set exactly where a bracket warp is scheduled; the "not on the `notice()`
  beat" rule is expressed as "the boss health bar is assigned" (`notice()` is
  what assigns it). Both are correct for the current code paths but are not a
  literal "was `notice()` called this tick" flag.
- **`ChainAbility` resolves via `Buff.affect(hero, Chained.class, 1f)`; if the
  hero is airborne, `Chained.attachTo` refuses and the `no_anchor` line plays.**
  A flying hero therefore cannot be bound, exactly as designed (counterplay #2).
- **`Char.rooted` ownership guard.** `Chained` only clears `rooted` if it was
  the one that set it. If some *other* source clears `rooted` while `Chained`
  is active (only `Roots` writes that flag, and unconditionally), the hero
  could move while showing the buff. This mirrors the design's accepted
  deviation from `Roots` and is not reachable in the Tengu fight, which has no
  `Roots`.
- **Cooldown ticks in `canUseAbility()`.** That is the natural "once per Warden
  action" hook. In the rare double-call path (`Hunting.act` →
  `handleUnreachableTarget` → recursive `act`) the cooldown could tick twice in
  one visible turn by one; it can only ever make the next Snare *later*, never
  earlier.
- **Arabic is carried verbatim from design §5**, which was drafted, not
  native-reviewed. It is present and non-empty for every key (parity gate), but
  I did not author or vet the Arabic myself beyond copying it exactly.
- **Package note (not a code change):** the Java sources live under
  `com.shatteredpixel.shatteredpixeldungeon`; only `applicationId` in
  `build.gradle` is `com.mohammedguejjim.atlaspixeldungeon`. I followed the
  actual source tree. No package or identifier was changed.

## 7. Phone-test checklist (design §7 test plan — for Mohammed, on device)

Run this on a fresh profile, at phone proportions, through the real UI path
(enter The Prison of Chains, fight the Chain Warden):

1. **Teaching use is reachable.** Drop the Warden to just under 3/4 HP while
   still in phase 1 and stand 2–5 tiles away with clear line of sight. Expect:
   a wind-up line, a warning log line, an extra teaching line and a hint line,
   the hanging chain, the `!` marker, the rattle, and one turn later the
   `chained` bind for a single turn.
2. **The counterplay works.** Repeat, but during the warning turn step behind a
   wall (break line of sight) or step away. Expect a visible snap, the
   `snapped` line, and no bind.
3. **Range honesty.** Same, but close to adjacency (distance 1) or run far
   (6+). Expect no Snare at all.
4. **Bind is movement-only.** While bound, confirm you can still attack, throw,
   use items, read scrolls and teleport. You should lose only one turn of
   walking.
5. **Flying frees you.** Trigger the Snare, then be airborne at resolution
   (Levitation / Feather Fall). Expect the `no_anchor` line and no bind.
6. **Phase 2 pacing.** In the arena, let the fight run: the Snare should appear
   every ~4 of the Warden's actions (3 under the bosses challenge), never twice
   in a row, never on the same action as a warp, and never before the opening
   bomb and shocker have both been used.
7. **Nothing else moved.** Confirm the other three abilities, the warps, the
   trap cells and the drops all look and behave exactly as they did before this
   change.
8. **Save mid-warning.** Save and reload exactly during the warning turn.
   Expect the fight to resume with no chain drawn, and the pending Snare to
   still land (or snap) on schedule.

I did **not** perform any of the above; it is the hand-off for the phone.
