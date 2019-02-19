package com.kcirqueit.spinandearn.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
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
import com.kcirqueit.spinandearn.util.DateUtils;
import com.kcirqueit.spinandearn.viewModel.EarningViewModel;

public class DashBoardActivity extends AppCompatActivity implements RewardedVideoAdListener {


    private static final String TAG = "DashBoardActivity";

    @BindView(R.id.dash_toolbar)
    Toolbar mToolbar;


    private RewardedVideoAd mRewardedVideoAd;
    private InterstitialAd mInterstitialAd;

    private Earning mEarning;
    private String mCurrentUserId;
    private DatabaseReference mRootRef;
    private DatabaseReference mEarningRef;

    private FirebaseAuth mAuth;

    private EarningViewModel earningViewModel;

    private AdLoader adLoader;

    private int mCount = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        earningViewModel = ViewModelProviders.of(this).get(EarningViewModel.class);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mEarning = new Earning();
        mEarning.setUserId(mCurrentUserId);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mEarningRef = mRootRef.child("Earnings");

        ButterKnife.bind(this);


        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        mInterstitialAd.loadAd(adRequest);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                //Log.d("on close", "true");
                mInterstitialAd.loadAd(adRequest);

            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                // rewarded with 10 point for click on interstitial ad
                updateEarningForAd(10);
//                mInterstitialAd.loadAd(adRequest);
                //DashBoardActivity.this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            }
        });

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Spin And Earn");


    }


    @Override
    protected void onStart() {
        super.onStart();

        loadRewardedVideoAd();

        mEarningRef.keepSynced(true);
        mEarningRef.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Log.d("data snap", dataSnapshot+"");

                if (dataSnapshot.getValue() != null) {
                    mEarning = dataSnapshot.getValue(Earning.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.toException() + "");
            }
        });

    }

    private void loadRewardedVideoAd() {

        //Log.d(TAG, "called............");
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());

    }


    @OnClick(R.id.spin_bt)
    public void onSpinBtClick() {
        //mRewardedVideoAd.destroy(this);
        startActivity(new Intent(this, MainActivity.class));
    }

    @OnClick(R.id.video_bt)
    public void onVideoClick() {
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        } else {
            Toast.makeText(this, "The RewardedVideoAd wasn't loaded yet.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.click_on_ad_bt)
    public void onAddkBtClick() {

        if (mCount % 2 == 0) {
            // display interstitial add if mCount is even
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                Toast.makeText(this, "The ad wasn't loaded yet.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "The interstitial wasn't loaded yet.");
            }
            mCount++;
        } else {
            // display RewardedVideo Ad if mCount is odd
            if (mRewardedVideoAd.isLoaded()) {
                mRewardedVideoAd.show();
            } else {
                Toast.makeText(this, "The RewardedVideoAd wasn't loaded yet.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "The RewardedVideoAd wasn't loaded yet.");
            }
            mCount++;
        }
    }

    @OnClick(R.id.my_wallet_bt)
    public void onMywalletBtClick() {
        startActivity(new Intent(this, WalletActivity.class));
    }


    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

//        Log.d("reaward amount", rewardItem.getAmount()+"");
//        Log.d("total amount", mEarning.getTotalPoint()+"");

        updateEarningForAd(rewardItem.getAmount());


    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {

    }


    @Override
    public void onResume() {
        mRewardedVideoAd.resume(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }


    public void updateEarningForAd(int reward) {

        // updating the total point for reward amount

        // for first time watch video
        if (mEarning.getDate() == null) {

            mEarning.setDate(DateUtils.getCurrentDateString());
            mEarning.setTotalPoint(mEarning.getTotalPoint() + reward);
            mEarning.setDailySpingCount(30);
            mEarning.setDailyPoint(0);


            earningViewModel.addEarning(mEarning).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {

                        Toast.makeText(DashBoardActivity.this, "You got " + reward + " points. Now you can close the add.",
                                Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(DashBoardActivity.this, "Something wrong.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            mEarningRef.child(mCurrentUserId).child("totalPoint").setValue((mEarning.getTotalPoint() + reward))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                Toast.makeText(DashBoardActivity.this, "You got " + reward + " points. Now you can close the add.",
                                        Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(DashBoardActivity.this, "Something wrong.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }

    }
}
