package gui.utils;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_16S;
import static org.opencv.highgui.HighGui.imshow;

/**
 * 车牌定位工具类
 * Sobel定位
 *
 * 高斯模糊
 * 灰度化
 * 边缘化
 * 二值化
 * 闭操作
 */
public class CarSobelPlateLocationUtils {
    /**
     * 0、读取图片文件
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
     * 1、高斯模糊
     */
    public static Mat blurImage(Mat srcMat) {
        //预处理 ：去噪 让车牌区域更加突出
        Mat blur = new Mat();
        //1、高斯模糊（平滑） （1、为了后续操作 2、降噪 ）
        Imgproc.GaussianBlur(srcMat, blur, new Size(5, 5), 3);
        //imshow("高斯模糊",blur);
        return blur;
    }

    /**
     * 2、灰度化
     */
    public static Mat greyImage(Mat srcMat) {
        Mat gray = new Mat();
        //2、灰度化 去掉颜色 因为它对于我们这里没用  降噪
        Imgproc.cvtColor(srcMat, gray, Imgproc.COLOR_BGR2GRAY);
        //imshow("灰度", gray);
        return gray;
    }

    /**
     * 3、 边缘化
     */
    public static Mat sobelImage(Mat srcMat) {
        Mat sobel_16 = new Mat();
        //3、 边缘检测 让车牌更加突出  在调用时需要以16位来保存数据 在后续操作 以及显示的时候需要转回8位
        Imgproc.Sobel(srcMat, sobel_16, CV_16S, 1, 0);
        //转为8位
        Mat sobel = new Mat();
        Core.convertScaleAbs(sobel_16, sobel);
        //imshow("Sobel", sobel);
        return sobel;
    }

    /**
     * 4、 二值化
     */
    public static Mat sholdImage(Mat srcMat) {
        //4. 二值化 黑白
        Mat shold = new Mat();
        //大律法   最大类间算法
        Imgproc.threshold(srcMat, shold, 0, 255,
                Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
        //imshow("二值", shold);
        return shold;
    }

    /**
     * 5、 闭操作
     */
    public static Mat closeImage(Mat srcMat) {
        //5、闭操作
        // 将相邻的白色区域扩大 连接成一个整体
        Mat close = new Mat();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(17, 3));
        Imgproc.morphologyEx(srcMat, close, Imgproc.MORPH_CLOSE, element);
        //imshow("闭操作", close);
        return close;
    }

    /**
     * 6、查找轮廓
     */
    public static Mat findOutlineImage(Mat srcMat) {
        //获得初步筛选车牌轮廓
        //轮廓检测
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchyImage = new Mat();
        //查找轮廓 提取最外层的轮廓  将结果变成点序列放入 集合
        Imgproc.findContours(srcMat, contours, hierarchyImage, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        //遍历
        List<RotatedRect> vec_sobel_roi = new ArrayList<RotatedRect>();
        for (MatOfPoint point : contours) {
            MatOfPoint2f point2f = new MatOfPoint2f(point.toArray());
            RotatedRect rotatedRect = Imgproc.minAreaRect(point2f);
            Imgproc.rectangle(srcMat, rotatedRect.boundingRect(), new Scalar(255, 0, 0));
            //进行初步的筛选 把完全不符合的轮廓给排除掉 ( 比如：1x1，5x1000 )
            if (verifySizes(rotatedRect)) {
                vec_sobel_roi.add(rotatedRect);
            }
        }
        return srcMat;
    }

    /**
     * 7、初步赛选：宽高比 float aspec，把不符合的删除掉（1 * 1的， 5* 1000的等候选矩形）
     */
    public static boolean verifySizes(RotatedRect rotated_rect) {
        //容错率
        float error = 0.75f;
        //训练时候模型的宽高 136 * 32
        //获得宽高比
        float aspect = 136F / 32f;
        //最小 最大面积 不符合的丢弃
        //给个大概就行 随时调整
        //尽量给大一些没关系， 这还是初步筛选。
        int min = (int) (20 * aspect * 20);
        int max = (int) (180 * aspect * 180);

        //比例浮动 error认为也满足
        //最小宽、高比
        float rmin = aspect - aspect * error;
        //最大的宽高比
        float rmax = aspect + aspect * error;
        //矩形的面积
        float area = (float) (rotated_rect.size.height * rotated_rect.size.width);
        //矩形的比例
        float r = (float) rotated_rect.size.width / (float) rotated_rect.size.height;
        if ((area < min || area > max) || (r < rmin || r > rmax))
            return false;
        return true;
    }

    /**
     * 8、把斜的图片转正：仿射变换
     */
    public static Mat rotation(Mat src, Mat dst, Size rect_size,
                               Point center, double angle) {
        //获得旋转矩阵
        Mat rot_mat = Imgproc.getRotationMatrix2D(center, angle, 1);

        //运用仿射变换
        Mat mat_rotated = new Mat();
        //矫正后 大小会不一样，但是对角线肯定能容纳
        double max = Math.sqrt(Math.pow(src.rows(), 2) + Math.pow(src.cols(), 2));
        //仿射变换
        Imgproc.warpAffine(src, mat_rotated, rot_mat, new Size(max, max), 1);
        imshow("旋转前", src);
        imshow("旋转", mat_rotated);
        //截取 尽量把车牌多余的区域截取掉
        Imgproc.getRectSubPix(mat_rotated, new Size(rect_size.width, rect_size.height), center, dst);
        imshow("截取", dst);
        mat_rotated.release();
        rot_mat.release();
        return rot_mat;
    }
}
