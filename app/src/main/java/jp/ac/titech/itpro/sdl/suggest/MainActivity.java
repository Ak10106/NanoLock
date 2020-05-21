package jp.ac.titech.itpro.sdl.suggest;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    private final static int MSG_RESULT = 1234;

    private final SuggestHandler handler = new SuggestHandler(this);

    private EditText input;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        input = findViewById(R.id.input);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = input.getText().toString().trim();
                if (!query.isEmpty()) {
                    String suggestUrl = getResources().getString(R.string.suggest_url);
                    new SuggestThread(suggestUrl, handler, query).start();
                }
            }
        });

        ListView suggested = findViewById(R.id.suggested);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                new ArrayList<String>());
        suggested.setAdapter(adapter);
        suggested.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                String text = (String) parent.getItemAtPosition(pos);
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, text);
                startActivity(intent);
            }
        });
    }

    public void showResult(List<String> result) {
        adapter.clear();
        if (result.isEmpty()) {
            adapter.add(getResources().getString(R.string.result_no_suggestions));
        } else {
            adapter.addAll(result);
        }
        adapter.notifyDataSetChanged();
        input.selectAll();
    }


    private static class SuggestHandler extends Handler {
        private WeakReference<MainActivity> activityRef;

        SuggestHandler(MainActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            MainActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            if (msg.what == MSG_RESULT) {
                activity.showResult((List<String>) msg.obj);
            }
        }
    }

    private static class SuggestThread extends Thread {

        private final Suggester suggester;
        private final SuggestHandler handler;
        private final String query;

        SuggestThread(String baseUrl, SuggestHandler handler, String query) {
            this.suggester = new Suggester(baseUrl);
            this.handler = handler;
            this.query = query;
        }

        @Override
        public void run() {
            List<String> result = suggester.suggest(query);
            handler.sendMessage(handler.obtainMessage(MSG_RESULT, result));
        }
    }
}