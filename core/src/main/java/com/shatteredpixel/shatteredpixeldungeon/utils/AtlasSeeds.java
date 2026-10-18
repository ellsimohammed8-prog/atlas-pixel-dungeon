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

package com.shatteredpixel.shatteredpixeldungeon.utils;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Torch;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

import java.util.Locale;

public class AtlasSeeds {

	public static final String CROWN_OF_ATLAS   = "CROWN OF ATLAS";
	public static final String SUNKEN_CISTERNS  = "SUNKEN CISTERNS";
	public static final String BURIED_CITY      = "BURIED CITY";
	public static final String DAWN_OF_ATLAS    = "DAWN OF ATLAS";
	public static final String SEALED_DARKNESS  = "SEALED DARKNESS";

	public static final String[] PRESETS = {
			CROWN_OF_ATLAS,
			SUNKEN_CISTERNS,
			BURIED_CITY,
			DAWN_OF_ATLAS,
			SEALED_DARKNESS
	};

	public static boolean isAtlasSeed( String seedText ) {
		if (seedText == null || seedText.isEmpty()) return false;
		String norm = seedText.trim().toUpperCase(Locale.ROOT);
		for (String preset : PRESETS) {
			if (norm.equals(preset)) return true;
		}
		return false;
	}

	public static void applyStartBonus( Hero hero ) {
		if (Dungeon.customSeedText == null || Dungeon.customSeedText.isEmpty()) return;
		String seed = Dungeon.customSeedText.trim().toUpperCase(Locale.ROOT);

		if (seed.equals(CROWN_OF_ATLAS)) {
			Dungeon.gold += 150;
			GLog.p(Messages.get(AtlasSeeds.class, "crown_bonus"));
		} else if (seed.equals(DAWN_OF_ATLAS)) {
			new Torch().collect();
			new Sungrass.Seed().collect();
			GLog.p(Messages.get(AtlasSeeds.class, "dawn_bonus"));
		}
	}

	public static float goldMultiplier() {
		if (Dungeon.customSeedText != null && Dungeon.customSeedText.trim().equalsIgnoreCase(BURIED_CITY)) {
			return 1.35f;
		}
		return 1f;
	}

	public static int dewHealingBonus() {
		if (Dungeon.customSeedText != null && Dungeon.customSeedText.trim().equalsIgnoreCase(SUNKEN_CISTERNS)) {
			return 2;
		}
		return 0;
	}

	public static float torchDurationMultiplier() {
		if (Dungeon.customSeedText != null && Dungeon.customSeedText.trim().equalsIgnoreCase(SEALED_DARKNESS)) {
			return 1.5f;
		}
		return 1f;
	}
}
