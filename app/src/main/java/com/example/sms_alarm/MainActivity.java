package com.example.sms_alarm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends Activity implements OnItemClickListener {
    boolean turnedOn = false;

    @SuppressLint("StaticFieldLeak")
    private static MainActivity inst;
    ArrayList<String> smsMessagesList = new ArrayList<>();
    ListView smsListView;
    ArrayAdapter<String> arrayAdapter;

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
        turnOn();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        smsListView = findViewById(R.id.SMSList);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        smsListView.setAdapter(arrayAdapter);
        smsListView.setOnItemClickListener(this);

        refreshSmsInbox();
    }

    public void turnOn(){
        Switch sw = findViewById(R.id.Switch);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeState();
            }
        });
    }

    public void changeState(){
        turnedOn = !turnedOn;
    }

    public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        @SuppressLint("Recycle") Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        assert smsInboxCursor != null;
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexTime = smsInboxCursor.getColumnIndex("date");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm:ss");
        do {
            String str = sdf.format(new Date(smsInboxCursor.getLong(indexTime))) + "\n" +
                    smsInboxCursor.getString(indexBody) + "\n";
            arrayAdapter.add(str);
        } while (smsInboxCursor.moveToNext());
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)

    public void updateList(final String smsMessage) {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();

        if (turnedOn) {
            Intent setAlarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
            setAlarmIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            int h = Integer.parseInt(new SimpleDateFormat("HH").format(System.currentTimeMillis() + 60000));
            int min = Integer.parseInt(new SimpleDateFormat("mm").format(System.currentTimeMillis() + 60000));
            setAlarmIntent.putExtra(AlarmClock.EXTRA_HOUR, h);
            setAlarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, min);
            setAlarmIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
            setAlarmIntent.putExtra(AlarmClock.EXTRA_MESSAGE, smsMessage);
            startActivity(setAlarmIntent);
        }

    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        try {
            String[] smsMessages = smsMessagesList.get(pos).split("\n");
            String time = smsMessages[0];
            String smsMessage = "";
            for (int i = 1; i < smsMessages.length; ++i) {
                smsMessage += smsMessages[i];
            }

            String smsMessageStr = time + "\n";
            smsMessageStr += smsMessage;
            Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}