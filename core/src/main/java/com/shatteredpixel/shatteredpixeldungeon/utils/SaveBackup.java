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

package com.shatteredpixel.shatteredpixeldungeon.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Base64Coder;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Journal;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class SaveBackup {

	public static final String PREFIX = "ATLAS_SAVE:";
	public static final String TYPE_RUN = "RUN";
	public static final String TYPE_PROFILE = "PROFILE";

	public static final String RUN_BACKUP_FILE = "atlas_run_backup.dat";
	public static final String PROFILE_BACKUP_FILE = "atlas_profile_backup.dat";

	public static String bundleToString(Bundle bundle) {
		if (bundle == null) return null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Bundle.write(bundle, baos);
			return PREFIX + new String(Base64Coder.encode(baos.toByteArray()));
		} catch (Exception e) {
			ShatteredPixelDungeon.reportException(e);
			return null;
		}
	}

	public static Bundle stringToBundle(String str) {
		if (str == null) return null;
		try {
			str = str.trim();
			if (str.startsWith(PREFIX)) {
				str = str.substring(PREFIX.length()).trim();
			}
			byte[] bytes = Base64Coder.decode(str);
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			return Bundle.read(bais);
		} catch (Exception e) {
			ShatteredPixelDungeon.reportException(e);
			return null;
		}
	}

	public static Bundle createSlotBundle(int slot) {
		try {
			if (Dungeon.hero != null && GamesInProgress.curSlot == slot) {
				Dungeon.saveAll();
			}
			if (!GamesInProgress.gameExists(slot)) return null;

			Bundle bundle = new Bundle();
			bundle.put("atlas_backup_type", TYPE_RUN);
			bundle.put("version", ShatteredPixelDungeon.versionCode);
			bundle.put("slot", slot);

			Bundle gameBundle = FileUtils.bundleFromFile(GamesInProgress.gameFile(slot));
			bundle.put("game_dat", gameBundle);

			ArrayList<String> depthNames = new ArrayList<>();
			String folder = GamesInProgress.gameFolder(slot);
			ArrayList<String> files = FileUtils.filesInDir(folder);
			for (String file : files) {
				if (file.startsWith("depth")) {
					depthNames.add(file);
					Bundle dBundle = FileUtils.bundleFromFile(folder + "/" + file);
					bundle.put("file_" + file, dBundle);
				}
			}
			bundle.put("depth_files", depthNames.toArray(new String[0]));
			return bundle;
		} catch (Exception e) {
			ShatteredPixelDungeon.reportException(e);
			return null;
		}
	}

	public static Bundle createProfileBundle() {
		try {
			Badges.saveGlobal(true);
			Rankings.INSTANCE.save();
			Journal.saveGlobal(true);

			Bundle bundle = new Bundle();
			bundle.put("atlas_backup_type", TYPE_PROFILE);
			bundle.put("version", ShatteredPixelDungeon.versionCode);

			if (FileUtils.fileExists(Badges.BADGES_FILE)) {
				bundle.put("badges", FileUtils.bundleFromFile(Badges.BADGES_FILE));
			}
			if (FileUtils.fileExists(Rankings.RANKINGS_FILE)) {
				bundle.put("rankings", FileUtils.bundleFromFile(Rankings.RANKINGS_FILE));
			}
			if (FileUtils.fileExists(Journal.JOURNAL_FILE)) {
				bundle.put("journal", FileUtils.bundleFromFile(Journal.JOURNAL_FILE));
			}
			return bundle;
		} catch (Exception e) {
			ShatteredPixelDungeon.reportException(e);
			return null;
		}
	}

	public static int restoreSlotBundle(Bundle bundle, int targetSlot) {
		try {
			if (bundle == null || !TYPE_RUN.equals(bundle.getString("atlas_backup_type"))) {
				return -1;
			}
			if (targetSlot <= 0 || targetSlot > GamesInProgress.MAX_SLOTS) {
				targetSlot = GamesInProgress.firstEmpty();
				if (targetSlot <= 0) targetSlot = 1;
			}

			Bundle gameBundle = bundle.getBundle("game_dat");
			if (gameBundle == null) return -1;
			FileUtils.bundleToFile(GamesInProgress.gameFile(targetSlot), gameBundle);

			String[] depthFiles = bundle.getStringArray("depth_files");
			if (depthFiles != null) {
				String folder = GamesInProgress.gameFolder(targetSlot);
				for (String df : depthFiles) {
					Bundle dBundle = bundle.getBundle("file_" + df);
					if (dBundle != null) {
						FileUtils.bundleToFile(folder + "/" + df, dBundle);
					}
				}
			}

			GamesInProgress.setUnknown(targetSlot);
			GamesInProgress.check(targetSlot);
			return targetSlot;
		} catch (Exception e) {
			ShatteredPixelDungeon.reportException(e);
			return -1;
		}
	}

	public static boolean restoreProfileBundle(Bundle bundle) {
		try {
			if (bundle == null || !TYPE_PROFILE.equals(bundle.getString("atlas_backup_type"))) {
				return false;
			}
			if (bundle.contains("badges")) {
				FileUtils.bundleToFile(Badges.BADGES_FILE, bundle.getBundle("badges"));
				Badges.reloadGlobal();
			}
			if (bundle.contains("rankings")) {
				FileUtils.bundleToFile(Rankings.RANKINGS_FILE, bundle.getBundle("rankings"));
				Rankings.INSTANCE.load();
			}
			if (bundle.contains("journal")) {
				FileUtils.bundleToFile(Journal.JOURNAL_FILE, bundle.getBundle("journal"));
				Journal.reloadGlobal();
			}
			return true;
		} catch (Exception e) {
			ShatteredPixelDungeon.reportException(e);
			return false;
		}
	}

	public static boolean saveSlotToFile(int slot, String fileName) {
		Bundle b = createSlotBundle(slot);
		if (b == null) return false;
		try {
			FileUtils.bundleToFile(fileName, b);
			return true;
		} catch (Exception e) {
			ShatteredPixelDungeon.reportException(e);
			return false;
		}
	}

	public static boolean saveProfileToFile(String fileName) {
		Bundle b = createProfileBundle();
		if (b == null) return false;
		try {
			FileUtils.bundleToFile(fileName, b);
			return true;
		} catch (Exception e) {
			ShatteredPixelDungeon.reportException(e);
			return false;
		}
	}

	public static Bundle loadBundleFromFile(String fileName) {
		try {
			if (!FileUtils.fileExists(fileName)) return null;
			return FileUtils.bundleFromFile(fileName);
		} catch (Exception e) {
			ShatteredPixelDungeon.reportException(e);
			return null;
		}
	}
}
