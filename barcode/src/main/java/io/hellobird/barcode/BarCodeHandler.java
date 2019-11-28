package io.hellobird.barcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Collection;

import io.hellobird.barcode.camera.CameraManager;
import io.hellobird.barcode.decode.DecodeHandler;
import io.hellobird.barcode.decode.DecodeThread;

/*******************************************************************
 * BarCodeHandler.java  2019-11-27
 * <P>
 * 用于连接 CameraManager 与 DecodeThread 类，<br/>
 * 使相机可以将图像帧传递给解析类解析，同时接收解析类的返回结果<br/>
 * <br/>
 * </p>
 *
 * @author:zhoupeng
 *
 ******************************************************************/
class BarCodeHandler extends Handler {

    /**
     * 相机管理类
     */
    private CameraManager mCameraManager;
    /**
     * 解析线程
     */
    private DecodeThread mDecodeThread;

    /**
     * 成功回调
     */
    private Callback mCallback;

    /**
     * @param context       上下文
     * @param cameraManager 相机管理
     * @param decodeFormats 解析类型
     * @param callback      回调
     */
    BarCodeHandler(Context context, CameraManager cameraManager, Collection<BarcodeFormat> decodeFormats, Callback callback) {
        mCameraManager = cameraManager;
        mCallback = callback;
        mDecodeThread = new DecodeThread(context, cameraManager, this, decodeFormats, null, null, null);
        mDecodeThread.start();
    }

    /**
     * 销毁
     */
    void destroy() {
        if (mDecodeThread.isAlive()) {
            Message quit = Message.obtain(mDecodeThread.getHandler(), DecodeHandler.Msg.quit);
            quit.sendToTarget();
        }
        try {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            mDecodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }
        removeMessages(DecodeHandler.Msg.decode_succeeded);
        removeMessages(DecodeHandler.Msg.decode_failed);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case DecodeHandler.Msg.decode_succeeded: // 解析成功
                mCameraManager.stopPreview();
                Bundle bundle = msg.getData();
                Bitmap barcode = null;
                float scaleFactor = 1.0f;
                if (bundle != null) {
                    byte[] compressedBitmap = bundle.getByteArray(DecodeThread.BARCODE_BITMAP);
                    if (compressedBitmap != null) {
                        barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
                        // Mutable copy:
                        barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
                    }
                    scaleFactor = bundle.getFloat(DecodeThread.BARCODE_SCALED_FACTOR);
                }
                if (mCallback != null) {
                    mCallback.onSuccess((Result) msg.obj, barcode, scaleFactor);
                }
                break;
            case DecodeHandler.Msg.decode_failed: // 解析失败时，重新获取一帧
                mCameraManager.requestPreviewFrame(mDecodeThread.getHandler(), DecodeHandler.Msg.decode);
                break;
        }
    }

    public void restartPreviewAndDecode() {
        mCameraManager.startPreview();
        mCameraManager.requestPreviewFrame(mDecodeThread.getHandler(), DecodeHandler.Msg.decode);
    }

    /**
     * 解析成功的回调
     */
    interface Callback {
        /**
         * 解析成功
         *
         * @param result      结果
         * @param barcode     二维码图片
         * @param scaleFactor 缩放尺寸
         */
        void onSuccess(Result result, Bitmap barcode, float scaleFactor);
    }
}
