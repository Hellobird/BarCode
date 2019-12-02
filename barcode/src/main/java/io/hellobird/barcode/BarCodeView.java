package io.hellobird.barcode;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
     * 默认的扫描框比例
     */
    private static final float DEFAULT_RATIO = 0.8f;

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

    /**
     * 解析模式
     */
    private List<BarcodeFormat> mModeList;

    /**
     * 是否回传bitmap，回调结果时是否生成解析的bitmap
     */
    private boolean mCallBackBitmap;


    public BarCodeView(Context context) {
        this(context, null);
    }

    public BarCodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScanFrameRatio = DEFAULT_RATIO;
        mSyncScanFrame = true;
        initAttrs(attrs);
        getHolder().addCallback(mCallBack);
    }

    /**
     * 初始化View属性
     *
     * @param attrs 属性
     */
    private void initAttrs(@Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.BarCodeView);
        mScanFrameRatio = typedArray.getFloat(R.styleable.BarCodeView_scanFrameRatio, mScanFrameRatio);
        mSyncScanFrame = typedArray.getBoolean(R.styleable.BarCodeView_syncScanFrame, mSyncScanFrame);
        mCallBackBitmap = typedArray.getBoolean(R.styleable.BarCodeView_bitmap, mCallBackBitmap);
        int mode = typedArray.getInt(R.styleable.BarCodeView_mode, 0);
        // 解析模式
        mModeList = new ArrayList<>();
        if ((mode & Mode.AZTEC) > 0) mModeList.add(BarcodeFormat.AZTEC);
        if ((mode & Mode.CODABAR) > 0) mModeList.add(BarcodeFormat.CODABAR);
        if ((mode & Mode.CODE_39) > 0) mModeList.add(BarcodeFormat.CODE_39);
        if ((mode & Mode.CODE_93) > 0) mModeList.add(BarcodeFormat.CODE_93);
        if ((mode & Mode.CODE_128) > 0) mModeList.add(BarcodeFormat.CODE_128);
        if ((mode & Mode.DATA_MATRIX) > 0) mModeList.add(BarcodeFormat.DATA_MATRIX);
        if ((mode & Mode.EAN_8) > 0) mModeList.add(BarcodeFormat.EAN_8);
        if ((mode & Mode.EAN_13) > 0) mModeList.add(BarcodeFormat.EAN_13);
        if ((mode & Mode.ITF) > 0) mModeList.add(BarcodeFormat.ITF);
        if ((mode & Mode.MAXI_CODE) > 0) mModeList.add(BarcodeFormat.MAXICODE);
        if ((mode & Mode.PDF_417) > 0) mModeList.add(BarcodeFormat.PDF_417);
        if ((mode & Mode.QR_CODE) > 0) mModeList.add(BarcodeFormat.QR_CODE);
        if ((mode & Mode.RSS_14) > 0) mModeList.add(BarcodeFormat.RSS_14);
        if ((mode & Mode.RSS_EXPANDED) > 0) mModeList.add(BarcodeFormat.RSS_EXPANDED);
        if ((mode & Mode.UPC_A) > 0) mModeList.add(BarcodeFormat.UPC_A);
        if ((mode & Mode.UPC_EAN_EXTENSION) > 0) mModeList.add(BarcodeFormat.UPC_EAN_EXTENSION);

        typedArray.recycle();
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
                mHandler = new BarCodeHandler(getContext(), mCameraManager, mModeList, this, mCallBackBitmap);
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

    public interface Mode {
        int AZTEC = 0x1;
        int CODABAR = 0x2;
        int CODE_39 = 0x4;
        int CODE_93 = 0x8;
        int CODE_128 = 0x10;
        int DATA_MATRIX = 0x20;
        int EAN_8 = 0x40;
        int EAN_13 = 0x80;
        int ITF = 0x100;
        int MAXI_CODE = 0x200;
        int PDF_417 = 0x400;
        int QR_CODE = 0x800;
        int RSS_14 = 0x1000;
        int RSS_EXPANDED = 0x2000;
        int UPC_A = 0x4000;
        int UPC_EAN_EXTENSION = 0x8000;
    }
}
