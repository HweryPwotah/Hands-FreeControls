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
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.video.Video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Class that handles Camera View
 */

public class CameraView extends RelativeLayout implements CameraBridgeViewBase.CvCameraViewListener2 {
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
    private static final String RES_FACE_CASCADE = "haarcascade_frontalface_alt2.xml";
    private static final String RES_EYES_CASCADE = "haarcascade_eye_tree_eyeglasses.xml";
    private String[] mRawRes = {RES_FACE_CASCADE, RES_EYES_CASCADE};

    private CascadeClassifier mFaceCascade;


    private int mAbsoluteFaceSize;
    private static final float FACE_SIZE_PERCENTAGE = 0.3f;
    private PointF mPointerMotion;

    private MainEngine mainEngine;
    //    SurfaceView getCameraSurface(){
//        return mCameraView;
//    }

    /**
     * Camera constructor
     *
     * @param c Context
     * @param mEngine
     */
    public CameraView(@NonNull Context c, MainEngine mEngine) {
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

        mainEngine = mEngine;

        Resources r = getResources();

        //initializing camera
        int cameraId = CameraBridgeViewBase.CAMERA_ID_FRONT;

        mCameraView = new JavaCameraView(c, cameraId);
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
        RelativeLayout.LayoutParams lp =
                new RelativeLayout.LayoutParams(176, 144);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        v.setLayoutParams(lp);

        this.addView(v);
    }

    public void startCamera(@NonNull final Context context) {
        Log.i("CameraView", "Start Camera");

        (new AsyncTask<String, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(String... params) {

                try {
                    // First URL
                    {
                        // Copy the resource into a temp file so OpenCV can load it
                        InputStream is = context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
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
                if (isSuccess) {
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


        mCurrGrayImg = new Mat(height, width, CvType.CV_8UC4);

        // The faces will be a 30% of the height of the screen
        mAbsoluteFaceSize = (int) (height * FACE_SIZE_PERCENTAGE);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mRgbaF.release();
        mRgbaT.release();
    }

    private Mat mPrevGrayImg = new Mat();
    private Mat mCurrGrayImg = new Mat();
    Mat mPrevFaceLocation = new Mat();

    MatOfPoint mCorners = new MatOfPoint();
    MatOfPoint2f mCorners2f = new MatOfPoint2f();
    MatOfPoint2f mPrevCorners = new MatOfPoint2f();
    MatOfPoint2f mCurrCorners = new MatOfPoint2f();
    MatOfPoint2f mTempCorners = new MatOfPoint2f();

    Point[] mArrCurrCorners;

    int mCurrCornerCount = 0;

    Rect mFaceLocation = new Rect();
    Rect mFaceFeature = new Rect();

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        PointF mMotion = new PointF();

        // TODO: 1/21/2018 change this to preProcessing class
        mRgba = inputFrame.rgba();
        // Rotate mRgba 90 degrees
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgba.size(), 0, 0, 0);
        Core.flip(mRgbaF, mRgba, -1);
        mRgbaT.release();
        mRgbaF.release();

        Imgproc.cvtColor(mRgba, mCurrGrayImg, Imgproc.COLOR_RGBA2GRAY);

        //set Region
        Rect mFace = isFaceDetected(mCurrGrayImg);
        if (mFace.width != 0 && mFace.height != 0) {
            //face is detected
            //set Region of Interest to the face
            mFaceLocation = mFace.clone();

            float SMALL_AREA_RATIO = 0.4f;
            mFaceFeature.x = (int) (mFaceLocation.x + mFaceLocation.width * ((1.0f - SMALL_AREA_RATIO) / 2.0f));
            mFaceFeature.y = (int) (mFaceLocation.y + mFaceLocation.height * ((1.0f - SMALL_AREA_RATIO) / 2.0f));
            mFaceFeature.width = (int) (mFaceLocation.width * SMALL_AREA_RATIO);
            mFaceFeature.height = (int) (mFaceLocation.height * SMALL_AREA_RATIO);

            Imgproc.rectangle(mRgba, mFaceLocation.tl(), mFaceLocation.br(), new Scalar(0, 255, 0, 255), 1);

            mPrevFaceLocation = new Mat(mCurrGrayImg, mFaceFeature);

            //calculate for next iteration
            Imgproc.goodFeaturesToTrack(mPrevFaceLocation, mCorners, 15, 0.05, 20);

            mCorners.convertTo(mCorners2f, CvType.CV_32F);

            TermCriteria termCrit = new TermCriteria(TermCriteria.MAX_ITER + TermCriteria.EPS, 20, 0.03);
            Imgproc.cornerSubPix(mPrevFaceLocation, mCorners2f, new Size(5, 5), new Size(-1, -1), termCrit);

            mArrCurrCorners = mCorners2f.toArray();
            mCurrCornerCount = mArrCurrCorners.length;

            for (int i = 0; i < mCurrCornerCount; i++) {
                mArrCurrCorners[i].x += mFaceFeature.x;
                mArrCurrCorners[i].y += mFaceFeature.y;
            }

            mCurrCorners.fromArray(mArrCurrCorners);
        }

        if (mPrevCorners.rows() == 0) {
            //iteration is first frame. copy current to previous and enter next loop
            mCurrGrayImg.copyTo(mPrevGrayImg);
            // get safe copy of this corners
            mCurrCorners.copyTo(mTempCorners);
            mTempCorners.copyTo(mPrevCorners);
            return mRgba;
        }

        // retrieve the corners from the prev mat (saves calculating them again)
        // and save this corners for next time through
        mTempCorners.copyTo(mPrevCorners);

        // to contain the new corners from cvCalcOpticalFlow
        mCurrCorners.copyTo(mTempCorners);

        MatOfByte mStatus = new MatOfByte();
        MatOfFloat mErr = new MatOfFloat();
        Video.calcOpticalFlowPyrLK(mPrevGrayImg, mCurrGrayImg, mPrevCorners, mCurrCorners, mStatus, mErr);

        List<Point> cornersPrev = mPrevCorners.toList();
        List<Point> cornersCurr = mCurrCorners.toList();
        List<Byte> byteStatus = mStatus.toList();

        int y = byteStatus.size();
        float xVel = 0, yVel = 0;
        int mValidCorners = 0;

        for (int x = 0; x < y; x++) {
            if (byteStatus.get(x) == 1//){
                    && cornersPrev.get(x).x >= mFaceLocation.x
                    && cornersPrev.get(x).x < mFaceLocation.x + mFaceLocation.width
                    && cornersPrev.get(x).y >= mFaceLocation.y
                    && cornersPrev.get(x).y < mFaceLocation.y + mFaceLocation.height) {

                xVel = (float) (xVel + (cornersCurr.get(x).x - cornersPrev.get(x).x));
                yVel = (float) (yVel + (cornersCurr.get(x).y - cornersPrev.get(x).y));

//                //debugging purposes
                Point pt = cornersCurr.get(x);
//                Point pt2 = cornersPrev.get(x);
                Imgproc.circle(mRgba, pt, 5, new Scalar(255, 0, 0, 255));
//                Imgproc.line(mRgba, pt, pt2, new Scalar(255, 255, 255, 255));
                mValidCorners += 1;
            }
        }

        if (mValidCorners != 0) {
            xVel = xVel / mValidCorners;
            yVel = yVel / mValidCorners;
        } else {
            xVel = 0;
            yVel = 0;
        }

        mFaceLocation.x = (int) (mFaceLocation.x + xVel);
        mFaceLocation.y = (int) (mFaceLocation.y + yVel);
        HandleRectOnBorder(mFaceLocation, mRgba);

        //for debugging purposes
//        drawCross(mRgba, new Point((mFaceLocation.x + (mFaceLocation.width / 2)),
//                        (mFaceLocation.y + (mFaceLocation.height / 2))),
//                new Scalar(255, 0, 0, 255));
//        Point tl = new Point(mFaceLocation.x, mFaceLocation.y);
//        Point br = new Point((mFaceLocation.x + mFaceLocation.width),
//                (mFaceLocation.y + mFaceLocation.height));
//
//        Imgproc.rectangle(mRgba, tl, br, new Scalar(255, 255, 255, 255), 1);

        mMotion.x = xVel;
        mMotion.y = yVel;

//        setPointerMotion(mMotion);
//        mPointerMotion = mCameraView.getPointerMotion();

        mainEngine.processMotion(mMotion);

        mCurrGrayImg.copyTo(mPrevGrayImg);
        return mRgba;
    }

    private Rect isFaceDetected(Mat img) {
        if (mFaceCascade == null) {
            Log.e("CameraView", "Face Cascade is null");
        }

        MatOfRect mFaces = new MatOfRect();
        Size mMinSize = new Size(mAbsoluteFaceSize, mAbsoluteFaceSize);
        Size mMaxSize = new Size();
        Rect[] facesArray;
        Rect mFace;

        //https://stackoverflow.com/questions/20801015/recommended-values-for-opencv-detectmultiscale-parameters
        mFaceCascade.detectMultiScale(img, mFaces, 1.1, 2, 2, mMinSize, mMaxSize);
        facesArray = mFaces.toArray();

        if (facesArray.length > 0) {
            mFace = facesArray[0];
        } else {
            mFace = new Rect(0, 0, 0, 0);
        }

        return mFace;
    }

//    public void obtainPointerView(PointerView pointerView) {
//        mPointerView = pointerView;
//    }

    private void drawCross(Mat inputFrame, Point p, Scalar color) {
        int thickness = 2;
        int radius = 4;

        Point p1 = new Point();
        Point p2 = new Point();

	/*
     * Horizontal line
	 */
        p1.x = p.x - radius;
        p1.y = p.y;
        p2.x = p.x + radius;
        p2.y = p.y;
        Imgproc.line(inputFrame, p1, p2, color, thickness);

	/*
      * Vertical line
	 */
        p1.x = p.x;
        p1.y = p.y - radius;
        p2.x = p.x;
        p2.y = p.y + radius;
        Imgproc.line(inputFrame, p1, p2, color, thickness);
    }


    private Rect HandleRectOnBorder(Rect mFaceLocation, Mat mRgba) {
        int rows = mRgba.rows();
        int cols = mRgba.cols();

        mFaceLocation.x = (int) fitPoint(mFaceLocation.x, cols);
        mFaceLocation.y = (int) fitPoint(mFaceLocation.y, rows);

        mFaceLocation.width = (int) fitSize(mFaceLocation.width, cols);
        mFaceLocation.height = (int) fitSize(mFaceLocation.height, rows);

        mFaceLocation.x = (int) fitPointWithSize(mFaceLocation.x, mFaceLocation.width, cols);
        mFaceLocation.y = (int) fitPointWithSize(mFaceLocation.y, mFaceLocation.height, rows);

        return mFaceLocation;
    }

    private float fitPoint(float p, int max_size) {
        if (p < 0)
            p = 0;
        else if (p >= (float) max_size)
            p = (float) (max_size - 1);

        return p;
    }

    private float fitSize(float s, int max_size) {
        if (s < 0)
            s = 0;
        else if (s > (float) max_size)
            s = (float) max_size;

        return s;
    }


    private float fitPointWithSize(float p, float s, int max_size) {
        if (p + s > (float) max_size) {
            p = (float) max_size - s;
        }

        return p;
    }

    void drawCorners(Mat InputFrame, Point[] corners, int num_corners, Scalar color) {
        for (int i = 0; i < num_corners; i++) {
            drawCross(InputFrame, corners[i], color);
        }
    }

    public void setPointerMotion(PointF PointerMotion) {
        mPointerMotion = PointerMotion;
    }

    public PointF getPointerMotion() {
        return mPointerMotion;
    }

}