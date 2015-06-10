package camdet.credr.abhilashkulkarni.cameradetector;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    public Camera mCamera;
    private String TAG = "Check";
    public Boolean isPreview = false;
    private Display display;
    public CameraPreview(Context context) {
        super(context);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        //mHolder.addCallback(SurfaceHolderCallback);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            Log.d(TAG, "In Camera Opener");
            if (mCamera == null) {
                Log.d(TAG, "In if condition:40");
                mCamera = Camera.open();
            }
            isPreview = true;
        } catch (Exception e) {
            Log.e(TAG, "failed to open Camera");
            Log.d(TAG, "failed to open Camera " + e.getMessage());
            e.printStackTrace();
        }

        try {
            if (mCamera == null)
                Log.d(TAG, "Camera is null");
            if (holder == null)
                Log.d(TAG, "Holder is null");
            mCamera.setPreviewDisplay(holder);
            //mCamera.startPreview();
        } catch (IOException e1) {
            Log.d(TAG, "Error setting camera preview: " + e1.getMessage());
        }
        Camera.Parameters p = mCamera.getParameters();
        p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.setParameters(p);
        //mCamera.autoFocus(null);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        //this.getHolder().removeCallback(this);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        isPreview = false;
        Log.d(TAG, "Surface is destroyed");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        Camera.Parameters params = mCamera.getParameters();
        Camera.Size size = getBestPreviewSize(w, h, params);

        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        Log.d(TAG, "In Surface Changed");

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            Log.d(TAG, "Preview Surface does not exist");
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            /*if (size != null) {
                params.setPreviewSize(size.width, size.height);
                mCamera.setParameters(params);
                mCamera.startPreview();
                isPreview = true;

                Log.d(TAG, String.valueOf(size.width) + " : " + String.valueOf(size.height));

            }*/


            if(display.getRotation() == Surface.ROTATION_0)
            {
                params.setPreviewSize(h, w);
                mCamera.setDisplayOrientation(90);
            }

            if(display.getRotation() == Surface.ROTATION_90)
            {
                params.setPreviewSize(w, h);
            }

            if(display.getRotation() == Surface.ROTATION_180)
            {
                params.setPreviewSize(h, w);
            }

            if(display.getRotation() == Surface.ROTATION_270)
            {
                params.setPreviewSize(w, h);
                mCamera.setDisplayOrientation(180);
            }

            mCamera.setParameters(params);

            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            Log.d(TAG, String.valueOf(h) + ':' + String.valueOf(w));

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

        bestSize = sizeList.get(0);

        for (int i = 1; i < sizeList.size(); i++) {
            Log.d(TAG, String.valueOf(sizeList.get(i).width) + " : " + String.valueOf(sizeList.get(i).height));
            if ((sizeList.get(i).width * sizeList.get(i).height) >
                    (bestSize.width * bestSize.height)) {
                bestSize = sizeList.get(i);
            }
        }

        return bestSize;
    }


    public Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            Log.d(TAG, "onShutter'd");
        }
    };

    /**
     * Handles data for raw picture
     */
    public Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - raw");
        }
    };

    /**
     * Handles data for jpeg picture
     */
    public Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            FileOutputStream outStream = null;
            long time = 0;
            try {
                // write to local sandbox file system
//                outStream = CameraDemo.this.openFileOutput(String.format("%d.jpg", System.currentTimeMillis()), 0);
                // Or write to sdcard
                time = System.currentTimeMillis();
                outStream = new FileOutputStream(String.format("/sdcard/%d.jpg", time));
                outStream.write(data);
                outStream.close();
                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {


            }
            Log.d(TAG, "onPictureTaken - jpeg");
        }
    };

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.release();
        }
    }

    @Override

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.d(TAG, "focusing now");
            mCamera.autoFocus(null);
        }

        return true;
    }

    public SurfaceHolder.Callback SurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                Log.d(TAG, "In Camera Opener");
                    if(!isPreview){
                    Log.d(TAG, "In if condition:204");
                    mCamera = Camera.open();}

                isPreview = true;
            } catch (Exception e) {
                Log.e(TAG, "failed to open Camera");
                Log.d(TAG, "failed to open Camera " + e.getMessage());
                e.printStackTrace();
            }

            try {
                if (mCamera == null)
                    Log.d(TAG, "Camera is null");
                if (holder == null)
                    Log.d(TAG, "Holder is null");
                mCamera.setPreviewDisplay(holder);
                //mCamera.startPreview();
            } catch (IOException e1) {
                Log.d(TAG, "Error setting camera preview: " + e1.getMessage());
            }

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }


    };
}

