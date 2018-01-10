package com.example.os10.hands_freecontrols;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.Arrays;

/**
 * Created by OS 10 on 12/20/2017.
 */

public class CameraView extends RelativeLayout implements CameraBridgeViewBase.CvCameraViewListener2{

    private int CAM_SURFACE_WIDTH = 352;
    private int CAM_SURFACE_HEIGHT = 288;
    private SurfaceView mCameraSurfaceView;
    // OpenCV capture&view facility
    private final CameraBridgeViewBase mCameraView;
    private Mat mRgba;
    private Mat mRgbaF;
    private Mat mRgbaT;

    SurfaceView getCameraSurface(){
        return mCameraView;
    }
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

        //initializing camera
        int cameraId = CameraBridgeViewBase.CAMERA_ID_FRONT;

        mCameraView= new JavaCameraView(c, cameraId);
        mCameraView.setCvCameraViewListener(this);

        //352 288 works well on most devices
        mCameraView.setMaxFrameSize(352, 288);
        mCameraView.setVisibility(SurfaceView.VISIBLE);

        mCameraView.enableView();

        addCameraSurface(mCameraView);
    }

    /**
     * Add a surface in wich the image from the camera will be displayed
     *
     * @param v a surface view
     */
    public void addCameraSurface(SurfaceView v) {
        mCameraSurfaceView= v;

        // set layout and add to parent
        RelativeLayout.LayoutParams lp=
                new RelativeLayout.LayoutParams(CAM_SURFACE_WIDTH, CAM_SURFACE_HEIGHT);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        v.setLayoutParams(lp);

        this.addView(v);
    }

    static Mat recognize(Mat InputFrame) {
//        List<Mat> ImageList = new ArrayList<>();
//        ImageList.add(InputFrame);


//        variables were here
        Mat HSV_RoI = new Mat(InputFrame.size(), CvType.CV_8UC3);
//        MatOfInt channels = new MatOfInt(0);
        Mat Mask = new Mat(InputFrame.size(),CvType.CV_8UC1);
        Mat Hist = new Mat(InputFrame.size(),CvType.CV_8UC1);
        Mat BackProj = new Mat();
//        MatOfInt histSize = new MatOfInt(256, 256, 256);
//        MatOfFloat ranges = new MatOfFloat(0.0f, 255.0f, 0.0f, 255.0f, 0.0f, 255.0f);
//        MatOfInt histSize = new MatOfInt(180);

//        MatOfInt histSize = new MatOfInt(16);
//        MatOfFloat ranges = new MatOfFloat(0, 180);

//        TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 10, 1);

        RotatedRect curr_rect = new RotatedRect();
        Rect prev_rect = new Rect(58, 48, 58, 48);

//        Mat roi = new Mat(); //Region of Interest.
//        roi = mRgba.submat(rect);
//        Mat roiTmp = roi.clone();

//        org.opencv.core.Size size = new org.opencv.core.Size(250,250);
//        aInputFrame.size() = size;
//        Imgproc.resize(aInputFrame, aInputFrame, aInputFrame.size());//aInputFrame.size());

        //calculate Back Projection ===============================================================
        Imgproc.cvtColor(InputFrame, HSV_RoI, Imgproc.COLOR_RGB2HSV);

//        Core.inRange(HSV_RoI, new Scalar(0, 10, 60), new Scalar(20, 150, 255), Mask);
        Core.inRange(HSV_RoI, new Scalar(0, 60, 32), new Scalar(180, 256, 256), Mask);

//        Core.mixChannels(Arrays.asList(HSV_RoI), Arrays.asList(Mask), new MatOfInt(1));

        //calcHist function parameters explanation
        //(from http://opencv-java-tutorials.readthedocs.io/en/latest/04-opencv-basics.html)
//        Imgproc.calcHist(Arrays.asList(HSV_RoI), channels, Mask, Hist, histSize, ranges);
        Imgproc.calcHist(Arrays.asList(HSV_RoI), new MatOfInt(0), Mask, Hist, new MatOfInt(180), new MatOfFloat(0, 180));

        Core.normalize(Hist, Hist, 0, 255, Core.NORM_MINMAX);

        //calcBackProject( &hue, 1, 0, hist, backproj, &phranges, 1, true );

        Imgproc.calcBackProject(Arrays.asList(HSV_RoI), new MatOfInt(0), Hist, BackProj, new MatOfFloat(0, 180), 1);


        //MeanShift ===============================================================================
        //an algorithm to find modes in a set of data samples representing an underlying
        //probability density function

//        while (true) {
        curr_rect = Video.CamShift(BackProj, prev_rect, new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 10, 2));
        //current rectangle becomes previous rectangle.
//            curr_rect.angle = -curr_rect.angle;
//            Point pt1 = new Point(curr_rect.size.width);
//            Point pt2 = new Point();
        prev_rect = curr_rect.boundingRect();
        Imgproc.rectangle(InputFrame, new Point(prev_rect.x,
                        prev_rect.y), //Top Left
                new Point(prev_rect.x + prev_rect.width,
                        prev_rect.y + prev_rect.height), //Bottom Right
                new Scalar(255, 0, 0), 2);
//            Log.i(TAG, "Rectangle position is in (" + prev_rect.x + ", " + prev_rect.y + ")");
//            break;
//        }


//        descriptors2 = new Mat();
//        keypoints2 = new MatOfKeyPoint();
//        detector.detect(aInputFrame, keypoints2);
//        descriptor.compute(aInputFrame, keypoints2, descriptors2);

        //create square on face?

//        Point2f src_center(aInputFrame.cols()/2.0F, aInputFrame.rows()/2.0F);
//        Mat rot_mat = getRotationMatrix2D(src_center, angle, 1.0);
//        Mat dst;
//        warpAffine(source, dst, rot_mat, source.size());

//        Mat outputImg = new Mat();
//        return outputImg;

        /* Informative log for debug purposes */
//        mCapturedFrames++;
//        if (mCapturedFrames< 100) {
//            if ((mCapturedFrames % 10) == 0) {
//                Log.i(TAG, "onCameraFrame. Frame count:" + mCapturedFrames);
//            }
//        }

        return InputFrame;
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
        //initialize size?
        mRgba = new Mat(height, width, CvType.CV_8UC3);
        mRgbaF = new Mat(height, width, CvType.CV_8UC3);
        mRgbaT = new Mat(width, width, CvType.CV_8UC3);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mRgbaF.release();
        mRgbaT.release();
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //output somehow shows rotated to the left. Function below is to rotate 90 deg clockwise.
        //Update: OpenCV orients the camera to left by 90 degrees. So if the app is
        // in portrait more, camera will be in -90 or 270 degrees orientation.
        // We fix that in the next and the most important function. There you go!

        mRgba = inputFrame.rgba();
        // Rotate mRgba 90 degrees
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgba.size(), 0, 0, 0);
        Core.flip(mRgbaF, mRgba, 0);
        mRgbaT.release();
        mRgbaF.release();
        return CameraView.recognize(mRgba);
//        return mRgba;
    }
}


