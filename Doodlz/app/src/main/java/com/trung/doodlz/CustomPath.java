package com.trung.doodlz;

import android.graphics.Path;

class CustomPath extends Path {
    private int color;
    private float brushThickness;

    public float getBrushThickness() {
        return brushThickness;
    }

    public void setBrushThickness(float brushThickness) {
        this.brushThickness = brushThickness;
    }

    public CustomPath(int color, float brushThickness) {
        super();
        this.color = color;
        this.brushThickness = brushThickness;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

}
