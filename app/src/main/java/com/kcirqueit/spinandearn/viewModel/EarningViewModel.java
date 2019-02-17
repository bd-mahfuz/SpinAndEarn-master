package com.kcirqueit.spinandearn.viewModel;

import android.app.Application;

import com.google.android.gms.tasks.Task;
import com.kcirqueit.spinandearn.model.Earning;
import com.kcirqueit.spinandearn.repository.EarningRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class EarningViewModel extends AndroidViewModel {


    private EarningRepository earningRepository;

    public EarningViewModel(@NonNull Application application) {
        super(application);

        earningRepository = EarningRepository.getInstance();

    }


    public Task addEarning(Earning earning) {
        return earningRepository.addEarning(earning);
    }




}
