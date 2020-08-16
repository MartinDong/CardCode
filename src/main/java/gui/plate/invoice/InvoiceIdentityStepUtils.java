package gui.plate.invoice;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * 发票识别步骤拆解
 */
public class InvoiceIdentityStepUtils {
    /**
     * 轮廓颜色
     */
    private static Scalar profileColor = new Scalar(0, 0, 255);

    /**
     * 轮廓宽度
     */
    private static int profileWidth = 3;

    /**
     * 读取图片文件
     *
     * @param imagePath 图片路径
     * @return 图片Mat对象
     */
    public static Mat readImage(String imagePath) {
        return Imgcodecs.imread(imagePath);
    }

    /**
     * 图片灰度化,就是把图片转换为黑白照
     *
     * @param srcMat 图片路径
     * @return 图片Mat对象
     */
    public static Mat grayImage(Mat srcMat) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(srcMat, grayImage, Imgproc.COLOR_BGR2GRAY);
        return grayImage;
    }

    /**
     * 高斯降噪
     *
     * @param srcMat 图片路径
     * @return 图片Mat对象
     */
    public static Mat blurryImage(Mat srcMat) {
        Mat blurryImage = new Mat();
        Imgproc.GaussianBlur(srcMat, blurryImage, new Size(3, 3), 2, 2);
        return blurryImage;
    }

    /**
     * 使用Canndy检测边缘
     *
     * @param srcMat 图片路径
     * @return 图片Mat对象
     */
    public static Mat cannyImage(Mat srcMat) {
        Mat cannyImage = new Mat();
        Imgproc.Canny(srcMat, cannyImage, 20, 60, 3, false);
        return cannyImage;
    }

    /**
     * 膨胀，连接边缘
     *
     * @param srcMat 图片路径
     * @return 图片Mat对象
     */
    public static Mat dilateImage(Mat srcMat) {
        // 膨胀
        Mat dilateImage = new Mat();
        Imgproc.dilate(srcMat, dilateImage, new Mat(), new Point(-1, -1), 3, 1, new Scalar(1));
        return dilateImage;
    }

    /**
     * 寻找轮廓
     *
     * @param srcMat 图片
     * @return 图片Mat对象
     */
    public static List<MatOfPoint> findContours(Mat srcMat, Mat newImage) {
        // 寻找轮廓
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(newImage, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        // 在临时图片上画上轮廓
        Imgproc.drawContours(srcMat, contours, -1, profileColor, profileWidth);
        return contours;
    }

    /**
     * 将凸包转为矩形
     *
     * @param contours 轮廓集合
     * @return 图片Mat对象
     */
    public static Point[] findMaxContours(Mat image, List<MatOfPoint> contours) {
        // 寻找最大面积的轮廓
        int maxIndex = 0;
        for (int i = 0; i < contours.size(); i++) {
            Rect rect = Imgproc.boundingRect(contours.get(i));
            Rect maxRect = Imgproc.boundingRect(contours.get(maxIndex));
            if (rect.width * rect.height >= maxRect.width * maxRect.height) {
                maxIndex = i;
            }
        }
        // 用凸包计算出新的轮廓点
        MatOfInt hull = new MatOfInt();
        Imgproc.convexHull(contours.get(maxIndex), hull, false);
        // 取出凸包的点
        Point[] contourPoints = contours.get(maxIndex).toArray();
        int[] indices = hull.toArray();
        List<Point> newPoints = new ArrayList<Point>();
        for (int index : indices) {
            newPoints.add(contourPoints[index]);
        }
        // 将凸包转为矩形
        MatOfPoint2f contourHull = new MatOfPoint2f();
        contourHull.fromList(newPoints);
        RotatedRect rotatedRect = Imgproc.minAreaRect(contourHull);
        // 根据矩阵的角度，排序点
        Point[] rotatedRectPoint = sortCorners(rotatedRect);
        // 在临时图片上画上旋转矩阵
        for (int i = 0; i < 4; i++) {
            Imgproc.line(image, rotatedRectPoint[i], rotatedRectPoint[(i + 1) % 4], new Scalar(0, 0, 255), profileWidth);
        }
        return rotatedRectPoint;
    }

    /**
     * 提取图像
     */
    public static Mat findQuadContours(Mat image, Point[] rotatedRectPoint) {
        // 计算目标图像的尺寸
        Point p0 = rotatedRectPoint[0];
        Point p1 = rotatedRectPoint[1];
        Point p2 = rotatedRectPoint[2];
        Point p3 = rotatedRectPoint[3];
        // 计算边长
        double space0 = getSpacePointToPoint(p0, p1);
        double space1 = getSpacePointToPoint(p1, p2);
        double space2 = getSpacePointToPoint(p2, p3);
        double space3 = getSpacePointToPoint(p3, p0);
        double imgWidth = Math.max(space0, space2);
        double imgHeight = Math.max(space1, space3);
        // 指定旋转点
        Mat quad = Mat.zeros((int) imgHeight * 2, (int) imgWidth * 2, CvType.CV_8UC3);
        MatOfPoint2f cornerMat = new MatOfPoint2f(p0, p1, p2, p3);
        MatOfPoint2f quadMat = new MatOfPoint2f(new Point(0, 0),
                new Point(imgWidth, 0),
                new Point(imgWidth, imgHeight),
                new Point(0, imgHeight));
        // 提取图像
        Mat transmtx = Imgproc.getPerspectiveTransform(cornerMat, quadMat);
        Imgproc.warpPerspective(image, quad, transmtx, quad.size());

        Rect rect = Imgproc.boundingRect(quadMat);
        Mat resultMat = new Mat(quad, rect);
        return resultMat;
    }


    /**
     * 对多个点按顺时针排序
     *
     * @param rotatedRect
     */
    private static Point[] sortCorners(RotatedRect rotatedRect) {
        Point[] rotatedRectPoint = new Point[4];
        // 旋转角度
        double angle = Math.abs(rotatedRect.angle);// / Math.PI * 180.0;
//        angle = angle > 90 ? angle - 90 : angle;
        // 获取4个点
        rotatedRect.points(rotatedRectPoint);
        if (rotatedRect.size.width <= rotatedRect.size.height && angle >= 45 && angle <= 90) {
            // 正常摆放的发票且左边略微抬起
            swapPoint(rotatedRectPoint, 0, 2);
            swapPoint(rotatedRectPoint, 1, 3);
        } else if (rotatedRect.size.width >= rotatedRect.size.height && angle >= 0 && angle <= 45) {
            // 正常摆放的发票且右边略微抬起
            swapPoint(rotatedRectPoint, 0, 1);
            swapPoint(rotatedRectPoint, 1, 2);
            swapPoint(rotatedRectPoint, 2, 3);
        } else if (rotatedRect.size.width <= rotatedRect.size.height && angle >= 0 && angle <= 45) {
            // 竖着摆放的发票且右边略微抬起
            swapPoint(rotatedRectPoint, 0, 2);
            swapPoint(rotatedRectPoint, 1, 3);
        } else if (rotatedRect.size.width >= rotatedRect.size.height && angle >= 45 && angle <= 90) {
            // 竖着摆放的发票且左边略微抬起
            swapPoint(rotatedRectPoint, 0, 3);
            swapPoint(rotatedRectPoint, 3, 2);
            swapPoint(rotatedRectPoint, 2, 1);
        }
        return rotatedRectPoint;
    }

    /**
     * 交换坐标
     */
    private static void swapPoint(Point[] points, int a, int b) {
        Point tmp = points[a];
        points[a] = points[b];
        points[b] = tmp;
    }

    /**
     * 点到点的距离
     */
    private static double getSpacePointToPoint(Point p1, Point p2) {
        double a = p1.x - p2.x;
        double b = p1.y - p2.y;
        return Math.sqrt(a * a + b * b);
    }
}
