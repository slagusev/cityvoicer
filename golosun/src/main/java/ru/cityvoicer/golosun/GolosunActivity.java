package ru.cityvoicer.golosun;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.util.ArrayList;

import ru.cityvoicer.golosun.model.AdList;
import ru.cityvoicer.golosun.model.Cards;
import ru.cityvoicer.golosun.model.Profile;
import ru.cityvoicer.golosun.services.LocationService;
import ru.cityvoicer.golosun.services.RegistrationIntentService;

public class GolosunActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, Profile.IProfileChangesCallback {
    static public GolosunActivity gActivity;
    static public GolosunActivity gActiveActivity;

    private MainTopBar mTopBar;
    private DrawerLayout mDrawerLayout;
    private SideMenu mMenu;
    private ArrayList<PermissionedRequest> mPermissionedRequest = new ArrayList<>();

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gActivity = this;
        setContentView(R.layout.activity_city_voicer);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        mTopBar = (MainTopBar)findViewById(R.id.topbar);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mMenu = (SideMenu)findViewById(R.id.left_drawer);

        mMenu.init();
        mTopBar.init();

        if (!Constants.isReleaseBuild()) {
            UpdateManager.register(this, Constants.HockeyAppAppId);
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (Profile.getInstance().isRegistred()) {
                    showAdListScreen();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (AdList.getInstance().findBestAdToShow() == null) {
                                showAdListScreen();
                            }
                        }
                    });
                } else {
                    showRegistrationScreen();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mTopBar.deinit();
    }

    private boolean mPushTokenRequested;

    @Override
    public void onResume() {
        super.onResume();
        gActiveActivity = this;

        CrashManager.register(this, Constants.HockeyAppAppId);
        Profile.getInstance().activate();
        AdList.getInstance().activate();
        Profile.getInstance().addListener(this);
        mMenu.activate();
        Profile.getInstance().sendMac();
        LocationService.getInstance().start();
        Cards.getInstance().activate();

        if (!Constants.GcmSenderId.isEmpty() && checkPlayServices() && !Profile.getInstance().isPushTokenExist()) {
            if (!mPushTokenRequested) {
                mPushTokenRequested = true;
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }

        if (mPauseTime > 0 && System.currentTimeMillis() - mPauseTime > 1 * 60 * 1000 && Profile.getInstance().isRegistred()) {
            showAdListScreen();
        }
    }

    private long mPauseTime;

    @Override
    public void onPause() {
        super.onPause();
        if (gActiveActivity == this) {
            gActiveActivity = null;
        }
        UpdateManager.unregister();
        AdList.getInstance().deactivate();
        Profile.getInstance().deactivate();
        Profile.getInstance().removeListener(this);
        mMenu.deactivate();
        LocationService.getInstance().stop();
        Cards.getInstance().deactivate();

        mPauseTime = System.currentTimeMillis();
    }

    public void showAdListScreen() {
        Fragment fr = AdListFragment.newInstance();
        changeContentFragment(fr, false);
        createTopBarAndSetSideMenuMode(fr);
    }

    public void showRegistrationScreen() {
        Fragment fr = RegistrationFragment.newInstance();
        changeContentFragment(fr, false);
        createTopBarAndSetSideMenuMode(fr);
    }

    public void showGiveMoneyScreen() {
        if (mCurrentFragment instanceof GiveMoneyFragment)
            return;
        Fragment fr = GiveMoneyFragment.newInstance();
        changeContentFragment(fr, true);
        createTopBarAndSetSideMenuMode(fr);
    }

    public void showInfoScreen() {
        if (mCurrentFragment instanceof InfoFragment)
            return;
        Fragment fr = new InfoFragment();
        changeContentFragment(fr, true);
        createTopBarAndSetSideMenuMode(fr);
    }

    public void showInfoInitialScreen() {
        Fragment fr = new InfoInitialFragment();
        changeContentFragment(fr, false);
        createTopBarAndSetSideMenuMode(fr);
    }

    public void showTutorialScreen() {
        if (mCurrentFragment instanceof TutorialFragment && ((TutorialFragment)mCurrentFragment).getMode() == TutorialFragment.MODE_TUTORIAL)
            return;
        Fragment fr = TutorialFragment.newInstance(TutorialFragment.MODE_TUTORIAL);
        changeContentFragment(fr, true);
        createTopBarAndSetSideMenuMode(fr);
    }

    public void showInitialTutorialScreen() {
        if (mCurrentFragment instanceof TutorialFragment && ((TutorialFragment)mCurrentFragment).getMode() == TutorialFragment.MODE_INTRO)
            return;
        Fragment fr = TutorialFragment.newInstance(TutorialFragment.MODE_INTRO);
        changeContentFragment(fr, true);
        createTopBarAndSetSideMenuMode(fr);
    }

    public void showCardScreen() {
        if (mCurrentFragment instanceof CardFragment)
            return;
        Fragment fr = new CardFragment();
        changeContentFragment(fr, true);
        createTopBarAndSetSideMenuMode(fr);
    }

    public void showBarCodeScreen() {
        if (mCurrentFragment instanceof BarCodeFragment)
            return;
        Fragment fr = new BarCodeFragment();
        changeContentFragment(fr, true);
        createTopBarAndSetSideMenuMode(fr);
    }

    public void showImageListScreen() {
        if (mCurrentFragment instanceof ImageListFragment)
            return;
        Fragment fr = new ImageListFragment();
        changeContentFragment(fr, true);
        createTopBarAndSetSideMenuMode(fr);
    }

    @Override
    public void onChanged(Profile profile) {
    }

    @Override
    public void onBackStackChanged() {
        mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        createTopBarAndSetSideMenuMode(mCurrentFragment);
    }

    public void popFragment() {
        getSupportFragmentManager().popBackStack();
    }

    public void createTopBarAndSetSideMenuMode(Fragment fr) {
        if (fr instanceof AdListFragment) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mTopBar.setVisibility(View.VISIBLE);
            mTopBar.setBackButton(false);
        } else if (fr instanceof GiveMoneyFragment) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mTopBar.setVisibility(View.VISIBLE);
            mTopBar.setBackButton(true);
        } else if (fr instanceof InfoInitialFragment) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mTopBar.setVisibility(View.GONE);
        } else if (fr instanceof InfoFragment) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mTopBar.setVisibility(View.VISIBLE);
            mTopBar.setBackButton(true);
        } else if (fr instanceof TutorialFragment) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mTopBar.setVisibility(View.VISIBLE);
            mTopBar.setBackButton(true);
        } else if (fr instanceof CardFragment) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mTopBar.setVisibility(View.VISIBLE);
            mTopBar.setBackButton(true);
        } else if (fr instanceof BarCodeFragment) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mTopBar.setVisibility(View.VISIBLE);
            mTopBar.setBackButton(true);
        } else if (fr instanceof ImageListFragment) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mTopBar.setVisibility(View.VISIBLE);
            mTopBar.setBackButton(true);
        }  else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mTopBar.setVisibility(View.GONE);
        }
        lockSideMenuHack();
    }

    public void lockSideMenuHack() {
        if (!AdList.getInstance().isEmpty()) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    private Fragment mCurrentFragment;
    protected void changeContentFragment(Fragment fragment, boolean addToBackStack) {
        if (!addToBackStack) {
            for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
                getSupportFragmentManager().popBackStack();
            }
        }

        mCurrentFragment = fragment;

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            if (addToBackStack) {
                ft.addToBackStack(null);
            }
            ft.commit();
        }
    }
    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    public boolean isSideMenuExpanded() {
        return mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    public void expandSideMenu() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    public void closeSideMenu() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private Dialog mGoogleServicesErrorDialog = null;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                dismissGoogleErrorDialog();
                mGoogleServicesErrorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST);
                mGoogleServicesErrorDialog.show();
            } else {
                //Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void dismissGoogleErrorDialog() {
        if (mGoogleServicesErrorDialog != null) {
            mGoogleServicesErrorDialog.dismiss();
            mGoogleServicesErrorDialog = null;
        }
    }

    public void execute(PermissionedRequest req) {
        ArrayList<String> permList = new ArrayList();
        for (String perm : req.getPermissions()) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                permList.add(perm);
            }
        }
        if (permList.isEmpty()) {
            req.onPermissionGranted();
        } else {
            boolean needReq;
            if (req.getRetryPermissionFlag()) {
                needReq = true;
            } else {
                needReq = false;
                for (String perm : permList) {
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                        needReq = true;
                        break;
                    }
                }
            }
            if (needReq) {
                mPermissionedRequest.add(req);
                ActivityCompat.requestPermissions(this, req.getPermissions(), req.getCode());
            } else {
                req.onPermissionNotGranted();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        for (int i=0; i < mPermissionedRequest.size(); ) {
            PermissionedRequest req = mPermissionedRequest.get(i);
            if (req.getCode() == requestCode) {
                mPermissionedRequest.remove(i);
                if (granted) {
                    req.onPermissionGranted();
                } else {
                    req.onPermissionNotGranted();
                }
            } else {
                i ++;
            }
        }
    }

    public void startLocationService() {
        if (!LocationService.getInstance().isStarted()) {
            execute(new PermissionedRequest(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, true) {
                @Override
                void onPermissionGranted() {
                    LocationService.getInstance().start();
                }

                @Override
                void onCancel() {
                }

                @Override
                void onPermissionNotGranted() {
                }
            });
        }
    }
}
