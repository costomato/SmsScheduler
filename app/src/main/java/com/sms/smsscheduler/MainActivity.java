package com.sms.smsscheduler;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sms.smsscheduler.databinding.ActivityMainSchedulerBinding;

import java.util.Calendar;
import java.util.List;

import ru.ifr0z.timepickercompact.TimePickerCompact;

public class MainActivity extends AppCompatActivity {

    private EditText recipient, messageBody;
    String msg, theNumber;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PICK_CONTACT = 1000;

    private ActivityMainSchedulerBinding binding;
    private RadioGroup simGroup;
    private SmsManager sms;
    private List<SubscriptionInfo> localList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainSchedulerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recipient = binding.etContact;
        messageBody = binding.etMessage;
        simGroup = binding.radioGroup;


        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.SEND_SMS},
                    PERMISSION_REQUEST_CODE);
        }


        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_PHONE_STATE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSION_REQUEST_CODE);
        }
        SubscriptionManager localSubscriptionManager = SubscriptionManager.from(this);
        localList = localSubscriptionManager.getActiveSubscriptionInfoList();
        if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 1) {
            simGroup.setVisibility(View.VISIBLE);
        } else {
            simGroup.setVisibility(View.GONE);
        }


        if (checkSelfPermission(Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_DENIED) {

            Log.d("permission", "permission denied to SEND_SMS - requesting it");
            String[] permissions = {Manifest.permission.SEND_SMS};
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);

        }
        if (checkSelfPermission(Manifest.permission.WAKE_LOCK)
                == PackageManager.PERMISSION_DENIED) {

            Log.d("permission", "permission denied to WAKE_LOCK - requesting it");
            String[] permissions = {Manifest.permission.WAKE_LOCK};
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);

        }
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_DENIED) {

            Log.d("permission", "permission denied to READ_CONTACTS - requesting it");
            String[] permissions = {Manifest.permission.READ_CONTACTS};
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }

    }


    private boolean selectSim() {
        SubscriptionInfo simInfo;
        if (localList == null)
            return false;
        switch (localList.size()) {
            case 0:
                return false;
            case 1:
                simInfo = localList.get(0);
                sms = SmsManager.getSmsManagerForSubscriptionId(simInfo.getSubscriptionId());
                return true;
            default:
                simInfo = localList.get(
                        simGroup.getCheckedRadioButtonId() == R.id.sim1 ? 0 : 1
                );
                sms = SmsManager.getSmsManagerForSubscriptionId(simInfo.getSubscriptionId());
                return true;
        }
    }


    public void pickAContactNumber(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (reqCode == PICK_CONTACT) {
                contactPicked(data);
            }
        } else {
            Toast.makeText(this, "Contact not picked", Toast.LENGTH_SHORT).show();
        }
    }

    private void contactPicked(Intent data) {
        Cursor cursor;
        try {
            String phoneNo;
            Uri uri = data.getData();
            cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            phoneNo = cursor.getString(phoneIndex);
            recipient.setText(phoneNo);
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter("myBroadcast"));

    }

    @Override
    public void onPause() {
        super.onPause();
        registerReceiver(broadcastReceiver, new IntentFilter("myBroadcast"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    BroadcastReceiver broadcastReceiver = new MyTask() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sendMsg(theNumber, msg);
        }
    };

    public void scheduleMessage(View view) {
        theNumber = recipient.getText().toString();
        msg = messageBody.getText().toString();
        if (theNumber.isEmpty()) {
            Toast.makeText(this, "Please select recipient", Toast.LENGTH_LONG).show();
            return;
        }
        if (msg.isEmpty()) {
            Toast.makeText(this, "Please type a message", Toast.LENGTH_LONG).show();
            return;
        }

        if (selectSim()) {
            DatePicker dateP = binding.dateP;
            TimePickerCompact timeP = binding.timeP;
            Calendar customCalendar = Calendar.getInstance();
            customCalendar.set(dateP.getYear(),
                    dateP.getMonth(),
                    dateP.getDayOfMonth(),
                    timeP.getHour(),
                    timeP.getMinute(),
                    0);
            long customTime = customCalendar.getTimeInMillis();
            long currentTime = System.currentTimeMillis();
            long delay = customTime - currentTime;
            if (delay <= 0) {
                delay = 0;
                Log.d("DELAY", "TIME = " + (delay));
                Toast.makeText(this, "Sending SMS now...", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "SMS Scheduled", Toast.LENGTH_LONG).show();
            setAlarm(delay);
        } else {
            Toast.makeText(this, "Sim card not found", Toast.LENGTH_LONG).show();
        }
    }

    private void setAlarm(long timeInMillis) {
        AlarmManager alarmManger = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(MainActivity.this, MyTask.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
        alarmManger.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
    }


    public void sendMsg(String theNumber, String msg) {
        String sent = "Message sent!!";
        String delivered = "Message delivered!!";
        PendingIntent sentPI = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(sent), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(delivered), 0);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "SMS sent!!", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(MainActivity.this, "Generic Failure!!", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(MainActivity.this, "No service!!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(sent));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "SMS delivered!!", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(MainActivity.this, "SMS not delivered!!", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        }, new IntentFilter(delivered));

        sms.sendTextMessage(theNumber, null, msg, sentPI, deliveredPI);

    }
}
