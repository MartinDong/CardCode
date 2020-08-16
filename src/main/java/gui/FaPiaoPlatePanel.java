package gui;

import gui.plate.invoice.InvoiceIdentityStepUtils;
import gui.utils.ImageUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

public class FaPiaoPlatePanel extends JPanel {
    final String originalImgPath = ".\\src\\main\\resources\\test_img\\fapiao2.png";
//    final String originalImgPath = ".\\src\\main\\resources\\test_img\\01100190061195583380.jpg";

    JButton btn1 = new JButton("1、读取图片文件");
    JButton btn2 = new JButton("2、图片灰度化,就是把图片转换为黑白照");
    JButton btn3 = new JButton("3、高斯降噪");
    JButton btn4 = new JButton("4、使用Canndy检测边缘");
    JButton btn5 = new JButton("5、膨胀，连接边缘");
    JButton btn6 = new JButton("6、寻找轮廓");
    JButton btn7 = new JButton("7、将凸包转为矩形");
    JButton btn8 = new JButton("8、提取图像");


    Mat mat1;
    Mat mat2;
    Mat mat3;
    Mat mat4;
    Mat mat5;
    Mat mat6;
    Mat mat7;
    Mat mat8;

    List<MatOfPoint> contours;
    Point[] rotatedRectPoint;

    public FaPiaoPlatePanel() {
        init();
    }

    void init() {
        btn1.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn2.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn3.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn4.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn5.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn6.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn7.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn8.setVerticalTextPosition(SwingConstants.BOTTOM);

        //默认为1行，n列；2行3列，水平间距10，垂直间距5
        this.setLayout(new GridLayout(3, 4, 5, 5));
        this.add(btn1);
        this.add(btn2);
        this.add(btn3);
        this.add(btn4);
        this.add(btn5);
        this.add(btn6);
        this.add(btn7);
        this.add(btn8);

        initAction();
    }

    private void initAction() {

        btn1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());

                mat1 = InvoiceIdentityStepUtils.readImage(originalImgPath);

                Image loadedImage = ImageUtils.toBufferedImage(mat1);

                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
                        btn1.getWidth(), btn1.getHeight());

                ImageIcon imageIcon = new ImageIcon(loadedImage2);
                btn1.setIcon(imageIcon);
            }
        });
        btn2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());

                mat2 = InvoiceIdentityStepUtils.grayImage(mat1);

                Image loadedImage = ImageUtils.toBufferedImage(mat2);

                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
                        btn2.getWidth(), btn2.getHeight());

                ImageIcon imageIcon = new ImageIcon(loadedImage2);
                btn2.setIcon(imageIcon);
            }
        });
        btn3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());

                mat3 = InvoiceIdentityStepUtils.blurryImage(mat2);

                Image loadedImage = ImageUtils.toBufferedImage(mat3);

                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
                        btn3.getWidth(), btn3.getHeight());

                ImageIcon imageIcon = new ImageIcon(loadedImage2);
                btn3.setIcon(imageIcon);
            }
        });
        btn4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());

                mat4 = InvoiceIdentityStepUtils.cannyImage(mat3);

                Image loadedImage = ImageUtils.toBufferedImage(mat4);

                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
                        btn4.getWidth(), btn4.getHeight());

                ImageIcon imageIcon = new ImageIcon(loadedImage2);
                btn4.setIcon(imageIcon);
            }
        });
        btn5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());

                mat5 = InvoiceIdentityStepUtils.dilateImage(mat4);

                Image loadedImage = ImageUtils.toBufferedImage(mat5);

                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
                        btn5.getWidth(), btn5.getHeight());

                ImageIcon imageIcon = new ImageIcon(loadedImage2);
                btn5.setIcon(imageIcon);
            }
        });
        btn6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());

                mat6 = mat1.clone();

                contours = InvoiceIdentityStepUtils.findContours(mat6, mat5);

                Image loadedImage = ImageUtils.toBufferedImage(mat6);

                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
                        btn6.getWidth(), btn6.getHeight());

                ImageIcon imageIcon = new ImageIcon(loadedImage2);
                btn6.setIcon(imageIcon);
            }
        });
        btn7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());

                mat7 = mat1.clone();

                rotatedRectPoint = InvoiceIdentityStepUtils.findMaxContours(mat7, contours);

                Image loadedImage = ImageUtils.toBufferedImage(mat7);

                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
                        btn7.getWidth(), btn7.getHeight());

                ImageIcon imageIcon = new ImageIcon(loadedImage2);
                btn7.setIcon(imageIcon);
            }
        });
        btn8.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());

                mat8 = InvoiceIdentityStepUtils.findQuadContours(mat1, rotatedRectPoint);

                Image loadedImage = ImageUtils.toBufferedImage(mat8);

                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
                        btn8.getWidth(), btn8.getHeight());

                ImageIcon imageIcon = new ImageIcon(loadedImage2);
                btn8.setIcon(imageIcon);
            }
        });
    }
}
