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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;

//Atlas Blueprint evolution of the glaive.
//Rooting strike: hits landed at maximum reach root the enemy for 1 turn.
public class MirelordsReach extends Glaive {


	@Override
	public int proc(Char attacker, Char defender, int damage) {
		damage = super.proc(attacker, defender, damage);

		if (Dungeon.level.distance(attacker.pos, defender.pos) == RCH) {
			Buff.prolong(defender, Roots.class, 1f);
		}

		return damage;
	}
}
