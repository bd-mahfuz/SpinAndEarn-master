package com.kcirqueit.spinandearn.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kcirqueit.spinandearn.model.PaymentRequest;

import androidx.lifecycle.LiveData;

public class PaymentRequestsRepository {

    private static PaymentRequestsRepository paymentRequestsRepository;

    private FirebaseQueryLiveData firebaseQueryLiveData;

    private DatabaseReference mRootRef;
    private DatabaseReference mPaymentRef;

    private PaymentRequestsRepository() {
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mPaymentRef = mRootRef.child("PaymentRequests");
    }

    public static PaymentRequestsRepository getInstance() {

        if (paymentRequestsRepository == null) {
            paymentRequestsRepository = new PaymentRequestsRepository();
            return paymentRequestsRepository;
        }

        return paymentRequestsRepository;
    }


    public Task addPaymentRequest(PaymentRequest paymentRequest) {
        String key = mPaymentRef.push().getKey();
        paymentRequest.setId(key);
        return mPaymentRef.child(paymentRequest.getUserId()).child(key).setValue(paymentRequest);
    }

    public LiveData<DataSnapshot> getPaymentRequestByUserId(String userId) {
        mPaymentRef = mPaymentRef.child(userId);
        return new FirebaseQueryLiveData(mPaymentRef);
    }

}
