package gui.utils;

import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

/**
 * 处理图片
 */
public class ImageUtils {
    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    public static Image toBufferedImage(Mat matrix) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (matrix.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
        byte[] buffer = new byte[bufferSize];
        matrix.get(0, 0, buffer); // 获取所有的像素点
        BufferedImage image = new BufferedImage(matrix.cols(), matrix.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }

    /**
     * BufferedImage转换成Mat
     *
     * @param original 要转换的BufferedImage
     *                 imgType  bufferedImage的类型 如 BufferedImage.TYPE_3BYTE_BGR
     *                 matType  转换成mat的type 如 CvType.CV_8UC3
     */
    public static Mat bufImg2Mat(BufferedImage original) {
        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }

        // Don't convert if it already has correct type
        if (original.getType() != BufferedImage.TYPE_3BYTE_BGR) {

            // Create a buffered image
            BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

            // Draw the image onto the new buffer
            Graphics2D g = image.createGraphics();
            try {
                g.setComposite(AlphaComposite.Src);
                g.drawImage(original, 0, 0, null);
            } finally {
                g.dispose();
            }
        }
        byte[] pixels = ((DataBufferByte) original.getRaster().getDataBuffer()).getData();
        Mat mat = Mat.eye(original.getHeight(), original.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        return mat;
    }

    /**
     * 1、读取图片文件
     *
     * @param imagePath 图片路径
     * @return 图片Mat对象
     */
    public static Mat readImage(String imagePath) {
        // 1、 读取图片
        Mat srcMat = Imgcodecs.imread(imagePath);
        System.out.println(srcMat);
        if (srcMat.dataAddr() == 0) {
            System.out.println("打开文件出错");
            return null;
        }
        return srcMat;
    }

    /**
     * 2、图片灰度化,就是把图片转换为黑白照
     *
     * @param srcMat 图片路径
     * @return 图片Mat对象
     */
    public static Mat grayImage(Mat srcMat) {
        // 2、图片灰度化,就是把图片转换为黑白照
        Mat grayImage = new Mat();
        Imgproc.cvtColor(srcMat, grayImage, Imgproc.COLOR_RGB2GRAY);
        return grayImage;
    }

    /**
     * 3、使用Canndy检测边缘
     *
     * @param srcMat 图片路径
     * @return 图片Mat对象
     */
    public static Mat cannyImage(Mat srcMat) {
        Mat cannyImage = new Mat();
        double lowThresh = 500;//双阀值抑制中的低阀值
        double heightThresh = 200;//双阀值抑制中的高阀值
        Imgproc.Canny(srcMat, cannyImage, lowThresh, heightThresh, 3);
        return cannyImage;
    }

    /**
     * 4、形态学（膨胀腐蚀）处理
     *
     * @param srcMat 图片路径
     * @return 图片Mat对象
     */
    public static Mat blurryImage(Mat srcMat) {
        // 图片膨胀处理
        // 膨胀
        Mat dilateImage = new Mat();
        // 侵蚀
        Mat erodeImage = new Mat();
        Mat elementX = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(25, 3));
        Mat elementY = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 17));
        Point point = new Point(-1, -1);

        // 自定义 核进行 x 方向的膨胀腐蚀
        Imgproc.dilate(srcMat, dilateImage, elementX, point, 2);
        Imgproc.erode(dilateImage, erodeImage, elementX, point, 4);
        Imgproc.dilate(erodeImage, dilateImage, elementX, point, 2);

        // 自定义 核进行 Y 方向的膨胀腐蚀
        Imgproc.erode(dilateImage, erodeImage, elementY, point, 1);
        Imgproc.dilate(erodeImage, dilateImage, elementY, point, 2);

        // 噪声处理
        // 平滑处理 中值滤波
        Mat blurryImage = new Mat();
        Imgproc.medianBlur(dilateImage, blurryImage, 5);
        Imgproc.medianBlur(blurryImage, blurryImage, 5);

        return blurryImage;
    }

    /**
     * 5、轮廓处理
     *
     * @param srcMat 图片路径
     * @return 图片Mat对象
     */
    public static Mat roiGrayImage(Mat srcMat) {
        // 矩形轮廓查找与筛选：
        Mat contourImage;
        Mat hierarchyImage = new Mat();
        // 深拷贝，查找轮廓会改变源图像信息，需要重新拷贝图像
        contourImage = srcMat.clone();
        //查找轮廓 提取最外层的轮廓  将结果变成点序列放入 集合
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
        Imgproc.drawContours(contourImage, contours, -1, new Scalar(0, 0, 255), 1);
        // 轮廓表示为一个矩形  车牌提取
        Mat roiGrayImage = new Mat();
        List<MatOfPoint> vec_sobel_roi = new ArrayList<MatOfPoint>();
        for (MatOfPoint contour : contours) {
            Rect rectMin = Imgproc.boundingRect(contour);
            System.out.println("height = " + rectMin.height
                    + "  width = " + rectMin.width +
                    " rate = " + ((float) rectMin.width / rectMin.height));
            //  筛选小于车牌大小的区域,
            //  现行的九二式机动车号牌国标尺寸蓝牌和黑牌是 440 × 140，
            //中国车牌标准440mm*140mm
//            //  大车牌（黄牌）前牌尺寸同，后牌为440×220；
//            //  摩托车及轻便摩托车前牌是220×95，后牌是220×140。
//            if ((float) rectMin.width / rectMin.height >= 1.8
//                    && (float) rectMin.width / rectMin.height <= 3.3) {
//                System.out.println("r.x = " + rectMin.x + "  r.y  = " + rectMin.y);
//                Imgproc.rectangle(srcMat, rectMin, new Scalar(0, 0, 255), 2);
//                roiGrayImage = srcMat.submat(rectMin);
//                vec_sobel_roi.add(contour);
//            }

            if ((float) rectMin.width / rectMin.height >= 2.2
                    && (float) rectMin.width / rectMin.height <= 3.3) {
                System.out.println("r.x = " + rectMin.x + "  r.y  = " + rectMin.y);
                Imgproc.rectangle(srcMat, rectMin, new Scalar(0, 0, 255), 2);
                roiGrayImage = srcMat.submat(rectMin);
                vec_sobel_roi.add(contour);
            }
        }
        return roiGrayImage;
    }


    /**
     * 6、自适应二值化处理
     *
     * @param srcMat 图片路径
     * @return 图片Mat对象
     */
    public static Mat roiThreadHoldImage(Mat srcMat) {
        // 6、自适应二值化处理
        //Candy 边缘检测
        Mat candyRoiImage = new Mat();
        Imgproc.Canny(srcMat, candyRoiImage, 500, 120, 3);
        //二值化
        Mat roiThreadHoldImage = new Mat();
        Imgproc.threshold(candyRoiImage, roiThreadHoldImage, 50, 255, Imgproc.THRESH_BINARY);

        return roiThreadHoldImage;
    }
}
