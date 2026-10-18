# Deep Challenges — Design Specification (Slice B5.2)

**Document Status**: DESIGN-GATE approved candidate  
**Governing Rule**: `MISSION-V2-FINALE.md` Phase B (Slice B5.2 DESIGN-FIRST gate)  
**Date**: October 2026  

---

## 1. Context and Objective

Atlas Pixel Dungeon v1.2.0 introduced the Pacts of Atlas (8 dual-edged run modifiers chosen up to 3 at a time). Upstream Shattered Pixel Dungeon has featured 9 standard run challenges since its early days (Hostile Champions, Badder Bosses, On Diet, Faith Is My Armor, Pharmacophobia, Barren Land, Swarm Intelligence, Into Darkness, Forbidden Runes).

Roadmap item 2 (`ATLAS-AGENT-CONTEXT.md` Section 5) specifies:
> "v1.5.0 — Deep Challenges: 5 more challenges in the pact bitmask style, harsh by design, opt-in."

The goal of Slice B5.2 is to expand the opt-in challenge set with 5 new distinct challenges (bits 9 through 13 of the challenge bitmask, expanding total challenges from 9 to 14), providing hardcore players with fresh endurance and tactical trials without altering standard un-challenged runs.

---

## 2. Standing Canon & Design Constraints

1. **Halal Canon Compliance**:
   - King Atlas is a legendary ancient Mauritanian king and cartographer, not a deity. No gods, temples, prayers, or divine curses.
   - Challenges are framed strictly as geological hazards, deep kingdom collapse, ancient mineral hardness, and ambient mystical instability.
2. **Conservative Mechanics**:
   - No volatile or open-ended AI overhaul.
   - All mechanics build on existing, thoroughly tested game loops (turn counter, armor calculation, action time, monster death hooks, terrain trampled).
3. **Save Compatibility**:
   - Challenge bitmask is stored as a single integer (`Dungeon.challenges` / `SPDSettings.challenges`). Expanding from 9 bits (0..511) to 14 bits (0..16383) preserves 100% binary serialization compatibility with older saves and rankings.
4. **Bilingual Parity**:
   - All 5 challenges must ship with full English and Arabic titles and detailed tooltips.

---

## 3. The Five Deep Challenges

| Challenge ID | Bit Mask | Name (EN) | Name (AR) | Primary Mechanic |
|---|---|---|---|---|
| `CRUMBLING_DEPTHS` | `1 << 9` (512) | Crumbling Depths | الأعماق المتداعية | After 250 turns on a standard level, seismic tremors shake the ceiling, periodically alerting mobs or causing dust falls. |
| `BARREN_SOIL` | `1 << 10` (1024) | Arid Sands | الرمال القاحلة | Vegetation dries up: trampled plants drop no seeds, and high grass yields no dew. |
| `REINFORCED_FOES` | `1 << 11` (2048) | Armored Foes | أعداء مدرعون | Enemies scavenge ancient plates: all monsters gain +1/+2/+3 base armor damage reduction by chapter. |
| `UNSTABLE_MAGIC` | `1 << 12` (4096) | Unstable Leylines | خطوط طاقة مضطربة | Leyline interference slows magical action: scrolls and wands require 1.25 turns to cast/zap instead of 1.0 turn. |
| `RELENTLESS_SHADOWS` | `1 << 13` (8192) | Relentless Shadows | ظلال لا تلين | Slaying a non-undead enemy has a 25% chance to manifest a frail 1-HP shadow mirage at its death location. |

---

## 4. Design Decision Rationale

### Fork 1: Turn-limit vs soft enrage in Crumbling Depths
- *Option A*: Hard instant-death timer (instantly kill player after N turns).
- *Option B (Conservative)*: Soft enrage tremors after 250 turns that create audio/visual dust effects and wake distant sleeping mobs, preventing endless farming without creating arbitrary instant game-overs.
- **Selection**: Option B (Conservative).

### Fork 2: Armored Foes Scaling
- *Option A*: Percentage damage reduction (e.g. 50% flat damage cut).
- *Option B (Conservative)*: Flat armor DR bonus matching dungeon chapter (+1 in Cisterns/Prison, +2 in Caves/City, +3 in Halls). This directly tests player gear readiness and rewards precision attacks, wands, and physical penetration without making high-tier weapons useless.
- **Selection**: Option B (Conservative).

### Fork 3: Shadow Manifestations
- *Option A*: Resurrect the defeated monster at full health.
- *Option B (Conservative)*: 25% chance to spawn a single 1-HP `Wraith` (the engine's standard ephemeral spirit), identically to how bone piles or gravestones function.
- **Selection**: Option B (Conservative).

---

## 5. Implementation Architecture

1. `Challenges.java`:
   - Add constants: `CRUMBLING_DEPTHS = 512`, `BARREN_SOIL = 1024`, `REINFORCED_FOES = 2048`, `UNSTABLE_MAGIC = 4096`, `RELENTLESS_SHADOWS = 8192`.
   - Update `MAX_VALUE = 16383` and `MAX_CHALS = 14`.
   - Expand `NAME_IDS` and `MASKS` arrays.
2. `WndChallenges.java`:
   - Existing scroll and checkbox loop dynamically iterates `Challenges.NAME_IDS.length`, seamlessly displaying all 14 challenges.
3. Gameplay hooks:
   - `Char.drRoll()` / `Mob.drRoll()`: add chapter armor for `REINFORCED_FOES`.
   - `HighGrass.trample()`: suppress seed drop when `BARREN_SOIL` is active.
   - `Wand.execute()` / `Scroll.execute()`: apply 1.25x action delay when `UNSTABLE_MAGIC` is active.
   - `Mob.die()`: 25% chance to spawn Wraith for `RELENTLESS_SHADOWS`.
   - `Dungeon.java` / `Level.java`: track floor turns for `CRUMBLING_DEPTHS` tremors.
4. Messages:
   - Add EN and AR keys to `core/src/main/assets/messages/challenges/challenges.properties` and `challenges_ar.properties` (or `messages/challenges.properties`).

---

## 6. Verification Protocol

- Full compile with 0 errors before code commit.
- Verification of save compatibility across old saves.
- Arabic text verification for all new keys.
