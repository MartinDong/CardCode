package gui;

import gui.plate.car.CarSobelPlateLocation;
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

public class ImagePanel extends JPanel {
    final String originalImgPath =
            ".\\src\\main\\resources\\car\\test7.jpg";

    JButton btn1 = new JButton("1、读取图片");
    JButton btn2 = new JButton("2、图片灰度化");
    JButton btn3 = new JButton("3、高斯模糊");
    JButton btn4 = new JButton("4、使用Canndy检测边缘");
    JButton btn5 = new JButton("5、膨胀，连接边缘");
    JButton btn6 = new JButton("6、轮廓处理");
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

    public ImagePanel() {
        init();
    }

    void init() {
        initButtonStyle();
        //默认为1行，n列；2行3列，水平间距10，垂直间距5
        this.setLayout(new GridLayout(3, 3, 5, 5));
        this.add(btn1);
        this.add(btn2);
        this.add(btn3);
        this.add(btn4);
        this.add(btn5);
        this.add(btn6);
//        this.add(btn7);
//        this.add(btn8);
        initAction();
    }

    private void initButtonStyle() {
        //根据bai指定字体名称du、样式和磅值大小，创建zhi一个新 Font。
        Font f = new Font("宋体", Font.BOLD, 25);

        btn1.setFont(f);
        btn2.setFont(f);
        btn3.setFont(f);
        btn4.setFont(f);
        btn5.setFont(f);
        btn6.setFont(f);
        btn7.setFont(f);
        btn8.setFont(f);

        btn1.setForeground(Color.red);
        btn2.setForeground(Color.red);
        btn3.setForeground(Color.red);
        btn4.setForeground(Color.red);
        btn5.setForeground(Color.red);
        btn6.setForeground(Color.red);
        btn7.setForeground(Color.red);
        btn8.setForeground(Color.red);

        btn1.setHorizontalTextPosition(SwingConstants.CENTER);
        btn2.setHorizontalTextPosition(SwingConstants.CENTER);
        btn3.setHorizontalTextPosition(SwingConstants.CENTER);
        btn4.setHorizontalTextPosition(SwingConstants.CENTER);
        btn5.setHorizontalTextPosition(SwingConstants.CENTER);
        btn6.setHorizontalTextPosition(SwingConstants.CENTER);
        btn7.setHorizontalTextPosition(SwingConstants.CENTER);
        btn8.setHorizontalTextPosition(SwingConstants.CENTER);

        btn1.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn2.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn3.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn4.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn5.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn6.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn7.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn8.setVerticalTextPosition(SwingConstants.BOTTOM);
    }


    private void initAction() {

        btn1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                mat1 = ImageUtils.readImage(originalImgPath);

                Image loadedImage = ImageUtils.toBufferedImage(mat1);

                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
                        btn1.getWidth(), btn1.getHeight() - 40);

                ImageIcon imageIcon = new ImageIcon(loadedImage2);
                btn1.setIcon(imageIcon);
            }
        });
        btn2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                mat2 = ImageUtils.grayImage(mat1);

                Image loadedImage = ImageUtils.toBufferedImage(mat2);

                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
                        btn2.getWidth(), btn2.getHeight() - 40);

                ImageIcon imageIcon = new ImageIcon(loadedImage2);
                btn2.setIcon(imageIcon);
            }
        });
        btn3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());

                mat3 = CarSobelPlateLocation.blurImage(mat2, 3);

                Image loadedImage = ImageUtils.toBufferedImage(mat3);

                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
                        btn3.getWidth(), btn3.getHeight() - 40);

                ImageIcon imageIcon = new ImageIcon(loadedImage2);
                btn3.setIcon(imageIcon);
            }
        });
        btn4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                mat4 = ImageUtils.cannyImage(mat2);

                Image loadedImage = ImageUtils.toBufferedImage(mat4);

                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
                        btn4.getWidth(), btn4.getHeight() - 40);

                ImageIcon imageIcon = new ImageIcon(loadedImage2);
                btn4.setIcon(imageIcon);
            }
        });
        btn5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                mat5 = ImageUtils.blurryImage(mat4);

                Image loadedImage = ImageUtils.toBufferedImage(mat5);

                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
                        btn5.getWidth(), btn5.getHeight() - 40);

                ImageIcon imageIcon = new ImageIcon(loadedImage2);
                btn5.setIcon(imageIcon);
            }
        });
        btn6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                mat6 = mat1.clone();

                ImageUtils.roiGrayImage(mat6, mat5);

                Image loadedImage = ImageUtils.toBufferedImage(mat6);

                Image loadedImage2 = ImageUtils.resize((BufferedImage) loadedImage,
                        btn6.getWidth(), btn6.getHeight() - 40);

                ImageIcon imageIcon = new ImageIcon(loadedImage2);
                btn6.setIcon(imageIcon);
            }
        });
    }
}
