package jp.ac.titech.itpro.sdl.nanolock;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class HistoryAdapter extends ArrayAdapter<HistoryView> {
    public HistoryAdapter(@NonNull  Context context, ArrayList<HistoryView> array_list) {
        super(context, 0, array_list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convert_view, @NonNull ViewGroup parent) {
        // convert_view which is recyclable view
        View current_item_view = convert_view;

        if (current_item_view == null) {
            current_item_view = LayoutInflater.from(getContext()).inflate(R.layout.history_layout, parent, false);
        }

        HistoryView current_pos = getItem(position);

        TextView name_view = current_item_view.findViewById(R.id.historylist_name);
        assert current_pos != null;
        name_view.setText(current_pos.getName());

        TextView mac_view = current_item_view.findViewById(R.id.historylist_mac);
        mac_view.setText(current_pos.getMacAddress());

        TextView query_view = current_item_view.findViewById(R.id.historylist_query);
        query_view.setText(current_pos.getQuery());

        TextView datetime_view = current_item_view.findViewById(R.id.historylist_datetime);
        datetime_view.setText(current_pos.getDatetime());

        // then return the recyclable view
        return current_item_view;
    }
}
