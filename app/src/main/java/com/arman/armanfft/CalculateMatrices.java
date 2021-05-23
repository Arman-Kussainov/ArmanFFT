package com.arman.armanfft;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import static com.arman.armanfft.MainActivity.TAG;
import static com.arman.armanfft.MainActivity.c_licks;
import static com.arman.armanfft.MainActivity.img;
import static com.arman.armanfft.MainActivity.pi;
import static com.arman.armanfft.MainActivity.source_to_phantom;

public class CalculateMatrices {
    public static void D_gna(Mat g, Mat D) {

        int x_img, y_img;
        double v_alue, gna = 0.0;

        for (x_img = 0; x_img < img.cols(); x_img++) {
            double na = (double) (x_img - img.cols() / 2);
            v_alue = source_to_phantom *
                    Math.pow(1 / (source_to_phantom * source_to_phantom + na * na), 0.5);
            if (Math.abs(na) % 2 != 0) {
                gna = -1.0 / 2.0 * Math.pow(pi * na, -2.0);
            } else {
                gna = 0;
            }
            if (na == 0) {
                gna = 1.0 / 8.0;
            }
            for (y_img = 0; y_img < img.rows(); y_img++) {
                D.put(x_img, y_img, v_alue);
                //g.put(x_img, y_img, gna);
            }
            // just a single line
                g.put(x_img, img.cols() / 2, gna);
            //double[] old=g.get(x_img, 0);
            //Log.v(TAG, na+" "+String.valueOf(old[0]));
        }

    }

    public static void U2(Mat U, int slice_size) {
        // calculate U2 weighting matrix for each s_lice
        //U = new Mat(slice_size, slice_size, CvType.CV_32FC1, Scalar.all(0));
        double s_in = Math.sin((double) c_licks / 180.0 * pi);
        double c_os = Math.cos((double) c_licks / 180.0 * pi);
        int slice_center = slice_size / 2;
        int x_slice, y_slice;
        double U2_value;
        for (x_slice = 0; x_slice < slice_size; x_slice++) {
            for (y_slice = 0; y_slice < slice_size; y_slice++) {
                U2_value = Math.pow(
                        source_to_phantom / (source_to_phantom + (double) (x_slice - slice_center) * s_in +
                                (double) (y_slice - slice_center) * c_os),
                        2.0);
                U.put(x_slice, y_slice, U2_value);
                //double[] old=U.get(x_slice, y_slice);
                //Log.v(TAG, String.valueOf(old[0]));
            }
        }
    }
}
