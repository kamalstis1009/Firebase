package com.subra.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int GALLERY_REQUEST_CODE = 11;
    private static final int GALLERY_PERMISSIONS_REQUEST_CODE = 22;
    private ImageView mImageView;
    private List<Bitmap> mArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.image_view);
        EditText editText = findViewById(R.id.text_input);
        Button button = findViewById(R.id.submit_button);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSIONS_REQUEST_CODE);

                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, GALLERY_REQUEST_CODE);
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Session.getInstance(MainActivity.this).saveData(mArrayList);
                List<Bitmap> mList = Session.getInstance(MainActivity.this).getData();
                for (int i=0; mList != null && i<mList.size(); i++) {
                    if (mList.get(i) != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        mList.get(i).compress(Bitmap.CompressFormat.JPEG, 75, stream);
                        byte[] bytes = stream.toByteArray();
                        uploadImageToStorage(bytes, "IMG_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+"_"+i);
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GALLERY_PERMISSIONS_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, GALLERY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            mImageView.setImageURI(uri);

            //Compress based on fixed resolution
            Bitmap bitmap = Utility.getDownBitmap(this, uri, 250, 250);
            mArrayList.add(bitmap);
            /*if (bitmap != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                byte[] bytes = stream.toByteArray();
                uploadImageToStorage(bytes);
            }*/

            //Compress based on UI resolution
            /*Bitmap bitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
            if (bitmap != null) {
                mImageView.setDrawingCacheEnabled(true);
                mImageView.buildDrawingCache();
                Bitmap mBitmap = mImageView.getDrawingCache();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                byte[] bytes = stream.toByteArray();
                uploadImageToStorage(bytes);
            }*/
        }
    }

    private void uploadImageToStorage(byte[] data, String imageName) {
        final ProgressDialog mProgress = Utility.showProgressDialog(this, "waiting...", true);
        final StorageReference storageRef = FirebaseStorage.getInstance().getReference("Img/" +imageName+ ".jpg");
        storageRef.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot snapshot) {
                long bytes = snapshot.getBytesTransferred();
                Log.d(TAG,  (bytes/1024) +" KB");
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String url = uri.toString();
                        Log.d(TAG, url);
                        //Utility.dismissProgressDialog(mProgress);
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                long progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                mProgress.setMessage("Uploading "+progress+"%");
                if (progress == 100) {
                    Utility.dismissProgressDialog(mProgress);
                }
            }
        });
    }
}
