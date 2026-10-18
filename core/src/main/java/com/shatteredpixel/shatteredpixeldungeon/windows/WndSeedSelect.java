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

import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.AtlasSeeds;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;

public class WndSeedSelect extends Window {

	private static final int WIDTH = 130;
	private static final int BTN_HEIGHT = 16;
	private static final float GAP = 3;

	public interface Listener {
		void onSeedChanged();
	}

	public WndSeedSelect( final Listener listener ) {
		super();

		IconTitle titlebar = new IconTitle();
		titlebar.icon( Icons.get(Icons.SEED) );
		titlebar.label( Messages.titleCase(Messages.get(this, "title")) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		String current = SPDSettings.customSeed();
		String descStr = current.isEmpty() ?
				Messages.get(this, "desc") :
				Messages.get(this, "current", current);

		RenderedTextBlock message = PixelScene.renderTextBlock(descStr, 6);
		message.maxWidth(WIDTH);
		message.setPos(0, titlebar.bottom() + GAP);
		add( message );

		float pos = message.bottom() + GAP + 2;

		// Button to enter custom text
		RedButton btnCustom = new RedButton( Messages.get(this, "enter_custom") ) {
			@Override
			protected void onClick() {
				hide();
				String existingSeedtext = SPDSettings.customSeed();
				ShatteredPixelDungeon.scene().addToFront( new WndTextInput(Messages.get(HeroSelectScene.class, "custom_seed_title"),
						Messages.get(HeroSelectScene.class, "custom_seed_desc"),
						existingSeedtext,
						20,
						false,
						Messages.get(HeroSelectScene.class, "custom_seed_set"),
						Messages.get(HeroSelectScene.class, "custom_seed_clear")){
					@Override
					public void onSelect(boolean positive, String text) {
						text = DungeonSeed.formatText(text);
						long seed = DungeonSeed.convertFromText(text);

						if (positive && seed != -1){
							for (GamesInProgress.Info info : GamesInProgress.checkAll()){
								if (info.customSeed.isEmpty() && info.seed == seed){
									SPDSettings.customSeed("");
									if (listener != null) listener.onSeedChanged();
									ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(HeroSelectScene.class, "custom_seed_duplicate")));
									return;
								}
							}
							SPDSettings.customSeed(text);
						} else {
							SPDSettings.customSeed("");
						}
						if (listener != null) listener.onSeedChanged();
					}
				});
			}
		};
		btnCustom.setRect( 0, pos, WIDTH, BTN_HEIGHT );
		add( btnCustom );
		pos = btnCustom.bottom() + GAP;

		if (!current.isEmpty()) {
			RedButton btnClear = new RedButton( Messages.get(this, "clear") ) {
				@Override
				protected void onClick() {
					SPDSettings.customSeed("");
					hide();
					if (listener != null) listener.onSeedChanged();
				}
			};
			btnClear.setRect( 0, pos, WIDTH, BTN_HEIGHT );
			add( btnClear );
			pos = btnClear.bottom() + GAP;
		}

		pos += 3;
		RenderedTextBlock presetHeader = PixelScene.renderTextBlock( Messages.get(this, "atlas_presets"), 8 );
		presetHeader.hardlight( TITLE_COLOR );
		presetHeader.setPos((WIDTH - presetHeader.width()) / 2f, pos);
		PixelScene.align(presetHeader);
		add( presetHeader );
		pos = presetHeader.bottom() + GAP;

		for (final String preset : AtlasSeeds.PRESETS) {
			String labelKey = "preset_" + preset.toLowerCase().replace(' ', '_');
			RedButton btnPreset = new RedButton( Messages.get(this, labelKey) ) {
				@Override
				protected void onClick() {
					SPDSettings.customSeed(preset);
					hide();
					if (listener != null) listener.onSeedChanged();
				}
			};
			btnPreset.setRect( 0, pos, WIDTH, BTN_HEIGHT );
			add( btnPreset );
			pos = btnPreset.bottom() + GAP;
		}

		resize( WIDTH, (int)pos );
	}
}
