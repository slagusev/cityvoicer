package ru.cityvoicer.golosun;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import ru.cityvoicer.golosun.model.AdList;
import ru.cityvoicer.golosun.model.ESex;
import ru.cityvoicer.golosun.model.Profile;

public class RegistrationFragment extends Fragment implements Profile.IProfileChangesCallback {
    static public RegistrationFragment newInstance() {
        return new RegistrationFragment();
    }

    private Spinner mGenderSpinner;
    private EditText mAgeTextView;
    private View mRegisterButton;
    private TextView mRegisterButtonText;
    private TextView mLicenseText;
    private CheckBox mLicenseCheckBox;
    private WebView mLicenseWebView;
    private View mLicenseCloseButton;
    private View mLicenseFrame;

    public RegistrationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration, container, false);
        mGenderSpinner = (Spinner)view.findViewById(R.id.gender_spinner);
        mAgeTextView = (EditText)view.findViewById(R.id.age_edittext);
        mRegisterButton = view.findViewById(R.id.register_button);
        mLicenseText = (TextView)view.findViewById(R.id.license_text);
        mLicenseCheckBox = (CheckBox)view.findViewById(R.id.license_check_box);
        mRegisterButtonText = (TextView)view.findViewById(R.id.register_button_text);
        mLicenseWebView = (WebView)view.findViewById(R.id.web_view);
        mLicenseCloseButton = view.findViewById(R.id.close_license_button);
        mLicenseFrame = view.findViewById(R.id.license_frame);

        mLicenseFrame.setVisibility(View.INVISIBLE);
        mLicenseCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLicenseFrame.setVisibility(View.INVISIBLE);
            }
        });

        mLicenseWebView.loadUrl("file:///android_asset/license.html");

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sex = mGenderSpinner.getSelectedItemPosition() == 0 ? ESex.MALE.get() : ESex.FEMALE.get();
                Profile.getInstance().updateAgeAndSex(Integer.parseInt(mAgeTextView.getText().toString()), sex);
            }
        });

        mLicenseText.setPaintFlags(mLicenseText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        mLicenseText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.LicenseUrl)));
                mLicenseFrame.setVisibility(View.VISIBLE);
            }
        });

        mAgeTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateStatus();
            }
        });
        mLicenseCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateStatus();
            }
        });


        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.genders, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGenderSpinner.setAdapter(adapter);

        updateStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        Profile.getInstance().addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Profile.getInstance().removeListener(this);
    }

    private void updateStatus() {
        if (!mLicenseCheckBox.isChecked() || mAgeTextView.getText() == null || mAgeTextView.getText().toString().isEmpty() || Profile.getInstance().getAgeSetPending() != null || Profile.getInstance().getSexSetPending() != null) {
            mRegisterButton.setEnabled(false);
            mRegisterButtonText.setAlpha(0.5f);
            return;
        }
        String ageText = mAgeTextView.getText().toString();
        try {
            Integer.parseInt(ageText);
            mRegisterButton.setEnabled(true);
            mRegisterButtonText.setAlpha(1.0f);
        } catch (Exception ex) {
            mRegisterButton.setEnabled(false);
            mRegisterButtonText.setAlpha(0.5f);
        }
    }

    @Override
    public void onChanged(Profile profile) {
        if (profile.isRegistred()) {
            GolosunActivity.gActivity.popFragment();
            GolosunActivity.gActivity.showAdListScreen();
            if (AdList.getInstance().findBestAdToShow() == null) {
                GolosunActivity.gActivity.showCardScreen();
            } else {
                GolosunActivity.gActivity.showAdListScreen();
            }
        }
    }
}
