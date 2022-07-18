package jp.ac.titech.itpro.sdl.nanolock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.NetworkInterface;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    private TextView pagenameTextView;
    private Boolean locked = true;
    private String current_device = "";
    private String current_device_name = "";
    private Button lock;
    private String mac_address;
    private String android_name;
    private Boolean connected = false;
    TextView current_device_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        TextView macaddress_view = findViewById(R.id.macaddress_view);
        mac_address = getMacAddress();

        // String android_name = android.os.Build.BRAND + " " + android.os.Build.MODEL;
        android_name = android.os.Build.MODEL;
        macaddress_view.setText("My model:\n" + android_name + "\n" + mac_address);

        loadData();

        current_device_view = findViewById(R.id.current_device_view);
        if (current_device == "") {
            current_device_view.setText("No device selected");
        }
        else {
            current_device_view.setText("Lock device:\n" + current_device_name + ", " + current_device);
        }
        lock = findViewById(R.id.lock);
        pagenameTextView = findViewById(R.id.pagename);

        // First connection
        if (current_device != "") {
            connect("connect");
        }

        lock.setOnClickListener(v -> {
            if (current_device == "" || !connected) {
                return;
            }

            String query = "lock";
            if (locked) {
                query = "unlock";
            }
            connect(query);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            current_device = extras.getString("current_device");
            current_device_name = extras.getString("current_device_name");
            current_device_view.setText("Lock device:\n" + current_device_name + ", " + current_device);
        }
        connect("connect");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        saveData();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("current_device", current_device);
        outState.putString("current_device_name", current_device_name);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        current_device = savedInstanceState.getString("current_state");
        current_device_name = savedInstanceState.getString("current_state_name");

        super.onRestoreInstanceState(savedInstanceState);
    }

    private void loadData() {
        Log.d(TAG, "loadData");
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);

        current_device = sharedPreferences.getString("current_device", null);
        current_device_name = sharedPreferences.getString("current_device_name", null);

        if (current_device == null) {
            current_device = "";
        }
        if (current_device_name == null) {
            current_device_name = "";
        }
    }

    private void saveData() {
        Log.d(TAG, "saveData");
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("current_device", current_device);
        editor.putString("current_device_name", current_device_name);
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        Intent intent;
        Bundle bundle = new Bundle();
        switch (item.getItemId()) {
            case R.id.menu_devices:
                intent = new Intent(this, DeviceActivity.class);
                bundle.putString("current_device", current_device);
                bundle.putString("current_device_name", current_device_name);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.menu_history:
                if (connected) {
                    intent = new Intent(this, HistoryActivity.class);
                    bundle.putString("current_device", current_device);
                    bundle.putString("current_device_name", current_device_name);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.menu_refresh:
                if (current_device != "" && current_device != null) {
                    connect("connect");
                }
                break;
        }
        return true;
    }

    private void changeLockIcon() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (locked) {
                    lock.setText(R.string.button_locked);
                    lock.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
                } else {
                    lock.setText(R.string.button_unlocked);
                    lock.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unlock, 0, 0, 0);
                }
            }
        });
    }

    private void showConnectionResult(String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "server down", Toast.LENGTH_SHORT).show();
                pagenameTextView.setText(result);
            }
        });
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

    public void connect(String query) {
        // query: connect / lock / unlock
        if (current_device == "" || current_device == null) {
            return;
        }
        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = "https://" + current_device + ":5000/" + query;

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        OkHttpClient.Builder newBuilder = new OkHttpClient.Builder();
        newBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        newBuilder.hostnameVerifier((hostname, session) -> true);

        OkHttpClient client = newBuilder.build();

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
                    locked = state.getJSONObject("data").getBoolean("status");
                    changeLockIcon();

                    connected = state.getBoolean("result");
                    if (connected) {
                        showConnectionResult("Connected");
                    } else {
                        showConnectionResult("This phone is not registered");
                    }
                } catch (JSONException e) {
                }
            }
        });
    }
}