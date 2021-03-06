package ng.com.piccmaq.flutter.flutter_mobile_vision_2.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import ng.com.piccmaq.flutter.flutter_mobile_vision_2.R;
import ng.com.piccmaq.flutter.flutter_mobile_vision_2.ocr.MyTextBlock;
import ng.com.piccmaq.flutter.flutter_mobile_vision_2.ocr.OcrCaptureActivity;
import ng.com.piccmaq.flutter.flutter_mobile_vision_2.ui.CameraSource;
import ng.com.piccmaq.flutter.flutter_mobile_vision_2.ui.CameraSourcePreview;
import ng.com.piccmaq.flutter.flutter_mobile_vision_2.ui.GraphicOverlay;
import ng.com.piccmaq.flutter.flutter_mobile_vision_2.ui.ScanAreaGraphic;

public abstract class AbstractCaptureActivity<T extends GraphicOverlay.Graphic>
        extends Activity {

    public static final String AUTO_FOCUS = "AUTO_FOCUS";
    public static final String USE_FLASH = "USE_FLASH";
    public static final String FORMATS = "FORMATS";
    public static final String MULTIPLE = "MULTIPLE";
    public static final String WAIT_TAP = "WAIT_TAP";
    public static final String FORCE_CLOSE_CAMERA_ON_TAP = "FORCE_CLOSE_CAMERA_ON_TAP";
    public static final String SHOW_TEXT = "SHOW_TEXT";
    public static final String PREVIEW_WIDTH = "PREVIEW_WIDTH";
    public static final String PREVIEW_HEIGHT = "PREVIEW_HEIGHT";
    public static final String SCAN_AREA_WIDTH = "SCAN_AREA_WIDTH";
    public static final String SCAN_AREA_HEIGHT = "SCAN_AREA_HEIGHT";
    public static final String CAMERA = "CAMERA";
    public static final String FPS = "FPS";
    public static final String IMAGE_PATH = "IMAGE_PATH";

    public static final String OBJECT = "Object";
    public static final String ERROR = "Error";

    protected CameraSource cameraSource;
    protected CameraSourcePreview preview;
    protected GraphicOverlay<T> graphicOverlay;
    protected GraphicOverlay<ScanAreaGraphic> scanAreaOverlay;

    protected GestureDetector gestureDetector;
    protected boolean autoFocus;
    protected boolean useFlash;
    protected boolean multiple;
    protected boolean waitTap;
    protected boolean forceCloseCameraOnTap;
    protected boolean showText;
    protected int previewWidth;
    protected int previewHeight;
    protected int scanAreaWidth;
    protected int scanAreaHeight;
    protected int camera;
    protected float fps;
    protected String imagePath;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

            setContentView(R.layout.capture);

            preview = findViewById(R.id.preview);
            graphicOverlay = findViewById(R.id.graphic_overlay);
            scanAreaOverlay = findViewById(R.id.scan_area_overlay);

            autoFocus = getIntent().getBooleanExtra(AUTO_FOCUS, false);
            useFlash = getIntent().getBooleanExtra(USE_FLASH, false);
            multiple = getIntent().getBooleanExtra(MULTIPLE, false);
            waitTap = getIntent().getBooleanExtra(WAIT_TAP, false);
            forceCloseCameraOnTap = getIntent().getBooleanExtra(FORCE_CLOSE_CAMERA_ON_TAP, false);
            showText = getIntent().getBooleanExtra(SHOW_TEXT, false);
            previewWidth = getIntent().getIntExtra(PREVIEW_WIDTH, CameraSource.PREVIEW_WIDTH);
            previewHeight = getIntent().getIntExtra(PREVIEW_HEIGHT, CameraSource.PREVIEW_HEIGHT);
            scanAreaWidth = getIntent().getIntExtra(SCAN_AREA_WIDTH, 0);
            scanAreaHeight = getIntent().getIntExtra(SCAN_AREA_HEIGHT, 0);
            camera = getIntent().getIntExtra(CAMERA, CameraSource.CAMERA_FACING_BACK);
            fps = getIntent().getFloatExtra(FPS, 15.0f);
            imagePath  = getIntent().getStringExtra(IMAGE_PATH);
            imagePath = imagePath == null ? "" : imagePath;

            createCameraSource();

            gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
                }
            });
        } catch (Exception e) {
            onError(e);
        }
    }

    protected abstract void createCameraSource() throws MobileVisionException;

    private void onError(Exception e) {
        Intent data = new Intent();
        data.putExtra(ERROR, e);
        setResult(CommonStatusCodes.ERROR);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            startCameraSource();
        } catch (Exception e) {
            onError(e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (preview != null) {
            preview.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (preview != null) {
            preview.release();
        }
    }

    @SuppressLint("MissingPermission")
    private void startCameraSource() throws SecurityException, MobileVisionException {

        int code = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(getApplicationContext());

        if (code != ConnectionResult.SUCCESS) {
            throw new MobileVisionException("Google Api Availability Error: " + code);
        }

        if (cameraSource != null) {
            try {
                preview.start(cameraSource, graphicOverlay, scanAreaHeight, scanAreaWidth);
            } catch (IOException e) {
                cameraSource.release();
                cameraSource = null;
                throw new MobileVisionException("Unable to start camera source.", e);
            }
        }
    }

    protected void saveImage(final ImageSavedCallback imageSavedCallback){
        if(imagePath.isEmpty()){
            imageSavedCallback.onImageSaved(false);
            return;
        }

        cameraSource.takePicture(new CameraSource.ShutterCallback() {
            @Override
            public void onShutter() {

            }
        }, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] result) {
                try {
//                    File file = getExternalFilesDir("ocr-images");
//                    if (!file.exists())
//                        file.mkdir();
//
//                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.GERMAN);
//                    Date date = new Date();

//                    String imagePath = file.getPath()+ File.separator + dateFormat.format(date)+".jpg";
                    File image =  (new File(imagePath));
                    if (!image.exists())
                        image.delete();

                    Toast.makeText(AbstractCaptureActivity.this, imagePath, Toast.LENGTH_SHORT).show();

                    Bitmap picture = BitmapFactory.decodeByteArray(result, 0, result.length);

                    FileOutputStream out = new FileOutputStream(imagePath);
                    picture.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    picture.recycle();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                imageSavedCallback.onImageSaved(false);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return gestureDetector.onTouchEvent(e)
                || super.onTouchEvent(e);
    }

    protected abstract boolean onTap(float rawX, float rawY);


    public interface ImageSavedCallback{
        void onImageSaved(boolean saved);
    }
}
