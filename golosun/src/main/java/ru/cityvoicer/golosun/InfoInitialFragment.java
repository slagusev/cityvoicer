package ru.cityvoicer.golosun;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.cityvoicer.golosun.model.Profile;

public class InfoInitialFragment extends InfoFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Profile.getInstance().isInServerInitialRegistred()) {
                    GolosunActivity.gActivity.showRegistrationScreen();
                }
            }
        });
        return view;
    }
}
