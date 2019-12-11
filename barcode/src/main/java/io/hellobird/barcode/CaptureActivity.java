package io.hellobird.barcode;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import io.hellobird.barcode.decode.Decoder;
import io.hellobird.barcode.decode.ImageResizer;

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
     * 请求相册权限
     */
    public static final int REQUEST_READ_PERMISSION = 1234;

    /**
     * 请求获取相册图片
     */
    private static final int REQUEST_PHOTO = 1;

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

    /**
     * 二维码解析类
     */
    private Decoder mQRDecoder;

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
        findViewById(R.id.imgBtn_photo).setOnClickListener(this);

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
        } else if (id == R.id.imgBtn_photo) {
            if (checkReadPermission()) {
                accessStorage();
            }
        }
    }

    /**
     * 检查读权限
     *
     * @return 是否权限
     */
    private boolean checkReadPermission() {
        int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        // 如果有权限
        if (readPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_PERMISSION);
        return false;
    }

    /**
     * 请求权限返回
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                accessStorage();
            } else {
                Toast.makeText(this, getString(R.string.select_image_permission_hint), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 访问文件或相册
     */
    private void accessStorage() {
        String acceptType = "image/*";
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(acceptType);
        startActivityForResult(intent, REQUEST_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PHOTO) {
            if (resultCode == RESULT_OK && data != null) {
                Uri result = data.getData();
                if (result != null) {
                    String path = uri2filePath(result, this);
                    Bitmap bitmap = ImageResizer.decodeSampledBitmapFromFile(path, 500, 500);
                    if (mQRDecoder == null) {
                        mQRDecoder = new Decoder(BarcodeFormat.QR_CODE);
                    }
                    Result codeResult = mQRDecoder.decodeBitmap(bitmap);
                    if (codeResult == null) {
                        Toast.makeText(this, getString(R.string.parse_failed_from_photo), Toast.LENGTH_SHORT).show();
                    } else {
                        onCapture(codeResult.getText(), bitmap);
                    }
                }
            }
        }
    }

    /**
     * 将返回的uri转换为path
     *
     * @param uri     图片Uri
     * @param context 上下文，用于访问contentProvider
     * @return 图片文件地址
     */
    public static String uri2filePath(Uri uri, Context context) {
        String path = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT
                || !DocumentsContract.isDocumentUri(context, uri)) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri,
                    projection, null, null, null);
            if (cursor == null) {
                return "";
            }
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
        }
        // 版本4.4之后使用最近文件打开的图片处理方法有变化
        else if (DocumentsContract.isDocumentUri(context, uri)) {
            String wholeID = DocumentsContract.getDocumentId(uri);
            String id = wholeID.split(":")[1];
            String[] column = {MediaStore.Images.Media.DATA};
            String sel = MediaStore.Images.Media._ID + "=?";
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel,
                    new String[]{id}, null);
            if (cursor == null) {
                return "";
            }
            int columnIndex = cursor.getColumnIndex(column[0]);
            if (cursor.moveToFirst()) {
                path = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return path;
    }
}
