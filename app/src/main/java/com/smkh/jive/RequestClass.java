package com.smkh.jive;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Kumail on 30-Jan-18.
 */

public class RequestClass {
    private static String URL;
    private static RequestQueue requestQueue;

    static void stringRequest(Context context, String api, int method, final Map<String, String> parameters, Response.Listener<String> success, Response.ErrorListener error) {
        requestQueue = Volley.newRequestQueue(context);
        URL = context.getSharedPreferences("SharedPreferences", MODE_PRIVATE).getString("serverpath", "http://jtinnoventions17.com/jive/appserver/");
        StringRequest request = new StringRequest(method, URL + api, success, error) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if (parameters != null) {
                    return parameters;
                } else {
                    return super.getParams();
                }
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    static void hostUpdateRequest(Context context, String URL, Response.Listener<String> success) {
        requestQueue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.GET, URL, success, null);
        request.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }
}
