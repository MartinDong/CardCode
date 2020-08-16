package gui;

import gui.plate.invoice.FaPiaoPlateLocation;
import gui.plate.invoice.InvoiceIdentify;
import gui.utils.ImageUtils;
import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

public class FaPiaoPlatePanel extends JPanel {
    final String originalImgPath = ".\\src\\main\\resources\\test_img\\fapiao2.png";

    JButton btn1 = new JButton("1、读取图片");
    JButton btn2 = new JButton("2、图片灰度化");
    JButton btn3 = new JButton("3、使用Canndy检测边缘");
    JButton btn4 = new JButton("4、形态学（膨胀腐蚀）处理");
    JButton btn5 = new JButton("5、轮廓处理");
    JButton btn6 = new JButton("6、自适应二值化处理");

    Mat mat1;
    Mat mat2;
    Mat mat3;
    Mat mat4;
    Mat mat5;
    Mat mat6;

    public FaPiaoPlatePanel() {
        init();
    }

    void init() {
        //默认为1行，n列；2行3列，水平间距10，垂直间距5
        this.setLayout(new GridLayout(2, 3, 10, 5));
        this.add(btn1);
        this.add(btn2);
        this.add(btn3);
        this.add(btn4);
        this.add(btn5);
        this.add(btn6);

        initAction();
    }

    private void initAction() {

        btn1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                mat1 = ImageUtils.readImage(originalImgPath);
                Image loadedImage = ImageUtils.toBufferedImage(mat1);
                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
                        btn1.getWidth(), btn1.getHeight());
                ImageIcon imageIcon = new ImageIcon(loadedImage2);
                btn1.setIcon(imageIcon);
            }
        });
        btn2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                System.out.println(e.getActionCommand());
//                mat2 = FaPiaoPlateLocation.plateLocate(mat1);
//                Image loadedImage = ImageUtils.toBufferedImage(mat2);
//                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
//                        btn2.getWidth(), btn2.getHeight());
//                ImageIcon imageIcon = new ImageIcon(loadedImage2);
//                btn2.setIcon(imageIcon);

                long c = System.currentTimeMillis();
                InvoiceIdentify identify = new InvoiceIdentify(new File(".\\src\\main\\resources\\test_img\\01100190061195583380.jpg"));
                identify.debug(".\\src\\main\\resources\\debug");
                Mat resultMat = identify.identify();
//                System.out.println(resultMat);
                System.out.println("识别总时间:" + (System.currentTimeMillis() - c));

                Image loadedImage = ImageUtils.toBufferedImage(resultMat);
                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
                        btn2.getWidth(), btn2.getHeight());
                ImageIcon imageIcon = new ImageIcon(loadedImage2);
                btn2.setIcon(imageIcon);

            }
        });
        btn3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                mat3 = ImageUtils.cannyImage(mat2);
                Image loadedImage = ImageUtils.toBufferedImage(mat3);
                btn3.setIcon(new ImageIcon(loadedImage));
            }
        });
        btn4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                mat4 = ImageUtils.blurryImage(mat3);
                Image loadedImage = ImageUtils.toBufferedImage(mat4);
                btn4.setIcon(new ImageIcon(loadedImage));
            }
        });
        btn5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                mat5 = ImageUtils.roiGrayImage(mat4);
                Image loadedImage = ImageUtils.toBufferedImage(mat5);
                btn5.setIcon(new ImageIcon(loadedImage));
            }
        });
        btn6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                mat6 = ImageUtils.roiThreadHoldImage(mat5);
                Image loadedImage = ImageUtils.toBufferedImage(mat6);
                btn6.setIcon(new ImageIcon(loadedImage));
            }
        });
    }
}