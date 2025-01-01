package com.android.floatingtoolbar;

import android.content.pm.ApplicationInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.blankj.utilcode.util.ReflectUtils;

class ViewUtils {
    /**
     * @noinspection UnusedReturnValue
     */
    public static boolean getChildVisibleRect(Object parent,
                                              View child, Rect rect, Point offset, boolean forceParentCheck) {
        try {
            return ReflectUtils.reflect(parent).method("getChildVisibleRect", child, rect, offset, forceParentCheck).get();
        } catch (Throwable ignore) {
            if (parent instanceof ViewGroup) {
                return ((ViewGroup) parent).getChildVisibleRect(child, rect, offset);
            }
        }
        return false;
    }

    public static boolean hasRtlSupport(ApplicationInfo applicationInfo) {
        try {
            return ReflectUtils.reflect(applicationInfo).method("hasRtlSupport").get();
        } catch (Throwable ignore) {
            return false;
        }
    }

    public static int getWindowTypeAboveSubPanel() {
        try {
            return ReflectUtils.reflect(WindowManager.LayoutParams.class).field("TYPE_APPLICATION_ABOVE_SUB_PANEL").get();
        } catch (Throwable ignore) {
            return 0;
        }
    }
}
