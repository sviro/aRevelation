package com.github.marmalade.aRevelation;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;


/**
 * Author: <a href="mailto:alexey.kislin@gmail.com">Alexey Kislin</a>
 * Date: 5/29/13
 * Time: 11:26 PM
 */
public class MainActivity extends Activity {

    public final static String MAIN_MENU_FRAGMENT       = "MainMenuFragment";
    public final static String OPEN_FILE_FRAGMENT       = "OpenFileFragment";
    public final static String FILE_ENTRIES_FRAGMENT    = "FileEntriesFragment";
    public final static String ENTRY_FRAGMENT           = "EntryFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        if(savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.mainLinearLayout, new MainMenuFragment(), MAIN_MENU_FRAGMENT);
            fragmentTransaction.commit();
        }
    }


    @Override
    public void onBackPressed() {
        Fragment currentFragment = getCurrentFragment();
        if(currentFragment instanceof IBackPressedListener) {
            ((IBackPressedListener) currentFragment).OnBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }


    private Fragment getCurrentFragment() {
        Fragment myFragment = getFragmentManager().findFragmentByTag(MAIN_MENU_FRAGMENT);
        if (myFragment.isVisible()) return myFragment;
        myFragment = getFragmentManager().findFragmentByTag(OPEN_FILE_FRAGMENT);
        if (myFragment.isVisible()) return myFragment;
        myFragment = getFragmentManager().findFragmentByTag(FILE_ENTRIES_FRAGMENT);
        if (myFragment.isVisible()) return myFragment;
        myFragment = getFragmentManager().findFragmentByTag(ENTRY_FRAGMENT);
        if (myFragment.isVisible()) return myFragment;
        else return null;

    }

}
