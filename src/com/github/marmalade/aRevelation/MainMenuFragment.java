package com.github.marmalade.aRevelation;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Author: <a href="mailto:alexey.kislin@gmail.com">Alexey Kislin</a>
 * Date: 29.08.13
 * Time: 22:49
 */
public class MainMenuFragment extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_menu_layout, container, false);
    }

    @Override
    public void onStart() {
        getActivity().findViewById(R.id.tv).setOnClickListener(this);
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainLinearLayout, new OpenFileFragment())
        .addToBackStack(null)
        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
        .commit();
    }
}
