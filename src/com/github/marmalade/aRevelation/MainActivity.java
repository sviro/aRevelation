package com.github.marmalade.aRevelation;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;


/**
 * Author: <a href="mailto:alexey.kislin@gmail.com">Alexey Kislin</a>
 * Date: 5/29/13
 * Time: 11:26 PM
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        MainMenuFragment mmf = new MainMenuFragment();
        fragmentTransaction.add(R.id.mainLinearLayout, mmf);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        getFragmentManager().popBackStack();
    }
}
