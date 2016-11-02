package com.lionsquare.canoviusclient.servicios;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by archivaldo on 29/01/16.
 */
public class ReceiverBoot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


        Intent serviceIntent = new Intent(context, AutoSync.class);
        context.startService(serviceIntent); //problemas con el arranque



        /*Intent i = new Intent(context , Location.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);*/


    }


}
