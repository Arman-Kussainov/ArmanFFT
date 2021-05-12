package com.arman.armanfft;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OpenCVLoader.initDebug();
    }

    public void displayToast(View view){

        Mat img0 = new Mat( 9, 9, CvType.CV_64FC1 );
        int row = 0, col = 0;
        img0.put(row ,col,    0, -1, 0, -1, 5, -1, 0, -1, 0,
                                    0, -1, 0, -1, 5, -1, 0, -1, 0,
                                    0, -1, 0, -1, 5, -1, 0, -1, 0,
                                    0, -1, 0, -1, 5, -1, 0, -1, 0,
                                    0, -1, 0, -1, 5, -1, 0, -1, 0,
                                    0, -1, 0, -1, 5, -1, 0, -1, 0,
                                    0, -1, 0, -1, 5, -1, 0, -1, 0,
                                    0, -1, 0, -1, 5, -1, 0, -1, 0,
                                    0, -1, 0, -1, 5, -1, 0, -1, 0);
        Mat padded = new Mat();                     //expand input image to optimal size
        int m = Core.getOptimalDFTSize( img0.rows() );
        int n = Core.getOptimalDFTSize( img0.cols() ); // on the border add zero values
        Core.copyMakeBorder(img0, padded, 0, m - img0.rows(), 0, n - img0.cols(), Core.BORDER_CONSTANT, Scalar.all(0));
        List<Mat> planes = new ArrayList<Mat>();
        padded.convertTo(padded, CvType.CV_32F);
        planes.add(padded);
        planes.add(Mat.zeros(padded.size(), CvType.CV_32F));
        Mat complexI = new Mat();
        Core.merge(planes, complexI);         // Add to the expanded another plane with zeros
        Core.dft(complexI, complexI);         // this way the result may fit in the source matrix

        Core.split(complexI, planes);  // planes.get(0) = Re(DFT(I)
                                       // planes.get(1) = Im(DFT(I))
        Core.magnitude(planes.get(0), planes.get(1), planes.get(0));// planes.get(0) = magnitude
        Mat magI = planes.get(0);
        Mat matOfOnes = Mat.ones(magI.size(), magI.type());
        Core.add(matOfOnes, magI, magI);         // switch to logarithmic scale
        Core.log(magI, magI);
        // crop the spectrum, if it has an odd number of rows or columns
        magI = magI.submat(new Rect(0, 0, magI.cols() & -2, magI.rows() & -2));
        // rearrange the quadrants of Fourier image  so that the origin is at the image center
        int cx = magI.cols()/2;
        int cy = magI.rows()/2;
        Mat q0 = new Mat(magI, new Rect(0, 0, cx, cy));   // Top-Left - Create a ROI per quadrant
        Mat q1 = new Mat(magI, new Rect(cx, 0, cx, cy));  // Top-Right
        Mat q2 = new Mat(magI, new Rect(0, cy, cx, cy));  // Bottom-Left
        Mat q3 = new Mat(magI, new Rect(cx, cy, cx, cy)); // Bottom-Right
        Mat tmp = new Mat();               // swap quadrants (Top-Left with Bottom-Right)
        q0.copyTo(tmp);
        q3.copyTo(q0);
        tmp.copyTo(q3);
        q1.copyTo(tmp);                    // swap quadrant (Top-Right with Bottom-Left)
        q2.copyTo(q1);
        tmp.copyTo(q2);
        magI.convertTo(magI, CvType.CV_8UC1);
        Core.normalize(magI, magI, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1); // Transform the matrix with float values
        // into a viewable image form (float between
        // values 0 and 255).
        //HighGui.imshow("Input Image"       , img0   );    // Show the result
        //HighGui.imshow("Spectrum Magnitude", magI);
        //HighGui.waitKey();


        Mat img = null;

        try {
            img = Utils.loadResource(getApplicationContext(), R.drawable.he);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2BGRA);

        Mat img_result = img.clone();
        Imgproc.Canny(img, img_result, 80, 90);

        Bitmap img_bitmap = Bitmap.createBitmap(magI.cols(), magI.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(magI, img_bitmap);
        ImageView imageView = findViewById(R.id.img);
        imageView.setImageBitmap(img_bitmap);
    }
}