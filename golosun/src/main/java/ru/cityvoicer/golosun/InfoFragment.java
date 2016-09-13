package ru.cityvoicer.golosun;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.cityvoicer.golosun.model.Profile;

public class InfoFragment extends Fragment implements Profile.IProfileChangesCallback {
    static public String TAG = "InfoFragment";

    private TextView mLoginTextView;
    private TextView mPassTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        mLoginTextView = (TextView)view.findViewById(R.id.login_text);
        mPassTextView = (TextView)view.findViewById(R.id.pass_text);

        view.findViewById(R.id.login_text_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard("login", mLoginTextView.getText() != null ? mLoginTextView.getText().toString() : null);
            }
        });

        view.findViewById(R.id.pass_text_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard("password", mPassTextView.getText() != null ? mPassTextView.getText().toString() : null);
            }
        });

        view.findViewById(R.id.show_connect_tutorial).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GolosunActivity.gActivity.showInitialTutorialScreen();
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }

    void copyToClipboard(String label, String text) {
        try {
            ClipboardManager clipboard = (ClipboardManager) GolosunApp.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
        } catch (Exception ex) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Profile.getInstance().addListener(this);
        update();
    }

    @Override
    public void onPause() {
        super.onPause();
        Profile.getInstance().removeListener(this);
    }

    @Override
    public void onChanged(Profile profile) {
        update();
    }

    void update() {
        if (Profile.getInstance().isInServerInitialRegistred()) {
            mLoginTextView.setText("" + Profile.getInstance().getLogin());
            mPassTextView.setText("" + Profile.getInstance().getPassword());
        } else {
            mLoginTextView.setText(null);
            mPassTextView.setText(null);
        }
    }
}
