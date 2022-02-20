package com.tglt.notepad.ads;

import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tglt.notepad.utils.JsonParams;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.AutoInterstitialPreferences;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

import com.tglt.notepad.BuildConfig;

import java.util.ArrayList;
import java.util.Random;


public class StartappLib {
    private static String TAG = "StartAppLibActivity";
    public static final boolean DEBUG = !BuildConfig.BUILD_TYPE.equals("release");

    private static StartappLib INSTANCE = null;

    public static JsonParams jparam = new JsonParams();
    static Activity activity;
    ProgressDialog pDialog;

    private LinearLayout adViewNative;
    private StartAppNativeAd startAppNativeAd;
    private StartAppAd startAppAd;

    public static StartappLib getInstance(Activity activity ) {
        if (INSTANCE == null) {
            INSTANCE = new StartappLib( activity );
        }
        return(INSTANCE);
    }

    public StartappLib(Activity activity ) {
        StartappLib.activity = activity;
        init();
    }
    private void init() {
        TAG = activity.getClass().getSimpleName();
        StartAppSDK.setTestAdsEnabled( BuildConfig.DEBUG );
        StartAppSDK.setUserConsent (activity,"pas", System.currentTimeMillis(),true);//
        StartAppSDK.init(activity, JsonParams.getParam("startapp_id"), false);

        StartAppAd.disableSplash();//không cho cái ads splash tự động

        if( JsonParams.getParamInt("auto") > 0 ) {
            //"Autostitial" stands for "Auto Interstitial"; use this integration to show an Interstitial Ad each time an activity is changed.
            StartAppAd.enableAutoInterstitial();
            //You can set a minimum time interval between consecutive Autostitial Ads.
            //For example, set a 1 minute interval between two consecutive ads (time in seconds)
            StartAppAd.setAutoInterstitialPreferences(new AutoInterstitialPreferences().setSecondsBetweenAds( JsonParams.getParamInt("auto") ));
        }

        startAppNativeAd = new StartAppNativeAd(activity);
        startAppAd = new StartAppAd(activity);
    }

    public void interstitial(final boolean show, ProgressDialog pDl ) {
        if(  ( JsonParams.DATA == null || !JsonParams.getParam("interstitial").equals("startapp") || JsonParams.getParam("startapp.android.interstitial") == null ) ) {
            return;
        } else {
            if( pDl != null ) {
                pDialog = pDl;
            } else {
                pDialog = ProgressDialog.show(activity, "Loading...", "Please wait...", true, false);
            }
        }

        startAppAd.loadAd (new AdEventListener() {
            @Override
            public void onReceiveAd(Ad ad) {
                startAppAd.showAd(new AdDisplayListener() {
                    @Override
                    public void adHidden(Ad ad) {
                    }
                    @Override
                    public void adDisplayed(Ad ad) {
                    }
                    @Override
                    public void adClicked(Ad ad) {
                    }
                    @Override
                    public void adNotDisplayed(Ad ad) {
                    }
                });
                if( pDialog != null && pDialog.isShowing() ) {
                    pDialog.dismiss();
                }
            }
            @Override
            public void onFailedToReceiveAd(Ad ad) {
                if( pDialog != null && pDialog.isShowing() ) {
                    pDialog.dismiss();
                }
            }
        });


    }

    public void adsInterstitialRandom( ) {
        int random = new Random().nextInt(5 ); //int n = rand.nextInt(20); // Gives n such that 0 <= n < 20
        if ( random == 1) {
            interstitial(true, null);
        }
    }

    public void nativeads(final int native_id) {
// Declare Native Ad Preferences
        NativeAdPreferences nativePrefs = new NativeAdPreferences()
                .setAdsNumber(3)                // Load 3 Native Ads
                .setAutoBitmapDownload(true)    // Retrieve Images object
                .setPrimaryImageSize(2);        // 150x150 image

// Declare Ad Callbacks Listener
        AdEventListener adListener = new AdEventListener() {     // Callback Listener
            @Override
            public void onReceiveAd(Ad arg0) {
                // Get the native ad
                NativeAdDetails nativeAd = null;
                ArrayList<NativeAdDetails> nativeAdsList = startAppNativeAd.getNativeAds();
                if (nativeAdsList.size() > 0) {
                    nativeAd = nativeAdsList.get(0);
                }

                // Verify that an ad was retrieved
                if (nativeAd == null) {
                    return;
                }

                final LinearLayout layout = activity.findViewById(native_id);
                final ImageView imageView = new ImageView(activity);
                imageView.setImageBitmap(nativeAd.getImageBitmap());
                layout.addView(imageView);

                final TextView textView = new TextView(activity);
                final StringBuilder builder = new StringBuilder();
                builder
                        .append("Title: ").append(nativeAd.getTitle()).append("\n\n")
                        .append("Description: ").append(nativeAd.getDescription()).append("\n\n")
                        .append("Rating: ").append(nativeAd.getRating()).append("\n\n")
                        .append("ImageUrl: ").append(nativeAd.getImageUrl()).append("\n\n")
                        .append("SecondaryImageUrl: ").append(nativeAd.getSecondaryImageUrl());

                textView.setText(builder);
                layout.addView(textView);

                nativeAd.registerViewForInteraction(layout);
            }

            @Override
            public void onFailedToReceiveAd(Ad arg0) {
                // Error occurred while loading the native ad
                final LinearLayout layout = activity.findViewById(native_id);
                final TextView textView = new TextView(activity);
                textView.setText("Error while loading Native Ad");
                layout.addView(textView);
            }
        };

        startAppNativeAd.loadAd(nativePrefs, adListener);
    }

}
