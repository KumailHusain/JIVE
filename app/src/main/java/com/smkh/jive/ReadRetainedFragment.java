package com.smkh.jive;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Kumail on 05-Feb-18.
 */

public class ReadRetainedFragment extends Fragment {
    interface pdflistCallbacks {
        void onResponse(String response);

        void onErrorResponse(Exception e);
    }

    int totalCount = 0;
    int finalCount = 0;
    pdflistCallbacks mCallbacks;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle b = getArguments();
        int id = b.getInt("id", 0);
        if (getFragmentManager().findFragmentById(id) instanceof ReadFragment) {
            mCallbacks = (pdflistCallbacks) getFragmentManager().findFragmentById(id);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public static ReadRetainedFragment newInstance(int mode, int id) {
        //mode = 0: Full, mode = 1: partial
        ReadRetainedFragment f = new ReadRetainedFragment();
        Bundle args = new Bundle();
        args.putInt("mode", mode);
        args.putInt("id", id);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle b = getArguments();
        int mode = b.getInt("mode", 0);
        if (mode == 0) {
            RequestClass.stringRequest(getActivity(), "getpdflist.php", Request.Method.GET, null, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        final JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("success").equals("1")) {
                            if (jsonResponse.has("dataset")) {
                                final JSONArray array = new JSONArray(jsonResponse.getString("dataset"));
                                SharedPreferences preferences = getActivity().getSharedPreferences("SharedPreferences", MODE_PRIVATE);
                                final String serverpath = preferences.getString("serverpath", "http://jtinnoventions17.com/jive/appserver/");
                                final SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("pdflistarray", array.toString());
                                editor.apply();
                                for (int i = 0; i <= array.length() - 1; i++) {
                                    final JSONObject row = new JSONObject(array.get(i).toString());
                                    AsyncTask task = new AsyncTask() {
                                        @Override
                                        protected Object doInBackground(Object[] objects) {
                                            Bitmap b;
                                            try {
                                                String strURL = serverpath + "images/" + row.getString("graphicname");
                                                java.net.URL url = new java.net.URL(strURL);
                                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                                connection.setDoInput(true);
                                                connection.connect();
                                                InputStream input = connection.getInputStream();
                                                b = BitmapFactory.decodeStream(input);
                                                if (b != null) {
                                                    try {
                                                        FileOutputStream os = getActivity().openFileOutput(row.getString("graphicname"), Context.MODE_PRIVATE);
                                                        b.compress(Bitmap.CompressFormat.PNG, 100, os);
                                                        os.close();
                                                    } catch (FileNotFoundException e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    File file = new File(getActivity().getFilesDir(), row.getString("graphicname"));
                                                    if (file.exists()) {
                                                        file.delete();
                                                    }
                                                }
                                            } catch (IOException e) {
                                                try {
                                                    File file = null;
                                                    file = new File(getActivity().getFilesDir(), row.getString("graphicname"));
                                                    if (file.exists()) {
                                                        file.delete();
                                                    }
                                                } catch (JSONException e1) {
                                                    e1.printStackTrace();
                                                }
                                                e.printStackTrace();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Object o) {
                                            totalCount++;
                                        }
                                    };
                                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                                }
                                AsyncTask task = new AsyncTask() {
                                    @Override
                                    protected Object doInBackground(Object[] objects) {
                                        while (array.length() != totalCount) ;
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Object o) {
                                        sendResponse("full");
                                        editor.putBoolean("isfirstrun", false);
                                        editor.apply();
                                    }
                                };
                                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } else {
                                sendResponse("empty");
                            }
                        } else {
                            sendErrorResponse(new Exception("Server error"));
                        }
                    } catch (JSONException e) {
                        sendErrorResponse(e);
                        e.printStackTrace();
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
        } else {
            SharedPreferences preferences = getActivity().getSharedPreferences("SharedPreferences", MODE_PRIVATE);
            final String serverpath = preferences.getString("serverpath", "http://jtinnoventions17.com/jive/appserver/");
            try {
                final JSONArray array = new JSONArray(preferences.getString("pdflistarray", null));
                for (int i = 0; i <= array.length() - 1; i++) {
                    final JSONObject row = new JSONObject(array.get(i).toString());
                    File file = new File(getActivity().getFilesDir(), row.getString("graphicname"));
                    if (!file.exists()) {
                        AsyncTask task = new AsyncTask() {
                            @Override
                            protected Object doInBackground(Object[] objects) {
                                Bitmap b;
                                try {
                                    String strURL = serverpath + "images/" + row.getString("graphicname");
                                    java.net.URL url = new java.net.URL(strURL);
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setDoInput(true);
                                    connection.connect();
                                    InputStream input = connection.getInputStream();
                                    b = BitmapFactory.decodeStream(input);
                                    if (b != null) {
                                        try {
                                            FileOutputStream os = getActivity().openFileOutput(row.getString("graphicname"), Context.MODE_PRIVATE);
                                            b.compress(Bitmap.CompressFormat.PNG, 100, os);
                                            os.close();
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        File file = null;
                                        file = new File(getActivity().getFilesDir(), row.getString("graphicname"));
                                        if (file.exists()) {
                                            file.delete();
                                        }
                                    }
                                } catch (IOException e) {
                                    try {
                                        File file = null;
                                        file = new File(getActivity().getFilesDir(), row.getString("graphicname"));
                                        if (file.exists()) {
                                            file.delete();
                                        }
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Object o) {
                                totalCount++;
                            }
                        };
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        finalCount++;
                    } else {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.toString());
                        if (bitmap == null) {
                            file.delete();
                            AsyncTask task = new AsyncTask() {
                                @Override
                                protected Object doInBackground(Object[] objects) {
                                    Bitmap b;
                                    try {
                                        String strURL = serverpath + "images/" + row.getString("graphicname");
                                        java.net.URL url = new java.net.URL(strURL);
                                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                        connection.setDoInput(true);
                                        connection.connect();
                                        InputStream input = connection.getInputStream();
                                        b = BitmapFactory.decodeStream(input);
                                        if (b != null) {
                                            try {
                                                FileOutputStream os = getActivity().openFileOutput(row.getString("graphicname"), Context.MODE_PRIVATE);
                                                b.compress(Bitmap.CompressFormat.PNG, 100, os);
                                                os.close();
                                            } catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            File file = null;
                                            file = new File(getActivity().getFilesDir(), row.getString("graphicname"));
                                            if (file.exists()) {
                                                file.delete();
                                            }
                                        }
                                    } catch (IOException e) {
                                        try {
                                            File file = null;
                                            file = new File(getActivity().getFilesDir(), row.getString("graphicname"));
                                            if (file.exists()) {
                                                file.delete();
                                            }
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Object o) {
                                    totalCount++;
                                }
                            };
                            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            finalCount++;
                        }
                    }
                }
                //Wait
                AsyncTask task = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        while (totalCount != finalCount) ;
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        mCallbacks.onResponse("partial");
                    }
                };
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (JSONException e) {
                mCallbacks.onErrorResponse(e);
                e.printStackTrace();
            }
        }
    }

    private void sendErrorResponse(Exception e) {
        if (mCallbacks != null) {
            mCallbacks.onErrorResponse(e);
        }
    }

    private void sendResponse(String response) {
        if (mCallbacks != null) {
            mCallbacks.onResponse(response);
        }
    }


}
