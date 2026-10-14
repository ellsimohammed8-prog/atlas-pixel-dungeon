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

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Pacts;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.utils.DeviceCompat;

import java.util.ArrayList;

public class WndChallenges extends Window {

	private static final int WIDTH		= 120;
	private static final int TTL_HEIGHT = 16;
	private static final int BTN_HEIGHT = 16;
	private static final int GAP        = 1;

	private boolean editable;
	private ArrayList<CheckBox> boxes;
	private ArrayList<CheckBox> pactBoxes;

	public WndChallenges( int checked, boolean editable ) {

		super();

		this.editable = editable;

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "title"), 12 );
		title.hardlight( TITLE_COLOR );
		title.setPos(
				(WIDTH - title.width()) / 2,
				(TTL_HEIGHT - title.height()) / 2
		);
		PixelScene.align(title);
		add( title );

		boxes = new ArrayList<>();

		//Atlas: challenges stay victory-gated, pacts are open to all from the first run
		boolean challengesLocked = editable && !Badges.isUnlocked(Badges.Badge.VICTORY) && !DeviceCompat.isDebug();

		float pos = TTL_HEIGHT;
		for (int i=0; i < Challenges.NAME_IDS.length; i++) {

			final String challenge = Challenges.NAME_IDS[i];
			
			CheckBox cb = new CheckBox( Messages.titleCase(Messages.get(Challenges.class, challenge)) );
			cb.checked( (checked & Challenges.MASKS[i]) != 0 );
			cb.active = editable && !challengesLocked;

			if (i > 0) {
				pos += GAP;
			}
			cb.setRect( 0, pos, WIDTH-16, BTN_HEIGHT );

			add( cb );
			boxes.add( cb );
			
			IconButton info = new IconButton(Icons.get(Icons.INFO)){
				@Override
				protected void onClick() {
					super.onClick();
					ShatteredPixelDungeon.scene().add(
							new WndMessage(Messages.get(Challenges.class, challenge+"_desc"))
					);
				}
			};
			info.setRect(cb.right(), pos, 16, BTN_HEIGHT);
			add(info);
			
			pos = cb.bottom();
		}

		if (challengesLocked) {
			pos += GAP + 3;
			RenderedTextBlock lockNote = PixelScene.renderTextBlock( Messages.get(this, "challenges_locked"), 8 );
			lockNote.maxWidth(WIDTH);
			lockNote.hardlight( 0x999999 );
			lockNote.setPos( (WIDTH - lockNote.width()) / 2, pos );
			PixelScene.align(lockNote);
			add( lockNote );
			pos += lockNote.height();
		}

		//Atlas Pixel Dungeon: pacts section
		pos += GAP + 4;
		RenderedTextBlock pactsTitle = PixelScene.renderTextBlock( Messages.get(this, "pacts_title"), 12 );
		pactsTitle.hardlight( TITLE_COLOR );
		pactsTitle.setPos(
				(WIDTH - pactsTitle.width()) / 2,
				pos
		);
		PixelScene.align(pactsTitle);
		add( pactsTitle );
		pos += pactsTitle.height() + GAP + 3;

		pactBoxes = new ArrayList<>();
		int pactChecked = editable ? SPDSettings.pacts() : Dungeon.pacts;
		for (int i=0; i < Pacts.NAME_IDS.length; i++) {

			final String pact = Pacts.NAME_IDS[i];

			CheckBox cb = new CheckBox( Messages.titleCase(Messages.get(Pacts.class, pact)) ){
				@Override
				protected void onClick() {
					if (!checked() && editable) {
						int count = 0;
						for (CheckBox b : pactBoxes) if (b.checked()) count++;
						if (count >= Pacts.MAX_PACTS) {
							ShatteredPixelDungeon.scene().add(
									new WndMessage(Messages.get(WndChallenges.class, "pacts_max"))
							);
							return;
						}
					}
					super.onClick();
				}
			};
			cb.checked( (pactChecked & Pacts.MASKS[i]) != 0 );
			cb.active = editable;

			if (i > 0) {
				pos += GAP;
			}
			cb.setRect( 0, pos, WIDTH-16, BTN_HEIGHT );

			add( cb );
			pactBoxes.add( cb );

			IconButton info = new IconButton(Icons.get(Icons.INFO)){
				@Override
				protected void onClick() {
					super.onClick();
					ShatteredPixelDungeon.scene().add(
							new WndMessage(Messages.get(Pacts.class, pact+"_desc"))
					);
				}
			};
			info.setRect(cb.right(), pos, 16, BTN_HEIGHT);
			add(info);

			pos = cb.bottom();
		}

		resize( WIDTH, (int)pos );
	}

	@Override
	public void onBackPressed() {

		if (editable) {
			int value = 0;
			for (int i=0; i < boxes.size(); i++) {
				if (boxes.get( i ).checked()) {
					value |= Challenges.MASKS[i];
				}
			}
			SPDSettings.challenges( value );

			int pactValue = 0;
			for (int i=0; i < pactBoxes.size(); i++) {
				if (pactBoxes.get( i ).checked()) {
					pactValue |= Pacts.MASKS[i];
				}
			}
			SPDSettings.pacts( pactValue );
		}

		super.onBackPressed();
	}
}