package com.shatteredpixel.shatteredpixeldungeon.services.ads;

import android.app.Activity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Atlas Pixel Dungeon — AdMob backend for the single interstitial slot.
 *
 * Consent (UMP): on startup we run Google's User Messaging Platform flow.
 * In the EEA/UK this shows the GDPR consent form (which must be created in
 * the AdMob console under Privacy & messaging) before any ad is requested.
 * Ads are only initialized/loaded once consent allows it (canRequestAds).
 *
 * BEFORE RELEASE: replace the TEST unit id below with your own AdMob
 * interstitial unit id, and replace the test application id in
 * AndroidManifest.xml with your own AdMob app id.
 * See ~/workspace/spd-rebrand/release-checklist.md.
 */
public class AdMobAds extends AdService {

	// Google's public TEST interstitial unit — safe for development builds only.
	private static final String UNIT_ID = "ca-app-pub-3940256099942544/1033173712";

	private final Activity activity;
	private InterstitialAd loadedAd;
	private boolean loading = false;

	private ConsentInformation consentInformation;
	private final AtomicBoolean mobileAdsInitialized = new AtomicBoolean(false);

	public AdMobAds(Activity activity) {
		this.activity = activity;
		requestConsentAndInitialize();
	}

	private void requestConsentAndInitialize() {
		ConsentRequestParameters params = new ConsentRequestParameters.Builder()
				.setTagForUnderAgeOfConsent(false)
				.build();

		consentInformation = UserMessagingPlatform.getConsentInformation(activity);
		consentInformation.requestConsentInfoUpdate(
				activity,
				params,
				// Consent info updated: show the form if one is required.
				() -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(
						activity,
						loadAndShowError -> {
							// Consent gathering complete (form shown or not required).
							if (consentInformation.canRequestAds()) {
								initializeMobileAds();
							}
						}),
				// Consent info update failed: fall back to whatever state we have.
				requestConsentError -> {
					if (consentInformation.canRequestAds()) {
						initializeMobileAds();
					}
				});

		// On later launches consent is usually already gathered, so ads can
		// be requested immediately without waiting for the update round-trip.
		if (consentInformation.canRequestAds()) {
			initializeMobileAds();
		}
	}

	private void initializeMobileAds() {
		if (!mobileAdsInitialized.compareAndSet(false, true)) return;
		MobileAds.initialize(activity.getApplicationContext(), initializationStatus -> { });
		preload();
	}

	@Override
	public void preload() {
		if (!mobileAdsInitialized.get()) return;
		if (loading || loadedAd != null) return;
		loading = true;
		InterstitialAd.load(activity.getApplicationContext(), UNIT_ID,
				new AdRequest.Builder().build(),
				new InterstitialAdLoadCallback() {
					@Override
					public void onAdLoaded(InterstitialAd ad) {
						loadedAd = ad;
						loading = false;
					}

					@Override
					public void onAdFailedToLoad(LoadAdError error) {
						loadedAd = null;
						loading = false;
					}
				});
	}

	@Override
	public void showInterstitial() {
		if (!mobileAdsInitialized.get()) return;
		if (loadedAd != null) {
			final InterstitialAd ad = loadedAd;
			loadedAd = null;
			activity.runOnUiThread(() -> ad.show(activity));
			preload();
		} else {
			preload();
		}
	}
}
