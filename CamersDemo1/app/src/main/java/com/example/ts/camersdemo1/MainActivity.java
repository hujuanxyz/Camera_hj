package com.example.ts.camersdemo1;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "CameraSimple";

    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout mCameralayout;//布局时可以使用FrameLayout，这样其他的按钮或者元素可以叠加在预览图像上
    private ImageView mTakePictureBtn;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkCameraHardware(this)) {
            Toast.makeText(MainActivity.this, "相机不支持", Toast.LENGTH_SHORT)
                    .show();
        } else {
            //得到一个camer的实例
            mCamera = getCameraInstance();
            mPreview = new CameraPreview(MainActivity.this, mCamera);

            //触屏点击聚焦
            mPreview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mCamera.autoFocus(null);
                    return false;
                }
            });

            mCameralayout = (FrameLayout) findViewById(R.id.camera_preview);
            mCameralayout.addView(mPreview);

            mTakePictureBtn = (ImageView) findViewById(R.id.button_capture);
            mTakePictureBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //mCamera.takePicture(null, null, mPictureCallback);
                    //为拍照按钮添加聚焦，将原来的拍照放到聚焦成功后再调用
                    mCamera.autoFocus(mAutoFocusCallback);
                }
            });
            setCameraDisplayOrientation(this, mCameraId, mCamera);
        }
    }



    // 拍照回调
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            saveFile(data);
        }
    };


    // 聚焦回调
    private Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                mCamera.takePicture(null, null, mPictureCallback);
            }
        }
    };

    //保存照片
    private static final String PATH_IMAGES = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "easy_hj";
    public void saveFile(byte[] data) {
        //String fileName = UUID.randomUUID().toString() + ".jpg";
        String fileName =  new DateFormat().format("yyyyMMddHHmmss",
                new Date()).toString() + ".jpg";
        FileOutputStream outputStream = null;
        try {
            File file = new File(PATH_IMAGES);
            if (!file.exists()) {
                file.mkdirs();
            }
            outputStream = new FileOutputStream(PATH_IMAGES + File.separator + fileName);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            bufferedOutputStream.write(data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 判断相机是否支持
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    // 获取相机
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    // 释放相机
    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    // 设置相机横竖屏
    public void setCameraDisplayOrientation(Activity activity, int cameraId,
                                            Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }
}
