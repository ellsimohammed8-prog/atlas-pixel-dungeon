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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

//Atlas Liquid Gold: an alchemy reagent made by melting a ring down, then poured
//back into a ring to raise its upgrade level. It is the ring answer to arcane
//resin. Liquid gold is produced and spent entirely through the alchemy pot, and
//can raise a ring to a maximum of +3. Melting destroys the ring for good.
public class LiquidGold extends Item {

	{
		image = ItemSpriteSheet.LIQUID_GOLD;

		stackable = true;

		defaultAction = AC_APPLY;

		bones = true;
	}

	private static final String AC_APPLY = "APPLY";

	//hard cap: liquid gold can never raise a ring above this level
	public static final int MAX_RING_LEVEL = 3;

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_APPLY );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals(AC_APPLY)) {

			curUser = hero;
			GameScene.selectItem( itemSelector );

		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 30*quantity();
	}

	//liquid gold needed to raise a ring from its current level to the next one.
	//the cost is always equal to the target level (+1 costs 1, +2 costs 2, +3 costs 3)
	public static int upgradeCost( Ring ring ){
		return ring.level() + 1;
	}

	//a ring may be raised while it is at +0, +1 or +2. +3 and beyond are refused.
	public static boolean canUpgrade( Ring ring ){
		return ring.level() >= 0 && ring.level() < MAX_RING_LEVEL;
	}

	//liquid gold produced by melting a ring: 1 for a +0 ring, +1 per upgrade level.
	//a cursed ring yields half of that, rounded down (minimum 0).
	public static int meltYield( Ring ring ){
		int yield = Math.max( 0, ring.level() + 1 );
		if (ring.cursed) yield /= 2;
		return yield;
	}

	//raises a ring's level directly, so its curse state and identification state
	//are preserved exactly (Ring.upgrade() can, by contrast, remove a curse)
	private static void raiseRing( Ring ring ){
		ring.level( ring.level() + 1 );
	}

	private void consume( int amount ){
		Catalog.countUses( LiquidGold.class, amount );
		if (amount < quantity()){
			quantity( quantity() - amount );
		} else {
			detachAll( Dungeon.hero.belongings.backpack );
		}
	}

	private final WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(LiquidGold.class, "prompt");
		}

		@Override
		public boolean itemSelectable( Item item ) {
			//the picker excludes rings that liquid gold cannot improve (unknown level,
			//or already at +3 or higher)
			return item instanceof Ring
					&& item.cursedKnown
					&& ((Ring)item).levelKnown
					&& canUpgrade( (Ring)item );
		}

		@Override
		public void onSelect( Item item ) {
			if (item != null && item instanceof Ring) {
				Ring ring = (Ring)item;

				if (!canUpgrade( ring )){
					GLog.w( Messages.get(LiquidGold.class, "level_too_high") );
					return;
				}

				int goldToUse = upgradeCost( ring );

				if (quantity() < goldToUse){
					GLog.w( Messages.get(LiquidGold.class, "not_enough") );
					return;
				}

				consume( goldToUse );
				raiseRing( ring );
				Item.updateQuickslot();

				curUser.sprite.operate( curUser.pos );
				Sample.INSTANCE.play( Assets.Sounds.TELEPORT );
				curUser.sprite.emitter().start( Speck.factory( Speck.UP ), 0.2f, 3 );

				curUser.spendAndNext( Actor.TICK );
				GLog.p( Messages.get(LiquidGold.class, "apply") );
			}
		}
	};

	//Melting recipe (alchemy pot): 1 ring (any) + 3 energy -> liquid gold.
	//The ring is destroyed permanently. Any ring may be melted, but its curse
	//state must be known so the yield can be shown before the player confirms.
	public static class MeltRecipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			return ingredients.size() == 1
					&& ingredients.get(0) instanceof Ring
					&& ingredients.get(0).cursedKnown;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return 3;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			Ring ring = (Ring)ingredients.get(0);
			Item result = new LiquidGold().quantity( meltYield( ring ) );
			ring.quantity( 0 );
			return result;
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			Ring ring = (Ring)ingredients.get(0);
			return new LiquidGold().quantity( meltYield( ring ) );
		}
	}

	//Application recipe (alchemy pot): 1 ring + liquid gold -> the same ring at +1.
	//Cost in liquid gold equals the target level. Rings at +3 or higher are refused,
	//and the recipe only matches when the stack holds enough liquid gold.
	public static class ApplyRingRecipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			if (ingredients.size() != 2) return false;

			Ring ring = null;
			LiquidGold gold = null;
			for (Item i : ingredients){
				if (i instanceof Ring) ring = (Ring)i;
				else if (i instanceof LiquidGold) gold = (LiquidGold)i;
			}

			if (ring == null || gold == null) return false;
			if (!ring.cursedKnown || !ring.levelKnown) return false;
			if (!canUpgrade( ring )) return false;

			return gold.quantity() >= upgradeCost( ring );
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			//the pot charges no energy for this recipe; the cost is paid in liquid gold
			return 0;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;

			Ring ring = null;
			LiquidGold gold = null;
			for (Item i : ingredients){
				if (i instanceof Ring) ring = (Ring)i;
				else if (i instanceof LiquidGold) gold = (LiquidGold)i;
			}
			if (ring == null || gold == null) return null;

			//mirrors Trinket.UpgradeTrinket: duplicate the input, consume it, upgrade the copy
			Ring result = (Ring)ring.duplicate();
			gold.quantity( gold.quantity() - upgradeCost( ring ) );
			ring.quantity( 0 );
			raiseRing( result );
			return result;
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			Ring ring = null;
			for (Item i : ingredients){
				if (i instanceof Ring) ring = (Ring)i;
			}
			if (ring == null) return null;

			Ring result = (Ring)ring.duplicate();
			raiseRing( result );
			result.quantity( 1 );
			return result;
		}
	}

}
