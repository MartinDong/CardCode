package gui.utils;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.opencv.core.CvType.CV_8U;
import static org.opencv.highgui.HighGui.imshow;
import static org.opencv.imgproc.Imgproc.*;

/**
 * 使用颜色定位车牌
 */
public class CarColorPlateLocationUtils {

    public static void plateLocate(Mat srcImage, List<Mat> plates) {
//        Mat srcShowImage = new Mat();
//        srcImage.copyTo(srcShowImage);
//        //imshow("a",srcImage);
//        int cPointB, cPointG, cPointR;
//        for (int i = 1; i < srcImage.rows(); i++) {
//            for (int j = 1; j < srcImage.cols(); j++) {
//                // 获取每个像素
//                double[] clone = srcImage.get(i, j).clone();
//
//                cPointB = (int) clone[0];
//                cPointG = (int) clone[1];
//                cPointR = (int) clone[2];
//                if (cPointB > 80 & cPointR < 80 & cPointG < 80)    //提取蓝色。将该区域设置为黑色
//                {
//                    clone[0] = 0;
//                    clone[1] = 0;
//                    clone[2] = 0;
//                } else if (cPointB > 200 & cPointR > 200 & cPointG > 200)  //提取白色，将其设置为黑色
//                {
//                    clone[0] = 0;
//                    clone[1] = 0;
//                    clone[2] = 0;
//                } else {
//                    clone[0] = 255;
//                    clone[1] = 255;
//                    clone[2] = 255;
//                }
//
//            }
//        }
//
//        cvtColor(srcImage, srcImage, Imgproc.COLOR_BGR2GRAY);
//        threshold(srcImage, srcImage, 127, 255, 0);
//
//
//        //使用差分法。去掉不相关的区域。
//        for (int i = 1; i < srcImage.rows(); i++) {
//            for (int j = 1; j < srcImage.cols() - 1; j++) {
//                srcImage.get(i, j) = srcImage.get(i, j + 1) - srcImage.get(i, j);
//            }
//        }
//
//        threshold(srcImage, srcImage, 127, 255, 1);//通过二值化的方式来取反。
//        //erode(srcImage,srcImage,Mat(5,5,CV_8U),Point(-1,-1),2);  //腐蚀
//        //	dilate(src,src,Mat(5,5,CV_8U),Point(-1,-1),2); //膨胀
//        //	morphologyEx(src,src,MORPH_OPEN,Mat(3,3,CV_8U),Point(-1,-1),1);   //开运算
//        //	 morphologyEx(src,src,MORPH_CLOSE,Mat(3,3,CV_8U),Point(-1,-1),1);  //闭运算
//        erode(srcImage, srcImage, new Mat(3, 3, CV_8U), new Point(-1, -1), 5);
//        threshold(srcImage, srcImage, 127, 255, 1);
//        imshow("a", srcImage);
//        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        Mat hierarchy = new Mat();
//        findContours(srcImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, 2, new Point(0, 0));
//        Random random = new Random();
//        Scalar color = new Scalar(random.nextInt(255), random.nextInt(255), random.nextInt(255));
//        for (int i = 0; i < contours.size(); i++) {
//            //使用边界框的方式
//            Rect aRect = boundingRect(contours.get(i));
//            int tmparea = aRect.height * aRect.height;
//            if (((double) aRect.width / (double) aRect.height > 2) && ((double) aRect.width / (double) aRect.height < 6) && tmparea >= 2000 && tmparea <= 25000) {
//                rectangle(srcShowImage, new Point(aRect.x, aRect.y), new Point(aRect.x + aRect.width, aRect.y + aRect.height), color, 2);
//                //cvDrawContours( dst, contours, color, color, -1, 1, 8 );
//            }
//        }
//
//
//        imshow("da", srcShowImage);
    }
}
