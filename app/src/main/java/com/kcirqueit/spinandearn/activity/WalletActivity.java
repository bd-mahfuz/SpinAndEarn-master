package com.kcirqueit.spinandearn.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kcirqueit.spinandearn.R;
import com.kcirqueit.spinandearn.model.Earning;
import com.kcirqueit.spinandearn.model.PaymentRequest;
import com.kcirqueit.spinandearn.viewModel.PaymentRequestViewModel;

public class WalletActivity extends AppCompatActivity {

    private static final String TAG = "WalletActivity";

    @BindView(R.id.wallet_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.w_total_point_tv)
    TextView mTotalPointTv;

    @BindView(R.id.mobile_tv)
    TextView mMobileTv;

    @BindView(R.id.pay_type_tv)
    TextView mPayTypeTv;

    @BindView(R.id.amount_tv)
    TextView mAmountTv;

    @BindView(R.id.date_tv)
    TextView mDateTv;

    @BindView(R.id.pay_status_tv)
    TextView mPaymentStatusTv;

    @BindView(R.id.no_withdraw_history)
    TextView mNoWithdrawHistrory;

    @BindView(R.id.withdraw_layout)
    RelativeLayout mWithdrawLayout;


    private DatabaseReference mRootRef;
    private DatabaseReference mEarningRef;
    private DatabaseReference mPaymentRequestRef;
    private FirebaseAuth mAuth;

    private int mTotalEarnedAmount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        ButterKnife.bind(this);


        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Wallet");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mAuth = FirebaseAuth.getInstance();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mEarningRef = mRootRef.child("Earnings");
        mPaymentRequestRef = mRootRef.child("PaymentRequests");


        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mEarningRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue() != null) {

                    Earning earning = dataSnapshot.getValue(Earning.class);
                    mTotalEarnedAmount = earning.getTotalPoint();
                    mTotalPointTv.setText(earning.getTotalPoint()+"");
                    progressDialog.dismiss();
                } else {
                    mTotalPointTv.setText("0");
                    progressDialog.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.toException()+"");
            }
        });



    }


    @Override
    protected void onStart() {
        super.onStart();


        Query lastQuery = mPaymentRequestRef.child(mAuth.getCurrentUser().getUid()).orderByKey().limitToLast(1);

        lastQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() != null) {
                            mWithdrawLayout.setVisibility(View.VISIBLE);
                            mNoWithdrawHistrory.setVisibility(View.GONE);

                            for (DataSnapshot PaymentSnapshot : dataSnapshot.getChildren()) {

                                PaymentRequest paymentRequest = PaymentSnapshot.getValue(PaymentRequest.class);
                                mMobileTv.setText(paymentRequest.getMobileNo());
                                mDateTv.setText(paymentRequest.getDate());
                                mPaymentStatusTv.setText(paymentRequest.getTransactionStatus());
                                mPayTypeTv.setText(paymentRequest.getPaymentType());
                                mAmountTv.setText(paymentRequest.getWithdrawPoint()+" Point");

                                Log.d("id:", paymentRequest.getId()+"");

                            }

                        } else {
                            mWithdrawLayout.setVisibility(View.GONE);
                            mNoWithdrawHistrory.setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, databaseError.toException().toString());
                    }
                });
    }

    @OnClick(R.id.withdraw_bt)
    public void onWithDrawClick() {
        Intent paymentIntent = new Intent(this, PaymentMethodActivity.class);
        paymentIntent.putExtra("totalPoint", mTotalEarnedAmount);
        startActivity(paymentIntent);

    }


}
