package ru.cityvoicer.golosun;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.cityvoicer.golosun.model.Profile;

public class TutorialFragment extends Fragment {
    public static final String ARG_MODE = "mode";
    public static final int MODE_TUTORIAL = 1;
    public static final int MODE_INTRO = 2;

    public static TutorialFragment newInstance(int mode) {
        TutorialFragment fr = new TutorialFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MODE, mode);
        fr.setArguments(args);
        return fr;
    }

    private ViewPager mViewPager;
    private ScreensPagerAdapter mAdapter;

    public int getMode() {
        return getArguments().getInt(ARG_MODE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial, container, false);

        mViewPager = (ViewPager)view.findViewById(R.id.pager);

        view.findViewById(R.id.close_text_view_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new ScreensPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
    }

    private class ScreensPagerAdapter extends FragmentStatePagerAdapter {
        public ScreensPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (getMode() == MODE_TUTORIAL) {
                switch (position) {
                    case 0:
                        return TutorialPageFragment.newInstance(R.raw.tutorial_1);
                    case 1:
                        return TutorialPageFragment.newInstance(R.raw.tutorial_2);
                    case 2:
                        return TutorialPageFragment.newInstance(R.raw.tutorial_3);
                    case 3:
                        return TutorialPageFragment.newInstance(R.raw.tutorial_4);
                    case 4:
                        return TutorialPageFragment.newInstance(R.raw.tutorial_5);
                    case 5:
                        return TutorialPageFragment.newInstance(R.raw.tutorial_6);
                    case 6:
                        return TutorialPageFragment.newInstance(R.raw.tutorial_7);
                    case 7:
                        return TutorialPageFragment.newInstance(R.raw.tutorial_8);
                    case 8:
                        return TutorialPageFragment.newInstance(R.raw.tutorial_9);
                }
            } else if (getMode() == MODE_INTRO){
                switch (position) {
                    case 0:
                        return TutorialPageFragment.newInstance(R.raw.intro_1);
                    case 1:
                        return TutorialPageFragment.newInstance(R.raw.intro_2);
                    case 2:
                        return TutorialPageFragment.newInstance(R.raw.intro_3);
                    case 3:
                        return TutorialPageFragment.newInstance(R.raw.intro_4);
                    case 4:
                        return TutorialPageFragment.newInstance(R.raw.intro_5);
                    case 5:
                        return TutorialPageFragment.newInstance(R.raw.intro_6);
                    case 6:
                        return TutorialPageFragment.newInstance(R.raw.intro_7);
                }
            }

            return null;
        }

        @Override
        public int getCount() {
            if (getMode() == MODE_TUTORIAL) {
                return 9;
            } else if (getMode() == MODE_INTRO) {
                return 7;
            }
            return 0;
        }
    }

}
