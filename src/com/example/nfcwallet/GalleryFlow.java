package com.example.nfcwallet;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;
import android.widget.ImageView;

public class GalleryFlow extends Gallery {
	private Camera mCamera = new Camera();// 相機類
	private int mMaxRotationAngle = 50;// 最大轉動角度
	private int mMaxZoom = -250;// //最大縮放值
	private int mCoveflowCenter;// 半徑值

	public GalleryFlow(Context context) {
		super(context);
		// 支援轉換 ,執行getChildStaticTransformation方法
		this.setStaticTransformationsEnabled(true);
	}

	public GalleryFlow(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setStaticTransformationsEnabled(true);
	}

	public GalleryFlow(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setStaticTransformationsEnabled(true);
	}

	/**
	 * 獲取旋轉最大角度
	 * 
	 * @return
	 */
	public int getMaxRotationAngle() {
		return mMaxRotationAngle;
	}

	/**
	 * 設置旋轉最大角度
	 * 
	 * @param maxRotationAngle
	 */
	public void setMaxRotationAngle(int maxRotationAngle) {
		mMaxRotationAngle = maxRotationAngle;
	}

	/**
	 * 獲取最大縮放值
	 * 
	 * @return
	 */
	public int getMaxZoom() {
		return mMaxZoom;
	}

	/**
	 * 設置最大縮放值
	 * 
	 * @param maxZoom
	 */
	public void setMaxZoom(int maxZoom) {
		mMaxZoom = maxZoom;
	}

	/**
	 * 獲取半徑值
	 * 
	 * @return
	 */
	private int getCenterOfCoverflow() {
		//Log.e("CoverFlow Width+Height", getWidth() + "*" + getHeight());
		return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2
				+ getPaddingLeft();
	}

	/**
	 * @param view
	 * @return
	 */
	private static int getCenterOfView(View view) {
		return view.getLeft() + view.getWidth() / 2;
	}

	// 控制gallery中每個圖片的旋轉(重寫的gallery中方法)
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		// 取得當前子view的半徑值
		final int childCenter = getCenterOfView(child);
		final int childWidth = child.getWidth();
		// 旋轉角度
		int rotationAngle = 0;
		// 重置轉換狀態
		t.clear();
		// 設置轉換類型
		t.setTransformationType(Transformation.TYPE_MATRIX);
		// 如果圖片位於中心位置不需要進行旋轉
		if (childCenter == mCoveflowCenter) {
			transformImageBitmap((ImageView) child, t, 0);
			} 
		else {
			// 根據圖片在gallery中的位置來計算圖片的旋轉角度
			rotationAngle = (int) (((float) (mCoveflowCenter - childCenter) / childWidth) * mMaxRotationAngle);
			//System.out.println("rotationAngle:" + rotationAngle);
			// 如果旋轉角度絕對值大於最大旋轉角度返回（-mMaxRotationAngle或mMaxRotationAngle;）
			if (Math.abs(rotationAngle) > mMaxRotationAngle) {
				if(rotationAngle < 0){
					rotationAngle =  (-mMaxRotationAngle);
				}
				else{
					rotationAngle =  mMaxRotationAngle;
				}
			}
			transformImageBitmap((ImageView) child, t, rotationAngle);
		}
		
		return true;
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mCoveflowCenter = getCenterOfCoverflow();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	private void transformImageBitmap(ImageView child, Transformation t,
			int rotationAngle) {
		// 對效果進行保存
		mCamera.save();
		final Matrix imageMatrix = t.getMatrix();
		// 圖片高度
		final int imageHeight = child.getLayoutParams().height;
		// 圖片寬度
		final int imageWidth = child.getLayoutParams().width;

		// 返回旋轉角度的絕對值
		final int rotation = Math.abs(rotationAngle);

		// 在Z軸上正向移動camera的視角，實際效果為放大圖片。
		// 如果在Y軸上移動，則圖片上下移動；X軸上對應圖片左右移動。
		mCamera.translate(0.0f, 0.0f, 100.0f);
		// As the angle of the view gets less, zoom in
		if (rotation < mMaxRotationAngle) {
			float zoomAmount = (float) (mMaxZoom + (rotation * 1.5));
			mCamera.translate(0.0f, 0.0f, zoomAmount);
		}
		// 在Y軸上旋轉，對應圖片豎向向裡翻轉。
		// 如果在X軸上旋轉，則對應圖片橫向向裡翻轉。
		mCamera.rotateY(rotationAngle);
		mCamera.getMatrix(imageMatrix);
		imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2));
		imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2));
		mCamera.restore();
	}
}
