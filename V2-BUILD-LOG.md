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
