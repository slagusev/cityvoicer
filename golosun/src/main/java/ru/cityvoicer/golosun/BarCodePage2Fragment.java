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

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.cityvoicer.golosun.design.AspectFrameLayout;
import ru.cityvoicer.golosun.model.Cards;

public class BarCodePage2Fragment extends Fragment implements Cards.ICardsChangesCallback {
    @Bind(R.id.mBarCodeImageFrameLayout) AspectFrameLayout mBarCodeImageFrameLayout;
    @Bind(R.id.mImageView) ImageView mImageView;
    @Bind(R.id.mProgressBar) ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barcode_page2, container, false);
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
        Bitmap bitmap = Cards.getInstance().getBarCodeImage();
        mProgressBar.setVisibility(bitmap == null ? View.VISIBLE : View.INVISIBLE);

        if (bitmap == null) {
            mImageView.setImageBitmap(null);
            return;
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] src = new int[w * h];
        int[] dst = new int[w * h];
        bitmap.getPixels(src, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int j=0; j < h; j++) {
            for (int i=0; i < w; i++) {
                dst[i * h + h - 1 - j] = src[j * w + i];
            }
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap(dst, h, w, bitmap.getConfig());

        if (rotatedBitmap.getHeight() < 512) {
            rotatedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, rotatedBitmap.getWidth() * 2, rotatedBitmap.getHeight() * 2, false);
        } else  if (rotatedBitmap.getHeight() < 256) {
            rotatedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, rotatedBitmap.getWidth() * 4, rotatedBitmap.getHeight() * 4, false);
        }

        mBarCodeImageFrameLayout.setAspect((float)rotatedBitmap.getWidth() / (float)rotatedBitmap.getHeight());

        mImageView.setImageBitmap(rotatedBitmap);
    }
}
