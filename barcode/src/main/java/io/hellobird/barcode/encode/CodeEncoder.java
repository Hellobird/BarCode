/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.hellobird.barcode.encode;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;


/**
 * 生成指定码
 */
public final class CodeEncoder {

    private static final String TAG = CodeEncoder.class.getSimpleName();


    /**
     * 默认编码
     */
    private static final String ENCODING = "UTF-8";
    /**
     * 默认背景
     */
    private static final int WHITE = 0xFFFFFFFF;
    /**
     * 默认着色
     */
    private static final int BLACK = 0xFF000000;

    /**
     * 生成指定码
     *
     * @param format          格式
     * @param content         内容
     * @param width           宽度
     * @param height          高度
     * @param encoding        字符编码
     * @param tintColor       编码着色
     * @param backgroundColor 背景色
     * @return 生成的图片
     */
    public static Bitmap encode(@NonNull BarcodeFormat format, @NonNull String content, int width, int height, String encoding, @ColorInt int tintColor, @ColorInt int backgroundColor) {
        if (format == null) {
            Log.w(TAG, "生成格式不能为空");
            return null;
        }
        if (TextUtils.isEmpty(content)) {
            Log.w(TAG, "生成内容不能为空");
            return null;
        }
        if (width <= 0 || height <= 0) {
            Log.w(TAG, "指定的高度或者宽度必须大于0");
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        if (encoding != null) {
            hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(content, format, width, height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
        int outWidth = result.getWidth();
        int outHeight = result.getHeight();
        int[] pixels = new int[outWidth * outHeight];
        for (int y = 0; y < outHeight; y++) {
            int offset = y * outWidth;
            for (int x = 0; x < outWidth; x++) {
                pixels[offset + x] = result.get(x, y) ? tintColor : backgroundColor;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, outWidth, 0, 0, outWidth, outHeight);
        return bitmap;
    }

    /**
     * 生成指定码
     *
     * @param format  格式
     * @param content 内容
     * @param width   宽度
     * @param height  高度
     * @return 生成的图片
     */
    public static Bitmap encode(@NonNull BarcodeFormat format, @NonNull String content, int width, int height) {
        return encode(format, content, width, height, ENCODING, BLACK, WHITE);
    }

    /**
     * 生成二维码
     *
     * @param content 内容
     * @param size    大小
     * @return 生成的图片
     */
    public static Bitmap encodeQR(String content, int size) {
        return encode(BarcodeFormat.QR_CODE, content, size, size);
    }
}
