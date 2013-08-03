package com.github.marmalade.aRevelation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: <a href="mailto:alexey.kislin@gmail.com">Alexey Kislin</a>
 * Date: 7/7/13
 * Time: 11:46 AM
 */
public class Display {

    final static String ENTRY_NODE_NAME = "entry";
    final static String TYPE_ATTRIBUTE = "type";
    final static String NAME_ATTRIBUTE = "name";
    final static String DESCRIPTION_ATTRIBUTE = "description";
    final static String UPDATED_ATTRIBUTE = "updated";
    final static String NOTES_ATTRIBUTE = "notes";
    final static String FIELD_ATTRIBUTE = "field";
    final static String ID_ATTRIBUTE = "id";

    static String decryptedXml;
    private static List<Entry> entries;
    private static ArrayAdapter<Entry> entryArrayAdapter;

    private static List<Object> fields;
    private static ArrayAdapter<Object> fieldArrayAdapter;

    private static ListView lv;
    private static MainActivity activity;

    /**
     * Listener of the decrypted entries list
     */
    private static AdapterView.OnItemClickListener entryItemClickListener =
        new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                showRevelationEntry(entries.get(position), activity);
            }
        };

    private static AdapterView.OnItemLongClickListener entryLongClickListener =
        new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int i, long l) {
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
                            ClipData clip = ClipData.newPlainText("pass",entries.get(i).getSecretFieldData());
                            clipboard.setPrimaryClip(clip);
                        }
                    }
                });

                Dialog d = builder.create();
                d.show();
                return false;
            }
        };

    /**
     * Listener of fields of an entry
     */
    private static AdapterView.OnItemClickListener fieldItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //TODO Implement processing of field click
        }
    };

    private static AdapterView.OnItemLongClickListener fieldLongClickListener =
        new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int i, long l) {
                //TODO Implement processing of long field click
                return true;
            }
    };




    static void showRevelationEntries(String decryptedXML, MainActivity activity)   {
        try {
            Display.decryptedXml = decryptedXML;
            Display.activity = activity;
            activity.setContentView(R.layout.decrypted_file_layout);
            activity.status = MainActivity.MenuStatus.DecryptedEntriesDisplay;
            lv = (ListView)activity.findViewById(R.id.itemsListView);
            lv.setOnItemClickListener(entryItemClickListener);
            lv.setOnItemLongClickListener(entryLongClickListener);
            entries = Entry.parseDecryptedXml(decryptedXML);
            entryArrayAdapter = new ArrayAdapter<Entry>(activity, android.R.layout.simple_list_item_1, entries);
            lv.setAdapter(entryArrayAdapter);
            entryArrayAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showRevelationEntry(Entry entry, MainActivity activity) {
        activity.status = MainActivity.MenuStatus.EntryDisplay;
        lv.setOnItemClickListener(fieldItemClickListener);
        lv.setOnItemLongClickListener(fieldLongClickListener);
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
            values.put("First Line", Entry.getFieldName(key));
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


    public static class Entry {

        private String name, description, updated, notes;
        private HashMap<String, String> fields;
        EntryType type;

        private Entry(String name, String description, String updated, String notes, HashMap<String, String> fields, String type) throws Exception {
            this.name = name;
            this.description = description;
            this.updated = updated;
            this.notes = notes;
            this.fields = fields;
            this.type = EntryType.getType(type);
        }

        public static List<Entry> parseDecryptedXml(String rvlXml) throws Exception {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(rvlXml.getBytes("UTF-8")));

            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName(ENTRY_NODE_NAME);

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

            HashMap<String, String> attr = new HashMap();
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
                } else
                        ;//throw new Exception("Unknown node type - " + nameL.item(i).getNodeName());
            }
            return new Entry(name, descr, updated, notes, attr, type);
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

        static String getFieldName(String fieldName) {
            switch (fieldName) {
                case "generic-name"             : return activity.getString(R.string.name);
                case "generic-password"         : return activity.getString(R.string.password);
                case "generic-email"            : return activity.getString(R.string.email);
                case "generic-username"         : return activity.getString(R.string.username);
                case "generic-hostname"         : return activity.getString(R.string.hostname);
                case "generic-port"             : return activity.getString(R.string.port);
                case "generic-location"         : return activity.getString(R.string.location);
                case "generic-pin"              : return activity.getString(R.string.pin);
                case "generic-database"         : return activity.getString(R.string.database);
                case "generic-url"              : return activity.getString(R.string.url);
                case "generic-domain"           : return activity.getString(R.string.domain);
                case "generic-code"             : return activity.getString(R.string.code);
                case "creditcard-cardtype"      : return activity.getString(R.string.cardtype);
                case "creditcard-ccv"           : return activity.getString(R.string.ccv);
                case "creditcard-expirydate"    : return activity.getString(R.string.expirydate);
                case "creditcard-cardnumber"    : return activity.getString(R.string.cardnumber);
                case "phone-phonenumber"        : return activity.getString(R.string.phonenumber);

                default                         : return fieldName;
            }
        }
    }

    static enum EntryType {
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
        phone;

        static EntryType getType(String type) throws Exception {
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

}
