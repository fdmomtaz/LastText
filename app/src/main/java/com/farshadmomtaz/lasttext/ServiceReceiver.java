package com.farshadmomtaz.lasttext;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class ServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // check if auto message is disabled
        SharedPreferences sharedPref = context.getSharedPreferences("LAST_TEXT_SETTING", Context.MODE_PRIVATE);
        boolean disableAutoMessage = sharedPref.getBoolean("DISABLE_AUTO_MESSAGE", false);

        // Start service
        if(!disableAutoMessage) {
            Intent serviceIntent = new Intent(context.getApplicationContext(), BatteryService.class);
            context.getApplicationContext().startService(serviceIntent);
        }
    }
}
