package gui.plate.car;

import org.opencv.core.Mat;

import java.util.List;

/**
 * 车牌识别接口定义
 */
public interface CarPlateLocation {
    void plateLocate(Mat src, List<Mat> plates);
}
