package ru.cityvoicer.golosun;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.cityvoicer.golosun.api.ApiService;
import ru.cityvoicer.golosun.api.NetMoneyToPhoneResponse;
import ru.cityvoicer.golosun.model.Profile;

public class GiveMoneyFragment extends Fragment {
    static public String TAG = "GiveMoneyFragment";

    static public GiveMoneyFragment newInstance() {
        return new GiveMoneyFragment();
    }

    private EditText mPhoneEditText;
    private EditText mMoneyEditText;
    private View mGiveButton;
    private TextView mGiveButtonText;

    private boolean mRequestSend;
    private boolean mRequestSuccess;

    public GiveMoneyFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_givemoney, container, false);
        mPhoneEditText = (EditText)view.findViewById(R.id.phone_edittext);
        mMoneyEditText = (EditText)view.findViewById(R.id.money_edittext);
        mGiveButton = view.findViewById(R.id.give_button);
        mGiveButtonText = (TextView)view.findViewById(R.id.give_button_text);

        mGiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fixMoney()) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Сумма не подходящая")
                            .setMessage("Сумма денег должна быть кратна 50 рублям")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    return;
                }
                if (!mRequestSend && Profile.getInstance().isRegistred()) {
                    mRequestSend = true;
                    updateStatus();
                    ApiService.getApi().moneyToPhone(Profile.getInstance().getAutorization(), Integer.parseInt(mMoneyEditText.getText().toString()) , "+7" + mPhoneEditText.getText().toString()).enqueue(new Callback<NetMoneyToPhoneResponse>() {
                        @Override
                        public void onResponse(Response<NetMoneyToPhoneResponse> response, Retrofit retrofit) {
                            mRequestSend = false;
                            updateStatus();
                            mRequestSuccess = response.isSuccess() && response.body().success;
                            if (getActivity() != null && response.body() != null && response.body().data != null && response.body().data.name != null && response.body().data.message != null ) {
                                new AlertDialog.Builder(getContext())
                                        .setTitle(response.body().data.name)
                                        .setMessage(response.body().data.message)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (mRequestSuccess) {
                                                    GolosunActivity.gActivity.showAdListScreen();
                                                }
                                            }
                                        })
                                        .setIcon(response.body().success ? android.R.drawable.ic_dialog_info : android.R.drawable.ic_dialog_alert)
                                        .show();
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            mRequestSend = false;
                            updateStatus();
                        }
                    });
                }
            }
        });

        mPhoneEditText.addTextChangedListener(new TextWatcher() {
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

        mMoneyEditText.addTextChangedListener(new TextWatcher() {
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

        mMoneyEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    fixMoney();
                }
            }
        });

        return view;
    }

    public boolean fixMoney() {
        try {
            int initialMoney = Integer.parseInt(mMoneyEditText.getText().toString());
            int money = (initialMoney + 25) / 50 * 50;
            if (money <= 0) {
                money = 50;
            }
            if (initialMoney != money) {
                mMoneyEditText.setText("" + money);
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String num = "";
        try {
            num = ((TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
        mPhoneEditText.setText(num);

        updateStatus();
    }

    private void updateStatus() {
        if (mPhoneEditText.getText() == null || mPhoneEditText.getText().toString().isEmpty() || mMoneyEditText.getText() == null || mMoneyEditText.getText().toString().isEmpty() || mRequestSend) {
            mGiveButton.setEnabled(false);
            mGiveButtonText.setAlpha(0.5f);
            return;
        }
        String phoneText = "+7" + mPhoneEditText.getText().toString();
        String moneyText = mMoneyEditText.getText().toString();

        if (!android.util.Patterns.PHONE.matcher(phoneText).matches()) {
            mGiveButton.setEnabled(false);
            mGiveButtonText.setAlpha(0.5f);
            return;
        }

        try {
            Integer.parseInt(moneyText);
        } catch (Exception ex) {
            mGiveButton.setEnabled(false);
            mGiveButtonText.setAlpha(0.5f);
            return;
        }

        mGiveButton.setEnabled(true);
        mGiveButtonText.setAlpha(1.0f);
    }
}
