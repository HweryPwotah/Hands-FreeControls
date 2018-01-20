package com.example.os10.hands_freecontrols;

import android.app.Service;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

public class MainEngine extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//    }

    /* reference to the service which started the engine */
    private Service mService;

    /* root overlay view */
    private OverlayView mOverlayView;
    private CameraView mCameraView;
    private DockPanelView mDockPanelView;

    protected OverlayView getOverlayView() {
        return mOverlayView;
    }

    // layer for drawing the pointer
    private PointerView mPointerView;
    private PointF mPointer= new PointF(0, 0); // avoid creating a new PointF for each frame

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

    public void initialize(@NonNull Service service){
        mService = service;
        if (Preferences.initForA11yService(this) == null) return;

        //initializing User Interface: ViewGroup
        Log.i("MainEngine", "Try to initialize OverlayView");
        mOverlayView = new OverlayView(mService);

        //initializing User Interface: Camera
        Log.i("MainEngine", "Try to initialize DockPanelView");
        mDockPanelView = new DockPanelView(mService);
        mOverlayView.addFullScreenLayer(mDockPanelView);

        //Initializing User Interface: Dock Panel
        Log.i("MainEngine", "Try to initialize CameraView");
        mCameraView = new CameraView(mService);
        mOverlayView.addFullScreenLayer(mCameraView);

        mCameraView.startCamera(mService);

        //initializing User Interface: Pointer
        Log.i("MainEngine", "Try to initialize PointerView");
        mPointerView = new PointerView(mService);
        mOverlayView.addFullScreenLayer(mPointerView);

        mCameraView.obtainPointerView(mPointerView);

        Log.i("MainEngine", "end of initialize");
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        Log.i(TAG, "onCreate");
//        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        setContentView(R.layout.activity_main);
//    }
//
//    @Override
//    protected void onResume() {
//        Log.e(TAG, "onResume");
//        super.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        Log.e(TAG, "onPause");
//        //closeCamera();
////        stopBackgroundThread();
//        super.onPause();
////        if (mOpenCvCameraView != null)
////            mOpenCvCameraView.disableView();
//    }
//
//    public void onDestroy() {
//        super.onDestroy();
//        Log.e(TAG, "onDestroy");
////        if (mOpenCvCameraView != null)
////            mOpenCvCameraView.disableView();
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(MainEngine.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}