package com.arman.armanfft;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.minMaxLoc;

public class Convolve {
    public static Mat getconvolved(Mat Rb, Mat gna) {

        List<Mat> planes = new ArrayList<Mat>();
        List<Mat> planes_g = new ArrayList<Mat>();

        planes.add(Rb);
        planes_g.add(gna);

        planes.add(Mat.zeros(Rb.size(), CvType.CV_32F));
        planes_g.add(Mat.zeros(Rb.size(), CvType.CV_32F));

        Mat complexI = new Mat();
        Mat complexI_g = new Mat();

        //double min, max;
        //minMaxLoc(complexI,&min,&max);

        Core.merge(planes, complexI);         // Add to the expanded another plane with zeros
        Core.merge(planes_g, complexI_g);         // Add to the expanded another plane with zeros

        Core.dft(complexI, complexI);         // this way the result may fit in the source matrix
        Core.dft(complexI_g, complexI_g);         // this way the result may fit in the source matrix

        complexI=complexI.mul(complexI_g);
        Core.idft(complexI, complexI);         // this way the result may fit in the source matrix

        Core.split(complexI, planes);  // planes.get(0) = Re(DFT(I)
        // planes.get(1) = Im(DFT(I))
        Core.magnitude(planes.get(0), planes.get(1), planes.get(0));// planes.get(0) = magnitude
        Mat magI = planes.get(0);
        // crop the spectrum, if it has an odd number of rows or columns
        magI = magI.submat(new Rect(0, 0, magI.cols() & -2, magI.rows() & -2));
        return magI;
    }
}
