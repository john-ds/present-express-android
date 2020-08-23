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
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.DialogFragment;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.expressapps.presentexpress.ColorActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.rustamg.filedialogs.FileDialog;
import com.rustamg.filedialogs.OpenFileDialog;
import com.rustamg.filedialogs.SaveFileDialog;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/* PRESENT EXPRESS v1.0.0 BETA */
/* Part of Express Apps by John D */

public class MainActivity extends AppCompatActivity implements FileDialog.OnFileSelectedListener {
    private Toolbar mToolbar;
    public static ArrayList<HashMap<String, Object>> AllSlides = new ArrayList<>();
    public static final String SLIDES = "com.expressapps.presentexpress.slides";
    public static int backColor = Color.BLACK;
    public static boolean loop = true;
    public static boolean timings = true;
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

        ActivityCompat.requestPermissions((Activity) MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_PERMISSIONS);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_open:
                GridLayout image_grid = findViewById(R.id.image_grid);
                if (image_grid.getChildCount() == 0) {

                    if (checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        OpenFileDialog openFileDialog = new OpenFileDialog();
                        Bundle args = new Bundle();
                        args.putString(FileDialog.EXTENSION, ".present");
                        openFileDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme);
                        openFileDialog.setArguments(args);
                        openFileDialog.show(getSupportFragmentManager(), OpenFileDialog.class.getName());

                        newMessage(getString(R.string.select_present), Toast.LENGTH_SHORT);
                        newEventLog("nav_open", "Open button clicked");

                    } else {
                        ActivityCompat.requestPermissions((Activity) MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSIONS);
                    }
                    /*
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                    getIntent.setType("*//*");
                    Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.select_present));
                    startActivityForResult(chooserIntent, PICK_PRESENT);
                    */

                } else {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    if (checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                        OpenFileDialog openFileDialog = new OpenFileDialog();
                                        Bundle args = new Bundle();
                                        args.putString(FileDialog.EXTENSION, ".present");
                                        openFileDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme);
                                        openFileDialog.setArguments(args);
                                        openFileDialog.show(getSupportFragmentManager(), OpenFileDialog.class.getName());

                                        newMessage(getString(R.string.select_present), Toast.LENGTH_SHORT);
                                        newEventLog("nav_open", "Open button clicked");

                                    } else {
                                        ActivityCompat.requestPermissions((Activity) MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSIONS);
                                    }
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(R.string.open_dialog).setTitle(R.string.open)
                            .setPositiveButton(R.string.yes, dialogClickListener).setNegativeButton(R.string.no, dialogClickListener).show();
                }
                break;

            case R.id.nav_save:
                if (AllSlides.size() > 0) {
                    if (checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        SaveFileDialog openFileDialog = new SaveFileDialog();
                        Bundle args = new Bundle();
                        args.putString(FileDialog.EXTENSION, ".present");
                        openFileDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme);
                        openFileDialog.setArguments(args);
                        openFileDialog.show(getSupportFragmentManager(), SaveFileDialog.class.getName());
                        newEventLog("nav_save", "Save button clicked");

                    } else {
                        ActivityCompat.requestPermissions((Activity) MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_PERMISSIONS);
                    }

                } else {
                    newMessage(getString(R.string.add_slide), Toast.LENGTH_SHORT);
                }
                break;

            case R.id.nav_show:
                if (AllSlides.size() > 0) {
                    Intent intent = new Intent(this, SlideshowActivity.class);
                    startActivity(intent);

                } else {
                    newMessage(getString(R.string.add_slide), Toast.LENGTH_SHORT);
                }
                break;

            case R.id.nav_colour:
                Intent intent = new Intent(this, ColorActivity.class);
                startActivityForResult(intent, COLOR_SELECTED);
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
                            double timing = Double.parseDouble(input.getText().toString());
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

                builder.show();
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
                                    setItemNumbers();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                    builder2.setMessage(R.string.sure_clear).setTitle(R.string.clear_all_slides)
                            .setPositiveButton(R.string.yes, dialogClickListener2).setNegativeButton(R.string.no, dialogClickListener2).show();
                }
                break;

            case R.id.nav_about:
                Intent intent2 = new Intent(this, AboutActivity.class);
                startActivity(intent2);
                break;
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
            }

        } else if (requestCode == COLOR_SELECTED) {
            GridLayout image_grid = findViewById(R.id.image_grid);
            for (int i = 0; i < image_grid.getChildCount(); i++) {
                ConstraintLayout constraintLayout = (ConstraintLayout) ((LinearLayout) image_grid.getChildAt(i)).getChildAt(1);
                constraintLayout.setBackgroundColor(Color.WHITE);
                constraintLayout.setBackgroundResource(R.drawable.border);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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

        TextView imgText = new TextView(this);
        imgLayout.addView(imgText);
        imgText.setMinimumWidth(toDp(30));
        imgText.setText("#");
        imgText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        imgText.setTextSize(20);
        imgText.setTypeface(Typeface.DEFAULT_BOLD);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(toDp(15), 0, toDp(15), 0);
        imgText.setLayoutParams(params);

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
                            border.setBackgroundResource(R.drawable.border);
                            imgdata.put("bmp", resource);
                            img.setImageBitmap(resource);
                            AllSlides.add(imgdata);
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}
                    });

        } else if (bmp != null) {
            border.setBackgroundResource(R.drawable.border);
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

    @Override
    public void onFileSelected(FileDialog dialog, File file) {
        if (dialog instanceof OpenFileDialog) {
            try {
                StringBuilder info = new StringBuilder();
                boolean containsOtherContent = false;
                ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
                ZipEntry zipEntry = zipInputStream.getNextEntry();

                while(zipEntry != null){
                    if (zipEntry.getName().equals("info.xml")) {
                        Scanner sc = new Scanner(zipInputStream);
                        while (sc.hasNextLine()) {
                            info.append(sc.nextLine());
                        }
                    }
                    zipInputStream.closeEntry();
                    zipEntry = zipInputStream.getNextEntry();
                }
                zipInputStream.closeEntry();
                zipInputStream.close();

                zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
                zipEntry = zipInputStream.getNextEntry();

                InputSource is = new InputSource(new StringReader(info.toString()));
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
                NodeList slides = document.getElementsByTagName("slides").item(0).getChildNodes();
                clearSlides();

                for (int i = 0; i < slides.getLength(); i++) {
                    if (slides.item(i).getNodeName().equals("image") || slides.item(i).getNodeName().equals("screenshot")) {
                        NodeList nodes = slides.item(i).getChildNodes();
                        String filename = "";
                        double timing = 2.0;

                        for (int j = 0; j < nodes.getLength(); j++) {
                            if (nodes.item(j).getNodeName().equals("name")) {
                                filename = nodes.item(j).getTextContent();

                            } else if (nodes.item(j).getNodeName().equals("timing")) {
                                try {
                                    timing = Double.parseDouble(nodes.item(j).getTextContent());
                                } catch (Exception ignored) {}
                            }
                        }
                        HashMap<String, Object> imgdata = new HashMap<>();
                        imgdata.put("type", slides.item(i).getNodeName());
                        imgdata.put("name", filename);
                        imgdata.put("format", filename.substring(filename.lastIndexOf(".") + 1));
                        imgdata.put("timing", timing);
                        AllSlides.add(imgdata);

                    } else {
                        containsOtherContent = true;
                    }
                }

                while(zipEntry != null){
                    int idx = checkNameExists(zipEntry.getName());
                    if (idx != -1) {
                        try {
                            Bitmap bitmap = BitmapFactory.decodeStream(zipInputStream);
                            AllSlides.get(idx).put("bmp", bitmap);
                            createSlide(null, bitmap);

                        } catch (Exception ignored) {}
                    }
                    zipInputStream.closeEntry();
                    zipEntry = zipInputStream.getNextEntry();
                }

                setItemNumbers();
                cleanSlideList();
                zipInputStream.closeEntry();
                zipInputStream.close();

                if (containsOtherContent) {
                    newMessage(getString(R.string.only_images), Toast.LENGTH_LONG);
                }

            } catch (Exception e) {
                newMessage(getString(R.string.document_error), Toast.LENGTH_LONG);
            }

        } else if (dialog instanceof SaveFileDialog) {
            try {
                ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file, false)));

                for (HashMap<String, Object> i : AllSlides) {
                    Bitmap.CompressFormat format = getFormat((String) i.get("format"));
                    String formatstr = "jpg";
                    if (format.equals(Bitmap.CompressFormat.PNG)) formatstr = "png";

                    i.put("name", checkExistingNames(checkForExtension((String)i.get("name"), formatstr)));
                    ZipEntry imgentry = new ZipEntry((String) i.get("name"));
                    zipOutputStream.putNextEntry(imgentry);

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    Bitmap bmp = (Bitmap) i.get("bmp");
                    bmp.compress(format, 90, bos);

                    byte[] img = bos.toByteArray();
                    zipOutputStream.write(img, 0, img.length);
                    zipOutputStream.closeEntry();
                }

                ZipEntry entry = new ZipEntry("info.xml");
                zipOutputStream.putNextEntry(entry);
                StringBuilder info = new StringBuilder();
                info.append("<present><info>");
                info.append("<color>").append(Color.red(backColor)).append(",")
                        .append(Color.green(backColor)).append(",")
                        .append(Color.blue(backColor)).append("</color>");
                info.append("<width>160</width><height>90</height><fit>True</fit>");
                info.append("<loop>").append(formatBoolean(loop)).append("</loop>");
                info.append("<timings>").append(formatBoolean(timings)).append("</timings></info><slides>");

                for (HashMap<String, Object> i : AllSlides) {
                    info.append("<").append((String) i.get("type")).append(">");
                    info.append("<name>").append((String) i.get("name")).append("</name>");
                    info.append("<timing>").append(((Double) i.get("timing")).toString()).append("</timing>");
                    info.append("</").append((String) i.get("type")).append(">");
                }

                info.append("</slides></present>");

                byte[] d = info.toString().getBytes();
                zipOutputStream.write(d, 0, d.length);
                zipOutputStream.closeEntry();
                zipOutputStream.close();

            } catch (Exception e) {
                newMessage(getString(R.string.save_error), Toast.LENGTH_LONG);
            }
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
            TextView txt = (TextView) layout.getChildAt(0);
            String idx = "" + i;
            txt.setText(idx);
        }

        TextView txtView = findViewById(R.id.subtitle);
        FloatingActionButton fab = findViewById(R.id.fab2);

        if (image_grid.getChildCount() > 0) {
            txtView.setVisibility(View.INVISIBLE);
            txtView = findViewById(R.id.subtitleinfo);
            txtView.setVisibility(View.INVISIBLE);
            fab.show();
        } else {
            txtView.setVisibility(View.VISIBLE);
            txtView = findViewById(R.id.subtitleinfo);
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

    private View.OnClickListener imgclick = new View.OnClickListener() {
        public void onClick(final View v) {
            PopupMenu itemPopup = new PopupMenu(getApplicationContext(), v);
            itemPopup.getMenu().add(0, 1, 0, R.string.change_picture);
            itemPopup.getMenu().add(0, 2, 0, R.string.slide_timing);
            itemPopup.getMenu().add(0, 3, 0, R.string.move_up);
            itemPopup.getMenu().add(0, 4, 0, R.string.move_down);
            itemPopup.getMenu().add(0, 5, 0, R.string.remove);

            itemPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    GridLayout image_grid = findViewById(R.id.image_grid);
                    LinearLayout layout = (LinearLayout) v.getParent().getParent();
                    int idx = image_grid.indexOfChild(layout);

                    switch (item.getItemId()) {
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
                                        double timing = Double.parseDouble(input.getText().toString());
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

                            builder.show();
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