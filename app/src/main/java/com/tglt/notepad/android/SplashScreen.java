package com.tglt.notepad.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.tglt.notepad.BuildConfig;
import com.tglt.notepad.R;
import com.tglt.notepad.old.activity.MainActivity;
import com.tglt.notepad.utils.JsonParams;
import com.tglt.notepad.utils.RemoteJSONSource;
//import com.extractor.apkmanager.utils.UtilsUI;

//Yeah, I know this is weird code.
public class SplashScreen extends AppCompatActivity {
    private static final String TAG = "SplashScreen";
    public static final boolean DEBUG = !BuildConfig.BUILD_TYPE.equals("release");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash_screen);


        if( JsonParams.DATA == null ) {
            try {
                new RemoteJSONSource(this).execute("");
            } catch ( Exception e ) {
                if (DEBUG) Log.e(TAG, "Error ads..." + e.toString() );
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
            }
        } else {
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
//        FacebookAudienceAdsLibs.getInstance( this ).onDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onDestroy();
    }
}