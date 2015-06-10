package camdet.credr.abhilashkulkarni.cameradetector;

import android.content.Context;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
/**
 * Created by Abhilash Kulkarni on 30-May-15.
 */
public class OpenCVCameraView implements CameraBridgeViewBase.CvCameraViewListener2 {
    private CameraBridgeViewBase mOpenCVView;
    public CameraBridgeViewBase mOpenCVCameraView;

    public OpenCVCameraView(Context context){

    }

    public void setView(int id){

    }

    public void setViewBase(CameraBridgeViewBase base){

    }
    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return null;
    }
}
