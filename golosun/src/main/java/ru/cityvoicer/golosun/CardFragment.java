package ru.cityvoicer.golosun;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.cityvoicer.golosun.api.ApiService;
import ru.cityvoicer.golosun.api.NetMoneyToPhoneResponse;
import ru.cityvoicer.golosun.design.AspectFrameLayout;
import ru.cityvoicer.golosun.model.Cards;
import ru.cityvoicer.golosun.model.Profile;

public class CardFragment extends Fragment {
    static public String TAG = "CardFragment";

    static public CardFragment newInstance() {
        return new CardFragment();
    }

    public CardFragment() {
    }

    @Bind(R.id.mViewPager) ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewPager.setAdapter(new PagerAdapter(getChildFragmentManager()));
    }

    @OnClick(R.id.mUseCard)
    void onUseCardClick() {
        Cards.getInstance().notifyBarCodeShowed();
        GolosunActivity.gActiveActivity.showBarCodeScreen();
    }

    @OnClick(R.id.mGoToWeb)
    void onGoToWebClick() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://3skovorodki.ru/"));
        startActivity(browserIntent);
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new ImageFragment();
            Bundle args = new Bundle();
            args.putInt(ImageFragment.ARG_IMAGE_ID, i == 0 ? R.drawable.card_front : R.drawable.card_back);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public static class ImageFragment extends Fragment {
        public static final String ARG_IMAGE_ID = "image_id";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_card_page, container, false);
            ImageView image = ((ImageView)rootView.findViewById(R.id.image_view));
            image.setImageResource(getArguments().getInt(ARG_IMAGE_ID));
            /*
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CardFragment fr = (CardFragment)(((GolosunActivity)getActivity()).getCurrentFragment());
                    fr.openCardImage(getArguments().getInt(ARG_IMAGE_ID));
                }
            });
            */
            return rootView;
        }
    }
}
