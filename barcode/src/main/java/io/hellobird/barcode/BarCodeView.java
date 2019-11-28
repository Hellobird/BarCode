package io.hellobird.barcode;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.zxing.Result;

import java.io.IOException;

import io.hellobird.barcode.camera.CameraManager;

/*******************************************************************
 * CaptureView.java  2019-11-27
 * <P>
 * 二维码捕获界面<br/>
 * <br/>
 * </p>
 *
 * @author:zhoupeng
 *
 ******************************************************************/
public class BarCodeView extends SurfaceView implements BarCodeHandler.Callback {

    /**
     * 相机管理类
     */
    private CameraManager mCameraManager;

    /**
     * Surface是否已打开
     */
    private boolean mSurfaceEnable;

    /**
     * 解析处理类
     */
    private BarCodeHandler mHandler;

    /**
     * 监听接口
     */
    private OnCaptureListener mOnCaptureListener;

    /**
     * 扫描框大小比例
     */
    private float mScanFrameRatio;
    /**
     * 扫描范围是否与扫描框同步
     */
    private boolean mSyncScanFrame;


    public BarCodeView(Context context) {
        this(context, null);
    }

    public BarCodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BarCodeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(attrs);
        getHolder().addCallback(mCallBack);
    }

    /**
     * 初始化View属性
     *
     * @param attrs
     */
    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.BarCodeView);
        mScanFrameRatio = typedArray.getFloat(R.styleable.BarCodeView_scanFrameRatio, 0.8f);
        mSyncScanFrame = typedArray.getBoolean(R.styleable.BarCodeView_syncScanFrame, true);
    }

    /**
     * 设置闪光灯
     *
     * @param isOpen true 打开，false 关闭
     */
    public void setTorch(boolean isOpen) {
        if (mCameraManager != null) {
            mCameraManager.setTorch(isOpen);
        }
    }

    /**
     * 获取闪光灯状态
     *
     * @return true 打开，false 关闭
     */
    public boolean getTorchState() {
        return mCameraManager != null && mCameraManager.getTorchState();
    }

    /**
     * 打开相机
     */
    public void openCamera() {
        if (mCameraManager == null) {
            mCameraManager = new CameraManager(getContext().getApplicationContext(), mScanFrameRatio, mSyncScanFrame);
        }

        if (mSurfaceEnable) {
            try {
                mCameraManager.openDriver(getHolder());
                mHandler = new BarCodeHandler(getContext(), mCameraManager, null, this);
                // 开始预览与解析
                mHandler.restartPreviewAndDecode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭相机
     */
    public void closeCamera() {
        if (mCameraManager != null) {
            mCameraManager.stopPreview();
            mCameraManager.closeDriver();
            mCameraManager = null;
        }
        if (mHandler != null) {
            mHandler.destroy();
        }
    }

    /**
     * 获取相机管理类
     *
     * @return 相机管理类
     */
    public CameraManager getCameraManager() {
        return mCameraManager;
    }

    /**
     * 设置捕获到结果的回调
     *
     * @param onCaptureListener 回调接口
     */
    public void setOnCaptureListener(OnCaptureListener onCaptureListener) {
        this.mOnCaptureListener = onCaptureListener;
    }

    SurfaceHolder.Callback mCallBack = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceEnable = true;
            openCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mSurfaceEnable = false;
            closeCamera();
        }
    };

    @Override
    public void onSuccess(Result result, Bitmap barcode, float scaleFactor) {
        if (mOnCaptureListener != null) {
            mOnCaptureListener.onCapture(result.getText(), barcode);
        }
    }

    /**
     * 用于监听捕获到二维码的接口
     */
    public interface OnCaptureListener {
        /**
         * @param result 返回结果
         */
        void onCapture(String result, Bitmap barcode);
    }
}
