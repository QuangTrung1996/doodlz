package com.trung.doodlz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.provider.MediaStore;
import android.support.v4.print.PrintHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

// custom View for drawing
public class DoodleView extends View {

    private Bitmap bitmap; // drawing area for displaying or saving
    private Canvas bitmapCanvas; // used to to draw on the bitmap
    private final Paint paintScreen; // được sử dụng để vẽ bitmap lên màn hình
    private final Paint paintLine; // được sử dụng để vẽ các đường thẳng lên bitmap

    private CustomPath drawPath;

    private boolean erase;
    public boolean smoothStrokes;

    private float startX;
    private float startY;
    private float endX;
    private float endY;

    private int paintColor = 0xff000000;
    private int previousColor = paintColor;

    // danh sach cac duong da dc ve va cac duong da xoa
    private ArrayList<CustomPath> paths = new ArrayList<>();
    private ArrayList<CustomPath> undonePaths = new ArrayList<>();

    // DoodleView constructor initializes the DoodleView
    public DoodleView(Context context, AttributeSet attrs) {
        super(context, attrs); // pass context to View's constructor
        paintScreen = new Paint(); // used to display bitmap onto screen

        // set the initial display settings for the painted line
        paintLine = new Paint();
        paintLine.setAntiAlias(true); // smooth edges of drawn line
        paintLine.setColor(paintColor); // default color is black
        paintLine.setStyle(Paint.Style.STROKE); // solid line
        paintLine.setStrokeWidth(5); // set the default line width
        paintLine.setStrokeCap(Paint.Cap.ROUND); // rounded line ends

        erase = false;
        smoothStrokes = false;
        drawPath = new CustomPath(previousColor,getLineWidth());
    }

    // perform custom drawing when the DoodleView is refreshed on screen
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);

        for (CustomPath p : paths){
            paintLine.setStrokeWidth(p.getBrushThickness());
            paintLine.setColor(p.getColor());
            canvas.drawPath(p, paintLine);
        }
        if(!drawPath.isEmpty()) {
            paintLine.setStrokeWidth(drawPath.getBrushThickness());
            paintLine.setColor(drawPath.getColor());
            canvas.drawPath(drawPath, paintLine);
        }
    }

    // creates Bitmap and Canvas based on View's size
    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmap.eraseColor(Color.WHITE); // erase the Bitmap with white
    }

    // clear the painting
    public void clear() {

        paths.clear();
        undonePaths.clear();
        bitmap.eraseColor(Color.WHITE); // clear the bitmap
        invalidate(); // refresh the screen
    }

    // set the painted line's color
    public void setDrawingColor(int color) {
        paintLine.setColor(color);
        previousColor = color;
    }

    // return the painted line's color
    public int getDrawingColor() {
        return paintLine.getColor();
    }

    // set the painted line's width
    public void setLineWidth(int width) {
        paintLine.setStrokeWidth(width);
    }

    // return the painted line's width
    public int getLineWidth() {
        return (int) paintLine.getStrokeWidth();
    }

    // handle touch event
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked(); // event type
        int actionIndex = event.getActionIndex(); // pointer (i.e., finger)

        // determine whether touch started, ended or is moving
        if (action == MotionEvent.ACTION_DOWN ||
                action == MotionEvent.ACTION_POINTER_DOWN) {
            touchStarted(event.getX(actionIndex), event.getY(actionIndex));
        }
        else if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_POINTER_UP) {
            touchEnded(event.getX(actionIndex), event.getY(actionIndex));
        }
        else {
            touchMoved(event.getX(actionIndex), event.getY(actionIndex));
        }

        invalidate(); // redraw
        return true;
    }

    // called when the user touches the screen
    private void touchStarted(float touchX , float touchY) {
        drawPath.setColor(paintLine.getColor());
        drawPath.setBrushThickness(paintLine.getStrokeWidth());
        if(smoothStrokes & !erase) {
            startX = touchX;
            startY = touchY;
        }
        //undonePaths.clear();
        drawPath.reset();
        drawPath.moveTo(touchX, touchY);
    }

    // called when the user drags along the screen
    private void touchMoved(float touchX , float touchY) {
        drawPath.lineTo(touchX, touchY);
    }

    // called when the user finishes a touch
    private void touchEnded(float touchX , float touchY) {
        if(smoothStrokes && !erase) {
            endX = touchX;
            endY = touchY;
            drawPath.reset();
            drawPath.moveTo(startX, startY);
            drawPath.lineTo(endX, endY);
        }

        if (erase){
            paintLine.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }

        paths.add(drawPath);
        drawPath = new CustomPath(previousColor,getLineWidth());

        drawPath.reset();
        paintLine.setXfermode(null);
    }

    // save the current image to the Gallery
    public void saveImage() {
        // use "Doodlz" followed by current time as the image name
        final String name = "Doodlz" + System.currentTimeMillis() + ".jpg";

        // insert the image on the device
        String location = MediaStore.Images.Media.insertImage(
                getContext().getContentResolver(), bitmap, name,
                "Doodlz Drawing");

        if (location != null) {
            // display a message indicating that the image was saved
            Toast message = Toast.makeText(getContext(),
                    R.string.message_saved,
                    Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                    message.getYOffset() / 2);
            message.show();
        }
        else {
            // display a message indicating that there was an error saving
            Toast message = Toast.makeText(getContext(),
                    R.string.message_error_saving, Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                    message.getYOffset() / 2);
            message.show();
        }
    }

    // print the current image
    public void printImage() {
        if (PrintHelper.systemSupportsPrint()) {
            // use Android Support Library's PrintHelper to print image
            PrintHelper printHelper = new PrintHelper(getContext());

            // fit image in page bounds and print the image
            printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            printHelper.printBitmap("Doodlz Image", bitmap);
        }
        else {
            // display message indicating that system does not allow printing
            Toast message = Toast.makeText(getContext(),
                    R.string.message_error_printing, Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                    message.getYOffset() / 2);
            message.show();
        }
    }

    public void onClickUndo () {
        undo();
    }

    public void undo() {
        if (paths.size() > 0) {
            clearDraw();
            undonePaths.add(paths.remove(paths.size() - 1));
            invalidate();
        }
        else Toast.makeText(getContext(), "Empty", Toast.LENGTH_SHORT).show();
    }

    public void onClickRedo () {
        redo();
    }

    public void redo() {
        if (undonePaths.size() > 0) {
            clearDraw();
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            invalidate();
        }
    }

    public void clearDraw() {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas.setBitmap(bitmap);
        bitmap.eraseColor(Color.WHITE);
        invalidate();
    }

    public void setEraser(Boolean isErase) {
        //set erase true or false
        erase = isErase;
        if(erase) {
            paintLine.setColor(Color.WHITE);
        }
        else {
            paintLine.setColor(previousColor);
            paintLine.setXfermode(null);
        }
    }
}