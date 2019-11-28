package io.hellobird.barcode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/*******************************************************************
 * CaptureActivity.java  2019-11-26
 * <P>
 * 二维码扫描界面<br/>
 * <br/>
 * </p>
 *
 * @author:zhoupeng
 *
 ******************************************************************/
public class CaptureActivity extends AppCompatActivity implements BarCodeView.OnCaptureListener, View.OnClickListener {

    public static final String TAG = "CaptureActivity";

    /**
     * 标题
     */
    public static final String EXTRA_TITLE = "extra_title";

    /**
     * 返回结果
     */
    public static final String EXTRA_BARCODE = "extra_barcode";

    /**
     * 相机预览
     */
    private BarCodeView mPreviewView;

    /**
     * 扫描动画装饰
     */
    private DecorationView mDecorationView;

    /**
     * 返回按钮
     */
    private ImageButton mImgBtnBack;

    /**
     * 标题
     */
    private TextView mTvTitle;

    /**
     * 闪光灯
     */
    private ImageButton mImgBtnFlash;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 保持屏幕常亮
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_capture);

        initView();
    }

    private void initView() {
        mPreviewView = findViewById(R.id.preview_view);
        mPreviewView.setOnCaptureListener(this);

        mDecorationView = findViewById(R.id.decoration_view);
        mDecorationView.bindBarCodeView(mPreviewView);

        mImgBtnBack = findViewById(R.id.imgBtn_back);
        mImgBtnFlash = findViewById(R.id.imgBtn_flash);
        mImgBtnBack.setOnClickListener(this);
        mImgBtnFlash.setOnClickListener(this);

        mTvTitle = findViewById(R.id.tv_title);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (!TextUtils.isEmpty(title)) {
            mTvTitle.setText(title);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPreviewView.openCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreviewView.closeCamera();
    }

    @Override
    public void onCapture(String result, Bitmap barcode) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BARCODE, result);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.imgBtn_back) {
            onBackPressed();
        } else if (id == R.id.imgBtn_flash) {
            boolean newState = !mPreviewView.getTorchState();
            mPreviewView.setTorch(newState);
            if (newState) {
                mImgBtnFlash.setImageResource(R.drawable.ic_flash_on);
            } else {
                mImgBtnFlash.setImageResource(R.drawable.ic_flash_off);
            }
        }
    }
}
