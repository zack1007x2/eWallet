package com.example.nfcwallet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.Shader.TileMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;



public class ImageAdapter extends BaseAdapter {
	int mGalleryItemBackground;
	private Context mContext;

	private int[] mImageIds;

	private ImageView[] mImages;

	public ImageAdapter(Context c, int[] ImageIds) {
		mContext = c;
		mImageIds = ImageIds;
		mImages = new ImageView[mImageIds.length];
	}

	/** 
     * 創建倒影效果 
     * @return 
     */  
    public boolean createReflectedImages() {  
     //倒影圖和原圖之間的距離  
     final int reflectionGap = 4;  
     int index = 0;  
     for (int imageId : mImageIds) {  
          //返回原圖解碼之後的bitmap對象  
          Bitmap originalImage = BitmapFactory.decodeResource(mContext.getResources(), imageId);  
          int width = originalImage.getWidth();  
          int height = originalImage.getHeight();  
          //創建矩陣對象  
          Matrix matrix = new Matrix();  
            
          //指定一個角度以0,0為座標進行旋轉  
          // matrix.setRotate(30);  
            
          //指定矩陣(x軸不變，y軸相反)  
          matrix.preScale(1, -1);  
            
          //將矩陣應用到該原圖之中，返回一個寬度不變，高度為原圖1/2的倒影點陣圖  
          Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,  
            height/2, width, height/2, matrix, false);  
            
          //創建一個寬度不變，高度為原圖+倒影圖高度的點陣圖  
          Bitmap bitmapWithReflection = Bitmap.createBitmap(width,  
            (height + height / 2), Config.ARGB_8888);  
            
          //將上面創建的點陣圖初始化到畫布  
          Canvas canvas = new Canvas(bitmapWithReflection);  
          canvas.drawBitmap(originalImage, 0, 0, null);  
            
          Paint deafaultPaint = new Paint();   
          deafaultPaint.setAntiAlias(true);  
          //canvas.drawRect(0, height, width, height + reflectionGap,deafaultPaint);  
          canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);  
          Paint paint = new Paint();  
          paint.setAntiAlias(true);  
             
          /** 
           * 參數一:為漸變起初點座標x位置， 
           * 參數二:為y軸位置， 
           * 參數三和四:分辨對應漸變終點， 
           * 最後參數為平鋪方式， 
           * 這裡設置為鏡像Gradient是基於Shader類，所以我們通過Paint的setShader方法來設置這個漸變 
           */  
          LinearGradient shader = new LinearGradient(0,originalImage.getHeight(), 0,  
                  bitmapWithReflection.getHeight() + reflectionGap,0x70ffffff, 0x00ffffff, TileMode.MIRROR);  
          //設置陰影  
          paint.setShader(shader);  
          paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));  
          //用已經定義好的畫筆構建一個矩形陰影漸變效果  
          canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()+ reflectionGap, paint);  
            
          //創建一個ImageView用來顯示已經畫好的bitmapWithReflection  
          ImageView imageView = new ImageView(mContext);  
          imageView.setImageBitmap(bitmapWithReflection);  
          //設置imageView大小 ，也就是最終顯示的圖片大小  
          imageView.setLayoutParams(new GalleryFlow.LayoutParams(420, 630));  
          //imageView.setScaleType(ScaleType.MATRIX);  
          mImages[index++] = imageView;  
     }  
     return true;  
    }  
    
    
	public int getCount() {
		return mImageIds.length;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		return mImages[position];
	}

	/**
	 * Returns the size (0.0f to 1.0f) of the views depending on the 'offset' to
	 * the center.
	 */
	public float getScale(boolean focused, int offset) {
		// Formula: 1 / (2 ^ offset)
		return Math.max(0, 1.0f / (float) Math.pow(2, Math.abs(offset)));
	}

}