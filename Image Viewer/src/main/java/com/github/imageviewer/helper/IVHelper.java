package com.github.imageviewer.helper;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.TypedValue;

/**
 * Created by AT-Praveen on 30/01/18.
 */

public class IVHelper {
    private static final IVHelper INSTANCE = new IVHelper();


    private IVHelper() {
    }

    public static IVHelper getInstance() {
        return INSTANCE;
    }

    public boolean isNetworkAvailable(Context context) {


        if (context == null) return false;

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    /**
     * Converting dp to pixel
     */
    public int dpToPx(Context context, int dp) {
        if (context == null) {return 0;}
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
