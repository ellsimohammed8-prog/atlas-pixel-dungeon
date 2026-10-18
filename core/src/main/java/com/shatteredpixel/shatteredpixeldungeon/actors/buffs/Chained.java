/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

//A chain thrown by the Chain Warden. It binds the target in place for a single turn
//using Char.rooted, so attacks, throwing, items, scrolls and every teleport still work.
public class Chained extends FlavourBuff {

	//the chain binds for exactly one turn
	public static final float DURATION = 1f;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	//whether this buff is the one that set Char.rooted. If some other source already
	//held the target, this buff must not clear the flag when it expires.
	private boolean setRooted = false;

	@Override
	public boolean attachTo( Char target ) {
		//a chain cannot anchor something off the ground
		if (target.flying) {
			return false;
		}
		if (super.attachTo( target )) {
			if (!target.rooted) {
				target.rooted = true;
				setRooted = true;
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void detach() {
		if (setRooted) {
			target.rooted = false;
		}
		super.detach();
	}

	@Override
	public boolean act() {
		//any effect that lifts the target frees them the moment it lands
		if (target.flying) detach();
		return super.act();
	}

	@Override
	public int icon() {
		return BuffIndicator.ROOTS;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	private static final String SET_ROOTED = "set_rooted";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SET_ROOTED, setRooted);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		setRooted = bundle.getBoolean(SET_ROOTED);
	}
}
