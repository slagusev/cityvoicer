package ru.cityvoicer.golosun;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import ru.cityvoicer.golosun.model.AdList;
import ru.cityvoicer.golosun.model.Profile;

public class MainTopBar extends FrameLayout implements Profile.IProfileChangesCallback {
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public MainTopBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MainTopBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static MainTopBar create(LayoutInflater inflater) {
        return ((MainTopBar)inflater.inflate(R.layout.main_topbar, null)).init();
    }

    public MainTopBar init() {
        findViewById(R.id.side_menu_show_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBackButtonMode) {
                    GolosunActivity.gActivity.getSupportFragmentManager().popBackStack();
                } else {
                    if (!AdList.getInstance().isEmpty()) {
                        return;
                    }
                    if (GolosunActivity.gActivity.isSideMenuExpanded()) {
                        GolosunActivity.gActivity.closeSideMenu();
                    } else {
                        GolosunActivity.gActivity.expandSideMenu();
                    }
                }
            }
        });

        update();
        Profile.getInstance().addListener(this);
        return this;
    }

    public void deinit() {
        Profile.getInstance().removeListener(this);
    }

    @Override
    public void onChanged(Profile profile) {
        update();
    }

    public void update() {
        String strBalance = "";
        if (Profile.getInstance().isRegistred()) {
            strBalance = Profile.getInstance().getBalance();
            if (Profile.getInstance().getAdVoteMoneyCounter() > 0) {
                strBalance += "+" + Profile.getInstance().getAdVoteMoneyCounter();
            }
        }
        //strBalance += '\u20BD';
        ((TextView)findViewById(R.id.money_text)).setText(strBalance);
    }

    private boolean mBackButtonMode;

    public void setBackButton(boolean backButton) {
        mBackButtonMode = backButton;
        ((ImageView)findViewById(R.id.side_menu_show_button)).setImageResource(backButton ? R.mipmap.ico_arrow : R.mipmap.ico_menu);
    }
}
