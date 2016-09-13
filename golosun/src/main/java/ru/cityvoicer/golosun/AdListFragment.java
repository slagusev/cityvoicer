package ru.cityvoicer.golosun;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import ru.cityvoicer.golosun.api.NetAdItem;
import ru.cityvoicer.golosun.model.AdList;
import ru.cityvoicer.golosun.model.Cards;
import ru.cityvoicer.golosun.model.EAdFlagMedia;
import ru.cityvoicer.golosun.model.EVote;
import ru.cityvoicer.golosun.model.SavedVoteImages;
import ru.cityvoicer.golosun.services.NetworkStateReceiver;

public class AdListFragment extends Fragment {
    static public AdListFragment newInstance() {
        return new AdListFragment();
    }
    static final int LONG_TOUCH_TIME = 1500;


    String[] itemname = {
            "Три сковородки",
            "Карта 2",
            "Карта 3",
            "Карта 4"};

    Integer[] imgid = {
            R.drawable.card1,
            R.drawable.card2,
            R.drawable.card2,
            R.drawable.card2
    };

    public AdListFragment() {
    }

    private String mRequestedTitleImageUrl;
    private ImageView mTitleImageView;
    private String mRequestedMainImageUrl;
    private ImageView mMainImageView;
    private TextView mTitleTextView;
    private TextView mMainTextView;
    private View mGoodButton;
    private View mBadButton;
    private View mNoAdsFrame;
    private View mFeedbackFrame;
    private View mFeedbackButton;
    private EditText mFeedbackEditText;
    private View mButtonsFrame;
    private ImageView mGoodButtonImageView2;
    private ImageView mBadButtonImageView2;
    private ImageView mGoodButtonImageView;
    private ImageView mBadButtonImageView;
    private ImageView mReloadImageButton;
    private NetAdItem mCurrentAd;
    private EVote mCurrentVote;
    private Handler mHandler = new Handler();
    private Runnable mBadLongClickRunnable;
    private Runnable mGoodLongClickRunnable;
    private String mRequestedVideoUrl;
    private VideoView mVideoView;
    private View mMainTextFrame;
    private ImageView mHiddenImage;
    private Bitmap mCurrentMainImage;
    private View CityCardFrame;
    private View mainFrame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adlist, container, false);
        ListView cList = (ListView) view.findViewById(R.id.cityCard);
        CityCard_List adapter = new CityCard_List(getActivity(), itemname , imgid);
        cList.setAdapter(adapter);
        mTitleImageView = (ImageView)view.findViewById(R.id.title_image);
        mMainImageView = (ImageView)view.findViewById(R.id.main_image);
        mTitleTextView = (TextView)view.findViewById(R.id.title_text);
        mMainTextView = (TextView)view.findViewById(R.id.main_text);
        mGoodButton = view.findViewById(R.id.good_button);
        mBadButton = view.findViewById(R.id.bad_button);
        mNoAdsFrame = view.findViewById(R.id.noAdsFrame);
        mFeedbackFrame = view.findViewById(R.id.feedbackFrame);
        mFeedbackButton = view.findViewById(R.id.feedbackButton);
        mFeedbackEditText = (EditText)view.findViewById(R.id.feedbackEditText);
        mButtonsFrame = view.findViewById(R.id.buttons_frame);
        mGoodButtonImageView2 = (ImageView)view.findViewById(R.id.b_button_image);
        mBadButtonImageView2 = (ImageView)view.findViewById(R.id.a_button_image);
        mGoodButtonImageView = (ImageView)view.findViewById(R.id.good_button_image);
        mBadButtonImageView = (ImageView)view.findViewById(R.id.bad_button_image);
        mReloadImageButton = (ImageView)view.findViewById(R.id.reloadImageButton);
        mVideoView = (VideoView)view.findViewById(R.id.video_view);
        mMainTextFrame = view.findViewById(R.id.main_text_frame);
        mHiddenImage = (ImageView)view.findViewById(R.id.hidden_image);

        CityCardFrame = view.findViewById(R.id.CityCardFrame);
        mainFrame = view.findViewById(R.id.mainFrame);

        mNoAdsFrame.setVisibility(View.VISIBLE);
        mFeedbackFrame.setVisibility(View.INVISIBLE);
        mReloadImageButton.setVisibility(View.INVISIBLE);


        cList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView ts = (TextView)view.findViewById(R.id.item);
                String sel_item = ts.getText().toString();
                if(i == 0) {
                Cards.getInstance().notifyBarCodeShowed();
                GolosunActivity.gActiveActivity.showBarCodeScreen(); }
            }
        });
        
        mGoodButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mGoodButton.setPressed(true);
                    if (mGoodLongClickRunnable == null) {
                        mGoodLongClickRunnable = new Runnable() {
                            @Override
                            public void run() {
                                mGoodLongClickRunnable = null;
                                if (mGoodButton.isPressed()) {
                                    mCurrentVote = EVote.GOOD;
                                    mFeedbackFrame.setVisibility(View.VISIBLE);
                                    mFeedbackButton.setEnabled(false);
                                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.showSoftInput(mFeedbackEditText, InputMethodManager.SHOW_IMPLICIT);
                                }
                                mGoodButton.setPressed(false);
                            }
                        };
                        mHandler.postDelayed(mGoodLongClickRunnable, LONG_TOUCH_TIME);
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    Rect viewRect = new Rect();
                    mGoodButton.getHitRect(viewRect);
                    if (!viewRect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        mGoodButton.setPressed(false);
                        if (mGoodLongClickRunnable != null) {
                            mHandler.removeCallbacks(mGoodLongClickRunnable);
                            mGoodLongClickRunnable = null;
                        }
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mGoodButton.setPressed(false);
                    if (mGoodLongClickRunnable != null) {
                        mHandler.removeCallbacks(mGoodLongClickRunnable);
                        mGoodLongClickRunnable = null;
                        voteTunned(EVote.GOOD, null);
                        mCurrentAd = null;
                        update();
                    }
                }
                return true;
            }
        });

        mBadButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mBadButton.setPressed(true);
                    if (mBadLongClickRunnable == null) {
                        mBadLongClickRunnable = new Runnable() {
                            @Override
                            public void run() {
                                mBadLongClickRunnable = null;
                                if (mBadButton.isPressed()) {
                                    mCurrentVote = EVote.BAD;
                                    mFeedbackFrame.setVisibility(View.VISIBLE);
                                    mFeedbackButton.setEnabled(false);
                                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.showSoftInput(mFeedbackEditText, InputMethodManager.SHOW_IMPLICIT);
                                }
                                mBadButton.setPressed(false);
                            }
                        };
                        mHandler.postDelayed(mBadLongClickRunnable, LONG_TOUCH_TIME);
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    Rect viewRect = new Rect();
                    mBadButton.getHitRect(viewRect);
                    if (!viewRect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        mBadButton.setPressed(false);
                        if (mBadLongClickRunnable != null) {
                            mHandler.removeCallbacks(mBadLongClickRunnable);
                            mBadLongClickRunnable = null;
                        }
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mBadButton.setPressed(false);
                    if (mBadLongClickRunnable != null) {
                        mHandler.removeCallbacks(mBadLongClickRunnable);
                        mBadLongClickRunnable = null;
                        voteTunned(EVote.BAD, null);
                        mCurrentAd = null;
                        update();
                    }
                }
                return true;
            }
        });

        mFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFeedbackEditText.getText() != null && !mFeedbackEditText.getText().toString().isEmpty()) {
                    mFeedbackFrame.setVisibility(View.INVISIBLE);
                    voteTunned(mCurrentVote, mFeedbackEditText.getText().toString());
                    hideKeyboradAndClearFeedbackEditTextFocus();
                    mFeedbackEditText.getText().clear();
                    mCurrentAd = null;
                    update();
                }
            }
        });

        mFeedbackEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mFeedbackButton.setEnabled(mFeedbackEditText.getText() != null && !mFeedbackEditText.getText().toString().isEmpty());
            }
        });

        mFeedbackFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboradAndClearFeedbackEditTextFocus();
            }
        });

        mReloadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestedMainImageUrl = null;
                mRequestedTitleImageUrl = null;
                mRequestedVideoUrl = null;
                update();
                mReloadImageButton.setVisibility(View.INVISIBLE);
            }
        });

        return view;
    }

    private void voteTunned(EVote vote, String text) {
        if (mCurrentAd.flag_media == EAdFlagMedia.AB.get()) {
            switch (vote) {
                case GOOD:
                    AdList.getInstance().vote(mCurrentAd, EVote.GOOD, text);
                    break;
                case BAD:
                    AdList.getInstance().vote(mCurrentAd, EVote.BAD, text);
                    break;
            }

        } else {
            AdList.getInstance().vote(mCurrentAd, vote, text);
        }
        if (mCurrentMainImage != null && mCurrentAd.flag_media == EAdFlagMedia.BANNER.get() && vote == EVote.GOOD) {
            SavedVoteImages.getInstance().pushImage(mCurrentMainImage);
        }
    }

    private void hideKeyboradAndClearFeedbackEditTextFocus() {
        mFeedbackEditText.clearFocus();
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mFeedbackEditText.getWindowToken(), 0);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCurrentAd = null;
        mRequestedTitleImageUrl = null;
        mRequestedMainImageUrl = null;
        mCurrentAd = null;
        mCurrentVote = null;
        update();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCurrentAd = null;
        mRequestedMainImageUrl = null;
        mRequestedTitleImageUrl = null;

        /*
        mCurrentAd = new NetAdItem();
        mCurrentAd.flag_media = EAdFlagMedia.VIDEO.get();
        mCurrentAd.name = "fake";
        mCurrentAd.image = "http://download.wavetlan.com/SVV/Media/HTTP/H264/Other_Media/H264_test5_voice_mp4_480x360.mp4";
*/

/*
        mCurrentAd = new NetAdItem();
        mCurrentAd.flag_media = EAdFlagMedia.AB.get();
        mCurrentAd.name = "fake";
        mCurrentAd.description = "description";
        mCurrentAd.image = "http://www.gettyimages.ca/gi-resources/images/Homepage/Category-Creative/UK/UK_Creative_462809583.jpg";
*/

        tick();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
        mBadLongClickRunnable = null;
        mGoodLongClickRunnable = null;
    }

    public void tick() {
        update();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tick();
            }
        }, 300);
    }

    public boolean isFeedbackDialogVisible() {
        return mFeedbackFrame != null && mFeedbackFrame.getVisibility() == View.VISIBLE;
    }

    private ImageLoaderCallback mImageLoaderCallback;

    public void update() {
        if (mFeedbackFrame.getVisibility() == View.VISIBLE || mMainImageView.getWidth() == 0 || mMainImageView.getHeight() == 0)
            return;

        if (mCurrentAd == null) {
            mButtonsFrame.setVisibility(View.INVISIBLE);
            mCurrentAd = AdList.getInstance().findBestAdToShow();
            mRequestedMainImageUrl = null;
            mRequestedTitleImageUrl = null;
            mRequestedVideoUrl = null;
            mReloadImageButton.setVisibility(View.INVISIBLE);
            Picasso.with(getActivity()).load((String)null).into(mTitleImageView);
            mMainImageView.setImageBitmap(null);
            mCurrentMainImage = null;
        }

        //////////////////////////Показ сообщений по возвращению со штрихкода///////////////////////////////
        CityCardFrame.setVisibility(mCurrentAd == null ? View.VISIBLE : View.INVISIBLE);
        mainFrame.setVisibility(mCurrentAd == null ? View.INVISIBLE : View.VISIBLE);
////////////////////////////////////////////////////////////////////////////////////////////////////

        mNoAdsFrame.setVisibility(mCurrentAd == null ? View.INVISIBLE : View.INVISIBLE);

        if (mCurrentAd != null) {
            final NetAdItem curItem = mCurrentAd;

            if (!mCurrentAd.isVideo() && (mRequestedMainImageUrl == null || mCurrentAd.image == null || !mRequestedMainImageUrl.equals(mCurrentAd.image))) {
                mRequestedMainImageUrl = mCurrentAd.image;
                String url = mRequestedMainImageUrl != null ? (mRequestedMainImageUrl.isEmpty() ? null : mRequestedMainImageUrl) : null;
                url = Constants.fixImageUrl(url);
                mImageLoaderCallback = new ImageLoaderCallback(curItem);
                Picasso.with(getActivity()).load(url).error(R.drawable.ad_main_image_placeholder).into(mImageLoaderCallback);
                if (url == null) {
                    mButtonsFrame.setVisibility(View.VISIBLE);
                    mReloadImageButton.setVisibility(View.INVISIBLE);
                }

                NetAdItem secondItem = AdList.getInstance().findBestSecondAdToShow();
                if (secondItem != null) {
                    String secondUrl = Constants.fixImageUrl(secondItem.image);
                    if (secondUrl != null) {
                        Picasso.with(getActivity()).load(secondUrl).into(mHiddenImage);
                    }
                }
            }

            if (mRequestedTitleImageUrl == null || mCurrentAd.logo == null || !mRequestedTitleImageUrl.equals(mCurrentAd.logo)) {
                mRequestedTitleImageUrl = mCurrentAd.logo;
                String url = mRequestedTitleImageUrl != null ? (mRequestedTitleImageUrl.isEmpty() ? null : mRequestedTitleImageUrl) : null;
                url = Constants.fixImageUrl(url);
                Picasso.with(getActivity()).load(url).into(mTitleImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        mRequestedTitleImageUrl = null;
                    }
                });
            }

            if (!mCurrentAd.isVideo()) {
                mVideoView.stopPlayback();
                mVideoView.setVisibility(View.INVISIBLE);
            } else {
                mVideoView.setVisibility(View.VISIBLE);
            }

            if (mCurrentAd.isVideo() && (mRequestedVideoUrl == null || mCurrentAd.image == null || !mRequestedVideoUrl.equals(mCurrentAd.image))) {
                mRequestedVideoUrl = mCurrentAd.image;
                mReloadImageButton.setVisibility(View.INVISIBLE);
                if (mRequestedVideoUrl != null) {
                    String url = Constants.fixImageUrl(mRequestedVideoUrl);
                    mVideoView.setVideoPath(url);
                    mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if (curItem != mCurrentAd)
                                return;
                            mButtonsFrame.setVisibility(View.VISIBLE);
                        }
                    });
                    mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            if (curItem != mCurrentAd)
                                return false;
                            mButtonsFrame.setVisibility(View.VISIBLE);
                            return false;
                        }
                    });
                    mVideoView.start();
                } else {
                    mVideoView.stopPlayback();
                    mButtonsFrame.setVisibility(View.VISIBLE);
                }
                mCurrentMainImage = null;
            }

            mTitleTextView.setText(mCurrentAd.name);
            mMainTextView.setText(mCurrentAd.description);

            if (mCurrentAd.flag_media == EAdFlagMedia.NEWS.get()) {
                mMainTextFrame.setVisibility(View.VISIBLE);
            } else {
                mMainTextFrame.setVisibility(View.GONE);
            }

            if (mCurrentAd.flag_media == EAdFlagMedia.AB.get()) {
                mGoodButtonImageView.setVisibility(View.INVISIBLE);
                mBadButtonImageView.setVisibility(View.INVISIBLE);
                mGoodButtonImageView2.setVisibility(View.VISIBLE);
                mBadButtonImageView2.setVisibility(View.VISIBLE);
            } else {
                mGoodButtonImageView.setVisibility(View.VISIBLE);
                mBadButtonImageView.setVisibility(View.VISIBLE);
                mGoodButtonImageView2.setVisibility(View.INVISIBLE);
                mBadButtonImageView2.setVisibility(View.INVISIBLE);
            }
        }
    }

    private class ImageLoaderCallback implements Target {
        private NetAdItem mCurItem;

        public ImageLoaderCallback(NetAdItem curItem) {
            mCurItem = curItem;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if (mCurItem != mCurrentAd)
                return;

            mCurrentMainImage = bitmap;

            if (mMainImageView.getWidth() == 0 || mCurrentAd.flag_media == EAdFlagMedia.NEWS.get()) {
                mMainImageView.setImageBitmap(bitmap);
            } else {
                int imgMaxHeight = (int)((float)bitmap.getWidth() * (float)mMainImageView.getHeight() / (float)mMainImageView.getWidth());
                if (bitmap.getHeight() < imgMaxHeight) {
                    int recalcHeight = (int)((float)bitmap.getHeight() * (float)mMainImageView.getWidth() / (float)bitmap.getWidth());
                    int d = (mMainImageView.getHeight() - recalcHeight) / 2;
                    Rect targetRect = new Rect(0, d, mMainImageView.getWidth(), recalcHeight + d);
                    Bitmap dest = Bitmap.createBitmap(mMainImageView.getWidth(), mMainImageView.getHeight(), bitmap.getConfig());
                    Canvas canvas = new Canvas(dest);
                    canvas.drawColor(0xFFFFFFFF);
                    canvas.drawBitmap(bitmap, null, targetRect, null);
                    mMainImageView.setImageBitmap(dest);
                } else if (bitmap.getHeight() > imgMaxHeight) {
                    int d = bitmap.getHeight() - imgMaxHeight;
                    Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, 0, d / 2, bitmap.getWidth(), imgMaxHeight);
                    mMainImageView.setImageBitmap(croppedBitmap);
                } else {
                    mMainImageView.setImageBitmap(bitmap);
                }
            }

            mButtonsFrame.setVisibility(View.VISIBLE);
            mReloadImageButton.setVisibility(View.INVISIBLE);

            System.gc();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            if (mCurItem != mCurrentAd)
                return;
            mRequestedMainImageUrl = null;
            mMainImageView.setImageBitmap(null);
            mCurrentMainImage = null;
            mReloadImageButton.setVisibility(View.VISIBLE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mCurItem != mCurrentAd)
                        return;
                    mButtonsFrame.setVisibility(View.VISIBLE);
                }
            }, 2000);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    }
}
