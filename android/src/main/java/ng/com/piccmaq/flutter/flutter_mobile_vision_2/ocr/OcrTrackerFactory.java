package ng.com.piccmaq.flutter.flutter_mobile_vision_2.ocr;

import android.util.Log;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.text.TextBlock;

import ng.com.piccmaq.flutter.flutter_mobile_vision_2.ui.GraphicOverlay;
import ng.com.piccmaq.flutter.flutter_mobile_vision_2.ui.ScanAreaGraphic;

public class OcrTrackerFactory implements MultiProcessor.Factory<TextBlock> {
    private GraphicOverlay<OcrGraphic> graphicOverlay;
    private GraphicOverlay<ScanAreaGraphic> scanAreaOverlay;
    private boolean showText;
    private int scanAreaHeight;
    private int scanAreaWidth;

    public OcrTrackerFactory(GraphicOverlay<OcrGraphic> graphicOverlay, boolean showText, int scanAreaHeight, int scanAreaWidth, GraphicOverlay<ScanAreaGraphic> scanAreaOverlay) {
        this.graphicOverlay = graphicOverlay;
        this.showText = showText;
        this.scanAreaHeight = scanAreaHeight;
        this.scanAreaWidth = scanAreaWidth;
        this.scanAreaOverlay = scanAreaOverlay;
    }

    @Override
    public Tracker<TextBlock> create(TextBlock textBlock) {
        OcrGraphic graphic = new OcrGraphic(graphicOverlay, showText);
        try {
            return new OcrGraphicTracker(graphicOverlay, graphic, scanAreaOverlay);
        } catch (Exception ex) {
            Log.d("OcrTrackerFactory", ex.getMessage(), ex);
        }
        return null;
    }
}
