package ng.com.piccmaq.flutter.flutter_mobile_vision_2.face;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.face.FaceDetector;

import java.util.ArrayList;

import ng.com.piccmaq.flutter.flutter_mobile_vision_2.ui.CameraSource;
import ng.com.piccmaq.flutter.flutter_mobile_vision_2.util.AbstractCaptureActivity;
import ng.com.piccmaq.flutter.flutter_mobile_vision_2.util.MobileVisionException;

public final class FaceCaptureActivity extends AbstractCaptureActivity<FaceGraphic> {


    @SuppressLint("InlinedApi")
    protected void createCameraSource() throws MobileVisionException {
        Context context = getApplicationContext();

        // TODO: Verify attributes.
        FaceDetector faceDetector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        FaceTrackerFactory faceTrackerFactory = new FaceTrackerFactory(graphicOverlay, showText);

        faceDetector.setProcessor(
                new MultiProcessor.Builder<>(faceTrackerFactory).build());

        if (!faceDetector.isOperational()) {
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                throw new MobileVisionException("Low Storage.");
            }
        }

        cameraSource = new CameraSource
                .Builder(getApplicationContext(), faceDetector)
                .setFacing(camera)
                .setRequestedPreviewSize(previewWidth, previewHeight)
                .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .setRequestedFps(fps)
                .build();
    }

    protected boolean onTap(float rawX, float rawY) {
        final ArrayList<MyFace> list = new ArrayList<>();

        if (multiple) {
            for (FaceGraphic graphic : graphicOverlay.getGraphics()) {
                list.add(new MyFace(graphic.getFace()));
            }
        } else {
            FaceGraphic graphic = graphicOverlay.getBest(rawX, rawY);
            if (graphic != null && graphic.getFace() != null) {
                list.add(new MyFace(graphic.getFace()));
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

    private void success(final ArrayList<MyFace> list) {
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