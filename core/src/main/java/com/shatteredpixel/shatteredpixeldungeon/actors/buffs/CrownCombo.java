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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

//Atlas: invisible tracker for the Crownblade's consecutive hits on one enemy.
//Fades quickly if the wielder stops pressing the same target.
public class CrownCombo extends FlavourBuff {

	public static final float DURATION = 2f;

	public int hits = 0;

	{
		type = buffType.NEUTRAL;
		announced = false;
	}

	@Override
	public int icon() {
		return BuffIndicator.NONE;
	}

	private static final String HITS = "hits";

	@Override
	public void storeInBundle(com.watabou.utils.Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(HITS, hits);
	}

	@Override
	public void restoreFromBundle(com.watabou.utils.Bundle bundle) {
		super.restoreFromBundle(bundle);
		hits = bundle.getInt(HITS);
	}
}
