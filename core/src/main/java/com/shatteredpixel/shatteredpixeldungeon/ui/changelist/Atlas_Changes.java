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

		ChangeInfo changes131 = new ChangeInfo("v1.3.1", true, "");
		changes131.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes131);

		changes131.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ALCH_PAGE), "Blueprints, Fixed",
				"An apology and a fix: in v1.3.0 the Blueprints could not actually be used. The alchemy pot refused to accept any melee weapon, so no evolution could ever be brewed. The pot now accepts longswords, war hammers, glaives and battle axes as it always should have. If you tried in v1.3.0 and failed, please try again - it works now."));

		changes131.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.AMULET), "Pacts & Text Repairs",
				"Every Pact of Atlas now does exactly what its description promises. _Glass_ amplifies wand damage as well as weapon damage. _Iron Patience_ also slows thrown weapons and wand zaps. _Thorns_ only weakens healing potions (not elixirs or regrowth bombs), and its reflection now answers melee hits only, based on the damage you actually took. _Cartographer's Eye_ reveals secrets within 6 tiles as described, even through walls.\\n" +
				"\\n" +
				"Saved runs now show their own pacts in the game info window, and the Hammer of the Buried Engine's shockwave can no longer strike enemies through walls or in rooms you cannot see.\\n" +
				"\\n" +
				"Plus a round of text repairs: star mark naming is now consistent everywhere, ghost quests name their targets correctly, and leftover names from the game's ancestor have been swept out of dialogues, quest text and system messages."));

		ChangeInfo changes130 = new ChangeInfo("v1.3.0", true, "");
		changes130.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes130);

		changes130.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ALCH_PAGE), "Atlas Blueprints",
				"Evolve weapons at an alchemy pot! Place a base weapon and a scroll of upgrade in the pot and pay 8 energy: a longsword becomes the _Crownblade of Atlas_, a war hammer the _Hammer of the Buried Engine_, a glaive the _Mirelord's Reach_, and a battle axe the _Cedar Cleaver_.\\n" +
				"\\n" +
				"The evolved weapon keeps everything the old one had earned: its upgrade level, enchantment, and augment. Nothing is lost but the scroll.\\n" +
				"\\n" +
				"Each evolved weapon also fights differently: the Crownblade brands foes with _star marks_, the Engine Hammer looses _shockwaves_, the Mirelord's Reach _roots_ enemies struck at full reach, and the Cedar Cleaver _finishes the wounded_ and refunds time on kills.\\n" +
				"\\n" +
				"The Blueprints page waits in the alchemy guide from the very start of a run. No victories required, as always."));

		ChangeInfo changes122 = new ChangeInfo("v1.2.2", true, "");
		changes122.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes122);

		changes122.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.AMULET), "Pacts, Front and Center",
				"The challenges and pacts window could only be reached through the small options gear on the hero select screen - easy to miss entirely on a phone, and missing altogether before your first finished run. There is now a labelled _Challenges & Pacts_ button at the top of the hero select screen, always there when a hero is chosen."));

		ChangeInfo changes121 = new ChangeInfo("v1.2.1", true, "");
		changes121.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes121);

		changes121.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.AMULET), "Pacts, Open to All",
				"An apology and a fix. In v1.2.0 the Pacts of Atlas were locked until you had conquered the dungeon once - which hid this edition's newest system from exactly the new players it was made for. Pacts are now open to everyone from the very first run.\n" +
				"\n" +
				"Challenges still unlock with a first victory, as they always have."));

		ChangeInfo changes120 = new ChangeInfo("v1.2.0", true, "");
		changes120.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes120);

		changes120.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.AMULET), "Pacts of Atlas",
				"King Atlas sealed his kingdom with oaths, and oaths still hold power in the buried halls. Before a run, open the challenges window and swear up to 3 _Pacts of Atlas_ - bargains that grant a boon and exact a price:\n" +
				"\n" +
				"The Open Map reveals every floor's layout, but enemies strike harder. The Buried Hoard swells every gold pile, but shop prices rise with it. Thorns answer your wounds in kind, but healing runs weaker. Eight pacts in all - mix them with challenges for runs that play by new rules.\n" +
				"\n" +
				"Pacts are picked in the challenges window before a run, and mix freely with challenges."));

		changes120.addButton( new ChangeButton( ChangeIcons.V081_MISC, "Using Abilities & Targeting",
				"The Tome of Dungeon Mastery has a new page: _Using Abilities & Targeting_. It explains melee and ranged targeting, which heroes bear weapon abilities, and how armor abilities are earned - the page is yours as soon as you pick the tome up."));

		changes120.addButton( new ChangeButton( ChangeIcons.V074_SHPX, "Small Mercies",
				"Unidentified items now note the floor they were found on, so a mystery potion or scroll is easier to place."));

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
