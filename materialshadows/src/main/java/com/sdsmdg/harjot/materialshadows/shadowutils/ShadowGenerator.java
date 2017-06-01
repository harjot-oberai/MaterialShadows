package com.sdsmdg.harjot.materialshadows.shadowutils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.sdsmdg.harjot.materialshadows.MaterialShadowViewWrapper;
import com.sdsmdg.harjot.materialshadows.outlineprovider.CustomViewOutlineProvider;
import com.sdsmdg.harjot.materialshadows.utilities.GrahamScan;
import com.sdsmdg.harjot.materialshadows.utilities.Point2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.sdsmdg.harjot.materialshadows.Constants.DEFAULT_ANIMATE_SHADOW;
import static com.sdsmdg.harjot.materialshadows.Constants.DEFAULT_ANIMATION_TIME;
import static com.sdsmdg.harjot.materialshadows.Constants.DEFAULT_CALCULATE_ASYNC;
import static com.sdsmdg.harjot.materialshadows.Constants.DEFAULT_SHADOW_ALPHA;
import static com.sdsmdg.harjot.materialshadows.Constants.DEFAULT_SHOW_WHEN_ALL_READY;
import static com.sdsmdg.harjot.materialshadows.Constants.DEFAULT_X_OFFSET;
import static com.sdsmdg.harjot.materialshadows.Constants.DEFAULT_Y_OFFSET;
import static com.sdsmdg.harjot.materialshadows.Constants.POS_UPDATE_ALL;

public class ShadowGenerator {

    private final Object TASKS_LOCK = new Object();

    private List<Future<?>> tasksInProgress = new ArrayList<>();
    private ExecutorService workerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private Handler uiThreadHandler = new Handler(Looper.getMainLooper());

    private SparseArray<Path> viewPaths;

    private float offsetX = DEFAULT_X_OFFSET;
    private float offsetY = DEFAULT_Y_OFFSET;

    private float shadowAlpha = DEFAULT_SHADOW_ALPHA;

    private boolean shouldShowWhenAllReady = DEFAULT_SHOW_WHEN_ALL_READY;
    private boolean shouldCalculateAsync = DEFAULT_CALCULATE_ASYNC;
    private boolean shouldAnimateShadow = DEFAULT_ANIMATE_SHADOW;

    private int animationDuration = DEFAULT_ANIMATION_TIME;

    private int childrenWithShadow;

    private ViewGroup viewGroup;

    public ShadowGenerator(ViewGroup viewGroup, float offsetX, float offsetY, float shadowAlpha, boolean shouldShowWhenAllReady, boolean shouldCalculateAsync, boolean shouldAnimateShadow, int animationDuration) {
        this.viewGroup = viewGroup;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.shadowAlpha = shadowAlpha;
        this.shouldShowWhenAllReady = shouldShowWhenAllReady;
        this.shouldCalculateAsync = shouldCalculateAsync;
        this.shouldAnimateShadow = shouldAnimateShadow;
        this.animationDuration = animationDuration;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
        updateShadows(POS_UPDATE_ALL);
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
        updateShadows(POS_UPDATE_ALL);
    }

    public float getShadowAlpha() {
        return shadowAlpha;
    }

    public void setShadowAlpha(float shadowAlpha) {
        this.shadowAlpha = shadowAlpha;
        updateShadows(POS_UPDATE_ALL);
    }

    public boolean isShouldShowWhenAllReady() {
        return shouldShowWhenAllReady;
    }

    public void setShouldShowWhenAllReady(boolean shouldShowWhenAllReady) {
        this.shouldShowWhenAllReady = shouldShowWhenAllReady;
    }

    public boolean isShouldCalculateAsync() {
        return shouldCalculateAsync;
    }

    public void setShouldCalculateAsync(boolean shouldCalculateAsync) {
        this.shouldCalculateAsync = shouldCalculateAsync;
    }

    public boolean isShouldAnimateShadow() {
        return shouldAnimateShadow;
    }

    public void setShouldAnimateShadow(boolean shouldAnimateShadow) {
        this.shouldAnimateShadow = shouldAnimateShadow;
    }

    public int getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    public void generate() {
        clearShadowCache(); //Maybe some children changed their size
        childrenWithShadow = 0;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof MaterialShadowViewWrapper) {
                continue;
            }
            childrenWithShadow++;
            if (shouldCalculateAsync) {
                calculateAndRenderShadowAsync(view, i);
            } else {
                calculateAndRenderShadow(view, i);
            }
        }
    }

    public void releaseResources() {
        workerPool.shutdown();
        uiThreadHandler.removeCallbacksAndMessages(null);
    }

    private void clearShadowCache() {
        cancelTasksInProgress();
        uiThreadHandler.removeCallbacksAndMessages(null);
        viewPaths = new SparseArray<>();
    }

    private void updateShadows(int pos) {
        if (pos == POS_UPDATE_ALL) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                setShadowOutlineProviderAt(i);
            }
        } else {
            setShadowOutlineProviderAt(pos);
        }
    }

    private void setShadowOutlineProviderAt(int childIndex) {
        Path shadowPath = getViewPathWithOffsetAt(childIndex);
        if (shadowPath == null) {
            //Path calculation is still in progress
            return;
        }
        final View child = viewGroup.getChildAt(childIndex);
        CustomViewOutlineProvider outlineProvider = new CustomViewOutlineProvider(shadowPath, shadowAlpha);
        child.setOutlineProvider(outlineProvider);

        if (shouldAnimateShadow) {
            animateOutlineAlpha(child, outlineProvider);
        }
    }

    private void animateOutlineAlpha(final View child, CustomViewOutlineProvider outlineProvider) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(outlineProvider, "alpha", 0, shadowAlpha);
        animator.setDuration(animationDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                child.invalidateOutline();
            }
        });
        animator.start();
    }

    private Path getViewPathWithOffsetAt(int position) {
        Path noOffsetPath = viewPaths.get(position);
        if (noOffsetPath == null) {
            return null;
        }
        Path path = new Path();
        path.set(noOffsetPath);
        path.offset(offsetX, offsetY);
        return path;
    }

    private void calculateAndRenderShadowAsync(final View view, final int pos) {
        final Future[] future = new Future[1];
        future[0] = workerPool.submit(new Runnable() {
            @Override
            public void run() {
                calculateAndRenderShadow(view, pos);
                synchronized (TASKS_LOCK) {
                    tasksInProgress.remove(future[0]);
                }
            }
        });
        tasksInProgress.add(future[0]);
    }

    private void cancelTasksInProgress() {
        synchronized (TASKS_LOCK) {
            for (int i = tasksInProgress.size() - 1; i >= 0; i--) {
                Future<?> task = tasksInProgress.get(i);
                task.cancel(true);
                tasksInProgress.remove(i);
            }
        }
    }

    private void calculateAndRenderShadow(final View view, int pos) {
        view.buildDrawingCache();
        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        Path path = new Path();
        List<Point2D> arrayListOutlinePoints;
        Bitmap bitmap = view.getDrawingCache();

        try {
            //We need to copy it, because drawing cache will be recycled if view is detached
            bitmap = bitmap.copy(bitmap.getConfig(), false);
            arrayListOutlinePoints = getOutlinePoints(bitmap);
            if (arrayListOutlinePoints.isEmpty()) {
                return;
            }
        } catch (Exception e) {
            //If drawing cache has been recycled, IllegalStateException will be thrown on copy
            return;
        } finally {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }

        Point2D[] outlinePoints = arrayListOutlinePoints.toArray(new Point2D[arrayListOutlinePoints.size()]);

        GrahamScan grahamScan = new GrahamScan(outlinePoints);

        List<Point2D> arrayListHullPoints = new ArrayList<>();
        for (Point2D point2D : grahamScan.hull()) {
            arrayListHullPoints.add(point2D);
        }
        Point2D[] hullPoints = arrayListHullPoints.toArray(new Point2D[arrayListHullPoints.size()]);

        path.moveTo((float) hullPoints[0].x(), (float) hullPoints[0].y());
        for (int i = 1; i < hullPoints.length; i++) {
            path.lineTo((float) hullPoints[i].x(), (float) hullPoints[i].y());
        }

        synchronized (TASKS_LOCK) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            uiThreadHandler.postAtFrontOfQueue(new SetViewOutlineTask(pos, path));
        }
    }

    private List<Point2D> getOutlinePoints(Bitmap bitmap) {
        ArrayList<Point2D> arrayList = new ArrayList<>();

        for (int i = 0; i < bitmap.getHeight(); i++) {
            if (Color.alpha(bitmap.getPixel(0, i)) > 0) {
                arrayList.add(new Point2D(0, i));
            }

            if (Color.alpha(bitmap.getPixel(bitmap.getWidth() - 1, i)) > 0) {
                arrayList.add(new Point2D(bitmap.getWidth() - 1, i));
            }
        }

        if (Thread.currentThread().isInterrupted()) {
            return Collections.emptyList();
        }

        for (int i = 0; i < bitmap.getHeight(); i++) {
            for (int j = 1; j < bitmap.getWidth() - 1; j++) {
                if (Color.alpha(bitmap.getPixel(j - 1, i)) == 0 && Color.alpha(bitmap.getPixel(j, i)) > 0) {
                    arrayList.add(new Point2D(j, i));
                }
                if (Color.alpha(bitmap.getPixel(j - 1, i)) > 0 && Color.alpha(bitmap.getPixel(j, i)) == 0) {
                    arrayList.add(new Point2D(j - 1, i));
                }
            }
        }

        return arrayList;
    }

    private class SetViewOutlineTask implements Runnable {

        private int viewPos;
        private Path shadowPath;

        private SetViewOutlineTask(int pos, Path path) {
            this.viewPos = pos;
            this.shadowPath = path;
        }

        @Override
        public void run() {
            if (viewGroup.isAttachedToWindow()) {
                viewPaths.put(viewPos, shadowPath);
                if (shouldShowWhenAllReady) {
                    if (viewPaths.size() == childrenWithShadow) {
                        updateShadows(POS_UPDATE_ALL);
                    }
                    return;
                }
                updateShadows(viewPos);
            }
        }
    }

}
