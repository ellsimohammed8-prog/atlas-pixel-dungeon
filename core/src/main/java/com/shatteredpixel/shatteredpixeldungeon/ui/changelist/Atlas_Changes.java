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

package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

import java.util.ArrayList;

public class Atlas_Changes {

	public static void add_Atlas_Changes( ArrayList<ChangeInfo> changeInfos ){

		ChangeInfo changes113 = new ChangeInfo("v1.1.3", true, "");
		changes113.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes113);

		changes113.addButton( new ChangeButton( ChangeIcons.V081_MISC, "Arabic, Drawn Solid",
				"Arabic text is now drawn with a hairline outline in the text's own colour, thickening the fine strokes that join letters together. Words now read as the connected cursive Arabic is written in — in menus, descriptions, and the dungeon log alike."));

		ChangeInfo changes112 = new ChangeInfo("v1.1.2", true, "");
		changes112.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes112);

		changes112.addButton( new ChangeButton( ChangeIcons.V081_MISC, "Arabic, Finally Connected",
				"A fault in how Arabic letters were spaced — every letter was advanced at double its proper width — has been found and fixed. Arabic words now join up in true cursive, the way Arabic is written, in menus, descriptions, and the dungeon log alike."));

		ChangeInfo changes111 = new ChangeInfo("v1.1.1", true, "");
		changes111.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes111);

		changes111.addButton( new ChangeButton( ChangeIcons.V081_MISC, "Heroes, Framed for Your Phone",
				"Every hero's splash artwork has been repainted in portrait, composed for phone screens, so your hero stands front and center instead of being cropped away at the edges.\n" +
				"\n" +
				"The hero select screen also opens with the Atlas King already presented — no more empty black screen before you choose."));

		changes111.addButton( new ChangeButton( ChangeIcons.V074_SHPX, "Arabic, Drawn Tighter",
				"Arabic letter spacing has been tightened throughout the game, scaled to the larger text sizes phones actually use, so words hold together the way they should."));

		ChangeInfo changes110 = new ChangeInfo("v1.1.0", true, "");
		changes110.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes110);

		changes110.addButton( new ChangeButton( ChangeIcons.V081_MISC, "Full Arabic Translation",
				"Atlas Pixel Dungeon now speaks Arabic completely: every hero, item, enemy, and story text — almost five thousand strings — translated by hand for this edition.\n" +
				"\n" +
				"Arabic text is also laid out properly right-to-left, the way it is meant to be read. Choose Arabic from the language settings to play the whole dungeon in Arabic."));

		changes110.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_KEY), "Polish in Every Tongue",
				"The Crown of Atlas is now named correctly in all 23 languages, in every ranking, description, and dialogue where it appears. A few garbled names left over from the great renaming — including the antimagic champion — have been set right as well."));

		changes110.addButton( new ChangeButton( ChangeIcons.V074_SHPX, "Ads, Your Choice",
				"This edition is supported by a single ad shown only after a run ends, never during play. On Android, players in Europe are now asked for consent first, as the law requires.\n" +
				"\n" +
				"And if you would rather see none at all, there is a switch: Settings, Connectivity tab, _Show ads_."));

		ChangeInfo changes = new ChangeInfo("v1.0.0", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton( new ChangeButton( ChangeIcons.V074_SHPX, "Atlas Pixel Dungeon",
				"Welcome to Atlas Pixel Dungeon, an original edition by Mohammed Guejjim, built on the open source of Shattered Pixel Dungeon by Evan Debenham and Pixel Dungeon by Watabou.\n" +
				"\n" +
				"A thousand years ago, King Atlas — cartographer, astronomer, and ruler of a mountain kingdom — forged the Crown of Atlas and sealed an ancient darkness beneath his realm. The kingdom was buried layer by layer, and its halls became this dungeon. The seal weakens, and dark energy seeps upward once more."));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.SCIMITAR), "Six Heroes of the Mountain",
				"Descend as one of six heroes, renamed and reimagined for the buried kingdom:\n" +
				"\n" +
				"**-** The _Atlas King_, heir of the mountain throne\n" +
				"**-** The _Star Sage_, reader of constellations\n" +
				"**-** The _Dune Shadow_, unseen blade of the sands\n" +
				"**-** The _Cedar Huntress_, keeper of the high forests\n" +
				"**-** The _Sirocco Blade_, duelist of the sandstorm\n" +
				"**-** The _Dawn Priest_, bearer of the Tome of Dawn"));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_KEY), "A Buried Kingdom",
				"Five regions of the fallen kingdom await:\n" +
				"\n" +
				"**-** _The Sunken Cisterns_, watched by the Mirelord\n" +
				"**-** _The Prison of Chains_, ruled by the Chain Warden\n" +
				"**-** _The Root Caves_, under the great cedars\n" +
				"**-** _The Buried City_, guarded by the Buried Engine and the Buried King\n" +
				"**-** _The Sealed Halls_, where the Sealed Darkness waits behind its gate\n" +
				"\n" +
				"Recover the Crown of Atlas from the depths, and ascend — if you can."));

		changes.addButton( new ChangeButton( ChangeIcons.V081_MISC, "New Artwork",
				"Every hero and every region has brand-new splash artwork, and the inhabitants of the dungeon have been recolored to belong to their regions: the murk of the cisterns, the iron of the prison, the roots of the caves, the bronze of the buried city, and the embers of the sealed halls.\n" +
				"\n" +
				"The update history below is the changelog of Shattered Pixel Dungeon, the open-source game this edition is built on, kept here as a record of its lineage."));
	}
}
