package com.farshadmomtaz.lasttext;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class BatteryService extends Service {
    DBHelper LastTextdb;
    boolean MessageSentClean = true;

    @Override
    public void onCreate() {
        super.onCreate();

        LastTextdb = new DBHelper(getApplicationContext());
        MessageSentClean = !(LastTextdb.getMessageSentStatus());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent pIntent, int flags, int startId) {
        // Register Battery Reciver
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        this.registerReceiver(bReceiver, filter);

        return super.onStartCommand(pIntent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = 100 * (level / (float)scale);
            int batteryPctRound = Math.round(batteryPct);

            if (batteryPct == batteryPctRound) {

                if (isCharging && !MessageSentClean  && batteryPct >= 55 ) { // reset message sent
                    LastTextdb.resetMessageSentAllContact();
                    MessageSentClean = true;
                }
                else if (!isCharging && batteryPct <= 25) { // send messages
                    ArrayList<ListItem> list = LastTextdb.getContactsByPercentage(batteryPctRound);

                    for (ListItem contact : list) {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(contact.ItemTitle, null, contact.ItemDescription, null, null);

                        // insert message into the history table
                        LastTextdb.insertHistory(contact.ItemId);
                        MessageSentClean = false;

                        // set up and send notification
                        String notificationBody = getString(R.string.notification_body);
                        notificationBody = notificationBody.replace("$s", contact.ItemDetail);
                        notificationBody = notificationBody.replace("$d", Integer.toString(batteryPctRound));
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                        mBuilder.setContentTitle(getString(R.string.notification_title));
                        mBuilder.setContentText(notificationBody);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mBuilder.setSmallIcon(R.drawable.ic_launcher_transparent);
                            mBuilder.setColor(Color.parseColor("#15AB5F"));
                        } else {
                            mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
                        }

                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(contact.ItemId, mBuilder.build());
                    }

                }
            }
        }
    };
}
