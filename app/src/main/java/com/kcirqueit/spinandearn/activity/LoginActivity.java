package com.kcirqueit.spinandearn.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;

import com.google.firebase.auth.FirebaseAuth;
import com.kcirqueit.spinandearn.R;
import com.kcirqueit.spinandearn.util.InternetConnection;
import com.rilixtech.CountryCodePicker;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {


    private static final String TAG = "LoginActivity";

    private AppCompatEditText mPhoneEt;

    private CountryCodePicker mCountryCodePicker;

    private AppCompatButton mLoginBt;

    private FirebaseAuth mAuth;

    @BindView(R.id.terms_check_box)
    AppCompatCheckBox mTermsCheckBox;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();


        /*SharedPreferences pref2 = getApplicationContext().getSharedPreferences("MyPref2", 0);
        boolean isSaved = pref2.getBoolean("isAlreadyOpened", false);
        if (!isSaved) {
            Log.d("isSaved", isSaved+"");
            gotoWelcomeActivity();
        }*/



        // checking for internet connection
        /*if (!InternetConnection.checkConnection(this)) {
            Log.d(TAG, "Internet connection failed");
            InternetConnection.showNoInternetDialog(this);
        } else {

            if (mAuth.getCurrentUser() != null) {
                Intent mainInten = new Intent(LoginActivity.this, DashBoardActivity.class);
                startActivity(mainInten);
                finish();
            }

        }*/
        ButterKnife.bind(this);

        initView();

        mCountryCodePicker.registerPhoneNumberTextView(mPhoneEt);

        mTermsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mLoginBt.setEnabled(true);
                } else {
                    mLoginBt.setEnabled(false);
                }
            }
        });

    }


    public void initView() {
        mPhoneEt = findViewById(R.id.phone_et);
        mCountryCodePicker = findViewById(R.id.ccp);
        mLoginBt = findViewById(R.id.login_bt);
    }


    @Override
    protected void onResume() {
        super.onResume();

        mLoginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

    }

    public void performLogin() {

        String code = mCountryCodePicker.getSelectedCountryCodeWithPlus();

        String countryName = mCountryCodePicker.getSelectedCountryName();

        String number = mPhoneEt.getText().toString();

        if (number.isEmpty() || number.length() < 10) {
            mPhoneEt.setError("Phone number is not valid!");
            mPhoneEt.requestFocus();
            return;
        }

        String phoneNumber = code + number;

        Intent verifyIntent = new Intent(this, VerificationActivity.class);
        verifyIntent.putExtra("phoneNumber", phoneNumber);
        verifyIntent.putExtra("country", countryName);
        startActivity(verifyIntent);
        finish();

    }


    public void gotoWelcomeActivity() {
        Intent welcomeIntent = new Intent(this, WelcomActivity.class) ;
        startActivity(welcomeIntent);
    }


}
