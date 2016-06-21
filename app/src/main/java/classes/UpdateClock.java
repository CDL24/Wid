package classes;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.sample.foo.simplewidget.R;
import com.sample.foo.simplewidget.SimpleWidgetProvider;

import java.text.DateFormatSymbols;
import java.util.Calendar;

/**
 * Created by C.limbachiya on 6/21/2016.
 */
public class UpdateClock extends Service {

    /**
     * Used by the AppWidgetProvider to notify the Service that the views
     * need to be updated and redrawn.
     */
    public static final String ACTION_UPDATE = "com.sample.foo.simplewidget.action.UPDATE";

    private final static IntentFilter sIntentFilter;

    private final static String FORMAT_12_HOURS = "h:mm";
    private final static String FORMAT_24_HOURS = "kk:mm";

    private String mTimeFormat;
    private String mDateFormat;
    private Calendar mCalendar;
    private String mAM, mPM;

    static {
        sIntentFilter = new IntentFilter();
        sIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        sIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        sIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        reinit();
        registerReceiver(mTimeChangedReceiver, sIntentFilter);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mTimeChangedReceiver);
    }

        /*@Override
        public void onStart(Intent intent, int startId) {
            super.onStart(intent, startId);

            if (ACTION_UPDATE.equals(intent.getAction())) {

            }
            update();
        }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
            /*if (ACTION_UPDATE.equals(intent.getAction())) {
                update();
            }*/

        update();
        return START_REDELIVER_INTENT;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    /**
     * Updates and redraws the Widget.
     */
    private void update() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        final CharSequence time = DateFormat.format(mTimeFormat, mCalendar);
        final CharSequence date = DateFormat.format(mDateFormat, mCalendar);

        /*if ( ! is24HourMode(this)) {
            final boolean isMorning = (mCalendar.get(Calendar.AM_PM) == 0);
            views.setTextViewText(R.id.AM_PM, (isMorning ? mAM : mPM));
        }
        else {
            views.setTextViewText(R.id.AM_PM, "");
        }*/

        RemoteViews views = new RemoteViews(getPackageName(), R.layout.simple_widget);

        if ( ! is24HourMode(this)) {
            final boolean isMorning = (mCalendar.get(Calendar.AM_PM) == 0);
            views.setTextViewText(R.id.text_date_time, time+" "+(isMorning ? mAM : mPM)+" \n "+date);
        }else {
            views.setTextViewText(R.id.text_date_time, time+" \n "+date);
        }

        //views.setTextViewText(R.id.Date, date);

        Toast.makeText(this, "update : "+time+" "+date, Toast.LENGTH_SHORT).show();

        ComponentName widget = new ComponentName(this, SimpleWidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(widget, views);
    }

    private void reinit() {
        final String[] ampm = new DateFormatSymbols().getAmPmStrings();
        mDateFormat = getString(R.string.date_format);
        mTimeFormat = is24HourMode(this) ? FORMAT_24_HOURS : FORMAT_12_HOURS;
        mCalendar = Calendar.getInstance();
        mAM = ampm[0].toUpperCase();
        mPM = ampm[1].toUpperCase();
    }

    private static boolean is24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }

    /**
     * Automatically registered when the Service is created, and unregistered
     * when the Service is destroyed.
     */
    private final BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(Intent.ACTION_TIME_CHANGED) ||
                    action.equals(Intent.ACTION_TIMEZONE_CHANGED))
            {
                    /*
                     * The user went in and tweaked his time settings, reinitialize our
                     * date/time format strings and am/pm strings before we redraw.
                     */
                reinit();
            }

            update();
        }
    };

}
