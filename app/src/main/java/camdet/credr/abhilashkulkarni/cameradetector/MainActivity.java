package camdet.credr.abhilashkulkarni.cameradetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


public class MainActivity extends Activity {

    static {
        if(!OpenCVLoader.initDebug()){
            Log.d("Check", "OpenCV Initialisation Failed");
        }
        else
            Log.d("Check", "OpenCV Initialisation Successful");
    }

    private Button Capture;
    private Button oCVCapture;
    private String TAG = "Check";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Capture = (Button)findViewById(R.id.bCapture);
        oCVCapture = (Button)findViewById(R.id.oCVCapture);
        if(checkCameraHardware(this)) {
            Log.d(TAG, "Camera is available");

            Capture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(MainActivity.this, CameraScreen.class);
                    startActivity(i);
                }
            });

            oCVCapture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(MainActivity.this,OpenCVCameraScreen.class);
                    startActivity(i);
                }
            });


        }
        else {
            Log.d(TAG, "Camera is unavailable");
            Toast.makeText(this,"This app cannot work without a Camera",Toast.LENGTH_LONG).show();
        }
    }

  // @Override
   /* protected void onPause(){
        super.onPause();
       Log.d(TAG,"In onPause");
    if(getCameraInstance()==null) {
        Log.d(TAG,"In onPause if condition");
        preview.mCamera.release();
    }
}*/

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG,"In onResume");

    }

    @Override
    protected void onPostResume(){
        super.onPostResume();
        Log.d(TAG, "In onPostResume");
          //  preview.mCamera = getCameraInstance();

    }
    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d("Check","Camera is not available for use");
        }
        return c; // returns null if camera is unavailable
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
