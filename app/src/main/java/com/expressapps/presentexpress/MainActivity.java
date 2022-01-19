package com.expressapps.presentexpress;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.DialogFragment;

import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.ByteArrayLoader;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.expressapps.presentexpress.ColorActivity;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.rustamg.filedialogs.FileDialog;
import com.rustamg.filedialogs.OpenFileDialog;
import com.rustamg.filedialogs.SaveFileDialog;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/* PRESENT EXPRESS MOBILE  v2.0.0 */
/* Part of Express Apps by John D */

public class MainActivity extends AppCompatActivity implements FileDialog.OnFileSelectedListener {
    private Toolbar mToolbar;
    public static ArrayList<HashMap<String, Object>> AllSlides = new ArrayList<>();
    public static final String SLIDES = "com.expressapps.presentexpress.slides";
    public static HashMap<String, Object> FileData = new HashMap<>();
    public static int backColor = Color.BLACK;
    public static boolean loop = true;
    public static boolean timings = true;
    public static boolean clear_slides = false;
    public static int editingImageIdx = 0;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_GradientStatusBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (!isStorageGranted()) requestStoragePermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private boolean checkPermissions(String permission) {
        int result = getApplicationContext().checkSelfPermission(permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isStorageGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(getString(R.string.storage_permission_info)).setTitle(getString(R.string.permission_needed))
                        .setPositiveButton("OK", dialogClickListener).setNegativeButton(R.string.cancel, dialogClickListener);

                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setLetterSpacing(0);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setLetterSpacing(0);
            }
        }
        else {
            newMessage(getString(R.string.no_permission), Toast.LENGTH_SHORT);
            ActivityCompat.requestPermissions((Activity) MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_PERMISSIONS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_open:
                GridLayout image_grid = findViewById(R.id.image_grid);
                if (image_grid.getChildCount() == 0) {

                    if (isStorageGranted()) {
                        OpenFileDialog openFileDialog = new OpenFileDialog();
                        Bundle args = new Bundle();
                        args.putString(FileDialog.EXTENSION, ".present");
                        openFileDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme);
                        openFileDialog.setArguments(args);
                        openFileDialog.show(getSupportFragmentManager(), OpenFileDialog.class.getName());

                        newMessage(getString(R.string.select_present), Toast.LENGTH_SHORT);
                        newEventLog("nav_open", "Open button clicked");

                    } else {
                        requestStoragePermission();
                    }

                    /*Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                    getIntent.setType("* / *");   // remove spaces here when uncommented
                    Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.select_present));
                    startActivityForResult(chooserIntent, PICK_PRESENT);

                    newMessage(getString(R.string.select_present), Toast.LENGTH_SHORT);
                    newEventLog("nav_open", "Open button clicked");*/

                } else {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    if (isStorageGranted()) {
                                        OpenFileDialog openFileDialog = new OpenFileDialog();
                                        Bundle args = new Bundle();
                                        args.putString(FileDialog.EXTENSION, ".present");
                                        openFileDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme);
                                        openFileDialog.setArguments(args);
                                        openFileDialog.show(getSupportFragmentManager(), OpenFileDialog.class.getName());

                                        newMessage(getString(R.string.select_present), Toast.LENGTH_SHORT);
                                        newEventLog("nav_open", "Open button clicked");

                                    } else {
                                        requestStoragePermission();
                                    }

                                    /*Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                    getIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                                    getIntent.setType("* / *");   // remove spaces here when uncommented
                                    Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.select_present));
                                    startActivityForResult(chooserIntent, PICK_PRESENT);

                                    newMessage(getString(R.string.select_present), Toast.LENGTH_SHORT);
                                    newEventLog("nav_open", "Open button clicked");*/
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(R.string.open_dialog).setTitle(R.string.open)
                            .setPositiveButton(R.string.yes, dialogClickListener).setNegativeButton(R.string.no, dialogClickListener);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setLetterSpacing(0);
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setLetterSpacing(0);
                }
                break;

            case R.id.nav_save:
                if (AllSlides.size() > 0) {
                    if (isStorageGranted()) {
                        SaveFileDialog openFileDialog = new SaveFileDialog();
                        Bundle args = new Bundle();
                        args.putString(FileDialog.EXTENSION, ".present");
                        openFileDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme);
                        openFileDialog.setArguments(args);
                        openFileDialog.show(getSupportFragmentManager(), SaveFileDialog.class.getName());
/*
                        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*//*");
                        startActivityForResult(intent, SAVE_PRESENT);*/
                        newEventLog("nav_save", "Save button clicked");

                    } else {
                        requestStoragePermission();
                    }

                } else {
                    newMessage(getString(R.string.add_slide), Toast.LENGTH_SHORT);
                }
                break;

            case R.id.nav_options:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, CLEAR_SLIDES);
                newEventLog("nav_options", "Settings button clicked");
                break;

            /*case R.id.nav_show:
                if (AllSlides.size() > 0) {
                    Intent intent = new Intent(this, SlideshowActivity.class);
                    startActivity(intent);

                } else {
                    newMessage(getString(R.string.add_slide), Toast.LENGTH_SHORT);
                }
                break;

            case R.id.nav_timings:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.set_timings);

                LinearLayout container = new LinearLayout(MainActivity.this);
                container.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(toDp(20), 0, toDp(20), 0);

                final EditText input = new EditText(MainActivity.this);
                input.setLayoutParams(lp);
                input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                input.setText("2.0");
                container.addView(input);
                builder.setView(container);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            double timing = convertToDouble(input.getText().toString());
                            if (timing > 10.0) {
                                timing = 10.0;
                                newMessage(getString(R.string.timings_max), Toast.LENGTH_SHORT);
                            } else if (timing < 0.5) {
                                timing = 0.5;
                                newMessage(getString(R.string.timings_min), Toast.LENGTH_SHORT);
                            }
                            for (HashMap<String, Object> i : AllSlides) {
                                i.put("timing", timing);
                            }

                        } catch (Exception ignored) {
                            newMessage(getString(R.string.timings_error), Toast.LENGTH_LONG);
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setLetterSpacing(0);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setLetterSpacing(0);
                input.requestFocus();
                input.selectAll();
                break;

            case R.id.nav_loop:
                item.setChecked(!item.isChecked());
                loop = item.isChecked();
                break;

            case R.id.nav_use_timings:
                item.setChecked(!item.isChecked());
                timings = item.isChecked();
                break;

            case R.id.nav_clear:
                if (AllSlides.size() > 0) {
                    DialogInterface.OnClickListener dialogClickListener2 = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    GridLayout img_grid = findViewById(R.id.image_grid);
                                    img_grid.removeAllViews();
                                    AllSlides.clear();
                                    FileData.clear();
                                    setItemNumbers();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                    builder2.setMessage(R.string.sure_clear).setTitle(R.string.clear_all_slides)
                            .setPositiveButton(R.string.yes, dialogClickListener2).setNegativeButton(R.string.no, dialogClickListener2);

                    AlertDialog dialog2 = builder2.create();
                    dialog2.show();
                    dialog2.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
                    dialog2.getButton(AlertDialog.BUTTON_POSITIVE).setLetterSpacing(0);
                    dialog2.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
                    dialog2.getButton(AlertDialog.BUTTON_NEGATIVE).setLetterSpacing(0);
                }
                break;

            case R.id.nav_about:
                Intent intent2 = new Intent(this, AboutActivity.class);
                startActivity(intent2);
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }


    // ADD IMAGES / OPEN

    @SuppressLint("IntentReset")
    public void addImage_click(View view) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        getIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.select_images));
        //@SuppressLint("IntentReset") Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //pickIntent.setType("image/*");
        //chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        selectedImageView = null;
        startActivityForResult(chooserIntent, PICK_IMAGE);
        newEventLog("addImage", "Add images button clicked");
    }

    public void show_click(View view) {
        if (AllSlides.size() > 0) {
            Intent intent = new Intent(this, SlideshowActivity.class);
            startActivity(intent);

        } else {
            newMessage(getString(R.string.add_slide), Toast.LENGTH_SHORT);
        }
    }

    public static final int PICK_IMAGE = 1;
    public static final int PICK_PRESENT = 2;
    public static final int COLOR_SELECTED = 3;
    public static final int SAVE_PRESENT = 4;
    public static final int READ_PERMISSIONS = 5;
    public static final int CLEAR_SLIDES = 6;
    public static final int EDIT_IMAGE = 7;
    public ImageView selectedImageView = null;
    public String chosenFilename = "";

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (data != null) {
            if (requestCode == PICK_IMAGE && selectedImageView == null) {
                ArrayList<Uri> list = new ArrayList<>();
                try {
                    boolean unsupported = false;

                    if (data.getData() != null) {
                        if (verifyImageFile(data.getData())) {
                            list.add(data.getData());
                        } else {
                            unsupported = true;
                        }

                    } else if (data.getClipData() != null) {
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            if (verifyImageFile(data.getClipData().getItemAt(i).getUri())) {
                                list.add(data.getClipData().getItemAt(i).getUri());
                            } else {
                                unsupported = true;
                            }
                        }
                    }

                    for (int i = 0; i < list.size(); i++) {
                        createSlide(list.get(i), null);
                    }

                    setItemNumbers();
                    if (unsupported) throw new Exception("Unsupported");

                } catch (Exception e) {
                    newMessage(getString(R.string.unsupported_images), Toast.LENGTH_LONG);
                }

            } else if (requestCode == PICK_IMAGE) {
                try {
                    if (verifyImageFile(data.getData())) {
                        final LinearLayout layout = (LinearLayout) selectedImageView.getParent().getParent();
                        final ConstraintLayout border = (ConstraintLayout) selectedImageView.getParent();
                        final GridLayout image_grid = findViewById(R.id.image_grid);
                        final Uri uri = data.getData();
                        border.setBackgroundResource(R.drawable.ic_loading_wide);

                        Glide.with(this)
                                .asBitmap()
                                .load(data.getData())
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        border.setBackgroundResource(R.drawable.border);
                                        AllSlides.get(image_grid.indexOfChild(layout)).put("bmp", resource);
                                        AllSlides.get(image_grid.indexOfChild(layout)).put("format", MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri)));
                                        AllSlides.get(image_grid.indexOfChild(layout)).put("type", "image");
                                        selectedImageView.setImageBitmap(resource);
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                    }
                                });
                    } else {
                        throw new Exception("Unsupported");
                    }
                } catch (Exception e) {
                    newMessage(getString(R.string.unsupported_image), Toast.LENGTH_LONG);
                }

            } /*else if (requestCode == PICK_PRESENT) {
                try {
                    if (verifyPresentFile(data.getData())) {
                        loadFile(data.getData());
                    } else {
                        throw new Exception("Unsupported");
                    }
                } catch (Exception e) {
                    newMessage(getString(R.string.document_error), Toast.LENGTH_LONG);
                }
            } else if (requestCode == SAVE_PRESENT) {
            }*/

        } else if (requestCode == CLEAR_SLIDES) {
            GridLayout image_grid = findViewById(R.id.image_grid);
            for (int i = 0; i < image_grid.getChildCount(); i++) {
                ConstraintLayout constraintLayout = (ConstraintLayout) ((LinearLayout) image_grid.getChildAt(i)).getChildAt(1);
                constraintLayout.setBackgroundColor(Color.WHITE);
                constraintLayout.setBackgroundResource(R.drawable.border);
            }

            if (clear_slides && AllSlides.size() > 0) {
                GridLayout img_grid = findViewById(R.id.image_grid);
                img_grid.removeAllViews();
                AllSlides.clear();
                FileData.clear();
                setItemNumbers();
                clear_slides = false;
            }

        } else if (requestCode == EDIT_IMAGE) {
            if (AllSlides.get(editingImageIdx).containsKey("filters")) {
                final LinearLayout layout = (LinearLayout) selectedImageView.getParent().getParent();
                final GridLayout image_grid = findViewById(R.id.image_grid);

                if (!AllSlides.get(editingImageIdx).containsKey("original")) {
                    AllSlides.get(image_grid.indexOfChild(layout)).put("original",
                            AllSlides.get(image_grid.indexOfChild(layout)).get("bmp"));
                }
                AllSlides.get(image_grid.indexOfChild(layout))
                        .put("bmp", applyFilters((Bitmap)AllSlides.get(image_grid.indexOfChild(layout)).get("original"),
                                (HashMap<String, Object>)AllSlides.get(image_grid.indexOfChild(layout)).get("filters")));

                selectedImageView.setImageBitmap((Bitmap)AllSlides.get(image_grid.indexOfChild(layout)).get("bmp"));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static Bitmap applyFilters(Bitmap originalImage, HashMap<String, Object> filtersApplied) {
        ColorMatrix clr = new ColorMatrix();

        switch ((String)filtersApplied.get("filter")) {
            case "Greyscale":
            case "Red":
            case "Green":
            case "Blue":
                clr.setSaturation(0);
                break;
            case "Sepia":
                clr.setSaturation(0);
                ColorMatrix matrixB = new ColorMatrix();
                matrixB.setScale(1f, .95f, .82f, 1.0f);
                clr.setConcat(matrixB, clr);
                break;
            case "BlackWhite":
                clr.setSaturation(0);
                ColorMatrix matrixC = new ColorMatrix(new float[] {
                        85.f, 85.f, 85.f, 0.f, -255.f * 90f,
                        85.f, 85.f, 85.f, 0.f, -255.f * 90f,
                        85.f, 85.f, 85.f, 0.f, -255.f * 90f,
                        0f, 0f, 0f, 1f, 0f
                });
                clr.setConcat(matrixC, clr);
                break;
        }

        /* ColorMatrix matrixD = new ColorMatrix(new float[] {
                0.5f..2f, 0, 0, 0, -127.5f..127.5f,
                0, 0.5f..2f, 0, 0, -127.5f..127.5f,
                0, 0, 0.5f..2f, 0, -127.5f..127.5f,
                0, 0, 0, 1, 0
        }); */

        float brightness = EditorActivity.transformRange((float)filtersApplied.get("brightness"), -0.5f, 0.5f, -127.5f, 127.5f);
        float contrast = (float)filtersApplied.get("contrast");

        ColorMatrix matrixD = new ColorMatrix(new float[] {
                contrast, 0, 0, 0, brightness,
                0, contrast, 0, 0, brightness,
                0, 0, contrast, 0, brightness,
                0, 0, 0, 1, 0
        });
        clr.setConcat(matrixD, clr);

        Matrix matrixRotate = new Matrix();
        matrixRotate.postRotate((int)filtersApplied.get("rotation"));
        matrixRotate.postScale((boolean)filtersApplied.get("fliph") ? -1 : 1,
                (boolean)filtersApplied.get("flipv") ? -1 : 1,
                originalImage.getWidth() / 2f, originalImage.getHeight() / 2f);

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        Bitmap ret = Bitmap.createBitmap(width, height, originalImage.getConfig());
        Canvas canvas = new Canvas(ret);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(clr));
        canvas.drawBitmap(originalImage, 0, 0, paint);

        switch ((String)filtersApplied.get("filter")) {
            case "Red":
                Paint redpaint = new Paint();
                redpaint.setColor(Color.argb(100, 235, 58, 52));
                canvas.drawRect(0, 0, width, height, redpaint);
                break;
            case "Green":
                Paint greenpaint = new Paint();
                greenpaint.setColor(Color.argb(100, 52, 235, 73));
                canvas.drawRect(0, 0, width, height, greenpaint);
                break;
            case "Blue":
                Paint bluepaint = new Paint();
                bluepaint.setColor(Color.argb(100, 52, 122, 235));
                canvas.drawRect(0, 0, width, height, bluepaint);
                break;
        }

        Bitmap rotateImage = Bitmap.createBitmap(ret, 0, 0, width, height, matrixRotate, true);
        ret.recycle();
        return rotateImage;
    }

    private void createSlide(Uri uri, Bitmap bmp) throws IOException {
        GridLayout image_grid = findViewById(R.id.image_grid);
        LinearLayout imgLayout = new LinearLayout(this);
        image_grid.addView(imgLayout);
        imgLayout.setOrientation(LinearLayout.HORIZONTAL);
        imgLayout.setPaddingRelative(0, toDp(20), toDp(20), 0);

        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.width = GridLayout.LayoutParams.MATCH_PARENT;
        layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        imgLayout.setLayoutParams(layoutParams);

        LinearLayout sidebarLayout = new LinearLayout(this);
        sidebarLayout.setOrientation(LinearLayout.VERTICAL);
        sidebarLayout.setMinimumWidth(toDp(40));

        LinearLayout.LayoutParams sidebarParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sidebarParams.setMargins(toDp(10), 0, toDp(10), 0);
        sidebarLayout.setLayoutParams(sidebarParams);
        imgLayout.addView(sidebarLayout);

        TextView imgText = new TextView(this);
        sidebarLayout.addView(imgText);
        imgText.setMinimumWidth(toDp(40));
        imgText.setText("#");
        imgText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        imgText.setTextSize(20);
        imgText.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        imgText.setTextColor(getColor(R.color.text_color));

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(0, toDp(5), 0, toDp(5));
        imgText.setLayoutParams(textParams);

        MaterialButton matButton = new MaterialButton(this);
        sidebarLayout.addView(matButton);
        matButton.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.white));
        matButton.setGravity(Gravity.CENTER_VERTICAL);
        matButton.setLetterSpacing(0f);
        matButton.setInsetTop(0);
        matButton.setInsetBottom(0);
        matButton.setPaddingRelative(toDp(5), toDp(5), toDp(5), toDp(5));
        matButton.setStateListAnimator(null);
        matButton.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_more_horiz_24));
        matButton.setIconPadding(0);
        matButton.setIconSize(toDp(30));
        matButton.setIconTint(ContextCompat.getColorStateList(MainActivity.this, R.color.text_color));
        matButton.setRippleColor(ContextCompat.getColorStateList(MainActivity.this, R.color.ripple_color));
        matButton.setCornerRadius(0);
        matButton.setOnClickListener(moreclick);

        LinearLayout.LayoutParams moreParams = new LinearLayout.LayoutParams(toDp(40), toDp(40));
        moreParams.gravity = Gravity.CENTER;
        matButton.setLayoutParams(moreParams);

        final ConstraintLayout border = new ConstraintLayout(this);
        imgLayout.addView(border);
        border.setId(View.generateViewId());
        border.setBackgroundResource(R.drawable.ic_loading_wide);
        border.setPaddingRelative(toDp(1), toDp(1), toDp(1), toDp(1));

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.gravity = Gravity.CENTER_HORIZONTAL;
        border.setLayoutParams(params1);

        final ImageView img = new ImageView(this);
        border.addView(img);
        img.setId(View.generateViewId());
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if (uri != null) {
            final HashMap<String, Object> imgdata = new HashMap<>();
            imgdata.put("type", "image");
            imgdata.put("name", generateNewName("image"));
            imgdata.put("format", MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri)));
            imgdata.put("timing", 2.0);

            Glide.with(this)
                    .asBitmap()
                    .load(uri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            border.setBackgroundColor(Color.WHITE);
                            border.setBackgroundResource(R.drawable.border);
                            ((GradientDrawable) ContextCompat.getDrawable(getBaseContext(), R.drawable.border)).setColor(backColor);
                            imgdata.put("bmp", resource);
                            img.setImageBitmap(resource);
                            AllSlides.add(imgdata);
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}
                    });

        } else if (bmp != null) {
            border.setBackgroundColor(Color.WHITE);
            border.setBackgroundResource(R.drawable.border);
            ((GradientDrawable) ContextCompat.getDrawable(getBaseContext(), R.drawable.border)).setColor(backColor);
            img.setImageBitmap(bmp);
        }

        ConstraintLayout.LayoutParams params2 = new ConstraintLayout.LayoutParams(0, 0);
        params2.setMargins(toDp(1), toDp(1), toDp(1), toDp(1));
        img.setLayoutParams(params2);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.connect(img.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraintSet.connect(img.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.connect(img.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(img.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.setDimensionRatio(img.getId(), "16:9");
        constraintSet.applyTo(border);

        img.setOnClickListener(imgclick);
    }

    private void loadFile(File file) {
        try {
            StringBuilder info = new StringBuilder();
            boolean containsOtherContent = false;
            boolean otherContentFailed = false;
            boolean imageContentFailed = false;

            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
            // For Uri --> ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(getContentResolver().openInputStream(file)));
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while(zipEntry != null){
                if (zipEntry.getName().equals("info.xml")) {
                    Scanner sc = new Scanner(zipInputStream);
                    while (sc.hasNextLine()) {
                        info.append(sc.nextLine() + "\n");
                    }
                }
                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
            zipInputStream.close();

            zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
            zipEntry = zipInputStream.getNextEntry();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(info.toString().replace("\uFEFF", "")));
            Document document = builder.parse(is);

            NodeList settings = document.getElementsByTagName("info").item(0).getChildNodes();
            FileData.clear();

            for (int i = 0; i < settings.getLength(); i++) {
                String settingtype = settings.item(i).getNodeName();

                if (settingtype.equals("color")) {
                    String[] colorVals = settings.item(i).getTextContent().split(",");
                    if (colorVals.length == 3) {
                        try {
                            backColor = Color.rgb(Integer.parseInt(colorVals[0]),
                                    Integer.parseInt(colorVals[1]),Integer.parseInt(colorVals[2]));

                            ((GradientDrawable) ContextCompat.getDrawable(getBaseContext(), R.drawable.border)).setColor(backColor);

                        } catch (Exception ignored) {}
                    }
                } else if (settingtype.equals("width")) {
                    if (settings.item(i).getTextContent().equals("120")) {
                        FileData.put("widescreen", false);
                    } else {
                        FileData.put("widescreen", true);
                    }
                } else if (settingtype.equals("fit")) {
                    if (settings.item(i).getTextContent().equalsIgnoreCase("true")) {
                        FileData.put("fit", true);
                    } else {
                        FileData.put("fit", false);
                    }
                } else if (settingtype.equals("loop")) {
                    if (settings.item(i).getTextContent().equalsIgnoreCase("true")) {
                        loop = true;
                    } else {
                        loop = false;
                    }
                } else if (settingtype.equals("timings")) {
                    if (settings.item(i).getTextContent().equalsIgnoreCase("true")) {
                        timings = true;
                    } else {
                        timings = false;
                    }
                }
            }

            NodeList slides = document.getElementsByTagName("slides").item(0).getChildNodes();
            clearSlides();

            for (int i = 0; i < slides.getLength(); i++) {
                String slidetype = slides.item(i).getNodeName();

                if (slidetype.equals("image") || slidetype.equals("screenshot")) {
                    NodeList nodes = slides.item(i).getChildNodes();
                    HashMap<String, Object> filters = new HashMap<>();
                    String filename = "";
                    double timing = 2.0;

                    for (int j = 0; j < nodes.getLength(); j++) {
                        if (nodes.item(j).getNodeName().equals("name")) {
                            filename = nodes.item(j).getTextContent();

                        } else if (nodes.item(j).getNodeName().equals("filters") && slidetype.equals("image")) {
                            NodeList filterNodes = nodes.item(j).getChildNodes();
                            filters.put("filter", "");
                            filters.put("brightness", 0f);
                            filters.put("contrast", 1f);
                            filters.put("rotation", 0);
                            filters.put("fliph", false);
                            filters.put("flipv", false);

                            for (int k = 0; k < filterNodes.getLength(); k++) {
                                if (filterNodes.item(k).getNodeName().equals("filter")) {
                                    if (Arrays.asList("Greyscale", "Sepia", "BlackWhite", "Red", "Green", "Blue")
                                            .contains(filterNodes.item(k).getTextContent())) {
                                        filters.put("filter", filterNodes.item(k).getTextContent());
                                    }
                                } else if (filterNodes.item(k).getNodeName().equals("brightness")) {
                                    try {
                                        float brightness = convertToFloat(filterNodes.item(k).getTextContent());
                                        if (brightness >= -0.5f && brightness <= 0.5f) {
                                            filters.put("brightness", brightness);
                                        }
                                    } catch (Exception ignored) {}

                                } else if (filterNodes.item(k).getNodeName().equals("contrast")) {
                                    try {
                                        float contrast = convertToFloat(filterNodes.item(k).getTextContent());
                                        if (contrast >= 0.5f && contrast <= 2f) {
                                            filters.put("contrast", contrast);
                                        }
                                    } catch (Exception ignored) {}

                                } else if (filterNodes.item(k).getNodeName().equals("rotation")) {
                                    if (Arrays.asList("0", "90", "180", "270")
                                            .contains(filterNodes.item(k).getTextContent())) {
                                        filters.put("rotation", Integer.parseInt(filterNodes.item(k).getTextContent()));
                                    }
                                } else if (filterNodes.item(k).getNodeName().equals("fliph")) {
                                    if (filterNodes.item(k).getTextContent().equalsIgnoreCase("true")) {
                                        filters.put("fliph", true);
                                    } else {
                                        filters.put("fliph", false);
                                    }
                                } else if (filterNodes.item(k).getNodeName().equals("flipv")) {
                                    if (filterNodes.item(k).getTextContent().equalsIgnoreCase("true")) {
                                        filters.put("flipv", true);
                                    } else {
                                        filters.put("flipv", false);
                                    }
                                }
                            }

                        } else if (nodes.item(j).getNodeName().equals("timing")) {
                            try {
                                timing = convertToDouble(nodes.item(j).getTextContent());
                            } catch (Exception ignored) {}
                        }
                    }
                    HashMap<String, Object> imgdata = new HashMap<>();
                    imgdata.put("type", slidetype);
                    imgdata.put("name", filename);
                    imgdata.put("format", filename.substring(filename.lastIndexOf(".") + 1));
                    imgdata.put("timing", timing);
                    if (filters.size() > 0) imgdata.put("filters", filters);
                    AllSlides.add(imgdata);

                } else if (slidetype.equals("chart") || slidetype.equals("drawing") || slidetype.equals("text")) {
                    containsOtherContent = true;

                    try {
                       //slides.item(i).normalize();
                       XPath xpath = XPathFactory.newInstance().newXPath();
                       XPathExpression expr = xpath.compile("//text()[normalize-space()='']");
                       NodeList nodeList = (NodeList) expr.evaluate(slides.item(i), XPathConstants.NODESET);

                       for (int n = 0; n < nodeList.getLength(); ++n) {
                           Node nd = nodeList.item(n);
                           nd.getParentNode().removeChild(nd);
                       }

                       Transformer transformer = TransformerFactory.newInstance().newTransformer();
                       transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                       transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

                       StringWriter writer = new StringWriter();
                       transformer.transform(new DOMSource(slides.item(i)), new StreamResult(writer));
                       String outerXML = writer.toString();

                       NodeList nodes = slides.item(i).getChildNodes();
                       String filename = "";
                       String inkfile = "";
                       double timing = 2.0;

                       for (int j = 0; j < nodes.getLength(); j++) {
                           if (nodes.item(j).getNodeName().equals("name")) {
                               if (slidetype.equals("drawing")) {
                                   inkfile = nodes.item(j).getTextContent();
                                   filename = nodes.item(j).getTextContent().replace(".isf", "-prender.png");
                               } else {
                                   filename = nodes.item(j).getTextContent() + "-prender.png";
                               }
                           } else if (nodes.item(j).getNodeName().equals("timing")) {
                               try {
                                   timing = convertToDouble(nodes.item(j).getTextContent());
                               } catch (Exception ignored) {
                               }
                           }
                       }
                       HashMap<String, Object> imgdata = new HashMap<>();
                       imgdata.put("type", slidetype);
                       imgdata.put("name", filename);
                       imgdata.put("format", "png");
                       imgdata.put("xmlout", outerXML);
                       imgdata.put("timing", timing);

                       if (slidetype.equals("drawing")) {
                           imgdata.put("inkname", inkfile);
                       }
                       AllSlides.add(imgdata);

                   } catch (Exception ignored) {
                       otherContentFailed = true;
                   }
                }
            }

            while(zipEntry != null){
                int idx = checkNameExists(zipEntry.getName());
                if (idx != -1) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(zipInputStream);
                        if (AllSlides.get(idx).containsKey("filters")) {
                            AllSlides.get(idx).put("original", bitmap);
                            AllSlides.get(idx).put("bmp", applyFilters(bitmap, (HashMap<String, Object>)AllSlides.get(idx).get("filters")));
                        } else {
                            AllSlides.get(idx).put("bmp", bitmap);
                        }
                    } catch (Exception ignored) {
                        imageContentFailed = true;
                    }
                } else {
                    idx = checkISFNameExists(zipEntry.getName());
                    if (idx != -1) {
                        try {
                            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                            int nRead;
                            byte[] data = new byte[1024];

                            while ((nRead = zipInputStream.read(data, 0, data.length)) != -1) {
                                buffer.write(data, 0, nRead);
                            }

                            buffer.flush();
                            byte[] targetArray = buffer.toByteArray();
                            AllSlides.get(idx).put("inkdata", targetArray);

                        } catch (Exception ignored) {
                            otherContentFailed = true;
                        }
                    }
                }
                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();
            }

            // clean slide list
            ArrayList<HashMap<String, Object>> toreplace = new ArrayList<>();
            for (HashMap<String, Object> i : AllSlides) {
                if (i.containsKey("bmp")) {
                    toreplace.add(i);
                } else {
                    String slidetype = i.get("name").toString();
                    if (slidetype.equals("image") || slidetype.equals("screenshot")) {
                        imageContentFailed = true;
                    } else {
                        otherContentFailed = true;
                    }
                }
            }
            AllSlides.clear();
            AllSlides.addAll(toreplace);

            for (HashMap<String, Object> i : AllSlides) {
                createSlide(null, (Bitmap) i.get("bmp"));
            }

            setItemNumbers();
            zipInputStream.closeEntry();
            zipInputStream.close();

            if (imageContentFailed) {
                newMessage(getString(R.string.corrupted_file), Toast.LENGTH_LONG);

            } else if (otherContentFailed) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle(getString(R.string.compat_mode))
                        .setMessage(getString(R.string.compat_mode_desc)).setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}
                        });
                AlertDialog alert = builder2.create();
                alert.show();

            } else if (containsOtherContent) {
                newMessage(getString(R.string.only_images), Toast.LENGTH_LONG);
            }

            GridLayout image_grid = findViewById(R.id.image_grid);
            for (int i = 0; i < image_grid.getChildCount(); i++) {
                ConstraintLayout constraintLayout = (ConstraintLayout) ((LinearLayout) image_grid.getChildAt(i)).getChildAt(1);
                constraintLayout.setBackgroundColor(Color.WHITE);
                constraintLayout.setBackgroundResource(R.drawable.border);
            }

        } catch (Exception e) {
            newMessage(getString(R.string.document_error), Toast.LENGTH_LONG);
        }
    }

    private void saveFile(File file) {
        try {
            file.delete();
            ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file, false)));

            for (HashMap<String, Object> i : AllSlides) {
                Bitmap.CompressFormat format = getFormat((String) i.get("format"));
                String formatstr = "jpg";
                if (format.equals(Bitmap.CompressFormat.PNG)) formatstr = "png";

                if (i.get("type").equals("image")) {
                    i.put("name", checkExistingNames(checkForExtension((String) i.get("name"), formatstr)));
                }
                ZipEntry imgentry = new ZipEntry((String) i.get("name"));
                zipOutputStream.putNextEntry(imgentry);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Bitmap bmp;
                if (i.containsKey("original")) {
                    bmp = (Bitmap) i.get("original");
                } else {
                    bmp = (Bitmap) i.get("bmp");
                }
                bmp.compress(format, 90, bos);

                byte[] img = bos.toByteArray();
                zipOutputStream.write(img, 0, img.length);
                zipOutputStream.closeEntry();

                if (i.containsKey("inkdata")) {
                    ZipEntry isfentry = new ZipEntry(((String) i.get("inkname")));
                    zipOutputStream.putNextEntry(isfentry);

                    byte[] isf = (byte[])i.get("inkdata");
                    zipOutputStream.write(isf, 0, isf.length);
                    zipOutputStream.closeEntry();
                }
            }

            ZipEntry entry = new ZipEntry("info.xml");
            zipOutputStream.putNextEntry(entry);
            StringBuilder info = new StringBuilder();
            info.append("<present><info>");
            info.append("<color>").append(Color.red(backColor)).append(",")
                    .append(Color.green(backColor)).append(",")
                    .append(Color.blue(backColor)).append("</color>");

            if (FileData.containsKey("widescreen")) {
                if ((boolean)FileData.get("widescreen")) {
                    info.append("<width>160</width><height>90</height>");
                } else {
                    info.append("<width>120</width><height>90</height>");
                }
            } else {
                info.append("<width>160</width><height>90</height>");
            }
            if (FileData.containsKey("widescreen")) {
                if ((boolean)FileData.get("fit")) {
                    info.append("<fit>True</fit>");
                } else {
                    info.append("<fit>False</fit>");
                }
            } else {
                info.append("<fit>True</fit>");
            }
            info.append("<loop>").append(formatBoolean(loop)).append("</loop>");
            info.append("<timings>").append(formatBoolean(timings)).append("</timings></info><slides>");

            for (HashMap<String, Object> i : AllSlides) {
                if (i.containsKey("xmlout")) {
                    info.append(((String)i.get("xmlout")).replaceFirst("<timing>[\\S\\s]+?<\\/timing>",
                            "<timing>" + ((Double) i.get("timing")).toString() + "</timing>"));
                } else {
                    info.append("<").append((String) i.get("type")).append(">");
                    info.append("<name>").append((String) i.get("name")).append("</name>");

                    if (i.containsKey("filters")) {
                        HashMap<String, Object> filters = (HashMap<String, Object>)i.get("filters");
                        info.append("<filters><filter>").append(filters.get("filter"))
                                .append("</filter><brightness>").append(((Float)filters.get("brightness")).toString())
                                .append("</brightness><contrast>").append(((Float)filters.get("contrast")).toString())
                                .append("</contrast><rotation>").append(((Integer)filters.get("rotation")).toString())
                                .append("</rotation><fliph>").append(formatBoolean((boolean)filters.get("fliph")))
                                .append("</fliph><flipv>").append(formatBoolean((boolean)filters.get("flipv")))
                                .append("</flipv></filters>");
                    }
                    info.append("<timing>").append(((Double) i.get("timing")).toString()).append("</timing>");
                    info.append("</").append((String) i.get("type")).append(">");
                }
            }
            info.append("</slides></present>");

            byte[] d = info.toString().getBytes();
            zipOutputStream.write(d, 0, d.length);
            zipOutputStream.closeEntry();
            zipOutputStream.close();

            newMessage(getString(R.string.file_saved), Toast.LENGTH_SHORT);

        } catch (Exception e) {
            newMessage(getString(R.string.save_error), Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onFileSelected(FileDialog dialog, File file) {
        if (dialog instanceof OpenFileDialog) {
            loadFile(file);
        } else if (dialog instanceof SaveFileDialog) {
            saveFile(file);
        }
    }


    // OTHER FUNCTIONS

    private String checkExistingNames(String s) {
        boolean exists = true;
        int counter = 1;
        String newstr = s;

        while (exists) {
            exists = false;
            for (HashMap<String, Object> i : AllSlides) {
                if (newstr.equals(i.get("name"))) {
                    newstr += counter + s;
                    counter++;
                    exists = true;
                    break;
                }
            }
        }
        return newstr;
    }

    public static double convertToDouble(String s) throws Exception {
        return Double.parseDouble(s.replace(',', '.'));
    }

    public static float convertToFloat(String s) throws Exception {
        return Float.parseFloat(s.replace(',', '.'));
    }

    private String checkForExtension(String name, String ext) {
        if (name.toLowerCase().endsWith("." + ext)) {
            return name;
        } else {
            return name + "." + ext;
        }
    }

    private String formatBoolean(boolean b) {
        if (b) {
            return "True";
        } else {
            return "False";
        }
    }

    private Bitmap.CompressFormat getFormat(String s) {
        if (s.toLowerCase().equals("jpeg") || s.toLowerCase().equals("jpg")) {
            return Bitmap.CompressFormat.JPEG;
        } else {
            return Bitmap.CompressFormat.PNG;
        }
    }

    private boolean verifyImageFile(Uri uri) {
        try {
            String[] formats = new String[]{"jpg", "jpeg", "png", "bmp", "gif"};
            String mimeType = getContentResolver().getType(uri);

            if ((DocumentFile.fromSingleUri(getApplicationContext(), uri).length() / 1024 / 1024) > 10) {
                return false;
            }

            for (String i : formats) {
                if (MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType).equals(i)) {
                    return true;
                }
            }
            return false;

        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean verifyPresentFile(Uri uri) {
        try {
            String[] formats = new String[]{"bin", "present", "zip"};
            String mimeType = getContentResolver().getType(uri);

            if ((DocumentFile.fromSingleUri(getApplicationContext(), uri).length() / 1024 / 1024) > 10) {
                return false;
            }

            for (String i : formats) {
                if (MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType).equals(i)) {
                    return true;
                }
            }
            return false;

        } catch (Exception ignored) {
            return false;
        }
    }

    private ArrayList<Bitmap> getAllBitmaps() {
        ArrayList<Bitmap> bmps = new ArrayList<>();
        for (HashMap<String, Object> i : AllSlides) {
            bmps.add((Bitmap) i.get("bmp"));
        }
        return bmps;
    }

    private int checkNameExists(String s) {
        for (HashMap<String, Object> i : AllSlides) {
            if (s.equals(i.get("name"))) {
                return AllSlides.indexOf(i);
            }
        }
        return -1;
    }

    private int checkISFNameExists(String s) {
        for (HashMap<String, Object> i : AllSlides) {
            if (s.equals(i.get("inkname"))) {
                return AllSlides.indexOf(i);
            }
        }
        return -1;
    }

    private String generateNewName(String s) {
        boolean exists = true;
        int counter = 1;
        String newstr = s;

        while (exists) {
            exists = false;
            for (HashMap<String, Object> i : AllSlides) {
                if (newstr.equals(i.get("name"))) {
                    newstr += s + "-" + counter;
                    counter++;
                    exists = true;
                    break;
                }
            }
        }
        return newstr;
    }

    private int toDp(int i) {
        final float scale = this.getResources().getDisplayMetrics().density;
        return (int) (i * scale + 0.5f);
    }

    private int fromDp(int i) {
        final float scale = this.getResources().getDisplayMetrics().density;
        return (int) (i / scale + 0.5f);
    }

    private void clearSlides() {
        GridLayout image_grid = findViewById(R.id.image_grid);
        image_grid.removeAllViews();
        AllSlides.clear();
    }

    private void setItemNumbers() {
        GridLayout image_grid = findViewById(R.id.image_grid);
        for (int i = 1; i <= image_grid.getChildCount(); i++) {
            LinearLayout layout = (LinearLayout) image_grid.getChildAt(i-1);
            LinearLayout sidebar = (LinearLayout) layout.getChildAt(0);
            TextView txt = (TextView) sidebar.getChildAt(0);
            String idx = "" + i;
            txt.setText(idx);
        }

        ScrollView txtView = findViewById(R.id.welcomelayout);
        FloatingActionButton fab = findViewById(R.id.fab2);

        if (image_grid.getChildCount() > 0) {
            txtView.setVisibility(View.INVISIBLE);
            fab.show();
        } else {
            txtView.setVisibility(View.VISIBLE);
            fab.hide();
        }
    }

    private void cleanSlideList() {
        ArrayList<HashMap<String, Object>> toreplace = new ArrayList<>();
        for (HashMap<String, Object> i : AllSlides) {
            if (i.containsKey("bmp")) {
                toreplace.add(i);
            }
        }
        AllSlides.clear();
        AllSlides.addAll(toreplace);
    }

    private void newMessage(String s, int length) {
        Toast toast = Toast.makeText(getApplicationContext(), s, length);
        toast.show();
    }

    private void newEventLog(String id, String type) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        } catch (Exception ignored) {}
    }

    private int currentSlide = 0;

    private View.OnClickListener moreclick = new View.OnClickListener() {
        public void onClick(final View v) {
            LinearLayout layout = (LinearLayout) v.getParent().getParent();
            ImageView img = (ImageView)((ConstraintLayout)layout.getChildAt(1)).getChildAt(0);
            img.performClick();
        }
    };

    private View.OnClickListener imgclick = new View.OnClickListener() {
        public void onClick(final View v) {
            GridLayout image_grid = findViewById(R.id.image_grid);
            LinearLayout layout = (LinearLayout) v.getParent().getParent();
            int idx = image_grid.indexOfChild(layout);

            PopupMenu itemPopup = new PopupMenu(getApplicationContext(), v);

            if (AllSlides.get(idx).get("type").equals("image")) {
                itemPopup.getMenu().add(0, 0, 0, R.string.edit_picture);
                itemPopup.getMenu().add(0, 1, 0, R.string.change_picture);
            }
            itemPopup.getMenu().add(0, 2, 0, R.string.slide_timing);
            itemPopup.getMenu().add(0, 3, 0, R.string.move_up);
            itemPopup.getMenu().add(0, 4, 0, R.string.move_down);
            itemPopup.getMenu().add(0, 5, 0, R.string.remove);

            itemPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case 0 : // Edit picture
                            Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                            intent.putExtra("idx", idx);

                            if (AllSlides.get(idx).containsKey("filters")) {
                                intent.putExtra("filter", (String)((HashMap<String, Object>)AllSlides.get(idx).get("filters")).get("filter"));
                                intent.putExtra("brightness", (float)((HashMap<String, Object>)AllSlides.get(idx).get("filters")).get("brightness"));
                                intent.putExtra("contrast", (float)((HashMap<String, Object>)AllSlides.get(idx).get("filters")).get("contrast"));
                                intent.putExtra("rotation", (int)((HashMap<String, Object>)AllSlides.get(idx).get("filters")).get("rotation"));
                                intent.putExtra("fliph", (boolean)((HashMap<String, Object>)AllSlides.get(idx).get("filters")).get("fliph"));
                                intent.putExtra("flipv", (boolean)((HashMap<String, Object>)AllSlides.get(idx).get("filters")).get("flipv"));
                            }
                            editingImageIdx = idx;
                            selectedImageView = (ImageView) v;
                            startActivityForResult(intent, EDIT_IMAGE);
                            newEventLog("nav_edit", "Photo editor opened");
                            break;

                        case 1 : // Change picture
                            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            getIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                            getIntent.setType("image/*");

                            Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.select_image));
                            selectedImageView = (ImageView) v;
                            startActivityForResult(chooserIntent, PICK_IMAGE);
                            break;

                        case 2 : // Set timings
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(R.string.set_timings);

                            LinearLayout container = new LinearLayout(MainActivity.this);
                            container.setOrientation(LinearLayout.VERTICAL);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(toDp(20), 0, toDp(20), 0);

                            final EditText input = new EditText(MainActivity.this);
                            input.setLayoutParams(lp);
                            input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                            input.setText(AllSlides.get(idx).get("timing").toString());
                            container.addView(input);
                            builder.setView(container);
                            currentSlide = idx;

                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        double timing = convertToDouble(input.getText().toString());
                                        if (timing > 10.0) {
                                            timing = 10.0;
                                            newMessage(getString(R.string.timings_max), Toast.LENGTH_SHORT);
                                        } else if (timing < 0.5) {
                                            timing = 0.5;
                                            newMessage(getString(R.string.timings_min), Toast.LENGTH_SHORT);
                                        }
                                        AllSlides.get(currentSlide).put("timing", timing);

                                    } catch (Exception ignored) {
                                        newMessage(getString(R.string.timings_error), Toast.LENGTH_LONG);
                                    }
                                }
                            });
                            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setLetterSpacing(0);
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setLetterSpacing(0);
                            input.requestFocus();
                            input.selectAll();
                            break;

                        case 3 : // Move up
                            if (idx != 0) {
                                image_grid.removeView(layout);
                                image_grid.addView(layout, idx-1);
                                Collections.swap(AllSlides, idx, idx-1);
                            }
                            setItemNumbers();
                            break;

                        case 4 : // Move down
                            if (idx+1 < image_grid.getChildCount()) {
                                image_grid.removeView(layout);
                                image_grid.addView(layout, idx+1);
                                Collections.swap(AllSlides, idx, idx+1);
                            }
                            setItemNumbers();
                            break;

                        case 5 : // Remove
                            image_grid.removeView(layout);
                            AllSlides.remove(idx);
                            setItemNumbers();
                            break;
                    }
                    return true;
                }
            });

            itemPopup.show();
        }
    };
}