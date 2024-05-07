package com.example.googlemlkitdemoapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

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

import com.bumptech.glide.Glide;
import com.example.googlemlkitdemoapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    private Bitmap selectedImage;

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
            });

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