package gui;

import javax.swing.*;
import java.awt.*;

public class DiscernFrame extends JFrame {
    ImagePanel imagePanel = new ImagePanel();

    DiscernFrame() {
        init();
        this.setTitle("车牌号识别过程");
        this.setResizable(true);
        this.setSize(900, 500);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    void init() {
        //默认为1行，n列；2行3列，水平间距10，垂直间距5
        this.setLayout(new BorderLayout());
        this.add(imagePanel, BorderLayout.CENTER);
    }

    public static void main(String args[]) {
        new DiscernFrame();
    }
}