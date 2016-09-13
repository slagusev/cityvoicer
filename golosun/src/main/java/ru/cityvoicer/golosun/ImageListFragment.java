package ru.cityvoicer.golosun;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.cityvoicer.golosun.design.AspectFrameLayout;
import ru.cityvoicer.golosun.model.SavedVoteImages;

public class ImageListFragment extends Fragment {
    static public String TAG = "ImageListFragment";

    @Bind(R.id.mRecyclerView) RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(new MyAdapter());
    }

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        public static class ViewHolder extends RecyclerView.ViewHolder {
            public AspectFrameLayout mRoot;
            public ImageView mImageView;
            public ViewHolder(View v) {
                super(v);
                mRoot = (AspectFrameLayout) v.findViewById(R.id.mAspectFrameLayout);
                mImageView = (ImageView) v.findViewById(R.id.mImageView);
            }
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_image_list, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Bitmap bm = SavedVoteImages.getInstance().loadImagesByNumber(SavedVoteImages.getInstance().getImagesCount() - position - 1);
            holder.mImageView.setImageBitmap(bm);
            if (bm != null) {
                holder.mRoot.setAspect((float)bm.getWidth() / (float)bm.getHeight());
            } else {
                holder.mRoot.setAspect(10);
            }
        }

        @Override
        public int getItemCount() {
            return SavedVoteImages.getInstance().getImagesCount();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}
