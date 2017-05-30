package com.sdsmdg.harjot.materialshadows.outlineprovider;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Outline;
import android.graphics.Path;
import android.view.View;
import android.view.ViewOutlineProvider;

public class CustomViewOutlineProvider extends ViewOutlineProvider {

    private Path path;
    private float alpha;

    public CustomViewOutlineProvider(Path path, float alpha) {
        this.path = path;
        this.alpha = alpha;
    }

    @Override
    public void getOutline(View view, Outline outline) {
        outline.setConvexPath(path);
        if (alpha >= 1.0f) {
            alpha = 0.99f;
        } else if (alpha < 0.0f) {
            alpha = 0.0f;
        }
        outline.setAlpha(alpha);
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}
