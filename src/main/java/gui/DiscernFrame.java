package gui;

import org.opencv.core.Core;

import javax.swing.*;
import java.awt.*;

public class DiscernFrame extends JFrame {
    static {
        // 加载本地 JNI 库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //注意程序运行的时候需要在VM option添加该行 指明opencv的dll文件所在路径
        //-Djava.library.path=$PROJECT_DIR$\opencv\x64
    }

    ImagePanel imagePanel = new ImagePanel();
    CarPlateImagePanel carPlateImagePanel = new CarPlateImagePanel();
    FaPiaoPlatePanel faPiaoPlatePanel = new FaPiaoPlatePanel();

    DiscernFrame() {
        init();
        this.setTitle("车牌号识别过程");
        this.setResizable(true);
        this.setSize(1300, 800);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    void init() {
        //默认为1行，n列；2行3列，水平间距10，垂直间距5
        this.setLayout(new BorderLayout());
        this.add(faPiaoPlatePanel, BorderLayout.CENTER);
    }

    public static void main(String args[]) {
        new DiscernFrame();
    }
}