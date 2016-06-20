package classes;

import android.Manifest;
import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.sample.foo.simplewidget.R;
import com.sample.foo.simplewidget.SimpleWidgetProvider;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by C.limbachiya on 5/4/2016.
 */
public class UpdateIntentService extends IntentService implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static Location mLastLocation;

    // Google client to interact with Google API
    private static GoogleApiClient mGoogleApiClient;
    static FusedLocationProviderApi fusedLocationProviderApi;

    static LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 60000; // 60 sec
    private static int FATEST_INTERVAL = 10000; // 10 sec

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
  /*  public UpdateIntentService(String name) {
        super(name);
    }*/
    public UpdateIntentService() {
        super("UpdateIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Toast.makeText(this, "onHandleIntent", Toast.LENGTH_SHORT).show();

       /* createLocationRequest();

        fusedLocationProviderApi = LocationServices.FusedLocationApi;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if (mGoogleApiClient != null) {
            //Toast.makeText(getApplicationContext(), "Get Client", Toast.LENGTH_SHORT).show();
            mGoogleApiClient.connect();
        }*/

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

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


        Toast.makeText(getBaseContext(),"onStart : IntentService", Toast.LENGTH_LONG).show();
        // Build the widget update for today
        RemoteViews updateViews = buildUpdate(this);

        // Push update for this widget to the home screen
        ComponentName thisWidget = new ComponentName(this, SimpleWidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(thisWidget, updateViews);
        return START_REDELIVER_INTENT;
    }

    public RemoteViews buildUpdate(Context context) {
        // Here I am updating the remoteview
        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.simple_widget);
        return prepareWidgetLayout(updateViews, context);
    }

    public void createLocationRequest() {
        //Toast.makeText(getApplicationContext(), "createLocationRequest",Toast.LENGTH_SHORT).show();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //Updating Widget UI design
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
    public void onConnected(@Nullable Bundle bundle) {
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
    public void onLocationChanged(Location location) {
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "onConnectionFailed", Toast.LENGTH_SHORT).show();

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.simple_widget);
        remoteViews.setViewVisibility(R.id.progressBar,
                View.INVISIBLE);
    }
}
