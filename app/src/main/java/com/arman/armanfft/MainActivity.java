package com.arman.armanfft;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;

public class MainActivity extends AppCompatActivity {
    public static int c_licks = -10;
    public static final String TAG = "CCCP";
    public static Mat img;
    public static Mat slice;
    public static Mat slice_sum;
    public static int slice_size = 512;

    public static Bitmap slice_bitmap;
    public static double source_to_phantom = 2000; // corresponds to D value in book
    public static double phantom_to_detector = 1040;
    public static double pi = 3.14159265359;
    public static Mat D;
    public static Mat g;
    public static Mat U;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isReadStoragePermissionGranted();
        isWriteStoragePermissionGranted();

        OpenCVLoader.initDebug();

        slice = new Mat(slice_size, slice_size, CvType.CV_32FC1, Scalar.all(0));
        slice_sum = new Mat(slice_size, slice_size, CvType.CV_32FC1, Scalar.all(0));
        slice_bitmap = Bitmap.createBitmap(slice.cols(), slice.rows(), Bitmap.Config.ARGB_8888);

        String fileName = "projection_";
        c_licks = 0;
        String completePath = "/sdcard/Download/" + fileName + c_licks + ".png";


        img = Imgcodecs.imread(completePath, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

        // calculate D weighting matrix
        // calculate g(na) filter in real space (may be load it next time?)
        D = new Mat(img.cols(), img.rows(), CvType.CV_32FC1, Scalar.all(0));
        g = new Mat(img.cols(), img.rows(), CvType.CV_32FC1, Scalar.all(0));
        CalculateMatrices.D_gna(g, D);

/*
        int x_img;
        for (x_img = 0; x_img < img.cols(); x_img++) {
            double na = (double) (x_img - img.cols() / 2);
            double[] old = g.get(x_img, img.cols() / 2);
            Log.v(TAG, na + " " + old[0]);
        }
*/

        ImageView imageView = findViewById(R.id.Pic);
        U = new Mat(slice_size, slice_size, CvType.CV_32FC1, Scalar.all(0));
        for (c_licks = 0; c_licks <= 0; c_licks += 10) {
            completePath = "/sdcard/Download/" + fileName + c_licks + ".png";
            img = Imgcodecs.imread(completePath, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
            img.convertTo(img, CvType.CV_32FC1);
            img = img.mul(D);
            img = Convolve.getconvolved(img, g);

            slice = Backprojection.getprojected(img, slice, c_licks);
            CalculateMatrices.U2(U, slice_size);
            slice = slice.mul(U);
            Core.add(slice_sum, slice, slice_sum);
        }
        DisplayMat.display_float(slice_sum, imageView);

/*
        int x_img, y_img;
        double sum = 0;
        int x_center = img.cols() / 2;
        int y_center = img.cols() / 2;
        for (x_img = 0; x_img < img.cols(); x_img++) {
            for (y_img = 0; y_img < img.cols(); y_img++) {
                if ((Math.abs(x_img - x_center) <= 10) && (Math.abs(y_img - y_center) <=10)) {
                    Log.v(TAG, String.valueOf(x_img) +" "+ String.valueOf(y_img));
                    g.put(x_img, y_img, 100);
                } else {
                    g.put(x_img, y_img, 0);
                }
                //  double na = (double) (x_img - img.cols() / 2);
                //double[] old = g.get(x_img, y_img);
                //sum+=old[0];
                //if(old[0]!=0){
                //Log.v(TAG, String.valueOf(x_img) +" "+ String.valueOf(y_img)+" "+ String.valueOf(old[0])+" "+sum);}
            }
        }
*/
        //g = DFT.FFT(g);

/*
        int x_img;
        for (x_img = 0; x_img < img.cols(); x_img++) {
            double na = (double) (x_img - img.cols() / 2);
            double[] old = g.get(x_img, img.cols() / 2);
            Log.v(TAG, na + " " + old[0]);
        }
*/

//        DisplayMat.display_float(g, imageView);
    }


    public void displayToast(View view) {
        ImageView imageView = findViewById(R.id.Pic);
        imageView.setImageBitmap(slice_bitmap);
    }

    public boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission to READ EXTERNAL STORAGE is granted");
                return true;
            } else {

                Log.v(TAG, "Permission to READ EXTERNAL STORAGE is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "permission to READ EXTERNAL STORAGE is automatically granted on sdk<23 upon installation");
            return true;
        }
    }

    public boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission to WRITE EXTERNAL STORAGE is granted");
                return true;
            } else {

                Log.v(TAG, "Permission to WRITE EXTERNAL STORAGE is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "permission to WRITE EXTERNAL STORAGE is automatically granted on sdk<23 upon installation");
            return true;
        }
    }
}