package ng.com.piccmaq.flutter.flutter_mobile_vision_2.barcode;

import androidx.annotation.UiThread;

import com.google.android.gms.vision.barcode.Barcode;

public interface BarcodeUpdateListener {

    @UiThread
    void onBarcodeDetected(Barcode barcode);

}
