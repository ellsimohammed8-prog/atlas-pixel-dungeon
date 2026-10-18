# The Sunken Deep — Endless Descent Specification (Slice B6.1)

**Document Status**: DESIGN-GATE approved candidate  
**Governing Rule**: `MISSION-V2-FINALE.md` Phase B (Slice B6.1 DESIGN-FIRST gate)  
**Date**: October 2026  

---

## 1. Concept and Lore Framing (Halal Canon)

In Atlas Pixel Dungeon, King Atlas was a legendary Mauritanian monarch and master cartographer, not a deity. No temples, worship, divine covenants, or mystical idols exist in the dungeon. The Sealed Darkness at floor 25 was an ancient malevolence imprisoned beneath the kingdom's foundations.

Beyond the chamber of the Crown of Atlas (floor 26) lie **The Sunken Deep** (*الأعماق الغارقة*):
- The drowned subterranean aquifers and deep root caverns underlying the deepest foundations of the buried kingdom.
- Once engineered by Atlas's architects as a massive hydraulic cistern network to siphon ancient desert water tables, now overgrown with ancient root masses and flooded chambers.
- Delving into the Sunken Deep represents an endless test of endurance, tactical mastery, and survival against ever-harder subterranean predators.

---

## 2. Standing Design Constraints

1. **Halal Canon Compliance**:
   - No gods, prayers, altars, or divine curses. The Sunken Deep is purely geological and hydrological: subterranean flooded aquifers, ancient roots, and mineral stone.
2. **Opt-in Player Agency**:
   - Players who defeat the Sealed Darkness and collect the Crown of Atlas can still claim standard victory or pursue ascension to floor 1. Endless descent is an explicit, unforced third choice.
3. **Rock-Solid Level Generation**:
   - Uses the battle-tested `RegularLevel` pipeline and `CavesPainter` with subterranean water features to ensure 100% reachable stairs and zero generation softlocks.
4. **Bilingual Parity**:
   - Full English and Arabic titles, prompts, buttons, and examine text.
5. **Save Compatibility**:
   - `SunkenDeepLevel` implements standard `Level` bundling. Saving and reloading at depth 27+ works identically to any standard dungeon floor.

---

## 3. Forks and Conservative Design Choices

### Fork 1: Entry Mechanism
- *Option A*: Irreversible plunge as soon as the Crown is taken.
- *Option B (Conservative)*: Explicit player choice. When `Statistics.amuletObtained` is true:
  1. In `AmuletScene.java`, a third button "Descend into the Deep" appears alongside "Call it a day" and "I'm not done yet".
  2. In `LastLevel.java` (floor 26), an archway north of the Crown altar opens with downward stairs (`Terrain.EXIT`) leading to depth 27.
- **Selection**: Option B (Conservative). Preserves all existing victory paths while making the Sunken Deep clearly accessible.

### Fork 2: Level Generation Architecture
- *Option A*: Complex new procedural generator with novel tile schemas.
- *Option B (Conservative)*: `SunkenDeepLevel` extending `RegularLevel`, utilizing `Assets.Environment.TILES_CAVES` and `Assets.Environment.WATER_CAVES`, submerged deep teal lighting (`0x0a2b2b`), subterranean mist, and standard room connections.
- **Selection**: Option B (Conservative). Guarantees stability, robust pathfinding, and zero memory leaks.

### Fork 3: Endless Enemy Scaling Curve
- *Option A*: Exponential scaling (doubling enemy HP/damage every floor).
- *Option B (Conservative)*: Progressive linear scaling: for each depth past 26 (`deepLevel = Dungeon.depth - 26`), enemies gain `+10%` health and `+8%` attack damage, while rotation features late-game elite mobs.
- **Selection**: Option B (Conservative). Rewards refined endgame builds and positioning rather than creating artificial instant-death walls.

---

## 4. Implementation Details

1. `SunkenDeepLevel.java`:
   - Extends `RegularLevel`.
   - Palette: dark aquifer teal (`color1 = 0x0c2828`, `color2 = 0x228888`), Caves tileset and water texture.
   - Standard entrance from `depth - 1` and downward stairs to `depth + 1`.
2. `Dungeon.java`:
   - In `newLevel()`: when `depth >= 27`, instantiate `new SunkenDeepLevel()`.
   - Level feelings and depth names properly resolve for depth 27+.
3. `LastLevel.java`:
   - Adds exit transition to depth 27 when `Statistics.amuletObtained` is true.
4. `AmuletScene.java`:
   - Adds descent option to delve directly into the Sunken Deep.
5. `Mob.java` & `MobSpawner.java`:
   - Mobs at depth 27+ receive progressive health and damage scaling.
   - Rotations spawn late-game challenges (Eyes, Scorpios, Succubi, Golems, Monks).
6. `Rankings.java` & `RankingsScene.java`:
   - Depth record displays exact depth attained in the Sunken Deep (e.g. "Depth 32").

---

## 5. Verification Protocol

- 0 compile errors before code commit.
- Verification of save/restore at depth >= 27.
- Full Arabic parity on all user-facing strings.
