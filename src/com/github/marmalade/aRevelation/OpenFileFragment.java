/*
 * Copyright 2013 Aleksey Kislin
 * Copyright 2013 Michal Švirec
 *
 * This file is part of aRevelation.
 *
 * aRevelation is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aRevelation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with aRevelation.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.marmalade.aRevelation;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import javax.crypto.BadPaddingException;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Author: <a href="mailto:alexey.kislin@gmail.com">Alexey Kislin</a>
 * Date: 30.08.13
 * Time: 2:10
 */
public class OpenFileFragment extends Fragment implements AdapterView.OnItemClickListener, IBackPressedListener {

    private static final String DEFAULT_PATH="/";

	private static final String PATH = "path";

    // Current path of a showed menu
    private                                                                                                                                                                                                                                  String path;

    private ListView lv;
    private ArrayList<FileWrapper> filesBrowserItems = new ArrayList<FileWrapper>();
    private ArrayAdapter<FileWrapper> filesBrowserAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.open_file_layout, container, false);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	Bundle arguments = getArguments();
    	if (arguments != null) {
	        path = arguments.getString(PATH);
	}

        if (path == null) {
    	        path = DEFAULT_PATH;        
        }
    }

    @Override
    public void onStart() {
        this.lv = (ListView)getActivity().findViewById(R.id.listView);
        filesBrowserAdapter = new ArrayAdapter<FileWrapper>(this.getActivity(),
                android.R.layout.simple_list_item_1, filesBrowserItems);
        lv.setAdapter(filesBrowserAdapter);
        lv.setOnItemClickListener(this);
        setLocation(new FileWrapper(path));
        super.onStart();
    }

    
    public static OpenFileFragment newInstance(String path) {
    	OpenFileFragment fragment = new OpenFileFragment();
    	
    	Bundle bundle = new Bundle();
    	bundle.putString(PATH, path);
    	fragment.setArguments(bundle);
    	
    	return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    private void setLocation(FileWrapper path) {
        this.path = path.getFile().getAbsolutePath();
        filesBrowserItems.clear();
        if(path.getFile().getParent() != null)
            filesBrowserItems.add(new FileWrapper(path.getFile().getParentFile(), "..."));
        File[] sortedChildren = path.getFile().listFiles();
        Arrays.sort(sortedChildren);
        for(File childFile : sortedChildren)
            filesBrowserItems.add(new FileWrapper(childFile));
        filesBrowserAdapter.notifyDataSetChanged();
        lv.setSelection(0);         // Go to the top
    }


    private void openFile(final File file) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        askPassword(file);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }

            private void askPassword(final File file) {
                final EditText input = new EditText(OpenFileFragment.this.getActivity());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                tryToOpenFile(file, input.getText().toString());
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                // Nothing to do
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(OpenFileFragment.this.getActivity());
                builder.setMessage("Input password")
                        .setView(input)
                        .setNegativeButton("Cancel", dialogClickListener)
                        .setPositiveButton("Submit", dialogClickListener)
                        .show();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setMessage("Do you want to open " + file.getName() + "?")
                .setNegativeButton("No", dialogClickListener)
                .setPositiveButton("Yes", dialogClickListener)
                .show();
    }


    private void tryToOpenFile(File file, String password) {
        try {
            RandomAccessFile f = new RandomAccessFile(file.getAbsoluteFile(), "r");
            byte[] fileData = new byte[(int)f.length()];
            f.read(fileData);
            String decryptedXML = Cryptographer.decrypt(fileData, password);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.mainLinearLayout,
                    FileEntriesFragment.newInstance(decryptedXML, fileData, password),
                    MainActivity.FILE_ENTRIES_FRAGMENT)
                    .addToBackStack(null)
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .commit();

        } catch (BadPaddingException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
            builder.setTitle("Error")
                    .setMessage("Invalid password");
            builder.show();
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
            builder.setTitle("Error")
                    .setMessage(e.getMessage());
            builder.show();
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileWrapper clickedFile = filesBrowserItems.get(position);
        if(clickedFile.getFile().isDirectory() && clickedFile.getFile().canRead())
            setLocation(clickedFile);
        else if (clickedFile.getFile().isFile() && clickedFile.getFile().canRead())
            openFile(clickedFile.getFile());
    }


    private class FileWrapper {

        private File file;

        private String name;

        private boolean isBackElement;

        FileWrapper(File file) {
            this.file = file;
            this.name = file.getName();
        }

        FileWrapper(String file) {
            this(new File(file));
        }

        FileWrapper(File file, String name) {
            this.file = file;
            this.name = name;
            isBackElement = true;
        }

        File getFile() {
            return file;
        }

        @Override
        public String toString() {
            return name;
        }

        public boolean isBackElement() {
            return isBackElement;
        }
    }


    @Override
    public void onBackPressed() {
        if(filesBrowserItems.get(0).isBackElement)
            setLocation(filesBrowserItems.get(0));
        else
            getFragmentManager().popBackStack();
    }
}
