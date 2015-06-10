package camdet.credr.abhilashkulkarni.cameradetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
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

import static org.opencv.core.Core.addWeighted;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2RGBA;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.findContours;


public class OpenCVCameraScreen extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private String TAG = "Check";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Display display;
    private boolean canShow = true, canCopy = true, copied = true;
    private int k = 0;
    private int i = 10, width = 640, height = 480,j,largest_area=0,largest_contour_index = 0,area = 0;
    private int KERNEL_SIZE = 9, BOX_SIZE = 4, vals;
    private ArrayList<double[][]> window;
    private int x_val,y_val;
    private int[] chainCode;
    private Rect bounding_rect;
    private double value[];
    private int NUMBER_OF_CORES;
    private int[] initialPoint;
    private Mat real,merged,canny,dst,thr;
    private Bitmap bmap;
    private File imagesFolder;
    private String name;
    private Menu menu;
    private MenuItem menuitem;
    private List<MatOfPoint> contours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called OpenCV onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_open_cvcamera_screen);
        display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
       // mOpenCvCannyView = (CameraBridgeViewBase) findViewById(R.id.CannyOpenCvView);
        window = new ArrayList<>();
        Log.d(TAG,"Initial Size: " + window.size());
        initialPoint = new int[2];
        k = 0;
        mOpenCvCameraView.setMaxFrameSize(640,480);
        File f = new File(Environment.getExternalStorageDirectory() + "/CredRImages");
        if (f.exists()) {
            imagesFolder = f;
        } else {
            imagesFolder = new File(Environment.getExternalStorageDirectory(), "CredRImages");
            imagesFolder.mkdirs();
        }
       // mOpenCvCannyView.setMaxFrameSize(640,480);

        mOpenCvCameraView.setCvCameraViewListener(this);
        //mOpenCvCannyView.setCvCameraViewListener(this);
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


    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    public Mat onCameraFrame(Mat inputFrame) {
        return null;
    }


   /* @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        return inputFrame.rgba();
    }*/
   @Override
   public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
       //gray = inputFrame.gray();
       real = inputFrame.rgba();
       if (i == 10) {
           if(copied) {
               canCopy = true;
               copied = false;
           }
           contours = new ArrayList<MatOfPoint>();
           merged = real.clone();
           canny = inputFrame.gray();
           dst = new Mat(canny.rows(),canny.cols(), CvType.CV_8UC1);
           Imgproc.Canny(canny, canny, 60, 80, 3, true);
           if(canCopy) {
               thr = canny.clone();
               dst = canny.clone();

           }
           Imgproc.findContours(dst, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
           cvtColor(canny, canny, COLOR_GRAY2RGBA, 4);
           Log.d(TAG, "Countours Size: " + contours.size());
           largest_area=0;
           largest_contour_index = 0;
           for(int j=0; j< contours.size();j++) {
               area = (int) Imgproc.contourArea(contours.get(j));
               //.out.println(Imgproc.contourArea(contours.get(i))
               if (area < 200)
                   continue;
               if (area > largest_area) {
                   largest_area = area;
                   largest_contour_index = j;
               } else
                   continue;
           }
               window = new ArrayList<>();
               Log.e("Values","Point: " + contours.get(largest_contour_index).get(0,0)[0] + " : " + contours.get(largest_contour_index).get(0,0)[1]);
                   Log.e("Values", "Index:" + largest_contour_index);
                   double[][] data = new double[BOX_SIZE][BOX_SIZE];
                   for(int x = 0; x < KERNEL_SIZE;x++) {
                       for (int y = 0; y < BOX_SIZE; y++)
                           for (int z = 0; z < BOX_SIZE; z++) {
                               try {
                                   switch(x){
                                       case 0:
                                           x_val = y + BOX_SIZE + (int)contours.get(largest_contour_index).get(0,0)[0];
                                           y_val = z + (int)contours.get(largest_contour_index).get(0,0)[1];
                                           break;
                                       case 1:
                                           x_val = y + BOX_SIZE + (int)contours.get(largest_contour_index).get(0,0)[0];
                                           y_val = z + BOX_SIZE + (int)contours.get(largest_contour_index).get(0,0)[1];
                                           break;
                                       case 2:
                                           x_val = y  + (int)contours.get(largest_contour_index).get(0,0)[0];
                                           y_val = z + BOX_SIZE + (int)contours.get(largest_contour_index).get(0,0)[1];
                                           break;
                                       case 3:
                                           x_val = y - BOX_SIZE + (int)contours.get(largest_contour_index).get(0,0)[0];
                                           y_val = z + BOX_SIZE + (int)contours.get(largest_contour_index).get(0,0)[1];
                                           break;
                                       case 4:
                                           x_val = y - BOX_SIZE + (int)contours.get(largest_contour_index).get(0,0)[0];
                                           y_val = z + (int)contours.get(largest_contour_index).get(0,0)[1];
                                           break;
                                       case 5:
                                           x_val = y - BOX_SIZE + (int)contours.get(largest_contour_index).get(0,0)[0];
                                           y_val = z - BOX_SIZE + (int)contours.get(largest_contour_index).get(0,0)[1];
                                           break;
                                       case 6:
                                           x_val = y  + (int)contours.get(largest_contour_index).get(0,0)[0];
                                           y_val = z - BOX_SIZE +  (int)contours.get(largest_contour_index).get(0,0)[1];
                                           break;
                                       case 7:
                                            x_val = y + BOX_SIZE + (int)contours.get(largest_contour_index).get(0,0)[0];
                                            y_val = z - BOX_SIZE + (int)contours.get(largest_contour_index).get(0,0)[1];
                                           break;
                                   }
                                   data[y][z] = thr.get(x_val,y_val)[0];
                               } catch (NullPointerException e) {
                                   Log.e(TAG, "Error");
                               }
                           }
                       Log.d("Values","Data " + x + " added");
                       window.add(x, data);
                   }
                vals = 0;
               for(int k = 0;k < window.size();k++)
                   for(int l = 0;l < 4;l++)
                       for(int m = 0;m < 4;m++) {
                           if(window.get(k)[l][m]==255.0)
                               //Log.d("Values", "Vector: " + k + " Row: " + l + " Column: " + m + " Value: " + window.get(k)[l][m]);
                                vals++;
                       }
                Log.d("Values", "No of values: " + vals);
                copied = true;

           if(contours.size() != 0) {
               Rect rect = Imgproc.boundingRect(contours.get(largest_contour_index));

               //System.out.println(rect.height);
               //Log.d(TAG,rect.x + "," + rect.y + "," + rect.height + "," + rect.width);
               Imgproc.rectangle(canny, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255));
           }
               }
       try {
           addWeighted(real, 1.0, canny, 1.0, 0.0, merged);
       }
       catch(CvException e){
           Log.e(TAG,e.getMessage());
       }
           i--;
           if (i == 0)
               i = 10;


       k = 1;

       if(canShow) {
       return merged;}
       else
           return real;
   }

    private void setvalues(double[] val) {
        Log.d("Values","Length: " + dst.get(0,0).length);
        double[][] data = new double[BOX_SIZE][BOX_SIZE];
        for(int x = 0; x < KERNEL_SIZE;x++) {
            for (int y = 0; y < BOX_SIZE; y++)
                for (int z = 0; z < BOX_SIZE; z++) {
                    try {
                        data[y][z] = thr.get(y + (int) val[0] - 1, z + (int) val[1] - 1)[0];
                    } catch (NullPointerException e) {
                        Log.d(TAG, "Error");
                    }
                }
            window.add(x, data);
        }
    }

    private void outputvalues() {
        for(int k = 0;k < window.size();k++)
            for(int l = 0;l < 4;l++)
                for(int m = 0;m < 4;m++) {
                    if(window.get(k)[l][m]==255.0)
                        Log.d("Values", "Vector: " + k + " Row: " + l + " Column: " + m + " Value: " + window.get(k)[l][m]);
                }
    }

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
            Log.d(TAG, String.valueOf(merged.cols()) + " : " + String.valueOf(merged.rows()));
            j = imagesFolder.listFiles().length;
            Log.d(TAG, "Number of Files is: " + String.valueOf(j));
            name = "CredR_OpenCV_" + String.valueOf(j) + ".jpg";
            File f = new File(imagesFolder, name);
            bmap = Bitmap.createBitmap(merged.cols(), merged.rows(), Bitmap.Config.ARGB_8888);
            Toast.makeText(this,"Image Taken: " + name,Toast.LENGTH_LONG).show();
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

            /*Intent intent = new Intent(Intent.ACTION_CHOOSER);
            Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
                    + "/CredRImages/");
            intent.setDataAndType(uri, "text/csv");
            startActivity(Intent.createChooser(intent, "Open folder"));*/

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
