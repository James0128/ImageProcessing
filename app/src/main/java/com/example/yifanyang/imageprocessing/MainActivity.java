package com.example.yifanyang.imageprocessing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;

public class MainActivity extends AppCompatActivity {


    private final int SELECTED_PHOTO=1;
    private ImageView ivImage,ivImageProcessed;
    Mat src;
    static  int ACTION_MODE = 0;
    private BaseLoaderCallback mOpenCVCallBack= new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                    //完成工作
                    break;
                default:
                    super.onManagerConnected(status);
                    break;

            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivImage= (ImageView) findViewById(R.id.ivImage);
        ivImageProcessed = (ImageView) findViewById(R.id.ivImageProcessed);
        Intent intent = getIntent();
        if (intent.hasExtra("ACTION_MODE")){
            ACTION_MODE = intent.getIntExtra("ACTION_MODE",0);
        }
    }
    //从相册插入图片的代码

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_load_image){
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent,SELECTED_PHOTO);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode){
            case SELECTED_PHOTO:
                if (requestCode != RESULT_OK){

                    try {
                        //以位图载入图像，并将其转换为MAT以供处理的代码
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage= BitmapFactory.decodeStream(imageStream);
                        src = new Mat(selectedImage.getHeight(),selectedImage.getWidth(), CvType.CV_8UC4);
                        Utils.bitmapToMat(selectedImage,src);
                        switch (ACTION_MODE){
                            //根据需要操作在这里加入不同的case
                            case HomeActivity.MEAN_BLUR:
                                Imgproc.blur(src, src, new Size(10,10));
                                break;
                            case HomeActivity.GAUSSIAN_BLUR:
                                Imgproc.GaussianBlur(src, src, new Size(3,3), 0);
                                break;

                            //椒盐噪声
                            case HomeActivity.MEDIAN_BLUR:
                                Imgproc.medianBlur(src,src,3);
                                break;
                            //锐化
                            case HomeActivity.SHARPEN:
                                Mat kernel = new Mat(3,3,CvType.CV_16SC1);//图像深度设为16sc1，表示图像包含一个通道C1。
                                kernel.put(0,0,0,-1,0,-1,5,-1,0,-1,0);
                                Imgproc.filter2D(src,src,src.depth(),kernel);
                                break;
                            //膨胀
                            case HomeActivity.DILATE:
                                Mat kernelDilate = Imgproc.getStructuringElement(
                                        Imgproc.MORPH_RECT,new Size(3,3));
                                Imgproc.dilate(src,src,kernelDilate);
                                break;
                            //腐蚀
                            case HomeActivity.ERODE:
                                Mat KernelErode = Imgproc.getStructuringElement(
                                        Imgproc.MORPH_ELLIPSE,new Size(5,5)
                                );
                                Imgproc.erode(src,src,KernelErode);
                                break;
                            //注意 腐蚀和膨胀并不是逆运算

                            //阈值化
                            case HomeActivity.THRESHOLD:
                                Imgproc.threshold(src,src,100,255,Imgproc.THRESH_BINARY);
                                break;
                            case HomeActivity.ADAPTIVE_THRESHOLD:
                                Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
                                Imgproc.adaptiveThreshold(src, src, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 3, 0);
                                break;


                        }

                        //将mat转换为位图，以便在ImageView中显示的代码
                        //还要在Imageview中载入原始图像
                        Bitmap processedImage = Bitmap.createBitmap(src.cols(),src.rows(),Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(src,processedImage);
                        Log.i("ccc","ot null");
                        if (selectedImage != null){

                        }
                        ivImage.setImageBitmap(selectedImage);
                        ivImageProcessed.setImageBitmap(processedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

        }


    }
    @Override
    protected void onResume() {
        super.onResume();


           OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mOpenCVCallBack);

       // mOpenCVCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);

    }
}
