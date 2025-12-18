package com.expressapps.presentexpress;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.expressapps.presentexpress.helper.Funcs;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.analytics.FirebaseAnalytics;

public class AboutActivity extends AppCompatActivity {
    private FirebaseAnalytics mFA;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        mFA = FirebaseAnalytics.getInstance(this);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar), (v, insets) -> {
            Insets in = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(in.left, 0, in.right, 0);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scroller), (v, insets) -> {
            Insets in = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(in.left, 0, in.right, in.bottom);
            return insets;
        });

        try {
            PackageInfo pInfo = getApplicationContext().getPackageManager()
                    .getPackageInfo(getApplicationContext().getPackageName(), 0);
            ((TextView) findViewById(R.id.version)).setText("Android v" + pInfo.versionName);

        } catch (PackageManager.NameNotFoundException e) {
            ((TextView) findViewById(R.id.version)).setText("Android");
        }
    }

    public void onWebsiteClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.johnjds.co.uk/express"));
        Funcs.newEventLog(mFA, "webOpen", "Website button clicked");
        startActivity(browserIntent);
        finish();
    }

    public void onHelpClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Funcs.getHelpGuideLink());
        Funcs.newEventLog(mFA, "helpOpen", "Help button clicked");
        startActivity(browserIntent);
        finish();
    }
}