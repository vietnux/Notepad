/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tglt.notepad.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.tglt.notepad.ads.AdmobLib;
import com.tglt.notepad.ads.StartappLib;
import com.tglt.notepad.old.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

/**
 * Utility class to get a list of MusicTrack's based on a server-side JSON
 * configuration.
 */
public class RemoteJSONSource extends AsyncTask<String, Long, String> {

    private static String URL = "http://thegioilaptrinh.net/policy/configads_notepad.json";
    private static final String JSON_DATA = "data";
    private static final String JSON_AD_UNITS = "ad_units";
    private static final String JSON_ADMOBID = "admobid";
    private static JSONObject jsonTracks = null;

    Activity context;

    // Progress Dialog
    private ProgressDialog pDialog;

    public RemoteJSONSource(Activity context) {
        this.context = context;
    }

    public Iterator<JsonParams> getUrl( ) {
        try {
//            int slashPos = this.URL.lastIndexOf('/');
//            String path = this.URL.substring(0, slashPos + 1);
            JSONObject jsonObj = fetchJSONFromUrl(this.URL);
            if (jsonObj != null) {
                this.jsonTracks = jsonObj.getJSONObject(JSON_DATA);
//                Log.e("Lỗi", "OK get param " + this.jsonTracks );

                JsonParams.setData(this.jsonTracks);
//                if (jsonTracks != null) {
//                    for (int j = 0; j < jsonTracks.length(); j++) {
//                        tracks.add(buildFromJSON(jsonTracks.getJSONObject(j), path));
//                    }
//                }
            }
        } catch (JSONException e) {
            throw new RuntimeException("Could not retrieve music list", e);
        }
        return null;
    }

    /**
     * Download a JSON file from a server, parse the content and return the JSON
     * object.
     *
     * @return result JSONObject containing the parsed representation.
     */
    private JSONObject fetchJSONFromUrl(String urlString) throws JSONException {
//        if(!BuildConfig.BUILD_TYPE.equals("release") ) {
//            return new JSONObject( "{\"banner\":\"admob\",\"interstitial\":\"admob\",\"native\":\"admob\",\"startapp_id\":\"204294420\",\"download\":\"1\",\"popup\":\"1\",\"list_menu_popup\":\"1\",\"ad_units\":{\"ios\":{\"banner\":\"\",\"interstitial\":\"\",\"nativeAd\":\"\"},\"android\":{\"banner\":\"\",\"interstitial\":\"\",\"nativeAd\":\"\"}},\"admobid\":{\"android\":{\"banner\":\"\",\"interstitial\":\"ca-app-pub-1656614863517753\\/3578818989\",\"rewardvideo\":\"\"},\"ios\":{\"banner\":\"\",\"interstitial\":\"\",\"rewardvideo\":\"\"}},\"startapp\":{\"android\":{\"banner\":\"\",\"interstitial\":\"1\",\"rewardvideo\":\"\"},\"ios\":{\"banner\":\"\",\"interstitial\":\"\",\"rewardvideo\":\"\"}},\"position\":{\"begin\":\"1\",\"view\":\"0\",\"down\":\"1\"}}");
//        }
//        loadingstart();
//        Log.i("Url", urlString);
        BufferedReader reader = null;
        try {
            URLConnection urlConnection = new URL(urlString).openConnection();
            reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream(), "iso-8859-1"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
//            loadingstop();
            return new JSONObject(sb.toString());
        } catch (JSONException e) {
            throw e;
        } catch (Exception e) {
//            loadingstop();
            pDialog.dismiss();
            Log.e("Lỗi", "Failed to parse the json for media list", e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
//            loadingstop();
        }
    }
    @Override
    protected String doInBackground(String... params) {
        getUrl();
        return "";
    }

    //loading start
    void loadingstart() { }
    void loadingstop() { }

    @Override
    protected void onProgressUpdate(Long... values) { super.onProgressUpdate(values);}
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = ProgressDialog.show( context, "Loading...", "Please wait...", true, false);
    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute( result );

//        AdsLib.getInstance( context ).interstitial( true, pDialog );
        if( JsonParams.getParamInt("begin") == 1 ) {
            //fb ads
//            new FacebookAudienceAdsLibs( context ).adsInterstitial( pDialog );
            new StartappLib(context).interstitial(true, pDialog);

            //admob ads
            AdmobLib.getInstance(context).interstitial( true, pDialog );

        } else if( JsonParams.getParamInt("begin") == 2 ) {
            AdmobLib.getInstance(context).fetchAd(pDialog);;
//            AdmobLib.getInstance(context).showAdIfAvailable();
        } else {
            pDialog.dismiss();
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
        }

    }
}
