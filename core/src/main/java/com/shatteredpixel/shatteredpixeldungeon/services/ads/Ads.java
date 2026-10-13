/*
 * Atlas Pixel Dungeon — ad slot policy
 *
 * Non-intrusive by design:
 *  - one interstitial slot, only after a run ends (death or victory),
 *    shown when the player returns to the title screen
 *  - never during gameplay, no banners, no rewarded slots
 *  - grace period: no ads during a player's first 3 completed runs
 *  - frequency cap: at most one interstitial every 4 minutes
 *  - very short runs (< 45 seconds) never trigger a slot
 */
package com.shatteredpixel.shatteredpixeldungeon.services.ads;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;

public class Ads {

	public static AdService service;

	public static boolean supportsAds() {
		return service != null;
	}

	private static final String KEY_RUNS = "atlas_ads_runs";
	private static final String KEY_LAST_SHOWN = "atlas_ads_last_shown";
	private static final String KEY_ENABLED = "atlas_ads_enabled";

	/** Player-facing toggle (Settings > Connectivity). Default: on. */
	public static boolean enabled() {
		return SPDSettings.getBoolean(KEY_ENABLED, true);
	}

	public static void setEnabled(boolean value) {
		SPDSettings.put(KEY_ENABLED, value);
	}

	private static final int GRACE_RUNS = 3;
	private static final long MIN_INTERVAL_MS = 4 * 60 * 1000;
	private static final long MIN_RUN_MS = 45 * 1000;

	private static long runStartedAt = 0;
	private static boolean pendingSlot = false;
	private static long pendingRunDuration = 0;

	public static void noteRunStarted() {
		runStartedAt = System.currentTimeMillis();
		pendingSlot = false;
	}

	/** Called from Dungeon.fail / Dungeon.win exactly once per finished run. */
	public static void onRunEnded(boolean victory) {
		int runs = SPDSettings.getInt(KEY_RUNS, 0) + 1;
		SPDSettings.put(KEY_RUNS, runs);
		pendingRunDuration = runStartedAt > 0 ? System.currentTimeMillis() - runStartedAt : 0;
		runStartedAt = 0;
		pendingSlot = true;
	}

	/** Called when the title screen is created. Shows the slot if policy allows. */
	public static void tryShowOnTitle() {
		if (!pendingSlot) return;
		pendingSlot = false;
		if (!supportsAds()) return;
		if (!enabled()) return;
		int runs = SPDSettings.getInt(KEY_RUNS, 0);
		if (runs <= GRACE_RUNS) return;
		if (pendingRunDuration > 0 && pendingRunDuration < MIN_RUN_MS) return;
		long now = System.currentTimeMillis();
		long last = SPDSettings.getLong(KEY_LAST_SHOWN, 0);
		if (now - last < MIN_INTERVAL_MS) return;
		SPDSettings.put(KEY_LAST_SHOWN, now);
		try {
			service.showInterstitial();
		} catch (Exception e) {
			Gdx.app.log("AtlasAds", "interstitial failed: " + e.getMessage());
		}
	}
}
