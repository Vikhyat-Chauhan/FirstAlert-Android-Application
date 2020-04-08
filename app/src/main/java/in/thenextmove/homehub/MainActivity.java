package in.thenextmove.homehub;

import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import helpers.DBHelper;

public class MainActivity extends AppCompatActivity {

    DBHelper dbHelper;
    private ConstraintLayout add_button_LAYOUT;
    private TextView devicename_TEXTVIEW, status_TEXTVIEW, message_TEXTVIEW;
    private EditText name_EDITTEXT,username_EDITTEXT,password_EDITTEXT,chipdid_EDITTEXT;
    BottomNavigationView navView;
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    status_TEXTVIEW.setText(R.string.title_home);

                    ArrayList<String> usernames = dbHelper.getusernames();
                    for(int i = 0; i<usernames.size(); i++) {
                        Log.i("Debug", usernames.get(i));
                    }
                    return true;
                case R.id.navigation_dashboard:
                    status_TEXTVIEW.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    status_TEXTVIEW.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        status_TEXTVIEW = findViewById(R.id.status_Textview);
        status_TEXTVIEW.setBackgroundResource(R.drawable.rounded_background_blue);
        devicename_TEXTVIEW = findViewById(R.id.devicename_Textview);
        message_TEXTVIEW = findViewById(R.id.message_Textview);
        chipdid_EDITTEXT = findViewById(R.id.chipid_editText);
        name_EDITTEXT = findViewById(R.id.name_editText);
        username_EDITTEXT = findViewById(R.id.username_editText);
        password_EDITTEXT = findViewById(R.id.password_editText);
        add_button_LAYOUT = findViewById(R.id.add_button_layout);

        dbHelper = new DBHelper(this, null, null, 1);

        add_button_LAYOUT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_button_LAYOUT.setPressed(true);
                Toast.makeText(getApplicationContext(), "Adding Credentials", Toast.LENGTH_SHORT).show();
                getdata();
            }
        });
    }

    protected void getdata(){
        Log.i("Debug","Getting data");
        String chipid = chipdid_EDITTEXT.getText().toString();
        String name = name_EDITTEXT.getText().toString();
        String username = username_EDITTEXT.getText().toString();
        String password = password_EDITTEXT.getText().toString();
        if(username.length() >0 & password.length() >0 & chipid.length() >0 ) {
            dbHelper.addCredentials(username,password,name,chipid);
            Log.i("H.O.M.E", "Database Created");
            Intent myIntent = new Intent(this, DeviceActivity.class);
            myIntent.putExtra("username",username);
            startActivity(myIntent);
        }
        else{
            if(username.length()<=0){
                Toast.makeText(getApplicationContext(), "Enter Username", Toast.LENGTH_SHORT).show();
            }
            if(password.length()<=0) {
                Toast.makeText(getApplicationContext(), "Enter Password", Toast.LENGTH_SHORT).show();
            }
            if(chipid.length()<=0) {
                Toast.makeText(getApplicationContext(), "Enter Chipid", Toast.LENGTH_SHORT).show();
            }
            chipdid_EDITTEXT.setText("");
            username_EDITTEXT.setText("");
            password_EDITTEXT.setText("");
        }
    }
}

