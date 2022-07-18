package jp.ac.titech.itpro.sdl.nanolock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class DeviceActivity extends AppCompatActivity {
    private final static String TAG = DeviceActivity.class.getSimpleName();
    private LockDevicesAdapter adapter;
    private String current_device = "";
    private String current_device_name;
    private Intent main_intent;
    private Bundle main_bundle;
    private ArrayList<LockDevicesView> array_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        main_intent = new Intent(DeviceActivity.this, MainActivity.class);
        main_bundle = new Bundle();
        Button register = findViewById(R.id.button_register);

        loadData();

        register.setOnClickListener(v -> {
            EditText input_ip = findViewById(R.id.input_ip);
            String input_ip_str = input_ip.getText().toString().trim();
            EditText input_name = findViewById(R.id.input_name);
            String input_name_str = input_name.getText().toString().trim();

            adapter.add(new LockDevicesView(input_name_str, input_ip_str));
        });

        ListView devices = findViewById(R.id.devices);
        adapter = new LockDevicesAdapter(this, array_list, this);
        devices.setAdapter(adapter);

    }

    public void deleteDevice(LockDevicesView v) {
        adapter.remove(v);
    }

    public void selectDevice(LockDevicesView v) {
        Log.d(TAG, "devices.setOnItemClickListener");
        current_device = v.getIP();
        current_device_name = v.getName();
        main_bundle.putString("current_device", current_device);
        main_bundle.putString("current_device_name", current_device_name);
        main_intent.putExtras(main_bundle);
        startActivity(main_intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            current_device = extras.getString("current_device");
            current_device_name = extras.getString("current_device_name");
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("current_device", current_device);
        outState.putString("current_device_name", current_device_name);
        saveData();

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        current_device = savedInstanceState.getString("current_state");
        current_device_name = savedInstanceState.getString("current_state_name");

        super.onRestoreInstanceState(savedInstanceState);
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);

        Gson gson = new Gson();
        String json = sharedPreferences.getString("devices", null);
        Type type = new TypeToken<ArrayList<LockDevicesView>>() {}.getType();

        array_list = gson.fromJson(json, type);

        if (array_list == null) {
            array_list = new ArrayList<>();
        }
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(array_list);
        editor.putString("devices", json);
        editor.apply();

        Toast.makeText(this, "Saved Array List to Shared preferences. ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        Intent intent;
        Bundle bundle = new Bundle();
        switch (item.getItemId()) {
            case R.id.menu_lock:
                startActivity(main_intent);
                break;
            case R.id.menu_history:
                if (current_device != "") {
                    intent = new Intent(this, HistoryActivity.class);
                    bundle.putString("current_device", current_device);
                    bundle.putString("current_device_name", current_device_name);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.menu_refresh:
                break;
        }
        return true;
    }

}
