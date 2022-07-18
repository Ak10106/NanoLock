package jp.ac.titech.itpro.sdl.nanolock;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class LockDevicesAdapter extends ArrayAdapter<LockDevicesView> implements View.OnClickListener {
    private AppCompatActivity activity;
    private final static String TAG = LockDevicesAdapter.class.getSimpleName();
    public LockDevicesAdapter(@NonNull  Context context, ArrayList<LockDevicesView> array_list, AppCompatActivity activity) {
        super(context, 0, array_list);
        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convert_view, @NonNull ViewGroup parent) {
        // convert_view which is recyclable view
        View current_item_view = convert_view;

        if (current_item_view == null) {
            current_item_view = LayoutInflater.from(getContext()).inflate(R.layout.locks_layout, parent, false);
        }

        // get the position of the view from the ArrayAdapter
        LockDevicesView current_pos = getItem(position);

        // then according to the position of the view assign the desired image for the same
        TextView device_name_view = current_item_view.findViewById(R.id.devicelist_name);
        assert current_pos != null;
        device_name_view.setText(current_pos.getName());

        // then according to the position of the view assign the desired TextView 1 for the same
        TextView device_ip_view = current_item_view.findViewById(R.id.devicelist_ip);
        device_ip_view.setText(current_pos.getIP());

        ImageButton delete_button = current_item_view.findViewById(R.id.button_delete);
        delete_button.setOnClickListener((view -> {
            ((DeviceActivity)activity).deleteDevice(current_pos);
        }));

        current_item_view.setOnClickListener((view -> {
            ((DeviceActivity)activity).selectDevice(current_pos);
        }));

        // then return the recyclable view
        return current_item_view;
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick");
        int position=(Integer) v.getTag();
        Object object = getItem(position);
        LockDevicesView lock = (LockDevicesView)object;

    }
}
