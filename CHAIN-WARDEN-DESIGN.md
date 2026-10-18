# Chain Warden — signature chain mechanic (design note)

**Status: DESIGN ONLY — no code.** One file exists on this branch: `CHAIN-WARDEN-DESIGN.md`.
Nothing else was created, edited or moved.

| | |
| --- | --- |
| Mission | MISSION 1.2 — Chain Warden boss mechanic, design note only |
| Branch | `agent/chain-warden-design`, created from `main` @ `964507a` ("v1.3.2: news entry") |
| Version at base | `build.gradle`: `appVersionCode 932`, `appVersionName '1.3.2'` |
| Read first | `ATLAS-AGENT-CONTEXT.md` (v2, 2026-10-16) — supplied with the mission; it is not in the repo tree |
| Rule 2 (no agent files) | The mission names this one file as the deliverable. It is the product, not an agent-process file: no `AGENTS.md`, no brief/plan/prompt/trace files, no signatures, no co-author lines |

**The mechanic in one sentence.** In phase 2 the Chain Warden throws a chain that leaves his fist one
full turn before it lands (telegraph: log line + rattle + a chain drawn along the line to you); if you
are still in his reach and in his sight when it lands, you are **chained in place for one turn** — no
damage, movement only — and if you were at four or more tiles he **reels you two tiles closer** first.
(Phase 1 gets exactly one of these, as the teaching use.) A jailer's office, not a spell.

**Why this one and not five ideas.** The mission asks for one mechanic done well. The Warden's numbers
already tell a story — he is bad at melee (attack skill 10 adjacent, 20 at range, `Tengu.attackSkill`),
and his own AI insists on 5–7 tiles of distance every time he warps (`Tengu.jump`, arena branch). His
weakness is a hero who has closed in; his answer to that is not damage but *reach*. The chain is that
reach, and it is the one thing the fight does not yet have.

---

## 1. Research — how the fight actually works today

Everything below was read from this repo at the base commit. Where a number matters it is quoted with
its source. Nothing here is from memory of another version.

### 1.1 Where the fight lives

| File | Role |
| --- | --- |
| `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Tengu.java` | The boss: stats, phase logic in `damage()`, `jump()`, the ability engine, and the three abilities as **inner classes** (`BombAbility`, `FireAbility`, `ShockerAbility`) |
| `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/PrisonBossLevel.java` | The arena level: `State` machine, `progress()`, three map layouts, trap seeding, item carry-over |
| `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/levels/traps/TenguDartTrap.java` | The hidden poison dart trap the boss plants in the cell |
| `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/TenguSprite.java` | Animations (`idle`, `run`, `attack`, `zap`, `die`) and the shuriken throw |
| `core/src/main/assets/messages/actors/actors.properties` | EN strings (tengu block ≈ lines 1819–1837; `actors.buffs.roots` ≈ lines 391–393) |
| `core/src/main/assets/messages/actors/actors_ar.properties` | AR mirror (tengu name/desc at lines 45–46, ability + item names at 133–134 and 1534–1545, `actors.buffs.roots` at 451–453) |

### 1.2 The boss, by the numbers

From the initialiser block of `Tengu`:

- `HP = HT = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 250 : 200`
- `EXP = 20`, `defenseSkill = 15`, `viewDistance = 12`, `properties.add(Property.BOSS)`
- `damageRoll()` → `Random.NormalIntRange(6, 12)`
- `attackSkill(target)` → **10 if adjacent, otherwise 20** (the Warden is deliberately worse in melee)
- `drRoll()` → `super.drRoll() + Random.NormalIntRange(0, 5)`
- `immunities.add(...)` for `Roots`, `Blindness`, `Dread`, `Terror`
- `add(Buff)` refuses everything except `Doom` while the boss is out of `Actor.chars()` (i.e. removed
  from the level), guarded by the `loading` flag set in `restoreFromBundle`

The hero is assumed to be a floor-6–10 build. Depth-scaled damage in this codebase is built on
`Dungeon.scalingDepth()`, which is simply `Dungeon.depth` (26 during the ascension challenge), so at
depth 10 the numbers below are literal.

### 1.3 Phases, thresholds and the state machine

`PrisonBossLevel.State` = `START, FIGHT_START, FIGHT_PAUSE, FIGHT_ARENA, WON`, driven by
`PrisonBossLevel.progress()`. The boss reacts to damage in `Tengu.damage()` with `hpBracket = HT / 8`
(= **25** HP normally, **31** at 250 HP), and the rule *"cannot be hit through multiple brackets at a
time"* (HP is clamped to `(curbracket-1)*hpBracket + 1`).

1. **`START`** — hero and Warden stand in the cell (`tenguCell`), the door locks (`seal()`), the boss
   yells `notice_gotcha`, `BossHealthBar` takes over, `Statistics.qualifiedForBossChallengeBadge = true`.
   The transition runs from `PrisonBossLevel.occupyCell()` when the hero's `y > tenguCell.top`.
2. **`FIGHT_START` — phase 1, HP 200 → 100 (four brackets).** The Warden teleports inside the cell and
   plants traps. **No abilities at all**: `canUseAbility()` starts with `if (HP > HT/2) return false`.
3. **Phase 1 ends** when `state == FIGHT_START && HP <= HT/2`: HP is clamped to `HT/2`, the boss yells
   `interesting`, `progress()` runs, `BossHealthBar.bleed(true)` marks the bar. `progress()` clears the
   cell, rebuilds the map as the *pause* layout, removes the Warden from `mobs` (`Actor.remove(tengu)`),
   and moves to `FIGHT_PAUSE`.
4. **`FIGHT_PAUSE`** — the hero walks back north; `PrisonBossLevel.occupyCell()` fires `progress()` once
   the hero's `y <= startHallway.top + 1`. That builds the **arena** (`setMapArena()`), places the
   Warden at `(arena.left + arena.width()/2) + width()*(arena.top+2)` = **cell (10,3)**, and sets
   `FIGHT_ARENA`. The hero keeps the position they walked to, i.e. about `(9..12, 7..8)` — so the arena
   fight **opens at 4–5 tiles of separation** (Chebyshev, `Level.distance`).
5. **`FIGHT_ARENA` — phase 2, HP 100 → 0 (four more brackets).** Here the boss uses the three ranged
   abilities, warps at every bracket, and — important, see 1.6 — **there are no traps on this map**.
6. **Death** when `HP == 0 && state == FIGHT_ARENA`: `progress()` runs the `FIGHT_ARENA` case, the hero
   is moved to `(10,25)`, the map becomes the start map plus the exit art (`setMapEnd()`, `endMap`,
   `ExitVisual`/`ExitVisualWalls`), and `tengu.die(Dungeon.hero)` fires. The `TengusMask` drop,
   `GameScene.bossSlain()`, badges and `Statistics.bossScores[1] += 2000` are in `Tengu.die()`.

Every warp (`jump()`) and both phase-end handlers are scheduled one step late inside a `VFX_PRIO`
actor, so the current attack animation always finishes first.

### 1.4 `jump()` — what the warps actually do

- **In phase 1** (`state() == FIGHT_START`): `cleanTenguCell()`, then up to 100 tries for
  `randomTenguCellPos()` until the new cell is more than **3.5 true-distance** from both the enemy and
  the hero and unoccupied. Then `fill = 0.9f - 0.5f*((HP-(HT/2f))/(HT/2f))` and
  `placeTrapsInTenguCell(fill)` — so the trap patch is re-cut **on every warp**, growing from **40 %
  fill at full HP to 90 % fill at half HP**. The whole move is a puff (`Speck.WOOL`, `Assets.Sounds.PUFF`).
- **In the arena**: a random cell with Chebyshev distance from the enemy **and** from the hero in
  **[5, 7]**, distance from the previous position `>= 5`, not solid, unoccupied, no heap. `arenaJumps`
  increments up to **4**.
- **On any other level** (`randomRespawnCell`), used only if the boss is somehow fought elsewhere.

`arenaJumps` is not just flavour: `targetAbilityUses()` = `1 + 2*arenaJumps + max(0, arenaJumps-2)`,
i.e. **1, 3, 5, 8, 11** ability uses at 0–4 warps. The arena fight therefore has an *ability budget*,
not an endless stream.

### 1.5 The ability engine (phase 2 only)

- **Gate:** `canUseAbility()` — `HP > HT/2` → false; `abilitiesUsed >= targetAbilityUses()` → false;
  otherwise `abilityCooldown--`, then catch-up pacing: at least 4 uses behind → `abilityCooldown = 0`
  (and under `STRONGER_BOSSES`, no time is spent at all); at least 3 behind → forced to `1`; else
  `Random.IntRange(1, 4)`. Returns true only at `abilityCooldown == 0`.
- **Selection:** `useAbility()` — use #1 is always `BOMB_ABILITY` (0), use #2 is always
  `SHOCKER_ABILITY` (2), after that a random roll of `0/1/2` (`0/2` under `STRONGER_BOSSES`: the fire
  ability is never rolled with the challenge), with a **9/10 reroll if it repeats the last ability**.
  Under `STRONGER_BOSSES` a fire throw is *added on top* of whatever was used.
- **Cost:** `spend(TICK)` when four or more uses behind, otherwise `spend(2*TICK)` — an ability costs
  the Warden about two turns of tempo. The method returns `lastAbility == FIRE_ABILITY`, so the bomb
  and shocker paths return `false` and the fire path returns `true`.
- **AI entry points** (`Tengu.Hunting extends Mob.Hunting`): if the hero is in FOV, not charmed and
  `canAttack(enemy)` → `if (canUseAbility()) return useAbility();` else a normal attack. If the hero is
  not attackable, `handleUnreachableTarget()` re-chooses a target and *still* tries `canUseAbility()`,
  else `spend(TICK)`. `canAttack()` is `new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos`
  — i.e. **the boss already gates its attacks on clear line of sight**.

### 1.6 The three abilities, exactly

| Ability | Placement / delivery | Numbers | Bundle fields |
| --- | --- | --- | --- |
| **Bomb** (0) | Closest non-solid cell *adjacent to the hero* that has no bomb yet; thrown with a `MissileSprite` and `sprite.zap()` | Buff `BombAbility` with `timer = 3`: floating **"3…" / "2…" / "1…"** in warning colour, then a blast over every cell with `PathFinder.distance < 2` (a 3×3 block). Damage `Random.NormalIntRange(5 + scalingDepth, 10 + scalingDepth*2)` minus `drRoll()` → **15–30** at depth 10. Hero hit: `qualifiedForBossChallengeBadge = false`, `bossScores[1] -= 100` | `bomb_pos`, `timer` |
| **Fire** (1) | `Ballistica(thrower.pos, target.pos, WONT_STOP)`; only lands if the aim's first step is one of the eight `CIRCLE8` directions | `FireAbility` spreads a 3-tile-wide front one step per turn along that direction, seeding `FireBlob` (volume 2, `alwaysVisible`); anything caught burns (`Burning.reignite`), heaps burn, plants wither, flammable terrain is destroyed. Hero affected: badge + score penalty | `direction`, `cur_cells` |
| **Shocker** (2) | Closest cell adjacent to the hero that is `>= 2` tiles from the Warden and `>= 2` from any other shocker | `ShockerAbility` alternates its lightning between ordinals and diagonals every turn and seeds `ShockerBlob`; a char standing on a charge takes **`2 + scalingDepth` = 12** as `Electricity`, repeating every turn until it is destroyed. Hero killed: badge/score penalty, `Dungeon.fail`, `electricity.ondeath` | `shocker_pos`, `shocking_ordinals` |

Two things the player is *not* told by damage alone, and that matter for pacing: the bomb takes **three
full turns** from throw to blast, and a shocker keeps arcing on the tile it was thrown at until the
hero steps off it. Both punish a hero who cannot move for a turn.

### 1.7 The arena, as the player experiences it

- **Phase 1 map (`setMapStart`)**: 32×32. `tenguCell = Rect(6,23,15,32)` (inside walls spanning x 7–14,
  y 24–31), centre `(10,27)`, door `(10,23)`; entrance room `Rect(8,2,13,8)`; the hallway
  `Rect(9,7,12,24)`; four starting cells; seven torches; up to five cage decorations.
- **Trap seeding (`placeTrapsInTenguCell`)**: `Patch.generate(7, 7, fill, 0, false)` in a 7×7 window
  over the cell, redrawn until the hero's *walking* distance to the Warden lands between
  `ceil(7*fill)` and `ceil(4+4*fill)` (the code comments: 3–6 tiles of path at low fill, 7–8 at high
  fill). Each trap is `Terrain.SECRET_TRAP` + `new TenguDartTrap().hide()` (hidden, unsearchable,
  `canTarget` refuses the Warden). `TenguDartTrap.poisonAmount()` = **8 ("17 damage total")** normally
  and **15 ("50 damage total, equal to poison dart traps on floor 10")** under `STRONGER_BOSSES`. A
  `FadingTraps` overlay draws them faintly for `fadeDelay = 2f` so a fast player can read the field.
- **Phase 2 map (`setMapArena`)**: everything is `WALL` except `fillEllipse(arena, 1, EMPTY)` with
  `arena = Rect(3,1,18,16)` — a ~14×14 ellipse whose four corner regions are solid, i.e. the only
  line-of-sight breakers in the fight. `cleanMapState()` wipes blobs, traps, visited/mapped and the
  fading overlays. All mobs except allies are destroyed; items survive only inside
  `pauseSafeArea = Rect(9,2,12,12)`; returning boomerangs are cancelled into `storedItems` and dropped
  back at the end, minus the boss's own `BombItem`/`ShockerItem`.
- `viewDistance = 12` on both the level and the boss, so **the whole arena is visible** — a deliberate
  design choice that makes a fully telegraphed mechanic fair.
- **What the player is expected to do.** Phase 1: chase a teleporting target around a small room while
  reading a hidden trap field that gets denser as the boss weakens; damage wins by melee (his melee
  skill is 10) and the dart traps are the tax. Phase 2: close the gap after each warp (he leaves at
  5–7 tiles, so it costs 4–8 steps), keep track of a bomb counters, step off shocker tiles, and never
  stand still in the open. The fight is a movement test with a paper-thin margin for standing still.

### 1.8 The chain precedent already in this codebase

This matters for the mechanic and for the builder — there are two existing chains, and the new one
must not read as either of them.

- **`Guard.chain(int)` (`actors/mobs/Guard.java`)** — a Prison mob's *one-shot* chain: only while
  `chainsUsed == false`, only when the hero is not attackable and `Dungeon.level.distance(pos, enemy.pos) < 5`;
  requires `Ballistica.PROJECTILE` collision with the hero (no LOS, no chain); pulls the hero to the
  closest open cell along the path via `effects/Pushing.java` + `Chains` effect
  (`Effects.Type.CHAIN`, `Assets.Sounds.CHAINS`), then applies `Cripple.prolong(enemy, Cripple.class, 4f)`.
  Serialised as one boolean. **The difference the design must preserve:** the Warden's chain is not a
  one-off pull-and-limp, it is a repeatable, telegraphed *lockdown* with a warning turn.
- **`EtherealChains` (artifact, `items/artifacts/EtherealChains.java`)** — the hero's own chain:
  `chainEnemy()` / `chainLocation()`, `hero.busy()`, `Pushing`, `Effects.Type.ETHEREAL_CHAIN`, charge
  cost equal to the distance moved, refuses `Property.IMMOVABLE` targets and refuses to pull a
  `rooted` hero. That is the upstream precedent for *displacement as an effect*, and the reel below
  reuses its exact plumbing. Thematically it is a gift: the jailer's chain against the hero's chain.
- **Existing telegraph precedent:** `Tengu.BombAbility` itself — a `Buff` on the boss that counts down
  with `FloatingText.show(...)` and `spend(TICK)`, resolving in `act()`, fully serialised. The new
  wind-up copies this pattern; no new timing machinery is needed anywhere.
- **Existing lockdown precedent:** `actors/buffs/Roots.java` (`DURATION = 5f`) sets `Char.rooted` and
  refuses to attach to a flying target; `Levitation.attachTo` sets `flying = true` and calls
  `Roots.detach(target, Roots.class)`; `Hero.getCloser()` and `Hero.actTransition()` refuse to move a
  rooted hero. Chaining a hero should reuse `Char.rooted`, which means every existing rule (no
  movement, no swapping, attacks/items/teleport still fine) comes for free.

### 1.9 Three facts that changed the design

1. **Traps are a phase-1 phenomenon.** `placeTrapsInTenguCell()` is only called from `jump()`'s
   `FIGHT_START` branch, and `setMapArena()` replaces the whole map while `cleanMapState()` clears
   `traps`. **The arena has no traps at all.** A chain that exists only to shove the hero onto a trap
   would be dead content in phase 2; the reel below is therefore tied to the *bombs and shockers*,
   which are real phase-2 threats (a bomb centred on the hero's tile, a shocker arcing every turn).
2. **The boss's own AI wants distance** (`jump()` targets 5–7 tiles; melee skill 10 vs ranged 20). A
   mechanic whose only outcome is "the hero is now closer" would be working against the boss's design,
   so a full reel-to-melee is *not* the primary. A reel of two tiles is: it stays outside the band the
   Warden's own code keeps, while still being a genuine, visible loss of position for the hero.
3. **The fight opens at 4–5 tiles** (Warden at (10,3), hero around y 7–8). A 5-tile reach means the
   chain is a live threat from the first turns of phase 2 — which is exactly why the cooldown and the
   wind-up, not the range, are the fairness dials.

---

## 2. The mechanic — **the Chain Snare**

One mechanic: **a thrown chain that shackles the hero in place for one turn**, telegraphed for a full
turn before it lands. Internal name: *the Chain Snare*; the state it leaves on the hero is the
`chained` buff. **The drag appears only as a phase-2 variant** (the reel, §2.5).

### 2.1 Trigger conditions (all must hold)

| Condition | Value | Why |
| --- | --- | --- |
| Level state | `FIGHT_START` (teaching use only) or `FIGHT_ARENA` | Never in `START`, `FIGHT_PAUSE`, `WON`, or a non-prison level |
| Boss HP | `HP <= HT/2` for regular uses; the single teaching use also requires `HP <= 3*HT/4` | Inherits the existing "no abilities in phase 1" gate; the teaching use is deliberately not on the fight's first turn |
| Distance | Chebyshev `Dungeon.level.distance(pos, hero.pos)` **between 2 and 5 inclusive** | Line of sight is already the boss's attack gate; 5 is also the arena's opening distance, and 2 keeps the "get in his face" answer honest |
| Line of sight | `new Ballistica(pos, hero.pos, Ballistica.PROJECTILE).collisionPos == hero.pos` | Identical to `Tengu.canAttack()` and `Guard.chain()` — free, familiar, and the basis of counterplay #1 |
| Hero state | alive, not already `Chained`, not `Property.IMMOVABLE` | No refresh, no stacking, no absurd edge cases |
| Boss state | not mid-warp (no jump scheduled on this action), not immediately after `notice()` | Never on the same action as an HP-bracket warp or the opening yell |
| Cooldown | `chainCooldown == 0` and `lastAbility != CHAIN_ABILITY` | See §2.6 |

### 2.2 The telegraph (one full turn, always)

On the action the Snare is chosen, the Warden does nothing else. He spends a turn, and the following
happen **immediately, before the hero's next turn**:

1. `GLog.w` line — the mechanical warning, in the message file, so it is translatable
   (`actors.mobs.tengu$chainability.telegraph`).
2. `yell(...)` in the Warden's voice (`...chainability.yell_wind`, e.g. "Nowhere left to run, %s.").
3. **Visual:** a chain drawn from the Warden's sprite to the hero's tile, hanging there for the whole
   warning turn. Implementation: a small `Group` of `Image`s built exactly the way
   `effects/Chains.java` builds its links, from `Effects.get(Effects.Type.CHAIN)` — the existing
   chain texture in the effects atlas, **no new art** — but with a long, low-alpha life instead of the
   0.2 s + distance/320 snap of the normal effect. A `FloatingText.show(..., "!", CharSprite.WARNING)`
   marker sits on the hero's tile as the unambiguous "this is you" cue. Neither `effects/Chains.java`
   nor `Effects.java` has to change for this.
4. **Rattle:** `CellEmitter.get(hero.pos).burst(Speck.factory(Speck.RATTLE), 4)` and
   `Sample.INSTANCE.play(Assets.Sounds.CHAINS)` (`"sounds/chains.mp3"`, the same cue `Guard` and
   `EtherealChains` use).
5. The Warden's sprite plays its **existing** `zap` animation (`TenguSprite.zap = attack.clone()`), so
   the windup reads as him whipping the chain out.

The first Snare of the fight uses the same telegraph plus one extra teaching line
(`...chainability.telegraph_teaching`) and a follow-up `GLog.i` line naming the two counters. Same
mechanics, louder sign-posting: the mission's "teaching use" rule.

### 2.3 Resolution

Resolution happens on the wind-up buff's **first `act()`**, one full turn later, and it **re-verifies
everything** — no cached tiles, no delayed surprises:

- If the state is no longer `FIGHT_START`/`FIGHT_ARENA`, or the boss is not in `Dungeon.level.mobs`,
  the Snare is dropped silently (see §4.2).
- If line of sight, the 2–5 distance, or the "hero is not already chained" test now fails, the chain
  **snaps** visibly: `Chains` snap effect + `Assets.Sounds.CHAINS` + `...chainability.snapped`
  ("The chain lashes empty air."). No effect on the hero. This is the reward for the counterplay.
- Otherwise the chain connects: `Assets.Sounds.CHAINS`, the hero is displaced if the reel applies
  (§2.5), and `Buff.affect(hero, Chained.class, 1f)` is applied.

### 2.4 What `Chained` does, precisely

A new `actors/buffs/Chained.java`, modelled line-for-line on `Roots`:

- `type = buffType.NEGATIVE`, `announced = true`, `icon()` → `BuffIndicator.ROOTS` (11) — an existing
  icon, no new art (`ROOTS` already means "you cannot move", which is the whole truth of this buff).
  `PARALYSIS` (4) is deliberately **not** used: it advertises "all actions stop", which is false here.
- `attachTo`: if the target is flying → refuse (same rule as `Roots`, and the basis of counterplay #2). If
  `target.rooted` is already true from some other source, attach **without** touching the flag; only set
  `target.rooted = true` when this buff is the one that set it, and only clear it in `detach()` if this
  buff set it. (`Char.rooted` is written in exactly two places today, `Roots.attachTo` and
  `Roots.detach`, so this guard is cheap insurance, and this is a *deliberate* deviation from `Roots`,
  which would otherwise fight with it.)
- `act()`: `if (target.flying) detach();` then super — so any effect that lifts the hero (Levitation,
  Elixir of Feather Fall) **instantly frees them**, mirroring `Levitation.attachTo`'s
  `Roots.detach(...)` precedent without editing `Levitation.java`.
- `heromsg` is shown automatically by `Hero.add(Buff)`, so the player sees the buff line the moment it
  lands.
- Duration is exactly **1 turn** (`1f`), which with `Char.rooted` means: no walking, no swapping, no
  level transition — but **attacks, throwing, item use, reading scrolls and every teleport effect still
  work**, exactly as `Roots`' own description promises the player.
- **No damage**, no paralysis, no `Hero.interrupt()`, no stat changes, no `qualifiedForBossChallengeBadge`
  penalty, no `bossScores[1]` penalty. The chain cannot kill anyone; the *fight* can.

### 2.5 The phase-2 variant — the reel

In `FIGHT_ARENA`, if the hero was at **`distance >= 4`** when the chain connected, the Warden first
hauls them in: up to **2 tiles** along the chain's path toward him, landing on the furthest open,
unoccupied, non-solid cell within those two steps (`Ballistica` path, `Actor.findChar(cell) == null`,
`!Dungeon.level.solid[cell]`), moved with `effects/Pushing.java` and the full
`Dungeon.level.occupyCell / Dungeon.observe / GameScene.updateFog` sequence that `Guard.pullEnemy` and
`EtherealChains.chainLocation` already use. **Then** the bind lands. Never a full reel: two tiles from
5–7 keeps the Warden outside the range where his own attack skill degrades, and it can never deposit
the hero adjacent to him.

Why the drag is the *variant* and not the primary: the Warden's melee is his weak spot
(`attackSkill` 10 adjacent vs 20 at range) and his own `jump()` exists to hold 5–7 tiles. A 2-tile
displacement is a real loss for the hero (position, and possibly a landing on a shocker tile the hero
was about to step off) without handing the hero the fight's best position. The reel is the part of the
mechanic most likely to need phone-tuning, hence open question 3.

### 2.6 Cooldowns and phase interaction

The Snare is a **fourth ability id** (`CHAIN_ABILITY = 3`) so it shares the existing engine rather than
inventing a second clock. It is *not* part of the random `0/1/2` roll: when the eligibility test above
passes it is taken deterministically, which is why it needs its own throttle.

| Parameter | Phase 1 (FIGHT_START) | Phase 2 (FIGHT_ARENA) |
| --- | --- | --- |
| Uses per fight | **exactly one** (the teaching use), only if `HP <= 3*HT/4` | as the budget allows, see below |
| Dedicated cooldown | — | **4 Warden actions** (3 under `STRONGER_BOSSES`) between snares |
| Consecutive snares | never | never (`lastAbility != CHAIN_ABILITY`, and never while the hero already carries `Chained`) |
| Bind duration | 1 turn | 1 turn |
| Reel | none | 2 tiles when the hero is `>= 4` away |
| Binding strength | no damage | no damage |

Justification against the fight's real numbers:

- **Why one teaching use in phase 1.** `canUseAbility()` refuses everything in phase 1, so this is a
  deliberate, single exception, not a change to the phase. It costs the hero one turn of chase in a
  room where the only damage source is a trap they must walk onto, so it can teach without being able
  to kill. If the hero rushes the phase-transition before `HP <= 3*HT/4` (entirely possible — it is a
  100 HP window), the teaching use simply moves to the first arena snare, which is the same handler
  and the same extra log line. The "first use of the fight is the teaching use" rule therefore holds
  in every path.
- **Why 4 actions in phase 2.** An ability already costs the Warden `spend(2*TICK)`, and the whole
  arena fight has an ability budget of at most **11** uses (`targetAbilityUses()` at four warps). A
  4-action chain cooldown, on top of the expensive ability turn, lands the chain roughly **4–7 times
  across phase 2** — comparable to the bomb's share, and never enough for the mechanic to *become* the
  fight. Because it draws from the same `abilitiesUsed` budget, the Snare **does not add damage
  output**: it reshuffles the Warden's mix towards control. That is the intent of a signature mechanic
  on a boss that is already lethal enough.
- **Why 1 turn, no damage.** The bind's teeth are the *existing* threats: a bomb counts down for three
  turns centred next to the hero (15–30 at depth 10) and a shocker arcs for 12 every single turn. One
  turn of being unable to move while a shocker ticks, or while the third bomb tick passes, is the whole
  threat — and it is the hero's own board state doing it, which is honest. If the mechanic proves
  toothless on the phone, the first dial is the bind duration (1 → 2 turns), **not** damage: damage
  would let the chain kill, which is the one thing it must never do.
- **`STRONGER_BOSSES`.** 250 HP / 31 per bracket, faster ability cadence, `poisonAmount` 15, fire on top
  of every other throw. The chain gets one notch of extra cadence (4 → 3 actions) and nothing else. It
  never adds a second instance, and it still never repeats back-to-back.

### 2.7 What the player actually sees, turn by turn

```
Turn N     Warden: "Nowhere left to run." + "The Chain Warden winds a chain around his fist!"
           A chain hangs from his hands to your tile; your tile shows "!"; chains rattle.
           (First time only: "the chain catches you where you stand — break his sight, or close the gap.")
Turn N+1   You act normally. Step behind an edge wall, walk to 6+ tiles, or charge to 1 tile: the chain
           will snap. Stand still at 2-5 tiles in the open: it lands at the end of your turn.
           If it lands: "The chain snaps taut — you can't move!" (phase 2, from 4+ tiles: "The chain
           hauls you across the floor!" first.) You lose only movement for one turn; you can still hit
           him, throw, drink and teleport.
```

---

## 3. Counterplay

**1. Break his line of sight (positioning — the primary answer).** The Snare uses the same
`Ballistica.PROJECTILE` test as `Tengu.canAttack()` and `Guard.chain()`, so anything that makes
`collisionPos != hero.pos` makes the chain snap: stepping behind the arena's solid corner regions
(`setMapArena()` leaves the four corners of `Rect(3,1,18,16)` solid), or putting an edge in the way.
It is one step of work for the player and it is *learnable in one demonstration*.

**2. Get inside his reach, or outside it (positioning — the second answer).** The Snare only exists
between 2 and 5 tiles. At 1 tile his own numbers punish him (attack skill **10** adjacent against
**20** at range), and at 6+ the chain simply cannot reach. The arena opens at 4–5 tiles, so the hero
is *inside* the ring at the start of phase 2 and has to cross it deliberately: the correct play becomes
"close the gap in two moves, don't linger at three". This is the design's best property — it turns the
Warden's own "5–7 tile" warp habit into a measurable threat zone.

**3. Snap it with his own warp (damage tempo).** `Tengu.damage()` schedules `jump()` in a `VFX_PRIO`
actor the moment a hit crosses an HP bracket, and `VFX_PRIO` (100) runs **before** `BUFF_PRIO` (-30),
which is where the wind-up resolves. Land enough damage on the warning turn to cross one of the
25-HP brackets (31 under `STRONGER_BOSSES`) and the Warden warps out and back to 5–7 tiles, breaking
his own chain; the resolution code re-verifies and logs the snap. No extra code — the existing
priority order does it — and it rewards players who understand the bracket system. Build-dependent
(it needs ~25 damage in one turn), which is honest and will be stated in the test plan.

**4. Item answers (specific classes).**

- **Potion of Levitation** (`actors/buffs/Levitation`, `DURATION = 20f`): `Chained.attachTo` refuses a
  flying target, exactly as `Roots.attachTo` does, so a levitating hero cannot be anchored — and an
  *already-chained* hero is freed the instant the potion lands, because `Chained.act()` detaches on
  `target.flying`. The reel still connects (he is reeling, not anchoring): the potion converts the
  lockdown into a shove. This is deliberate — the arena has **no traps** (`setMapArena` +
  `cleanMapState`), so Levitation's only real use in this fight is against the chain, and a total
  immunity would make one potion trivialise the mechanic.
- **Scroll of Teleportation, Ethereal Chains, or any teleport effect:** "other actions are not
  affected, including teleportation effects" is the promise `Roots` already makes the player, and the
  bind preserves it. Being chained costs the turn, not the escape.
- **Ranged weapons and wands:** stand at 6+ tiles and shoot; the Snare cannot reach, and the reel needs
  a connect. The Huntress is naturally the best liar against this mechanic, which is fine — phase 1's
  hidden darts already favour her.
- **Potion of Haste:** cross the danger ring (2–5) in one turn instead of two, so the warning turn is
  always enough.

**Does the mechanic fail the counterplay test?** No, with one honest caveat. It has three
positional/tempo answers and four item answers, all of which are visible during the telegraph turn, and
none of which require a resource the player has no access to. The caveat: answer #3 is build-dependent,
and answer #2 requires the player to *know* the 2–5 band. That is why the teaching line names the
counters, and why every use logs the windup; the mechanic is meant to be learned in one look, and the
buff description repeats the rules to any player who taps it.

---

## 4. Feel and fairness rules

### 4.1 Hard rules (never break these)

1. **Never twice in a row.** `lastAbility != CHAIN_ABILITY` *and* the hero must not already carry
   `Chained`. No refresh, no stacking, no "second chain as the first lands".
2. **Never during the scripted transitions.** The three transitions are phase 1 → pause
   (`HP <= HT/2`, the `interesting` yell), pause → arena (the hero walking north into
   `startHallway.top+1`), and the death sequence (`HP == 0` in `FIGHT_ARENA`, which rebuilds the map
   and kills the boss). The wind-up **re-checks the state at resolution** and aborts, because the boss
   is removed from `Dungeon.level.mobs` during the pause transition while its buffs keep ticking. Abort
   silently (no chain visual, no log) — a visible chain resolving during a level rebuild would be a
   bug the player sees.
3. **The first use of the fight is always the teaching use** — full telegraph, plus the extra teaching
   line (and the follow-up counter line). Where it happens depends only on how the fight went; the
   handler is identical.
4. **One full turn of warning, every time, always visible.** The warning is emitted when the wind-up
   starts, never later; the resolution is the buff's first `act()`. The builder verifies on the phone
   that the log line and the chain are on screen before the hero's next input (see §7).
5. **No damage, no paralysis, no interrupts, no badge/score penalty.** The chain takes movement and one
   turn; nothing else. This is why a chained hero can still fight, and why the boss-challenge badge is
   not silently destroyed by a control mechanic.
6. **Never on the same action as a warp.** If `damage()` just scheduled a `jump()` (bracket crossed),
   the Warden does not start a wind-up on that action; a warp already in flight is never paired with a
   fresh chain.
7. **Snap, don't vanish.** A failed resolution always plays the snap effect and the snap line; a
   mechanic the player has been shown must visibly whiff, never just disappear.
8. **No chain in the opening beat.** The teaching use requires `HP <= 3*HT/4` and a resolved `notice()`
   first, so the hero's first read of the room is clean.
9. **The chain never moves the hero into anything illegal.** The reel only lands on non-solid,
   unoccupied cells inside the chain path, and never adjacent to the Warden.

### 4.2 Deliberate non-goals (so the builder does not invent them)

- Not a new phase, not a new ability *slot* in the random roll, not a second HP gate.
- No new arena, no arena changes, no trap changes, no changes to the boss's name, drops or the other
  phases (mission rule).
- No new art: the chain texture (`Effects.Type.CHAIN`), the `zap` animation, `Speck.RATTLE`, the
  `Chains` effect and `chains.mp3` all already exist.
- Not a damage mechanic; not a "the chain drags you into the fire" gimmick — the fire ability is
  already a spread effect and does not need help.
- No new is-this-a-deity framing anywhere. The Warden is a jailer using chains, his office; nothing is
  chanted, prayed to or blessed. The buff and the log lines are written that way.

---

## 5. Presentation — draft strings (EN + AR)

All keys live in the same two files that already hold the boss's strings:
`core/src/main/assets/messages/actors/actors.properties` and `.../actors_ar.properties`. Key naming
follows the existing nested-class convention (`actors.mobs.tengu$bombability$bombitem.name`). Every
English line below has its Arabic partner in the same change (context rule 8: no empty or
English-placeholder slots). These are drafts for the builder to wire; the Arabic is written to match
the vocabulary the Arabic file already uses for the same concepts — `سجّان السلاسل` for the Warden,
`لا تستطيع التحرك!` for "you can't move", and the `Roots` description wording for "locked in place".

| Key | EN | AR |
| --- | --- | --- |
| `actors.buffs.chained.name` | chained | مكبَّل |
| `actors.buffs.chained.heromsg` | The chain snaps taut — you can't move! | تشدّ السلسلة بإحكام — لا تستطيع التحرك! |
| `actors.buffs.chained.desc` | A chain thrown by the Chain Warden has snagged you and pinned you to the spot.\n\nA chain locks a target in place, making movement and most movement abilities impossible. Other actions are not affected, including teleportation effects.\n\nTurns of chain remaining: %s. | سلسلة قذفها سجّان السلاسل قد شبكت بك وثبّتك في مكانك.\n\nالسلسلة تقفل هدفا في مكانه، وتجعل الحركة ومعظم قدرات الحركة مستحيلة. الإجراءات الأخرى غير متأثرة، بما فيها آثار النقل الآني.\n\nأدوار السلسلة المتبقية: %s. |
| `actors.mobs.tengu$chainability.yell_wind` | Nowhere left to run, %s. | لا مكان للهرب يا %s. |
| `actors.mobs.tengu$chainability.telegraph` | The Chain Warden winds a chain around his fist! | سجّان السلاسل يلفّ سلسلة حول قبضته! |
| `actors.mobs.tengu$chainability.telegraph_teaching` | The Chain Warden winds a chain — step out of his reach! | سجّان السلاسل يلفّ سلسلة — اخرج من مداه! |
| `actors.mobs.tengu$chainability.teaching_hint` | A chain catches you where you stand. Break his line of sight, or close the gap. | السلسلة تُمسك بك حيث تقف. اقطع خط نظره، أو اقترب منه. |
| `actors.mobs.tengu$chainability.reeled` | The chain hauls you across the floor! | السلسلة تجرّك على الأرض! |
| `actors.mobs.tengu$chainability.snapped` | The chain lashes empty air. | السلسلة تضرب الهواء. |
| `actors.mobs.tengu$chainability.no_anchor` | The chain can't find purchase — you're off the ground! | السلسلة لا تجد ما تتعلّق به — أنت مرتفع عن الأرض! |

Notes for the builder: `snapped` is a `GLog.i` (neutral, like other "nothing happened" lines);
everything else is `GLog.w` or `yell(...)`. `heromsg` is displayed automatically by
`Hero.add(Buff)`, so it must not be repeated by hand. Keep the ability names inside the existing
`actors.mobs.tengu$chainability.*` namespace so nothing has to be registered elsewhere. No marketing
text: this is a design note, and nothing in-game mentions any language or "new" content
(context rules 6–8).

---

## 6. Implementation sketch (no code — responsibilities only)

### 6.1 Files the builder touches

| File | Touch |
| --- | --- |
| `actors/mobs/Tengu.java` | `CHAIN_ABILITY = 3`; new state `chainCooldown`, `chainUsed` (teaching flag), `pendingJump`; `boolean chainUsable()` (state + HP + distance + LOS + cooldown + no-repeat); a deterministic branch at the top of `useAbility()` before the random roll; new `public static boolean throwChain(final Char thrower, final Char target)` mirroring `throwBomb`/`throwShocker` (sprite `zap`, rattle, the long-lived chain node, `Buff.append(thrower, ChainAbility.class)`); new inner `public static class ChainAbility extends Buff` (the one-turn wind-up; `act()` resolves, mirroring `BombAbility`); set/clear `pendingJump` where `jump()` is scheduled in `damage()`; extend `storeInBundle`/`restoreFromBundle` |
| `actors/buffs/Chained.java` | **New.** `FlavourBuff`-based bind: one turn, `Char.rooted` with the "did I set it" guard, flying check on attach and in `act()`, `icon()` → `BuffIndicator.ROOTS`, `heromsg`, `desc` |
| `assets/messages/actors/actors.properties` + `actors_ar.properties` | The §5 strings, EN and AR in the same change |
| `sprites/TenguSprite.java` | **Probably not touched.** The wind-up reuses `zap`; add nothing unless playtest demands a distinct windup pose (and then it is an edit of existing frames, never new art) |

Explicitly **not** touched: `levels/PrisonBossLevel.java` (no map, trap, transition or geometry change),
`levels/traps/TenguDartTrap.java`, `effects/Chains.java`, `Effects.java`, `items/artifacts/EtherealChains.java`,
`actors/mobs/Guard.java`, `actors/buffs/Roots.java`, `actors/buffs/Levitation.java`.

### 6.2 How it hooks into the existing AI

`Tengu.Hunting.act()` calls `canUseAbility()` → `useAbility()` in exactly the two situations the chain
needs (the hero is attackable with LOS, or the hero is unreachable but the boss still wants to act).
Both are already LOS-gated by `canAttack()`, so the Snare slots into `useAbility()` as a fourth branch
without a new AI loop, without a new state machine, and without touching `Hunting`. The wind-up then
does the rest of the work off the `Buff` tick, exactly like the bomb's countdown.

### 6.3 State that must be serialised (boss fights save mid-fight)

| State | Where | Why / how |
| --- | --- | --- |
| `chain_cooldown` (int) | `Tengu.storeInBundle` / `restoreFromBundle` | Same treatment as the existing `ability_cooldown`, `abilities_used`, `arena_jumps`, `last_ability` |
| `chain_used` (boolean, the teaching flag) | same | Must survive a save so the fight never teaches twice and never skips teaching |
| wind-up in flight (`target_pos`, `timer`) | `ChainAbility.storeInBundle` / `restoreFromBundle` on the boss | Copy `BombAbility`'s `BOMB_POS`/`TIMER` exactly. Buffs are serialised with the char (`Char.storeInBundle`), and `PrisonBossLevel` stores the boss object itself in the `tengu` key across the pause, so this survives even a save taken mid-phase |
| the bind (`cooldown`) | `Chained` (inherited `Buff` serialisation) | Same mechanism `Roots` relies on; nothing custom |
| `pendingJump` (boolean) | optional; recomputed on load if omitted | Only a guard against starting a wind-up on the warp action |

Two serialisation rules for the builder (both learned from the existing code): the restored wind-up
must **not** resolve during `restoreFromBundle` (the bomb pattern only re-reads fields; the resolution
lives in `act()`), and `Tengu.restoreFromBundle` sets `loading = true` around `super` so that
`Tengu.add(Buff)` cannot reject restored buffs while the boss is out of the world.

### 6.4 Risks I foresee

1. **Frame order of the warning.** The design leans on `Actor` priorities (`VFX_PRIO` 100 > `HERO_PRIO`
   0 > `BUFF_PRIO` -30), which is what makes "the warp breaks the chain" and "the warning lands before
   the hero's next turn" true. This is reasoned from the source, not observed. The builder must verify
   on the phone that the log line and chain are visible before the hero's next input; if the warning
   ever lands *after* the hero's turn, bump the timer by one turn (that is the only knob needed).
2. **The pause transition removes the boss mid-fight.** The wind-up buff keeps ticking after
   `Actor.remove(tengu)`. Without the state guard the chain would resolve on a map that no longer
   exists. Non-negotiable: re-verify state + `Dungeon.level.mobs.contains(this)` at resolution.
3. **`useAbility()`'s return value.** It returns `lastAbility == FIRE_ABILITY`; the bomb and shocker
   paths return `false` and the fight relies on that. The new branch must behave like the bomb/shocker
   paths, and the builder should not "tidy" the return semantics as part of this work.
4. **Two sources of `Char.rooted`.** If a `Roots` buff were somehow present in the arena (an ally's
   effect, a debug path), a naive `Chained` would clear `rooted` when it expires and free the hero
   early. The guard in §2.4 prevents it; do not skip it.
5. **The reel is the riskiest piece.** Any displacement code must re-check the destination for solid
   terrain and other actors (a warp can happen between resolution and the pull), must not be able to
   move the hero onto the Warden, and must not produce a chasm fall (the arena has no pits, but the
   guard is cheap). If the reel is cut for scope, the primary mechanic is unaffected.
6. **Balance spread.** The bind's cost to the player depends on the board (a bomb already counting down,
   a live shocker). This will need the owner's phone, not arithmetic — expect one tuning pass on the
   cooldown and the reel distance, and keep both as single constants.
7. **Arabic review.** The AR drafts follow the file's existing vocabulary but were not reviewed by a
   native speaker; treat them as drafts to be checked at the same time as the EN strings.

---

## 7. Test plan — how the owner playtests this on a phone

Fresh profile, 720×1280, real entry points (Sewers → Prison → the Warden's cell). No console, no
harness; the mission's own gate is "the real path a new player would use".

**Runs (each one about 5–10 minutes):**

1. **Warrior (Atlas King), normal difficulty, one full fight.** Watch, in order:
   the cell fight (teaching use should fire once, well after the fight starts, and only after the HP bar
   has visibly bled to 3/4);
   the phase break (HP bar bleeds at half HP with the "Let's make this interesting" yell — **no chain
   may be in flight across this moment**);
   the walk back north (no chain at all in the pause);
   the arena entry (the chain may fire for the first time only after a few Warden actions);
   the kill.
2. **Warrior again, deliberately standing at 3 tiles in the open.** Does the telegraph give a full turn?
   Does the bind cost exactly one turn, and does hitting, throwing and drinking still work while chained?
   Does it ever chain twice in a row?
3. **Huntress (Cedar Huntress), ranged.** Fights from 6+ tiles. The chain must never reach; confirm the
   log stays quiet apart from the wind-up that then snaps. This is the "is the ring geometrically
   honest" run.
4. **Warrior with `STRONGER_BOSSES` (and any pacts/challenges already shipped).** Confirms the 3-action
   cooldown, 250 HP / 31-per-bracket behaviour, and that nothing double-fires when the challenge's fast
   cadence is in play. Also confirms the chain never appears while the hero already carries the bind.
5. **Phase-1 rush.** Kill phase 1 fast (before `HP <= 3*HT/4`). The teaching use must reappear — as the
   first arena chain, with the teaching line. This is the rule most likely to break.
6. **Save/quit twice:** once while the chain is hanging (after the windup, before the resolution) and
   once while chained. Reload: no chain may resolve on load, and the bind must have the right amount of
   turn left. Also save during the pause walk-back.
7. **Item answers:** Potion of Levitation before a windup (no bind, reel only) and *while* chained
   (freed immediately); Scroll of Teleportation while chained; Ethereal Chains while chained if the
   run provides it.

**What "feels wrong" would look like (owner's checklist):**

- The chain landing without a full turn of warning, or the warning being hard to read at 720×1280
  (chain line or log line too subtle, off-screen, or lost in the chat scroll).
- Chained for two turns, or chained twice back-to-back, or a chain on the same action as a warp.
- A chain in flight or landing during the phase change or the walk back.
- The reel landing the hero on the Warden, through a wall, or at 1 tile.
- Attacking, throwing, drinking or teleporting *blocked* by the chain (movement only).
- The chain feeling like free damage (`badge/score lost, or a death the player attributes to the chain
  rather than to the bomb/shocker they were standing in).
- The chain never firing at all in a fight (cooldown/eligibility too strict), or firing so often that
  the fight becomes "chain, chain, chain" (cooldown too loose).
- Anyone reading the mechanic as the Warden being "chosen" or empowered by something outside himself
  (canon: chains are his office, not worship).

**How balance would be judged.** Count chains per fight (target: 4–7 in phase 2, 1 in phase 1) and note
how many were *dodged* — if a competent player never gets caught, the ring is too small/obvious; if
they always get caught, the warning or the ring needs work before any number changes.

---

## 8. What I did not verify, and where I guessed

- **Build: not run.** This branch contains one Markdown file and no code; there is nothing to compile.
  No build claim is made.
- **Playtest: not done.** No emulator or device was used. Everything here is read from source at
  `main` @ `964507a`.
- **Guesses, labelled:**
  - The 2–5 tile reach, the 4-action (3 under `STRONGER_BOSSES`) cooldown, the 1-turn bind and the
    2-tile reel are my judgement calls, not measured values. They are chosen to sit around the facts in
    §1 (25-HP brackets, 3-turn bomb fuse, 12-per-turn shockers, 5–7-tile warps).
  - "4–7 chains across phase 2" is an estimate from the ability budget (`targetAbilityUses()` 1/3/5/8/11)
    plus the cooldown, not a simulation.
  - The claim that `VFX_PRIO` beats `BUFF_PRIO` in the same frame (so a bracket warp breaks a chain in
    flight) is read from `Actor`'s priority constants and the scheduling in `Tengu.damage()`; it is
    not observed behaviour.
  - The Arabic strings are drafts written to match the file's existing vocabulary; they have not been
    reviewed.
  - "The arena has no traps" is read from `setMapArena()` + `cleanMapState()` + the fact that
    `placeTrapsInTenguCell()` is only called from the `FIGHT_START` branch of `jump()`; a playtest
    should confirm it visually.
- **Reachability check (context rule 9):** the mechanic needs no new door — it appears inside the
  existing Prison of Chains boss fight, and its teaching happens in the fight itself. Nothing here is
  gated behind a first victory or an unlabelled icon.

---

## OPEN QUESTIONS (for the owner — max 3)

1. **Bind primary, or drag primary?** I recommend **bind primary with the 2-tile reel as the phase-2
   variant**, because the boss's own numbers say so: `attackSkill` is 10 adjacent and 20 at range, and
   `jump()` exists to hold 5–7 tiles, so "the hero is now next to me" is the one outcome that works
   against him. If you prefer drag-primary (chains as pure reeling, with the bind as the variant), the
   design flips cleanly — but the Warden then spends his signature ability making his own melee worse,
   and I would want the reel capped at two tiles so he never hands you adjacency for free.
2. **Should the Snare ever deal damage, or cost the boss-challenge badge?** My recommendation: **no to
   both** — no damage, no badge, no score. The threat comes from the bomb (15–30 at depth 10) and the
   shocker (12 a turn) already running while you cannot move: one lost turn is the whole mechanic. If it
   feels toothless on your phone, the first dial is bind duration (1 → 2 turns), not damage, and the
   second is the cooldown.
3. **Ship the phase-2 reel in the first cut?** The reel is the only part of the design that moves the
   hero (via the same `Pushing` machinery `Guard` and `EtherealChains` use) and it is the piece most
   likely to need tuning. Cutting it costs nothing structurally — the bind is the mechanic. Say the
   word and I can also hold it as a follow-up patch to keep the first cut as small as possible.
