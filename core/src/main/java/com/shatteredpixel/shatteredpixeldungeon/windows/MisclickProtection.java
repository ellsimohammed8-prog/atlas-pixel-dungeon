package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorrosion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

import java.util.HashSet;
import java.util.Set;

public class MisclickProtection {

	public static Shopkeeper findActiveShopkeeper() {
		if (Dungeon.level == null || Dungeon.level.mobs == null) {
			return null;
		}
		for (Mob m : Dungeon.level.mobs) {
			if (m instanceof Shopkeeper && m.isAlive()) {
				return (Shopkeeper) m;
			}
		}
		return null;
	}

	public static boolean isHarmfulSplashPotion(Item item) {
		if (!(item instanceof Potion)) {
			return false;
		}
		String name = item.getClass().getSimpleName();
		return name.contains("LiquidFlame")
				|| name.contains("ToxicGas")
				|| name.contains("ParalyticGas")
				|| name.contains("Frost")
				|| name.contains("CorrosiveGas")
				|| name.contains("SnapFreeze")
				|| name.contains("StormCloud")
				|| name.contains("CausticBrew")
				|| name.contains("DragonsBreath")
				|| name.contains("Blizzard")
				|| name.contains("Infernal");
	}

	public static boolean confirmAoEThrow(final Hero user, final Item item, final int target, final Callback onConfirm) {
		if (user == null || item == null || Dungeon.level == null) {
			return false;
		}

		int impact = item.throwPos(user, target);
		Set<Integer> affected = new HashSet<>();
		affected.add(impact);

		boolean isAoE = false;

		if (item instanceof Bomb) {
			isAoE = true;
			int range = 2;
			for (int i = 0; i < Dungeon.level.length(); i++) {
				if (Dungeon.level.distance(impact, i) <= range) {
					affected.add(i);
				}
			}
		} else if (isHarmfulSplashPotion(item)) {
			isAoE = true;
			for (int i : PathFinder.NEIGHBOURS9) {
				int c = impact + i;
				if (c >= 0 && c < Dungeon.level.length()) {
					affected.add(c);
				}
			}
		}

		return checkAndConfirm(affected, isAoE, onConfirm);
	}

	public static boolean confirmAoEWand(final Hero user, final Wand wand, final int target, final Callback onConfirm) {
		if (user == null || wand == null || Dungeon.level == null) {
			return false;
		}

		Ballistica bolt = new Ballistica(user.pos, target, wand.collisionProperties(target));
		int collisionPos = bolt.collisionPos;

		Set<Integer> affected = new HashSet<>();
		affected.add(collisionPos);

		boolean isAoE = false;

		if (wand instanceof WandOfFireblast) {
			isAoE = true;
			int maxDist = 9;
			ConeAOE cone = new ConeAOE(bolt, maxDist, 90f,
					Ballistica.STOP_TARGET | Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID);
			affected.addAll(cone.cells);
		} else if (wand instanceof WandOfBlastWave) {
			isAoE = true;
			for (int i : PathFinder.NEIGHBOURS9) {
				int c = collisionPos + i;
				if (c >= 0 && c < Dungeon.level.length()) {
					affected.add(c);
				}
			}
		} else if (wand instanceof WandOfCorrosion) {
			isAoE = true;
			for (int i : PathFinder.NEIGHBOURS9) {
				int c = collisionPos + i;
				if (c >= 0 && c < Dungeon.level.length()) {
					affected.add(c);
				}
			}
		} else if (wand instanceof WandOfLightning) {
			isAoE = true;
			for (int i = 0; i < Dungeon.level.length(); i++) {
				if (Dungeon.level.distance(collisionPos, i) <= 2) {
					affected.add(i);
				}
			}
		} else if (wand instanceof WandOfDisintegration) {
			isAoE = true;
			for (int c : bolt.subPath(1, bolt.dist)) {
				affected.add(c);
			}
		}

		return checkAndConfirm(affected, isAoE, onConfirm);
	}

	public static boolean checkAndConfirm(final Set<Integer> affectedCells, final boolean isAoE, final Callback onConfirm) {
		Shopkeeper shopkeeper = findActiveShopkeeper();
		boolean hitsShopkeeper = false;
		if (shopkeeper != null && affectedCells.contains(shopkeeper.pos)) {
			hitsShopkeeper = true;
		}

		boolean hitsTrap = false;
		if (isAoE) {
			for (int cell : affectedCells) {
				if (cell >= 0 && cell < Dungeon.level.length()) {
					Trap trap = Dungeon.level.traps.get(cell);
					if (trap != null && trap.visible && trap.active) {
						hitsTrap = true;
						break;
					}
				}
			}
		}

		if (hitsShopkeeper) {
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndOptions(
							Icons.get(Icons.WARNING),
							Messages.get(MisclickProtection.class, "shopkeeper_title"),
							Messages.get(MisclickProtection.class, "shopkeeper_body"),
							Messages.get(MisclickProtection.class, "yes"),
							Messages.get(MisclickProtection.class, "no")
					) {
						@Override
						protected void onSelect(int index) {
							if (index == 0 && onConfirm != null) {
								onConfirm.call();
							}
						}
					});
				}
			});
			return true;
		} else if (hitsTrap) {
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndOptions(
							Icons.get(Icons.WARNING),
							Messages.get(MisclickProtection.class, "trap_title"),
							Messages.get(MisclickProtection.class, "trap_body"),
							Messages.get(MisclickProtection.class, "yes"),
							Messages.get(MisclickProtection.class, "no")
					) {
						@Override
						protected void onSelect(int index) {
							if (index == 0 && onConfirm != null) {
								onConfirm.call();
							}
						}
					});
				}
			});
			return true;
		}

		return false;
	}

	public static void confirmStepTrap(final Hero hero, final int step) {
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show(new WndOptions(
						Icons.get(Icons.WARNING),
						Messages.get(MisclickProtection.class, "step_trap_title"),
						Messages.get(MisclickProtection.class, "step_trap_body"),
						Messages.get(MisclickProtection.class, "yes"),
						Messages.get(MisclickProtection.class, "no")
				) {
					@Override
					protected void onSelect(int index) {
						if (index == 0) {
							hero.trapConfirmed = true;
							hero.resume();
						}
					}
				});
			}
		});
	}
}
