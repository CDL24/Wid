package com.sample.foo.simplewidget;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import classes.MyTimer;
import classes.RepeatingAlarm;
import classes.UpdateIntentService;


public class SimpleWidgetProvider extends AppWidgetProvider {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private static Location mLastLocation;

    // Google client to interact with Google API
    private static GoogleApiClient mGoogleApiClient;
    static FusedLocationProviderApi fusedLocationProviderApi;

    static LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 60000; // 60 sec
    private static int FATEST_INTERVAL = 10000; // 10 sec
    private static final String LOCATION_CLICKED = "LocationButtonClick";
    private static final String SHARE_CLICKED = "ShareButtonClick";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.simple_widget);
        Toast.makeText(context, "onEnable", Toast.LENGTH_SHORT).show();
        if(null != updateViews){
            updateViews = prepareWidgetLayout(updateViews, context);

            ComponentName thisWidget = new ComponentName(context, SimpleWidgetProvider.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(context);

            MyTimer myTimer = new MyTimer(context);
            myTimer.runAndUpdateTheWidget();

            manager.updateAppWidget(thisWidget, updateViews);
        }
    }
    public RemoteViews prepareWidgetLayout(RemoteViews updateViews, Context context) {

        SharedPreferences pref = context.getSharedPreferences("MyPref", context.MODE_PRIVATE);
        int position = pref.getInt("COLOR", 0); // getting color position

        if(position == 0){

            updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.youtube_horizontal_gradient);
        }else if(position == 1){

            updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.default_horizontal_gradient);
        }else if(position == 2){

            updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.facebook_horizontal_gradient);
        }else if(position == 3){

            updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.stumbledupon_horizontal_gradient);
        }else if(position == 4){

            updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.yahoo_horizontal_gradient);
        }else if(position == 5){

            updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.twitter_horizontal_gradient);
        }else if(position == 6){

            updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.vine_horizontal_gradient);
        }else if(position == 7){

            updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.snapchat_horizontal_gradient);
        }else if(position == 8){

            updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.foursqure_horizontal_gradient);
        }else if(position == 9){

            updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.whesphalt_horizontal_gradient);
        }else if(position == 10){

            updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.whisteria_horizontal_gradient);
        }else if(position == 11){

            updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.orange_horizontal_gradient);
        }
        return updateViews;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;

        Toast.makeText(context, "onUpdate", Toast.LENGTH_SHORT).show();

        RemoteViews remoteViews;
        ComponentName watchWidget;

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.simple_widget);
        watchWidget = new ComponentName(context, SimpleWidgetProvider.class);


        Intent intent = new Intent(context, RepeatingAlarm.class);
        PendingIntent sender = PendingIntent
                .getBroadcast(context, 0, intent, 0);

        // We want the alarm to go off 1 seconds from now.
        long firstTime = SystemClock.elapsedRealtime();
        //firstTime += 1000;

        // Schedule the alarm!
        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 60000,
                sender);

        remoteViews = prepareWidgetLayout(remoteViews, context);

        remoteViews.setOnClickPendingIntent(R.id.action_location, getPendingSelfIntent(context, LOCATION_CLICKED));
        remoteViews.setOnClickPendingIntent(R.id.action_share, getPendingSelfIntent(context, SHARE_CLICKED));
        appWidgetManager.updateAppWidget(watchWidget, remoteViews);

    }

    /*public static class UpdateService extends Service implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onCreate() {
            super.onCreate();

            createLocationRequest();

            fusedLocationProviderApi = LocationServices.FusedLocationApi;
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            if (mGoogleApiClient != null) {
                //Toast.makeText(getApplicationContext(), "Get Client", Toast.LENGTH_SHORT).show();
                mGoogleApiClient.connect();
            }

        }

        *//**
         * Method to verify google play services on the device
         *
         * @param context
         *//*
        private boolean checkPlayServices(Context context) {
            int resultCode = GooglePlayServicesUtil
                    .isGooglePlayServicesAvailable(context);
            if (resultCode != ConnectionResult.SUCCESS) {
                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                *//*GooglePlayServicesUtil.getErrorDialog(resultCode, context,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();*//*

                    Toast.makeText(context,
                            "Error." + PLAY_SERVICES_RESOLUTION_REQUEST, Toast.LENGTH_LONG)
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

        public void createLocationRequest() {
            //Toast.makeText(getApplicationContext(), "createLocationRequest",Toast.LENGTH_SHORT).show();

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FATEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Toast.makeText(getBaseContext(),"onStart : Service", Toast.LENGTH_LONG).show();
            // Build the widget update for today
            RemoteViews updateViews = buildUpdate(this);

            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName(this, SimpleWidgetProvider.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);
            return START_REDELIVER_INTENT;
        }

        // In service start method, I am registering for GPS Updates
        *//*@Override
        public void onStart(Intent intent, int startId) {

            Toast.makeText(getBaseContext(),"onStart : Service", Toast.LENGTH_LONG).show();
            // Build the widget update for today
            RemoteViews updateViews = buildUpdate(this);

            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName(this, SimpleWidgetProvider.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);

        }*//*

        public RemoteViews buildUpdate(Context context) {
            // Here I am updating the remoteview



            RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.simple_widget);
            //prepareWidgetLayout(updateViews, context);

            *//*SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            int position = pref.getInt("COLOR", 0); // getting color position

            if(position == 0){

                updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.youtube_horizontal_gradient);
            }else if(position == 1){

                updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.default_horizontal_gradient);
            }else if(position == 2){

                updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.facebook_horizontal_gradient);
            }else if(position == 3){

                updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.stumbledupon_horizontal_gradient);
            }else if(position == 4){

                updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.yahoo_horizontal_gradient);
            }else if(position == 5){

                updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.twitter_horizontal_gradient);
            }else if(position == 6){

                updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.vine_horizontal_gradient);
            }else if(position == 7){

                updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.snapchat_horizontal_gradient);
            }else if(position == 8){

                updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.foursqure_horizontal_gradient);
            }else if(position == 9){

                updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.whesphalt_horizontal_gradient);
            }else if(position == 10){

                updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.whisteria_horizontal_gradient);
            }else if(position == 11){

                updateViews.setInt(R.id.parent_layout,"setBackgroundResource", R.drawable.orange_horizontal_gradient);
            }*//*


            return prepareWidgetLayout(updateViews, context);
        }

        @Override
        public IBinder onBind(Intent intent) {
            // We don't need to bind to this service
            return null;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            if (null != mGoogleApiClient) {

                //Toast.makeText(getApplicationContext(),"Stop Service",Toast.LENGTH_SHORT).show();
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        mGoogleApiClient, this);
                mGoogleApiClient.disconnect();


            }
        }

        @Override
        public void onConnected(Bundle bundle) {
            //startLocationUpdates();
            //Toast.makeText(getApplicationContext(),"onConnected Service",Toast.LENGTH_SHORT).show();
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

            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.simple_widget);
            remoteViews.setViewVisibility(R.id.progressBar,
                    View.INVISIBLE);
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Toast.makeText(getApplicationContext(), "onConnectionFailed", Toast.LENGTH_SHORT).show();

            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.simple_widget);
            remoteViews.setViewVisibility(R.id.progressBar,
                    View.INVISIBLE);
        }

        @Override
        public void onLocationChanged(Location location) {
            *//*Location mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);*//*
            mLastLocation = location;
            if (mLastLocation != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

                RemoteViews remoteViews;
                ComponentName watchWidget;

                remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.simple_widget);
                watchWidget = new ComponentName(getApplicationContext(), SimpleWidgetProvider.class);

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

                    //remoteViews.setViewVisibility(R.id.ball_view, View.GONE);

                    remoteViews.setTextViewText(R.id.text_detail,
                            Html.fromHtml("<h2><u>Your are at</u><br/>")
                            + "Address : " + address + "\n" +
                                    "City : " + city + "\n" +
                                    "State : " + state + "\n" +
                                    "Country : " + country);

                } else {

                    //remoteViews.setViewVisibility(R.id.ball_view, View.GONE);
                    remoteViews.setTextViewText(R.id.text_detail, "Your internet connection is too slow! Please Retry");
                }


                //remoteViews = new RemoteViews(getPackageName(), R.layout.simple_widget);
                remoteViews.setViewVisibility(R.id.progressBar,
                        View.INVISIBLE);

                appWidgetManager.updateAppWidget(watchWidget, remoteViews);


                stopSelf();

            } else {

                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.simple_widget);
                remoteViews.setViewVisibility(R.id.progressBar,
                        View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Couldn't get the location", Toast.LENGTH_SHORT).show();
            }
        }

    }*/

    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO Auto-generated method stub
        super.onReceive(context, intent);

        if (LOCATION_CLICKED.equals(intent.getAction())) {

            boolean isNetWork = isNetworkAvailable(context);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            RemoteViews remoteViews;
            ComponentName watchWidget;

            remoteViews = new RemoteViews(context.getPackageName(), R.layout.simple_widget);
            watchWidget = new ComponentName(context, SimpleWidgetProvider.class);


            if (isNetWork) {

                if (isGPSOn(context)) {

                    //remoteViews.setViewVisibility(R.id.ball_view, View.VISIBLE);
                    //Yes, GPS is ON

                    remoteViews.setViewVisibility(R.id.progressBar,
                            View.VISIBLE);
                    //context.startService(new Intent(context, WidgetUpdateService.class));
                    //Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DownloadService.class);

                    /* Send optional extras to Download IntentService */
                    /*intent.putExtra("url", url);
                    intent.putExtra("receiver", mReceiver);
                    intent.putExtra("requestId", 101);*/

                    context.startService(new Intent(context, UpdateIntentService.class));

                    remoteViews.setTextViewText(R.id.text_detail, "Fetching Location...");

                } else {
                    //remoteViews.setViewVisibility(R.id.ball_view, View.GONE);
                    //NO, GPS is OFF
                    remoteViews.setTextViewText(R.id.text_detail, "Please ON your GPS first");
                }


            } else {

                remoteViews.setTextViewText(R.id.text_detail, "Unable to get location.Make sure your internet connection is on");
            }
            appWidgetManager.updateAppWidget(watchWidget, remoteViews);
        } else if (SHARE_CLICKED.equals(intent.getAction())) {

            /*Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            viewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(viewIntent);*/

            /*Intent i = new Intent(android.content.Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject test");
            i.putExtra(android.content.Intent.EXTRA_TEXT, "extra text that you want to put");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(i, "Share via"));*/

            //String url = "http://www.google.com";
            //Intent i = new Intent(Intent.ACTION_SEND);
            //i.setData(Uri.parse(url));
            //i.setType("text/plain");
            //i.putExtra(Intent.EXTRA_SUBJECT, "Subject test");
            //i.putExtra(Intent.EXTRA_TEXT, "extra text that you want to put");
            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //context.startActivity(Intent.createChooser(i, "Share via"));

            /*String email = "test@gmail.com";
            Intent intSend = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null));
            intSend.putExtra(android.content.Intent.EXTRA_SUBJECT, "Test Subject");
            intSend.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(intent, "Send mail..."));*/

            /*Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image*//*");
            Intent chooser = Intent.createChooser(intent, "Choose a Picture");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);*/

            Intent popUpIntent = new Intent(context, LauncherActivity.class);
            popUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(popUpIntent);

        }
    }

    private boolean isGPSOn(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isNetworkAvailable(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    /**
     * Method to verify google play services on the device
     *
     * @param context
     *//*
    private boolean checkPlayServices(Context context) {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                *//*GooglePlayServicesUtil.getErrorDialog(resultCode, context,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();*//*

                Toast.makeText(context,
                        "Error." + PLAY_SERVICES_RESOLUTION_REQUEST, Toast.LENGTH_LONG)
                        .show();

            } else {
                Toast.makeText(context,
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();

            }
            return false;
        }
        return true;
    }*/
    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}
