package com.smkh.jive;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kumail on 05-Feb-18.
 */

public class FeedbackRetainedFragment extends Fragment {
    interface FeedbackRetainedCallbacks {
        void onResponse(String response);
        void onErrorResponse(VolleyError error);
    }
    FeedbackRetainedCallbacks mCallbacks;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle b = getArguments();
        int id = b.getInt("id", 0);
        if (activity.getFragmentManager().findFragmentById(id) instanceof  FeedbackFragment) {
            mCallbacks = (FeedbackRetainedCallbacks) activity.getFragmentManager().findFragmentById(id);
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public static FeedbackRetainedFragment newInstance(String name, String email, String feedback, int id) {
        FeedbackRetainedFragment f = new FeedbackRetainedFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("email", email);
        args.putString("feedback", feedback);
        args.putInt("id", id);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle b= getArguments();
        if (b != null) {
            Map<String, String> params = new HashMap<>();
            params.put("name", b.getString("name", ""));
            params.put("email", b.getString("email", ""));
            params.put("feedback", b.getString("feedback", ""));
            RequestClass.stringRequest(getActivity(), "feedback.php", Request.Method.POST, params, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (mCallbacks != null) {
                        mCallbacks.onResponse(response);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (mCallbacks != null) {
                        mCallbacks.onErrorResponse(error);
                    }
                }
            });
        }

    }
}
