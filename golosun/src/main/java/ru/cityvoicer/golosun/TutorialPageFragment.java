package ru.cityvoicer.golosun;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import ru.cityvoicer.golosun.model.Profile;

public class TutorialPageFragment extends Fragment {
    public static final String ARG_IMAGE_ID = "image_id";

    public static TutorialPageFragment newInstance(int imageId) {
        TutorialPageFragment fr = new TutorialPageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_IMAGE_ID, imageId);
        fr.setArguments(args);
        return fr;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial_page, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;
        Bitmap bm = BitmapFactory.decodeResource(getContext().getResources(), getArguments().getInt(ARG_IMAGE_ID), opts);
        ((ImageView)getView().findViewById(R.id.image_view)).setImageBitmap(bm);
        System.gc();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((ImageView)getView().findViewById(R.id.image_view)).setImageBitmap(null);
        System.gc();
    }
}
