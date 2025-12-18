package com.expressapps.presentexpress;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.expressapps.presentexpress.helper.Funcs;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SoundtrackActivity extends AppCompatActivity {
    private FirebaseAnalytics mFA;
    private MediaPlayer mediaPlayer;
    private final ArrayList<Map.Entry<String, byte[]>> selectedAudioFiles = new ArrayList<>();
    private final int maxAudioFiles = 10;

    private final View.OnClickListener play_click = v -> {
        int position = getPositionOfView(v);
        if (position < 0) return;

        if (v.getTag() != null && (boolean) v.getTag()) {
            releasePlayer();
            refreshPlayButtons();
        } else {
            refreshPlayButtons();
            ((MaterialButton) v).setIcon(AppCompatResources.getDrawable(getBaseContext(), R.drawable.icon_stop_24));
            v.setTag(true);

            playAudio(selectedAudioFiles.get(position).getValue(), selectedAudioFiles.get(position).getKey());
        }
    };

    private final View.OnClickListener remove_click = v -> {
        LinearLayout container = findViewById(R.id.soundtrack_items);
        int position = getPositionOfView(v);
        if (position < 0) return;

        releasePlayer();
        refreshPlayButtons();

        container.removeViewAt(position);
        selectedAudioFiles.remove(position);
        onListRefresh();
    };

    private final ActivityResultLauncher<Intent> audioLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    ArrayList<Map.Entry<String, byte[]>> items = new ArrayList<>();
                    boolean error = false;

                    if (data.getData() != null) {
                        Uri uri = data.getData();
                        Map.Entry<String, byte[]> entry = processAudioFile(uri);

                        if (entry != null) {
                            items.add(entry);
                        } else {
                            error = true;
                        }

                    } else if (data.getClipData() != null) {
                        ClipData clipData = data.getClipData();
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri uri = clipData.getItemAt(i).getUri();
                            Map.Entry<String, byte[]> entry = processAudioFile(uri);

                            if (entry != null) {
                                items.add(entry);
                            } else {
                                error = true;
                            }
                        }
                    }

                    if (error) {
                        Funcs.newLongMessage(SoundtrackActivity.this, R.string.unsupported_audio);
                    }
                    if (!items.isEmpty()) {
                        addAudioFiles(items);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_soundtrack);
        mFA = FirebaseAnalytics.getInstance(this);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        ArrayList<Map.Entry<String, byte[]>> items = new ArrayList<>();
        selectedAudioFiles.clear();

        for (String filename : MainActivity.slideshow.info.soundtrack.filenames) {
            items.add(Map.entry(filename,
                    Objects.requireNonNull(MainActivity.slideshow.info.soundtrack.audio.get(filename))));
        }
        addAudioFiles(items);

        MaterialCheckBox loop_check = findViewById(R.id.loop_soundtrack);
        loop_check.setChecked(MainActivity.slideshow.info.soundtrack.loop);

        ScrollView scroller = findViewById(R.id.scroller);
        ExtendedFloatingActionButton fab = findViewById(R.id.fab_done);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar), (v, insets) -> {
            Insets in = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(in.left, 0, in.right, 0);
            return insets;
        });

        Insets[] systemInsets = {null};
        Runnable updateScrollerPadding = () -> {
            if (systemInsets[0] != null && fab.getHeight() > 0) {
                Insets in = systemInsets[0];
                int bottomPadding = in.bottom + fab.getHeight() + Funcs.toDp(16);
                scroller.setPadding(in.left, 0, in.right, bottomPadding);
            }
        };

        ViewCompat.setOnApplyWindowInsetsListener(scroller, (v, insets) -> {
            systemInsets[0] = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            updateScrollerPadding.run();
            return insets;
        });

        fab.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                fab.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                updateScrollerPadding.run();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(fab, (v, insets) -> {
            Insets in = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            int margin = Funcs.toDp(20);
            params.setMargins(0, 0, in.right + margin, in.bottom + margin);

            v.setLayoutParams(params);
            return insets;
        });

        // Handle back button press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Funcs.showDialog(SoundtrackActivity.this, R.string.editor_apply_changes, R.string.closing_soundtrack_editor, (d, b) -> {
                    switch (b) {
                        case DialogInterface.BUTTON_POSITIVE:
                            applyChangesAndFinish();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            releasePlayer();
                            finish();
                            break;
                    }
                });
            }
        });
    }

    private void applyChangesAndFinish() {
        MainActivity.slideshow.info.soundtrack.loop =
                ((MaterialCheckBox) findViewById(R.id.loop_soundtrack)).isChecked();

        MainActivity.slideshow.info.soundtrack.filenames.clear();
        MainActivity.slideshow.info.soundtrack.audio.clear();

        for (Map.Entry<String, byte[]> entry : selectedAudioFiles) {
            MainActivity.slideshow.info.soundtrack.filenames.add(entry.getKey());
            MainActivity.slideshow.info.soundtrack.audio.put(entry.getKey(), entry.getValue());
        }

        releasePlayer();
        finish();
    }

    public void onFabClick(View view) {
        applyChangesAndFinish();
    }

    private void onListRefresh() {
        TextView itemCount = findViewById(R.id.audio_file_count);
        itemCount.setText(String.format(Locale.getDefault(), "%d/%d",
                selectedAudioFiles.size(), maxAudioFiles));

        MaterialButton addBtn = findViewById(R.id.add_audio_files);
        addBtn.setVisibility(selectedAudioFiles.size() < maxAudioFiles ? View.VISIBLE : View.GONE);
    }

    private int getPositionOfView(View v) {
        LinearLayout container = findViewById(R.id.soundtrack_items);
        return container.indexOfChild((View) v.getParent());
    }

    private void addAudioFiles(ArrayList<Map.Entry<String, byte[]>> items) {
        for (Map.Entry<String, byte[]> entry : items) {
            if (selectedAudioFiles.size() >= maxAudioFiles) {
                Funcs.newLongMessage(SoundtrackActivity.this, R.string.audio_limit_exceeded);
                break;
            }

            String originalKey = entry.getKey();
            String newKey = originalKey;
            int suffix = 1;

            // Check for existing keys and append a suffix if necessary
            while (true) {
                boolean keyExists = false;
                for (Map.Entry<String, byte[]> existingEntry : selectedAudioFiles) {
                    if (existingEntry.getKey().equals(newKey)) {
                        keyExists = true;
                        break;
                    }
                }
                if (!keyExists) break;

                int dotIndex = originalKey.lastIndexOf('.');
                String baseName = (dotIndex == -1) ? originalKey : originalKey.substring(0, dotIndex);
                String extension = (dotIndex == -1) ? "" : originalKey.substring(dotIndex);
                newKey = baseName + "-" + (suffix++) + extension;
            }

            selectedAudioFiles.add(Map.entry(newKey, entry.getValue()));

            LayoutInflater inflater = LayoutInflater.from(this);
            LinearLayout container = findViewById(R.id.soundtrack_items);
            View itemView = inflater.inflate(R.layout.audio_item_layout, container, false);

            TextView nameText = itemView.findViewById(R.id.itemLayoutName);
            nameText.setText(newKey);

            MaterialButton playBtn = itemView.findViewById(R.id.imgLayoutPlayBtn);
            playBtn.setOnClickListener(play_click);
            playBtn.setTag(false);

            MaterialButton removeBtn = itemView.findViewById(R.id.imgLayoutRemoveBtn);
            removeBtn.setOnClickListener(remove_click);

            container.addView(itemView);
        }

        onListRefresh();
    }

    public void onAddAudioFilesClick(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        String[] mimeTypes = {"audio/wav", "audio/x-wav", "audio/mpeg", "audio/mp3"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        Intent chooser = Intent.createChooser(intent, getString(R.string.select_audio_files));
        audioLauncher.launch(chooser);

        Funcs.newEventLog(mFA, "addAudio", "Add audio button clicked");
    }

    private Map.Entry<String, byte[]> processAudioFile(Uri uri) {
        try {
            String filename = getFileName(uri);
            if (filename == null) {
                filename = "audio.mp3";
            }

            InputStream inputStream = getContentResolver().openInputStream(uri);
            byte[] audioData = readBytesFromInputStream(Objects.requireNonNull(inputStream));
            inputStream.close();

            return Map.entry(filename, audioData);

        } catch (Exception ignored) {
            return null;
        }
    }

    private String getFileName(Uri uri) {
        String fileName = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex >= 0) {
                fileName = cursor.getString(nameIndex);
            }
            cursor.close();
        }
        return fileName;
    }

    private byte[] readBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            if (buffer.size() + nRead > 15728640) {
                throw new IOException("Exceeds 15MB limit");
            }
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }

    private void refreshPlayButtons() {
        LinearLayout container = findViewById(R.id.soundtrack_items);
        MaterialButton playBtn = container.findViewWithTag(true);
        if (playBtn != null) {
            playBtn.setIcon(AppCompatResources.getDrawable(getBaseContext(), R.drawable.icon_play_24));
            playBtn.setTag(false);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void playAudio(byte[] audioData, String name) {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
            } else {
                mediaPlayer = new MediaPlayer();
            }

            File tempFile = File.createTempFile("audio", name, this.getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(audioData);
            fos.close();

            mediaPlayer.setDataSource(tempFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> {
                refreshPlayButtons();
                tempFile.delete();
            });

        } catch (IOException ignored) {
            refreshPlayButtons();
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
