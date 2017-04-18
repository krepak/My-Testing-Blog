package com.zwemun.dev.simpleblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostActivity extends AppCompatActivity {

    private ImageButton mSelectImage;
    private static final int GALLERY_REQUEST = 1;
    private EditText mPostTitle,mPostDesc;
    private Button btnSubmit;
    private Uri imageUri = null;
    private StorageReference mStorage;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");

        mPostTitle = (EditText) findViewById(R.id.txtTitle);
        mPostDesc = (EditText) findViewById(R.id.txtDesc);
        btnSubmit = (Button) findViewById(R.id.submit);

        mProgressDialog = new ProgressDialog(this);
        mSelectImage = (ImageButton) findViewById(R.id.imageSelect);

        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            imageUri = data.getData();

            mSelectImage.setImageURI(imageUri);
        }
    }

    private void startPosting() {
        mProgressDialog.setMessage("Uploading to blog..");
        
        final String title = mPostTitle.getText().toString().trim();
        final String desc = mPostDesc.getText().toString().trim();

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(desc) && imageUri !=null){
            mProgressDialog.show();
            StorageReference filePath = mStorage.child("Blog_images").child(imageUri.getLastPathSegment());

            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri downloadURL = taskSnapshot.getDownloadUrl();
                    DatabaseReference newPost = mDatabase.push();
                    newPost.child("title").setValue(title);
                    newPost.child("desc").setValue(desc);
                    newPost.child("image").setValue(downloadURL.toString());

                    mProgressDialog.dismiss();
                    Toast.makeText(PostActivity.this,"Successfully Upload",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(PostActivity.this,MainActivity.class));
                }
            });
        }


    }


}
