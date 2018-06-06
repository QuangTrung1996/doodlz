package com.trung.doodlz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.support.v4.app.DialogFragment;

// class for the select color dialog
public class ColorDialogFragment extends DialogFragment{
    private SeekBar alphaSeekBar;
    private SeekBar redSeekBar;
    private SeekBar greenSeekBar;
    private SeekBar blueSeekBar;
    private View colorView;
    private int color;

    //create an Alert Dialog and return it
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // create Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View colorDialogView = getActivity().getLayoutInflater().inflate(R.layout.fragment_color,null);
        builder.setView(colorDialogView);

        // set the AlertDialog's message
        builder.setTitle(R.string.title_color_dialog);

        // get the color seekbar and set their onChange listeners
        alphaSeekBar = (SeekBar) colorDialogView.findViewById(R.id.alphaSeekBar);
        redSeekBar = (SeekBar) colorDialogView.findViewById(R.id.redSeekBar);
        greenSeekBar = (SeekBar) colorDialogView.findViewById(R.id.greenSeekBar);
        blueSeekBar = (SeekBar) colorDialogView.findViewById(R.id.blueSeekBar);
        colorView = colorDialogView.findViewById(R.id.colorView);

        //register SeekBar event listeners
        alphaSeekBar.setOnSeekBarChangeListener(colorChangedListener);
        redSeekBar.setOnSeekBarChangeListener(colorChangedListener);
        greenSeekBar.setOnSeekBarChangeListener(colorChangedListener);
        blueSeekBar.setOnSeekBarChangeListener(colorChangedListener);


        // use current drawing color to set SeekBar values
        final DoodleView doodleView = getDoodleFragment().getDoodleView();
        color = doodleView.getDrawingColor();
        alphaSeekBar.setProgress(Color.alpha(color));
        redSeekBar.setProgress( Color.red(color));
        greenSeekBar.setProgress( Color.green(color));
        blueSeekBar.setProgress( Color.blue(color));

        // add set color button
        builder.setPositiveButton(R.string.button_set_color,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doodleView.setDrawingColor(color);
                    }
                });

        return builder.create(); //return dialog



    }
    // get a reference to the MainActivityFragment
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
        if(fragment != null)
            fragment.setDialogOnScreen(false);
    }

    // OnSeekBarChangeListener for the SeekBars in the color dialog
    private final SeekBar.OnSeekBarChangeListener colorChangedListener =
            new SeekBar.OnSeekBarChangeListener() {
                // display the updated color
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    if (fromUser) // user, not program, changed SeekBar progress
                    {
                        color = Color.argb(alphaSeekBar.getProgress(),
                                redSeekBar.getProgress(),greenSeekBar.getProgress(),
                                blueSeekBar.getProgress());

                    }
                    colorView.setBackgroundColor(color);
                }



                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            };
}