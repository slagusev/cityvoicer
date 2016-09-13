package ru.cityvoicer.golosun;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.cityvoicer.golosun.design.AspectFrameLayout;
import ru.cityvoicer.golosun.model.Cards;

public class BarCodeFragment extends Fragment {
    static public String TAG = "BarCodeFragment";

    static public BarCodeFragment newInstance() {
        return new BarCodeFragment();
    }

    public BarCodeFragment() {
    }

    @Bind(R.id.mViewPager) ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barcode, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewPager.setAdapter(new PagerAdapter(getChildFragmentManager()));
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                return new BarCodePage1Fragment();
            } else {
                return new BarCodePage2Fragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
