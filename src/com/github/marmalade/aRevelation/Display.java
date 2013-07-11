package com.github.marmalade.aRevelation;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    private static List<Entry> entries;
    private static ArrayAdapter<Entry> entryArrayAdapter;
    private static ListView lv;

    static void showRevelationEntries(String decryptedXML, MainActivity activity) {
        try {
            activity.setContentView(R.layout.decrypted_file_layout);
            activity.status = MainActivity.MenuStatus.DecryptedEntriesDisplay;
            lv = (ListView)activity.findViewById(R.id.itemsListView);
            entries = Entry.parseDecryptedXml(decryptedXML);
            entryArrayAdapter = new ArrayAdapter<Entry>(activity, android.R.layout.simple_list_item_1, entries);
            lv.setAdapter(entryArrayAdapter);
            entryArrayAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class Entry {

        public static List<Entry> parseDecryptedXml(String rvlXml) throws Exception {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(rvlXml.getBytes("UTF-8")));

            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName(ENTRY_NODE_NAME);

            List<Entry> result = new ArrayList<>();

            for(int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    result.add(getEntry((Element) node));
                }
            }
            return result;
        }

        private static Entry getEntry(Element elem) throws Exception {

            if(elem.getAttribute(TYPE_ATTRIBUTE).equals(Phone.TYPE)) {
                String name = "", descr = "", updated = "", notes = "";
                NodeList nameL = elem.getElementsByTagName(ENTRY_NODE_NAME);
                HashMap<String, String> fields = new HashMap<String, String>();
                for(int i = 0; i < nameL.getLength(); i++) {
                    if(nameL.item(i).getNodeName().equals(NAME_ATTRIBUTE))
                        name =  nameL.item(i).getTextContent();
                    else if (nameL.item(i).getNodeName().equals(DESCRIPTION_ATTRIBUTE))
                        descr = nameL.item(i).getTextContent();
                    else if (nameL.item(i).getNodeName().equals(UPDATED_ATTRIBUTE))
                        updated = nameL.item(i).getTextContent();
                    else if (nameL.item(i).getNodeName().equals(NOTES_ATTRIBUTE))
                        notes = nameL.item(i).getTextContent();
                    else if (nameL.item(i).getNodeName().equals(FIELD_ATTRIBUTE)) {
                        String fieldName = ( (Element)nameL.item(i)).getAttribute(ID_ATTRIBUTE);
                        String value = nameL.item(i).getTextContent();
                        if(fieldName != null)
                            fields.put(fieldName, value);
                    } else
                        throw new Exception("Unknown node type - " + nameL.item(i).getNodeName());
                }
                return new Phone(name, descr, updated, notes, fields);

            }
            else
                throw new Exception("Unknown type of XML node");
        }
    }

    private static class Phone extends Entry {

        final static String TYPE = "phone";

        private String name;

        Phone(String name,
              String description,
              String updated,
              String notes,
              Map<String, String> fields) {
            this.name = name;

        }

        @Override
        public String toString() {
            return name;
        }
    }



}
