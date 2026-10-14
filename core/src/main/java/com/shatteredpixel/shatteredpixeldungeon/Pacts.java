/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Atlas Pixel Dungeon
 * Copyright (C) 2026 Mohammed Guejjim
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

package com.shatteredpixel.shatteredpixeldungeon;

//Atlas Pixel Dungeon: run modifiers. Each pact grants a boon and exacts a price.
//Pacts are stored in a bitmask parallel to Challenges and are fully opt-in.
public class Pacts {

	public static final int OPEN_MAP		= 1;
	public static final int BURIED_HOARD	= 2;
	public static final int THORNS			= 4;
	public static final int HUNGRY_DARK		= 8;
	public static final int GLASS			= 16;
	public static final int CARTOGRAPHER	= 32;
	public static final int IRON_PATIENCE	= 64;
	public static final int GAMBLER			= 128;

	public static final int MAX_VALUE		= 255;
	public static final int MAX_PACTS		= 3;

	public static final String[] NAME_IDS = {
			"open_map",
			"buried_hoard",
			"thorns",
			"hungry_dark",
			"glass",
			"cartographer",
			"iron_patience",
			"gambler"
	};

	public static final int[] MASKS = {
			OPEN_MAP, BURIED_HOARD, THORNS, HUNGRY_DARK, GLASS, CARTOGRAPHER, IRON_PATIENCE, GAMBLER
	};

	public static int activePacts(){
		return activePacts(Dungeon.pacts);
	}

	public static int activePacts(int mask){
		int count = 0;
		for (int p : MASKS){
			if ((mask & p) != 0) count++;
		}
		return count;
	}

}
