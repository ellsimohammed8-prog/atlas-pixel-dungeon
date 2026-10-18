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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;

public class WndCasualRevive extends Window {

	private static final int WIDTH = 130;
	private static final int BTN_HEIGHT = 20;
	private static final float GAP = 4;

	public WndCasualRevive( final Object cause ) {
		super();

		IconTitle titlebar = new IconTitle();
		titlebar.icon( Icons.get(Icons.REPEAT) );
		titlebar.label( Messages.titleCase(Messages.get(this, "title")) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		RenderedTextBlock message = PixelScene.renderTextBlock(Messages.get(this, "message"), 6 );
		message.maxWidth(WIDTH);
		message.setPos(0, titlebar.bottom() + GAP);
		add( message );

		RedButton btnRevive = new RedButton( Messages.get(this, "revive") ) {
			@Override
			protected void onClick() {
				hide();
				WndResurrect.instance = null;

				Dungeon.hero.casualRevive();

				if (Dungeon.level.locked) {
					InterlevelScene.mode = InterlevelScene.Mode.RESET;
					Game.switchScene( InterlevelScene.class );
				} else {
					int entrance = Dungeon.level.entrance();
					if (entrance != -1 && (Dungeon.level.passable[entrance] || Dungeon.level.avoid[entrance])) {
						Dungeon.hero.pos = entrance;
					} else {
						Dungeon.hero.pos = Dungeon.level.randomRespawnCell(Dungeon.hero);
					}
					GameScene.flash(0x8080FFFF);
					Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
					GLog.p(Messages.get(WndCasualRevive.class, "respawned"));
					Dungeon.observe();
					GameScene.updateFog();
				}
			}
		};
		btnRevive.setRect( 0, message.bottom() + GAP * 2, WIDTH, BTN_HEIGHT );
		add( btnRevive );

		RedButton btnAbandon = new RedButton( Messages.get(this, "abandon") ) {
			@Override
			protected void onClick() {
				hide();
				WndResurrect.instance = null;
				Actor.fixTime();
				Hero.reallyDie(cause);
			}
		};
		btnAbandon.setRect( 0, btnRevive.bottom() + GAP, WIDTH, BTN_HEIGHT );
		add( btnAbandon );

		resize( WIDTH, (int)btnAbandon.bottom() );
	}

	@Override
	public void onBackPressed() {
		// Do nothing, require deliberate choice between respawning or abandoning
	}
}
