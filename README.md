# BarCode
集成ZXing的二维码扫描工具库，修复zxing竖屏下对条码扫描不支持的问题，同时增加一些扫描自定义接口

## 使用方式

代码下载后，引入 `barcode` module


### 使用定义好的 CaptureActivity

直接使用 `startActivityForResult` 方法启动 `CaptureActivity`，通过 `onActivityResult` 获取扫描结果


### 通过布局引入

布局添加 `BarcodeView` 增加相机预览界面，通过引入 `DecorationView` 增加扫描框，注意需要将 `DecorationView` 覆盖到 `BarcodeView` 上

**xml 布局中**

```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <io.hellobird.barcode.BarCodeView xmlns:app="http://schemas.android.com/apk/res-auto"
        android:id="@+id/preview_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:scanFrameRatio="0.8"
        app:syncScanFrame="false" />
</FrameLayout>
```

**java 代码中**

```java
  mPreviewView = findViewById(R.id.preview_view);
  mPreviewView.setOnCaptureListener(this);

  mDecorationView = findViewById(R.id.decoration_view);
  mDecorationView.bindBarCodeView(mPreviewView);
````
