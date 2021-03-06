package ng.com.piccmaq.flutter.flutter_mobile_vision_2.ocr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;

import ng.com.piccmaq.flutter.flutter_mobile_vision_2.face.MyFace;
import ng.com.piccmaq.flutter.flutter_mobile_vision_2.ui.CameraSource;
import ng.com.piccmaq.flutter.flutter_mobile_vision_2.util.AbstractCaptureActivity;
import ng.com.piccmaq.flutter.flutter_mobile_vision_2.util.MobileVisionException;

public final class OcrCaptureActivity extends AbstractCaptureActivity<OcrGraphic> {

    @SuppressLint("InlinedApi")
    protected void createCameraSource() throws MobileVisionException {
        Context context = getApplicationContext();

        TextRecognizer textRecognizer = new TextRecognizer.Builder(context)
                .build();

        OcrTrackerFactory ocrTrackerFactory = new OcrTrackerFactory(graphicOverlay, showText, scanAreaHeight, scanAreaWidth, scanAreaOverlay);

        textRecognizer.setProcessor(
                new MultiProcessor.Builder<>(ocrTrackerFactory).build());

        if (!textRecognizer.isOperational()) {
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                throw new MobileVisionException("Low Storage.");
            }
        }

        cameraSource = new CameraSource
                .Builder(getApplicationContext(), textRecognizer)
                .setFacing(camera)
                .setRequestedPreviewSize(previewWidth, previewHeight)
                .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .setRequestedFps(fps)
                .build();
    }

    protected boolean onTap(float rawX, float rawY) {
        if(!waitTap) {
            return false;
        }

       final ArrayList<MyTextBlock> list = new ArrayList<>();

        if (multiple) {
            for (OcrGraphic graphic : graphicOverlay.getGraphics()) {
                list.add(new MyTextBlock(graphic.getTextBlock()));
            }
        } else {
            OcrGraphic graphic = graphicOverlay.getBest(rawX, rawY);
            if (graphic != null && graphic.getTextBlock() != null) {
                list.add(new MyTextBlock(graphic.getTextBlock()));
            }
        }

        if(forceCloseCameraOnTap){
            success(list);
            return true;
        }

        if (!list.isEmpty()) {
            success(list);
            return true;
        }

        return false;
    }



    private void success(final ArrayList<MyTextBlock> list) {
        this.saveImage(new ImageSavedCallback() {
            @Override
            public void onImageSaved(boolean saved) {
                Intent data = new Intent();
                data.putExtra(OBJECT, list);
                setResult(CommonStatusCodes.SUCCESS, data);
                finish();
            }
        });
    }
}