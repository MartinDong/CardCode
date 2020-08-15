package gui.utils;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_16S;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.resize;

/**
 * 车牌定位工具类
 * Sobel定位
 * <p>
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
    public static Mat blurImage(Mat srcMat, int blur_size) {
        //预处理 ：去噪 让车牌区域更加突出
        Mat blur = new Mat();
        //1、高斯模糊（平滑） （1、为了后续操作 2、降噪 ）
        Imgproc.GaussianBlur(srcMat, blur, new Size(blur_size, blur_size), 0);
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
     * 加权
     */
    public static Mat addWeighted(Mat srcMat) {
        //4. 二值化 黑白
        Mat weighted = new Mat();
        //大律法   最大类间算法
        Core.addWeighted(srcMat, 1, weighted, 0, 0, weighted);
        //imshow("二值", shold);
        return weighted;
    }

    /**
     * 4、 二值化
     */
    public static Mat thresholdsImage(Mat srcMat) {
        //4. 二值化 黑白
        Mat thresholds = new Mat();
        //大律法   最大类间算法
        Imgproc.threshold(srcMat, thresholds, 0, 255,
                Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
        //imshow("二值", shold);
        return thresholds;
    }

    /**
     * 5、 闭操作  先膨胀、后腐蚀
     * 把白色区域连接起来，或者扩大。任何黑色区域如果小于结构元素的大小都会被消除
     * 对于结构大小 由于中国车牌比如 湘A 12345 有断层 所以 width过小不行,而过大会连接不必要的区域
     * sobel的方式定位不能100%匹配
     */
    public static Mat closeImage(Mat srcMat, int close_w, int close_h) {
        //5、闭操作
        // 将相邻的白色区域扩大 连接成一个整体
        Mat close = new Mat();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(close_w, close_h));
        Imgproc.morphologyEx(srcMat, close, Imgproc.MORPH_CLOSE, element);
        //imshow("闭操作", close);
        return close;
    }

    /**
     * 6、查找轮廓
     */
    public static List<RotatedRect> findOutlineImage(Mat srcMat) {
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
        //满足初步筛选条件的轮廓
        //遍历
        List<RotatedRect> vec_sobel_roi = new ArrayList<RotatedRect>();
        for (MatOfPoint point : contours) {
            MatOfPoint2f point2f = new MatOfPoint2f(point.toArray());
            RotatedRect rotatedRect = Imgproc.minAreaRect(point2f);
            //绘制边框将符合的轮廓标注出来
            //Imgproc.rectangle(srcMat, rotatedRect.boundingRect(), new Scalar(255, 0, 0));
            //进行初步的筛选 把完全不符合的轮廓给排除掉 ( 比如：1x1，5x1000 )
            if (verifySizes(rotatedRect)) {
                vec_sobel_roi.add(rotatedRect);
            }
        }
        return vec_sobel_roi;
    }

    /**
     * 7、初步赛选：宽高比 float aspec，把不符合的删除掉（1 * 1的， 5* 1000的等候选矩形）
     */
    public static boolean verifySizes(RotatedRect rotated_rect) {
        //容错率
        float error = 0.75f;
        //中国车牌标准440mm*140mm
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
        //可能是竖的车牌 宽比高小就用 高宽比
        float r = (float) rotated_rect.size.width / (float) rotated_rect.size.height;
        if (r < 1) r = (float) rotated_rect.size.height / (float) rotated_rect.size.width;
        if ((area < min || area > max) || (r < rmin || r > rmax)) {
            return false;
        }
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
        Imgproc.warpAffine(src, mat_rotated, rot_mat, new Size(src.cols(), src.rows()), 2);
        //截取
        Imgproc.getRectSubPix(mat_rotated, new Size(rect_size.width, rect_size.height),
                center, dst);
        mat_rotated.release();
        rot_mat.release();
        return rot_mat;
    }

    public static void plateLocate(Mat src, List<Mat> plates) {
        Mat src_threshold = processMat(src, 5, 17, 3);
        //imshow("processMat", src_threshold);

        //获得初步筛选车牌轮廓================================================================
        //轮廓检测
        List<RotatedRect> vec_sobel_roi = findOutlineImage(src_threshold);
//        src_threshold.release();

        tortuosity(src, vec_sobel_roi, plates);
    }

    public static Mat processMat(Mat src, int blur_size, int close_w, int close_h) {
        //图像预处理 ———— 降噪================================================================
        //高斯滤波 也就是高斯模糊 降噪
        Mat blur = blurImage(src, blur_size);
        //imshow("高斯滤波", blur);
        //灰度
        Mat gray = greyImage(blur);
        //imshow("灰度", gray);
        //边缘检测滤波器 边缘检测 便于区分车牌
        Mat abs_sobel = sobelImage(gray);
        //imshow("边缘检测", abs_sobel);
        //加权
        //Mat weighted = addWeighted(abs_sobel);
        //imshow("加权", weighted);
        //二值
        Mat thresholds = thresholdsImage(abs_sobel);
        //闭操作 先膨胀、后腐蚀
        Mat dst = closeImage(thresholds, 17, 3);

        // 释放资源
        blur.release();
        gray.release();
        abs_sobel.release();
//        weighted.release();
        thresholds.release();
        return dst;
    }


    /**
     * 车牌矫正
     *
     * @param src
     * @param rects
     * @param dst_plates
     */
    public static void tortuosity(Mat src, List<RotatedRect> rects, List<Mat> dst_plates) {
        //循环要处理的矩形
        for (RotatedRect roi_rect : rects) {
            //矩形角度
            double roi_angle = roi_rect.angle;
            float r = (float) roi_rect.size.width / (float) roi_rect.size.height;
            //矩形大小
            Size roi_rect_size = roi_rect.size;
            //交换宽高
            if (r < 1) {
                roi_angle = 90 + roi_angle;
                MathUtils.swap(roi_rect_size.width, roi_rect_size.height);
            }
            //让rect在一个安全的范围(不能超过src)
            Rect safa_rect = safeRect(src, roi_rect);
            //候选车牌
            //抠图  这里不是产生一张新图片 而是在src身上定位到一个Mat 让我们处理
            //数据和src是同一份
            Mat src_rect = src.submat(safa_rect);

            Mat dst;
            //不需要旋转的 旋转角度小没必要旋转了
            if (roi_angle - 5 < 0 && roi_angle + 5 > 0) {
                dst = src_rect.clone();
            } else {
                //相对于roi的中心点 不减去左上角坐标是相对于整个图的
                //减去左上角则是相对于候选车牌的中心点 坐标
                //相对于roi的中心点 不减去左上角坐标是相对于整个图的
                //减去左上角则是相对于候选车牌的中心点 坐标
                Point roi_ref_center = new Point(roi_rect.center.x - safa_rect.tl().x, roi_rect.center.y - safa_rect.tl().y);
//                Point roi_ref_center = roi_rect.center - rect.tl();
                Mat rotated_mat = new Mat();
                //矫正 rotated_mat: 矫正后的图片
                rotation(src_rect, rotated_mat, roi_rect_size, roi_ref_center, roi_angle);
                dst = rotated_mat;
            }
            //定义大小
            Mat plate_mat = new Mat();
            //高+宽
            plate_mat.create(32, 136, CV_8UC3);
            resize(dst, plate_mat, plate_mat.size());
            dst_plates.add(plate_mat);
            dst.release();
        }
    }

    /**
     * 转换安全矩形，防止矩形框超出图像边界
     */
    public static Rect safeRect(Mat src, RotatedRect rect) {
        //RotatedRect 没有坐标
        //转为正常的带坐标的边框
        Rect boudRect = rect.boundingRect();
        //左上角 x,y
        int tl_x = Math.max(boudRect.x, 0);
        int tl_y = Math.max(boudRect.y, 0);
        //这里是拿 坐标 x，y 从0开始的 所以-1
        //比如宽长度是10，x坐标最大是9， 所以src.clos-1
        //右下角
        float br_x = boudRect.x + boudRect.width < src.cols()
                ? boudRect.x + boudRect.width - 1
                : src.cols() - 1;
        float br_y = boudRect.y + boudRect.height < src.rows()
                ? boudRect.y + boudRect.height - 1
                : src.rows() - 1;
        int w = (int) (br_x - tl_x);
        int h = (int) (br_y - tl_y);
        if (w <= 0 || h <= 0) return new Rect(0, 0, 0, 0);
        return new Rect(tl_x, tl_y, w, h);
    }

}
