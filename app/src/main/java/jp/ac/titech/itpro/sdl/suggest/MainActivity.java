package jp.ac.titech.itpro.sdl.suggest;

import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

    private EditText input;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        input = findViewById(R.id.input);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            String query = input.getText().toString().trim();
            if (!query.isEmpty()) {
                String suggestUrl = getResources().getString(R.string.suggest_url);
                new SuggestTask(MainActivity.this, suggestUrl).execute(query);
            }
        });

        ListView suggested = findViewById(R.id.suggested);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>());
        suggested.setAdapter(adapter);
        suggested.setOnItemClickListener((parent, view, pos, id) -> {
            String text = (String) parent.getItemAtPosition(pos);
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, text);
            startActivity(intent);
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


    private static class SuggestTask extends AsyncTask<String, Void, List<String>> {
        private final WeakReference<MainActivity> activityRef;
        private final Suggester suggester;

        SuggestTask(MainActivity activity, String baseUrl) {
            super();
            this.activityRef = new WeakReference<>(activity);
            this.suggester = new Suggester(baseUrl);
        }

        @Override
        protected List<String> doInBackground(String... strings) {
            return suggester.suggest(strings[0]);
        }

        @Override
        protected void onPostExecute(List<String> result) {
            MainActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.showResult(result);
        }
    }
}