package com.example.googlemlkitdemoapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.googlemlkitdemoapp.analyzer.TextAnalyzer;
import com.example.googlemlkitdemoapp.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    private Bitmap selectedImage;
    private TextRecognizer recognizer;
    private final ActivityResultLauncher<Intent> pickerImageResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != RESULT_OK) {
                    toggleSelectImageBtnState(false);
                    return;
                }

                clearOldSelectedImage();

                Intent data = result.getData();
                if (data == null) {
                    Toast.makeText(getApplicationContext(), "You haven't picked Image", Toast.LENGTH_SHORT)
                            .show();
                    toggleSelectImageBtnState(false);
                    return;
                }

                if (data.getClipData() != null) {
                    Toast.makeText(getApplicationContext(), "Unsupported: Please pick only one image", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                Uri uri = data.getData();
                setSelectedImage(uri);
                toggleSelectImageBtnState(false);

                recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                TextAnalyzer analyzer = new TextAnalyzer(recognizer);

                analyzer.analyze(this.selectedImage, textResult -> {
                    binding.txvResult.setText(textResult);

                    identifyLanguage(textResult);
                });
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();
    }

    private void setListeners() {
        binding.btnInput.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            pickerImageResultLauncher.launch(Intent.createChooser(intent, "Select Images"));
        });

        binding.btnCopy.setOnClickListener(v -> {
            CharSequence charSequence = binding.txvResult.getText();
            if (charSequence == null || charSequence.equals("")) {
                Toast.makeText(getApplicationContext(), "Text is empty", Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            String textToCopy = charSequence.toString();
            copyToClipboard(textToCopy);
        });
    }

    private void copyToClipboard(String textToCopy) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", textToCopy);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), "Text copied to clipboard", Toast.LENGTH_SHORT)
                .show();
    }

    private void identifyLanguage(String text) {
        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
        languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String languageCode) {
                        String msg = "Language Code: ";
                        if (languageCode.equals("und")) {
                            msg += "Unknow";
                            Log.i(TAG, "Can't identify language.");
                        } else {
                            msg += languageCode;
                        }
                        binding.txvLanguage.setText(msg);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Language identification failed: " + e.getMessage());
                    }
                });
    }

    private void clearOldSelectedImage() {
        binding.imgvPreview.setImageDrawable(null);
        selectedImage = null;
    }

    private void setSelectedImage(Uri uri) {
        Bitmap bitmap = Utils.uriToBitmap(this, uri);
        this.selectedImage = bitmap;

        Glide.with(this)
                .load(bitmap)
                .into(binding.imgvPreview);
    }

    private void toggleSelectImageBtnState(boolean inProgress) {
        if (inProgress) {
            binding.btnInput.setVisibility(View.INVISIBLE);
        } else {
            binding.btnInput.setVisibility(View.VISIBLE);
        }
    }
}