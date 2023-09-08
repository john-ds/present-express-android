package com.expressapps.presentexpress.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.expressapps.presentexpress.MainActivity;
import com.expressapps.presentexpress.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

public class Funcs {
    public static void showDialog(Context context, @StringRes int message, @StringRes int title,
        DialogInterface.OnClickListener listener, String positiveBtnText, @Nullable String negativeBtnText) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message).setTitle(title)
            .setPositiveButton(positiveBtnText, listener);

        if (negativeBtnText == null)
            builder.setCancelable(false);
        else builder.setNegativeButton(negativeBtnText, listener);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setLetterSpacing(0);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setLetterSpacing(0);
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
        DialogInterface.OnClickListener listener, String defaultText, int type, int margin) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(margin, 0, margin, 0);

        input.setLayoutParams(lp);
        input.setInputType(type);
        input.setText(defaultText);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("OK", listener);
        builder.setNegativeButton(R.string.cancel, listener);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setLetterSpacing(0);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setLetterSpacing(0);

        input.requestFocus();
        input.selectAll();
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
        } catch (Exception ignored) {}
    }
}
