package com.github.marmalade.aRevelation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Author: <a href="mailto:alexey.kislin@gmail.com">Alexey Kislin</a>
 * Date: 5/29/13
 * Time: 11:26 PM
 */
public class MainActivity extends Activity {

    private ArrayList<FileWrapper> filesBrowserItems;
    private ArrayAdapter<FileWrapper> filesBrowserAdapter;
    private ListView lv;
    MenuStatus status;


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
                        FileWrapper clickedFile = filesBrowserItems.get(position);
                        if(clickedFile.getFile().isDirectory() && clickedFile.getFile().canRead())
                            changeLocation(clickedFile);
                        else if (clickedFile.getFile().isFile() && clickedFile.getFile().canRead()) {
                            askIfOpenFile(clickedFile.getFile());
                        }
                    }
                };

        try {
            setContentView(R.layout.open_file_layout);
            status = MenuStatus.OpenFile;
            lv = (ListView)findViewById(R.id.listView);
            filesBrowserItems = new ArrayList<FileWrapper>();
            filesBrowserAdapter = new ArrayAdapter<FileWrapper>(this,
                    android.R.layout.simple_list_item_1, filesBrowserItems);
            lv.setOnItemClickListener(mMessageClickedHandler);
            lv.setAdapter(filesBrowserAdapter);
            changeLocation(new FileWrapper(new File("/")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeLocation(FileWrapper path) {
        filesBrowserItems.clear();
        if(path.getFile().getParent() != null)
            filesBrowserItems.add(new FileWrapper(path.getFile().getParentFile(), "..."));
        File[] sortedChildren = path.getFile().listFiles();
        Arrays.sort(sortedChildren);
        for(File childFile : sortedChildren)
            filesBrowserItems.add(new FileWrapper(childFile));
        filesBrowserAdapter.notifyDataSetChanged();
        lv.setSelection(0);
    }

    private void askIfOpenFile(final File file) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        askPassword(file);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // Nothing to do
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to open " + file.getName() + "?")
                .setNegativeButton("No", dialogClickListener)
                .setPositiveButton("Yes", dialogClickListener)
                .show();
    }

    private void askPassword(final File file) {
        final EditText input = new EditText(MainActivity.this);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        openFile(file, input.getText().toString());
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // Nothing to do
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Input password")
                .setView(input)
                .setNegativeButton("Cancel", dialogClickListener)
                .setPositiveButton("Submit", dialogClickListener)
                .show();
    }

    private void openFile(File file, String password) {
        try {
            String decryptedXML = Cryptographer.decrypt(file, password);
            Display.showRevelationEntries(decryptedXML, this);
        } catch (Exception e) {
            throw new UnsupportedOperationException("No implementation of error processing.");
        }
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

    private class FileWrapper {

        private File file;

        private String name;

        FileWrapper(File file) {
            this.file = file;
            this.name = file.getName();
        }

        FileWrapper(File file, String name) {
            this.file = file;
            this.name = name;
        }

        File getFile() {
            return file;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    enum MenuStatus {
        MainPage,
        OpenFile,
        DecryptedEntriesDisplay
    }
}
