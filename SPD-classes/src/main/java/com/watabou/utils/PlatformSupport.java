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

package com.watabou.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.watabou.input.ControllerHandler;
import com.watabou.noosa.Game;

import java.util.HashMap;

public abstract class PlatformSupport {
	
	public abstract void updateDisplaySize();

	public boolean supportsFullScreen(){
		return true; //default
	}

	public static final int INSET_ALL = 3; //All insets, from hole punches to nav bars
	public static final int INSET_LRG = 2; //Only big insets, full size notches and nav bars
	public static final int INSET_BLK = 1; //only complete blocker assets like navbars

	public RectF getSafeInsets( int level ){
		return new RectF(
				Gdx.graphics.getSafeInsetLeft(),
				Gdx.graphics.getSafeInsetTop(),
				Gdx.graphics.getSafeInsetRight(),
				Gdx.graphics.getSafeInsetBottom()
		);
	}

	//returns a display cutout (if one is present) in device pixels, or empty if none is present
	public RectF getDisplayCutout(){
		return new RectF();
	}
	
	public abstract void updateSystemUI();

	public abstract boolean connectedToUnmeteredNetwork();

	public abstract boolean supportsVibration();

	public void vibrate( int millis ){
		if (ControllerHandler.isControllerConnected()) {
			ControllerHandler.vibrate(millis);
		} else {
			Gdx.input.vibrate( millis );
		}
	}

	public void setHonorSilentSwitch( boolean value ){
		//does nothing by default
	}

	public boolean openURI( String uri ){
		return Gdx.net.openURI( uri );
	}

	public void setOnscreenKeyboardVisible(boolean value, boolean multiline){
		//by default ignore multiline
		Gdx.input.setOnscreenKeyboardVisible(value, Input.OnscreenKeyboardType.Default);
	}

	//TODO should consider spinning this into its own class, rather than platform support getting ever bigger
	protected static HashMap<FreeTypeFontGenerator, HashMap<Integer, BitmapFont>> fonts;

	protected int pageSize;
	protected PixmapPacker packer;
	//Atlas: glyphs whose advance has already been tightened, per font
	private static final java.util.HashMap<BitmapFont, java.util.HashSet<Integer>> atlasTweakedGlyphs = new java.util.HashMap<>();
	protected boolean systemfont;
	
	public abstract void setupFontGenerators(int pageSize, boolean systemFont );

	protected abstract FreeTypeFontGenerator getGeneratorForString( String input );

	public abstract String[] splitforTextBlock( String text, boolean multiline );

	public void resetGenerators(){
		resetGenerators( true );
	}

	public void resetGenerators( boolean setupAfter ){
		if (fonts != null) {
			for (FreeTypeFontGenerator generator : fonts.keySet()) {
				for (BitmapFont f : fonts.get(generator).values()) {
					f.dispose();
				}
				fonts.get(generator).clear();
				generator.dispose();
			}
			fonts.clear();
			if (packer != null) {
				for (PixmapPacker.Page p : packer.getPages()) {
					p.getTexture().dispose();
				}
				packer.dispose();
			}
			fonts = null;
		}
		if (setupAfter) setupFontGenerators(pageSize, systemfont);
	}

	public void reloadGenerators(){
		if (packer != null) {
			for (FreeTypeFontGenerator generator : fonts.keySet()) {
				for (BitmapFont f : fonts.get(generator).values()) {
					f.dispose();
				}
				fonts.get(generator).clear();
			}
			if (packer != null) {
				for (PixmapPacker.Page p : packer.getPages()) {
					p.getTexture().dispose();
				}
				packer.dispose();
			}
			packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 1, false);
		}
	}

	//flipped is needed because Shattered's graphics are y-down, while GDX graphics are y-up.
	//this is very confusing, I know.
	public BitmapFont getFont(int size, String text, boolean flipped, boolean border) {
		FreeTypeFontGenerator generator = getGeneratorForString(text);

		if (generator == null){
			return null;
		}

		//Atlas: detect Arabic text (logical or presentation-form ranges)
		boolean arabicText = false;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if ((c >= 0x0600 && c <= 0x06FF) || (c >= 0xFB50 && c <= 0xFDFF) || (c >= 0xFE70 && c <= 0xFEFF)) {
				arabicText = true;
				break;
			}
		}

		int key = size;
		if (border) key += Short.MAX_VALUE; //surely we'll never have a size above 32k
		if (flipped) key = -key;
		if (!fonts.get(generator).containsKey(key)) {
			//Atlas: Arabic is generated at its TRUE size. Do NOT supersample Arabic:
			//with incremental glyph loading, glyphs loaded after generation keep
			//their generation-scale xadvance (setScale does not reach them), so a
			//2x generation silently doubles every Arabic letter advance and words
			//can never join. Measured directly: at size 13 a medial beh advances
			//8px under 2x+setScale vs the font's designed 4px at 1x.
			int genSize = size;
			FreeTypeFontGenerator.FreeTypeFontParameter parameters = new FreeTypeFontGenerator.FreeTypeFontParameter();
			parameters.size = genSize;
			parameters.flip = flipped;
			if (border) {
				//Atlas: Arabic gets a hairline border in the SAME colour as the text
				//itself (white — glyphs are tinted at draw time). Naskh joining
				//strokes rasterize ~1px and nearly invisible at UI sizes, so words
				//look disconnected even with correct advances; the same-colour
				//outline fattens every stroke, joins included, so words read as
				//connected cursive. (A dark outline would cut the joins instead —
				//that was the original reason Arabic had no border at all.)
				parameters.borderWidth = arabicText ? Math.max(0.5f, parameters.size * 0.03f) : parameters.size / 10f;
				if (arabicText) parameters.borderColor = com.badlogic.gdx.graphics.Color.WHITE;
			}
			if (size >= 20){
				parameters.renderCount = 2;
			} else {
				parameters.renderCount = 3;
			}
			parameters.hinting = arabicText ? FreeTypeFontGenerator.Hinting.Full : FreeTypeFontGenerator.Hinting.None;
			parameters.spaceX = -(int) parameters.borderWidth;
			parameters.incremental = true;
			parameters.characters = "�";
			parameters.packer = packer;

			try {
				BitmapFont font = generator.generateFont(parameters);
				font.getData().missingGlyph = font.getData().getGlyph('�');
				fonts.get(generator).put(key, font);
			} catch ( Exception e ){
				Game.reportException(e);
				return null;
			}
		}

		BitmapFont result = fonts.get(generator).get(key);
		//Atlas: tighten Arabic glyph advances slightly (~6% of each advance) so
		//joining strokes overlap a hair beyond the font's own metrics — a safety
		//margin against per-glyph rounding at small sizes.
		//integer advances rounded up from the font's metrics, which spreads joining
		//letters apart; pulling each advance in by one unit closes the joins.
		if (result != null && arabicText) {
			java.util.HashSet<Integer> done = atlasTweakedGlyphs.get(result);
			if (done == null) {
				done = new java.util.HashSet<>();
				atlasTweakedGlyphs.put(result, done);
			}
			for (com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph[] page : result.getData().glyphs) {
				if (page == null) continue;
				for (com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph g : page) {
					if (g != null && done.add(g.id)) {
						int cut = (g.xadvance > 3) ? Math.max(1, Math.round(g.xadvance * 0.06f)) : 0;
						g.xadvance = Math.max(1, g.xadvance - cut);
					}
				}
			}
		}
		return result;
	}

}
