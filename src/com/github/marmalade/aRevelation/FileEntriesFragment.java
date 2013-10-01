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

import android.app.*;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.crypto.BadPaddingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: <a href="mailto:alexey.kislin@gmail.com">Alexey Kislin</a>
 * Date: 8/31/13
 * Time: 8:54 PM
 */
public class FileEntriesFragment extends Fragment implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, IBackPressedListener {

    final static String REVELATION_XML_NODE_NAME    = "revelationdata";
    final static String ENTRY_NODE_NAME             = "entry";
    final static String TYPE_ATTRIBUTE              = "type";
    final static String NAME_ATTRIBUTE              = "name";
    final static String DESCRIPTION_ATTRIBUTE       = "description";
    final static String UPDATED_ATTRIBUTE           = "updated";
    final static String NOTES_ATTRIBUTE             = "notes";
    final static String FIELD_ATTRIBUTE             = "field";
    final static String ID_ATTRIBUTE                = "id";
	private static final String DECRYPTED_XML       = "decrypted_xml";
	private static final String ENCRYPTED_XML       = "encrypted_xml";
	private static final String PASSWORD            = "password";

    private String decryptedXML;
    private byte[] encryptedXML;
    private String password;
    private ListView lv;
    private int savedScrollBarPosition;
    private int top;
    private List<Entry> entries;
    private ArrayAdapter<Entry> entryArrayAdapter;
    private Activity activity;
    

    public static FileEntriesFragment newInstance(String decryptedXML, byte[] encryptedXML, String password) {
    	FileEntriesFragment fragment = new FileEntriesFragment();
    	
    	Bundle bundle = new Bundle();
    	bundle.putString(DECRYPTED_XML, decryptedXML);
    	bundle.putString(PASSWORD, password);
        bundle.putByteArray(ENCRYPTED_XML, encryptedXML);
    	fragment.setArguments(bundle);

    	return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	Bundle arguments = getArguments();
    	if (arguments != null) {
		    decryptedXML = arguments.getString(DECRYPTED_XML);
            encryptedXML = arguments.getByteArray(ENCRYPTED_XML);
            password = arguments.getString(PASSWORD);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.activity = getActivity();
        return inflater.inflate(R.layout.decrypted_file_layout, container, false);
    }


    @Override
    public void onStart() {
        lv = (ListView)activity.findViewById(R.id.itemsListView);
        lv.setOnItemClickListener(this);
        lv.setOnItemLongClickListener(this);
        try {
            if(entries == null) {
                entries = Entry.parseDecryptedXml(decryptedXML);
            } else if(entries.isEmpty()) {
                // Blocked state
                if(decryptedXML == null && encryptedXML != null) {
                    restoreAccess();
                } else
                    entries = Entry.parseDecryptedXml(decryptedXML);
            }
            updateEntries();
        } catch (Exception e) {
            //TODO Process error
            e.printStackTrace();
        }
        super.onStart();
        // Restore previous position
        lv.setSelectionFromTop(savedScrollBarPosition, top);
    }


    @Override
    public void onPause() {
        // Save previous position
        savedScrollBarPosition = lv.getFirstVisiblePosition();
        top = (lv.getChildAt(0) == null) ? 0 : lv.getChildAt(0).getTop();
        super.onPause();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // I really don't like this. I will make it a little bit more beautiful later.

        Entry selectedEntry = entryArrayAdapter.getItem(position);
        if(selectedEntry.type == EntryType.folder) {
            try {
                Entry nonReal = new Entry("...", null, null, null, null, EntryType.nonreal.toString(), new ArrayList<Entry>(entries));
                entryArrayAdapter = new ArrayAdapter<Entry>(activity, android.R.layout.simple_list_item_1, new ArrayList<Entry>(selectedEntry.children));
                entryArrayAdapter.insert(nonReal, 0);
                entries = new ArrayList<Entry>();
                entries.add(nonReal);
                entries.addAll(selectedEntry.children);
                lv.setAdapter(entryArrayAdapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
            entryArrayAdapter.notifyDataSetChanged();
        } else if(selectedEntry.type == EntryType.nonreal) {
            entryArrayAdapter = new ArrayAdapter<Entry>(activity, android.R.layout.simple_list_item_1, new ArrayList<Entry>(selectedEntry.children));
            entries = new ArrayList<Entry>(selectedEntry.children);
            lv.setAdapter(entryArrayAdapter);
            entryArrayAdapter.notifyDataSetChanged();
        } else {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.mainLinearLayout,
                    new EntryFragment(selectedEntry, password),
                    MainActivity.ENTRY_FRAGMENT)
                    .addToBackStack(null)
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .commit();
        }
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final ActionsMenuItems[] menuItems = new ActionsMenuItems[] {ActionsMenuItems.copySecretData};
        ArrayAdapter<ActionsMenuItems> menuAdapter = new ArrayAdapter<ActionsMenuItems>(activity,
                android.R.layout.simple_list_item_1, menuItems);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final CharSequence[] items= ActionsMenuItems.getCharSequences();
        builder.setItems(items,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(items[which].equals(ActionsMenuItems.copySecretData.toString())) {
                    ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("pass", entries.get(position).getSecretFieldData());
                    clipboard.setPrimaryClip(clip);
                }
            }
        });

        Dialog d = builder.create();
        d.show();
        return false;
    }


    /**
     * Block access to decrypted data on application exit (home button pressed)
     */
    public void blockAccess() {
        getArguments().remove(DECRYPTED_XML);
        decryptedXML = null;
        entries.clear();
    }


    /**
     * Restore access on application open
     */
    private void restoreAccess() {
        final AskPasswordDialogFragment d = new AskPasswordDialogFragment();

        AskPasswordDialogFragment.AskPasswordOnClickListener dialogClickListener =  d.new AskPasswordOnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        tryToDecrypt(d.editText.getEditableText().toString());
                        updateEntries();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        FileEntriesFragment.this.onBackPressed(); // Go to file menu
                        break;
                }
            }
        };

        d.setOnClickListener(dialogClickListener);
        d.show(getFragmentManager(), null);
    }


    /**
     * Try to decrypt current encrypted data with user's password. If it is failed - it returns to restoreAccess and
     * suggests to enter password again or cancel.
     * @param password User's password
     */
    private void tryToDecrypt(String password) {
        try {
            decryptedXML = Cryptographer.decrypt(encryptedXML, password);
            entries = Entry.parseDecryptedXml(decryptedXML);
        } catch (BadPaddingException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
            builder.setTitle("Error")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            restoreAccess();
                        }
                    })
                    .setMessage("Invalid password");
            builder.show();
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
            builder.setTitle("Error")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            restoreAccess();
                        }
                    })
                    .setMessage(e.getMessage());
            builder.show();
        }
    }


    private void updateEntries() {
        entryArrayAdapter = new ArrayAdapter<Entry>(activity, android.R.layout.simple_list_item_1, entries);
        lv.setAdapter(entryArrayAdapter);
        entryArrayAdapter.notifyDataSetChanged();
    }


    public static class Entry {

        public String name, description, updated, notes;
        public HashMap<String, String> fields;
        EntryType type;
        List<Entry> children;

        private Entry(String name, String description,
                      String updated, String notes,
                      HashMap<String, String> fields, String type) throws Exception {
            this.name = name;
            this.description = description;
            this.updated = updated;
            this.notes = notes;
            this.fields = fields;
            this.type = EntryType.getType(type);
        }

        Entry(String name, String description,
                      String updated, String notes,
                      HashMap<String, String> fields, String type,
                      List<Entry> entries) throws Exception {
            this(name, description, updated, notes, fields, type);
            this.children = entries;
        }

        public static List<Entry> parseDecryptedXml(String rvlXml)
                throws Exception {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(rvlXml.getBytes("UTF-8")));

            doc.getDocumentElement().normalize();
            Element rvlXML = doc.getDocumentElement();
            NodeList nodeList = rvlXML.getChildNodes();


            List<Entry> result = new ArrayList<Entry>();

            for(int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    result.add(getEntry((Element) node));
                }
            }
            return result;
        }

        private static Entry getEntry(Element elem) throws Exception {
            String name = "", descr = "", updated = "", notes = "", type="";
            NodeList nameL = elem.getChildNodes();
            type = elem.getAttribute(TYPE_ATTRIBUTE);

            HashMap<String, String> attr = new HashMap<String, String>();
            List<Entry> children = new ArrayList<Entry>();


            for(int i = 0; i < nameL.getLength(); i++) {
                Node item = nameL.item(i);
                if(item.getNodeName().equals(NAME_ATTRIBUTE))
                    name =  item.getTextContent();
                else if (item.getNodeName().equals(DESCRIPTION_ATTRIBUTE))
                    descr = item.getTextContent();
                else if (item.getNodeName().equals(UPDATED_ATTRIBUTE))
                    updated = item.getTextContent();
                else if (item.getNodeName().equals(NOTES_ATTRIBUTE))
                    notes = item.getTextContent();
                else if (item.getNodeName().equals(FIELD_ATTRIBUTE)) {
                    String fieldName = ( (Element)item).getAttribute(ID_ATTRIBUTE);
                    String value = nameL.item(i).getTextContent();
                    if(fieldName != null)
                        attr.put(fieldName, value);
                } else if(item.getNodeName().equals(ENTRY_NODE_NAME)) {
                    children.add(getEntry((Element) item));
                } else
                    ;//throw new Exception("Unknown node type - " + nameL.item(i).getNodeName());
            }
            if(EntryType.getType(type) == EntryType.folder) {
                return new Entry(name, descr, updated, notes, attr, type, children);
            } else {
                return new Entry(name, descr, updated, notes, attr, type);
            }

        }

        @Override
        public String toString() {
            return name;
        }

        String getSecretFieldData() {
            if(type == EntryType.creditcard)
                return fields.get("generic-pin");
            else if (type == EntryType.door)
                return fields.get("generic-code");
            else if (type == EntryType.phone)
                return fields.get("generic-pin");
            else if (
                    type == EntryType.database
                            || type == EntryType.cryptokey
                            || type == EntryType.email
                            || type == EntryType.generic
                            || type == EntryType.ftp
                            || type == EntryType.remotedesktop
                            || type == EntryType.shell
                            || type == EntryType.vnc
                            || type == EntryType.website)
                return fields.get("generic-password");
            else
                return "";
        }

        static String getFieldName(String fieldName, Activity activity) {
        	if ("generic-name".equals(fieldName)) {
        		return activity.getString(R.string.name);
			}
        	if ("generic-password".equals(fieldName)) {
        		return activity.getString(R.string.password);
        	}
        	if ("generic-email".equals(fieldName)) {
        		return activity.getString(R.string.email);
        	}
        	if ("generic-username".equals(fieldName)) {
        		return activity.getString(R.string.username);
        	}
        	if ("generic-hostname".equals(fieldName)) {
        		return activity.getString(R.string.hostname);
        	}
        	if ("generic-port".equals(fieldName)) {
        		return activity.getString(R.string.port);
        	}
        	if ("generic-location".equals(fieldName)) {
        		return activity.getString(R.string.location);
        	}
        	if ("generic-pin".equals(fieldName)) {
        		return activity.getString(R.string.pin);
        	}
        	if ("generic-database".equals(fieldName)) {
        		return activity.getString(R.string.database);
        	}
        	if ("generic-url".equals(fieldName)) {
        		return activity.getString(R.string.url);
        	}
        	if ("generic-domain".equals(fieldName)) {
        		return activity.getString(R.string.domain);
        	}
        	if ("generic-code".equals(fieldName)) {
        		return activity.getString(R.string.code);
        	}
        	if ("creditcard-cardtype".equals(fieldName)) {
        		return activity.getString(R.string.cardtype);
        	}
        	if ("creditcard-ccv".equals(fieldName)) {
        		return activity.getString(R.string.ccv);
        	}
        	if ("creditcard-expirydate".equals(fieldName)) {
        		return activity.getString(R.string.expirydate);
        	}
        	if ("creditcard-cardnumber".equals(fieldName)) {
        		return activity.getString(R.string.cardnumber);
        	}
        	if ("phone-phonenumber".equals(fieldName)) {
        		return activity.getString(R.string.phonenumber);
        	}
        	
        	return fieldName;
        }
    }


    static enum EntryType {
        folder,
        creditcard,
        cryptokey,
        door,
        database,
        email,
        ftp,
        generic,
        remotedesktop,
        shell,
        vnc,
        website,
        phone,
        nonreal;

        static EntryType getType(String type) throws Exception {
            if(type.equals(EntryType.folder.toString()))
                return EntryType.folder;
            if(type.equals(EntryType.creditcard.toString()))
                return EntryType.creditcard;
            else if(type.equals(EntryType.cryptokey.toString()))
                return EntryType.cryptokey;
            else if (type.equals(EntryType.database.toString()))
                return EntryType.database;
            else if (type.equals(EntryType.door.toString()))
                return EntryType.door;
            else if (type.equals(EntryType.email.toString()))
                return EntryType.email;
            else if (type.equals(EntryType.ftp.toString()))
                return EntryType.ftp;
            else if (type.equals(EntryType.generic.toString()))
                return EntryType.generic;
            else if (type.equals(EntryType.phone.toString()))
                return EntryType.phone;
            else if (type.equals(EntryType.remotedesktop.toString()))
                return EntryType.remotedesktop;
            else if (type.equals(EntryType.shell.toString()))
                return EntryType.shell;
            else if (type.equals(EntryType.vnc.toString()))
                return EntryType.vnc;
            else if (type.equals(EntryType.website.toString()))
                return EntryType.website;
            else if (type.equals(EntryType.nonreal.toString()))
                return EntryType.nonreal;
            else throw new Exception("Unknown type of entry - " + type);
        }

    }


    /**
     * Menu items of entry manipulating
     */
    static enum ActionsMenuItems {
        copySecretData;

        @Override
        public String toString() {
            if(this == ActionsMenuItems.copySecretData)
                return "Copy secret data";
            else
                return super.toString();
        }

        static CharSequence[] getCharSequences() {
            CharSequence[] result = new CharSequence[values().length];
            for(int i = 0; i < values().length; i++) {
                result[i] = values()[i].toString();
            }
            return result;
        }
    }


    @Override
    public void onBackPressed() {
        if(entryArrayAdapter.getCount() > 0 && entryArrayAdapter.getItem(0).type == EntryType.nonreal)
            lv.performItemClick(null, 0, 0);
        else
            getFragmentManager().popBackStack();
    }
}
