package com.github.marmalade.aRevelation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Author: <a href="mailto:alexey.kislin@gmail.com">Alexey Kislin</a>
 * Date: 10/1/13
 * Time: 8:21 PM
 */
public class AskPasswordDialogFragment extends DialogFragment {

    private AskPasswordOnClickListener onClickListener;
    EditText editText;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.ask_password_dialog, null);
        builder.setView(v)
                .setPositiveButton("Submit", onClickListener)
                .setNegativeButton("Cancel", onClickListener);
        editText = (EditText) v.findViewById(R.id.inputPasswordEditText);
        return builder.create();
    }


    void setOnClickListener(AskPasswordOnClickListener listener) {
        onClickListener = listener;
    }


    public abstract class AskPasswordOnClickListener implements OnClickListener {

        private String getInputTextValue() {
            return editText.getEditableText().toString();
        }

    }

}
