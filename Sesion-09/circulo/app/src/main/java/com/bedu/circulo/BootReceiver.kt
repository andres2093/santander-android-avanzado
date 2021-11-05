package com.bedu.circulo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if(action != null){
            if (action == Intent.ACTION_BOOT_COMPLETED) {
                AlarmReceiver().setAlarm(context, true)
            }
        }
    }
}