import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Main {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //注意程序运行的时候需要在VM option添加该行 指明opencv的dll文件所在路径
        //-Djava.library.path=$PROJECT_DIR$\opencv\x64
    }

    public static void main(String[] args) {
        System.out.println("Hello word!");
        String originalImgPath = "D:\\lunwen\\CardCode\\src\\main\\resources\\test_img\\test1.jpg";

        Mat destMat = new Mat();

        // 1. 读取图片
        Mat srcMat = Imgcodecs.imread(originalImgPath);
        System.out.println(srcMat);
        if (srcMat.dataAddr() == 0) {
            System.out.println("打开文件出错");
        }

        // 2.图片灰度化,就是把图片转换为黑白照
        Imgproc.cvtColor(srcMat, destMat, Imgproc.COLOR_RGB2GRAY);

        // 3.二值化处理,也就是只留两个值,黑白
        Mat binaryMat = new Mat(srcMat.height(), srcMat.width(), CvType.CV_8UC1);
        Imgproc.threshold(destMat, binaryMat, 100, 255, Imgproc.THRESH_BINARY);

        // 4.图像腐蚀,这里使用3*3的图片去腐蚀
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Imgproc.erode(binaryMat, destMat, element);

//        // 5.遍历,获取和修改像素值,二值图片,当成一个二维矩阵就可以了,双重循环,使用get方法获取像素点,使用put方法修改像素点
//        for (int y = 0; y < destMat.height(); y++) {
//            for (int x = 0; x < destMat.width(); x++) {
//                //得到该行像素点的值
//                double[] data = destMat.get(y, x);
//                for (int i1 = 0; i1 < data.length; i1++) {
//                    data[i1] = 0;//像素点都改为白色
//                }
//                destMat.put(y, x, data);
//            }
//        }

        // 展示处理结果
        ImageViewer imageViewer = new ImageViewer(destMat, "图片预览");
        imageViewer.imshow();

    }
}
