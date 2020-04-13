package io.hellobird.barcode.decode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/*******************************************************************
 * Decoder.java  2019-11-29
 * <P>
 * 解析图片中的code<br/>
 * <br/>
 * </p>
 *
 * @author:zhoupeng
 *
 ******************************************************************/
public class Decoder {
    /**
     * 解析类
     */
    public static String TAG = "Decoder";


    /**
     * 实际的解析类
     */
    private final MultiFormatReader mMultiFormatReader;

    public Decoder(BarcodeFormat... formats) {
        this(Arrays.asList(formats), null);
    }

    public Decoder(Collection<BarcodeFormat> decodeFormats, String charset) {
        mMultiFormatReader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        // 如果传入的支持格式为空，则默认添加
        if (decodeFormats == null || decodeFormats.isEmpty()) {
            throw new IllegalArgumentException("请传入指定的解析格式");
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        if (charset != null) {
            hints.put(DecodeHintType.CHARACTER_SET, charset);
        }
        mMultiFormatReader.setHints(hints);
    }

    /**
     * 解析图像源中的码
     *
     * @param source 图像源
     * @return 解析结果
     */
    public Result decode(LuminanceSource source) {
        Result rawResult = null;
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = mMultiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                mMultiFormatReader.reset();
            }
        }
        return rawResult;
    }

    /**
     * 解析bitmap中的码
     *
     * @param bitmap bitmap
     * @return 解析结果
     */
    public Result decodeBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            Log.w(TAG, "解析失败，Bitmap 不能为空");
            return null;
        }
        return decode(buildSourceFromBitmap(bitmap));
    }

    /**
     * bitmap转换为zxing可识别的source
     *
     * @param bitmap bitmap
     * @return source
     */
    private RGBLuminanceSource buildSourceFromBitmap(@NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        return new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), pixels);
    }

    /**
     * 从文件中解析二维码
     *
     * @param filePath 图片Uri
     * @param width    最大宽度，防止读取为bitmap后oom
     * @param height   最大高度，防止读取为Bitmap后oom
     * @return 解析结果
     */
    public Result decodeFile(String filePath, int width, int height) {
        if (TextUtils.isEmpty(filePath)) {
            Log.w(TAG, "解析失败，filePath 不能为空");
            return null;
        }

        Bitmap bitmap = ImageResizer.decodeSampledBitmapFromFile(filePath, width, height);
        if (bitmap != null) {
            decodeBitmap(bitmap);
        } else {
            Log.w(TAG, "图片不能为空");
        }
        return null;
    }

    private static final ColorMatrix BLACK_WHITE = new ColorMatrix(new float[]{
            0.33f, 0.59f, 0.11f, 0, -1,
            0.33f, 0.59f, 0.11f, 0, -1,
            0.33f, 0.59f, 0.11f, 0, -1,
            0.33f, 0.59f, 0, 1, 0
    });

    private static final ColorMatrix DOWN_LIGHT = new ColorMatrix(new float[]{
            1, 0, 0, 0, -110,
            0, 1, 0, 0, -110,
            0, 0, 1, 0, -110,
            0, 0, 0, 1, 0
    });

    private static final ColorMatrix UP_CONTRAST = new ColorMatrix(new float[]{
            6f, 0, 0, 0, -255,
            0, 6f, 0, 0, -255,
            0, 0, 6f, 0, -255,
            0, 0, 0, 1, 0
    });

    /**
     * 创建高对比度图片
     *
     * @param bitmap
     * @return
     */
    public static Bitmap createContrastBitmap(@NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        ColorMatrix cMatrix = new ColorMatrix();
        // 先转成黑白
        cMatrix.postConcat(BLACK_WHITE);
        // 调低亮度
        cMatrix.postConcat(DOWN_LIGHT);
        // 调高对比度
        cMatrix.postConcat(UP_CONTRAST);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return result;
    }
}
