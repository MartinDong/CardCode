package gui.plate.invoice;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static gui.utils.CalculationUtils.*;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;

/**
 * 发票定位识别
 */
public class FaPiaoPlateLocation {
    /**
     * 定位识别
     * 算法的具体步骤如下：
     * <p>
     * 转灰度，降噪
     * 边缘检测
     * 轮廓提取
     * 寻找凸包，拟合多边形
     * 找到最大的正方形
     * 重新执行步骤3，提升精度
     * 找到长方形四条边，即为纸张的外围四边形
     * 透视变换，提取四边形
     *
     * @param srcMat
     */
    public static Mat plateLocate(Mat srcMat) {

        //1、转灰度，降噪====================
        // 彩色转灰度
        Mat gray = new Mat();
        Imgproc.cvtColor(srcMat, gray, Imgproc.COLOR_BGR2GRAY);

        // 高斯滤波，降噪
        Mat dst = new Mat();
        Imgproc.GaussianBlur(gray, dst, new Size(3, 3), 2, 2);


        //2、边缘检测======================
        // Canny边缘检测
        Mat edges = new Mat();
        Imgproc.Canny(dst, edges, 20, 60, 3, false);

        // 膨胀，连接边缘
        Mat dilate = new Mat();
        Imgproc.dilate(edges, dilate, new Mat(), new Point(-1, -1), 3, 1, new Scalar(1));

        //3、轮廓提取=====================
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(dilate, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        for (MatOfPoint contour : contours) {
//            MatOfPoint2f point2f = new MatOfPoint2f(p0, p1, p2, p3);
            MatOfPoint2f point2f = new MatOfPoint2f(contour.toArray());
            RotatedRect rotatedRect = Imgproc.minAreaRect(point2f);
            //绘制边框将符合的轮廓标注出来
            Imgproc.rectangle(srcMat, rotatedRect.boundingRect(), new Scalar(0, 0, 255), 5);

        }
        //4、寻找凸包，拟合多边形==================================
        // 找出轮廓对应凸包的四边形拟合
        List<MatOfPoint> squares = new ArrayList<MatOfPoint>();
        List<MatOfPoint> hulls = new ArrayList<MatOfPoint>();
        MatOfInt hull = new MatOfInt();
        MatOfPoint2f approx = new MatOfPoint2f();
        approx.convertTo(approx, CvType.CV_32F);

        for (MatOfPoint contour: contours) {
            // 边框的凸包
            Imgproc.convexHull(contour, hull);

            // 用凸包计算出新的轮廓点
            Point[] contourPoints = contour.toArray();
            int[] indices = hull.toArray();
            List<Point> newPoints = new ArrayList<Point>();
            for (int index : indices) {
                newPoints.add(contourPoints[index]);
            }
            MatOfPoint2f contourHull = new MatOfPoint2f();
            contourHull.fromList(newPoints);

            // 多边形拟合凸包边框(此时的拟合的精度较低)
            Imgproc.approxPolyDP(contourHull, approx, Imgproc.arcLength(contourHull, true)*0.02, true);

            // 筛选出面积大于某一阈值的，且四边形的各个角度都接近直角的凸四边形
            MatOfPoint approxf1 = new MatOfPoint();
            approx.convertTo(approxf1, CvType.CV_32S);
            if (approx.rows() == 4 && Math.abs(Imgproc.contourArea(approx)) > 40000 &&
                    Imgproc.isContourConvex(approxf1)) {
                double maxCosine = 0;
                for (int j = 2; j < 5; j++) {
                    double cosine = Math.abs(getAngle(approxf1.toArray()[j%4], approxf1.toArray()[j-2], approxf1.toArray()[j-1]));
                    maxCosine = Math.max(maxCosine, cosine);
                }
                // 角度大概72度
                if (maxCosine < 0.3) {
                    MatOfPoint tmp = new MatOfPoint();
                    contourHull.convertTo(tmp, CvType.CV_32S);
                    squares.add(approxf1);
                    hulls.add(tmp);
                }
            }
        }

        // 5、找到最大的正方形==================================
        // 找出外接矩形最大的四边形
        int index = findLargestSquare(squares);
        if (squares.isEmpty()) {
            System.out.println("无法找到矩形");
            return srcMat;
        }
        MatOfPoint largest_square = squares.get(index);
        if (largest_square.rows() == 0 || largest_square.cols() == 0)
            return srcMat;


        //6、重新执行步骤3，提升精度==================================
        // 找到这个最大的四边形对应的凸边框，再次进行多边形拟合，此次精度较高，拟合的结果可能是大于4条边的多边形
        MatOfPoint contourHull = hulls.get(index);
        MatOfPoint2f tmp = new MatOfPoint2f();
        contourHull.convertTo(tmp, CvType.CV_32F);
        Imgproc.approxPolyDP(tmp, approx, 3, true);
        List<Point> newPointList = new ArrayList<Point>();
        double maxL = Imgproc.arcLength(approx, true) * 0.02;

        //7、找到长方形四条边，即为纸张的外围四边形==================================
        // 找到高精度拟合时得到的顶点中 距离小于低精度拟合得到的四个顶点maxL的顶点，排除部分顶点的干扰
        for (Point p : approx.toArray()) {
            if (!(getSpacePointToPoint(p, largest_square.toList().get(0)) > maxL &&
                    getSpacePointToPoint(p, largest_square.toList().get(1)) > maxL &&
                    getSpacePointToPoint(p, largest_square.toList().get(2)) > maxL &&
                    getSpacePointToPoint(p, largest_square.toList().get(3)) > maxL)) {
                newPointList.add(p);
            }
        }


        // 找到剩余顶点连线中，边长大于 2 * maxL的四条边作为四边形物体的四条边
        List<double[]> lines = new ArrayList<double[]>();
        for (int i = 0; i < newPointList.size(); i++) {
            Point p1 = newPointList.get(i);
            Point p2 = newPointList.get((i + 1) % newPointList.size());
            if (getSpacePointToPoint(p1, p2) > 2 * maxL) {
                lines.add(new double[]{p1.x, p1.y, p2.x, p2.y});
            }
        }

        // 计算出这四条边中 相邻两条边的交点，即物体的四个顶点
        List<Point> corners = new ArrayList<Point>();
        for (int i = 0; i < lines.size(); i++) {
            Point corner = computeIntersect(lines.get(i), lines.get((i + 1) % lines.size()));
            corners.add(corner);
        }
        //8、透视变换，提取四边形=====================
        // 对顶点顺时针排序
        sortCorners(corners);

        // 计算目标图像的尺寸
        Point p0 = corners.get(0);
        Point p1 = corners.get(1);
        Point p2 = corners.get(2);
        Point p3 = corners.get(3);
        double space0 = getSpacePointToPoint(p0, p1);
        double space1 = getSpacePointToPoint(p1, p2);
        double space2 = getSpacePointToPoint(p2, p3);
        double space3 = getSpacePointToPoint(p3, p0);

        double imgWidth = Math.max(space1, space3);
        double imgHeight = Math.max(space0, space2);

        // 如果提取出的图片宽小于高，则旋转90度
        if (imgWidth < imgHeight) {
            double temp = imgWidth;
            imgWidth = imgHeight;
            imgHeight = temp;
            Point tempPoint = p0.clone();
            p0 = p1.clone();
            p1 = p2.clone();
            p2 = p3.clone();
            p3 = tempPoint.clone();
        }



        Mat quad = Mat.zeros((int) imgHeight * 2, (int) imgWidth * 2, CvType.CV_8UC3);

        MatOfPoint2f cornerMat = new MatOfPoint2f(p0, p1, p2, p3);
        MatOfPoint2f quadMat = new MatOfPoint2f(new Point(imgWidth * 0.4, imgHeight * 1.6),
                new Point(imgWidth * 0.4, imgHeight * 0.4),
                new Point(imgWidth * 1.6, imgHeight * 0.4),
                new Point(imgWidth * 1.6, imgHeight * 1.6));

        // 提取图像
        Mat transmtx = Imgproc.getPerspectiveTransform(cornerMat, quadMat);
        Imgproc.warpPerspective(srcMat, quad, transmtx, quad.size());

        return quad;
    }


}
