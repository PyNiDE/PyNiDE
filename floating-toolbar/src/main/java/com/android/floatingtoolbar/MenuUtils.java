package com.android.floatingtoolbar;

import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.ReflectUtils;

class MenuUtils {
    public static final int TEXT_ASSIST_ID = 16908353;

    public static void setDefaultShowAsAction(Menu menu, int flags) {
        try {
            ReflectUtils.reflect(menu).method("setDefaultShowAsAction", flags);
        } catch (Throwable ignore) {

        }
    }

    public static boolean requiresActionButton(MenuItem menuItem) {
        try {
            return ReflectUtils.reflect(menuItem).method("requiresActionButton").get();
        } catch (Throwable ignore) {
            return false;
        }
    }

    public static boolean requiresOverflow(MenuItem menuItem) {
        try {
            return ReflectUtils.reflect(menuItem).method("requiresOverflow").get();
        } catch (Throwable ignore) {
            return false;
        }
    }
}
