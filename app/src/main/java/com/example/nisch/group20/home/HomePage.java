package com.example.nisch.group20.home;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nisch.group20.R;
import com.example.nisch.group20.graph.GraphForLabels;
import com.example.nisch.group20.server.S3Uploader;
import com.example.nisch.group20.server.S3Utils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nisch on 4/22/2018.
 */

public class HomePage extends AppCompatActivity {

    private int PICK_IMAGE_REQUEST = 1;
    S3Uploader s3uploaderObj;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        s3uploaderObj = new S3Uploader(this);
        progressDialog = new ProgressDialog(this);

        isStoragePermissionGranted();

        Button browseImage = findViewById(R.id.browse);

        browseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final List<Uri> imageURIs = new ArrayList<>();

        if (resultCode == RESULT_OK) {
            try {

                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    int currentItem = 0;
                    TextView selectImageText = findViewById(R.id.selectImage);
                    selectImageText.setText("The selected images are - ");
                    int[] imageIDs = new int[]{R.id.browsedImage0, R.id.browsedImage1};
                    while (currentItem < count) {
                        Uri imageUri = data.getClipData().getItemAt(currentItem).getUri();
                        imageURIs.add(imageUri);
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        int nh = (int) (selectedImage.getHeight() * (256.0 / selectedImage.getWidth()));
                        final Bitmap scaled = Bitmap.createScaledBitmap(selectedImage, 256, nh, true);
                        ImageView image_view = findViewById(imageIDs[currentItem]);
                        image_view.setImageBitmap(scaled);
                        currentItem = currentItem + 1;
                    }
                }

                Button upload = findViewById(R.id.upload);
                upload.setVisibility(View.VISIBLE);
                upload.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int i = 0;
                        while (i < imageURIs.size()) {
                            uploadImageTos3(imageURIs.get(i),i);
                            i++;
                        }

                        Toast.makeText(HomePage.this,"Images uploaded",Toast.LENGTH_SHORT).show();

                    }
                });

                Button download = findViewById(R.id.download);
                download.setVisibility(View.VISIBLE);
                download.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Toast.makeText(HomePage.this,"Segmented Pic Labels downloaded",Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(HomePage.this, GraphForLabels.class);
                        startActivity(i);
                    }
                });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(HomePage.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(HomePage.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }
/*
    private String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }*/

    private String getFilePathfromURI(Uri selectedImageUri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(selectedImageUri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;

    }

    private void uploadImageTos3(Uri imageUri, int i) {
        final String path = getFilePathfromURI(imageUri);
        if (path != null) {
            showLoading("Uploading details !!");
            s3uploaderObj.initUpload(path,i);
            s3uploaderObj.setOns3UploadDone(new S3Uploader.S3UploadInterface() {
                @Override
                public void onUploadSuccess(String response) {
                    if (response.equalsIgnoreCase("Success")) {
                        hideLoading();

                    }
                }

                @Override
                public void onUploadError(String response) {
                    hideLoading();
                    //tvStatus.setText("Error : "+response);
                    Log.e("a", "Error Uploading");

                }
            });
        }
    }

    private void showLoading(String message) {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.setMessage(message);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

}


