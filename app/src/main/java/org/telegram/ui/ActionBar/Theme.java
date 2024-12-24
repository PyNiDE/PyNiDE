package org.telegram.ui.ActionBar;

import static com.blankj.utilcode.util.SizeUtils.dp2px;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.StateSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pynide.utils.FileLog;

public class Theme {
    private static final RectF rectTmp = new RectF();
    private static final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public static final int RIPPLE_MASK_CIRCLE_20DP = 1;
    public static final int RIPPLE_MASK_ALL = 2;
    public static final int RIPPLE_MASK_CIRCLE_TO_BOUND_EDGE = 3;
    public static final int RIPPLE_MASK_CIRCLE_TO_BOUND_CORNER = 4;
    public static final int RIPPLE_MASK_CIRCLE_AUTO = 5;
    public static final int RIPPLE_MASK_ROUNDRECT_6DP = 7;

    public static Drawable createSelectorDrawable(int color, int maskType, int radius) {
        Drawable maskDrawable = null;
        if (maskType == RIPPLE_MASK_CIRCLE_20DP ||
                maskType == RIPPLE_MASK_CIRCLE_TO_BOUND_EDGE ||
                maskType == RIPPLE_MASK_CIRCLE_TO_BOUND_CORNER ||
                maskType == RIPPLE_MASK_CIRCLE_AUTO ||
                maskType == 6 ||
                maskType == RIPPLE_MASK_ROUNDRECT_6DP
        ) {
            maskPaint.setColor(0xffffffff);
            maskDrawable = new Drawable() {
                RectF rect;

                @Override
                public void draw(@NonNull Canvas canvas) {
                    android.graphics.Rect bounds = getBounds();
                    if (maskType == RIPPLE_MASK_ROUNDRECT_6DP) {
                        if (rect == null) {
                            rect = new RectF();
                        }
                        rect.set(bounds);
                        float rad = radius <= 0 ? dp2px(6) : radius;
                        canvas.drawRoundRect(rect, rad, rad, maskPaint);
                    } else {
                        int rad;
                        if (maskType == RIPPLE_MASK_CIRCLE_20DP || maskType == 6) {
                            rad = radius <= 0 ? dp2px(20) : radius;
                        } else if (maskType == RIPPLE_MASK_CIRCLE_TO_BOUND_EDGE) {
                            rad = (Math.max(bounds.width(), bounds.height()) / 2);
                        } else {
                            // RIPPLE_MASK_CIRCLE_AUTO = 5
                            // RIPPLE_MASK_CIRCLE_TO_BOUND_CORNER = 4
                            rad = (int) Math.ceil(Math.sqrt((bounds.left - bounds.centerX()) * (bounds.left - bounds.centerX()) + (bounds.top - bounds.centerY()) * (bounds.top - bounds.centerY())));
                        }
                        canvas.drawCircle(bounds.centerX(), bounds.centerY(), rad, maskPaint);
                    }
                }

                @Override
                public void setAlpha(int alpha) {

                }

                @Override
                public void setColorFilter(ColorFilter colorFilter) {

                }

                @Override
                public int getOpacity() {
                    return PixelFormat.UNKNOWN;
                }
            };
        } else if (maskType == RIPPLE_MASK_ALL) {
            maskDrawable = new ColorDrawable(0xffffffff);
        }

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{StateSet.WILD_CARD},
                new int[]{color}
        );
        RippleDrawable rippleDrawable = new RippleDrawableSafe(colorStateList, null, maskDrawable);
        if (maskType == RIPPLE_MASK_CIRCLE_20DP) {
            rippleDrawable.setRadius(radius <= 0 ? dp2px(20) : radius);
        } else if (maskType == RIPPLE_MASK_CIRCLE_AUTO) {
            rippleDrawable.setRadius(RippleDrawable.RADIUS_AUTO);
        }
        return rippleDrawable;
    }

    public static Drawable createSelectorDrawable(int color, int maskType) {
        return createSelectorDrawable(color, maskType, -1);
    }

    public static Drawable getSelectorDrawable(int color) {
        return createSelectorDrawable(color, 2);
    }

    public static Drawable createRadSelectorDrawable(int color, int topLeftRad, int topRightRad, int bottomRightRad, int bottomLeftRad) {
        maskPaint.setColor(0xffffffff);
        Drawable maskDrawable = new RippleRadMaskDrawable(topLeftRad, topRightRad, bottomRightRad, bottomLeftRad);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{StateSet.WILD_CARD},
                new int[]{color}
        );
        return new RippleDrawableSafe(colorStateList, null, maskDrawable);
    }

    public static class RippleDrawableSafe extends RippleDrawable {
        public RippleDrawableSafe(@NonNull ColorStateList color, @Nullable Drawable content, @Nullable Drawable mask) {
            super(color, content, mask);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            try {
                super.draw(canvas);
            } catch (Exception e) {
                FileLog.e("probably forgot to put setCallback", e);
            }
        }
    }

    public static class RippleRadMaskDrawable extends Drawable {
        private final Path path = new Path();
        private final float[] radii = new float[8];
        boolean invalidatePath = true;

        public RippleRadMaskDrawable(float top, float bottom) {
            radii[0] = radii[1] = radii[2] = radii[3] = dp2px(top);
            radii[4] = radii[5] = radii[6] = radii[7] = dp2px(bottom);
        }

        public RippleRadMaskDrawable(float topLeft, float topRight, float bottomRight, float bottomLeft) {
            radii[0] = radii[1] = dp2px(topLeft);
            radii[2] = radii[3] = dp2px(topRight);
            radii[4] = radii[5] = dp2px(bottomRight);
            radii[6] = radii[7] = dp2px(bottomLeft);
        }

        public void setRadius(float top, float bottom) {
            radii[0] = radii[1] = radii[2] = radii[3] = dp2px(top);
            radii[4] = radii[5] = radii[6] = radii[7] = dp2px(bottom);
            invalidatePath = true;
            invalidateSelf();
        }

        public void setRadius(float topLeft, float topRight, float bottomRight, float bottomLeft) {
            radii[0] = radii[1] = dp2px(topLeft);
            radii[2] = radii[3] = dp2px(topRight);
            radii[4] = radii[5] = dp2px(bottomRight);
            radii[6] = radii[7] = dp2px(bottomLeft);
            invalidatePath = true;
            invalidateSelf();
        }

        @Override
        protected void onBoundsChange(@NonNull Rect bounds) {
            invalidatePath = true;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            if (invalidatePath) {
                invalidatePath = false;
                path.reset();
                rectTmp.set(getBounds());
                path.addRoundRect(rectTmp, radii, Path.Direction.CW);
            }
            canvas.drawPath(path, maskPaint);
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.UNKNOWN;
        }
    }
}
