package com.example.os10.hands_freecontrols;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class that handles Camera View
 */

public class CameraView extends RelativeLayout implements CameraBridgeViewBase.CvCameraViewListener2{
//
//    private static final int CAM_SURFACE_MIN_WIDTH_DP = 50;
//    private static final int CAM_SURFACE_WIDTH_DEFAULT = 80;
//    private static final int CAM_SURFACE_HEIGHT_DEFAULT = 60;
//
//    private int CAM_SURFACE_WIDTH = 176;
//    private int CAM_SURFACE_HEIGHT = 144;

//    private SurfaceView mCameraSurfaceView;
    // OpenCV capture&view facility
    private final CameraBridgeViewBase mCameraView;
    private Mat mRgba;
    private Mat mRgbaF;
    private Mat mRgbaT;


    //Haar Cascade variables
    private static final String RES_FACE_CASCADE = "haarcascade_frontalface_alt.xml";
    private static final String RES_EYES_CASCADE = "haarcascade_eye_tree_eyeglasses.xml";
    private String[] mRawRes = {RES_FACE_CASCADE, RES_EYES_CASCADE};

    private CascadeClassifier mFaceCascade;
    private Mat mGrayscaleImage;

    private int mAbsoluteFaceSize;
    private static final float FACE_SIZE_PERCENTAGE = 0.3f;
    private PointerView mPointerView;

//    SurfaceView getCameraSurface(){
//        return mCameraView;
//    }
    /**
     *
     * Camera constructor
     * @param c Context
     */
    public CameraView(@NonNull Context c) {
        super(c);
        //initializing OpenCV
//        if (!OpenCVLoader.initDebug()) {
//            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, c, mLoaderCallback);
//        } else {
//            Log.d(TAG, "OpenCV library found inside package. Using it!");
//            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//        }

        if (!OpenCVLoader.initDebug()) {
            throw new RuntimeException("Cannot initialize OpenCV");
        }


        Resources r = getResources();

        //initializing camera
        int cameraId = CameraBridgeViewBase.CAMERA_ID_FRONT;

        mCameraView= new JavaCameraView(c, cameraId);
        mCameraView.setCvCameraViewListener(this);

        //352 288 works well on most devices
        mCameraView.setMaxFrameSize(352, 288);
        mCameraView.setVisibility(SurfaceView.VISIBLE);

        addCameraSurface(mCameraView);
    }

    /**
     * Add a surface in which the image from the camera will be displayed
     *
     * @param v a surface view
     */
    public void addCameraSurface(SurfaceView v) {
        Log.i("CameraView", "Adding CameraSurface");
//        mCameraSurfaceView= v;

        // set layout and add to parent
//        RelativeLayout.LayoutParams lp=
//                new RelativeLayout.LayoutParams(CAM_SURFACE_WIDTH, CAM_SURFACE_HEIGHT);
//
       RelativeLayout.LayoutParams lp=
                new RelativeLayout.LayoutParams(176, 144);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        v.setLayoutParams(lp);

        this.addView(v);
    }

    public void startCamera(@NonNull final Context context){
        Log.i("CameraView", "Start Camera");

        (new AsyncTask<String, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(String... params) {

                try {
                    // First URL
                    {
                        // Copy the resource into a temp file so OpenCV can load it
                        InputStream is = context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
                        File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
                        File mCascadeFile = new File(cascadeDir, params[0]);
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        // Load the cascade classifier
                        mFaceCascade = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean isSuccess) {
                if(isSuccess) {
                    mCameraView.enableView();
//                    mIsDetectionOn = true;
//                    mTimer.scheduleAtFixedRate(mBlinkCounterTask, 0, DETECTION_STEP_DURATION);
                }
//                else {
//                    mToast.cancel();
//                    mToast = Toast.makeText(getApplicationContext(), CASCADE_INIT_ERROR, Toast.LENGTH_SHORT);
//                    mToast.show();
//                }
                super.onPostExecute(isSuccess);
            }
        }).execute(mRawRes);
    }

//    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
//        // OpenCV library is loaded, you may want to perform some actions.
//        // For example, displaying a success or failure message.
//        @Override
//        public void onManagerConnected(int status) {
//            switch (status) {
//                case LoaderCallbackInterface.SUCCESS: {
//                    Log.i(TAG, "OpenCV loaded successfully");
//                }
//                break;
//                default: {
//                    super.onManagerConnected(status);
//                }
//                break;
//            }
//        }
//    };

    @Override
    public void onCameraViewStarted(int width, int height) {
        //initialize variable
        mRgba = new Mat(height, width, CvType.CV_8UC3);
        mRgbaF = new Mat(height, width, CvType.CV_8UC3);
        mRgbaT = new Mat(width, width, CvType.CV_8UC3);


        mGrayscaleImage = new Mat(height, width, CvType.CV_8UC4);

        // The faces will be a 30% of the height of the screen
        mAbsoluteFaceSize = (int) (height * FACE_SIZE_PERCENTAGE);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mRgbaF.release();
        mRgbaT.release();
    }

    PointF testingPoint = new PointF(0,0);
    Rect prevFace;
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //output somehow shows rotated to the left. Function below is to rotate 90 deg clockwise.
        //Update: OpenCV orients the camera to left by 90 degrees. So if the app is
        // in portrait more, camera will be in -90 or 270 degrees orientation.
//
        mRgba = inputFrame.rgba();
        // Rotate mRgba 90 degrees
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgba.size(), 0, 0, 0);
        Core.flip(mRgbaF, mRgba, -1);
        mRgbaT.release();
        mRgbaF.release();

        Imgproc.cvtColor(mRgba, mGrayscaleImage, Imgproc.COLOR_RGBA2GRAY);
        MatOfRect mFaces = new MatOfRect();
        Size mMinSize = new Size(mAbsoluteFaceSize, mAbsoluteFaceSize);
        Size mMaxSize = new Size();

        if (mFaceCascade != null) {
            mFaceCascade.detectMultiScale(mGrayscaleImage, mFaces, 1.1, 2, 2, mMinSize, mMaxSize);
        }

        Rect[] facesArray = mFaces.toArray();


        if(facesArray.length > 0) {
            int i = 0;
            //Face rectangle
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 1);

            mPointerView.processMotion(prevFace, facesArray[0]);
            prevFace = facesArray[0];
        }else{
            prevFace = null;
        }

//        Log.e("CameraView", "TestingPoint is (" + testingPoint.x + "," + testingPoint.y + ").");




        // make sure visible changes are updated
        mPointerView.postInvalidate();

        return mRgba;
    }

    public void obtainPointerView(PointerView pointerView) {
        mPointerView = pointerView;
    }
}


