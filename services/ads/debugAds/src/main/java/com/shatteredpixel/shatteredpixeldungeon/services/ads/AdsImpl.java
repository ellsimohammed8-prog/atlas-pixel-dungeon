package com.shatteredpixel.shatteredpixeldungeon.services.ads;

public class AdsImpl {

	private static AdService adService = new DebugAds();

	public static AdService getAdService() {
		return adService;
	}

	public static boolean supportsAds() {
		return true;
	}
}
