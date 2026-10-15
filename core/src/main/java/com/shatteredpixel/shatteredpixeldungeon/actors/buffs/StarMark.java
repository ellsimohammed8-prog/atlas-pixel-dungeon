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

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

//Atlas: the mark left by the Crownblade of Atlas. Marked enemies take
//increased damage from the Crownblade, and killing one restores satiety.
public class StarMark extends FlavourBuff {

	public static final float DURATION = 5f;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	@Override
	public int icon() {
		return BuffIndicator.INVERT_MARK;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(1f, 0.75f, 0.15f);
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	@Override
	public void fx(boolean on) {
		if (on) target.sprite.add(CharSprite.State.MARKED);
		else target.sprite.remove(CharSprite.State.MARKED);
	}
}
