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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

//Atlas Blueprint evolution of the war hammer.
//Shockwave: hits have a 20% chance to also strike enemies adjacent
//to the target for 50% of the hit's damage.
public class EngineHammer extends WarHammer {


	@Override
	public int proc(Char attacker, Char defender, int damage) {
		damage = super.proc(attacker, defender, damage);

		if (Random.Float() < 0.20f) {
			for (int offset : PathFinder.NEIGHBOURS8) {
				int cell = defender.pos + offset;
				if (!Dungeon.level.insideMap(cell)) continue;
				Char ch = Actor.findChar(cell);
				if (ch != null && ch != defender && ch.isAlive()
						&& ch.alignment == Char.Alignment.ENEMY) {
					ch.damage(Math.round(damage * 0.5f), attacker);
				}
			}
		}

		return damage;
	}
}
