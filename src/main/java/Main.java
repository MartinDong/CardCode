import gui.view.ImageViewer;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Main {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //注意程序运行的时候需要在VM option添加该行 指明opencv的dll文件所在路径
        //-Djava.library.path=$PROJECT_DIR$\opencv\x64
    }

    public static void main(String[] args) {
        System.out.println("Hello word!");
        String originalImgPath = ".\\src\\main\\resources\\test_img\\test8.jpg";

        // 1、 读取图片
        Mat srcMat = Imgcodecs.imread(originalImgPath);
        System.out.println(srcMat);
        if (srcMat.dataAddr() == 0) {
            System.out.println("打开文件出错");
        }

        // 2、图片灰度化,就是把图片转换为黑白照
        Mat grayImage = new Mat();
        Imgproc.cvtColor(srcMat, grayImage, Imgproc.COLOR_RGB2GRAY);

        // 3、使用Canndy检测边缘
        Mat cannyImage = new Mat();
        double lowThresh = 500;//双阀值抑制中的低阀值
        double heightThresh = 200;//双阀值抑制中的高阀值
        Imgproc.Canny(grayImage, cannyImage, lowThresh, heightThresh, 3);

        // 4、形态学（膨胀腐蚀）处理
        // 图片膨胀处理
        // 膨胀
        Mat dilateImage = new Mat();
        // 侵蚀
        Mat erodeImage = new Mat();
        Mat elementX = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(25, 3));
        Mat elementY = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 17));
        Point point = new Point(-1, -1);

        // 自定义 核进行 x 方向的膨胀腐蚀
        Imgproc.dilate(cannyImage, dilateImage, elementX, point, 2);
        Imgproc.erode(dilateImage, erodeImage, elementX, point, 4);
        Imgproc.dilate(erodeImage, dilateImage, elementX, point, 2);

        // 自定义 核进行 Y 方向的膨胀腐蚀
        Imgproc.erode(dilateImage, erodeImage, elementY, point, 1);
        Imgproc.dilate(erodeImage, dilateImage, elementY, point, 2);

        // 噪声处理
        // 平滑处理 中值滤波
        Mat blurrImage = new Mat();
        Imgproc.medianBlur(dilateImage, blurrImage, 5);
        Imgproc.medianBlur(blurrImage, blurrImage, 5);

        // 展示处理结果
        ImageViewer blurrImageViewer = new ImageViewer(blurrImage, "图片预览");
        blurrImageViewer.imshow();

        // 5、轮廓处理
        // 矩形轮廓查找与筛选：
        Mat contourImage;
        Mat hierarchyImage = new Mat();
        // 深拷贝，查找轮廓会改变源图像信息，需要重新拷贝图像
        contourImage = blurrImage.clone();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(
                contourImage,
                contours,
                hierarchyImage,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_NONE);

        System.out.println("轮廓数量：" + contours.size());
        System.out.println("hierarchy类型：" + hierarchyImage);

        // 画出轮廓
        Imgproc.drawContours(contourImage, contours, -1, new Scalar(255), 1);
        // 轮廓表示为一个矩形  车牌提取
        Mat roiGrayImage = new Mat();
        for (MatOfPoint contour : contours) {
            Rect rectMin = Imgproc.boundingRect(contour);
            System.out.println(
                    "height = " + rectMin.height
                            + "  width = " + rectMin.width +
                            " rate = " + ((float) rectMin.width / rectMin.height)
            );
            // 筛选小于车牌大小的区域,
            //  现行的九二式机动车号牌国标尺寸蓝牌和黑牌是440×140，
            //  大车牌（黄牌）前牌尺寸同，后牌为440×220；
            //  摩托车及轻便摩托车前牌是220×95，后牌是220×140。
            if ((float) rectMin.width / rectMin.height >= 1.8
                    && (float) rectMin.width / rectMin.height <= 3.3) {
                System.out.println("r.x = " + rectMin.x + "  r.y  = " + rectMin.y);
                Imgproc.rectangle(srcMat, rectMin, new Scalar(0, 0, 255), 2);
                roiGrayImage = srcMat.submat(rectMin);
            }

            if ((float) rectMin.width / rectMin.height >= 2.2
                    && (float) rectMin.width / rectMin.height <= 3.3) {
                System.out.println("r.x = " + rectMin.x + "  r.y  = " + rectMin.y);
                Imgproc.rectangle(srcMat, rectMin, new Scalar(0, 0, 255), 2);
                roiGrayImage = srcMat.submat(rectMin);
            }
        }


        // 6、自适应二值化处理
        //Candy 边缘检测
        Mat candyRoiImage = new Mat();
        Imgproc.Canny(roiGrayImage, candyRoiImage, 500, 120, 3);
        //二值化
        Mat roiThreadHoldImage = new Mat();
        Imgproc.threshold(candyRoiImage, roiThreadHoldImage, 50, 255, Imgproc.THRESH_BINARY);

        // 展示处理结果
        ImageViewer imageViewer = new ImageViewer(candyRoiImage, "图片预览");
        imageViewer.imshow();

    }
}
