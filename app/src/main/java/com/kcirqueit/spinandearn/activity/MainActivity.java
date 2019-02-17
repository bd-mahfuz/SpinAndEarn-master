package com.kcirqueit.spinandearn.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kcirqueit.spinandearn.R;
import com.kcirqueit.spinandearn.model.Earning;
import com.kcirqueit.spinandearn.model.User;
import com.kcirqueit.spinandearn.util.DateUtils;
import com.kcirqueit.spinandearn.util.InternetConnection;
import com.kcirqueit.spinandearn.viewModel.EarningViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import rubikstudio.library.LuckyWheelView;
import rubikstudio.library.model.LuckyItem;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";

    @BindView(R.id.main_tool_bar)
    Toolbar mToolbar;

    @BindView(R.id.luckyWheel)
    LuckyWheelView luckyWheelView;

    @BindView(R.id.spin_count_show_tv)
    TextView mSpinCountTv;

    @BindView(R.id.point_count_show_tv)
    TextView mPointCountTv;

    private TextView totalPointTv;
    private CircleImageView mUserProfileIv;

    private FirebaseAuth mAuth;

    private EarningViewModel earningViewModel;

    private DatabaseReference mRootRef;
    private DatabaseReference mEarningRef;
    private DatabaseReference mUserRef;

    private int mCount;

    private String mCurrentUserId;

    private Earning mEarning;


    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        mInterstitialAd.loadAd(adRequest);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d("on close", "true");
                mInterstitialAd.loadAd(adRequest);

            }
            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();


                Toast.makeText(MainActivity.this, "clicked", Toast.LENGTH_SHORT).show();
                mInterstitialAd.loadAd(adRequest);

            }
        });

        int color[] = new int[] {
                R.color.amaranth,
                R.color.amber,
                R.color.red,
                R.color.oliv,
                R.color.maroon,
                R.color.teal,
                R.color.purple,
                R.color.blue,
                R.color.blue_purple,
                R.color.green,

        };

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mEarningRef = mRootRef.child("Earnings");
        mUserRef = mRootRef.child("Users");

        earningViewModel = ViewModelProviders.of(this).get(EarningViewModel.class);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            View view = LayoutInflater.from(this).inflate(R.layout.wallet_layout, null);
            totalPointTv = view.findViewById(R.id.total_point_tv);
            mUserProfileIv = view.findViewById(R.id.user_profile_iv);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(view);

            LinearLayout walletLayout = view.findViewById(R.id.wallet_layout);
            walletLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, WalletActivity.class));
                }
            });

            mUserProfileIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }
            });
            
        }

        List<LuckyItem> data = new ArrayList<>();
        int point = 10;
        for (int i = 0; i < 10; i++) {
            LuckyItem luckyItem1 = new LuckyItem();
            luckyItem1.topText = "" + point;
            luckyItem1.icon = R.drawable.coins;
            luckyItem1.color = ContextCompat.getColor(this, color[i]);
            data.add(luckyItem1);

            point+=10;
        }

        luckyWheelView.setData(data);
        luckyWheelView.setRound(10);


        // start
        //luckyWheelView.startLuckyWheelWithTargetIndex(0);

        // listener after finish lucky wheel
        luckyWheelView.setLuckyRoundItemSelectedListener(new LuckyWheelView.LuckyRoundItemSelectedListener() {
            @Override
            public void LuckyRoundItemSelected(int index) {

                int point = Integer.valueOf(data.get(index).topText);


                //Log.d(TAG, mEarning.getDailySpingCount()+" spin remain");

                if (mEarning.getDailySpingCount() != 0) {

                    if (mEarning.getDate() == null) {
                        mEarning.setDate(DateUtils.getCurrentDateString());
                    }
                    mEarning.setDailyPoint(mEarning.getDailyPoint()+point);
                    //mEarning.setTotalPoint(mEarning.getTotalPoint() +point);

                    if (mEarning.getDailySpingCount() == 1) {
                        // decreasing the total amount from daily earned point
                        mEarning.setTotalPoint(mEarning.getTotalPoint() + mEarning.getDailyPoint());
                    }
                    mEarning.setDailySpingCount(mEarning.getDailySpingCount() - 1);

                    if (InternetConnection.checkConnection(MainActivity.this)) {

                        earningViewModel.addEarning(mEarning).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {

                                    // display add
                                    if (mInterstitialAd.isLoaded()) {
                                        mInterstitialAd.show();
                                    } else {
                                        Log.d("TAG", "The interstitial wasn't loaded yet.");
                                    }

                                    Toast.makeText(MainActivity.this, "You got "+point+" point", Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(MainActivity.this, "Something went wrong. Contact with developer.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {

                        Toast.makeText(MainActivity.this, "Your Internet connection is gone. Please connect and try again", Toast.LENGTH_SHORT).show();

                    }

                } else {
                    showAlertDialog();
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        mEarning = new Earning();
        mEarning.setUserId(mAuth.getCurrentUser().getUid());

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mUserRef.keepSynced(true);
        mUserRef.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    User user = dataSnapshot.getValue(User.class);
                    if (!user.getPhotoUrl().isEmpty() || user.getPhotoUrl() != null) {

                        Log.d("Photo url:", user.getPhotoUrl());

                        if (!user.getPhotoUrl().equals("default")) {
                            Picasso.get().load(user.getPhotoUrl()).placeholder(R.drawable.user_avater)
                                    .into(mUserProfileIv);
                        }
                        progressDialog.dismiss();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.toException()+"");
            }
        });



        mEarningRef.keepSynced(true);
        mEarningRef.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Log.d("data snap", dataSnapshot+"");

                if (dataSnapshot.getValue() != null) {

                    mEarning = dataSnapshot.getValue(Earning.class);
                    //Log.d(TAG, mEarning.getDailySpingCount()+" spin remain");

                    String currentDateString = DateUtils.getCurrentDateString();

                     // reset the earning account for update date
                    if (DateUtils.getDateToString(currentDateString)
                            .after(DateUtils.getDateToString(mEarning.getDate()))) {

                        ProgressDialog progressDialog1 = new ProgressDialog(MainActivity.this);
                        progressDialog1.setMessage("Updating Data...");
                        progressDialog1.setCancelable(false);
                        progressDialog1.show();

                        /*if (mEarning.getDailySpingCount() > 0) {
                            // decreasing the total amount from daily earned point
                            mEarning.setTotalPoint(mEarning.getTotalPoint() - mEarning.getDailyPoint());
                        }*/

                        mEarning.setDate(currentDateString);
                        mEarning.setDailySpingCount(30);
                        mEarning.setDailyPoint(0);
                        earningViewModel.addEarning(mEarning).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    totalPointTv.setText(mEarning.getTotalPoint()+"");
                                    mSpinCountTv.setText(mEarning.getDailySpingCount()+" spin remaining.");
                                    mPointCountTv.setText("Your Points: "+mEarning.getDailyPoint());
                                    progressDialog1.dismiss();
                                }
                            }
                        });

                    }
                    totalPointTv.setText(mEarning.getTotalPoint()+"");
                    mSpinCountTv.setText(mEarning.getDailySpingCount()+" spin remaining.");
                    mPointCountTv.setText("Your Points: "+mEarning.getDailyPoint());

                } else {
                    totalPointTv.setText(mEarning.getTotalPoint()+"");
                    mSpinCountTv.setText(30+" spin remaining.");
                    mPointCountTv.setText("Your Points: "+0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.toException()+"");
            }
        });

    }

    @OnClick(R.id.spin_bt)
    public void spinWheel() {
        luckyWheelView.startLuckyWheelWithTargetIndex(0);
    }

    @OnClick(R.id.info_iv)
    public void showInfo() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Information");

        View view = LayoutInflater.from(this).inflate(R.layout.layout_info, null);
        builder.setView(view);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public void showAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Information");
        builder.setMessage("Your Daily Spin Quota is finished." +
                " You will don't get any point for any spin now. Please try again in tomorrow. Thank you.");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
