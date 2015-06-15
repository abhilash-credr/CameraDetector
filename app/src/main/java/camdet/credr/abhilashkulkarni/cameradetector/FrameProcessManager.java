package camdet.credr.abhilashkulkarni.cameradetector;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;

import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.addWeighted;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2RGBA;
import static org.opencv.imgproc.Imgproc.cvtColor;

/**
 * Created by Abhilash Kulkarni on 15-Jun-15.
 */
public class FrameProcessManager{
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
    private String TAG = "Check";
    public static interface Processed {
        public void onProcessComplete(Mat outputFrame);
    }

    public void addFrameToQueue(CameraTraceView.CvCameraViewFrame inputFrame, Processed listener) {
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
            Imgproc.Canny(canny, canny, 60, 100, 3, true);
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
            listener.onProcessComplete(merged);}
        else
            listener.onProcessComplete(real);
    }


    public void onCameraViewStarted(int width, int height) {

    }

    public void onCameraViewStopped() {

    }
}