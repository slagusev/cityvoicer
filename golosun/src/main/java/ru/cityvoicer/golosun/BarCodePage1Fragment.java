package ru.cityvoicer.golosun;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.cityvoicer.golosun.design.AspectFrameLayout;
import ru.cityvoicer.golosun.model.Cards;

public class BarCodePage1Fragment extends Fragment implements Cards.ICardsChangesCallback {

    @Bind(R.id.mBarCodeImageFrameLayout) AspectFrameLayout mBarCodeImageFrameLayout;
    @Bind(R.id.mImageView) ImageView mImageView;
    @Bind(R.id.mProgressBar) ProgressBar mProgressBar;
    @Bind(R.id.mBarCodeNumberTextView) TextView mBarCodeNumberTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barcode_page1, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Cards.getInstance().addListener(this);
        update();
    }

    @Override
    public void onPause() {
        super.onPause();
        Cards.getInstance().removeListener(this);
    }

    @Override
    public void onChanged(Cards cards) {
        update();
    }

    void update() {
        Bitmap bm = Cards.getInstance().getBarCodeImage();
        mImageView.setImageBitmap(bm);
        mBarCodeNumberTextView.setText(Cards.getInstance().getBarCodeNumber());
        mProgressBar.setVisibility(bm != null ? View.INVISIBLE : View.VISIBLE);
        mBarCodeImageFrameLayout.setAspect(bm != null ? (float)bm.getWidth() / (float)bm.getHeight() : 3.27f);
    }
}
