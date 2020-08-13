package gui;

import gui.utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ImagePanel extends JPanel {
    final String originalImgPath = ".\\src\\main\\resources\\test_img\\test8.jpg";

    JButton btn1 = new JButton("1、读取图片");
    JButton btn2 = new JButton("2、图片灰度化");
    JButton btn3 = new JButton("3、使用Canndy检测边缘");
    JButton btn4 = new JButton("4、形态学（膨胀腐蚀）处理");
    JButton btn5 = new JButton("5、轮廓处理");
    JButton btn6 = new JButton("6、自适应二值化处理");

    public ImagePanel() {
        init();
        this.setSize(900, 500);
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
                Image loadedImage = ImageUtils.toBufferedImage(ImageUtils.readImage(originalImgPath));
            }
        });
        btn2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
            }
        });
        btn3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
            }
        });
        btn4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
            }
        });
        btn5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
            }
        });
        btn6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
            }
        });
    }
}
