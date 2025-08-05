package com.expressapps.presentexpress;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.expressapps.presentexpress.helper.DrawingSlide;
import com.expressapps.presentexpress.helper.FilterItem;
import com.expressapps.presentexpress.helper.Funcs;
import com.expressapps.presentexpress.helper.ImageSlide;
import com.expressapps.presentexpress.helper.Slide;
import com.expressapps.presentexpress.helper.SlideType;
import com.expressapps.presentexpress.helper.SlideshowItem;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.simpleframework.xml.core.Persister;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity {
    public static SlideshowItem slideshow = new SlideshowItem();
    private SlideItemAdapter adapter;
    public static int editingImageIdx = 0;
    private FirebaseAnalytics mFA;

    private final ActivityResultLauncher<Intent> pickPresentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    try {
                        if (verifyPresentFile(result.getData().getData())) {
                            loadFile(result.getData().getData());
                        } else {
                            throw new Exception("Unsupported");
                        }
                    } catch (Exception e) {
                        Funcs.newLongMessage(MainActivity.this, R.string.document_error);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> savePresentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null)
                    saveFile(result.getData().getData());
            });

    private final ActivityResultLauncher<Intent> optionsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                refreshBackgroundRes();

                if (result.getResultCode() == Activity.RESULT_OK && !slideshow.slides.isEmpty()) {
                    clearSlides();
                    checkIsEmpty();
                }
            });

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    if (editingImageIdx == -1) {
                        ArrayList<Uri> list = new ArrayList<>();
                        try {
                            boolean unsupported = false;
                            Uri resData = result.getData().getData();
                            ClipData clipData = result.getData().getClipData();

                            if (resData != null) {
                                if (verifyImageFile(resData))
                                    list.add(resData);
                                else unsupported = true;

                            } else if (clipData != null) {
                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    if (verifyImageFile(clipData.getItemAt(i).getUri()))
                                        list.add(clipData.getItemAt(i).getUri());
                                    else unsupported = true;
                                }
                            }

                            for (int i = 0; i < list.size(); i++)
                                createSlide(list.get(i));

                            checkIsEmpty();
                            if (unsupported) throw new Exception("Unsupported");

                        } catch (Exception e) {
                            Funcs.newLongMessage(MainActivity.this, R.string.unsupported_images);
                        }
                    } else {
                        try {
                            final Uri uri = result.getData().getData();
                            if (verifyImageFile(uri)) {
                                adapter.updateItem(null, editingImageIdx);

                                Glide.with(this)
                                        .asBitmap()
                                        .load(result.getData().getData())
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                ImageSlide s = (ImageSlide) slideshow.slides.get(editingImageIdx);

                                                s.name = generateNewName(MimeTypeMap.getSingleton()
                                                        .getExtensionFromMimeType(getContentResolver().getType(uri)));
                                                s.original = resource;
                                                s.bitmap = applyFilters(resource, s.filters);

                                                adapter.updateItem(s.bitmap, editingImageIdx);
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                            }
                                        });
                            } else {
                                throw new Exception("Unsupported");
                            }
                        } catch (Exception e) {
                            Funcs.newLongMessage(MainActivity.this, R.string.unsupported_image);
                        }
                    }
                }
            });

    private final ActivityResultLauncher<Intent> editImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (slideshow.slides.get(editingImageIdx).getSlideType() == SlideType.IMAGE) {
                    ImageSlide s = (ImageSlide) slideshow.slides.get(editingImageIdx);
                    s.bitmap = applyFilters(s.original, s.filters);
                    adapter.updateItem(s.bitmap, editingImageIdx);
                }
            });

    private final View.OnClickListener img_click = v -> setupOptions(v, (int) v.getTag());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_GradientStatusBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);
        mFA = FirebaseAnalytics.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.image_grid);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(MainActivity.this);
        layoutManager.setJustifyContent(JustifyContent.CENTER);
        layoutManager.setAlignItems(AlignItems.FLEX_START);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SlideItemAdapter(new ArrayList<>(), img_click);
        recyclerView.setAdapter(adapter);
    }

    // region Toolbar

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_open) {
            RecyclerView image_grid = findViewById(R.id.image_grid);
            if (image_grid.getChildCount() == 0) {
                startOpenIntent();
            } else {
                Funcs.showDialog(MainActivity.this, R.string.open_dialog, R.string.open, (d, b) -> {
                    switch (b) {
                        case DialogInterface.BUTTON_POSITIVE:
                            startOpenIntent();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            d.cancel();
                            break;
                    }
                });
            }
        } else if (itemId == R.id.nav_save) {
            if (!slideshow.slides.isEmpty()) {
                startSaveIntent();
            } else {
                Funcs.newMessage(getApplicationContext(), R.string.add_slide, Toast.LENGTH_SHORT);
            }
        } else if (itemId == R.id.nav_options) {
            Intent intent = new Intent(this, SettingsActivity.class);
            optionsLauncher.launch(intent);
            Funcs.newEventLog(mFA, "nav_options", "Settings button clicked");
        }
        return super.onOptionsItemSelected(item);
    }

    // endregion
    // region Open

    private void startOpenIntent() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        getIntent.setType("*/*");

        Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.select_present));
        pickPresentLauncher.launch(chooserIntent);

        Funcs.newMessage(getApplicationContext(), R.string.select_present, Toast.LENGTH_SHORT);
        Funcs.newEventLog(mFA, "nav_open", "Open button clicked");
    }

    private void loadFile(Uri file) {
        try {
            StringBuilder info = new StringBuilder();
            boolean containsOtherContent = false;
            boolean otherContentFailed = false;
            boolean imageContentFailed = false;

            ZipInputStream zipInputStream = new ZipInputStream(
                    new BufferedInputStream(getContentResolver().openInputStream(file)));
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {
                if (zipEntry.getName().equals("info.xml")) {
                    Scanner sc = new Scanner(zipInputStream);
                    while (sc.hasNextLine())
                        info.append(sc.nextLine()).append("\n");
                    break;
                }
                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.close();

            Persister serializer = new Persister();
            slideshow = serializer.read(SlideshowItem.class, info.toString().replace("\uFEFF", ""));

            zipInputStream = new ZipInputStream(
                    new BufferedInputStream(getContentResolver().openInputStream(file)));
            zipEntry = zipInputStream.getNextEntry();

            setBackgroundColour();

            while (zipEntry != null) {
                int idx = checkNameExists(zipEntry.getName());
                if (idx != -1) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(zipInputStream);
                        Slide s = slideshow.slides.get(idx);

                        if (s.getSlideType() == SlideType.IMAGE) {
                            ((ImageSlide) s).original = bitmap;
                            s.bitmap = applyFilters(bitmap, ((ImageSlide) s).filters);
                        } else {
                            s.bitmap = bitmap;
                        }
                    } catch (Exception ignored) {
                        imageContentFailed = true;
                    }
                } else {
                    idx = checkNameExists(zipEntry.getName(), true);
                    if (idx != -1 && slideshow.slides.get(idx).getSlideType() == SlideType.DRAWING) {
                        try {
                            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                            int nRead;
                            byte[] data = new byte[1024];

                            while ((nRead = zipInputStream.read(data, 0, data.length)) != -1)
                                buffer.write(data, 0, nRead);

                            buffer.flush();
                            ((DrawingSlide) slideshow.slides.get(idx)).strokes = buffer.toByteArray();

                        } catch (Exception ignored) {
                            otherContentFailed = true;
                        }
                    }
                }
                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.close();

            // clean slide list
            ArrayList<Slide> toReplace = new ArrayList<>();
            for (Slide i : slideshow.slides) {
                if (i.bitmap != null && i.getSlideType() != SlideType.UNKNOWN) {
                    toReplace.add(i);
                } else {
                    if (i.getSlideType() == SlideType.IMAGE || i.getSlideType() == SlideType.SCREENSHOT) {
                        imageContentFailed = true;
                    } else {
                        otherContentFailed = true;
                    }
                }
            }

            clearSlides();
            slideshow.slides.addAll(toReplace);

            for (Slide i : slideshow.slides) {
                createSlide(i.bitmap);

                if (!containsOtherContent)
                    if (i.getSlideType() == SlideType.CHART || i.getSlideType() == SlideType.TEXT ||
                            i.getSlideType() == SlideType.DRAWING) containsOtherContent = true;
            }
            findViewById(R.id.welcomelayout).setVisibility(View.GONE);
            checkIsEmpty();

            if (imageContentFailed) {
                Funcs.newLongMessage(MainActivity.this, R.string.corrupted_file);

            } else if (otherContentFailed) {
                Funcs.showDialog(MainActivity.this, R.string.compat_mode_desc, R.string.compat_mode,
                        (d, b) -> {
                        }, "OK", null);

            } else if (containsOtherContent) {
                Funcs.newLongMessage(MainActivity.this, R.string.only_images);
            }

            refreshBackgroundRes();

        } catch (Exception e) {
            Funcs.newLongMessage(MainActivity.this, R.string.document_error);
        }
    }

    // endregion
    // region Save

    private void startSaveIntent() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "filename.present");

        savePresentLauncher.launch(intent);
        Funcs.newEventLog(mFA, "nav_save", "Save button clicked");
    }

    private void saveFile(Uri file) {
        try {
            ZipOutputStream zipOutputStream = new ZipOutputStream(
                    new BufferedOutputStream(getContentResolver().openOutputStream(file)));

            for (Slide i : slideshow.slides) {
                Bitmap.CompressFormat format = getFormat(i.getFileName());
                ZipEntry imgEntry = new ZipEntry(i.getFileName());
                zipOutputStream.putNextEntry(imgEntry);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Bitmap bmp = i.getSlideType() == SlideType.IMAGE ? ((ImageSlide) i).original : i.bitmap;
                bmp.compress(format, 90, bos);

                byte[] img = bos.toByteArray();
                zipOutputStream.write(img, 0, img.length);
                zipOutputStream.closeEntry();

                if (i.getSlideType() == SlideType.DRAWING) {
                    ZipEntry isfEntry = new ZipEntry(i.name);
                    zipOutputStream.putNextEntry(isfEntry);

                    byte[] isf = ((DrawingSlide) i).strokes;
                    zipOutputStream.write(isf, 0, isf.length);
                    zipOutputStream.closeEntry();
                }
            }

            ZipEntry entry = new ZipEntry("info.xml");
            zipOutputStream.putNextEntry(entry);

            Persister serializer = new Persister();
            StringWriter writer = new StringWriter();
            serializer.write(slideshow, writer);

            byte[] d = writer.toString().getBytes();
            zipOutputStream.write(d, 0, d.length);
            zipOutputStream.close();

            Funcs.newMessage(getApplicationContext(), R.string.file_saved, Toast.LENGTH_SHORT);

        } catch (Exception e) {
            Funcs.newMessage(getApplicationContext(), R.string.save_error, Toast.LENGTH_LONG);
        }
    }

    // endregion
    // region Options

    public void addImage_click(View view) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        getIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.select_images));
        editingImageIdx = -1;
        pickImageLauncher.launch(chooserIntent);
        Funcs.newEventLog(mFA, "addImage", "Add images button clicked");
    }

    public void show_click(View view) {
        if (!slideshow.slides.isEmpty()) {
            Intent intent = new Intent(this, SlideshowActivity.class);
            startActivity(intent);
        } else {
            Funcs.newMessage(getApplicationContext(), R.string.add_slide, Toast.LENGTH_SHORT);
        }
    }

    public void setupOptions(View v, int idx) {
        PopupMenu itemPopup = new PopupMenu(getApplicationContext(), v);

        if (slideshow.slides.get(idx).getSlideType() == SlideType.IMAGE) {
            itemPopup.getMenu().add(0, 0, 0, R.string.edit_picture);
            itemPopup.getMenu().add(0, 1, 0, R.string.change_picture);
        }
        itemPopup.getMenu().add(0, 2, 0, R.string.set_transition);
        itemPopup.getMenu().add(0, 3, 0, R.string.move_up);
        itemPopup.getMenu().add(0, 4, 0, R.string.move_down);
        itemPopup.getMenu().add(0, 5, 0, R.string.remove);

        itemPopup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0: // Edit picture
                    Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                    intent.putExtra("idx", idx);

                    FilterItem filters = ((ImageSlide) slideshow.slides.get(idx)).filters;
                    intent.putExtra("filter", filters.getFilter().getStringValue());
                    intent.putExtra("brightness", filters.getBrightness());
                    intent.putExtra("contrast", filters.getContrast());
                    intent.putExtra("rotation", filters.getRotation());
                    intent.putExtra("fliph", filters.flipHorizontal);
                    intent.putExtra("flipv", filters.flipVertical);

                    editingImageIdx = idx;
                    editImageLauncher.launch(intent);
                    Funcs.newEventLog(mFA, "nav_edit", "Photo editor opened");
                    break;

                case 1: // Change picture
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                    getIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.select_image));
                    editingImageIdx = idx;
                    pickImageLauncher.launch(chooserIntent);
                    break;

                case 2: // Set transition
                    Intent transIntent = new Intent(MainActivity.this, TransitionActivity.class);
                    transIntent.putExtra("idx", idx);

                    editingImageIdx = idx;
                    startActivity(transIntent);
                    Funcs.newEventLog(mFA, "nav_trans", "Transition editor opened");
                    break;

                case 3: // Move up
                    if (idx != 0) {
                        Collections.swap(slideshow.slides, idx, idx - 1);
                        adapter.moveItems(idx, idx - 1);
                    }
                    break;

                case 4: // Move down
                    if (idx + 1 < slideshow.slides.size()) {
                        Collections.swap(slideshow.slides, idx, idx + 1);
                        adapter.moveItems(idx, idx + 1);
                    }
                    break;

                case 5: // Remove
                    adapter.removeItem(idx);
                    slideshow.slides.remove(idx);
                    checkIsEmpty();
                    break;
            }
            return true;
        });

        itemPopup.show();
    }

    // endregion
    // region Helper Functions

    private void createSlide(Bitmap bmp) {
        setBackgroundColour();
        adapter.addItem(bmp);
        refreshBackgroundRes();
    }

    private void createSlide(Uri uri) {
        ImageSlide s = new ImageSlide();
        s.name = generateNewName(MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(getContentResolver().getType(uri)));

        adapter.addItem(null);
        int idx = adapter.getItemCount() - 1;

        Glide.with(this)
                .asBitmap()
                .load(uri)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        setBackgroundColour();

                        s.original = resource;
                        s.bitmap = applyFilters(resource, s.filters);
                        adapter.updateItem(s.bitmap, idx);
                        slideshow.slides.add(s);
                        checkIsEmpty();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    public static Bitmap applyFilters(Bitmap originalImage, FilterItem filtersApplied) {
        ColorMatrix clr = new ColorMatrix();

        switch (filtersApplied.getFilter()) {
            case GREYSCALE:
            case RED:
            case GREEN:
            case BLUE:
                clr.setSaturation(0);
                break;
            case SEPIA:
                clr.setSaturation(0);
                ColorMatrix matrixB = new ColorMatrix();
                matrixB.setScale(1f, .95f, .82f, 1.0f);
                clr.setConcat(matrixB, clr);
                break;
            case BLACK_WHITE:
                clr.setSaturation(0);
                ColorMatrix matrixC = new ColorMatrix(new float[]{
                        85.f, 85.f, 85.f, 0.f, -255.f * 90f,
                        85.f, 85.f, 85.f, 0.f, -255.f * 90f,
                        85.f, 85.f, 85.f, 0.f, -255.f * 90f,
                        0f, 0f, 0f, 1f, 0f
                });
                clr.setConcat(matrixC, clr);
                break;
            case NONE:
                break;
        }

        float contrast = filtersApplied.getContrast();
        float brightness = Funcs.transformRange(
                filtersApplied.getBrightness(), -0.5f, 0.5f, -127.5f, 127.5f);

        ColorMatrix matrixD = new ColorMatrix(new float[]{
                contrast, 0, 0, 0, brightness,
                0, contrast, 0, 0, brightness,
                0, 0, contrast, 0, brightness,
                0, 0, 0, 1, 0
        });
        clr.setConcat(matrixD, clr);

        Matrix matrixRotate = new Matrix();
        matrixRotate.postRotate(filtersApplied.getRotation());
        matrixRotate.postScale(filtersApplied.flipHorizontal ? -1 : 1, filtersApplied.flipVertical ? -1 : 1,
                originalImage.getWidth() / 2f, originalImage.getHeight() / 2f);

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        Bitmap ret = Bitmap.createBitmap(width, height, originalImage.getConfig());
        Canvas canvas = new Canvas(ret);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(clr));
        canvas.drawBitmap(originalImage, 0, 0, paint);

        switch (filtersApplied.getFilter()) {
            case RED:
                Paint redPaint = new Paint();
                redPaint.setColor(Color.argb(100, 235, 58, 52));
                canvas.drawRect(0, 0, width, height, redPaint);
                break;
            case GREEN:
                Paint greenPaint = new Paint();
                greenPaint.setColor(Color.argb(100, 52, 235, 73));
                canvas.drawRect(0, 0, width, height, greenPaint);
                break;
            case BLUE:
                Paint bluePaint = new Paint();
                bluePaint.setColor(Color.argb(100, 52, 122, 235));
                canvas.drawRect(0, 0, width, height, bluePaint);
                break;
        }

        Bitmap rotateImage = Bitmap.createBitmap(ret, 0, 0, width, height, matrixRotate, true);
        ret.recycle();
        return rotateImage;
    }

    private void clearSlides() {
        adapter.clearItems();
        slideshow.slides.clear();
    }

    private void checkIsEmpty() {
        ScrollView txtView = findViewById(R.id.welcomelayout);
        FloatingActionButton fab = findViewById(R.id.fab2);

        if (!slideshow.slides.isEmpty()) {
            txtView.setVisibility(View.INVISIBLE);
            fab.show();
        } else {
            txtView.setVisibility(View.VISIBLE);
            fab.hide();
        }
    }

    private Bitmap.CompressFormat getFormat(String s) {
        if (s.endsWith(".jpeg") || s.endsWith(".jpg")) {
            return Bitmap.CompressFormat.JPEG;
        } else {
            return Bitmap.CompressFormat.PNG;
        }
    }

    private boolean verifyImageFile(Uri uri) {
        try {
            String[] formats = new String[]{"jpg", "jpeg", "png", "bmp", "gif"};
            String mimeType = getContentResolver().getType(uri);

            if ((Objects.requireNonNull(
                    DocumentFile.fromSingleUri(getApplicationContext(), uri)).length() / 1024 / 1024) > 10)
                return false;

            for (String i : formats) {
                if (MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType).equals(i))
                    return true;
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

            if ((Objects.requireNonNull(
                    DocumentFile.fromSingleUri(getApplicationContext(), uri)).length() / 1024 / 1024) > 50)
                return false;

            if (MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) == null && mimeType.equals("application/octet-stream"))
                return true;

            for (String i : formats) {
                if (MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType).equals(i))
                    return true;
            }
            return false;

        } catch (Exception ignored) {
            return false;
        }
    }

    private int checkNameExists(String s, boolean isf) {
        for (Slide i : slideshow.slides) {
            if (isf ? s.equals(i.name) : s.equals(i.getFileName()))
                return slideshow.slides.indexOf(i);
        }
        return -1;
    }

    private int checkNameExists(String s) {
        return checkNameExists(s, false);
    }

    private String generateNewName(String ext) {
        boolean exists = true;
        int counter = new Random().nextInt(9999);
        String newstr = "image" + counter;
        String extension = ext.startsWith(".") ? ext : "." + ext;

        while (exists) {
            exists = false;
            for (Slide i : slideshow.slides) {
                if ((newstr + extension).equals(i.getFileName())) {
                    counter++;
                    newstr = "image" + counter;
                    exists = true;
                    break;
                }
            }
        }
        return newstr + extension;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshBackgroundRes() {
        setBackgroundColour();
        adapter.notifyDataSetChanged();
    }

    private void setBackgroundColour() {
        ((GradientDrawable) Objects.requireNonNull(ContextCompat.getDrawable(getBaseContext(), R.drawable.border)))
                .setColor(slideshow.info.getBackColour());
    }

    // endregion
}
