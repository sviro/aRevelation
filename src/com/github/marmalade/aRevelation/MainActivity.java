package com.github.marmalade.aRevelation;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Author: <a href="mailto:alexey.kislin@gmail.com">Alexey Kislin</a>
 * Date: 5/29/13
 * Time: 11:26 PM
 */
public class MainActivity extends Activity {

    private ArrayList<File> filesBrowserItems;
    private ArrayAdapter<File> filesBrowserAdapter;
    private ListView lv;
    private MenuStatus status;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main_layout);
            status = MenuStatus.MainPage;
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void openFile(View view) {

        AdapterView.OnItemClickListener mMessageClickedHandler =
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        File clickedFile = filesBrowserItems.get(position);
                        if(clickedFile.isDirectory() && clickedFile.canRead())
                            changeLocation(clickedFile);
                        else if (clickedFile.isFile() && clickedFile.canRead()) {
                            checkFile(clickedFile);
                        }
                    }
                };

        try {
            setContentView(R.layout.open_file_layout);
            status = MenuStatus.OpenFile;
            lv = (ListView)findViewById(R.id.listView);
            filesBrowserItems = new ArrayList<File>();
            filesBrowserAdapter = new ArrayAdapter<File>(this,
                    android.R.layout.simple_list_item_1, filesBrowserItems);
            lv.setOnItemClickListener(mMessageClickedHandler);
            lv.setAdapter(filesBrowserAdapter);
            changeLocation(new File("/"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeLocation(File path) {
        filesBrowserItems.clear();
        if(path.getParent() != null)
            filesBrowserItems.add(new FileListItem(path.getParent(), "..."));
        String[] sortedChildren = path.list();
        Arrays.sort(sortedChildren);
        for(String childFile : sortedChildren)
            filesBrowserItems.add(new FileListItem(childFile));
        filesBrowserAdapter.notifyDataSetChanged();
    }

    private boolean checkFile(File file) {
        //TODO Implementation of file checking
        throw new UnsupportedOperationException("No implementation of checkFile method.");
    }

    private void goToStartLayout() {
        setContentView(R.layout.main_layout);
        status = MenuStatus.MainPage;
        lv = null;
        filesBrowserAdapter = null;
        filesBrowserItems = null;
    }

    @Override
    public void onBackPressed() {
        if(status == MenuStatus.OpenFile)
            goToStartLayout();
        else
          super.onBackPressed();
    }

    private class FileListItem extends File {

        String name;

        @Override
        public String toString() {
            if(name != null)
                return name;
            else
                return getName();
        }

        FileListItem(String path) {
            super(path);
        }

        FileListItem(String path, String name) {
            super(path);
            this.name = name;
        }

        @Override
        public String getParent() {
            return new File(getAbsolutePath()).getParent();
        }
    }

    enum MenuStatus {
        MainPage,
        OpenFile
    }
}
