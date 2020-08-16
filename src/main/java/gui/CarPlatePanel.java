package gui;

import gui.plate.car.CarSobelPlateLocation;
import gui.utils.ImageUtils;
import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class CarPlatePanel extends JPanel {
    final String originalImgPath = ".\\src\\main\\resources\\test_img\\test8.jpg";

    JButton btn1 = new JButton("1、读取图片");
    JButton btn2 = new JButton("2、高斯模糊");
    JButton btn3 = new JButton("3、灰度化");
    JButton btn4 = new JButton("4、边缘化");
    JButton btn5 = new JButton("5、二值化");
    JButton btn6 = new JButton("6、闭操作");
    JButton btn7 = new JButton("7、最大面积、最小面积.宽高比。");

    Mat mat1;
    Mat mat2;
    Mat mat3;
    Mat mat4;
    Mat mat5;
    Mat mat6;
    Mat mat7;

    public CarPlatePanel() {
        init();
    }

    void init() {
        //默认为1行，n列；2行3列，水平间距10，垂直间距5
        this.setLayout(new GridLayout(3, 4, 5, 5));
        this.add(btn1);
        this.add(btn2);
        this.add(btn3);
        this.add(btn4);
        this.add(btn5);
        this.add(btn6);
        this.add(btn7);

        initAction();
    }

    private void initAction() {

        btn1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                mat1 = ImageUtils.readImage(originalImgPath);
                Image loadedImage = ImageUtils.toBufferedImage(mat1);
                btn1.setIcon(new ImageIcon(loadedImage));
            }
        });
        btn2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                mat2 = CarSobelPlateLocation.blurImage(mat1, 5);
                Image loadedImage = ImageUtils.toBufferedImage(mat2);
                btn2.setIcon(new ImageIcon(loadedImage));
            }
        });
        btn3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                mat3 = CarSobelPlateLocation.greyImage(mat2);
                Image loadedImage = ImageUtils.toBufferedImage(mat3);
                btn3.setIcon(new ImageIcon(loadedImage));
            }
        });
        btn4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                mat4 = CarSobelPlateLocation.sobelImage(mat3);
                Image loadedImage = ImageUtils.toBufferedImage(mat4);
                btn4.setIcon(new ImageIcon(loadedImage));
            }
        });
        btn5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                mat5 = CarSobelPlateLocation.thresholdsImage(mat4);
                Image loadedImage = ImageUtils.toBufferedImage(mat5);
                btn5.setIcon(new ImageIcon(loadedImage));
            }
        });
        btn6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                mat6 = CarSobelPlateLocation.closeImage(mat5,17,3);
                Image loadedImage = ImageUtils.toBufferedImage(mat6);
                btn6.setIcon(new ImageIcon(loadedImage));
            }
        });
        btn7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                List<Mat> plates = new ArrayList<Mat>();

                CarSobelPlateLocation.plateLocate(mat1,plates);
                for (Mat plate : plates) {
                    Image loadedImage = ImageUtils.toBufferedImage(plate);
                    btn7.setIcon(new ImageIcon(loadedImage));
                }

//                Mat src_threshold = CarSobelPlateLocationUtils.processMat(
//                        mat1, 5, 17, 3);
//                Image loadedImage = ImageUtils.toBufferedImage(src_threshold);
//                btn7.setIcon(new ImageIcon(loadedImage));

            }
        });
    }
}
