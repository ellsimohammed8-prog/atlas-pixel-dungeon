/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Atlas Pixel Dungeon
 * Copyright (C) 2026 Mohammed Guejjim
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

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.SaveBackup;
import com.watabou.noosa.ColorBlock;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class WndSaveBackup extends Window {

	private static final int WIDTH_P = 130;
	private static final int WIDTH_L = 170;
	private static final int BTN_HEIGHT = 16;
	private static final float GAP = 2;

	private RenderedTextBlock statusMsg;

	public WndSaveBackup() {
		super();

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((width - title.width()) / 2, GAP);
		add(title);

		ColorBlock sep1 = new ColorBlock(width, 1, 0xFF000000);
		sep1.x = 0;
		sep1.y = title.bottom() + 2 * GAP;
		add(sep1);

		RenderedTextBlock desc = PixelScene.renderTextBlock(Messages.get(this, "desc"), 6);
		desc.maxWidth(width);
		desc.setPos(0, sep1.y + sep1.height() + GAP);
		add(desc);

		statusMsg = PixelScene.renderTextBlock("", 6);
		statusMsg.maxWidth(width);
		statusMsg.hardlight(0xFFFF88);
		statusMsg.setPos(0, desc.bottom() + GAP);
		add(statusMsg);

		float pos = desc.bottom() + 4 * GAP;

		// 1. Export Run (Clipboard)
		RedButton btnExportRunClip = new RedButton(Messages.get(this, "export_run_clip")) {
			@Override
			protected void onClick() {
				super.onClick();
				int slot = resolveExportSlot();
				if (slot <= 0) {
					setStatus(Messages.get(WndSaveBackup.this, "no_run"), 0xFF6666);
					return;
				}
				Bundle b = SaveBackup.createSlotBundle(slot);
				if (b != null) {
					String str = SaveBackup.bundleToString(b);
					Gdx.app.getClipboard().setContents(str);
					setStatus(Messages.get(WndSaveBackup.this, "run_copied"), 0x88FF88);
				} else {
					setStatus(Messages.get(WndSaveBackup.this, "import_err_invalid"), 0xFF6666);
				}
			}
		};
		btnExportRunClip.setRect(0, pos, width, BTN_HEIGHT);
		add(btnExportRunClip);
		pos = btnExportRunClip.bottom() + GAP;

		// 2. Export Run (File)
		RedButton btnExportRunFile = new RedButton(Messages.get(this, "export_run_file")) {
			@Override
			protected void onClick() {
				super.onClick();
				int slot = resolveExportSlot();
				if (slot <= 0) {
					setStatus(Messages.get(WndSaveBackup.this, "no_run"), 0xFF6666);
					return;
				}
				boolean ok = SaveBackup.saveSlotToFile(slot, SaveBackup.RUN_BACKUP_FILE);
				if (ok) {
					setStatus(Messages.format(Messages.get(WndSaveBackup.this, "run_saved"), SaveBackup.RUN_BACKUP_FILE), 0x88FF88);
				} else {
					setStatus(Messages.get(WndSaveBackup.this, "import_err_invalid"), 0xFF6666);
				}
			}
		};
		btnExportRunFile.setRect(0, pos, width, BTN_HEIGHT);
		add(btnExportRunFile);
		pos = btnExportRunFile.bottom() + GAP;

		// 3. Export Profile (Clipboard)
		RedButton btnExportProfClip = new RedButton(Messages.get(this, "export_profile_clip")) {
			@Override
			protected void onClick() {
				super.onClick();
				Bundle b = SaveBackup.createProfileBundle();
				if (b != null) {
					String str = SaveBackup.bundleToString(b);
					Gdx.app.getClipboard().setContents(str);
					setStatus(Messages.get(WndSaveBackup.this, "profile_copied"), 0x88FF88);
				} else {
					setStatus(Messages.get(WndSaveBackup.this, "import_err_invalid"), 0xFF6666);
				}
			}
		};
		btnExportProfClip.setRect(0, pos, width, BTN_HEIGHT);
		add(btnExportProfClip);
		pos = btnExportProfClip.bottom() + GAP;

		// 4. Export Profile (File)
		RedButton btnExportProfFile = new RedButton(Messages.get(this, "export_profile_file")) {
			@Override
			protected void onClick() {
				super.onClick();
				boolean ok = SaveBackup.saveProfileToFile(SaveBackup.PROFILE_BACKUP_FILE);
				if (ok) {
					setStatus(Messages.format(Messages.get(WndSaveBackup.this, "profile_saved"), SaveBackup.PROFILE_BACKUP_FILE), 0x88FF88);
				} else {
					setStatus(Messages.get(WndSaveBackup.this, "import_err_invalid"), 0xFF6666);
				}
			}
		};
		btnExportProfFile.setRect(0, pos, width, BTN_HEIGHT);
		add(btnExportProfFile);
		pos = btnExportProfFile.bottom() + 2 * GAP;

		ColorBlock sep2 = new ColorBlock(width, 1, 0xFF000000);
		sep2.x = 0;
		sep2.y = pos;
		add(sep2);
		pos = sep2.y + sep2.height() + GAP;

		// 5. Import from Clipboard
		RedButton btnImportClip = new RedButton(Messages.get(this, "import_clip")) {
			@Override
			protected void onClick() {
				super.onClick();
				if (!Gdx.app.getClipboard().hasContents()) {
					setStatus(Messages.get(WndSaveBackup.this, "import_err_empty"), 0xFF6666);
					return;
				}
				String text = Gdx.app.getClipboard().getContents();
				Bundle b = SaveBackup.stringToBundle(text);
				processImport(b);
			}
		};
		btnImportClip.setRect(0, pos, width, BTN_HEIGHT);
		add(btnImportClip);
		pos = btnImportClip.bottom() + GAP;

		// 6. Restore from File
		RedButton btnImportFile = new RedButton(Messages.get(this, "import_file")) {
			@Override
			protected void onClick() {
				super.onClick();
				Bundle runB = SaveBackup.loadBundleFromFile(SaveBackup.RUN_BACKUP_FILE);
				Bundle profB = SaveBackup.loadBundleFromFile(SaveBackup.PROFILE_BACKUP_FILE);
				if (runB == null && profB == null) {
					setStatus(Messages.get(WndSaveBackup.this, "no_file"), 0xFF6666);
				} else if (runB != null && profB == null) {
					processImport(runB);
				} else if (runB == null && profB != null) {
					processImport(profB);
				} else {
					processImport(runB);
					processImport(profB);
				}
			}
		};
		btnImportFile.setRect(0, pos, width, BTN_HEIGHT);
		add(btnImportFile);
		pos = btnImportFile.bottom() + 2 * GAP;

		resize(width, (int)pos);
	}

	private void setStatus(String text, int color) {
		statusMsg.text(text);
		statusMsg.hardlight(color);
	}

	private int resolveExportSlot() {
		if (Dungeon.hero != null && GamesInProgress.curSlot > 0) {
			return GamesInProgress.curSlot;
		}
		if (GamesInProgress.curSlot > 0 && GamesInProgress.gameExists(GamesInProgress.curSlot)) {
			return GamesInProgress.curSlot;
		}
		ArrayList<GamesInProgress.Info> list = GamesInProgress.checkAll();
		if (!list.isEmpty()) {
			return list.get(0).slot;
		}
		return -1;
	}

	private void processImport(Bundle b) {
		if (b == null) {
			setStatus(Messages.get(this, "import_err_invalid"), 0xFF6666);
			return;
		}
		String type = b.getString("atlas_backup_type");
		if (SaveBackup.TYPE_RUN.equals(type)) {
			int slot = GamesInProgress.firstEmpty();
			if (slot <= 0) slot = 1;
			int res = SaveBackup.restoreSlotBundle(b, slot);
			if (res > 0) {
				setStatus(Messages.format(Messages.get(this, "import_success_run"), res), 0x88FF88);
			} else {
				setStatus(Messages.get(this, "import_err_invalid"), 0xFF6666);
			}
		} else if (SaveBackup.TYPE_PROFILE.equals(type)) {
			boolean ok = SaveBackup.restoreProfileBundle(b);
			if (ok) {
				setStatus(Messages.get(this, "import_success_profile"), 0x88FF88);
			} else {
				setStatus(Messages.get(this, "import_err_invalid"), 0xFF6666);
			}
		} else {
			setStatus(Messages.get(this, "import_err_invalid"), 0xFF6666);
		}
	}
}
