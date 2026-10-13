package com.shatteredpixel.shatteredpixeldungeon.services.ads;

import com.badlogic.gdx.Gdx;

/** Desktop/debug ad backend: no network, just logs when a slot fires so the
 *  placement policy can be verified in play-testing. */
public class DebugAds extends AdService {

	@Override
	public void showInterstitial() {
		Gdx.app.log("AtlasAds", "INTERSTITIAL SLOT FIRED (debug backend: no real ad on desktop)");
	}
}
