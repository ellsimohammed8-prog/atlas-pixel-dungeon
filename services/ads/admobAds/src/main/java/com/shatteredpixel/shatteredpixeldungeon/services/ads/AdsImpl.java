package com.shatteredpixel.shatteredpixeldungeon.services.ads;

import android.app.Activity;

public class AdsImpl {

	private static Activity activity;
	private static AdService adService;

	/** Called once from AndroidLauncher before getAdService(). */
	public static void init(Activity launcherActivity) {
		activity = launcherActivity;
	}

	public static AdService getAdService() {
		if (adService == null) {
			adService = new AdMobAds(activity);
		}
		return adService;
	}

	public static boolean supportsAds() {
		return activity != null;
	}
}
