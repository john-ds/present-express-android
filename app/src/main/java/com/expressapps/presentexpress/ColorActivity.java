package com.expressapps.presentexpress;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;

import com.expressapps.presentexpress.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.view.View;
import android.widget.Button;

public class ColorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.choose_colour);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onItemClick(View v) {
        ColorDrawable colorDrawable = (ColorDrawable) ((Button) v).getBackground();
        ((GradientDrawable)getDrawable(R.drawable.border)).setColor(colorDrawable.getColor());
        MainActivity.backColor = colorDrawable.getColor();
        finishActivity(3);
        finish();
    }
}