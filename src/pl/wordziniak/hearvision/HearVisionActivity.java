package pl.wordziniak.hearvision;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import pl.wordziniak.hearvision.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class HearVisionActivity extends Activity implements CvCameraViewListener2 {
    private static final String  TAG              = "OCVSample::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ProcessorColor    mDetector;
//    private Mat                  mSpectrum;
//    private Size                 SPECTRUM_SIZE;
//    private Scalar               CONTOUR_COLOR;
    String[] tableTone = {"C1","D1","E1","F1","G1","A1","H1","C2"};
    private ProcessorAudio procAudio = new ProcessorAudio();
    int i=0;
   
    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
//                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

	

    public HearVisionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        mStartStop = (ToggleButton) this.findViewById( R.id.buttonStartStop );
//        mStartStop.setOnCheckedChangeListener( this );

        setContentView(R.layout.color_blob_detection_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ProcessorColor();
//        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
//        SPECTRUM_SIZE = new Size(200, 64);
//        CONTOUR_COLOR = new Scalar(255,0,0,255);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }
    
    
    public boolean setColor(){
    	
    	int xOffset = (mOpenCvCameraView.getWidth() / 2);
        int yOffset = (mOpenCvCameraView.getHeight() / 2);

        Rect centerRect = new Rect();

        centerRect.x = xOffset-25;
        centerRect.y = yOffset-25;

        centerRect.width = 50;
        centerRect.height = 50;
        
        Mat touchedRegionRgba = mRgba.submat(centerRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = centerRect.width*centerRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;
        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

//        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
//                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mDetector.setHsvColor(mBlobColorHsv);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();
        procAudio.setFrequency(mDetector.getColor(mBlobColorRgba));
//        procAudio.setFrequency(tableTone[i]);
//      	i=(i+1)%tableTone.length;
    	procAudio.start();

        return false; // don't need subsequent touch events
    }

//    public boolean onTouch(View v, MotionEvent event) {
//        int cols = mRgba.cols();
//        int rows = mRgba.rows();
//
//        int xOffset = (mOpenCvCameraView.getWidth() / 2);
//        int yOffset = (mOpenCvCameraView.getHeight() / 2);
//
//        int x = (int)event.getX() - xOffset;
//        int y = (int)event.getY() - yOffset;
//
//        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");
//
//        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;
//
//        Rect centerRect = new Rect();
//
//        centerRect.x = xOffset-25;
//        centerRect.y = yOffset-25;
//
//        centerRect.width = 50;
//        centerRect.height = 50;
//
//        Mat touchedRegionRgba = mRgba.submat(centerRect);
//
//        Mat touchedRegionHsv = new Mat();
//        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
//
//        // Calculate average color of touched region
//        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
//        int pointCount = centerRect.width*centerRect.height;
//        for (int i = 0; i < mBlobColorHsv.val.length; i++)
//            mBlobColorHsv.val[i] /= pointCount;
//
//        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
//
//        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
//                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");
//
//        mDetector.setHsvColor(mBlobColorHsv);
//
////        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
//
//        mIsColorSelected = true;
//
//        touchedRegionRgba.release();
//        touchedRegionHsv.release();
//        
//        procAudio.setFrequency(tableTone[i]);
//      	i=(i+1)%tableTone.length;
//    	procAudio.start();
////        try {
////     	   Thread.sleep(1000);
////        } catch (InterruptedException e) {
////     	   // TODO Auto-generated catch block
////		   	e.printStackTrace();
////        }
////        	procAudio.pause();
//
//        return setColor(); // don't need subsequent touch events
//    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        if (mIsColorSelected) {
            mDetector.process(mRgba);
//            List<MatOfPoint> contours = mDetector.getContours();
//            Log.e(TAG, "Contours count: " + contours.size());
//            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);
//
//            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
//            mSpectrum.copyTo(spectrumLabel);
        }
        setColor();

        return mRgba;
    }
    


    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
    
}