package com.smkh.jive;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Kumail on 04-Feb-18.
 */

public class RetainedFragment extends Fragment {
    interface TaskCallbacks {
        void onProgressUpdate(Object[] percent);
        void onPostExecute(Object value);
    }
    TaskCallbacks mCallbacks;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (TaskCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public static RetainedFragment newInstance(String pdfName, int totalSize) {
        RetainedFragment f = new RetainedFragment();
        Bundle args = new Bundle();
        args.putString("pdfname", pdfName);
        args.putInt("totalsize", totalSize);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle b = getArguments();
        if (b != null) {
            final String pdfName = b.getString("pdfname", "");
            final int totalSize = b.getInt("totalsize", -1);

            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] objects) {
                    try {
                        SharedPreferences preferences = getActivity().getSharedPreferences("SharedPreferences", MODE_PRIVATE);
                        String path = preferences.getString("serverpath", "http://jtinnoventions17.com/jive/appserver/");
                        URL url = new URL(path + "pdf/" + pdfName);
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setDoOutput(true);
                        urlConnection.connect();
                        FileOutputStream fileOutput = getActivity().openFileOutput(pdfName, MODE_PRIVATE);
                        InputStream inputStream = urlConnection.getInputStream();
                        byte[] buffer = new byte[1024];
                        int bufferLength = 0;
                        int count = 1;
                        int totalbytes = 0;
                        while ((bufferLength = inputStream.read(buffer)) > 0) {
                            fileOutput.write(buffer, 0, bufferLength);
                            totalbytes += bufferLength;
                            if (count % 20 == 0) {
                                DecimalFormat twoDForm = new DecimalFormat("#.##");
                                publishProgress(Double.valueOf(twoDForm.format((double) totalbytes * 100 / totalSize)));
                            }
                            count++;
                        }
                        Log.d("JIVEDEBUG", Integer.toString(totalbytes));
                        fileOutput.close();

                    } catch (MalformedURLException e) {
                        Log.d("JIVEDEBUG", "Malformed");
                        e.printStackTrace();
                        return "error";
                    } catch (IOException e) {
                        Log.d("JIVEDEBUG", "IOException");
                        return "error";
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(Object[] values) {
                    if (mCallbacks != null) {
                        mCallbacks.onProgressUpdate(values);
                    }
                }

                @Override
                protected void onPostExecute(Object o) {
                    if (mCallbacks != null) {
                        mCallbacks.onPostExecute(o);
                    }

                }
            };
            task.execute();
        }
    }


}
