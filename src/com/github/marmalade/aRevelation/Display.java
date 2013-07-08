package com.github.marmalade.aRevelation;

/**
 * Author: <a href="mailto:alexey.kislin@gmail.com">Alexey Kislin</a>
 * Date: 7/7/13
 * Time: 11:46 AM
 */
class Display {
    static void showRevelationEntries(String decryptedXML, MainActivity activity) {
        try {
            activity.setContentView(R.layout.decrypted_file_layout);
            activity.status = MainActivity.MenuStatus.DecryptedEntriesDisplay;
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
