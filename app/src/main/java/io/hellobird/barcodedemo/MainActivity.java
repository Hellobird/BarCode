package io.hellobird.barcodedemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.hellobird.barcode.CaptureActivity;
import io.hellobird.barcode.encode.CodeEncoder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_barcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, 0);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String result = data.getStringExtra(CaptureActivity.EXTRA_BARCODE);
            TextView textView = findViewById(R.id.tv_result);
            textView.setText("扫描结果为：" + result);
        }
    }
}
