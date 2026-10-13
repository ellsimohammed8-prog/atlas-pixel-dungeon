/*
 * Atlas Pixel Dungeon — ad slot service (interface)
 *
 * Ads in Atlas are deliberately restrained: a single interstitial slot that
 * may appear only when a run has ended and the player is back at the title
 * screen. Never during gameplay, never as a banner.
 */
package com.shatteredpixel.shatteredpixeldungeon.services.ads;

public abstract class AdService {

	/** Show a full-screen interstitial NOW. Called only from the title scene
	 *  after the policy in Ads has approved the slot. Implementations should
	 *  quietly do nothing if no ad is loaded. */
	public abstract void showInterstitial();

	/** Optional: warm up an ad for the next slot. */
	public void preload() {}
}
