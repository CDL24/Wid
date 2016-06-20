package com.sample.foo.simplewidget;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.os.Handler;

import java.util.logging.LogRecord;

import loadingball.factory.BallView;
import utility.Utils;

/**
 * Created by C.limbachiya on 1/1/2016.
 */
public class ShareActivity extends Activity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // UI elements
    private TextView lblLocation;
    private BallView bbl;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

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

    Typeface typefaceRoboto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.sharing_view);

        /* This code together with the one in onDestroy()
         * will make the screen be always on until this Activity gets destroyed. */
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();

        bbl = (BallView) findViewById(R.id.ball_view);
        lblLocation = (TextView) findViewById(R.id.lblLocation);

        typefaceRoboto = Typeface.createFromAsset(getAssets(), "font/Roboto-Light.ttf");

        lblLocation.setVisibility(View.GONE);

        lblLocation.setTypeface(typefaceRoboto);

        if (Utils.isNetworkAvailable(this)) {

            if (Utils.isGPSOn(this)) {

                //Yes, GPS is ON
                //context.startService(new Intent(context, UpdateService.class));
                //remoteViews.setTextViewText(R.id.text_detail, "Fetching Location...");

            } else {
                //NO, GPS is OFF
                //remoteViews.setTextViewText(R.id.text_detail, "Please ON your GPS first");
            }


        } else {

            //remoteViews.setTextViewText(R.id.text_detail, "Unable to get location.Make sure your internet connection is on");

        }

        new Handler().postDelayed(new Runnable() {


            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity

                bbl.setVisibility(View.VISIBLE);

                createLocationRequest();

                fusedLocationProviderApi = LocationServices.FusedLocationApi;
                mGoogleApiClient = new GoogleApiClient.Builder(ShareActivity.this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(ShareActivity.this)
                        .addOnConnectionFailedListener(ShareActivity.this)
                        .build();
                if (mGoogleApiClient != null) {
                    //Toast.makeText(getApplicationContext(), "Get Client", Toast.LENGTH_SHORT).show();
                    mGoogleApiClient.connect();
                }
            }
        }, SPLASH_TIME_OUT);

        /*Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);*/


    }

    public void createLocationRequest() {
        //Toast.makeText(getApplicationContext(), "createLocationRequest",Toast.LENGTH_SHORT).show();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != mWakeLock)
            this.mWakeLock.release();

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (null != mGoogleApiClient) {

            //Toast.makeText(getApplicationContext(),"Stop Service",Toast.LENGTH_SHORT).show();
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);

            if (mGoogleApiClient.isConnected())
                mGoogleApiClient.disconnect();

            //Toast.makeText(ShareActivity.this,"onStop : All Close",Toast.LENGTH_SHORT).show();
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

                bbl.setVisibility(View.INVISIBLE);
                lblLocation.setVisibility(View.VISIBLE);

                lblLocation.setText(
                        "Address : " + address + "\n" +
                                "City : " + city + "\n" +
                                "State : " + state + "\n" +
                                "Country : " + country);

            } else {
                bbl.setVisibility(View.INVISIBLE);
                lblLocation.setVisibility(View.VISIBLE);
                lblLocation.setText("Your internet connection is too slow! Please Retry");
            }

        } else {
            bbl.setVisibility(View.INVISIBLE);
            lblLocation.setVisibility(View.VISIBLE);
            lblLocation.setText("Couldn't get the location");
            Toast.makeText(getApplicationContext(), "Couldn't get the location", Toast.LENGTH_SHORT).show();
        }
    }

}
