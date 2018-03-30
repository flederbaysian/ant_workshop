package com.toastandtesla.antmaps;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * A class which provides a single Volley RequestQueue for the app, as recommended in
 * https://developer.android.com/training/volley/requestqueue.html
 */
public final class RequestQueueSingleton {
  private static RequestQueue instance;

  synchronized public static RequestQueue getInstance(Context context) {
    if (instance == null) {
      instance = Volley.newRequestQueue(context.getApplicationContext());
    }
    return instance;
  }
}
