package com.kcirqueit.spinandearn.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kcirqueit.spinandearn.R;
import com.kcirqueit.spinandearn.model.PaymentRequest;
import com.kcirqueit.spinandearn.model.User;
import com.kcirqueit.spinandearn.util.DateUtils;
import com.kcirqueit.spinandearn.util.SpinConstant;
import com.kcirqueit.spinandearn.viewModel.PaymentRequestViewModel;

import java.util.HashMap;
import java.util.Map;

public class PaymentMethodActivity extends AppCompatActivity {

    private static final String TAG ="PaymentMethodActivity";
    private static final int MIN_CON_TO_WITHDRAW = 500000;

    private String mPaymentType = "bKash";

    @BindView(R.id.pm_name_et)
    EditText mNameEt;

    @BindView(R.id.pay_type_spinner)
    Spinner mPaymentSpinner;

    @BindView(R.id.pm_email_et)
    EditText mEmailEt;

    @BindView(R.id.payment_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.mobile_et)
    EditText mMobileEt;

    @BindView(R.id.point_et)
    EditText mPointEt;

    @BindView(R.id.account_type_rg)
    RadioGroup mAccountTypeRg;

    private int mTotalPoint;
    private String mAccountType = "personal";

    private PaymentRequestViewModel paymentRequestViewModel;

    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private DatabaseReference mEarningRef;
    private DatabaseReference mUserRef;
    private String mCurrentUserId;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        paymentRequestViewModel = ViewModelProviders.of(this).get(PaymentRequestViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mEarningRef = mRootRef.child("Earnings");
        mUserRef = mRootRef.child("Users");

        ButterKnife.bind(this);

        mTotalPoint = getIntent().getIntExtra("totalPoint", 0);
        Log.d(TAG, mTotalPoint+"");


        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Payment Method");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAccountTypeRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {

                    case R.id.personal_rb:
                        mAccountType = "personal";
                        break;
                    case R.id.agent_rb:
                        mAccountType = "agent";
                        break;

                }
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
        mUser = new User();
        mUserRef.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {

                    mUser = dataSnapshot.getValue(User.class);

                    mNameEt.setText(mUser.getUserName());
                    mEmailEt.setText(mUser.getEmail());

                    String country = mUser.getCountry();

                    if (country.equals("Bangladesh")) {
                        mAccountTypeRg.setVisibility(View.VISIBLE);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(PaymentMethodActivity.this,
                                android.R.layout.simple_spinner_item,
                                SpinConstant.LOCAL_PAYMENT_METHOD
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        mPaymentSpinner.setAdapter(adapter);
                    } else {
                        mAccountTypeRg.setVisibility(View.GONE);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(PaymentMethodActivity.this,
                                android.R.layout.simple_spinner_item,
                                SpinConstant.INTERNATIONAL_PAYMENT_METHOD
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mPaymentSpinner.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.toException().toString());
            }
        });

    }

    @OnClick(R.id.sendRequest_bt)
    public void onSendRequestClick() {
        String mobileNumber = mMobileEt.getText().toString();
        String withdrawPoint = mPointEt.getText().toString();
        String name = mNameEt.getText().toString();
        String email = mEmailEt.getText().toString();
        String paymentType = mPaymentSpinner.getSelectedItem().toString();

        if (mobileNumber.isEmpty()) {
            mMobileEt.setError("This field should not be empty.");
            return;
        } else if(mobileNumber.length() < 10) {
            mMobileEt.setError("Invalid mobile number");
            return;
        } else if (withdrawPoint.isEmpty()) {
            mPointEt.setError("Point field should not be empty.");
            return;
        } else if (Integer.parseInt(withdrawPoint) > mTotalPoint) {
            mPointEt.setError("Your entered amount is exceed to your main amount");
            return;
        }  else if (Integer.parseInt(withdrawPoint) < MIN_CON_TO_WITHDRAW) {
            mPointEt.setError("You can't withdraw point less than 500000");
            return;
        } else if(name.isEmpty()) {
            mNameEt.setError("Name should not be empty");
            return;
        } else if(email.isEmpty()) {
            mEmailEt.setError("Email should not be empty");
            return;
        }

        String currentDate = DateUtils.getCurrentDateString();
        PaymentRequest paymentRequest = new PaymentRequest(mCurrentUserId, currentDate,
                Integer.parseInt(withdrawPoint), mobileNumber, mAccountType, mPaymentType);

        paymentRequest.setName(name);
        paymentRequest.setEmail(email);
        paymentRequest.setPaymentType(paymentType);

        // adding payment info
        paymentRequestViewModel.addPaymentRequest(paymentRequest).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {

                    int updatedTotalPoint = mTotalPoint - Integer.parseInt(withdrawPoint);

                    Map map = new HashMap();
                    map.put("totalPoint", updatedTotalPoint);

                    // updating earning node
                    mEarningRef.child(mCurrentUserId).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(PaymentMethodActivity.this, "Your Payment request is sent. Please wait for complete.",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });


                }
            }
        });



    }

}
