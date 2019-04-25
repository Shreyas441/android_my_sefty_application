package com.thesavior.utilities;

import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

public class ViewAnim extends Animation
{
	public void ViewAnimation2(){}

	@Override
	public void initialize(int width, int height, int parentWidth,
                                 int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		setDuration(1000);
		setFillAfter(true);
		setInterpolator(new LinearInterpolator());
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t){
		final Matrix matrix = t.getMatrix();
		matrix.setScale(interpolatedTime, interpolatedTime);
	}
}