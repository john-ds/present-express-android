package com.expressapps.presentexpress.helper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import com.expressapps.presentexpress.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

public class Funcs {
    public static void showDialog(Context context, @StringRes int message, @StringRes int title,
                                  DialogInterface.OnClickListener listener, String positiveBtnText, @Nullable String negativeBtnText) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setMessage(message).setTitle(title)
                .setPositiveButton(positiveBtnText, listener);

        if (negativeBtnText == null)
            builder.setCancelable(false);
        else builder.setNegativeButton(negativeBtnText, listener);

        AlertDialog dialog = builder.create();
        dialog.show();

        if (negativeBtnText != null)
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(
                    context.getResources().getColor(R.color.colorDark, context.getTheme()));

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
                context.getResources().getColor(R.color.colorDark, context.getTheme()));
    }

    public static void showDialog(Context context, @StringRes int message, @StringRes int title,
                                  DialogInterface.OnClickListener listener, @StringRes int positiveBtnText, @StringRes int negativeBtnText) {

        showDialog(context, message, title, listener,
                context.getString(positiveBtnText), context.getString(negativeBtnText));
    }

    public static void showDialog(Context context, @StringRes int message, @StringRes int title,
                                  DialogInterface.OnClickListener listener) {

        showDialog(context, message, title, listener, R.string.yes, R.string.no);
    }

    public static void showInputDialog(Context context, EditText input, @StringRes int title,
                                       DialogInterface.OnClickListener listener, String defaultText, int type) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(title);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(toDp(20), 0, toDp(20), 0);

        input.setLayoutParams(lp);
        input.setInputType(type);
        input.setText(defaultText);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("OK", listener);
        builder.setNegativeButton(R.string.cancel, listener);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(
                context.getResources().getColor(R.color.colorDark, context.getTheme()));
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
                context.getResources().getColor(R.color.colorDark, context.getTheme()));

        input.requestFocus();
        input.selectAll();
    }

    public static void showRadioDialog(Context context, @StringRes int[] options, @StringRes int title,
                                       DialogInterface.OnClickListener listener, int selectedIndex) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(title);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(toDp(20), 0, toDp(20), 0);
        container.setLayoutParams(lp);

        RadioGroup radioGroup = new RadioGroup(context);
        LinearLayout.LayoutParams radioGroupLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        radioGroupLayoutParams.setMargins(toDp(20), toDp(10), toDp(20), 0);
        radioGroup.setLayoutParams(radioGroupLayoutParams);

        for (int i = 0; i < options.length; i++) {
            MaterialRadioButton radioButton = new MaterialRadioButton(context);
            radioButton.setText(options[i]);
            radioButton.setId(i);
            radioButton.setPadding(toDp(16), 0, 0, 0);

            if (i == selectedIndex) radioButton.setChecked(true);
            radioGroup.addView(radioButton);
        }

        container.addView(radioGroup);
        builder.setView(container);

        builder.setPositiveButton("OK", (dialog, which) -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (listener != null) listener.onClick(dialog, selectedId);
        });
        builder.setNegativeButton(R.string.cancel, (d, b) -> d.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(
                context.getResources().getColor(R.color.colorDark, context.getTheme()));
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
                context.getResources().getColor(R.color.colorDark, context.getTheme()));
    }

    public static int toDp(int i) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (i * scale + 0.5f);
    }

    public static double convertToDouble(String s) {
        return Double.parseDouble(s.replace(',', '.'));
    }

    public static float transformRange(float value, float r1min, float r1max, float r2min, float r2max) {
        float scale = (r2max - r2min) / (r1max - r1min);
        return (value - r1min) * scale + r2min;
    }

    public static void newMessage(Context appContext, @StringRes int s, int length) {
        Toast.makeText(appContext, s, length).show();
    }

    public static void newLongMessage(Activity activity, @StringRes int s) {
        Snackbar.make(activity.findViewById(android.R.id.content), s, 4000).show();
    }

    public static void newEventLog(FirebaseAnalytics analytics, String id, String type) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
            analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        } catch (Exception ignored) {
        }
    }

    public static Uri getHelpGuideLink() {
        return Uri.parse("https://express.johnjds.co.uk/present/help/?topic=12&version=latest");
    }

    public static int getThemePreference(Context appContext) {
        SharedPreferences sharedPref = appContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return sharedPref.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static void setThemePreference(Activity appContext, int theme) {
        appContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit().putInt("theme", theme).apply();

        newLongMessage(appContext, R.string.restart_app);
    }
}
