/////////////////////////////////////////////////////////////////////////////////////
//打开视频文件以及车辆跟踪和识别
void CTrackandIDDlg::OnStartTrackandID() 
{
        // TODO: Add your control notification handler code here
    int argc=2;
        ////打开文件///////////////////////////////////////////////////
        CString FilePathName;
        CFileDialog dlg(TRUE);
        if(dlg.DoModal()==IDOK)
                FilePathName=dlg.GetPathName();


    IplImage* pFrame = NULL;
    IplImage* pFrImg = NULL;
    IplImage* pBkImg = NULL;
        IplImage* pFrImg1 = NULL;


    CvMat* pFrameMat = NULL;
    CvMat* pFrMat = NULL;
    CvMat* pBkMat = NULL;
        CvMat* pFrMat1 = NULL;


        CvMemStorage * storage = cvCreateMemStorage(0);//轮廓边缘提取时的参数
    CvSeq * contour = 0;//轮廓边缘提取时的参数
    int mode = CV_RETR_EXTERNAL;//轮廓边缘提取时的参数
        //形态学处理时内核的大小
        IplConvKernel* Element = cvCreateStructuringElementEx(13,13,1,1,CV_SHAPE_RECT,NULL);


        CvFont font1;//初始化字体格式
        int linetype=CV_AA;
        cvInitFont(&font1, CV_FONT_HERSHEY_SIMPLEX, 0.5, 0.5, 0, 1, 8);
        //用字符串时一定要把using namespace std;写在前面，否则不能用,下面是用于显示的字符串
        string msg[10]={"JGD01","JGD02","JGD03","JGD04","JGD05","JGD06","JGD07","JGD08","JGD09","JGD10"};
        int No=0;//用于记录显示车辆
        bool FindCar=false;
  
        //在视频中画出感兴趣的区域，怎么样才能沿车道画线？？？？？？？
        CvPoint pt1,pt2,pt3,pt4,pt5;
        pt1.x=292;//(视频中左下点)
        pt1.y=100;
        pt2.x=412;//(视频中右上点)
        pt2.y=280;
        CvRect bndRect=cvRect(0,0,0,0);//用cvBoundingRect画出外接矩形时需要的矩形
        int avgX = 0;//The midpoint X position of the rectangle surrounding the moving objects
        int avgY = 0;//The midpoint Y position of the rectangle surrounding the moving objects
        int avgX1=0;//用来合并相近的车辆
        int avgY1=0;             
        for(int i=0;i<10;i++)
        {
                TrackBlock[i]=NULL;
                if((TrackBlock[i]=(struct AvTrackBlock *) malloc(sizeof(struct AvTrackBlock)))==NULL)
                {
                        MessageBox("内存分配错误");
                        exit(1);
                }                         
        }////////////////////


    CvCapture* pCapture = NULL;  
    int nFrmNum = 0;//表示图像的帧数


    //创建窗口
    cvNamedWindow("video", 1);
         //cvNamedWindow("background",1);
    cvNamedWindow("foreground",1);
    //使窗口有序排列
    cvMoveWindow("video", 30, 0);
        //cvMoveWindow("background", 360, 0);
    cvMoveWindow("foreground", 690, 0);


    if( argc > 2 ){
        fprintf(stderr, "Usage: bkgrd [video_file_name]\n");
        //return -1;
    }


    ////打开摄像头///////////////////////////////////////////////////
    if (argc ==1)
        if( !(pCapture = cvCaptureFromCAM(-1))){
                fprintf(stderr, "Can not open camera.\n");
                //return -2;
                }


    ///打开视频文件//////////////////////////////////////////////////
    if(argc == 2)
        if( !(pCapture = cvCaptureFromFile(FilePathName))){
                        fprintf(stderr, "Can not open video file %s\n", FilePathName);
                        //return -2;
                }
  
                pFrame = cvQueryFrame(pCapture);


                int widthT,heightT;
                widthT = pFrame->width;
                heightT = pFrame->height;


        //        pt2.x = widthT-1;
        //        pt2.y = heightT -1;


IplImage* pFrameTemp = cvQueryFrame(pCapture);


pFrame = cvCreateImage(cvGetSize(pFrameTemp),8,3);
cvCopy(pFrameTemp,pFrame);


    //逐帧读取视频,cvQueryFrame从摄像头或者文件中抓取并返回一帧
    while(pFrameTemp = cvQueryFrame(pCapture))
    {
                cvFlip(pFrameTemp);
                cvCopy(pFrameTemp,pFrame);
        nFrmNum++; 
                
                


        //如果是第一帧，需要申请内存，并初始化
        if(nFrmNum == 1)
                {
                        pBkImg = cvCreateImage(cvSize(pFrame->width, pFrame->height),  IPL_DEPTH_8U,1);
                        pFrImg = cvCreateImage(cvSize(pFrame->width, pFrame->height),  IPL_DEPTH_8U,1);


                         pBkMat = cvCreateMat(pFrame->height, pFrame->width, CV_32FC1);
                         pFrMat = cvCreateMat(pFrame->height, pFrame->width, CV_32FC1);
                         pFrameMat = cvCreateMat(pFrame->height, pFrame->width, CV_32FC1);
                        cvCvtColor(pFrame, pBkImg, CV_BGR2GRAY);
                        cvCvtColor(pFrame, pFrImg, CV_BGR2GRAY);
                        cvConvert(pFrImg, pFrameMat);
                        cvConvert(pFrImg, pFrMat);
                        cvConvert(pFrImg, pBkMat);
                }


                else if(nFrmNum == 3)
                {
                        cvCvtColor(pFrame, pFrImg, CV_BGR2GRAY);
                        cvConvert(pFrImg, pFrameMat);
                        //高斯滤波先，以平滑图像
                        cvSmooth(pFrameMat, pFrameMat, CV_GAUSSIAN, 3, 0, 0);


                        //在视频中设置并画出感兴趣的区域
                        cvRectangle(pFrame,pt1,pt2,CV_RGB(255,0,0),2, 8, 0 );


                        //当前帧跟背景图相减,cvAbsDiff计算两个数组差的绝对值
                        cvAbsDiff(pFrameMat, pBkMat, pFrMat);


                        //二值化前景图
                        cvThreshold(pFrMat, pFrImg, 60, 255.0, CV_THRESH_BINARY);
                        
                        //通过查找边界找出ROI矩形区域内的运动车辆，建立完全目标档案
                        //cvCanny(pFrImg, pBkImg, 50, 150, 3);
                        cvDilate(pFrImg,pBkImg,Element,1);
                        cvFindContours(pBkImg, storage, &contour, sizeof(CvContour),
                                mode, CV_CHAIN_APPROX_SIMPLE);
                        //process each moving contour in the current frame用函数cvBoundingRect
                        for(;contour!=0;contour=contour->h_next)
                        {
                                //Get a bounding rectangle around the moving object.
                                bndRect = cvBoundingRect(contour, 0);
                                
                                //Get an average X position of the moving contour.
                                avgX = (bndRect.x + bndRect.x + bndRect.width) / 2; 
                                avgY = (bndRect.y + bndRect.y + bndRect.height) / 2;
                                pt5.x = bndRect.x;//写字的左下角点
                                pt5.y = avgY;


                                //If the center of contour is within ROI than show it
                                if(avgX>300 && avgX<400 && avgY<300 && avgY>80)
                                {
                                        pt3.x = bndRect.x;
                                        pt3.y = bndRect.y;
                                        pt4.x = bndRect.x + bndRect.width;
                                        pt4.y = bndRect.y + bndRect.height;
                                        if(bndRect.height>35) //把长度小于某个阀值的干扰矩形去掉
                                        {
                                                cvRectangle(pFrame,pt3,pt4,CV_RGB(255,0,0),1, 8, 0 );
                                                //在车辆的中心写编号
                                                cvPutText( pFrame, msg[No].c_str(), pt5, &font1, cvScalar(0,255,0));
                                                //把当前车辆存入跟踪数组
                                                TrackBlock[No]->Direction=1;
                                                TrackBlock[No]->FramesTracked=nFrmNum;
                                                TrackBlock[No]->avgX=avgX;
                                                TrackBlock[No]->avgY=avgY;
                                                No++;
                                        }
                                }
                        }/////查找边界的for 循环结束                
                        
                        //更新背景///////////////////////////////////////////////////
                        cvRunningAvg(pFrameMat, pBkMat, 0.005, 0);
                        //将背景转化为图像格式，用以显示
                        cvConvert(pBkMat, pBkImg);


                        //显示图像////////////////////////////////////////////////////
                        cvShowImage("video", pFrame);
                        //cvShowImage("background", pBkImg);
                        //cvShowImage("foreground", pFrImg);


                        //如果有按键事件,则跳出循环,此等待也为cvShowImage函数提供时间完成显示,等待时间可以根据CPU速度调整
                        if( cvWaitKey(2) >= 0 )
                                break;
                }


        else if(nFrmNum > 3)//从第三帧开始，根据完全目标档案来新增或删除运动车辆档案。
                {
                         cvCvtColor(pFrame, pFrImg, CV_BGR2GRAY);
                         cvConvert(pFrImg, pFrameMat);
                        //高斯滤波先，以平滑图像
                         cvSmooth(pFrameMat, pFrameMat, CV_GAUSSIAN, 3, 0, 0);


                        //在视频中设置并画出感兴趣的区域
                        //cvSetImageROI(pFrame,rect1);
                        cvRectangle(pFrame,pt1,pt2,CV_RGB(255,0,0),2, 8, 0 );


                        //当前帧跟背景图相减,cvAbsDiff计算两个数组差的绝对值
                        cvAbsDiff(pFrameMat, pBkMat, pFrMat);


                        //二值化前景图,void cvThreshold( const CvArr* src, CvArr* dst, double threshold,
            //double max_value, int threshold_type );
                        cvThreshold(pFrMat, pFrImg, 60, 255.0, CV_THRESH_BINARY);


                        //通过查找边界找出ROI矩形区域内的运动车辆，建立完全目标档案
                        //cvCanny(pFrImg, pBkImg, 50, 150, 3);
                        cvDilate(pFrImg,pBkImg,Element,1);
                        cvFindContours( pBkImg, storage, &contour, sizeof(CvContour),
                                mode, CV_CHAIN_APPROX_SIMPLE);
                        //process each moving contour in the current frame用函数cvBoundingRect
                        for(;contour!=0;contour=contour->h_next)
                        {
                                //Get a bounding rectangle around the moving object.
                                bndRect = cvBoundingRect(contour, 0);
                                
                                //Get an average X position of the moving contour.
                                avgX = (bndRect.x + bndRect.x + bndRect.width) / 2; 
                                avgY = (bndRect.y + bndRect.y + bndRect.height) / 2;
                                pt5.x=bndRect.x;//写字的左下角点
                                pt5.y=avgY;


                                //If the center of contour is within ROI than show it
                                if(avgX > 300 && avgX < 400 && avgY < 280 && avgY > 100)
                                {
                                        pt3.x = bndRect.x;
                                        pt3.y = bndRect.y;
                                        pt4.x = bndRect.x + bndRect.width;
                                        pt4.y = bndRect.y + bndRect.height;
                                        if(bndRect.height>35) //把长度小于某个阀值的干扰矩形去掉
                                        {
                                                cvRectangle(pFrame,pt3,pt4,CV_RGB(255,0,0),1, 8, 0 );
                                                //cvPutText(pFrame,msg[No].c_str(), pt5, &font1, cvScalar(0,255,0));
                                                //在跟踪数组中寻找看是否有匹配的车辆，没有则表示是新车辆
                                                for(int i=0;i<10;i++)
                                                {
                                                        if(TrackBlock[i]->avgX !=0 && abs(avgX-TrackBlock[i]->avgX)<20 && 
                                                                 abs(avgY-TrackBlock[i]->avgY)<50)
                                                        {
                                                            cvPutText(pFrame,msg[i].c_str(), pt5, &font1, cvScalar(0,255,0));
                                                                TrackBlock[i]->FramesTracked=nFrmNum;
                                                                TrackBlock[i]->avgX=avgX;
                                                                TrackBlock[i]->avgY=avgY;
                                                                i=10;//使跳出for循环
                                                                FindCar=true;
                                                        }
                                                }
                                                if(FindCar!=true && avgY<120)//表示没有找到车辆
                                                {
                                                        TrackBlock[No]->Direction=1;
                                                        TrackBlock[No]->FramesTracked=nFrmNum;
                                                        TrackBlock[No]->avgX=avgX;
                                                        TrackBlock[No]->avgY=avgY;
                                                        if(No==9){
                                                                No=0;
                                                        }
                                                        else
                                                                No++;
                                                }                        
                                                FindCar=false;//赋值为false为下一次寻找车辆做准备                                                
                                        }
                                }
                        }//轮廓分for循环结束


                        //对于没有匹配的车辆，表示已经出了边界，清空数组
                        for(int j=0;j<10;j++)
                        {
                                if(TrackBlock[j]->FramesTracked != nFrmNum)
                                {
                                        //虽然置为零，但是可能零和当前中心的值在设定的范围内，所以不行。
                                        //TrackBlock[j]=NULL;为何用NULL不行。
                                        TrackBlock[j]->Direction=0;
                                        TrackBlock[j]->FramesTracked=0;
                                        TrackBlock[j]->avgX=0;
                                        TrackBlock[j]->avgY=0;
                                }
                        }


                        //更新背景///////////////////////////////////////////////////
                        cvRunningAvg(pFrameMat, pBkMat, 0.005, 0);
                        //将背景转化为图像格式，用以显示
                        cvConvert(pBkMat, pBkImg);


                        //显示图像////////////////////////////////////////////////////
                        cvShowImage("video", pFrame);
                        cvShowImage("background", pBkImg);
                        cvShowImage("foreground", pFrImg);


                        /*if(nFrmNum/2 ==0)
                        pBkMat=pFrameMat;*/
                        //如果有按键事件,则跳出循环,此等待也为cvShowImage函数提供时间完成显示,等待时间可以根据CPU速度调整
                        if( cvWaitKey(2) >= 0 )
                                break;
                }//
    }//while循环结束


        cvReleaseStructuringElement(&Element);//删除结构元素
    //销毁窗口
    cvDestroyWindow("video");
    cvDestroyWindow("background");
    cvDestroyWindow("foreground");


    //释放图像和矩阵
    cvReleaseImage(&pFrImg);
    cvReleaseImage(&pBkImg);


    cvReleaseMat(&pFrameMat);
    cvReleaseMat(&pFrMat);
    cvReleaseMat(&pBkMat);
}