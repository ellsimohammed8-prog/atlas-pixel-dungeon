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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DirectableAlly;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrystalWispSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CallWisp extends ClericSpell {

	public static final CallWisp INSTANCE = new CallWisp();

	//slice 1 balance block: every wisp number lives here. Conservative on purpose, tune here.
	public static final int   CHARGE_COST       = 3;    // dawn tome holy power spent per summon
	public static final int   DURATION          = 40;   // base turns the wisp lasts before it fades
	public static final int   HP_FLAT           = 6;    // base wisp HP
	public static final int   HP_PER_LEVEL      = 1;    // wisp HP gained per priest level
	public static final float DAMAGE_MIN_FLAT   = 2f;   // base minimum damage
	public static final float DAMAGE_MIN_PER_LVL= 0.25f;// minimum damage gained per priest level
	public static final float DAMAGE_MAX_FLAT   = 3f;   // base maximum damage
	public static final float DAMAGE_MAX_PER_LVL= 0.5f; // maximum damage gained per priest level
	public static final int   ENDURANCE_HP      = 2;    // +HP per Wisp Endurance talent point
	public static final int   ENDURANCE_TURNS   = 5;    // +duration per Wisp Endurance talent point
	public static final int   STRENGTH_DMG_MIN  = 1;    // +minimum damage per Wisp Strength talent point
	public static final int   STRENGTH_DMG_MAX  = 2;    // +maximum damage per Wisp Strength talent point
	public static final int   AURA_INTERVAL     = 5;    // turns between healing aura ticks
	public static final int   AURA_RANGE        = 2;    // healing aura reach, in tiles

	@Override
	public int icon() {
		return HeroIcon.CALL_WISP;
	}

	@Override
	public float chargeUse(Hero hero) {
		//directing an existing wisp is free, only summoning spends holy power
		return getWisp() == null ? CHARGE_COST : 0f;
	}

	@Override
	public boolean canCast(Hero hero) {
		//castable both to summon a wisp and to direct a wisp that is already out
		return super.canCast(hero)
				&& hero.subClass == HeroSubClass.HERALD_OF_DAWN;
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {
		if (getWisp() != null){
			//a wisp is already out: choose a cell to direct it to, free of charge
			GameScene.selectCell(new CellSelector.Listener() {
				@Override
				public void onSelect(Integer cell) {
					if (cell == null) return;
					WispOfDawn active = getWisp();
					if (active == null) return;
					active.directTocell(cell);
					hero.spendAndNext(1f);
				}

				@Override
				public String prompt() {
					return Messages.get(CallWisp.this, "prompt");
				}
			});
			return;
		}

		ArrayList<Integer> spawnPoints = new ArrayList<>();
		for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
			int p = hero.pos + PathFinder.NEIGHBOURS8[i];
			if (Actor.findChar(p) == null && (Dungeon.level.passable[p] || Dungeon.level.avoid[p])) {
				spawnPoints.add(p);
			}
		}

		if (spawnPoints.isEmpty()){
			GLog.w(Messages.get(this, "no_space"));
			return;
		}

		WispOfDawn wisp = new WispOfDawn();
		wisp.pos = Random.element(spawnPoints);
		GameScene.add(wisp);
		ScrollOfTeleportation.appear(wisp, wisp.pos);
		Dungeon.observe();

		Invisibility.dispel();
		hero.spend(1f);
		hero.busy();
		hero.sprite.operate(hero.pos);

		onSpellCast(tome, hero);
	}

	public static WispOfDawn getWisp(){
		for (Char ch : Actor.chars()){
			if (ch instanceof WispOfDawn){
				return (WispOfDawn) ch;
			}
		}
		return null;
	}

	//the Wisp of Dawn: a spirit of first light, built on the DirectableAlly framework (same base as the Sad Ghost's hero)
	public static class WispOfDawn extends DirectableAlly {

		private float timeRemaining = DURATION;
		private int auraTicker = 0;

		{
			//slice 1 placeholder: reuses the existing crystal wisp art, no new art
			spriteClass = CrystalWispSprite.Blue.class;

			flying = true;
			state = HUNTING;

			Hero hero = Dungeon.hero;
			int lvl = hero != null ? hero.lvl : 1;
			int endurance = hero != null ? hero.pointsInTalent(Talent.WISP_ENDURANCE) : 0;

			//HP and duration scale from the priest's level only, never from his weapon
			timeRemaining = DURATION + ENDURANCE_TURNS*endurance;
			HP = HT = HP_FLAT + HP_PER_LEVEL*lvl + ENDURANCE_HP*endurance;

			//same dodge and accuracy as the hero's level
			defenseSkill = lvl + 4;

			viewDistance = 4;

			immunities.addAll(new BlobImmunity().immunities());
			immunities.add(AllyBuff.class);
		}

		@Override
		public int attackSkill(Char target) {
			return (Dungeon.hero != null ? Dungeon.hero.lvl : 1) + 6;
		}

		@Override
		public int damageRoll() {
			int lvl = Dungeon.hero != null ? Dungeon.hero.lvl : 1;
			int strength = Dungeon.hero != null ? Dungeon.hero.pointsInTalent(Talent.WISP_STRENGTH) : 0;
			int min = Math.round(DAMAGE_MIN_FLAT + lvl*DAMAGE_MIN_PER_LVL) + strength*STRENGTH_DMG_MIN;
			int max = Math.round(DAMAGE_MAX_FLAT + lvl*DAMAGE_MAX_PER_LVL) + strength*STRENGTH_DMG_MAX;
			return Random.NormalIntRange(min, max);
		}

		@Override
		protected boolean act() {
			Hero hero = Dungeon.hero;
			//fades when the priest falls
			if (hero == null || !hero.isAlive()){
				die(null);
				return true;
			}
			//fades when its duration ends
			if (timeRemaining <= 0){
				die(null);
				hero.interrupt();
				return true;
			}

			//small healing aura, focused on the priest
			int auraPoints = hero.pointsInTalent(Talent.WISP_HEALING_AURA);
			if (auraPoints > 0 && Dungeon.level.distance(pos, hero.pos) <= AURA_RANGE){
				auraTicker++;
				if (auraTicker >= AURA_INTERVAL){
					auraTicker = 0;
					if (hero.HP < hero.HT){
						hero.HP = Math.min(hero.HT, hero.HP + auraPoints);
					}
				}
			}

			return super.act();
		}

		@Override
		public void die(Object cause) {
			flying = false;
			super.die(cause);
		}

		@Override
		protected void spend(float time) {
			super.spend(time);
			timeRemaining -= time;
		}

		@Override
		public void destroy() {
			super.destroy();
			Dungeon.observe();
		}

		@Override
		public void defendPos(int cell) {
			GLog.i(Messages.get(this, "direct_defend"));
			super.defendPos(cell);
		}

		@Override
		public void followHero() {
			GLog.i(Messages.get(this, "direct_follow"));
			super.followHero();
		}

		@Override
		public void targetChar(Char ch) {
			GLog.i(Messages.get(this, "direct_attack"));
			super.targetChar(ch);
		}

		@Override
		public String description() {
			return Messages.get(this, "desc", (int)timeRemaining);
		}

		private static final String TIME_REMAINING = "time_remaining";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(TIME_REMAINING, timeRemaining);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			timeRemaining = bundle.getFloat(TIME_REMAINING);
		}
	}
}
