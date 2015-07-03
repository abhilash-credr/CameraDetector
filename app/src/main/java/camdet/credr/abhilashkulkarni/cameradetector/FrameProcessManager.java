package camdet.credr.abhilashkulkarni.cameradetector;

import android.app.Activity;
import android.util.Log;

import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.opencv.core.Core.addWeighted;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2RGBA;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class FrameProcessManager{

    static Activity activity;
    static String TAG = "Check";
    static private List<QueueElement> PendingQueue;
    static private List<QueueElement> ProcessingQueue;
    public FrameProcessManager(Activity context){
        this.activity = context;
        PendingQueue = new LinkedList<>();;
        ProcessingQueue = new LinkedList<>();
    }

    public void onCameraViewStarted(int mFrameWidth, int mFrameHeight) {
    }

    public void onCameraViewStopped() {
    }

    public static interface Processed {
        public void onProcessComplete(Mat outputFrame);
    }

    static class QueueElement {
        static long SEQUENCE = 1;
        long sequenceId;
        CameraTraceView.CvCameraViewFrame inputFrame;
        Mat outputFrame;
        Processed callback;
        boolean canCalculate;
        boolean calculated;

        QueueElement(CameraTraceView.CvCameraViewFrame inputFrame, Processed callback) {
            sequenceId = SEQUENCE++;
            this.inputFrame = inputFrame;
            this.callback = callback;

        }

        public void setCanCalculate(boolean val){
            this.canCalculate = val;
        }
    }
       public Mat getLastFrame(){
            if(PendingQueue.size()>0)
                return PendingQueue.get(0).inputFrame.rgba();
           if(ProcessingQueue.size()>0)
               return ProcessingQueue.get(0).inputFrame.rgba();
           return null;
        }

    public void addFrameToQueue(CameraTraceView.CvCameraViewFrame inputFrame, Processed listener) {
        if(PendingQueue.size() > 50)
        {PendingQueue.remove(PendingQueue.size()-1);}
        PendingQueue.add(new QueueElement(inputFrame, listener));


        Log.d("WTF","PendingQueue: " + PendingQueue.size());
        triggerProcessing();
}

    synchronized private void triggerProcessing() {
        if(PendingQueue.size() == 0 || !ProcessingManager.hasIdleThreads())
            return;
        QueueElement element = PendingQueue.get(0);
        if(ProcessingManager.processFrame(element)) {
            PendingQueue.remove(0);
            ProcessingQueue.add(element);
            Log.d("WTF","ProcessingQueue: " + ProcessingQueue.size());
        }
    }

    static class ProcessingManager {
        static long lastDeliveredSequenceId = 0;
        static List<Worker> workerList = new ArrayList();
        static List<Thread> threadList = new ArrayList();
        static {
            for(int i = 0; i < 4; i++) {
                workerList.add(new Worker());
            }
        }

        static {
            for(int i = 0; i < 4; i++) {
                threadList.add(new Thread());
            }
        }

        static boolean hasIdleThreads() {
            for(Worker worker : workerList) {
                if(!worker.busy)
                    return true;
            }
            return false;
        }

        synchronized static boolean processFrame (QueueElement element) {
            long id;
            for(Worker worker : workerList) {
                if(!worker.busy) {
                    id = element.sequenceId;
                    if(id % 3 == 0)
                        element.canCalculate = true;
                    else
                        element.canCalculate = false;
                    if(id % 3 == 0){
                        worker.setFrameElement(element);
                    Thread WorkerThread = new Thread(worker);
                    WorkerThread.start();}
                    return true;
                }
            }

           /* for(Thread thread : threadList) {
                if (thread.getState() != Thread.State.RUNNABLE){
                    Worker worker = new Worker();
                worker.setFrameElement(element);
                thread.run();
            }


            }*/
            return false;
        }

        synchronized static void processingCompleted(final QueueElement element) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public synchronized void run() {

                        Log.d("WTF2","Element_ID: " + element.sequenceId + " Last_ID: " + lastDeliveredSequenceId);
                        if(element.sequenceId > lastDeliveredSequenceId ){
                        if(element.calculated)
                                element.callback.onProcessComplete(element.outputFrame);
                            lastDeliveredSequenceId = element.sequenceId;
                        }

                            ProcessingQueue.remove(element);
                    }
                });
        }

        public static class Worker implements Runnable {
            private boolean  cannied = true;
            private int k = 0;
            private int largest_area=0,largest_contour_index = 0,area = 0;
            private int KERNEL_SIZE = 9, BOX_SIZE = 4, vals;
            private ArrayList<double[][]> window;
            private int x_val,y_val;
            private Mat real,merged,canny,dst,thr;
            private List<MatOfPoint> contours = new ArrayList<>();
            private long id;

            boolean busy = false;
            boolean check;
            QueueElement element;

            boolean setFrameElement(QueueElement element) {
                if(busy)
                    return false;
                this.element = element;
                return true;
            }

            @Override
            public void run() {
                busy = true;

                real = element.inputFrame.rgba();
                     merged = real.clone();
                    canny = element.inputFrame.gray();
                    thr = element.inputFrame.gray();
                cannied = true;
                Log.d("WTF1","ID: " + element.canCalculate);
               // if(element.canCalculate) {
                    if(cannied) {
                        cannied = false;
                        canny = element.inputFrame.gray();
                        dst = new Mat(canny.rows(), canny.cols(), CvType.CV_8UC1);
                        Imgproc.Canny(canny, canny, 60, 100, 3, true);
                        element.calculated = true;
                        dst = canny.clone();
                    }
                    if(!cannied) {
                        Imgproc.findContours(dst, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                        try {
                            cvtColor(canny, canny, COLOR_GRAY2RGBA, 4);
                        } catch (Exception e) {
                            Log.e("WTF", e.getMessage());
                        }
                        Log.d(TAG, "Countours Size: " + contours.size());

                        largest_area = 0;
                        largest_contour_index = 0;
                        for (int j = 0; j < contours.size(); j++) {
                            area = (int) Imgproc.contourArea(contours.get(j));
                            if (area < 200)
                                continue;
                            if (area > largest_area) {
                                largest_area = area;
                                largest_contour_index = j;
                            } else
                                continue;
                        }
                    }
                    //window = new ArrayList<>();
                    //Log.e("Values","Point: " + contours.get(largest_contour_index).get(0,0)[0] + " : " + contours.get(largest_contour_index).get(0,0)[1]);
                    //Log.e("Values", "Index:" + largest_contour_index);
                    /*if (contours.size() > 0) {
                        double[][] data = new double[BOX_SIZE][BOX_SIZE];
                        for (int x = 0; x < KERNEL_SIZE; x++) {
                            for (int y = 0; y < BOX_SIZE; y++)
                                for (int z = 0; z < BOX_SIZE; z++) {
                                    try {
                                        switch (x) {
                                            case 0:
                                                x_val = y + BOX_SIZE + (int) contours.get(largest_contour_index).get(0, 0)[0];
                                                y_val = z + (int) contours.get(largest_contour_index).get(0, 0)[1];
                                                break;
                                            case 1:
                                                x_val = y + BOX_SIZE + (int) contours.get(largest_contour_index).get(0, 0)[0];
                                                y_val = z + BOX_SIZE + (int) contours.get(largest_contour_index).get(0, 0)[1];
                                                break;
                                            case 2:
                                                x_val = y + (int) contours.get(largest_contour_index).get(0, 0)[0];
                                                y_val = z + BOX_SIZE + (int) contours.get(largest_contour_index).get(0, 0)[1];
                                                break;
                                            case 3:
                                                x_val = y - BOX_SIZE + (int) contours.get(largest_contour_index).get(0, 0)[0];
                                                y_val = z + BOX_SIZE + (int) contours.get(largest_contour_index).get(0, 0)[1];
                                                break;
                                            case 4:
                                                x_val = y - BOX_SIZE + (int) contours.get(largest_contour_index).get(0, 0)[0];
                                                y_val = z + (int) contours.get(largest_contour_index).get(0, 0)[1];
                                                break;
                                            case 5:
                                                x_val = y - BOX_SIZE + (int) contours.get(largest_contour_index).get(0, 0)[0];
                                                y_val = z - BOX_SIZE + (int) contours.get(largest_contour_index).get(0, 0)[1];
                                                break;
                                            case 6:
                                                x_val = y + (int) contours.get(largest_contour_index).get(0, 0)[0];
                                                y_val = z - BOX_SIZE + (int) contours.get(largest_contour_index).get(0, 0)[1];
                                                break;
                                            case 7:
                                                x_val = y + BOX_SIZE + (int) contours.get(largest_contour_index).get(0, 0)[0];
                                                y_val = z - BOX_SIZE + (int) contours.get(largest_contour_index).get(0, 0)[1];
                                                break;
                                        }
                                        data[y][z] = thr.get(x_val, y_val)[0];
                                    } catch (NullPointerException e) {
                                    }
                                }
                            Log.d("Values", "Data " + x + " added");
                            window.add(x, data);
                        }
                        vals = 0;
                        for (int k = 0; k < window.size(); k++)
                            for (int l = 0; l < 4; l++)
                                for (int m = 0; m < 4; m++) {
                                    if (window.get(k)[l][m] == 255.0)
                                        //Log.d("Values", "Vector: " + k + " Row: " + l + " Column: " + m + " Value: " + window.get(k)[l][m]);
                                        vals++;
                                }
                        Log.d("Values", "No of values: " + vals);

                    }

                    if (contours.size() != 0) {
                        Rect rect = Imgproc.boundingRect(contours.get(largest_contour_index));

                        //System.out.println(rect.height);
                        //Log.d(TAG,rect.x + "," + rect.y + "," + rect.height + "," + rect.width);
                        Imgproc.rectangle(canny, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255));
                    }*/

               // }

                try {
                        addWeighted(real, 1.0, canny, 1.0, 0.0, merged);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                check = thr!=canny;
                Log.d("WTF1","Cannied " + check );
                if( element.calculated && check) {
                    element.outputFrame = canny;
                }
                else {
                    element.outputFrame = real;
                }
                processingCompleted(element);
                busy = false;
            }
        }
    }

}
