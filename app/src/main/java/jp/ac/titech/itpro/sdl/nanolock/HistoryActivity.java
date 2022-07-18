package jp.ac.titech.itpro.sdl.nanolock;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HistoryActivity extends AppCompatActivity {
    private final static String TAG = HistoryActivity.class.getSimpleName();
    private HistoryAdapter adapter;
    private String current_device = "";
    private String current_device_name;
    private Intent main_intent;
    private Bundle main_bundle;

    private String android_name;
    private String mac_address;
    private TextView pagenameTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        main_intent = new Intent(HistoryActivity.this, MainActivity.class);
        main_bundle = new Bundle();

        ListView devices = findViewById(R.id.devices);
        adapter = new HistoryAdapter(this,
                new ArrayList<HistoryView>());
        devices.setAdapter(adapter);

        mac_address = getMacAddress();
        android_name = android.os.Build.MODEL;

        Bundle extras = getIntent().getExtras();
        TextView current_device_view = findViewById(R.id.current_device_view);
        if (extras != null) {
            current_device = extras.getString("current_device");
            current_device_name = extras.getString("current_device_name");
            current_device_view.setText("Lock device:\n" + current_device_name + ", " + current_device);
            connect();
        }
        else if (current_device == "") {
            current_device_view.setText("No device selected");
        }
        else {
            current_device_view.setText("Lock device:\n" + current_device_name + ", " + current_device);
            connect();
        }
        pagenameTextView = findViewById(R.id.pagename);
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
                bundle.putString("current_device", current_device);
                bundle.putString("current_device_name", current_device_name);
                main_intent.putExtras(bundle);
                startActivity(main_intent);
                break;
            case R.id.menu_devices:
                intent = new Intent(this, DeviceActivity.class);
                bundle.putString("current_device", current_device);
                bundle.putString("current_device_name", current_device_name);
                intent.putExtras(bundle);
                startActivity(intent);
            case R.id.menu_refresh:
                connect();
                break;
        }
        return true;
    }

    //getting mac address from mobile
    private String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    // res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }

    private void showConnectionResult(String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HistoryActivity.this, "server down", Toast.LENGTH_SHORT).show();
                pagenameTextView.setText(result);
            }
        });
    }

    private void showLog(JSONArray log) throws JSONException {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.clear();
                for (int i = 0; i < log.length(); i++) {
                    JSONArray row = null;
                    try {
                        row = log.getJSONArray(i);
                        String name = row.getString(0);
                        String mac_address = row.getString(1);
                        String query = row.getString(2);
                        String datetime = row.getString(3);
                        adapter.add(new HistoryView(name, mac_address, query, datetime));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void connect() {
        // query: connect / lock / unlock
        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = "http://" + current_device + ":5000/log";

        OkHttpClient client = new OkHttpClient();

        JSONObject postdata = new JSONObject();
        try {
            postdata.put("name", android_name);
            postdata.put("mac_address", mac_address);
        } catch(JSONException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(postdata.toString(), MEDIA_TYPE);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showConnectionResult("Error connecting to the server");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseData = response.body().string();
                Log.e(TAG, "onResponse: " + responseData);
                try {
                    JSONObject state = new JSONObject(responseData);
                    // Log.e(TAG, "onResponse: locked = " + locked);
                    Boolean connected = state.getBoolean("result");
                    if (connected) {
                        showConnectionResult("Connected");
                        JSONArray log = state.getJSONObject("data").getJSONArray("log");
                        showLog(log);
                    } else {
                        showConnectionResult("This phone is not registered");
                    }
                } catch (JSONException e) {
                }
            }
        });
    }
}
