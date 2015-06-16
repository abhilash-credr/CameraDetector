package camdet.credr.abhilashkulkarni.cameradetector;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class OpenCVCameraScreen extends Activity {
    private String TAG = "WTF";
    private CameraTraceView mOpenCvCameraView;
    private Display display;
    private boolean canShow = true, canCopy = true, copied = true;
    private int k = 0;
    private int i = 10, width = 640, height = 480,j;
    private int KERNEL_SIZE = 9, BOX_SIZE = 4, vals;
    private ArrayList<double[][]> window;
    private FrameProcessManager manager;
    private Menu menu;
    private File imagesFolder;
    private int NUMBER_OF_CORES;
    private String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called OpenCV onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_open_cvcamera_screen);
        display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mOpenCvCameraView = (CameraTraceView) findViewById(R.id.HelloOpenCvView);
        NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
       // mOpenCvCannyView = (CameraBridgeViewBase) findViewById(R.id.CannyOpenCvView);
        window = new ArrayList<>();

        File f = new File(Environment.getExternalStorageDirectory() + "/CredRImages");
        if (f.exists()) {
            imagesFolder = f;
        } else {
            imagesFolder = new File(Environment.getExternalStorageDirectory(), "CredRImages");
            imagesFolder.mkdirs();
        }
        Log.d(TAG,"Initial Size: " + window.size());
        k = 0;
        mOpenCvCameraView.setMaxFrameSize(640,480);

        manager = new FrameProcessManager(this);
        mOpenCvCameraView.setCvCameraViewListener(manager);
    }



    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
       /* if (mOpenCvCannyView != null)
            mOpenCvCannyView.disableView();*/
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
            Log.d(TAG,"OpenCV Camera disabled");
        }
       /* if (mOpenCvCannyView != null)
            mOpenCvCannyView.disableView();*/
    }






   /* @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        return inputFrame.rgba();
    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_open_cvcamera_screen, menu);
        this.menu = menu;
        return true;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                   // mOpenCvCannyView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Bitmap bmap;
            Mat real = manager.getLastFrame();

            if(real == null)
                return true;

            Log.d(TAG, String.valueOf(real.cols()) + " : " + String.valueOf(real.rows()));
            j = imagesFolder.listFiles().length;
            Log.d(TAG, "Number of Files is: " + String.valueOf(j));
            name = "CredR_OpenCV_" + String.valueOf(j) + ".jpg";
            File f = new File(imagesFolder, name);
            bmap = Bitmap.createBitmap(real.cols(), real.rows(), Bitmap.Config.ARGB_8888);
            try {
                Utils.matToBitmap(real, bmap);
            }
            catch (IllegalArgumentException e){
                Log.e(TAG,"OpenCVCameraScreen: 216 " + e.getMessage());
            }

            try {
                f.createNewFile();
            } catch (IOException e) {
                Log.e(TAG,"OpenCVCameraScreen: 221 " + e.getMessage());
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            try {
                bmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            } catch (NullPointerException e) {
                Log.e(TAG,"OpenCVCameraScreen: 229 " + e.getMessage());
            }
            byte[] imgData = bos.toByteArray();
            try {
                FileOutputStream fos = new FileOutputStream(f);

                fos.write(imgData);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Image Taken: " + name, Toast.LENGTH_LONG).show();
        }

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edges) {
           // menuitem = menu.getItem(R.id.action_edges);
            if(canShow) {
                canShow = false;
                item.setTitle(getResources().getString(R.string.cannyFalse));

            }
            else {
                canShow = true;
                item.setTitle(getResources().getString(R.string.canny));
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
