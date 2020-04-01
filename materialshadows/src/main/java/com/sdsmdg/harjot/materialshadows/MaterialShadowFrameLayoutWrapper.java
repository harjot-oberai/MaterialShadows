package com.sdsmdg.harjot.materialshadows;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.sdsmdg.harjot.materialshadows.shadowutils.ShadowGenerator;

import static com.sdsmdg.harjot.materialshadows.Constants.DEFAULT_ANIMATE_SHADOW;
import static com.sdsmdg.harjot.materialshadows.Constants.DEFAULT_ANIMATION_TIME;
import static com.sdsmdg.harjot.materialshadows.Constants.DEFAULT_CALCULATE_ASYNC;
import static com.sdsmdg.harjot.materialshadows.Constants.DEFAULT_SHADOW_ALPHA;
import static com.sdsmdg.harjot.materialshadows.Constants.DEFAULT_SHOW_WHEN_ALL_READY;
import static com.sdsmdg.harjot.materialshadows.Constants.DEFAULT_X_OFFSET;
import static com.sdsmdg.harjot.materialshadows.Constants.DEFAULT_Y_OFFSET;

public class MaterialShadowFrameLayoutWrapper extends FrameLayout {

    float offsetX = DEFAULT_X_OFFSET;
    float offsetY = DEFAULT_Y_OFFSET;

    float shadowAlpha = DEFAULT_SHADOW_ALPHA;

    boolean shouldShowWhenAllReady = DEFAULT_SHOW_WHEN_ALL_READY;
    boolean shouldCalculateAsync = DEFAULT_CALCULATE_ASYNC;
    boolean shouldAnimateShadow = DEFAULT_ANIMATE_SHADOW;

    int animationDuration = DEFAULT_ANIMATION_TIME;

    ShadowGenerator shadowGenerator;

    public MaterialShadowFrameLayoutWrapper(Context context) {
        super(context);
    }

    public MaterialShadowFrameLayoutWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        initXMLAttrs(context, attrs);
    }

    public MaterialShadowFrameLayoutWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initXMLAttrs(context, attrs);
    }

    void initXMLAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaterialShadowFrameLayoutWrapper);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.MaterialShadowFrameLayoutWrapper_shadowAlpha) {
                shadowAlpha = a.getFloat(attr, DEFAULT_SHADOW_ALPHA);
            } else if (attr == R.styleable.MaterialShadowFrameLayoutWrapper_shadowOffsetX) {
                offsetX = a.getFloat(attr, DEFAULT_X_OFFSET);
            } else if (attr == R.styleable.MaterialShadowFrameLayoutWrapper_shadowOffsetY) {
                offsetY = a.getFloat(attr, DEFAULT_Y_OFFSET);
            } else if (attr == R.styleable.MaterialShadowFrameLayoutWrapper_calculateAsync) {
                shouldCalculateAsync = a.getBoolean(attr, DEFAULT_CALCULATE_ASYNC);
            } else if (attr == R.styleable.MaterialShadowFrameLayoutWrapper_animateShadow) {
                shouldAnimateShadow = a.getBoolean(attr, DEFAULT_ANIMATE_SHADOW);
            } else if (attr == R.styleable.MaterialShadowFrameLayoutWrapper_showWhenAllReady) {
                shouldShowWhenAllReady = a.getBoolean(attr, DEFAULT_SHOW_WHEN_ALL_READY);
            } else if (attr == R.styleable.MaterialShadowFrameLayoutWrapper_animationDuration) {
                animationDuration = a.getInteger(attr, DEFAULT_ANIMATION_TIME);
            }
        }
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (shadowGenerator == null) {
            shadowGenerator = new ShadowGenerator(this, offsetX, offsetY, shadowAlpha, shouldShowWhenAllReady, shouldCalculateAsync, shouldAnimateShadow, animationDuration);
        }
        shadowGenerator.generate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (shadowGenerator != null) {
            shadowGenerator.releaseResources();
            shadowGenerator = null;
        }
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
        if (shadowGenerator != null) {
            shadowGenerator.setOffsetX(offsetX);
        }
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
        if (shadowGenerator != null) {
            shadowGenerator.setOffsetY(offsetY);
        }
    }

    public float getShadowAlpha() {
        return shadowAlpha;
    }

    public void setShadowAlpha(float shadowAlpha) {
        this.shadowAlpha = shadowAlpha;
        if (shadowGenerator != null) {
            shadowGenerator.setShadowAlpha(shadowAlpha);
        }
    }

    public void setShouldAnimateShadow(boolean shouldAnimateShadow) {
        this.shouldAnimateShadow = shouldAnimateShadow;
        if (shadowGenerator != null) {
            shadowGenerator.setShouldAnimateShadow(shouldAnimateShadow);
        }
    }

    public boolean shouldAnimateShadow() {
        return shouldAnimateShadow;
    }

    public void setShouldCalculateAsync(boolean shouldCalculateAsync) {
        this.shouldCalculateAsync = shouldCalculateAsync;
        if (shadowGenerator != null) {
            shadowGenerator.setShouldCalculateAsync(shouldCalculateAsync);
        }
    }

    public boolean shouldCalculateAsync() {
        return shouldCalculateAsync;
    }

    public void setShowShadowsWhenAllReady(boolean shouldWaitForAllReady) {
        this.shouldShowWhenAllReady = shouldWaitForAllReady;
        if (shadowGenerator != null) {
            shadowGenerator.setShouldShowWhenAllReady(shouldWaitForAllReady);
        }
    }

    public boolean shouldWaitForAllReady() {
        return shouldShowWhenAllReady;
    }

    public int getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
        if (shadowGenerator != null) {
            shadowGenerator.setAnimationDuration(animationDuration);
        }
    }

}
