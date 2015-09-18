package com.github.siyamed.shapeimageview.shader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import com.github.siyamed.shapeimageview.R;

public class BubbleShader extends ShaderHelper {
    private static final int DEFAULT_HEIGHT_DP = 10;

    private enum ArrowPosition {
        @SuppressLint("RtlHardcoded")
        LEFT,
        RIGHT
    }

    private final Path path = new Path();

    private int triangleHeightPx;
    private ArrowPosition arrowPosition = ArrowPosition.LEFT;
    
    private int radius = 0;
    private int bitmapRadius;

    public BubbleShader() {
    }

    @Override
    public void init(Context context, AttributeSet attrs, int defStyle) {
        super.init(context, attrs, defStyle);
        borderWidth = 0;
        if(attrs != null){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShaderImageView, defStyle, 0);
            triangleHeightPx = typedArray.getDimensionPixelSize(R.styleable.ShaderImageView_siTriangleHeight, 0);
            int arrowPositionInt = typedArray.getInt(R.styleable.ShaderImageView_siArrowPosition, ArrowPosition.LEFT.ordinal());
            arrowPosition = ArrowPosition.values()[arrowPositionInt];
            radius = typedArray.getDimensionPixelSize(R.styleable.ShaderImageView_siRadius, radius);
            typedArray.recycle();
        }

        if(triangleHeightPx == 0) {
            triangleHeightPx = dpToPx(context.getResources().getDisplayMetrics(), DEFAULT_HEIGHT_DP);
        }
    }

    @Override
    public void draw(Canvas canvas, Paint imagePaint, Paint borderPaint) {
        canvas.save();
        canvas.concat(matrix);
        
        imagePaint.setColor(Color.RED);  //…Ë÷√ª≠± —’…´         
        imagePaint.setStrokeWidth(5);//…Ë÷√ª≠± øÌ∂»  
        canvas.drawPath(path, imagePaint);
        canvas.restore();
    }

    @Override
    public void calculate(int bitmapWidth, int bitmapHeight,
                          float width, float height,
                          float scale,
                          float translateX, float translateY) {
        path.reset();
        float x = -translateX;
        float y = -translateY;
        float scaledTriangleHeight = triangleHeightPx / scale;
        float resultWidth = bitmapWidth + 2 * translateX;
        float resultHeight = bitmapHeight + 2 * translateY;
        float centerY  = resultHeight / 2f + y;

        path.setFillType(Path.FillType.EVEN_ODD);
        float rectLeft;
        float rectRight;
        bitmapRadius = Math.round(radius / scale);
        switch (arrowPosition) {
            case LEFT:
                rectLeft = scaledTriangleHeight + x;
                rectRight = resultWidth;
                //path.addRect(rectLeft, y, rectRight, resultHeight + y, Path.Direction.CW);
                //path.addRoundRect(rectLeft, y, rectRight, resultHeight + y, 6, 6, Path.Direction.CW);
                RectF rect1 =  new RectF(rectLeft, y, rectRight, resultHeight + y);
                path.addRoundRect(rect1, bitmapRadius, bitmapRadius, Path.Direction.CW);

//                path.moveTo(x, centerY);
//                path.lineTo(rectLeft, centerY - scaledTriangleHeight);
//                path.lineTo(rectLeft, centerY + scaledTriangleHeight);
//                path.lineTo(x, centerY);
                path.moveTo(x, bitmapRadius);
                path.lineTo(rectLeft, bitmapRadius);
                path.lineTo(rectLeft, bitmapRadius * 2);
                //path.lineTo(x, bitmapRadius);
                RectF rect =  new RectF(x-(rectLeft-x), bitmapRadius, rectLeft, bitmapRadius*3);
                path.addArc(rect, 0, -90);
                break;
            case RIGHT:
                rectLeft = x;
                float imgRight = resultWidth + rectLeft;
                rectRight = imgRight - scaledTriangleHeight;
                //path.addRect(rectLeft, y, rectRight, resultHeight + y, Path.Direction.CW);
                RectF rect2 =  new RectF(rectLeft, y, rectRight, resultHeight + y);
                path.addRoundRect(rect2, bitmapRadius, bitmapRadius, Path.Direction.CW);
                
//                path.moveTo(imgRight, centerY);
//                path.lineTo(rectRight, centerY - scaledTriangleHeight);
//                path.lineTo(rectRight, centerY + scaledTriangleHeight);
//                path.lineTo(imgRight, centerY);
                path.moveTo(imgRight, bitmapRadius);
                path.lineTo(rectRight, bitmapRadius);
                path.lineTo(rectRight, bitmapRadius * 2);
                //path.lineTo(x, bitmapRadius);
                RectF rectF =  new RectF(rectRight, bitmapRadius, imgRight*2 - rectRight, bitmapRadius*3);
                path.addArc(rectF, -90, -90);
                break;
        }
    }

    @Override
    public void reset() {
        bitmapRadius = 0;
        path.reset();
    }
}