package com.sample.foo.simplewidget;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.ogaclejapan.arclayout.ArcLayout;
import com.tjeannin.apprate.AppRate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import utility.Utils;

/**
 * Created by C.limbachiya on 4/26/2016.
 */
public class LauncherActivity extends AppCompatActivity implements View.OnClickListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private TextView lblLocation;
    ProgressBar pBar;
    LinearLayout innerLay;
    RelativeLayout layoutParent;
    View includedLayout = null;
    IntentFilter intentFilter = null;
    MyBroadcastReceiver broadcastReceiver = null;
    SharedPreferences pref = null;
    ImageView imgGetLocation;
    ClipRevealFrame colorMenuLayout, shareMenuLayout, commonMenuLayout, layoutAboutUs;
    ArcLayout colorArcLayout, shareArcLayout, commonArcLayout;
    View colorCenterItem, shareCenterItem, commonCenterItem;
    View rootLayout;

    private static Location mLastLocation;

    // Google client to interact with Google API
    private static GoogleApiClient mGoogleApiClient;
    FusedLocationProviderApi fusedLocationProviderApi;

    LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 60 sec
    private static int FATEST_INTERVAL = 5000; // 10 sec
    private static int SPLASH_TIME_OUT = 3000;
    protected PowerManager.WakeLock mWakeLock;
    AdView mAdView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.launcher_view);

        initUIControls();

        initObject();

        wakelockManage();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int density = metrics.densityDpi;

        if (density == DisplayMetrics.DENSITY_HIGH) {

            Toast.makeText(LauncherActivity.this, "Screen Density: HIGH", Toast.LENGTH_SHORT).show();
        } else if (density == DisplayMetrics.DENSITY_MEDIUM) {
            Toast.makeText(LauncherActivity.this, "Screen Density: MEDIUM", Toast.LENGTH_SHORT).show();
        } else if (density == DisplayMetrics.DENSITY_LOW) {
            Toast.makeText(LauncherActivity.this, "Screen Density: LOW", Toast.LENGTH_SHORT).show();
        } else if (density == DisplayMetrics.DENSITY_XHIGH) {
            Toast.makeText(LauncherActivity.this, "Screen Density: XHIGH", Toast.LENGTH_SHORT).show();

        } else if (density == DisplayMetrics.DENSITY_XXHIGH) {
            Toast.makeText(LauncherActivity.this, "Screen Density: XXHIGH", Toast.LENGTH_SHORT).show();
            new AppRate(this)
                    .setMinDaysUntilPrompt(0)
                    .setMinLaunchesUntilPrompt(20)
                    .setShowIfAppHasCrashed(false)
                    .init();
        } else {
            Toast.makeText(LauncherActivity.this, "Screen Density: UNKNOWN_CATEGORY", Toast.LENGTH_SHORT).show();
        }


        rootLayout = findViewById(R.id.root_layout);
        colorMenuLayout = (ClipRevealFrame) findViewById(R.id.color_menu_layout);
        colorArcLayout = (ArcLayout) findViewById(R.id.color_arc_layout);
        colorCenterItem = findViewById(R.id.color_center_item);

        shareMenuLayout = (ClipRevealFrame) findViewById(R.id.share_menu_layout);
        shareArcLayout = (ArcLayout) findViewById(R.id.share_arc_layout);
        shareCenterItem = findViewById(R.id.share_center_item);

        colorCenterItem.setOnClickListener(this);
        shareCenterItem.setOnClickListener(this);

        colorCenterItem.setId(0);
        shareCenterItem.setId(0);

        for (int i = 0, size = colorArcLayout.getChildCount(); i < size; i++) {
            colorArcLayout.getChildAt(i).setId(i + 1);
            colorArcLayout.getChildAt(i).setOnClickListener(this);
        }

        for (int i = 0, size = shareArcLayout.getChildCount(); i < size; i++) {
            shareArcLayout.getChildAt(i).setId(i + 1);
            shareArcLayout.getChildAt(i).setOnClickListener(this);
        }

        findViewById(R.id.fab_color).setOnClickListener(this);
        findViewById(R.id.fab_share).setOnClickListener(this);
        findViewById(R.id.fab_about_us).setOnClickListener(this);
        findViewById(R.id.action_location).setOnClickListener(this);
    }

    //Load addMob add
    private void loadAdd() {

        boolean isNetWork = Utils.isNetworkAvailable(this);
        if (isNetWork) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

    }

    private void wakelockManage() {

        /* This code together with the one in onDestroy()
         * will make the screen be always on until this Activity gets destroyed. */
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAdd();
        checkingForPermission();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    //Check for permission
    private void checkingForPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int writeResult = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            int readResult = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (writeResult == PackageManager.PERMISSION_GRANTED && readResult == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(LauncherActivity.this, "Now granted", Toast.LENGTH_SHORT).show();

                if(checkPlayServices(this))
                    createLocationRequest();

            } else {
                Intent intent = new Intent(this, PermissionActivity.class);
                startActivity(intent);
            }
        }else{

            if(checkPlayServices(this))
                createLocationRequest();
        }

    }

    public void createLocationRequest() {
        //Toast.makeText(getApplicationContext(), "createLocationRequest",Toast.LENGTH_SHORT).show();

        pBar.setVisibility(View.VISIBLE);
        boolean isNetWork = Utils.isNetworkAvailable(this);
        if (isNetWork) {

            if (Utils.isGPSOn(this)) {
                mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(UPDATE_INTERVAL);
                mLocationRequest.setFastestInterval(FATEST_INTERVAL);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                fusedLocationProviderApi = LocationServices.FusedLocationApi;
                mGoogleApiClient = new GoogleApiClient.Builder(LauncherActivity.this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(LauncherActivity.this)
                        .addOnConnectionFailedListener(LauncherActivity.this)
                        .build();
                if (mGoogleApiClient != null) {
                    Toast.makeText(getApplicationContext(), "Get Client", Toast.LENGTH_SHORT).show();
                    mGoogleApiClient.connect();
                }
            }else{ //GPS is OFF
                lblLocation.setText("Please ON your GPS first");
                pBar.setVisibility(View.INVISIBLE);
            }
        }else{ //No internet connection
            pBar.setVisibility(View.INVISIBLE);
            lblLocation.setText("Unable to get location.Make sure your internet connection is on");
        }

    }

    //Init all custom objects
    private void initObject() {

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.sample.foo.simplewidget.DYNAMIC_BROADCAST");
        broadcastReceiver = new MyBroadcastReceiver();

        int position = pref.getInt("COLOR", 1); // getting color position and change selected color of widget
        changeWidgetColor(position);
    }

    //Initialize UI controls
    private void initUIControls() {
        //innerLay = (LinearLayout) findViewById(R.id.innerLay);
        includedLayout = findViewById(R.id.include_widget);
        layoutParent = (RelativeLayout) includedLayout.findViewById(R.id.relative_parent);
        lblLocation = (TextView) includedLayout.findViewById(R.id.text_detail);
        pBar = (ProgressBar) includedLayout.findViewById(R.id.progressBar);
        layoutAboutUs = (ClipRevealFrame) findViewById(R.id.layout_about_us);
        mAdView = (AdView) findViewById(R.id.addView);
    }

    private void changeWidgetColor(int position) {

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("COLOR", position);  //storing color position
        editor.commit(); // commit changes
        editor.clear();

        if (position == 0) {
            Drawable currentBG = layoutParent.getBackground();
            Drawable newBG = ContextCompat.getDrawable(this, R.drawable.youtube_horizontal_gradient);
            startTransition(currentBG, newBG);
        } else if (position == 1) {
            Drawable currentBG = layoutParent.getBackground();
            Drawable newBG = ContextCompat.getDrawable(this, R.drawable.default_horizontal_gradient);
            startTransition(currentBG, newBG);
        } else if (position == 2) {
            Drawable currentBG = layoutParent.getBackground();
            Drawable newBG = ContextCompat.getDrawable(this, R.drawable.facebook_horizontal_gradient);
            startTransition(currentBG, newBG);
        } else if (position == 3) {
            Drawable currentBG = layoutParent.getBackground();
            Drawable newBG = ContextCompat.getDrawable(this, R.drawable.stumbledupon_horizontal_gradient);
            startTransition(currentBG, newBG);
        } else if (position == 4) {
            Drawable currentBG = layoutParent.getBackground();
            Drawable newBG = ContextCompat.getDrawable(this, R.drawable.yahoo_horizontal_gradient);
            startTransition(currentBG, newBG);
        } else if (position == 5) {
            Drawable currentBG = layoutParent.getBackground();
            Drawable newBG = ContextCompat.getDrawable(this, R.drawable.twitter_horizontal_gradient);
            startTransition(currentBG, newBG);
        } else if (position == 6) {
            Drawable currentBG = layoutParent.getBackground();
            Drawable newBG = ContextCompat.getDrawable(this, R.drawable.vine_horizontal_gradient);
            startTransition(currentBG, newBG);
        } else if (position == 7) {
            Drawable currentBG = layoutParent.getBackground();
            Drawable newBG = ContextCompat.getDrawable(this, R.drawable.snapchat_horizontal_gradient);
            startTransition(currentBG, newBG);
        } else if (position == 8) {
            Drawable currentBG = layoutParent.getBackground();
            Drawable newBG = ContextCompat.getDrawable(this, R.drawable.foursqure_horizontal_gradient);
            startTransition(currentBG, newBG);
        } else if (position == 9) {
            Drawable currentBG = layoutParent.getBackground();
            Drawable newBG = ContextCompat.getDrawable(this, R.drawable.whesphalt_horizontal_gradient);
            startTransition(currentBG, newBG);
        } else if (position == 10) {
            Drawable currentBG = layoutParent.getBackground();
            Drawable newBG = ContextCompat.getDrawable(this, R.drawable.whisteria_horizontal_gradient);
            startTransition(currentBG, newBG);
        } else if (position == 11) {
            Drawable currentBG = layoutParent.getBackground();
            Drawable newBG = ContextCompat.getDrawable(this, R.drawable.orange_horizontal_gradient);
            startTransition(currentBG, newBG);
        }
    }

    private void startTransition(Drawable currentBG, Drawable newBG) {

        TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{currentBG, newBG});
        transitionDrawable.setCrossFadeEnabled(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            layoutParent.setBackgroundDrawable(transitionDrawable);
        } else {
            layoutParent.setBackground(transitionDrawable);
        }
        transitionDrawable.startTransition(400);

        Intent intent = new Intent();
        intent.setAction("com.sample.foo.simplewidget.DYNAMIC_BROADCAST");
        intent.putExtra("BROADCAST_TYPE", "Dynamic");
        sendBroadcast(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (null != broadcastReceiver)
            unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_color) {
            onFabClick(v, "color");
            return;
        } else if (v.getId() == R.id.fab_share) {
            onFabClick(v, "share");
            return;
        } else if (v.getId() == R.id.fab_about_us) {
            onFabAboutClick(v);
            return;
        }else if(v.getId() == R.id.action_location){
            createLocationRequest();
            return;
        }else if (v instanceof Button) {
            colorChanger((Button) v);
        } else if (v instanceof ImageButton) {
            shareAppVia((ImageButton) v);
        }
    }

    private void shareAppVia(ImageButton v) {

        int position = v.getId();

        if (position == 0) { //EMail
            shareViaGmail();

        } else if (position == 1) { //Facebook
            Toast.makeText(LauncherActivity.this, "Facebook coming soon...", Toast.LENGTH_SHORT).show();
        }
        if (position == 2) { //Twitter
            Toast.makeText(LauncherActivity.this, "Twitter coming soon..." + position, Toast.LENGTH_SHORT).show();
        }
        if (position == 3) { //Whatsapp
            shareViaWhatsApp();
        }
    }

    private void shareViaGmail() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "your_email@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Suggetions");
            startActivity(intent);

            /*Intent shareIntent = new PlusShare.Builder(this)
                    .setType("text/plain")
                    .setText("This is widget app")
                    .setContentUrl(Uri.parse("https://developers.google.com/+/"))
                    .getIntent();

            startActivityForResult(shareIntent, 0);*/
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(LauncherActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareViaWhatsApp() {
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp");
            startActivity(sendIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(LauncherActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    //enable/disable addview click event
    public void addViewEnable(boolean isClickEnable){

        if(isClickEnable){
            mAdView.setVisibility(View.VISIBLE);
        }else{
            mAdView.setVisibility(View.GONE);
        }
    }
    private void colorChanger(Button btn) {

        int position = btn.getId();
        //int position = Integer.parseInt(btn.getText().toString());
        changeWidgetColor(position);
    }

    private void onFabAboutClick(View v) {
        int x = (v.getLeft() + v.getRight()) / 2;
        int y = (v.getTop() + v.getBottom()) / 2;
        float radiusOfFab = 1f * v.getWidth() / 2f;
        float radiusFromFabToRoot = (float) Math.hypot(
                Math.max(x, rootLayout.getWidth() - x),
                Math.max(y, rootLayout.getHeight() - y));

        if (v.isSelected()) {
            addViewEnable(true);
            hideAboutMenu(x, y, radiusFromFabToRoot, radiusOfFab);
        } else {
            addViewEnable(false);
            showAboutMenu(x, y, radiusOfFab, radiusFromFabToRoot);
        }
        v.setSelected(!v.isSelected());
    }

    private void onFabClick(View v, String action) {
        int x = (v.getLeft() + v.getRight()) / 2;
        int y = (v.getTop() + v.getBottom()) / 2;
        float radiusOfFab = 1f * v.getWidth() / 2f;
        float radiusFromFabToRoot = (float) Math.hypot(
                Math.max(x, rootLayout.getWidth() - x),
                Math.max(y, rootLayout.getHeight() - y));

        if (v.isSelected()) {
            addViewEnable(true);
            hideMenu(x, y, radiusFromFabToRoot, radiusOfFab, action);
        } else {
            addViewEnable(false);
            showMenu(x, y, radiusOfFab, radiusFromFabToRoot, action);
        }
        v.setSelected(!v.isSelected());
    }

    private void showAboutMenu(int cx, int cy, float startRadius, float endRadius) {
        layoutAboutUs.setVisibility(View.VISIBLE);
        List<Animator> animList = new ArrayList<>();

        Animator revealAnim = createCircularReveal(layoutAboutUs, cx, cy, startRadius, endRadius);
        revealAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        revealAnim.setDuration(200);
        revealAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                addViewEnable(false);
                //layoutAboutUs.setVisibility(View.VISIBLE);

                /*Animation textAnim = new TranslateAnimation(0,20,0,0);
                textAnim.setDuration(5000);
                textAnim.setRepeatMode(Animation.REVERSE);
                textAnim.setRepeatCount(Animation.INFINITE);
                findViewById(R.id.text_about_info).startAnimation(textAnim);*/

                /*final TextView txtInfo = (TextView) findViewById(R.id.text_about_info);
                final TextView txtInfo2 = (TextView) findViewById(R.id.text_about_info2);
                final TextView txtInfo3 = (TextView) findViewById(R.id.text_about_info3);
                final TextView txtInfo4 = (TextView) findViewById(R.id.text_about_info4);

                final float startSize = 21;
                final float endSize = 15;
                final int animationDuration = 600; // Animation duration in ms

                ValueAnimator animator = ValueAnimator.ofFloat(startSize, endSize);
                animator.setDuration(animationDuration);

                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float animatedValue = (float) valueAnimator.getAnimatedValue();
                        txtInfo.setTextSize(animatedValue);
                        txtInfo2.setTextSize(animatedValue);
                        txtInfo3.setTextSize(animatedValue);
                        txtInfo4.setTextSize(animatedValue);
                    }
                });

                animator.start();*/
            }
        });
        animList.add(revealAnim);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playSequentially(animList);
        animSet.start();
    }

    private void hideAboutMenu(int cx, int cy, float startRadius, float endRadius) {
        List<Animator> animList = new ArrayList<>();

        Animator revealAnim = createCircularReveal(layoutAboutUs, cx, cy, startRadius, endRadius);
        revealAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        revealAnim.setDuration(200);
        revealAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                layoutAboutUs.setVisibility(View.INVISIBLE);
                addViewEnable(true);
            }
        });

        animList.add(revealAnim);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playSequentially(animList);
        animSet.start();

    }

    private void showMenu(int cx, int cy, float startRadius, float endRadius, String action) {

        if (action.equals("color")) {
            commonArcLayout = colorArcLayout;
            commonCenterItem = colorCenterItem;
            commonMenuLayout = colorMenuLayout;
        } else {
            commonArcLayout = shareArcLayout;
            commonCenterItem = shareCenterItem;
            commonMenuLayout = shareMenuLayout;
        }

        commonMenuLayout.setVisibility(View.VISIBLE);

        List<Animator> animList = new ArrayList<>();

        Animator revealAnim = createCircularReveal(commonMenuLayout, cx, cy, startRadius, endRadius);
        revealAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        revealAnim.setDuration(200);

        animList.add(revealAnim);
        animList.add(createShowItemAnimator(commonCenterItem));

        for (int i = 0, len = commonArcLayout.getChildCount(); i < len; i++) {
            animList.add(createShowItemAnimator(commonArcLayout.getChildAt(i)));
        }

        AnimatorSet animSet = new AnimatorSet();
        animSet.playSequentially(animList);
        animSet.start();
    }

    private void hideMenu(int cx, int cy, float startRadius, float endRadius, String action) {
        List<Animator> animList = new ArrayList<>();

        if (action.equals("color")) {
            commonArcLayout = colorArcLayout;
            commonCenterItem = colorCenterItem;
            commonMenuLayout = colorMenuLayout;
        } else {
            commonArcLayout = shareArcLayout;
            commonCenterItem = shareCenterItem;
            commonMenuLayout = shareMenuLayout;
        }

        for (int i = commonArcLayout.getChildCount() - 1; i >= 0; i--) {
            animList.add(createHideItemAnimator(commonArcLayout.getChildAt(i)));
        }

        animList.add(createHideItemAnimator(commonCenterItem));

        Animator revealAnim = createCircularReveal(commonMenuLayout, cx, cy, startRadius, endRadius);
        revealAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        revealAnim.setDuration(200);
        revealAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                commonMenuLayout.setVisibility(View.INVISIBLE);
            }
        });

        animList.add(revealAnim);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playSequentially(animList);
        animSet.start();

    }

    private Animator createShowItemAnimator(View item) {
        float dx = commonCenterItem.getX() - item.getX();
        float dy = commonCenterItem.getY() - item.getY();

        item.setScaleX(0f);
        item.setScaleY(0f);
        item.setTranslationX(dx);
        item.setTranslationY(dy);

        Animator anim = ObjectAnimator.ofPropertyValuesHolder(
                item,
                AnimatorUtils.scaleX(0f, 1f),
                AnimatorUtils.scaleY(0f, 1f),
                AnimatorUtils.translationX(dx, 0f),
                AnimatorUtils.translationY(dy, 0f)
        );

        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(50);
        return anim;
    }

    private Animator createHideItemAnimator(final View item) {
        final float dx = commonCenterItem.getX() - item.getX();
        final float dy = commonCenterItem.getY() - item.getY();

        Animator anim = ObjectAnimator.ofPropertyValuesHolder(
                item,
                AnimatorUtils.scaleX(1f, 0f),
                AnimatorUtils.scaleY(1f, 0f),
                AnimatorUtils.translationX(0f, dx),
                AnimatorUtils.translationY(0f, dy)
        );

        anim.setInterpolator(new DecelerateInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                item.setTranslationX(0f);
                item.setTranslationY(0f);
            }
        });
        anim.setDuration(50);
        return anim;
    }

    private Animator createCircularReveal(final ClipRevealFrame view, int x, int y, float startRadius,
                                          float endRadius) {
        final Animator reveal;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            reveal = ViewAnimationUtils.createCircularReveal(view, x, y, startRadius, endRadius);
        } else {
            view.setClipOutLines(true);
            view.setClipCenter(x, y);
            reveal = ObjectAnimator.ofFloat(view, "ClipRadius", startRadius, endRadius);
            reveal.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setClipOutLines(false);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        return reveal;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        commonMenuLayout = null;
        commonArcLayout = null;
        commonCenterItem = null;

        if (null != mWakeLock)
            this.mWakeLock.release();
    }

    @Override
    protected void onStop() {
        super.onStop();

        closeFusedLocation();
    }

    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getApplicationContext(), "onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            //Toast.makeText(getApplicationContext(), "latitude : "+latitude+" longitude : "+longitude, Toast.LENGTH_SHORT).show();

            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (IOException e) {
                e.printStackTrace();
            }

            String address = null;
            String city = null;
            String state = null;
            String country = null;
            //String postalCode = null;
            //String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

            if (null != addresses) {

                if (null == addresses.get(0).getAddressLine(0)) {
                    address = "unknown";
                } else {
                    address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                }

                if (null == addresses.get(0).getLocality()) {
                    city = "unknown";
                } else {
                    city = addresses.get(0).getLocality();
                }

                if (null == addresses.get(0).getAdminArea()) {
                    state = "unknown";
                } else {
                    state = addresses.get(0).getAdminArea();
                }

                if (null == addresses.get(0).getCountryName()) {
                    country = "unknown";
                } else {
                    country = addresses.get(0).getCountryName();
                }

                pBar.setVisibility(View.INVISIBLE);
                lblLocation.setVisibility(View.VISIBLE);

                lblLocation.setText(
                        address + " > " +
                                city + " > " +
                                state + " > " +
                                country);



            } else {
                pBar.setVisibility(View.INVISIBLE);
                lblLocation.setVisibility(View.VISIBLE);
                lblLocation.setText("Your internet connection is too slow! Please Retry");
            }

        } else {
            pBar.setVisibility(View.INVISIBLE);
            lblLocation.setVisibility(View.VISIBLE);
            lblLocation.setText("Couldn't get the location");
            Toast.makeText(getApplicationContext(), "Couldn't get the location", Toast.LENGTH_SHORT).show();
        }

        closeFusedLocation();

    }

    public void closeFusedLocation(){
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            if (mGoogleApiClient.isConnected())
                mGoogleApiClient.disconnect();
        }
    }

    /**
     * Method to verify google play services on the device
     *
     * @param context
     */
    private boolean checkPlayServices(Context context) {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                /*GooglePlayServicesUtil.getErrorDialog(resultCode, context,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();*/

                Toast.makeText(context,
                        "Google Play Service Error : " + PLAY_SERVICES_RESOLUTION_REQUEST, Toast.LENGTH_LONG)
                        .show();

            } else {
                Toast.makeText(context,
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();

            }
            return false;
        }
        return true;
    }
}