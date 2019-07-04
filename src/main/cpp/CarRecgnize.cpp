#include <iostream>
#include "CarPlateRecgnize.h"

int main() {
    CarPlateRecgnize p(
            "D:\\lunwen\\OpencvCarRecgnize\\resource\\HOG_ANN_DATA2.xml",
            "D:\\lunwen\\OpencvCarRecgnize\\resource\\HOG_ANN_DATA2.xml",
            "D:\\lunwen\\OpencvCarRecgnize\\resource\\HOG_ANN_ZH_DATA2.xml");
    Mat src = imread("D:\\lunwen\\OpencvCarRecgnize\\test_img\\test11.jpg");
    p.plateRecgnize(src);
    cout << p.plateRecgnize(src) << endl;
    waitKey();
    return 0;
}