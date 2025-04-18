package com.nimo.stickerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MotionEventCompat;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.nimo.fb_effect.R;
import com.nimo.facebeauty.FBEffect;
import com.nimo.facebeauty.model.FBItemEnum;
import java.util.ArrayList;
import java.util.Collections;
//import miaoyongjun.stickerview.R;
/**
 * Author: miaoyongjun
 * Date : 17/8/1
 */

public class StickerView extends AppCompatImageView {

    private ArrayList<Sticker> mStickers;
    private Paint mStickerPaint;
    private Bitmap btnDeleteBitmap;
    private Bitmap btnRotateBitmap;
    private Sticker currentSticker;
    private PointF lastPoint;
    private TouchState state;

    private int maxStickerCount;
    private float minStickerSizeScale;
    private float imageBeginScale;
    private int closeIcon, rotateIcon;
    private int closeSize, rotateSize;
    private int outLineWidth, outLineColor;
    private int mWidth,mHeight;
    private float mRotation;
    private float degree;
    private boolean isFocus;
    private float[] finalPoints;

    public StickerView(Context context) {
        super(context);
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributes(context, attrs);
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttributes(context, attrs);
        init(context);
    }

    private void setAttributes(Context context, AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.StickerView);
        try {
            imageBeginScale = typedArray.getFloat(R.styleable.StickerView_m_image_init_scale, 0.5f);
            maxStickerCount = typedArray.getInt(R.styleable.StickerView_m_max_count, 20);
            minStickerSizeScale = typedArray.getFloat(R.styleable.StickerView_m_image_min_size_scale, 0.5f);
            closeIcon = typedArray.getResourceId(R.styleable.StickerView_m_close_icon, R.drawable.icon_watermark_delete);
            rotateIcon = typedArray.getResourceId(R.styleable.StickerView_m_rotate_icon, R.drawable.icon_watermark_rotate);
            closeSize = typedArray.getDimensionPixelSize(R.styleable.StickerView_m_close_icon_size, dip2px(context, 20));
            rotateSize = typedArray.getDimensionPixelSize(R.styleable.StickerView_m_rotate_icon_size, dip2px(context, 20));
            outLineWidth = typedArray.getDimensionPixelSize(R.styleable.StickerView_m_outline_width, dip2px(context, 1));
            outLineColor = typedArray.getColor(R.styleable.StickerView_m_outline_color, Color.WHITE);

        } finally {
            typedArray.recycle();
        }
    }

    private void init(Context context) {
        mStickerPaint = new Paint();
        mStickerPaint.setAntiAlias(true);
        mStickerPaint.setStyle(Paint.Style.STROKE);
        mStickerPaint.setStrokeWidth(outLineWidth);
        mStickerPaint.setColor(outLineColor);

        Paint mBtnPaint = new Paint();
        mBtnPaint.setAntiAlias(true);
        mBtnPaint.setColor(Color.BLACK);
        mBtnPaint.setStyle(Paint.Style.FILL);

        mStickers = new ArrayList<>();

        btnDeleteBitmap = BitmapFactory.decodeResource(getResources(), closeIcon);
        btnDeleteBitmap = Bitmap.createScaledBitmap(btnDeleteBitmap, closeSize, closeSize, true);
        btnRotateBitmap = BitmapFactory.decodeResource(getResources(), rotateIcon);
        btnRotateBitmap = Bitmap.createScaledBitmap(btnRotateBitmap, rotateSize, rotateSize, true);

        lastPoint = new PointF();

    }


    public boolean addSticker(@DrawableRes int res) {
        if (mStickers.size() >= maxStickerCount) {
            return false;
        }
        Drawable drawable =
                ContextCompat.getDrawable(getContext(), res);
        return addSticker(drawable);
    }

    public boolean addSticker(Drawable drawable) {
        DrawableSticker drawableSticker = new DrawableSticker(drawable);
        mStickers.add(drawableSticker);
        currentSticker = drawableSticker;
        invalidate();
        return true;
    }

    public void removeSticker(Sticker sticker) {
        mStickers.remove(sticker);
        invalidate();
    }

    public void clearSticker() {
        mStickers.clear();
        invalidate();
    }

    public void updateSticker(float[] points) {
        finalPoints = points;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawStickers(canvas);

    }

    private void drawStickers(Canvas canvas) {
        for (Sticker sticker : mStickers) {
            if (!sticker.isInit()) {
                float imageWidth = imageBeginScale * getMeasuredWidth();
                float imageHeight = imageWidth / sticker.getBitmapScale();
                float minSize = (float) Math.sqrt(imageWidth * imageWidth + imageHeight * imageHeight);
                // sticker.setMinStickerSize(minSize * minStickerSizeScale / 2);
                sticker.getMatrix().postScale(imageWidth / sticker.getWidth(), imageWidth / sticker.getWidth());
                sticker.getMatrix().postTranslate(
                        (getMeasuredWidth() - imageWidth) / 2,
                        (getMeasuredHeight() - imageHeight) / 2);
                sticker.converse();
                sticker.setInit(true);
            }
            // sticker.draw(canvas);
            float[] points = sticker.getDst();
            int W = canvas.getWidth();
            int H = canvas.getHeight();
            float[] newPoints = new float[8];
            // if(finalPoints != points && finalPoints != null){
            //     newPoints[0] = finalPoints[0] / W;
            //     newPoints[1] = finalPoints[1] / H;
            //     newPoints[2] = finalPoints[6] / W;
            //     newPoints[3] = finalPoints[7] / H;
            //     newPoints[4] = finalPoints[4] / W;
            //     newPoints[5] = finalPoints[5] / H;
            //     newPoints[6] = finalPoints[2] / W;
            //     newPoints[7] = finalPoints[3] / H;
            // }else{
            //
            //     newPoints[0] = points[0] / W;
            //     newPoints[1] = points[1] / H;
            //     newPoints[2] = points[6] / W;
            //     newPoints[3] = points[7] / H;
            //     newPoints[4] = points[4] / W;
            //     newPoints[5] = points[5] / H;
            //     newPoints[6] = points[2] / W;
            //     newPoints[7] = points[3] / H;
            //
            // }
                newPoints[0] = points[0] / W;
                newPoints[1] = points[1] / H;
                newPoints[2] = points[6] / W;
                newPoints[3] = points[7] / H;
                newPoints[4] = points[4] / W;
                newPoints[5] = points[5] / H;
                newPoints[6] = points[2] / W;
                newPoints[7] = points[3] / H;
            FBEffect.shareInstance().setWatermarkParam(newPoints[0], newPoints[1], newPoints[2], newPoints[3], newPoints[4], newPoints[5], newPoints[6], newPoints[7]);


            //            FBEffect.shareInstance().setWatermarkParam((float) (points[0] / mWidth), (float) (points[1] / mHeight),width,height,newDegree);


            if (sticker == currentSticker) {
                //不能使用 drawPath  否则图片过大时会导致 Path too large to be rendered into a texture
                float[] dst = currentSticker.getDst();
                // if(finalPoints != dst && finalPoints != null){
                //     canvas.drawLine(finalPoints[0], finalPoints[1], finalPoints[2], finalPoints[3], mStickerPaint);
                //     canvas.drawLine(finalPoints[2], finalPoints[3], finalPoints[4], finalPoints[5], mStickerPaint);
                //     canvas.drawLine(finalPoints[4], finalPoints[5], finalPoints[6], finalPoints[7], mStickerPaint);
                //     canvas.drawLine(finalPoints[6], finalPoints[7], finalPoints[0], finalPoints[1], mStickerPaint);
                //     drawBtn(sticker, canvas,finalPoints);
                // }else{
                //     canvas.drawLine(dst[0], dst[1], dst[2], dst[3], mStickerPaint);
                //     canvas.drawLine(dst[2], dst[3], dst[4], dst[5], mStickerPaint);
                //     canvas.drawLine(dst[4], dst[5], dst[6], dst[7], mStickerPaint);
                //     canvas.drawLine(dst[6], dst[7], dst[0], dst[1], mStickerPaint);
                //     drawBtn(sticker, canvas,dst);
                // }
                    canvas.drawLine(dst[0], dst[1], dst[2], dst[3], mStickerPaint);
                    canvas.drawLine(dst[2], dst[3], dst[4], dst[5], mStickerPaint);
                    canvas.drawLine(dst[4], dst[5], dst[6], dst[7], mStickerPaint);
                    canvas.drawLine(dst[6], dst[7], dst[0], dst[1], mStickerPaint);
                    drawBtn(sticker, canvas,dst);


            }
            finalPoints = points;
        }
    }

    private void drawBtn(Sticker sticker, Canvas canvas, float[] points) {
        canvas.drawBitmap(btnDeleteBitmap,
            points[0] - btnDeleteBitmap.getWidth() / 2,
            points[1] - btnDeleteBitmap.getHeight() / 2,
                null);
        canvas.drawBitmap(btnRotateBitmap,
            points[4] - btnRotateBitmap.getWidth() / 2,
            points[5] - btnRotateBitmap.getHeight() / 2,
                null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float evX = event.getX(0);
        float evY = event.getY(0);
        int action = MotionEventCompat.getActionMasked(event);
        mRotation = StickerUtil.calculateRotation(event);
        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:
                midPoint = calculateMidPoint(event);
                oldDistance = StickerUtil.calculateDistance(event);
                oldRotation = StickerUtil.calculateRotation(event);

                if (touchInsideSticker(event.getX(0), event.getY(0))
                        && touchInsideSticker(event.getX(1), event.getY(1))) {
                    state = TouchState.DOUBLE_TOUCH;
                    isFocus = true;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                if (touchInsideDeleteButton(evX, evY)) {
                    state = TouchState.PRESS_DELETE;
                    isFocus = true;
                    break;
                }
                if (touchInsideRotateButton(evX, evY)) {
                    state = TouchState.PRESS_SCALE_AND_ROTATE;
                    isFocus = true;
                    break;
                }
                if (touchInsideSticker(evX, evY)) {
                    state = TouchState.TOUCHING_INSIDE;
                    isFocus = true;
                } else {
                    state = TouchState.TOUCHING_OUTSIDE;
                    isFocus = false;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                float dx = evX - lastPoint.x;
                float dy = evY - lastPoint.y;
                if (state == TouchState.PRESS_SCALE_AND_ROTATE) {
                    rotateAndScale(evX, evY);
                }
                if (state == TouchState.DOUBLE_TOUCH) {
                    rotateAndScaleDoubleTouch(event);
                }
                if (state == TouchState.TOUCHING_INSIDE) {
                    translate(dx, dy);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (state == TouchState.PRESS_DELETE && touchInsideDeleteButton(evX, evY)) {
                    mStickers.remove(currentSticker);
                    FBEffect.shareInstance().setARItem(FBItemEnum.FBItemWatermark.getValue(),"");
                    currentSticker = null;
                    invalidate();
                    break;
                }
                if (state == TouchState.TOUCHING_INSIDE || state == TouchState.PRESS_SCALE_AND_ROTATE) {
                    break;
                }
                if (state == TouchState.TOUCHING_OUTSIDE) {
                    currentSticker = null;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                oldDistance = 0;
                oldRotation = 0;
                break;
        }
        lastPoint.x = evX;
        lastPoint.y = evY;
        return true;
    }

    private void rotateAndScaleDoubleTouch(MotionEvent event) {
        if (event.getPointerCount() < 2) {
            return;
        }
        //双手旋转时根据映射出的四个点坐标来判断最小值的临界点
        float centerX = (currentSticker.getDst()[0] + currentSticker.getDst()[4]) / 2;
        float centerY = (currentSticker.getDst()[1] + currentSticker.getDst()[5]) / 2;
        float rightBottomX = currentSticker.getDst()[4];
        float rightBottomY = currentSticker.getDst()[5];

        float pathMeasureLength = getPathMeasureLength(centerX, centerY, rightBottomX, rightBottomY);
        float newDistance = StickerUtil.calculateDistance(event);
        float newRotation = StickerUtil.calculateRotation(event);
        if (oldDistance != 0) {
            Matrix matrix = currentSticker.getMatrix();
            //可以放大  不能缩小
            if ((newDistance - oldDistance) > 0) {
                matrix.postScale(newDistance / oldDistance, newDistance / oldDistance, midPoint.x,
                        midPoint.y);
            } else if (pathMeasureLength > currentSticker.getMinStickerSize()) {
                matrix.postScale(newDistance / oldDistance, newDistance / oldDistance, midPoint.x,
                        midPoint.y);
            }
            matrix.postRotate(newRotation - oldRotation, midPoint.x, midPoint.y);
            currentSticker.converse();
        }
        oldDistance = newDistance;
        oldRotation = newRotation;
    }

    private void rotateAndScale(float evX, float evY) {
        float[] src = currentSticker.getRotateSrcPts();
        float[] dst = new float[4];
        float centerX = (currentSticker.getDst()[0] + currentSticker.getDst()[4]) / 2;
        float centerY = (currentSticker.getDst()[1] + currentSticker.getDst()[5]) / 2;


        //获取到触摸点到中心点距离 计算到中心点的距离比例得到X和Y的比例  通过相似三角形计算出最终结果
        float pathMeasureLength = getPathMeasureLength(centerX, centerY, evX, evY);
        if (pathMeasureLength < currentSticker.getMinStickerSize()) {
            evX = currentSticker.getMinStickerSize() * (evX - centerX) / pathMeasureLength + centerX;
            evY = currentSticker.getMinStickerSize() * (evY - centerY) / pathMeasureLength + centerY;
        }
        dst[0] = centerX;
        dst[1] = centerY;
        dst[2] = evX;
        dst[3] = evY;
        PointF mLastDistanceVector = new PointF();
        PointF mDistanceVector = new PointF();
        mLastDistanceVector.set(src[0] - src[2], src[1] - src[3]);
        mDistanceVector.set(centerX - dst[2], centerY - dst[3]);
        degree = calculateDegrees(mLastDistanceVector, mDistanceVector);
        Matrix matrix = currentSticker.getMatrix();
        matrix.reset();
        //并不是将图片从一组点变成另一组点  而是获取这两个组的点变换的matrix
        matrix.setPolyToPoly(src, 0, dst, 0, 2);
        currentSticker.converse();
    }

    private float getPathMeasureLength(float moveX, float moveY, float lineX, float lineY) {
        Path path = new Path();
        path.moveTo(moveX, moveY);
        path.lineTo(lineX, lineY);
        PathMeasure pathMeasure = new PathMeasure(path, false);
        return pathMeasure.getLength();
    }

    private float oldDistance = 0f;
    private float oldRotation = 0f;
    private PointF midPoint = new PointF();

    /**
     * 计算旋转角度
     *
     * @param lastVector
     * @param currentVector
     * @return
     */
    public float calculateDegrees(PointF lastVector, PointF currentVector) {
        float lastDegrees = (float) Math.atan2(lastVector.y, lastVector.x);
        float currentDegrees = (float) Math.atan2(currentVector.y, currentVector.x);
        return (float) Math.toDegrees(currentDegrees - lastDegrees);
    }

    protected PointF calculateMidPoint(@Nullable MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) {
            midPoint.set(0, 0);
            return midPoint;
        }
        try {
            float x = (event.getX(0) + event.getX(1)) / 2;
            float y = (event.getY(0) + event.getY(1)) / 2;
            midPoint.set(x, y);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return midPoint;
    }


    private boolean touchInsideRotateButton(float evX, float evY) {
        return currentSticker != null && new RectF(currentSticker.getDst()[4] - btnRotateBitmap.getWidth() / 2, currentSticker.getDst()[5] - btnRotateBitmap.getHeight() / 2, currentSticker.getDst()[4] + btnRotateBitmap.getWidth() / 2, currentSticker.getDst()[5] + btnRotateBitmap.getHeight() / 2).contains(evX, evY);
    }

    private boolean touchInsideDeleteButton(float evX, float evY) {
        return currentSticker != null && new RectF(currentSticker.getDst()[0] - btnDeleteBitmap.getWidth() / 2, currentSticker.getDst()[1] - btnDeleteBitmap.getHeight() / 2, currentSticker.getDst()[0] + btnDeleteBitmap.getWidth() / 2, currentSticker.getDst()[1] + btnDeleteBitmap.getHeight() / 2).contains(evX, evY);
    }

    private void translate(float dx, float dy) {
        if (currentSticker == null) {
            return;
        }
        Matrix matrix = currentSticker.getMatrix();
        matrix.postTranslate(dx, dy);
        currentSticker.converse();
    }

    private boolean touchInsideSticker(float evX, float evY) {
        for (Sticker sticker : mStickers) {
            Region region = new Region();
            region.setPath(sticker.getBoundPath(), new Region(0, 0, getMeasuredWidth(), getMeasuredHeight()));
            if (region.contains((int) evX, (int) evY)) {
                currentSticker = sticker;
                int index = mStickers.indexOf(currentSticker);
                Collections.swap(mStickers, index, mStickers.size() - 1);
                return true;
            }
        }
        return false;
    }

    public boolean getIsFocus() {
        return isFocus;
    }

    public float[] getFinalDst() {
        return finalPoints;
    }

    private enum TouchState {
        TOUCHING_INSIDE, TOUCHING_OUTSIDE, PRESS_DELETE, PRESS_SCALE_AND_ROTATE, DOUBLE_TOUCH;
    }

    /**
     * 获取屏幕尺寸
     * @param width 屏幕宽度
     * @param height 屏幕高度
     */
    public void setScreenSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int getMaxStickerCount() {
        return maxStickerCount;
    }

    public void setMaxStickerCount(int maxStickerCount) {
        this.maxStickerCount = maxStickerCount;
    }

    public float getMinStickerSizeScale() {
        return minStickerSizeScale;
    }

    public void setMinStickerSizeScale(float minStickerSizeScale) {
        this.minStickerSizeScale = minStickerSizeScale;
    }

    public float getImageBeginScale() {
        return imageBeginScale;
    }

    public void setImageBeginScale(float imageBeginScale) {
        this.imageBeginScale = imageBeginScale;
    }

    public int getCloseIcon() {
        return closeIcon;
    }

    public void setCloseIcon(int closeIcon) {
        this.closeIcon = closeIcon;
    }

    public int getRotateIcon() {
        return rotateIcon;
    }

    public void setRotateIcon(int rotateIcon) {
        this.rotateIcon = rotateIcon;
    }

    public int getCloseSize() {
        return closeSize;
    }

    public void setCloseSize(int closeSize) {
        this.closeSize = closeSize;
    }

    public int getRotateSize() {
        return rotateSize;
    }

    public void setRotateSize(int rotateSize) {
        this.rotateSize = rotateSize;
    }

    public int getOutLineWidth() {
        return outLineWidth;
    }

    public void setOutLineWidth(int outLineWidth) {
        this.outLineWidth = outLineWidth;
    }

    public int getOutLineColor() {
        return outLineColor;
    }

    public void setOutLineColor(int outLineColor) {
        this.outLineColor = outLineColor;
    }

    public int dip2px(Context c, float dpValue) {
        final float scale = c.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public Bitmap saveSticker() {
        currentSticker = null;
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }
}
