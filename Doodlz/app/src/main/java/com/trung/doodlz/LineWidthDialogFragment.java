package com.trung.doodlz;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.support.v4.app.DialogFragment;


// class for the Select Line Width dialog
public class LineWidthDialogFragment extends DialogFragment {
    private ImageView widthImageView;
    // create an AlertDialog and return it

    @SuppressLint("CutPasteId")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View lineWidthDialogView = getActivity().getLayoutInflater().inflate(
                R.layout.fragment_line_width, null);
        builder.setView(lineWidthDialogView); // add GUI to dialog

        // set the AlertDialog's message
        builder.setTitle(R.string.title_line_width_dialog);

        // get the ImageView
        widthImageView = (ImageView) lineWidthDialogView.findViewById(R.id.imageView);

        // configure widthSeekBar
        final DoodleView doodleView = getDoodleFragment().getDoodleView();
        final SeekBar widthSeekBar = (SeekBar) lineWidthDialogView.findViewById(R.id.seekBar);
        widthSeekBar.setOnSeekBarChangeListener(lineWidthChanged);
        widthSeekBar.setProgress(doodleView.getLineWidth());

        // add Set Line Width Button
        builder.setPositiveButton(R.string.button_set_line_width,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doodleView.setLineWidth(widthSeekBar.getProgress());

                    }
                });
        return builder.create(); // return dialog


    }

    // return a reference to the MainActivityFragment
    private MainActivityFragment getDoodleFragment() {
        return (MainActivityFragment) getFragmentManager().findFragmentById(
                R.id.doodleFragment);
    }

    // tell MainActivityFragment that dialog is now displayed
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        MainActivityFragment fragment = getDoodleFragment();
        if (fragment != null) {
            fragment.setDialogOnScreen(true);
        }
    }

    // tell MainActivityFragment that dialog is no longer displayed
    @Override
    public void onDetach() {
        super.onDetach();
        MainActivityFragment fragment = getDoodleFragment();
        if (fragment != null) {
            fragment.setDialogOnScreen(false);
        }

    }

    // OnSeekBarChangeListener for the SeekBar in the width dialog
    private final SeekBar.OnSeekBarChangeListener lineWidthChanged =
            new SeekBar.OnSeekBarChangeListener() {
                final Bitmap bitmap = Bitmap.createBitmap(
                        400, 100, Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap); // draws into bitmap


                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    // configure a Paint object for the current SeekBar value
                    Paint p = new Paint();
                    p.setColor(getDoodleFragment().getDoodleView().getDrawingColor());
                    p.setStrokeCap(Paint.Cap.ROUND);
                    p.setStrokeWidth(progress);

                    // erase the bitmap and redraw the line
                    bitmap.eraseColor(
                            getResources().getColor(android.R.color.transparent,
                                    getContext().getTheme()));
                    canvas.drawLine(30, 50, 370, 50, p);
                    widthImageView.setImageBitmap(bitmap);


                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            };
}