package ru.cityvoicer.golosun;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import ru.cityvoicer.golosun.model.AdList;
import ru.cityvoicer.golosun.model.Profile;
import ru.cityvoicer.golosun.model.Settings;

public class SideMenu extends FrameLayout implements Profile.IProfileChangesCallback {
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SideMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SideMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private View mCardMenu;
    private View mGiveMoneyMenu;
    private TextView mIdTextView;
    private View mUserIdMenu;
    private CheckBox mSoundCheckBox;
    private CheckBox mVideoCheckBox;
    private View mTutorialMenu;
    private View mTutorialConnectMenu;

    public void init() {
        mCardMenu = findViewById(R.id.card_menu);
        mGiveMoneyMenu = findViewById(R.id.give_money_menu);
        mIdTextView = (TextView)findViewById(R.id.user_id_textview);
        mUserIdMenu = findViewById(R.id.user_id_menu);
        mTutorialMenu = findViewById(R.id.tutorial_menu);
        mTutorialConnectMenu = findViewById(R.id.connection_tutorial_menu);

        mSoundCheckBox = (CheckBox)findViewById(R.id.sound_checkbox);
        mVideoCheckBox = (CheckBox)findViewById(R.id.video_checkbox);

        mCardMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GolosunActivity.gActivity.closeSideMenu();
                GolosunActivity.gActivity.showCardScreen();
            }
        });

        mGiveMoneyMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GolosunActivity.gActivity.closeSideMenu();
                GolosunActivity.gActivity.showGiveMoneyScreen();
            }
        });

        mUserIdMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //GolosunActivity.gActivity.closeSideMenu();
                //GolosunActivity.gActivity.showInfoScreen();
            }
        });

        mTutorialMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GolosunActivity.gActivity.closeSideMenu();
                GolosunActivity.gActivity.showTutorialScreen();
            }
        });

        mTutorialConnectMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GolosunActivity.gActivity.closeSideMenu();
                GolosunActivity.gActivity.showInitialTutorialScreen();
            }
        });

        findViewById(R.id.image_list_menu).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GolosunActivity.gActivity.closeSideMenu();
                GolosunActivity.gActivity.showImageListScreen();
            }
        });

        update();

        mSoundCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.setSoundEnabled(isChecked);
            }
        });

        mVideoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.setVideoEnabled(isChecked);
            }
        });
    }

    public void activate() {
        Profile.getInstance().addListener(this);
        update();
    }

    public void deactivate() {
        Profile.getInstance().removeListener(this);
    }

    @Override
    public void onChanged(Profile profile) {
        update();
    }

    void update() {
        mIdTextView.setText(Profile.getInstance().isRegistred() ? "Данные подключения ID: " + (Profile.getInstance().getUserId() != null ? Profile.getInstance().getUserId() : "") : "");
        mSoundCheckBox.setChecked(Settings.isSoundEnabled());
        mVideoCheckBox.setChecked(Settings.isVideoEnabled());
    }
}