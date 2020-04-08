package in.thenextmove.homehub;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import helpers.MQTTHelper;
import helpers.DBHelper;

public class DeviceActivity extends AppCompatActivity {
    //Database helper
    DBHelper dbHelper;

    //user data = "4060431"
    String email,pass,name,chipid;

    //get these from calling activity
    String greetingstring = "Afternoon ";

    //MQTT Strings
    MQTTHelper mqttHelper;
    String Publish_Topic;
    String subscriptionTopic;
    final String serverUri = "tcp://m12.cloudmqtt.com:12233";//"tcp://api.sensesmart.in:1883";
    final String username = "wbynzcri";
    final String password = "uOIqIxMgf3Dl";
    String clientId = "";

    //Device details
    String codename,version,threshold,state,sensortype;

    //Views
    private TextView statustextview, devicenametextview, greetingtextview, temperaturetextview;
    private TextView gasstatustextview;
    private ConstraintLayout gasstatuslayout;
    private ConstraintLayout testbuttonlayout,sensitivitybuttonlayout,setupwifibuttonlayout,updatebuttonlayout;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    statustextview.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    statustextview.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    statustextview.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get data from starting activity
        Bundle b = getIntent().getExtras();
        if (b != null) {
            email = b.getString("email");
        }

        setContentView(R.layout.activity_device);
        // In Activity's onCreate() for instance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        //Bottom Navigation
        BottomNavigationView navView = findViewById(R.id.nav_view);
        //Textviews
        statustextview = findViewById(R.id.status_TextView);
        devicenametextview = findViewById(R.id.devicename_TextView);
        greetingtextview = findViewById(R.id.greeting_TextView);
        temperaturetextview = findViewById(R.id.temperature_TextView);
        gasstatustextview = findViewById(R.id.gasstatus_TextView);
        //constraint Layout
        gasstatuslayout = findViewById(R.id.gasstatus_Layout);
        testbuttonlayout = findViewById(R.id.testbutton_Layout);
        sensitivitybuttonlayout = findViewById(R.id.sensitivitybutton_Layout);
        setupwifibuttonlayout = findViewById(R.id.setupwifibutton_Layout);
        updatebuttonlayout = findViewById(R.id.updatebutton_Layout);

        //Navigation View Setup
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //initiate database
        dbHelper = new DBHelper(this, null, null, 1);
        name = dbHelper.getusernamenames(email);
        greetingstring = greetingstring.concat(name);
        pass = dbHelper.getusernamepasswords(email);
        chipid = dbHelper.getusernamechipid(email);
        Publish_Topic = chipid +"ESP";
        subscriptionTopic = chipid+"/#";

        //Setup Widget String Values
        statustextview.setText("Offline");
        greetingtextview.setText(greetingstring);
        gasstatustextview.setText("SAFE");

        //Setup Onclick Listners
        testbuttonlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mqttHelper.sendData(Publish_Topic+"/sensor/0/test/","");
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        setupwifibuttonlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mqttHelper.sendData(Publish_Topic+"/system/wifisetup/","");
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        updatebuttonlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mqttHelper.sendData(Publish_Topic+"/system/update/","http://thenextmovecorp.000webhostapp.com/bin/Firmware.ino.nodemcu.bin");
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        //This was previously in onstart case
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            // TODO: Consider calling
            //  ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        clientId = telephonyManager.getDeviceId();

        mqttHelper = new MQTTHelper(getApplicationContext(),serverUri,clientId,username,password,subscriptionTopic,Publish_Topic);

        //mqtt stuff
        mqttHelper.mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.e("Debug"," Device Connected");
                statustextview.setText("Online");
                statustextview.setBackgroundResource(R.drawable.rounded_background_blue);
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.e("Debug","Offline");
                statustextview.setBackgroundResource(R.drawable.rounded_background_red);
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                //Log.w("Debug",topic);
                //Log.w("Debug",mqttMessage.toString());
                String intopic = topic;
                String sub = intopic.substring(0, intopic.indexOf('/'));
                intopic = intopic.substring(intopic.indexOf('/') + 1);
                if (sub.contentEquals((chipid))) {
                    sub = intopic.substring(0, intopic.indexOf('/'));
                    intopic = intopic.substring(intopic.indexOf('/') + 1);
                    if (sub.contentEquals("sensor")) {
                        sub = intopic.substring(0, intopic.indexOf('/'));
                        intopic = intopic.substring(intopic.indexOf('/') + 1);
                        if (sub.contentEquals("0")) {
                            sub = intopic.substring(0, intopic.indexOf('/'));
                            intopic = intopic.substring(intopic.indexOf('/') + 1);
                            if (sub.contentEquals("type")) {
                                sensortype = mqttMessage.toString();
                            }
                            if (sub.contentEquals("threshold")) {
                                threshold = mqttMessage.toString();
                            }
                            if (sub.contentEquals("state")) {
                                state = mqttMessage.toString();
                                if ((state.contentEquals("0"))) {
                                    gasstatuslayout.setBackgroundResource(R.drawable.rounded_background_green);
                                    gasstatustextview.setText("SAFE");
                                }
                                if ((state.contentEquals("1"))) {
                                    gasstatuslayout.setBackgroundResource(R.drawable.rounded_background_red);
                                    gasstatustextview.setText("LEAK");
                                }
                            }
                        }
                    }
                    if (sub.contentEquals("information")) {
                        sub = intopic.substring(0, intopic.indexOf('/'));
                        intopic = intopic.substring(intopic.indexOf('/') + 1);
                        if (sub.contentEquals("codename")) {
                            codename = (mqttMessage.toString());
                            devicenametextview.setText(codename.substring(0, 8));
                        }
                        if (sub.contentEquals("version")) {
                            version = (mqttMessage.toString());
                        }
                    }
                    if (sub.contentEquals("offline")) {
                        if (((mqttMessage.toString())).contentEquals("0")) {
                            Log.w("Debug", "Device Online");
                            statustextview.setText("Online");
                            statustextview.setBackgroundResource(R.drawable.rounded_background_green);
                        }
                        if (((mqttMessage.toString())).contentEquals("1")) {
                            Log.w("Debug", "Device Offline");
                            statustextview.setText("Offline");
                            statustextview.setBackgroundResource(R.drawable.rounded_background_red);
                        }
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Debug", "onstart");

    }

}
