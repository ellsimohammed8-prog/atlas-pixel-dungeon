/*
 * Atlas Pixel Dungeon — Arabic render harness (diagnostic tool for agent/arabic-fix)
 *
 * Runs production pipeline pieces exactly:
 *  - DesktopPlatformSupport / PlatformSupport.getFont() (the same font code Android inherits)
 *  - ArabicSupport.shape()
 *  - DesktopPlatformSupport.splitforTextBlock() tokenization
 *  - RenderedTextBlock layout incl. the Atlas RTL mirror
 * and draws results with plain GL, glyph-for-glyph like RenderedText.draw().
 *
 * Run:  gradlew :desktop:arabicFixHarness   (writes build/harness png; prints metrics)
 * Optional properties:
 *   -Dfix.out=path.png            screenshot output
 *   -Dfix.gensizes=36,72,13       glyph generation sizes in device px (36 = phone-like)
 *   -Dfix.lang=ar|en              ar triggers the RTL mirror in blocks (production behaviour)
 *   -Dsweep.mode=sweep|production production = PlatformSupport.getFont exactly as-is
 *   -Dsweep.hinting=full|none|slight|medium
 *   -Dsweep.border=-1|0|<ratio>   -1 = production behaviour (hairline), 0 = none
 *   -Dsweep.preload=true|false    generate all FE70..FEFC + marks + digits at font creation
 */
package com.shatteredpixel.shatteredpixeldungeon.desktop;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.RenderedText;
import com.watabou.utils.ArabicSupport;

public class ArabicFixHarness extends ApplicationAdapter {

	//exact probe string from the mission
	private static final String STR_HP        = "يستعيد البطل 25 نقطة صحة خلال 3 أدوار (مكافأة +10%)";
	//real AR strings from the .properties files (placeholders substituted as the game shows them)
	private static final String STR_TRINKET   =
			"عند مستواها الحالي، هذه الحلية ستخفّض معدل استحداث الأعداء بمقدار _25%_ أثناء النهار في الحياة الواقعية " +
			"(8 صباحا-8 مساء)، وتزيد معدل استحداث الأعداء بمقدار _50%_ في الليل (8 مساء-8 صباحا).";
	private static final String STR_SLAM      =
			"_يقفز حتى 12 مربع(ات)_ ويسبب 40% (السلسلة×20%) من قوة صد ضررك كضرر إضافي. يعيد تعيين السلسلة عند الاستعمال.";
	private static final String STR_COMBREQ   = "(12 مركب)";
	private static final String STR_MIXED     = "مرحبا بك في Atlas Pixel Dungeon يا بطل";
	//English no-regression probes (rendered when -Dfix.lang=en)
	private static final String STR_EN_1      = "The hero restores 25 HP over 3 turns (bonus +10%)";
	private static final String STR_EN_2      = "_Slam_ deals 40% (chain x20%) bonus damage. Resets the chain.";

	private static final String STR_HARAKAT   = "مُهَرِّبٌ شؤون الـبَطْلُ ثُمَّ عَادَ";
	private static final String STR_DIGITS    = "٠١٢٣٤٥٦٧٨٩ وا 918 بعد";

	private SpriteBatch batch;
	private OrthographicCamera cam;
	private final HashMap<Integer, BitmapFont> prodFonts = new HashMap<>();

	private int[]   genSizes;
	private boolean arabicLang;
	private String  outPath;
	private String  sweepMode;
	private String  sweepHint;
	private float   sweepBorder;
	private boolean sweepPreload;

	private FreeTypeFontGenerator sweepGenerator = null;
	private final HashMap<Integer, BitmapFont> sweepFonts = new HashMap<>();

	private BitmapFont labelFont = null;

	public static void main(String[] args){
		Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
		cfg.setTitle("Arabic fix harness");
		cfg.setWindowedMode(1300, 1100);
		new Lwjgl3Application(new ArabicFixHarness(), cfg);
	}

	@Override
	public void create(){
		batch = new SpriteBatch();
		cam = new OrthographicCamera(1300, 1100);
		cam.position.set(650, 550, 0);
		cam.update();

		Game.width = 1300;
		Game.height = 1100;
		//emulate the phone: PixelScene defaultZoom 4 (1440x2560-class devices land at 3-6)
		PixelScene.defaultZoom = 4;

		Game.platform = new DesktopPlatformSupport();
		Game.platform.setupFontGenerators(512, false);

		outPath      = System.getProperty("fix.out", "arabic-harness.png");
		arabicLang   = "ar".equals(System.getProperty("fix.lang", "ar"));
		genSizes     = parseSizes(System.getProperty("fix.gensizes", "36"));
		sweepMode    = System.getProperty("sweep.mode", "production");
		sweepHint    = System.getProperty("sweep.hinting", "full");
		sweepBorder  = Float.parseFloat(System.getProperty("sweep.border", "-1"));
		sweepPreload = Boolean.parseBoolean(System.getProperty("sweep.preload", "false"));

		if (sweepMode.equals("sweep")){
			sweepGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arabic.ttf"));
		}

		if (arabicLang){
			try {
				Field f = Messages.class.getDeclaredField("lang");
				f.setAccessible(true);
				f.set(null, Languages.ARABIC);
			} catch (Exception e){ throw new RuntimeException(e); }
		}
	}

	private static int[] parseSizes(String s){
		String[] parts = s.split(",");
		int[] out = new int[parts.length];
		for (int i = 0; i < parts.length; i++) out[i] = Integer.parseInt(parts[i].trim());
		return out;
	}

	@Override
	public void render(){
		Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(cam.combined);
		batch.begin();

		int y = 1060;
		boolean english = !arabicLang;
		for (int gen : genSizes){
			label().draw(batch, "gen=" + gen + "px  mode=" + sweepMode + " hint=" + sweepHint
					+ " border=" + sweepBorder + " preload=" + sweepPreload
					+ " lang=" + (arabicLang ? "ARABIC(rtl align ON)" : "ENGLISH"), 20, y - 8);
			y -= 66;
			if (english){
				y = drawProbe(y, gen, STR_EN_1);
				y = drawProbe(y, gen, STR_EN_2, 296);
				y -= 34;
			} else {
				y = drawProbe(y, gen, STR_HP);
				y = drawProbe(y, gen, STR_HARAKAT);
				y = drawProbe(y, gen, STR_DIGITS);
				y = drawProbe(y, gen, STR_TRINKET, 296);
				y = drawProbe(y, gen, STR_SLAM, 296);
				y = drawProbe(y, gen, STR_COMBREQ, 296);
				y = drawProbe(y, gen, STR_MIXED, 296);
				y -= 34;
			}
		}
		batch.end();
		screenshot();
	}

	private static String shortTxt(String s){
		return s.length() > 12 ? s.substring(0, 12) + ".." : s;
	}

	private void screenshot(){
		Pixmap pm = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		PixmapIO.writePNG(Gdx.files.absolute(outPath), pm);
		Gdx.app.log("ArabicFixHarness", "screenshot: " + new File(outPath).getAbsolutePath() + " gen=" + genSizes[0]);
		Gdx.app.postRunnable(new Runnable(){ @Override public void run(){ Gdx.app.exit(); } });
	}

	// -------------------------------------------------------------------
	// fonts
	// -------------------------------------------------------------------
	private BitmapFont label(){
		if (labelFont == null){
			labelFont = Game.platform.getFont(20, "Atlas", false, true);
		}
		return labelFont;
	}

	//font exactly the way RenderedText.measure() gets it
	private BitmapFont prodFont(int gen, String shaped){
		BitmapFont f = prodFonts.get(gen);
		if (f == null){
			f = Game.platform.getFont(gen, shaped, true, true);
			prodFonts.put(gen, f);
		}
		return f;
	}

	private BitmapFont fontFor(int gen, String shaped){
		if (!sweepMode.equals("sweep")) return prodFont(gen, shaped);

		BitmapFont f = sweepFonts.get(gen);
		if (f != null) return f;

		FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
		p.size = gen;
		p.flip = true;
		p.incremental = true;
		p.renderCount = gen >= 20 ? 2 : 3;
		if (sweepBorder == 0){
			p.borderWidth = 0;
		} else if (sweepBorder > 0){
			p.borderWidth = Math.max(0.5f, p.size * sweepBorder);
			p.borderColor = com.badlogic.gdx.graphics.Color.WHITE;
		}
		switch (sweepHint){
			case "none":   p.hinting = FreeTypeFontGenerator.Hinting.None;   break;
			case "slight": p.hinting = FreeTypeFontGenerator.Hinting.Slight; break;
			case "medium": p.hinting = FreeTypeFontGenerator.Hinting.Medium; break;
			default:       p.hinting = FreeTypeFontGenerator.Hinting.Full;   break;
		}
		if (sweepPreload){
			StringBuilder chars = new StringBuilder("\uFFFD ()0123456789%$+-x\u00D7,._");
			for (int cp = 0x060C; cp <= 0x0652; cp++) chars.append((char) cp);
			for (int cp = 0x0660; cp <= 0x066F; cp++) chars.append((char) cp);
			for (int cp = 0xFE70; cp <= 0xFEFC; cp++) chars.append((char) cp);
			p.characters = chars.toString();
		}
		PixmapPacker packer = new PixmapPacker(512, 512, Pixmap.Format.RGBA8888, 1, false);
		p.packer = packer;
		f = sweepGenerator.generateFont(p);
		f.getData().missingGlyph = f.getData().getGlyph('\uFFFD');
		sweepFonts.put(gen, f);
		return f;
	}

	// -------------------------------------------------------------------
	// drawing: one probe, both in the RenderedText (whole-string shape)
	// path AND through the real RenderedTextBlock (tokenization, wrap, RTL
	// mirror). Word positions are already PixelScene-aligned (defaultZoom=4)
	// by the block layout code, exactly as on the phone.
	// -------------------------------------------------------------------
	private int drawProbe(int y, int gen, String text){
		return drawProbe(y, gen, text, Integer.MAX_VALUE / 2);
	}

	private int drawProbe(int y, int gen, String text, int maxWidthUnits){
		try {
			//pass 1: whole-line shaped + one font draw (RenderedText path)
			String shaped = ArabicSupport.shape(text);
			BitmapFont f = fontFor(gen, shaped);
			if (f == null){ return y - 40; }
			GlyphLayout glyphs = new GlyphLayout(f, shaped);
			float by = (y - f.getData().capHeight) - 6;
			f.draw(batch, shaped, 20, by);
			label().draw(batch, "1line w=" + (int) glyphs.width + "px", 740, by);
			y -= glyphs.height + 38;

			//pass 2: real block; RTL mirroring via layout() (lang=ARABIC)
			//PixelScene.renderTextBlock equivalent: gen at device px size, zoom 1/zoomScale.
			RenderedTextBlock block = new RenderedTextBlock(gen);
			block.text(text, maxWidthUnits);
			block.zoom(1 / 4f);

			Field wf = RenderedTextBlock.class.getDeclaredField("words");
			wf.setAccessible(true);
			@SuppressWarnings("unchecked")
			ArrayList<RenderedText> words = (ArrayList<RenderedText>) wf.get(block);

			Field shapeF = RenderedText.class.getDeclaredField("shapedText");
			shapeF.setAccessible(true);
			Field fontF = RenderedText.class.getDeclaredField("font");
			fontF.setAccessible(true);

			float blockX = 20;
			StringBuilder dbg = new StringBuilder();
			for (RenderedText w : words){
				if (w.width == 0 && w.height == 0) continue;
				if (w.text() == null || w.text().length() == 0) continue;
				String wShaped = (String) shapeF.get(w);
				BitmapFont wFont = (BitmapFont) fontF.get(w);
				if (wShaped == null || wShaped.length() == 0 || wFont == null) continue;
				float bx = blockX + Math.round(w.x * 4f);
				float by2 = y - Math.round(w.y * 4f) - wFont.getData().capHeight - 4;
				wFont.draw(batch, wShaped, bx, by2);
				if (dbg.length() < 1500){
					dbg.append(String.format("[t=%s | x=%.2f y=%.2f fW=%.1f sW=%.1f sx=%.2f] ",
							shortTxt(w.text()), w.x, w.y, w.width, w.width(), w.scale.x));
				}
			}
			Gdx.app.log("ArabicFixHarness", "BLOCK words: " + dbg);
			Gdx.app.log("ArabicFixHarness", String.format("BLOCK width=%.1f height=%.1f", block.width(), block.height()));
			Field hF = block.getClass().getSuperclass().getDeclaredField("height");
			hF.setAccessible(true);
			float blockHeight = (float) hF.get(block);
			y -= Math.round(blockHeight * 4f) + 34;
		} catch (Exception e){
			Gdx.app.error("ArabicFixHarness", "probe failed", e);
			y -= 60;
		}
		return y;
	}
}
