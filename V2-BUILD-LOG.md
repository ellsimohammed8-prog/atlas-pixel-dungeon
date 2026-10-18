# Atlas Pixel Dungeon v2.0 Build Log

Branch: `agent/v2-finale`
Base commit: `964507a` (main, v1.3.2)

---

## Slice A0: Herald of Dawn

- **Status**: INTEGRATED
- **What changed**: Integrated `agent/herald-slice-1` (head `d54c43f`) introducing the Herald of Dawn third subclass for the Dawn Priest (Cleric), the Call Wisp rite, Wisp of Dawn ally, subclass talents, and EN/AR strings. Closed known gap by adding `BOSS_SLAIN_3_HERALD_OF_DAWN` to `Badges.Badge` enum and `Badges.thirdBossSubclassBadges` mapping.
- **Files touched**:
  - `core/src/main/assets/messages/actors/actors.properties`
  - `core/src/main/assets/messages/actors/actors_ar.properties`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/HeroClass.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/HeroSubClass.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Talent.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/spells/CallWisp.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/spells/ClericSpell.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/HeroIcon.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Badges.java`
- **Save compatibility**: Old saves load cleanly. HeroSubClass and Badge use enum serialization by name (`valueOf`), existing saves have their original subclasses and badges unchanged.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

---

## Slice A1: Liquid Gold (Ring Recycling)

- **Status**: INTEGRATED
- **What changed**: Integrated `agent/liquid-gold` (commit `97c6b13`) adding Liquid Gold alchemy recipe to melt spare rings into a reagent that upgrades another ring (capped at +3, cost = target level), preserving curse and identification state. Includes EN/AR strings, sprite, catalog entry, and alchemy recipes.
- **Files touched**:
  - `LIQUID-GOLD-REPORT.md`
  - `core/src/main/assets/messages/items/items.properties`
  - `core/src/main/assets/messages/items/items_ar.properties`
  - `core/src/main/assets/sprites/items.png`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/LiquidGold.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Recipe.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/rings/Ring.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Catalog.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/scenes/AlchemyScene.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ItemSpriteSheet.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/QuickRecipe.java`
- **Save compatibility**: Old saves load cleanly. LiquidGold is a standard item class, old saves containing standard items and rings are unaffected.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

---

## Slice A2: Chain Snare (Chain Warden Boss Mechanic)

- **Status**: INTEGRATED
- **What changed**: Integrated `agent/chain-warden` (commits `d3f2cc5`, `2ac429c`, `8bd0ffa`) adding the Chain Warden's Chain Snare ability (bind-only, telegraphed chain attack with counterplay, no damage, no reel) with full EN/AR string parity and design report.
- **Files touched**:
  - `CHAIN-WARDEN-DESIGN.md`
  - `CHAIN-WARDEN-REPORT.md`
  - `core/src/main/assets/messages/actors/actors.properties`
  - `core/src/main/assets/messages/actors/actors_ar.properties`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/Chained.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Tengu.java`
- **Save compatibility**: Old saves load cleanly. New fields on Tengu and the Chained buff use standard bundle serialization with backwards-compatible defaults if absent.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

---
