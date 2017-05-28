package com.sdsmdg.harjot.materialshadows.outlineprovider;

import android.graphics.Outline;
import android.graphics.Path;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.sdsmdg.harjot.materialshadows.utilities.Point2D;

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
        outline.setAlpha(alpha);
    }
}
