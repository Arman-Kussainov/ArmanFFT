package com.arman.armanfft;

import android.graphics.Bitmap;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import static com.arman.armanfft.MainActivity.slice_bitmap;


public class DisplayMat {
    public static void display_float(Mat sum, ImageView imageView) {
        //img.convertTo(img, CvType.CV_8UC1);
        Core.normalize(sum, sum, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);
        Utils.matToBitmap(sum, slice_bitmap);
        imageView.setImageBitmap(slice_bitmap);
    }
}
