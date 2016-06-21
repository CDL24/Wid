package classes;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.sample.foo.simplewidget.R;
import com.sample.foo.simplewidget.SimpleWidgetProvider;

import java.util.Calendar;

/**
 * Created by C.limbachiya on 6/20/2016.
 */
public class MyTimer {

    RemoteViews remoteViews;
    Context context;
    AppWidgetManager appWidgetManager;
    ComponentName thisWidget;

    public MyTimer(Context context) {

        appWidgetManager = AppWidgetManager.getInstance(context);

        this.context = context;

        remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.simple_widget);

        thisWidget = new ComponentName(context, SimpleWidgetProvider.class);

    }

    public synchronized void runAndUpdateTheWidget() {

        //int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        //for (final int appWidgetId : allWidgetIds) {

        Toast.makeText(context, "Updating : "+getTodaysTime(), Toast.LENGTH_SHORT).show();
            /*System.out.println("UPDATING......" + getTodaysTime() + " ID = "
                    );*/

            /*remoteViews.setImageViewBitmap(R.id.text_detail,
                    buildUpdate(getTodaysTime()));*/
            remoteViews.setTextViewText(R.id.text_detail,getTodaysTime());

            appWidgetManager.updateAppWidget(thisWidget, remoteViews);

        //}

    }
    public Bitmap buildUpdate(String time) {
        int bmpWidth = 250;
        int bmpHeight = 100;
        Bitmap myBitmap = Bitmap.createBitmap(bmpWidth, bmpHeight,
                Bitmap.Config.ARGB_8888);
        Canvas myCanvas = new Canvas(myBitmap);
        Paint paint = new Paint();
        Typeface clock = Typeface.createFromAsset(context.getAssets(),
                "digital-7.ttf");
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(clock);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        paint.setTextSize(70);
        paint.setTextAlign(Paint.Align.CENTER);
        myCanvas.drawText(time, bmpWidth / 2, bmpHeight / 2 + (bmpHeight / 4),
                paint);
        return myBitmap;
    }

    public String getTodaysTime() {
        final Calendar c = Calendar.getInstance();
        int hour = Integer
                .parseInt(convertToNormal(c.get(Calendar.HOUR_OF_DAY)));
        int minute = c.get(Calendar.MINUTE);
        int seconds = c.get(Calendar.SECOND);
        int ampm = c.get(Calendar.AM_PM);
        return new StringBuilder().append(pad(hour)).append(":")
                .append(pad(minute)).append(":").append((ampm == 1)?"PM":"AM")
                .toString();
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    public String convertToNormal(int hour) {
        if (hour > 12)
            hour = hour - 12;

        return pad(hour);
    }
}