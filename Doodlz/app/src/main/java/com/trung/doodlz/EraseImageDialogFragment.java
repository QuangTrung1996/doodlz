package com.trung.doodlz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

// class for the EraseImage dialog
public class EraseImageDialogFragment extends DialogFragment {
    // create an alert dialog and return it

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // set the alert dialog's message
        builder.setMessage(R.string.message_erase);

        //add erase button
        builder.setPositiveButton(R.string.button_erase,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDoodleFragment().getDoodleView().clear(); // clear image
                    }
                });
        // add cancel button
        builder.setNegativeButton(android.R.string.cancel, null);
        return builder.create(); // return dialog

    }
    // gets a reference to the MainActivityFragment
    private MainActivityFragment getDoodleFragment(){
        return (MainActivityFragment) getFragmentManager().findFragmentById(R.id.doodleFragment);
    }

    // tell MainActivityFragment that dialog is now displayed

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        MainActivityFragment fragment = getDoodleFragment();

        if (fragment != null){
            fragment.setDialogOnScreen(true);
        }

    }
    // tell MainActivityFragment that dialog is no longer displayed

    @Override
    public void onDetach() {
        super.onDetach();
        MainActivityFragment fragment = getDoodleFragment();

        if ( fragment != null){
            fragment.setDialogOnScreen(false);

        }
    }

}
