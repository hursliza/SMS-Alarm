package com.example.sms_alarm;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;


public class SmsBroadcastReciever extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onReceive(Context context, Intent intent) {
        boolean setAlarm = false;
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            StringBuilder smsMessageStr = new StringBuilder();
            assert sms != null;
            for (Object sm : sms) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sm);

                String smsBody = smsMessage.getMessageBody();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM HH:mm:ss");
                String time = sdf.format(smsMessage.getTimestampMillis());

                if (smsBody.contains("XXXXXXXXXXX@XXX.XXX")) {
                    smsMessageStr.append(time).append("\n");
                    smsMessageStr.append(smsBody).append("\n");
                    setAlarm = true;

                }

            }
            if (setAlarm) {
                MainActivity inst = MainActivity.instance();
                inst.updateList(smsMessageStr.toString());
            }


        }
    }
}
