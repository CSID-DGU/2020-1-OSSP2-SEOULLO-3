package com.seoullo.seoullotour.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class Mark {

    private static final String TAG = "Heart";

    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    private ImageView bookmarkWhite, bookmarkBlack;

    public Mark(ImageView bookmarkWhite, ImageView bookmarkBlack) {
        this.bookmarkWhite = bookmarkWhite;
        this.bookmarkBlack = bookmarkBlack;
    }

    public void toggleBookmark() {
        Log.d(TAG, "toggleLike: toggling bookmark.");

        AnimatorSet animatorSet = new AnimatorSet();

        if (bookmarkBlack.getVisibility() == View.VISIBLE) {
            Log.d(TAG, "toggleLike: toggling black bookmark off.");
            bookmarkBlack.setScaleX(0.1f);
            bookmarkBlack.setScaleY(0.1f);

            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(bookmarkBlack, "scaleX", 1f, 0f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(ACCELERATE_INTERPOLATOR);

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(bookmarkBlack, "scaleY", 1f, 0f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(ACCELERATE_INTERPOLATOR);

            bookmarkBlack.setVisibility(View.GONE);
            bookmarkWhite.setVisibility(View.VISIBLE);

            animatorSet.playTogether(scaleDownY, scaleDownX);

        } else if (bookmarkBlack.getVisibility() == View.GONE) {
            Log.d(TAG, "toggleLike: toggling black bookmark on.");
            bookmarkBlack.setScaleX(0.1f);
            bookmarkBlack.setScaleY(0.1f);

            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(bookmarkBlack, "scaleX", 0.1f, 1f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(DECELERATE_INTERPOLATOR);

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(bookmarkBlack, "scaleY", 0.1f, 1f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(DECELERATE_INTERPOLATOR);

            bookmarkBlack.setVisibility(View.VISIBLE);
            bookmarkWhite.setVisibility(View.GONE);

            animatorSet.playTogether(scaleDownY, scaleDownX);
        }
        animatorSet.start();
    }
}
