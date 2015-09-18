package com.github.siyamed.shapeimageview.mask;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.github.siyamed.shapeimageview.R;

@SuppressWarnings("WeakerAccess")
public abstract class PorterImageView extends ImageView {
    private static final String TAG = PorterImageView.class.getSimpleName();

    private static final PorterDuffXfermode PORTER_DUFF_XFERMODE = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);

    private Canvas maskCanvas;
    private Bitmap maskBitmap;
    private Paint maskPaint;

    private Canvas drawableCanvas;
    private Bitmap drawableBitmap;
    private Paint drawablePaint;

    private boolean invalidated = true;
    private boolean square = false;

    public PorterImageView(Context context) {
        super(context);
        setup(context, null, 0);
    }

    public PorterImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs, 0);
    }

    public PorterImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup(context, attrs, defStyle);
    }

    @SuppressWarnings("SameParameterValue")
    public void setSquare(boolean square) {
        this.square = square;
    }

    private void setup(Context context, AttributeSet attrs, int defStyle) {
        if(attrs != null){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShaderImageView, defStyle, 0);
            square = typedArray.getBoolean(R.styleable.ShaderImageView_siSquare, false);
            typedArray.recycle();
        }

        if(getScaleType() == ScaleType.FIT_CENTER) {
            setScaleType(ScaleType.CENTER_CROP);
        }

        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setColor(Color.BLACK);
    }

    public void invalidate() {
        invalidated = true;
        super.invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createMaskCanvas(w, h, oldw, oldh);
    }

    private void createMaskCanvas(int width, int height, int oldw, int oldh) {
        boolean sizeChanged = width != oldw || height != oldh;
        boolean isValid = width > 0 && height > 0;
        if(isValid && (maskCanvas == null || sizeChanged)) {
            maskCanvas = new Canvas();
            maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            maskCanvas.setBitmap(maskBitmap);

            maskPaint.reset();
            paintMaskCanvas(maskCanvas, maskPaint, width, height);

            drawableCanvas = new Canvas();
            drawableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            drawableCanvas.setBitmap(drawableBitmap);
            drawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            invalidated = true;
        }
    }

    protected abstract void paintMaskCanvas(Canvas maskCanvas, Paint maskPaint, int width, int height);

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isInEditMode()) {
            int saveCount = canvas.saveLayer(0.0f, 0.0f, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
            try {
                if (invalidated) {
                    Drawable drawable = getDrawable();
                    if (drawable != null) {
                        invalidated = false;
                        Matrix imageMatrix = getImageMatrix();
                        if (imageMatrix == null){// && mPaddingTop == 0 && mPaddingLeft == 0) {
                            drawable.draw(drawableCanvas);
                        } else {
                            int drawableSaveCount = drawableCanvas.getSaveCount();
                            drawableCanvas.save();
                            drawableCanvas.concat(imageMatrix);
                            drawable.draw(drawableCanvas);
                            drawableCanvas.restoreToCount(drawableSaveCount);
                        }

                        drawablePaint.reset();
                        drawablePaint.setFilterBitmap(false);
                        drawablePaint.setXfermode(PORTER_DUFF_XFERMODE);
                        drawableCanvas.drawBitmap(maskBitmap, 0.0f, 0.0f, drawablePaint);
                    }
                }

                if (!invalidated) {
                    drawablePaint.setXfermode(null);
                    canvas.drawBitmap(drawableBitmap, 0.0f, 0.0f, drawablePaint);
                }
            } catch (Exception e) {
                String log = "Exception occured while drawing " + getId();
                Log.e(TAG, log, e);
            } finally {
                canvas.restoreToCount(saveCount);
            }
        } else {
            super.onDraw(canvas);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(square) {
            int width = getMeasuredWidth();
            int height = getMeasuredHeight();
            int dimen = Math.min(width, height);
            setMeasuredDimension(dimen, dimen);
        }
    }
}