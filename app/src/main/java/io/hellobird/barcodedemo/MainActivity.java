package io.hellobird.barcodedemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Arrays;

import io.hellobird.barcode.CaptureActivity;
import io.hellobird.barcode.decode.Decoder;
import io.hellobird.barcode.decode.ImageResizer;
import io.hellobird.barcode.encode.CodeEncoder;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SCAN = 1;

    private static final int REQUEST_PHOTO = 2;

    /**
     * 请求相册权限
     */
    public static final int REQUEST_READ_PERMISSION = 1234;

    /**
     * 请求相机权限
     */
    public static final int REQUEST_CAMERA_PERMISSION = 1235;

    /**
     * 二维码解析
     */
    private Decoder mQRDecoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_barcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermission()) {
                    Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                    startActivityForResult(intent, REQUEST_SCAN);
                }
            }
        });

        findViewById(R.id.btn_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkReadPermission()) {
                    accessStorage();
                }
            }
        });

        findViewById(R.id.btn_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edtContent = findViewById(R.id.edt_content);
                EditText edtSize = findViewById(R.id.edt_size);
                if (TextUtils.isEmpty(edtContent.getText())) {
                    Toast.makeText(MainActivity.this, "请输入生成二维码的内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                String content = edtContent.getText().toString();
                String sizeString = edtSize.getText().toString();
                int size = TextUtils.isEmpty(sizeString) ? 500 : Integer.parseInt(sizeString);

                ImageView imgCode = findViewById(R.id.img_code);
                imgCode.setImageBitmap(CodeEncoder.encodeQR(content, size));
            }
        });

        mQRDecoder = new Decoder(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_128);
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
     * 检查相机权限
     *
     * @return 是否有权限
     */
    private boolean checkCameraPermission() {
        int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        // 如果有权限
        if (readPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
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
                Toast.makeText(this, "请先同意访问相册权限", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_SCAN);
            } else {
                Toast.makeText(this, getString(R.string.camera_permission_hint), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //访问文件相册信息
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
        TextView textView = findViewById(R.id.tv_result);
        if (requestCode == REQUEST_SCAN) {
            if (resultCode == RESULT_OK && data != null) {
                String result = data.getStringExtra(CaptureActivity.EXTRA_BARCODE);
                textView.setText("扫描结果为：" + result);
            }
        } else if (requestCode == REQUEST_PHOTO) {
            if (resultCode == RESULT_OK && data != null) {
                Uri result = data.getData();
                if (result != null) {
                    String path = uri2filePath(result, this);
                    Bitmap bitmap = ImageResizer.decodeSampledBitmapFromFile(path, 500, 500);
                    ImageView imgCode = findViewById(R.id.img_code);
                    imgCode.setImageBitmap(bitmap);
                    Result codeResult = mQRDecoder.decodeBitmap(bitmap);
                    if (codeResult == null) {
                        textView.setText("解析图片失败");
                        Toast.makeText(this, "解析图片失败", Toast.LENGTH_SHORT).show();
                    } else {
                        textView.setText("扫描结果为：" + codeResult.getText());
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
