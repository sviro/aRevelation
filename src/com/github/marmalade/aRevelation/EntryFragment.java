package com.github.marmalade.aRevelation;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: <a href="mailto:alexey.kislin@gmail.com">Alexey Kislin</a>
 * Date: 9/4/13
 * Time: 2:00 AM
 */
public class EntryFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static ListView lv;
    private Activity activity;
    private FileEntriesFragment.Entry entry;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entry_layout, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        activity = getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        lv = (ListView)activity.findViewById(R.id.entry_list);
        showRevelationEntry(entry, activity);
        super.onStart();
    }

    EntryFragment(FileEntriesFragment.Entry entry) {
        this.entry = entry;
    }

    private void showRevelationEntry(FileEntriesFragment.Entry entry, Activity activity) {
        lv.setOnItemClickListener(this);
        lv.setOnItemLongClickListener(this);
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        HashMap<String, String> values = new HashMap<String, String>();
        values.put("First Line", activity.getString(R.string.name));
        values.put("Second Line", entry.name);
        data.add(values);
        values = new HashMap<String, String>();
        values.put("First Line", activity.getString(R.string.description));
        values.put("Second Line", entry.description);
        data.add(values);
        for(String key : entry.fields.keySet()) {
            values = new HashMap<String, String>();
            values.put("First Line", FileEntriesFragment.Entry.getFieldName(key, activity));
            values.put("Second Line", entry.fields.get(key));
            data.add(values);
        }
        values = new HashMap<String, String>();
        values.put("First Line", activity.getString(R.string.notes));
        values.put("Second Line", entry.notes);
        data.add(values);
        values = new HashMap<String, String>();
        values.put("First Line", "Updated");
        values.put("Second Line", entry.updated);
        data.add(values);

        SimpleAdapter adapter = new SimpleAdapter(activity, data,
                android.R.layout.simple_list_item_2,
                new String[] {"First Line", "Second Line" },
                new int[] {android.R.id.text1, android.R.id.text2 });
        lv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //TODO Implement behaviour
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //TODO Implement behaviour
        return false;
    }
}
