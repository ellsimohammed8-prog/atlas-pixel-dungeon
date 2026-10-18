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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BattleAxe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.CedarCleaver;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Crownblade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EngineHammer;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Glaive;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Longsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MirelordsReach;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WarHammer;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

//Atlas Blueprints: alchemy recipes that evolve a weapon into a new form.
//The catalyst is a scroll of upgrade, and the evolved weapon preserves the
//base weapon's upgrade level, enchantment, augment and curse — the player's
//investment is never eaten by the evolution.
public class Blueprints {

	public static abstract class Evolution extends Recipe {

		protected Class<? extends MeleeWeapon> base;
		protected Class<? extends MeleeWeapon> evolved;

		public static final int ENERGY_COST = 8;

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			boolean hasBase = false;
			boolean hasScroll = false;
			for (Item i : ingredients) {
				if (i.getClass() == base && i.isIdentified()) hasBase = true;
				if (i.getClass() == ScrollOfUpgrade.class && i.isIdentified()) hasScroll = true;
			}
			return hasBase && hasScroll;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return ENERGY_COST;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;

			MeleeWeapon weapon = null;
			Item scroll = null;
			for (Item i : ingredients) {
				if (i.getClass() == base) weapon = (MeleeWeapon) i;
				if (i.getClass() == ScrollOfUpgrade.class) scroll = i;
			}
			if (weapon == null || scroll == null) return null;

			MeleeWeapon result = Reflection.newInstance(evolved);
			result.level(weapon.level());
			result.enchantment = weapon.enchantment;
			result.enchantment2 = weapon.enchantment2;
			result.hasDualEnchant = weapon.hasDualEnchant;
			result.augment = weapon.augment;
			result.cursed = weapon.cursed;
			result.cursedKnown = weapon.cursedKnown;
			result.identify(false);

			scroll.quantity(scroll.quantity() - 1);
			weapon.quantity(0);
			return result;
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			return Reflection.newInstance(evolved);
		}
	}

	public static class CrownbladeRecipe extends Evolution {
		{ base = Longsword.class; evolved = Crownblade.class; }
	}

	public static class EngineHammerRecipe extends Evolution {
		{ base = WarHammer.class; evolved = EngineHammer.class; }
	}

	public static class MirelordsReachRecipe extends Evolution {
		{ base = Glaive.class; evolved = MirelordsReach.class; }
	}

	public static class CedarCleaverRecipe extends Evolution {
		{ base = BattleAxe.class; evolved = CedarCleaver.class; }
	}

	// Dual Enchantment: Blueprint recipe to unlock and grant a second enchantment slot
	public static class DualEnchantRecipe extends Recipe {
		public static final int ENERGY_COST = 8;

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			boolean hasWeapon = false;
			boolean hasScroll = false;
			for (Item i : ingredients) {
				if (i instanceof MeleeWeapon && i.isUpgradable() && !((MeleeWeapon) i).hasDualEnchant && i.isIdentified()) {
					hasWeapon = true;
				}
				if (i.getClass() == ScrollOfUpgrade.class && i.isIdentified()) hasScroll = true;
			}
			return hasWeapon && hasScroll && ingredients.size() == 2;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return ENERGY_COST;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;

			MeleeWeapon weapon = null;
			Item scroll = null;
			for (Item i : ingredients) {
				if (i instanceof MeleeWeapon && !((MeleeWeapon) i).hasDualEnchant) weapon = (MeleeWeapon) i;
				if (i.getClass() == ScrollOfUpgrade.class) scroll = i;
			}
			if (weapon == null || scroll == null) return null;

			MeleeWeapon result = Reflection.newInstance(weapon.getClass());
			result.level(weapon.level());
			result.enchantment = weapon.enchantment;
			result.augment = weapon.augment;
			result.cursed = weapon.cursed;
			result.cursedKnown = weapon.cursedKnown;
			result.hasDualEnchant = true;
			if (result.enchantment == null) {
				result.enchant();
			}
			result.enchant2();
			result.identify(false);

			scroll.quantity(scroll.quantity() - 1);
			weapon.quantity(0);
			return result;
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			for (Item i : ingredients) {
				if (i instanceof MeleeWeapon && i.isUpgradable()) {
					MeleeWeapon sample = Reflection.newInstance(((MeleeWeapon) i).getClass());
					sample.hasDualEnchant = true;
					return sample;
				}
			}
			return new Longsword();
		}
	}
}
