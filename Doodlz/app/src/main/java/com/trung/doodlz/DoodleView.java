package com.trung.doodlz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
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
    private boolean smoothStrokes;
    private String isPaint = "pen";

    private float startX;
    private float startY;

//  kiem tra mau background
    private Boolean checkBackground = false;
    private int backgroundColor;

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

        backgroundColor = Color.WHITE;
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
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
//        bitmap.eraseColor(Color.WHITE); // erase the Bitmap with white
        bitmap.eraseColor(backgroundColor);
    }

    // clear the painting
    public void clear() {
        paths.clear();
        undonePaths.clear();
//        bitmap.eraseColor(Color.WHITE); // clear the bitmap
        bitmap.eraseColor(backgroundColor);
        invalidate(); // refresh the screen
    }

    // set the painted line's color
    public void setDrawingColor(int color) {

        if (!checkBackground){
            paintLine.setColor(color);
            previousColor = color;
        }
        else {
            bitmap.eraseColor(color);
            backgroundColor = color;
            checkBackground = false;
        }
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
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked(); // event type
        int actionIndex = event.getActionIndex(); // pointer (i.e., finger)

        // determine whether touch started, ended or is moving
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            touchStarted(event.getX(actionIndex), event.getY(actionIndex));
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            switch (isPaint) {
                case "pen":
                    touchEnded(event.getX(actionIndex), event.getY(actionIndex));
                    break;
                case "line":
                    touch_up_line(event.getX(actionIndex), event.getY(actionIndex));
                    break;
                case "rect":
                    touch_up_rect(event.getX(actionIndex), event.getY(actionIndex));
                    break;
                case "circle":
                    touch_up_circle(event.getX(actionIndex), event.getY(actionIndex));
                    break;
                case "oval":
                    touch_up_oval(event.getX(actionIndex), event.getY(actionIndex));
                    break;
                case "square":
                    touch_up_square(event.getX(actionIndex), event.getY(actionIndex));
                    break;
            }
        } else {
            touchMoved(event.getX(actionIndex), event.getY(actionIndex));
        }


        invalidate(); // redraw
        return true;
    }

    // called when the user touches the screen
    private void touchStarted(float touchX , float touchY) {
        drawPath.setColor(paintLine.getColor());
        drawPath.setBrushThickness(paintLine.getStrokeWidth());

        //undonePaths.clear();
        drawPath.reset();
        drawPath.moveTo(touchX, touchY);

        startX = touchX;
        startY = touchY;
    }
    // called when the user drags along the screen
    private void touchMoved(float touchX , float touchY) {
        drawPath.lineTo(touchX, touchY);
    }

    // called when the user finishes a touch
    private void touchEnded(float touchX , float touchY) {

        if(smoothStrokes && !erase) {
            drawPath.reset();
            drawPath.moveTo(startX, startY);
            drawPath.lineTo(touchX, touchY);
        }

        if (erase){
            paintLine.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }

        paths.add(drawPath);
        drawPath = new CustomPath(previousColor,getLineWidth());

        drawPath.reset();
        paintLine.setXfermode(null);
    }

    // ve duong thang
    private void touch_up_line(float x,float y) {
        drawPath.lineTo(x, y);
        drawPath.reset();

        drawPath.moveTo(startX, startY);
        drawPath.lineTo(x, y);
        paths.add(drawPath);
        drawPath = new CustomPath(previousColor,getLineWidth());

        drawPath.reset();
    }

    // ve hinh chu nhat
    private void touch_up_rect(float x,float y) {
        drawPath.lineTo(x, y);
        drawPath.reset();

        if (startX > x && startY > y){
            drawPath.addRect(x, y, startX, startY, Path.Direction.CW);
        }
        else if (startX > x && startY < y){
            drawPath.addRect(x, startY, startX, y, Path.Direction.CW);
        }
        else
        if (startX < x && startY > y){
            drawPath.addRect(startX, y, x, startY, Path.Direction.CW);
        }
        else{
            drawPath.addRect(startX, startY, x, y, Path.Direction.CW);
        }

        paths.add(drawPath);
        drawPath = new CustomPath(previousColor,getLineWidth());

        drawPath.reset();
    }

    // ve hinh vuông
    private void touch_up_square(float x,float y) {
        drawPath.lineTo(x, y);
        drawPath.reset();

        if (startX > x && startY > y){
            drawPath.addRect(startX + y - startY , y ,startX , startY ,Path.Direction.CW);
        }
        else if (startX > x && startY < y){
            drawPath.addRect(startX - y + startY , startY, startX, y, Path.Direction.CW);
        }
        else
        if (startX < x && startY > y){
            drawPath.addRect(startX, y, startX - y + startY, startY, Path.Direction.CW);
        }
        else{
            drawPath.addRect(startX , startY , startX + y - startY , y ,Path.Direction.CW);
        }

        paths.add(drawPath);
        drawPath = new CustomPath(previousColor,getLineWidth());

        drawPath.reset();
    }

    // ve hinh tron
    private void touch_up_circle(float x,float y) {
        drawPath.lineTo(x, y);
        drawPath.reset();

        float cx = (startX + x )/2;
        float cy = (startY + y )/2;

        // tam giac ABC vuong cân tại A , H la duong cao = ban kinh
        float AB = (float) Math.sqrt((cx - startX)*(cx - startX) + (cy - startY)*(cy - startY));
        float radius = (float) Math.sqrt(2) * AB / 2;

        drawPath.addCircle(cx,cy,radius,Path.Direction.CW);
        paths.add(drawPath);
        drawPath = new CustomPath(previousColor,getLineWidth());

        drawPath.reset();
    }

    // ve hinh oval
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void touch_up_oval(float x, float y) {
        drawPath.lineTo(x, y);
        drawPath.reset();

        drawPath.addOval(startX,startY,x,y,Path.Direction.CW);
        paths.add(drawPath);
        drawPath = new CustomPath(previousColor,getLineWidth());

        drawPath.reset();
    }

    // save the current image to the Gallery
    public void saveImage(Bitmap bm) {
        // use "Doodlz" followed by current time as the image name
        final String name = "Doodlz_" + System.currentTimeMillis() + ".jpg";

        // insert the image on the device
        MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bm, name, "Doodlz Drawing");

        Toast.makeText(getContext(),"Đã lưu!!!",Toast.LENGTH_SHORT).show();
    }

    public void onClickUndo () {
        if (paths.size() > 0) {
            clearDraw();
            undonePaths.add(paths.remove(paths.size() - 1));
            invalidate();
        }
        else Toast.makeText(getContext(), "Empty", Toast.LENGTH_SHORT).show();
    }

    public void onClickRedo () {
        if (undonePaths.size() > 0) {
            clearDraw();
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            invalidate();
        }
        else Toast.makeText(getContext(), "Empty", Toast.LENGTH_SHORT).show();
    }

    public void clearDraw() {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas.setBitmap(bitmap);
//        bitmap.eraseColor(Color.WHITE);
        bitmap.eraseColor(backgroundColor);
        invalidate();
    }

    public void setEraser(Boolean isErase) {
        //set erase true or false
        erase = isErase;
        if(erase) {
            paintLine.setColor(backgroundColor);
            paintLine.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
        else {
            paintLine.setColor(previousColor);
            paintLine.setXfermode(null);
        }
    }

    public void setPaint(String style) {
        isPaint = style;
    }

    public void setBackgroundColor(Boolean b){
        checkBackground = b;
    }
}