package com.sdsmdg.harjot.materialshadows;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.RelativeLayout;

import com.sdsmdg.harjot.materialshadows.outlineprovider.CustomViewOutlineProvider;
import com.sdsmdg.harjot.materialshadows.utilities.GrahamScan;
import com.sdsmdg.harjot.materialshadows.utilities.Point2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MaterialShadowViewWrapper extends RelativeLayout {

    private static final float DEFAULT_X_OFFSET = 0.0f;
    private static final float DEFAULT_Y_OFFSET = 0.0f;
    private static final float DEFAULT_SHADOW_ALPHA = 0.99f;
    private static final boolean DEFAULT_SHOW_WHEN_ALL_READY = true;
    private static final boolean DEFAULT_CALCULATE_ASYNC = true;
    private static final boolean DEFAULT_ANIMATE_SHADOW = true;
    private static final int DEFAULT_ANIMATION_TIME = 300;

    private static final int POS_UPDATE_ALL = -1;

    private final Object TASKS_LOCK = new Object();

    List<Future<?>> tasksInProgress;
    ExecutorService workerPool;
    Handler uiThreadHandler;

    SparseArray<Path> viewPaths;

    float offsetX = DEFAULT_X_OFFSET;
    float offsetY = DEFAULT_Y_OFFSET;

    float shadowAlpha = DEFAULT_SHADOW_ALPHA;

    boolean shouldShowWhenAllReady = DEFAULT_SHOW_WHEN_ALL_READY;
    boolean shouldCalculateAsync = DEFAULT_CALCULATE_ASYNC;
    boolean shouldAnimateShadow = DEFAULT_ANIMATE_SHADOW;

    int animationDuration = DEFAULT_ANIMATION_TIME;

    int childrenWithShadow;

    public MaterialShadowViewWrapper(Context context) {
        super(context);
    }

    public MaterialShadowViewWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        initXMLAttrs(context, attrs);
    }

    public MaterialShadowViewWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initXMLAttrs(context, attrs);
    }

    {
        workerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        uiThreadHandler = new Handler(Looper.getMainLooper());
        tasksInProgress = new ArrayList<>();
    }

    void initXMLAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaterialShadowViewWrapper);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.MaterialShadowViewWrapper_shadowAlpha) {
                shadowAlpha = a.getFloat(attr, DEFAULT_SHADOW_ALPHA);
            } else if (attr == R.styleable.MaterialShadowViewWrapper_shadowOffsetX) {
                offsetX = a.getFloat(attr, DEFAULT_X_OFFSET);
            } else if (attr == R.styleable.MaterialShadowViewWrapper_shadowOffsetY) {
                offsetY = a.getFloat(attr, DEFAULT_Y_OFFSET);
            } else if (attr == R.styleable.MaterialShadowViewWrapper_calculateAsync) {
                shouldCalculateAsync = a.getBoolean(attr, DEFAULT_CALCULATE_ASYNC);
            } else if (attr == R.styleable.MaterialShadowViewWrapper_animateShadow) {
                shouldAnimateShadow = a.getBoolean(attr, DEFAULT_ANIMATE_SHADOW);
            } else if (attr == R.styleable.MaterialShadowViewWrapper_showWhenAllReady) {
                shouldShowWhenAllReady = a.getBoolean(attr, DEFAULT_SHOW_WHEN_ALL_READY);
            } else if (attr == R.styleable.MaterialShadowViewWrapper_animationDuration) {
                animationDuration = a.getInteger(attr, DEFAULT_ANIMATION_TIME);
            }
        }
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        clearShadowCache(); //Maybe some children changed their size
        childrenWithShadow = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        workerPool.shutdown();
        uiThreadHandler.removeCallbacksAndMessages(null);
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

    public void setShouldAnimateShadow(boolean shouldAnimateShadow) {
        this.shouldAnimateShadow = shouldAnimateShadow;
    }

    public boolean shouldAnimateShadow() {
        return shouldAnimateShadow;
    }

    public void setShouldCalculateAsync(boolean shouldCalculateAsync) {
        this.shouldCalculateAsync = shouldCalculateAsync;
    }

    public boolean shouldCalculateAsync() {
        return shouldCalculateAsync;
    }

    public void setShowShadowsWhenAllReady(boolean shouldWaitForAllReady) {
        this.shouldShowWhenAllReady = shouldWaitForAllReady;
    }

    public boolean shouldWaitForAllReady() {
        return shouldShowWhenAllReady;
    }

    void clearShadowCache() {
        cancelTasksInProgress();
        uiThreadHandler.removeCallbacksAndMessages(null);
        viewPaths = new SparseArray<>();
    }

    void updateShadows(int pos) {
        if (pos == POS_UPDATE_ALL) {
            for (int i = 0; i < getChildCount(); i++) {
                setShadowOutlineProviderAt(i);
            }
        } else {
            setShadowOutlineProviderAt(pos);
        }
    }

    void setShadowOutlineProviderAt(int childIndex) {
        Path shadowPath = getViewPathWithOffsetAt(childIndex);
        if (shadowPath == null) {
            //Path calculation is still in progress
            return;
        }
        final View child = getChildAt(childIndex);
        CustomViewOutlineProvider outlineProvider = new CustomViewOutlineProvider(shadowPath, shadowAlpha);
        child.setOutlineProvider(outlineProvider);

        if (shouldAnimateShadow) {
            animateOutlineAlpha(child, outlineProvider);
        }
    }

    void animateOutlineAlpha(final View child, CustomViewOutlineProvider outlineProvider) {
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

    Path getViewPathWithOffsetAt(int position) {
        Path noOffsetPath = viewPaths.get(position);
        if (noOffsetPath == null) {
            return null;
        }
        Path path = new Path();
        path.set(noOffsetPath);
        path.offset(offsetX, offsetY);
        return path;
    }

    void calculateAndRenderShadowAsync(final View view, final int pos) {
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

    void cancelTasksInProgress() {
        synchronized (TASKS_LOCK) {
            for (int i = tasksInProgress.size() - 1; i >= 0; i--) {
                Future<?> task = tasksInProgress.get(i);
                task.cancel(true);
                tasksInProgress.remove(i);
            }
        }
    }

    void calculateAndRenderShadow(final View view, int pos) {
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

    List<Point2D> getOutlinePoints(Bitmap bitmap) {
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
            if (isAttachedToWindow()) {
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
