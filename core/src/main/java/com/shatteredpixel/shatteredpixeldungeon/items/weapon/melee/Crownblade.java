/*
 * Atlas Pixel Dungeon
 * Copyright (C) 2026 Mohammed Guejjim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Atlas Pixel Dungeon is an edition built on Shattered Pixel Dungeon
 * by Evan Debenham, itself based on Pixel Dungeon by Watabou.
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CrownCombo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.StarMark;

//Atlas Blueprint evolution of the longsword.
//Star Mark: every 4th consecutive hit marks the enemy for 5 turns.
//Hits on a marked enemy deal +20% damage, and killing one restores satiety.
public class Crownblade extends Longsword {


	@Override
	public int proc(Char attacker, Char defender, int damage) {
		damage = super.proc(attacker, defender, damage);

		boolean marked = defender.buff(StarMark.class) != null;
		if (marked) {
			damage = Math.round(damage * 1.2f);
		}

		CrownCombo combo = Buff.affect(defender, CrownCombo.class);
		combo.hits++;
		Buff.prolong(defender, CrownCombo.class, CrownCombo.DURATION);
		if (combo.hits % 4 == 0) {
			Buff.prolong(defender, StarMark.class, StarMark.DURATION);
		}

		if (marked && attacker == Dungeon.hero
				&& damage >= defender.HP + defender.shielding()) {
			Hunger hunger = Dungeon.hero.buff(Hunger.class);
			if (hunger != null) hunger.satisfy(25f);
		}

		return damage;
	}
}
