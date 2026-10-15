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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

//Atlas Blueprint evolution of the battle axe.
//Executioner's arc: +30% damage against enemies below 30% health,
//and a kill refunds half a turn.
public class CedarCleaver extends BattleAxe {


	@Override
	public int proc(Char attacker, Char defender, int damage) {
		damage = super.proc(attacker, defender, damage);

		if (defender.HP < defender.HT * 0.3f) {
			damage = Math.round(damage * 1.3f);
		}

		if (damage >= defender.HP + defender.shielding()
				&& attacker instanceof Hero) {
			((Hero) attacker).spend(-0.5f);
		}

		return damage;
	}
}
