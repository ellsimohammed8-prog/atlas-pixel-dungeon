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

## Slice B3.3: Save Export and Import

- **Status**: BUILT
- **What changed**: Added comprehensive in-game backup capabilities allowing players to export and restore active dungeon runs and full profile progress (Badges, Rankings, Journal) via clipboard (Base64-encoded GZIP bundles) or local files (`atlas_run_backup.dat` and `atlas_profile_backup.dat`).
  - Added `SaveBackup` utility for container bundle creation and serialization of game files and level files.
  - Added `WndSaveBackup` window accessible via "Save Backup & Restore" button in Settings -> Connectivity & Data tab (`WndSettings.DataTab`).
  - Added 1-tap "Export" button in `WndGameInProgress` slot cards to copy any slot's active run to clipboard immediately.
  - Added `reloadGlobal()` to `Badges` and `Journal` so restored profile progress refreshes immediately in memory without restarting.
  - Full EN/AR bilingual parity on all strings.
- **Files touched**:
  - `core/src/main/assets/messages/windows/windows.properties`
  - `core/src/main/assets/messages/windows/windows_ar.properties`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/Badges.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Journal.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/utils/SaveBackup.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndGameInProgress.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndSaveBackup.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndSettings.java`
- **Save compatibility**: Full compatibility. Saves are read and written using standard `Bundle` and `FileUtils` routines matching the engine's exact internal formats. No changes to existing save formats or structures; all previous saves load cleanly.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

---

## Slice B4.1: Combat Stats Inspector

- **Status**: BUILT
- **What changed**: Added full combat transparency in inspection and status windows:
  - In `WndHero`'s `StatsTab`, added scrollable combat stats inspector displaying exact Accuracy (`hero.attackSkill(null)`), Evasion (`hero.defenseSkill(null)`), Armor Roll absorption range (`[minDr - maxDr]`), and Speed multiplier (`hero.speed() + "x"`).
  - In `WndHero`'s `BuffsTab` buff list, added remaining turn timers next to buff names (e.g. `(4.5t)`).
  - In `WndInfoBuff`, added explicit remaining turns description for active timed buffs.
  - In `WndInfoMob` (examine window for monsters/bosses), added dedicated Combat Stats block showing HP/HT, Accuracy, Evasion against hero, and Speed.
  - Full EN/AR bilingual parity on all strings.
- **Files touched**:
  - `core/src/main/assets/messages/windows/windows.properties`
  - `core/src/main/assets/messages/windows/windows_ar.properties`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndHero.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndInfoBuff.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndInfoMob.java`
- **Save compatibility**: Full compatibility. All changes are entirely UI and inspection formatting; no save file formats, Bundles, or character state serialization fields modified.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

---

## Slice B4.2: Item and Hero Renaming plus Talent Queue

- **Status**: BUILT
- **What changed**: Added item & hero customization and auto-progression queueing:
  - Added hero renaming support: `hero.customName` field and `name()` override with full serialization in `Hero.java`. Added rename button in `WndHero` `StatsTab` triggering `WndTextInput`.
  - Added item renaming support: `item.customName` field and `name()` override with full serialization in `Item.java`. Added rename button in `WndUseItem` allowing renaming of any equipment or inventory item.
  - Added talent pre-queueing: players can queue unmaxed talents in `TalentButton` / `WndInfoTalent` when currently out of points. Queued talents glow with a distinct cyan border (`0x44AAFF`) and auto-apply via `Hero.processTalentQueue()` immediately upon gaining points during level-up or talent boosts.
  - Full bilingual EN/AR parity on all new keys.
- **Files touched**:
  - `core/src/main/assets/messages/actors/actors.properties`
  - `core/src/main/assets/messages/actors/actors_ar.properties`
  - `core/src/main/assets/messages/windows/windows.properties`
  - `core/src/main/assets/messages/windows/windows_ar.properties`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Item.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/TalentButton.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndHero.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndUseItem.java`
- **Save compatibility**: Full compatibility. `custom_name` and `queued_talents` bundle tags are optional and default to `null` if absent when restoring older saves.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

## Slice B4.3: Misclick Protection and Thrown-Weapon Stacking

- **Status**: BUILT
- **What changed**: Added comprehensive misclick safeguards and stacked identical thrown weapons:
  - Created `MisclickProtection.java` to detect dangerous AoE or direct hits against the Shopkeeper and active known traps before execution.
  - Added Shopkeeper and known trap AoE confirmation dialogs for throwing harmful potions/bombs and zapping AoE wands (Fireblast cone, Blast Wave radius, Corrosion gas, Lightning arc, Disintegration beam).
  - Added misclick trap step protection in `Hero.java`: intercepting hero movement before stepping onto a known active trap, showing a confirmation prompt (`MisclickProtection.confirmStepTrap`), resuming only if explicitly confirmed.
  - Allowed identical thrown weapons (matching class, level, curse, and enchantment) to merge into a single inventory stack regardless of setID, removing the artificial clamp to `defaultQuantity()` upon merging.
  - Full bilingual EN/AR parity on all new keys.
- **Files touched**:
  - `core/src/main/assets/messages/windows/windows.properties`
  - `core/src/main/assets/messages/windows/windows_ar.properties`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Item.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/wands/Wand.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/missiles/MissileWeapon.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/MisclickProtection.java`
- **Save compatibility**: Full compatibility. Thrown weapon bundles already serialize standard quantity and durability floats; existing saves load without alteration.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

---

## Slice B4.4: Catalog Completeness, Quest Counters, Pouch Routing, and Quickslot Retention

- **Status**: BUILT
- **What changed**:
  - Verified catalog completeness: Trinkets and Missile weapons were already integrated into `Catalog.equipmentCatalogs` in our base and properly discoverable.
  - Added live quest progress counters to journal landmark titles and descriptions in `Notes.LandmarkRecord` for Sad Ghost (0/1 vs 1/1), Old Wandmaker (0/1 vs 1/1 depending on quest item possession), Troll Blacksmith (Dark Gold quantity counter or completed), and Ambitious Imp (Dwarf Token counter x/5 or completed).
  - Added inventory auto-routing on grass trampling in `HighGrass.java`: Dewdrops auto-route directly into the hero's Waterskin if present and not full (or consume if hero is hurt); Seeds and Runestones auto-route directly into the Velvet Pouch via `hero.belongings.backpack` even when main general slots are filled.
  - Added direct Waterskin routing override in `Dewdrop.collect(Bag container)`.
  - Added quickslot preservation in `AlchemyScene.java`: tracked `inputQuickslots` when ingredients are placed into the alchemy pot (via guide, bag selection, or quick recipe populate); restored assigned quickslots if items are cancelled, cleared, or clicked back into inventory; transferred the primary ingredient's quickslot assignment to crafted output items.
  - Full bilingual EN/AR parity in `messages/journal/journal.properties` and `journal_ar.properties`.
- **Files touched**:
  - `core/src/main/assets/messages/journal/journal.properties`
  - `core/src/main/assets/messages/journal/journal_ar.properties`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/Ghost.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/Wandmaker.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Dewdrop.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/journal/Notes.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/features/HighGrass.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/scenes/AlchemyScene.java`
- **Save compatibility**: Full compatibility. No serialized variables modified; runtime routing and ephemeral scene quickslot tracking only.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

---

## Slice B4.5: Accessibility and Trap-Trinket Tunes

- **Status**: BUILT
- **What changed**:
  - Widened brightness and contrast accessibility range: expanded brightness settings in `SPDSettings.java`, `WndSettings.java`, and `FogOfWar.java` from 3 steps (`[-1, 1]`) to 5 steps (`[-2, 2]`), providing extra-dark high-contrast and extra-bright visibility modes for low-visibility environments and diverse displays.
  - Softened Salt Cube health regen penalty in `SaltCube.java`: adjusted health regen multiplier across levels 0 to 3 from `0.84/0.73/0.66/0.60` (-16% to -40%) to `0.92/0.85/0.80/0.75` (-8% to -25%), preserving its hunger-slowing identity without crippling health recovery on challenge runs.
  - Softened Chaotic Censer gas hazards in `ChaoticCenser.java`: increased minimum spawn distance from hero (3-7 tiles instead of 2-6 tiles) to prevent unavoidable engulfment; reduced common gas volume from 300 to 180 and corrosive gas from 200 to 130; rebalanced gas category chances (increasing utility/beneficial gases like storm cloud, smoke screen, and regrowth to 25%-35% and reducing harmful common gases).
- **Files touched**:
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/SPDSettings.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/trinkets/ChaoticCenser.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/trinkets/SaltCube.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/tiles/FogOfWar.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndSettings.java`
- **Save compatibility**: Full compatibility. Brightness setting is clamped safely with `Math.max/min`, existing saves with older values adapt cleanly, and trinket tuning is purely algorithmic.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

---

## Slice B4.6: Enemy Counterplay and Artifact Usability

- **Status**: BUILT
- **What changed**:
  - Improved Alchemist's Toolkit usability: allowed brewing and energizing directly from inventory (`actions()` and `execute()` in `AlchemistsToolkit.java`) without needing to equip the artifact into an accessory slot and wait for warm-up.
  - Cave Spinner counterplay in `Spinner.java`: reduced attack poison duration from 7-8 turns to 5-6 turns; reduced web volume from 20 to 12; increased web shooting cooldown from 10 to 12 turns.
  - Sewer Crab counterplay in `Crab.java`: adjusted base movement speed from 2.0x to 1.75x; adjusted armor roll from 0..4 to 0..3 to prevent complete damage deflection against tier-1 starter weapons in early sewer floors.
  - Scorpio counterplay in `Scorpio.java`: reduced cripple duration from 5.0 turns to 3.0 turns on attack; lowered view distance from 8 to 7 tiles; adjusted damage roll from 30..40 to 26..36 to prevent unpreventable off-screen kiting wipes.
- **Files touched**:
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Crab.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Scorpio.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Spinner.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/AlchemistsToolkit.java`
- **Save compatibility**: Full compatibility. All monster adjustments and artifact execution changes operate purely on standard in-memory actor stats and action handlers.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

---

## Slice B5.1: Tier-4 Talents

- **Status**: BUILT
- **What changed**:
  - Added 12 new class-specific Tier-4 capstone talents (2 for each hero class, max 4 points each) expanding high-level progression choices for levels 21–30 beyond fixed stat growth:
    - **Atlas King (Warrior)**: `VANGUARD_BULWARK` (+2/+4/+6/+8 base armor and Broken Seal shield generates 25-100% faster), `CRUSHING_FORCE` (+2/+4/+6/+8 melee weapon bonus damage and 15-60% chance to stagger/paralyze adjacent targets).
    - **Star Sage (Mage)**: `ASTRAL_FOCUS` (+1/+2/+3/+4 effective wand level and temporary Barrier shield up to 4-16), `ARCANE_HARVEST` (25-100% chance on kill to restore 1 wand or staff charge and grant astral Mind Vision).
    - **Dune Shadow (Rogue)**: `PHANTOM_STRIDE` (+25-100% evasion and +10-40% movement speed while invisible or freshly emerged from stealth), `DESERT_STRIKE` (+15-60% surprise attack damage and surprise kills restore 0.25-1.00 charge to Cloak of Shadows).
    - **Cedar Huntress (Huntress)**: `FOREST_SANCTUARY` (+2/+4/+6/+8 armor while standing in grass or vegetation), `PINPOINT_ACCURACY` (+12-48% ranged attack damage against targets 3+ tiles away with 25-100% chance to cripple).
    - **Sirocco Blade (Duelist)**: `TEMPEST_COMBO` (+10-40% accuracy and +8-32% attack speed for 2 turns on strike), `WEAPON_MASTERY` (+15-60% weapon ability recharge speed and defeating formidable enemies restores 1 weapon charge).
    - **Dawn Priest (Cleric)**: `DIVINE_ILLUMINATION` (reveals invisible and stealth foes within 4 tiles on casting holy spells), `DAWN_RESTORATION` (recovers +2/+4/+6/+8 HP whenever casting a Tome spell).
  - Multi-row layout support in `TalentsPane.TalentTierPane.layout()`: gracefully renders 2 balanced rows of buttons when tier buttons exceed 5, ensuring zero overlap or clipping on all screen sizes.
  - Full EN + AR parity across all 12 talents in `actors.properties` and `actors_ar.properties`.
- **Files touched**:
  - `core/src/main/assets/messages/actors/actors.properties`
  - `core/src/main/assets/messages/actors/actors_ar.properties`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Talent.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/spells/ClericSpell.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Mob.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/BrokenSeal.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/artifacts/HolyTome.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/wands/Wand.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/MeleeWeapon.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/weapon/missiles/MissileWeapon.java`
  - `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/TalentsPane.java`
  - `V2-BUILD-LOG.md`
- **Save compatibility**: Full compatibility. Talents use standard bundle restoration; old saves with armor ability talents retain all spent points, and unspent level 21-30 points can be allocated to the new talents immediately upon loading.
- **Compile result**: BUILD SUCCESSFUL, 0 errors.

---
