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

## Slice B1.3: Dual Enchantments via Blueprints

- **Status**: BUILT
- **What changed**: Added Dual Enchantment Blueprint recipe (`base weapon + Scroll of Upgrade + 8 energy`) preserving upgrade level, augment, curse, and existing enchantment. Dual-enchanted weapons proc both enchantments independently at 60% rate. Stones and Scrolls of Enchantment present a replace-or-fill prompt when used on a dual-enchant weapon. Curses on the second slot mirror slot one behavior for upgrades and identification. Full EN/AR string parity.
- **Files touched**:
  - `core/src/main/assets/messages/items/items.properties`
  - `core/src/main/assets/messages/items/items_ar.properties`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Blueprints.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Recipe.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/scrolls/exotic/ScrollOfEnchantment.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/stones/StoneOfEnchantment.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/Weapon.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/MeleeWeapon.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/QuickRecipe.java`
- **Save compatibility**: Old saves load cleanly. The new fields `enchantment2` and `hasDualEnchant` in `Weapon` serialize into Bundle tags. If absent when loading older saves, `enchantment2` restores as `null` and `hasDualEnchant` defaults to `false`.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

---

## Slice B2 (DESIGN-FIRST): Class Fixes Design Note

- **Status**: DESIGN-GATE
- **What changed**: Created `CLASS-FIXES-DESIGN.md` detailing conservative, code-argued fixes for Atlas King (seal shield cooldown 150 -> 100 turns, Berserker rage buffer 3 -> 6, decay 0.05 -> 0.03), Dawn Priest (HolyTome charge pacing 45 -> 32 turns, quickslot spell pinning via SpellProxy), and Sirocco Blade (floor-band weapon charge pacing: 35t sewers, 45t prison, 60t caves+, base charge cap 3). All fork ideas credited (RKPD2 / Rat King Adventure / Polished PD).
- **Files touched**:
  - `CLASS-FIXES-DESIGN.md`
- **Save compatibility**: Full compatibility; design covers constants, formulas, and Bundlable proxy items.
- **Compile result**: N/A (Documentation/Design commit before code).

---

## Slice B2.1: Atlas King Armor Seal and Berserker Pacing

- **Status**: BUILT
- **What changed**: Tuned BrokenSeal shield recharge cooldown from 150 to 100 turns in `BrokenSeal.WarriorShield`. In `Berserk`, extended rage loss buffer from 3 to 6 turns and reduced the base decay rate multiplier from 0.05f to 0.03f, preserving Berserker rage across adjacent rooms and sustaining the Warrior's armor identity under high challenges.
- **Files touched**:
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/Berserk.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/BrokenSeal.java`
- **Save compatibility**: Full compatibility. Only internal pacing constants and formulas modified; save structure and state bundles remain completely unchanged.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

---

## Slice B2.2: Dawn Priest Spell Pinning and Tome Charge Pacing

- **Status**: BUILT
- **What changed**: Smoothed HolyTome charge pacing in `HolyTome.TomeRecharge.act()` from `45 - missing` to `32f - missing * 0.5f` turns per charge, directly improving spell accessibility and pairing with Herald of Dawn's 3-charge Call Wisp. Added `HolyTome.SpellProxy` and updated `WndClericSpells` to allow pinning individual spells directly to toolbar quickslots on both mobile and desktop. Full EN/AR string parity.
- **Files touched**:
  - `core/src/main/assets/messages/items/items.properties`
  - `core/src/main/assets/messages/items/items_ar.properties`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Belongings.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/HolyTome.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndClericSpells.java`
- **Save compatibility**: Full compatibility. `SpellProxy` is a standard `Item` that serializes via `Bundle`. If absent in old saves, the proxy list initializes empty and populates on demand.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

---

## Slice B2.3: Sirocco Blade Weapon Charge Pacing

- **Status**: BUILT
- **What changed**: Tuned weapon charge pacing by floor band in `MeleeWeapon.Charger.act()`: 35 turns per charge in the Sunken Cisterns (depths 1-5), 45 turns in the Prison of Chains (depths 6-10), and standard 60 turns in Caves+ (depths 11+). Increased base baseline charge capacity from 2 to 3 charges in `chargeCap()` and initial `charges = 3`, giving the Duelist reliable weapon ability usage across early encounters without RNG starvation.
- **Files touched**:
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/MeleeWeapon.java`
- **Save compatibility**: Full compatibility. `Charger` restores `charges` and `partialCharge` from Bundle; older saves with 2 charges will charge up to the new cap of 3 naturally.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

---

## Slice B3.1: Assist / Casual Mode

- **Status**: BUILT
- **What changed**: Added optional unranked Assist / Casual Mode reachable from hero selection (via the top "Challenges & Pacts" / "[Casual]" entry point and Game Options pane). While active:
  - Hunger accumulates 50% slower, and starvation damage rate is softened.
  - Defeat offers a floor checkpoint respawn (`WndCasualRevive`): hero respawns at entrance with full health, cured negative buffs, and invulnerability grace, preserving full inventory and equipment. If defeated on a locked boss floor, the boss level is cleanly reset via `InterlevelScene.Mode.RESET`.
  - Badges and rankings are completely disabled during casual runs (`Badges.displayBadge`, `Badges.unlock`, and `Rankings.submit` return early). Ranked play is untouched.
  - Full bilingual EN/AR parity on all new strings.
- **Files touched**:
  - `core/src/main/assets/messages/scenes/scenes.properties`
  - `core/src/main/assets/messages/scenes/scenes_ar.properties`
  - `core/src/main/assets/messages/windows/windows.properties`
  - `core/src/main/assets/messages/windows/windows_ar.properties`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Badges.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Dungeon.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/GamesInProgress.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Rankings.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/SPDSettings.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs/Hunger.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/scenes/HeroSelectScene.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndCasualRevive.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndChallenges.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndGameInProgress.java`
- **Save compatibility**: Full compatibility. `casual_mode` boolean is stored in save bundles with a default fallback of `false`. Existing ranked runs load as normal ranked runs; casual runs resume cleanly with their unranked status intact.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

---

## Slice B3.2: Ungated Custom Seeds and Atlas Seed Phrases

- **Status**: BUILT
- **What changed**: Ungated custom dungeon seed selection on hero select, removing the prior victory requirement so all players can practice or explore specific seeds immediately. To preserve leaderboard integrity, seeded runs remain unranked until a normal victory badge is achieved. Created `WndSeedSelect` allowing players to enter arbitrary custom seed strings or select from 5 curated "Atlas Seed Phrases" with themed gameplay effects:
  - `CROWN OF ATLAS`: +150 starting gold.
  - `SUNKEN CISTERNS`: +2 health restored per consumed dewdrop.
  - `BURIED CITY`: +35% bonus gold picked up from all gold drops.
  - `DAWN OF ATLAS`: Starting backpack includes a bonus Torch and Sungrass seed.
  - `SEALED DARKNESS`: Torches burn 50% longer (150 turns).
- **Files touched**:
  - `core/src/main/assets/messages/misc/misc.properties`
  - `core/src/main/assets/messages/misc/misc_ar.properties`
  - `core/src/main/assets/messages/windows/windows.properties`
  - `core/src/main/assets/messages/windows/windows_ar.properties`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Rankings.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/HeroClass.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Dewdrop.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Gold.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Torch.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/scenes/HeroSelectScene.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/utils/AtlasSeeds.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndSeedSelect.java`
- **Save compatibility**: Full compatibility. Seed string is already part of standard Dungeon bundles (`Dungeon.customSeedText`). Seeds and modifiers trigger transiently or check the immutable active seed text; no changes to save bundle formats.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

---
