package com.expressapps.presentexpress;

import android.app.Application;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

import com.expressapps.presentexpress.helper.Funcs;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        int theme = Funcs.getThemePreference(this);
        switch (theme) {
            case AppCompatDelegate.MODE_NIGHT_NO:
            case AppCompatDelegate.MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(theme);
                break;
            default:
                int nightModeFlags = getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;

                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                    default:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                }
                break;
        }
    }
}
