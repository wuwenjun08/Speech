package com.tchip.carlauncher.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.OvershootInterpolator;

import com.tchip.carlauncher.view.CircularProgressDrawable;

public class ProgressAnimationUtil {

	/**
	 * This animation was intended to keep a pressed state of the Drawable
	 * 
	 * @return Animation
	 */
	private Animator preparePressedAnimation(CircularProgressDrawable drawable) {
		Animator animation = ObjectAnimator.ofFloat(drawable,
				CircularProgressDrawable.CIRCLE_SCALE_PROPERTY,
				drawable.getCircleScale(), 0.65f);
		animation.setDuration(120);
		return animation;
	}

	/**
	 * This animation will make a pulse effect to the inner circle
	 * 
	 * @return Animation
	 */
	public static Animator preparePulseAnimation(
			CircularProgressDrawable drawable) {
		AnimatorSet animation = new AnimatorSet();

		Animator firstBounce = ObjectAnimator.ofFloat(drawable,
				CircularProgressDrawable.CIRCLE_SCALE_PROPERTY,
				drawable.getCircleScale(), 0.88f);
		firstBounce.setDuration(300);
		firstBounce.setInterpolator(new CycleInterpolator(1));
		Animator secondBounce = ObjectAnimator.ofFloat(drawable,
				CircularProgressDrawable.CIRCLE_SCALE_PROPERTY, 0.75f, 0.83f);
		secondBounce.setDuration(300);
		secondBounce.setInterpolator(new CycleInterpolator(1));
		Animator thirdBounce = ObjectAnimator.ofFloat(drawable,
				CircularProgressDrawable.CIRCLE_SCALE_PROPERTY, 0.75f, 0.80f);
		thirdBounce.setDuration(300);
		thirdBounce.setInterpolator(new CycleInterpolator(1));

		animation.playSequentially(firstBounce, secondBounce, thirdBounce);
		return animation;
	}

	/**
	 * Style 1 animation will simulate a indeterminate loading while taking
	 * advantage of the inner circle to provide a progress sense
	 * 
	 * @return Animation
	 */
	public static Animator prepareStyle1Animation(
			CircularProgressDrawable drawable) {
		AnimatorSet animation = new AnimatorSet();
		final CircularProgressDrawable circularProgressDrawable = drawable;

		final Animator indeterminateAnimation = ObjectAnimator.ofFloat(
				drawable, CircularProgressDrawable.PROGRESS_PROPERTY, 0, 7200);
		indeterminateAnimation.setDuration(7200);

		Animator innerCircleAnimation = ObjectAnimator.ofFloat(drawable,
				CircularProgressDrawable.CIRCLE_SCALE_PROPERTY, 0f, 0.75f);
		innerCircleAnimation.setDuration(5500);
		innerCircleAnimation.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				circularProgressDrawable.setIndeterminate(true);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				indeterminateAnimation.end();
				circularProgressDrawable.setIndeterminate(false);
				circularProgressDrawable.setProgress(0);
			}
		});

		animation.playTogether(innerCircleAnimation, indeterminateAnimation);
		return animation;
	}

	/**
	 * Style 2 animation will fill the outer ring while applying a color effect
	 * from red to green
	 * 
	 * @return Animation
	 */
	public static Animator prepareStyle2Animation(Context context,
			CircularProgressDrawable drawable) {
		AnimatorSet animation = new AnimatorSet();

		ObjectAnimator progressAnimation = ObjectAnimator.ofFloat(drawable,
				CircularProgressDrawable.PROGRESS_PROPERTY, 0f, 1f);
		progressAnimation.setDuration(3600);
		progressAnimation
				.setInterpolator(new AccelerateDecelerateInterpolator());

		ObjectAnimator colorAnimator = ObjectAnimator.ofInt(
				drawable,
				CircularProgressDrawable.RING_COLOR_PROPERTY,
				context.getResources().getColor(android.R.color.holo_red_dark),
				context.getResources().getColor(
						android.R.color.holo_green_light));
		colorAnimator.setEvaluator(new ArgbEvaluator());
		colorAnimator.setDuration(3600);

		animation.playTogether(progressAnimation, colorAnimator);
		return animation;
	}

	/**
	 * Style 3 animation will turn a 3/4 animation with Anticipate/Overshoot
	 * interpolation to a blank waiting - like state, wait for 2 seconds then
	 * return to the original state
	 * 
	 * @return Animation
	 */
	public static Animator prepareStyle3Animation(
			CircularProgressDrawable drawable) {
		AnimatorSet animation = new AnimatorSet();

		ObjectAnimator progressAnimation = ObjectAnimator.ofFloat(drawable,
				CircularProgressDrawable.PROGRESS_PROPERTY, 0.75f, 0f);
		progressAnimation.setDuration(1200);
		progressAnimation.setInterpolator(new AnticipateInterpolator());

		Animator innerCircleAnimation = ObjectAnimator.ofFloat(drawable,
				CircularProgressDrawable.CIRCLE_SCALE_PROPERTY, 0.75f, 0f);
		innerCircleAnimation.setDuration(1200);
		innerCircleAnimation.setInterpolator(new AnticipateInterpolator());

		ObjectAnimator invertedProgress = ObjectAnimator.ofFloat(drawable,
				CircularProgressDrawable.PROGRESS_PROPERTY, 0f, 0.75f);
		invertedProgress.setDuration(1200);
		invertedProgress.setStartDelay(3200);
		invertedProgress.setInterpolator(new OvershootInterpolator());

		Animator invertedCircle = ObjectAnimator.ofFloat(drawable,
				CircularProgressDrawable.CIRCLE_SCALE_PROPERTY, 0f, 0.75f);
		invertedCircle.setDuration(1200);
		invertedCircle.setStartDelay(3200);
		invertedCircle.setInterpolator(new OvershootInterpolator());

		animation.playTogether(progressAnimation, innerCircleAnimation,
				invertedProgress, invertedCircle);
		return animation;
	}
}
