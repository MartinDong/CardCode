import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class Main {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //注意程序运行的时候需要在VM option添加该行 指明opencv的dll文件所在路径
        //-Djava.library.path=$PROJECT_DIR$\opencv\x64
    }

    public static void main(String[] args) {
        System.out.println("Hello word!");
        String originalImgPath = "D:\\lunwen\\CardCode\\src\\main\\resources\\test_img\\test1.jpg";

        Mat mat = Imgcodecs.imread(originalImgPath);
        System.out.println(mat);
        if (mat.dataAddr() == 0) {
            System.out.println("打开文件出错");
        }

        ImageViewer imageViewer = new ImageViewer(mat, "第一幅图片");
        imageViewer.imshow();
//        ShowImage window = new ShowImage(mat);
//        window.getFrame().setVisible(true);
    }
}
