package com.expressapps.presentexpress;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.expressapps.presentexpress.helper.Funcs;
import com.google.firebase.analytics.FirebaseAnalytics;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.TextView;

import java.util.Objects;

public class AboutActivity extends AppCompatActivity {
    private FirebaseAnalytics mFA;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_GradientStatusBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);
        mFA = FirebaseAnalytics.getInstance(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        try {
            PackageInfo pInfo = getApplicationContext().getPackageManager()
                .getPackageInfo(getApplicationContext().getPackageName(), 0);
            ((TextView)findViewById(R.id.version)).setText("v" + pInfo.versionName);

        } catch (PackageManager.NameNotFoundException e) {
            ((TextView) findViewById(R.id.version)).setText(R.string.unknown_version);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onWebsiteClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.johnjds.co.uk/express"));
        Funcs.newEventLog(mFA, "webOpen", "Website button clicked");
        startActivity(browserIntent);
        finish();
    }

    public void onHelpClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://express.johnjds.co.uk/present/help"));
        Funcs.newEventLog(mFA, "helpOpen", "Help button clicked");
        startActivity(browserIntent);
        finish();
    }
}