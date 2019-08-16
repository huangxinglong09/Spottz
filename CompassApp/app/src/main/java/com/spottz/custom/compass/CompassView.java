package com.spottz.custom.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.spottz.R;


/**
 * Created by Maxime on 2017/7/22.
 */

public class CompassView extends View {
    private static final double CONVERSION_ANGLE_CONST = Math.PI / 180;

    private Paint mOrientationTextPoint;

    private float rotate = 0f;
    private float textOriW = 0, textOriH = 0;


    private int width, height;

    private int orientationTextColor;
    private int orientationTextSize;
    private int oriTextMargin;

    public void setRotate(float rotate) {
        this.rotate = (int) -rotate;
        invalidate();
    }


    public CompassView(Context context) {
        super(context);
        init();

    }

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CompassView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        orientationTextColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        mOrientationTextPoint = new Paint();
        mOrientationTextPoint.setColor(orientationTextColor);
        mOrientationTextPoint.setStrokeWidth(3);
        mOrientationTextPoint.setTextSize(orientationTextSize);
        mOrientationTextPoint.setAntiAlias(true);
        initWH();
    }

    void initSize() {
        orientationTextSize = getContext().getResources().getDimensionPixelSize(R.dimen.fsize_direction);
        oriTextMargin = 40;
    }

    private void initWH() {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        Log.d("lwz", "width: " + width + " ;height :" + height);
        height = width;
        initSize();
        init();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float ax = 360 / 4;
        for (int i = 0; i < 4; i++) {
            float rotateAngle = (ax * i - rotate + 360) % 360;
            float genAngle = (((270 - rotateAngle < 0 ? (270 - rotateAngle + 360) : (270 - rotateAngle)) - rotate) + 360) % 360;

            String strOri;
            if ((int) genAngle == 0) {
                strOri = "N";
                drawOriText(canvas, strOri, rotateAngle);
            } else if (genAngle == 90) {
                strOri = "O";
                drawOriText(canvas, strOri, rotateAngle);
            } else if (genAngle == 180) {
                strOri = "Z";
                drawOriText(canvas, strOri, rotateAngle);
            } else if (genAngle == 270) {
                strOri = "W";
                drawOriText(canvas, strOri, rotateAngle);
            }
        }
    }

    private void drawOriText(Canvas canvas, String strOri, float rotateAngle) {
        canvas.drawText(strOri, getRotatePointX(rotateAngle, oriTextMargin, height / 2) - textOriW / 2, getRotatePointY(rotateAngle, oriTextMargin, height / 2) + textOriH / 2, mOrientationTextPoint);

    }

    private float getRotatePointX(float a, float x, float y) {
        return (float) ((x - width / 2) * Math.cos(CONVERSION_ANGLE_CONST * a) + (y - height / 2) * Math.sin(CONVERSION_ANGLE_CONST * a)) + width / 2;
    }

    private float getRotatePointY(float a, float x, float y) {
        return (float) ((y - height / 2) * Math.cos(CONVERSION_ANGLE_CONST * a) - (x - width / 2) * Math.sin(CONVERSION_ANGLE_CONST * a)) + height / 2;
    }

}
