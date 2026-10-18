# Atlas Pixel Dungeon — Class Fixes Design Note (Batch 2)

**Status**: DESIGN-FIRST note for Mohammed's review & integration.  
**Governing Documents**: `ATLAS-AGENT-CONTEXT.md`, `V2-SPEC.md` (Batch 2), `MISSION-V2-FINALE.md` (Phase B).  
**Credited Forks**: Ideas and pacing data adapted from RKPD2, Rat King Adventure, and Polished PD.

---

## 1. Executive Summary & Objective

In community rankings and historical player data, three classes consistently struggled with early-game consistency and class identity:
1. **Atlas King (Warrior)**: High-challenge armor identity collapse due to 150-turn seal cooldown and immediate Berserker rage decay.
2. **Dawn Priest (Cleric)**: Sluggish Tome charge pacing (45 turns/charge) and inability to pin individual spells directly to quickslots.
3. **Sirocco Blade (Duelist)**: 60 turns per weapon charge in floors 1–10 with only 2 charges, making early survivability an RNG roll.

This design presents one conservative, balance-safe, code-anchored fix for each of the three classes.

---

## 2. Atlas King (Warrior): Seal Shield & Berserker Rage Retention

### Current Code Numbers
- In `BrokenSeal.java`:
  `COOLDOWN_START = 150;`  
  Recharging the seal shield takes 150 turns of movement without combat. In high challenges (e.g. Faith is My Armor, On Diet, Hostile Champions), a Warrior spends most of the floor with 0 shield.
- In `Berserk.java`:
  `powerLossBuffer = 3;`  
  `power -= GameMath.gate(0.1f, power, 1f) * 0.05f * Math.pow((target.HP / (float) target.HT), 2);`  
  Rage only gains from taking damage and begins decaying after just 2 steps of walking. Traversing a hallway between rooms completely resets rage to zero.

### Fork Evidence & Precedent
- RKPD2 and Polished PD addressed this by reducing seal recharge cooldown to 90–100 turns and extending rage loss buffer to cover room-to-room transitions.

### Conservative Specification
1. **BrokenSeal Shield Recharge**:
   Change `COOLDOWN_START` from `150` to `100` turns.
   Shield capacity formula (`3 + 2*armTier + IronWill`) is preserved. The 33% faster recharge ensures the seal is actually active across multiple engagements per floor.
2. **Berserker Rage Retention**:
   Change `powerLossBuffer` from `3` to `6` turns when damaged, giving the Berserker 5 turns of grace before decay begins.
   Reduce the base quadratic decay factor from `0.05f` to `0.03f`.
   This allows Berserker players to preserve built-up rage across adjacent rooms while preserving the normal non-combat decay over long distances.

---

## 3. Dawn Priest (Cleric): Spell Pinning & Tome Pacing

### Current Code Numbers
- In `HolyTome.java`:
  `float turnsToCharge = (45 - missing);`  
  With starting charge cap 3 and 0 charges, `missing = 3`, so charging 1 point requires 42 turns. For the Herald of Dawn subclass (Phase A), `Call Wisp` costs 3 charges (126 turns to replenish).
- Quickslots:
  `HolyTome` only supports setting a single `quickSpell` into `ActionIndicator`, which shares space with weapon abilities and other situational buttons. The 6 toolbar quickslots only accept items from `Belongings`, so individual spells cannot be placed directly on the quickslot bar.

### Fork Evidence & Precedent
- Upstream requests (#1033) and Rat King Adventure implemented spell-to-quickslot binding so spellcasters can access their repertoire with 1 tap.

### Conservative Specification
1. **Smoother Tome Pacing**:
   Update charge calculation in `HolyTome.TomeRecharge.act()`:
   `float turnsToCharge = (32f - missing * 0.5f);`  
   Recharge scales from ~30 turns at low charges to ~32 turns at high charges. This smoothly supports Herald of Dawn's wisp summons and baseline Cleric spells without overpowering Ring of Energy.
2. **Spell Pinning to Quickslots**:
   Introduce `HolyTome.SpellProxy extends Item`, a lightweight bundlable proxy item for each known `ClericSpell`.
   - Name and description match the spell.
   - `execute(hero, action)` invokes `spell.onCast(tome, hero)` directly, forwarding targeting if needed.
   - In `WndClericSpells`, adding a "Pin to Quickslot" option on long-click and right-click menu pins the spell proxy to an available quickslot.
   - Old saves load seamlessly: `QuickSlot` restores proxies via `HolyTome.SpellProxy`, with fallback to empty slot if tome is absent.

---

## 4. Sirocco Blade (Duelist): Early Weapon-Charge Pacing

### Current Code Numbers
- In `MeleeWeapon.java` (`Charger`):
  `float chargeToGain = 1/(60f-1.5f*(chargeCap()-charges));`  
  `chargeCap = Math.min(8, 2 + (Dungeon.hero.lvl - 1) / 3);`  
  At levels 1–3, the Duelist has a cap of 2 charges, each taking 57–60 turns to recharge. If the player does not find an early upgraded weapon, floors 1–10 are starved of weapon abilities.

### Fork Evidence & Precedent
- RKPD2 and Polished PD resolved the early Duelist lull by applying early floor-band charge boosts that taper off once Tier 3/4 weapons appear in the Caves.

### Conservative Specification
1. **Floor-Band Charge Pacing**:
   In `MeleeWeapon.Charger.act()`:
   ```java
   float baseTurns;
   if (Dungeon.depth <= 5) {
       baseTurns = 35f; // Sewers: 35 turns per charge
   } else if (Dungeon.depth <= 10) {
       baseTurns = 45f; // Prison: 45 turns per charge
   } else {
       baseTurns = 60f; // Caves+: standard 60 turns
   }
   float chargeToGain = 1 / (baseTurns - 1.5f * (chargeCap() - charges));
   ```
2. **Starting Charge Cap**:
   Adjust baseline charge cap slightly in `chargeCap()`:
   `return Math.min(8, 3 + (Dungeon.hero.lvl - 1) / 3);` (gives 3 charges from level 1 instead of 2).
   This guarantees that the Duelist can reliably use their signature weapon technique at least once per serious encounter in early floors without making late-game Champion/Monk overpowered.

---

## 5. Decision & Compatibility Matrix

| Class | Change | Save-Safe? | Backwards-Compatible? |
| --- | --- | --- | --- |
| Atlas King | `COOLDOWN_START` = 100, `powerLossBuffer` = 6, decay = 0.03 | Yes (pure logic constants) | Fully |
| Dawn Priest | Charge base = 32f, `SpellProxy` quickslot integration | Yes (`SpellProxy` is standard Bundlable Item) | Fully |
| Sirocco Blade | Early floor-band recharge (35f/45f/60f), base cap 3 | Yes (pure logic formula) | Fully |
