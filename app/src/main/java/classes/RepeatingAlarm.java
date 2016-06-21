package classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by C.limbachiya on 6/20/2016.
 */
public class RepeatingAlarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MyTimer myTimer = new MyTimer(context);
        myTimer.runAndUpdateTheWidget();
    }
}
