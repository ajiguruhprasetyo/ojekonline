package com.rigen.rigen.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ILHAM HP on 29/11/2016.
 */

public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        context.startService(new Intent(context, MessagesService.class));
        context.startService(new Intent(context, BookingService.class));
    }
}