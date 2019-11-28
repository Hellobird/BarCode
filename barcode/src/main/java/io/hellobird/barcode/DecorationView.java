package io.hellobird.barcode;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import io.hellobird.barcode.camera.CameraManager;

/*******************************************************************
 * DecorationView.java  2019-11-27
 * <P>
 * 用于二维码扫描装饰的绘制<br/>
 * <br/>
 * </p>
 *
 * @author:zhoupeng
 *
 ******************************************************************/
public class DecorationView extends View {

    /**
     * 拐角线条长度比率
     */
    private static final float CORNER_LENGTH_RATIO = 0.1f;
    /**
     * 拐角线条粗细，单位dp
     */
    private static final int CORNER_STROKE_WIDTH = 3;

    /**
     * 扫描线粗细，单位dp
     */
    private static final int SCAN_LINE_WIDTH = 2;

    /**
     * 动画持续时间
     */
    private static final long ANIMATION_DURATION = 2000L;

    /**
     * 二维码扫描视图
     */
    private BarCodeView mBarCodeView;

    /**
     * 扫描框画笔
     */
    private Paint mPaint;

    /**
     * 遮罩层颜色
     */
    private int mMaskColor;

    /**
     * 扫描框拐角颜色
     */
    private int mCornerColor;

    /**
     * 扫描框拐角线条粗细
     */
    private int mCornerStrokeWidth;

    /**
     * 扫描线颜色
     */
    private int mScanLineColor;
    /**
     * 扫描线粗细
     */
    private int mScanLineWidth;

    /**
     * 扫描线动画
     */
    private ValueAnimator mAnimator;


    public DecorationView(Context context) {
        this(context, null);

    }

    public DecorationView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DecorationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DecorationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initPaint();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mMaskColor = getResources().getColor(R.color.barcode_default_mask);
        mCornerColor = getResources().getColor(R.color.barcode_default_corner);
        mCornerStrokeWidth = dip2px(getResources(), CORNER_STROKE_WIDTH);
        mScanLineColor = getResources().getColor(R.color.barcode_default_scan_line);
        mScanLineWidth = dip2px(getResources(), SCAN_LINE_WIDTH);
    }

    /**
     * 初始化扫描线动画
     */
    private void initAnimator(int top, int bottom) {
        mAnimator = ValueAnimator.ofInt(top, bottom);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.start();
        computeScroll();
    }

    /**
     * 绑定 BarCodeView
     *
     * @param barCodeView
     */
    public void bindBarCodeView(BarCodeView barCodeView) {
        this.mBarCodeView = barCodeView;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBarCodeView == null || mBarCodeView.getCameraManager() == null) {
            return;
        }
        // 获取相机扫描范围
        CameraManager cameraManager = mBarCodeView.getCameraManager();
        Rect frame = cameraManager.getFramingRect();
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        // 如果此时动画为空，则初始化
        if (mAnimator == null) {
            initAnimator(frame.top + mScanLineWidth / 2, frame.bottom - mScanLineWidth / 2);
        }

        // 绘制背景遮罩层
        canvas.save();
        canvas.clipRect(frame, Region.Op.DIFFERENCE);
        canvas.drawColor(mMaskColor);
        canvas.restore();

        // 绘制扫描线
        mPaint.setColor(mScanLineColor);
        mPaint.setStrokeWidth(mScanLineWidth);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawLine(frame.left, (int) mAnimator.getAnimatedValue(), frame.right, (int) mAnimator.getAnimatedValue(), mPaint);

        // 绘制四个角
        mPaint.setColor(mCornerColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mCornerStrokeWidth);
        canvas.save();
        int length = (int) (frame.width() * CORNER_LENGTH_RATIO);
        int halfStrokeWidth = mCornerStrokeWidth / 2;
        canvas.clipRect(frame.left, frame.top + length, frame.right, frame.bottom - length, Region.Op.DIFFERENCE);
        canvas.clipRect(frame.left + length, frame.top, frame.right - length, frame.bottom, Region.Op.DIFFERENCE);
        canvas.drawRect(frame.left + halfStrokeWidth, frame.top + halfStrokeWidth,
                frame.right - halfStrokeWidth, frame.bottom - halfStrokeWidth, mPaint);
        canvas.restore();

    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mAnimator != null && mAnimator.isRunning()) {
            postInvalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param resources
     * @param dipValue  （DisplayMetrics类中属性density）
     * @return
     */
    public static int dip2px(Resources resources, float dipValue) {
        final float scale = resources.getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
