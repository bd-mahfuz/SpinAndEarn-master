package com.kcirqueit.spinandearn.viewModel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.kcirqueit.spinandearn.model.User;
import com.kcirqueit.spinandearn.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {

    private UserRepository mUserRepository;

    public UserViewModel(@NonNull Application application) {
        super(application);
        mUserRepository = UserRepository.getInstance();

    }

    public Task addUser(User user) {
        return mUserRepository.addUser(user);
    }

    public LiveData<DataSnapshot> getAllUsers() {
        return mUserRepository.getAllUser();
    }



}
