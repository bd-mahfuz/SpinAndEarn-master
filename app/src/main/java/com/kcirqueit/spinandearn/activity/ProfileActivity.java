package com.kcirqueit.spinandearn.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kcirqueit.spinandearn.R;
import com.kcirqueit.spinandearn.model.User;
import com.kcirqueit.spinandearn.viewModel.UserViewModel;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    @BindView(R.id.p_user_iv)
    CircleImageView mUserIv;

    @BindView(R.id.edit_photo_iv)
    CircleImageView mEditPhotoId;

    @BindView(R.id.p_user_name_et)
    EditText mUserNameEt;

    @BindView(R.id.p_phone_et)
    EditText mPhoneEt;

    @BindView(R.id.p_email_et)
    EditText mEmailEt;

    @BindView(R.id.pr_dob_et)
    EditText mDobEt;

    @BindView(R.id.p_update_bt)
    AppCompatButton mUpdateBt;

    @BindView(R.id.profile_toolbar)
    Toolbar mToolbar;


    private FirebaseAuth mAuth;

    private DatabaseReference mRootRef;
    private DatabaseReference mUserRef;
    private DatabaseReference mEarningRef;
    private StorageReference rootStorageRef;

    private ProgressDialog mProgressDialog;

    private static final int GALLERY_REQUEST_CODE = 1;

    private Uri mResultUri;
    private byte[] thumbByte;
    private boolean isPhotoSelected;

    private User mUser;

    private UserViewModel mUserViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = mRootRef.child("Users");
        mEarningRef = mRootRef.child("Earnings");
        rootStorageRef = FirebaseStorage.getInstance().getReference();

        ButterKnife.bind(this);

        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();

        mUserRef.keepSynced(true);
        mUserRef.child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot != null) {

                            mUser = dataSnapshot.getValue(User.class);
                            mUserNameEt.setText(mUser.getUserName());
                            mPhoneEt.setText(mUser.getPhoneNumber());
                            mEmailEt.setText(mUser.getEmail());
                            mDobEt.setText(mUser.getDateOfBirth());
                            mUserNameEt.setText(mUser.getUserName());

                            Log.d("Phone :", mUser.getPhoneNumber());

                            if (!mUser.getPhotoUrl().equals("default")) {
                                Picasso.get().load(mUser.getPhotoUrl())
                                        .placeholder(R.drawable.user_avater).into(mUserIv);
                            }

                            mProgressDialog.dismiss();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        Log.d(TAG, databaseError.getMessage());
                        mProgressDialog.dismiss();

                    }
                });


    }

    @OnClick(R.id.p_update_bt)
    public void updateProfile() {

        // getting data from edit text
        String userName = mUserNameEt.getText().toString();
        String email = mEmailEt.getText().toString();
        String dob = mDobEt.getText().toString();

        // checking for validation
        if (userName.isEmpty()) {
            mUserNameEt.setError("User name should not be empty!");
            return;
        } else if (email.isEmpty()) {
            mEmailEt.setError("Email should not be empty");
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailEt.setError("Email is not valid");
            return;
        } else if (dob.isEmpty()){
            mDobEt.setError("Email is not valid");
            return;
        }

        // save data to the firebase
        mUser.setUserName(userName);
        mUser.setEmail(email);
        mUser.setDateOfBirth(dob);

        mProgressDialog.setMessage("Please wait until we update your profile.");
        mProgressDialog.show();

        if (isPhotoSelected) {
            //for main image
            final StorageReference filePathRef = rootStorageRef.child("user_profile_images")
                    .child(mAuth.getCurrentUser().getUid()+ ".jpg");
            //for thumb image
            final StorageReference thumbFilePathRef = rootStorageRef.child("user_profile_images")
                    .child("thumb_images").child(mAuth.getCurrentUser().getUid()+".jpg");

            filePathRef.putFile(mResultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {

                        try {
                            //getting the download uri
                            //final String userImageUrl = task.getResult().getDownloadUrl().toString();
                            //final String userImageUrl = filePathRef.getDownloadUrl().;



                            UploadTask uploadTask = thumbFilePathRef.putBytes(thumbByte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {


                                        filePathRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Log.d("User image url:", uri.toString());
                                                mUser.setPhotoUrl(uri.toString());
                                                mUserViewModel.addUser(mUser).addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(ProfileActivity.this, "Your Profile is Updated!", Toast.LENGTH_SHORT).show();

                                                            mProgressDialog.dismiss();
                                                        }
                                                    }
                                                });

                                            }
                                        });


                                        //download the thumbimage
                                           /* String thumImageUrl = task.getResult().getDownloadUrl().toString();

                                            Map map = new HashMap();
                                            map.put("userImage", userImageUrl);
                                            map.put("thumbnailImage", thumImageUrl);*/

                                            /*//upload the map url to the database
                                            mDbRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        mProgressDialog.dismiss();
                                                        Toast.makeText(UserInfoActivity.this, "Image Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });*/

                                    }else {
                                        mProgressDialog.dismiss();
                                        Toast.makeText(ProfileActivity.this, "Image is not uploaded, Please contact with developer." , Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });


                        }catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ProfileActivity.this, "No Image Found to Upload!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        mProgressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Image is not uploaded, Please contact with developer." , Toast.LENGTH_SHORT).show();

                    }
                }
            });

        } else {
            
            mUserViewModel.addUser(mUser).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        mProgressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Your Profile is Updated!",
                                Toast.LENGTH_SHORT).show();
                       /* startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                        finish();*/
                    }
                }
            });
            
        }
    }

    @OnClick(R.id.pr_dob_et)
    public void getDateString() {

        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(calendar);
            }

        };

        new DatePickerDialog(ProfileActivity.this, date, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel(Calendar calendar) {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        mDobEt.setText(sdf.format(calendar.getTime()));
    }

    @OnClick(R.id.edit_photo_iv)
    public void changePhoto() {
        openGallery();
    }


    public void openGallery() {
        //open photo from the gallery
        Intent galleryIntent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = data.getData();

            //croping the image
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);


        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                //progress bar
                mProgressDialog = new ProgressDialog(ProfileActivity.this);
                mProgressDialog.setTitle("Image uploading...");
                mProgressDialog.setMessage("Please wait while we upload you image.");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                mResultUri = result.getUri();

                // ------------- image compressing for thumb image
                File thumbFilePath = new File(mResultUri.getPath());
                Bitmap thumbBitMap = null;
                try {
                    isPhotoSelected = true;
                    mUpdateBt.setEnabled(true);
                    thumbBitMap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(60)
                            .compressToBitmap(thumbFilePath);
                    mUserIv.setImageBitmap(thumbBitMap);
                    mProgressDialog.dismiss();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbBitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                thumbByte= baos.toByteArray();
                //-------------------------------------




            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error in user Account:"+error.getMessage(), Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Cropping error! Please contact with developer.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_profile, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        if (item.getItemId() == R.id.delete_account_menu) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Warning");
            builder.setMessage("Are you sure to delete your account?");
            builder.setIcon(R.drawable.ic_warning_yellow);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mProgressDialog = new ProgressDialog(ProfileActivity.this);
                    mProgressDialog.setMessage("Deleting your account...");
                    mProgressDialog.show();

                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    mUserRef.child(firebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                mEarningRef.child(firebaseUser.getUid()).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    dialog.dismiss();
                                                    mAuth.signOut();
                                                    Toast.makeText(ProfileActivity.this, "Your Profile is deleted successfully.",
                                                            Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                                                    finish();
                                                } else {
                                                    Toast.makeText(ProfileActivity.this, "Error in deleting user earning info.", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                }
                                            }
                                        });
                            }
                            else {
                                Toast.makeText(ProfileActivity.this, "Error in deleting user info.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });


            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }

        return true;
    }

    @OnClick(R.id.edit_name_tv)
    public void editName() {
        mUserNameEt.setEnabled(true);
        mUpdateBt.setEnabled(true);
        mUpdateBt.setTextColor(ContextCompat.getColor(this,android.R.color.white));

    }

    @OnClick(R.id.edit_email_tv)
    public void editEmail() {
        mEmailEt.setEnabled(true);
        mUpdateBt.setEnabled(true);
        mUpdateBt.setTextColor(ContextCompat.getColor(this,android.R.color.white));

    }

    @OnClick(R.id.edit_dob_tv)
    public void editDOB() {
        mDobEt.setEnabled(true);
        mUpdateBt.setEnabled(true);
        mUpdateBt.setTextColor(ContextCompat.getColor(this,android.R.color.white));

    }

}
