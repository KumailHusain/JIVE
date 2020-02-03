package com.smkh.jive;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.text.DecimalFormat;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Kumail on 25-Jan-18.
 */

public class ReadFragment extends Fragment implements  ReadRetainedFragment.pdflistCallbacks {
    String state = "Idle"; //Idle, Downloading, Loaded, Error
    Dialog dialog;
    View v;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.read_fragment, container, false);
        this.v = v;
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ReadRetainedFragment fragment = (ReadRetainedFragment) getFragmentManager().findFragmentByTag("READ_RETAINED_FRAGMENT");
        final SharedPreferences preferences = getActivity().getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        Boolean isUpdateRequired = preferences.getBoolean("isfirstrun", true);
        if (fragment == null) {
            dialog = new Dialog(getActivity());
            dialog.setContentView(R.layout.read_loading_dialog);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            if (isUpdateRequired) {
                state = "Downloading";
                ReadRetainedFragment retainedFragment = ReadRetainedFragment.newInstance(0, getId());
                getFragmentManager().beginTransaction().add(retainedFragment, "READ_RETAINED_FRAGMENT").commit();
            } else {
                state = "Downloading";
                ReadRetainedFragment retainedFragment = ReadRetainedFragment.newInstance(1, getId());
                getFragmentManager().beginTransaction().add(retainedFragment, "READ_RETAINED_FRAGMENT").commit();
            }
        } else {
            if (savedInstanceState != null) {
                state = savedInstanceState.getString("state", "Idle");
                if (state.equals("Downloading")) {
                    dialog = new Dialog(getActivity());
                    dialog.setContentView(R.layout.read_loading_dialog);
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                } else if (state.equals("Loaded")) {
                    populateViews();
                } else if (state.equals("Error")) {
                    v.findViewById(R.id.read_scrollview).setVisibility(View.GONE);
                    TextView textView = (TextView) v.findViewById(R.id.read_error_textview);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(R.string.read_error);
                }
            } else {
                populateViews();
            }
        }

        /*
        if (isUpdateRequired) {
            final Dialog dialog = new Dialog(getActivity());
            dialog.setContentView(R.layout.read_loading_dialog);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            RequestClass.stringRequest(getActivity(), "getpdflist.php", Request.Method.GET, null, new Response.Listener<String>() {
                String str;

                @Override
                public void onResponse(String response) {
                    try {
                        final JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("success").equals("1")) {
                            if (jsonResponse.has("dataset")) {
                                final JSONArray array = new JSONArray(jsonResponse.getString("dataset"));
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("pdflistarray", array.toString());
                                editor.apply();
                                ViewGroup parent = (ViewGroup) v.findViewById(R.id.read_container);
                                View pdfHeader = null;
                                String lastType = "";
                                for (int i = 0; i <= array.length() - 1; i++) {
                                    try {
                                        final JSONObject row = new JSONObject(array.get(i).toString());
                                        if (!lastType.equals(row.getString("type"))) {
                                            lastType = row.getString("type");
                                            pdfHeader = LayoutInflater.from(getActivity()).inflate(R.layout.read_pdf_header, parent, false);
                                            ((TextView) pdfHeader.findViewById(R.id.read_pdf_header_text)).setText(lastType);
                                            parent.addView(pdfHeader);
                                        }
                                        final View pdf = LayoutInflater.from(getActivity()).inflate(R.layout.pdf_holder, (ViewGroup) pdfHeader, false);
                                        ((TextView) pdf.findViewById(R.id.pdf_holder_text)).setText(row.getString("previewtext"));
                                        if (row.getString("pdfhighname").equals("none") && row.getString("pdflowname").equals("none")) {
                                            pdf.setOnClickListener(new View.OnClickListener() {
                                                String str;

                                                @Override
                                                public void onClick(View view) {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    builder.setMessage("This magazine is not available yet")
                                                            .setPositiveButton("OK", null)
                                                            .create()
                                                            .show();
                                                }
                                            });
                                        } else {
                                            pdf.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_item);
                                                    try {
                                                        if (!row.getString("pdfhighname").equals("none")) {
                                                            adapter.add("High Quality [" + getSizeInString(row.getInt("pdfhighsize")) + "]");
                                                        }
                                                        if (!row.getString("pdflowname").equals("none")) {
                                                            adapter.add("Low Quality [" + getSizeInString(row.getInt("pdflowsize")) + "]");
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    builder.setTitle("Choose A Quality")
                                                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    try {
                                                                        if (adapter.getItem(i).contains("High")) {
                                                                            Intent intent = new Intent(getActivity(), PDFActivity.class);
                                                                            intent.putExtra("pdfname", row.getString("pdfhighname"));
                                                                            intent.putExtra("pdfsize", row.getInt("pdfhighsize"));
                                                                            startActivity(intent);
                                                                        } else if (adapter.getItem(i).contains("Low")) {
                                                                            Intent intent = new Intent(getActivity(), PDFActivity.class);
                                                                            intent.putExtra("pdfname", row.getString("pdflowname"));
                                                                            intent.putExtra("pdfsize", row.getInt("pdflowsize"));
                                                                            startActivity(intent);
                                                                        }
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            })
                                                            .create()
                                                            .show();
                                                }

                                                private String getSizeInString(int pdfhighsize) {
                                                    DecimalFormat twoDForm = new DecimalFormat("#.#");
                                                    if (pdfhighsize < 1024) {
                                                        return (Integer.toString(pdfhighsize) + " Bytes");
                                                    } else if (pdfhighsize < 1048576) {
                                                        return Double.toString(Double.valueOf(twoDForm.format((double) pdfhighsize / 1024))) + " KB";
                                                    } else if (pdfhighsize < 1073741824) {
                                                        return Double.toString(Double.valueOf(twoDForm.format((double) pdfhighsize / 1048576))) + " MB";
                                                    } else {
                                                        return Double.toString(Double.valueOf(twoDForm.format((double) pdfhighsize / 1073741824))) + " GB";
                                                    }
                                                }


                                            });
                                        }
                                        AsyncTask task = new AsyncTask() {
                                            @Override
                                            protected Object doInBackground(Object[] objects) {
                                                Bitmap b;
                                                try {
                                                    String strURL = "http://jtjive.com/appserver/images/" + row.getString("graphicname");
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
                                                    }
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                return null;
                                            }

                                            @Override
                                            protected void onPostExecute(Object o) {
                                                try {
                                                    File file = new File(getActivity().getFilesDir(), row.getString("graphicname"));
                                                    if (file.exists()) {
                                                        Bitmap b = BitmapFactory.decodeFile(file.toString());
                                                        if (b != null) {
                                                            ((ImageView) pdf.findViewById(R.id.pdf_holder_image)).setImageBitmap(b);
                                                        }
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        };
                                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                        //AsyncTask
                                        ((ViewGroup) pdfHeader).addView(pdf);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                dialog.dismiss();
                                SharedPreferences.Editor spEditor = preferences.edit();
                                spEditor.putBoolean("isfirstrun", false);
                                spEditor.apply();
                            } else {
                                //Show empty read section
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String str = "";
                }
            });
        } else {
            if (preferences.getString("pdflistarray", null) != null) {
                try {
                    final JSONArray array = new JSONArray(preferences.getString("pdflistarray", null));
                    ViewGroup parent = (ViewGroup) v.findViewById(R.id.read_container);
                    View pdfHeader = null;
                    String lastType = "";
                    for (int i = 0; i <= array.length() - 1; i++) {
                        try {
                            final JSONObject row = new JSONObject(array.get(i).toString());
                            if (!lastType.equals(row.getString("type"))) {
                                lastType = row.getString("type");
                                pdfHeader = LayoutInflater.from(getActivity()).inflate(R.layout.read_pdf_header, parent, false);
                                ((TextView) pdfHeader.findViewById(R.id.read_pdf_header_text)).setText(lastType);
                                parent.addView(pdfHeader);
                            }
                            final View pdf = LayoutInflater.from(getActivity()).inflate(R.layout.pdf_holder, (ViewGroup) pdfHeader, false);
                            ((TextView) pdf.findViewById(R.id.pdf_holder_text)).setText(row.getString("previewtext"));
                            if (row.getString("pdfhighname").equals("none") && row.getString("pdflowname").equals("none")) {
                                pdf.setOnClickListener(new View.OnClickListener() {
                                    String str;

                                    @Override
                                    public void onClick(View view) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage("This magazine is not available yet")
                                                .setPositiveButton("OK", null)
                                                .create()
                                                .show();
                                    }
                                });
                            } else {
                                pdf.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_item);
                                        try {
                                            if (!row.getString("pdfhighname").equals("none")) {
                                                adapter.add("High Quality [" + getSizeInString(row.getInt("pdfhighsize")) + "]");
                                            }
                                            if (!row.getString("pdflowname").equals("none")) {
                                                adapter.add("Low Quality [" + getSizeInString(row.getInt("pdflowsize")) + "]");
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        builder.setTitle("Choose A Quality")
                                                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        try {
                                                            if (adapter.getItem(i).contains("High")) {
                                                                Intent intent = new Intent(getActivity(), PDFActivity.class);
                                                                intent.putExtra("pdfname", row.getString("pdfhighname"));
                                                                intent.putExtra("pdfsize", row.getInt("pdfhighsize"));
                                                                startActivity(intent);
                                                            } else if (adapter.getItem(i).contains("Low")) {
                                                                Intent intent = new Intent(getActivity(), PDFActivity.class);
                                                                intent.putExtra("pdfname", row.getString("pdflowname"));
                                                                intent.putExtra("pdfsize", row.getInt("pdflowsize"));
                                                                startActivity(intent);
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                })
                                                .create()
                                                .show();
                                    }

                                    private String getSizeInString(int pdfhighsize) {
                                        DecimalFormat twoDForm = new DecimalFormat("#.#");
                                        if (pdfhighsize < 1024) {
                                            return (Integer.toString(pdfhighsize) + " Bytes");
                                        } else if (pdfhighsize < 1048576) {
                                            return Double.toString(Double.valueOf(twoDForm.format((double) pdfhighsize / 1024))) + " KB";
                                        } else if (pdfhighsize < 1073741824) {
                                            return Double.toString(Double.valueOf(twoDForm.format((double) pdfhighsize / 1048576))) + " MB";
                                        } else {
                                            return Double.toString(Double.valueOf(twoDForm.format((double) pdfhighsize / 1073741824))) + " GB";
                                        }
                                    }


                                });
                            }
                            File file = new File(getActivity().getFilesDir(), row.getString("graphicname"));
                            if (!file.exists()) {
                                AsyncTask task = new AsyncTask() {
                                    @Override
                                    protected Object doInBackground(Object[] objects) {
                                        Bitmap b;
                                        try {
                                            String strURL = "http://jtjive.com/appserver/images/" + row.getString("graphicname");
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
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Object o) {
                                        try {
                                            File file = new File(getActivity().getFilesDir(), row.getString("graphicname"));
                                            if (file.exists()) {
                                                Bitmap b = BitmapFactory.decodeFile(file.toString());
                                                if (b != null) {
                                                    ((ImageView) pdf.findViewById(R.id.pdf_holder_image)).setImageBitmap(b);
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                };
                                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } else {
                                Bitmap b = BitmapFactory.decodeFile(file.toString());
                                if (b != null) {
                                    ((ImageView) pdf.findViewById(R.id.pdf_holder_image)).setImageBitmap(b);
                                } else {
                                    AsyncTask task = new AsyncTask() {
                                        @Override
                                        protected Object doInBackground(Object[] objects) {
                                            Bitmap b;
                                            try {
                                                String strURL = "http://jtjive.com/appserver/images/" + row.getString("graphicname");
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
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Object o) {
                                            try {
                                                File file = new File(getActivity().getFilesDir(), row.getString("graphicname"));
                                                if (file.exists()) {
                                                    Bitmap b = BitmapFactory.decodeFile(file.toString());
                                                    if (b != null) {
                                                        ((ImageView) pdf.findViewById(R.id.pdf_holder_image)).setImageBitmap(b);
                                                    }
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    };
                                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            }
                            ((ViewGroup) pdfHeader).addView(pdf);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        */
    }

    private void populateViews() {
        SharedPreferences preferences = getActivity().getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        if (preferences.getString("pdflistarray", null) != null) {
            try {
                final JSONArray array = new JSONArray(preferences.getString("pdflistarray", null));
                ViewGroup parent = (ViewGroup) v.findViewById(R.id.read_container);
                View pdfHeader = null;
                String lastType = "";
                for (int i = 0; i <= array.length() - 1; i++) {
                    final JSONObject row = new JSONObject(array.get(i).toString());
                    if (!lastType.equals(row.getString("type"))) {
                        lastType = row.getString("type");
                        pdfHeader = LayoutInflater.from(getActivity()).inflate(R.layout.read_pdf_header, parent, false);
                        ((TextView) pdfHeader.findViewById(R.id.read_pdf_header_text)).setText(lastType);
                        parent.addView(pdfHeader);
                    }
                    final View pdf = LayoutInflater.from(getActivity()).inflate(R.layout.pdf_holder, (ViewGroup) pdfHeader, false);
                    ((TextView) pdf.findViewById(R.id.pdf_holder_text)).setText(row.getString("previewtext"));
                    if (row.getString("pdfhighname").equals("none") && row.getString("pdflowname").equals("none")) {
                        pdf.setOnClickListener(new View.OnClickListener() {
                            String str;

                            @Override
                            public void onClick(View view) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setMessage("This magazine is not available yet")
                                        .setPositiveButton("OK", null)
                                        .create()
                                        .show();
                            }
                        });
                    } else {
                        pdf.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_item);
                                try {
                                    if (!row.getString("pdfhighname").equals("none")) {
                                        adapter.add("High Quality [" + getSizeInString(row.getInt("pdfhighsize")) + "]");
                                    }
                                    if (!row.getString("pdflowname").equals("none")) {
                                        adapter.add("Low Quality [" + getSizeInString(row.getInt("pdflowsize")) + "]");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                builder.setTitle("Choose A Quality")
                                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                try {
                                                    if (adapter.getItem(i).contains("High")) {
                                                        Intent intent = new Intent(getActivity(), PDFActivity.class);
                                                        intent.putExtra("pdfname", row.getString("pdfhighname"));
                                                        intent.putExtra("pdfsize", row.getInt("pdfhighsize"));
                                                        startActivity(intent);
                                                    } else if (adapter.getItem(i).contains("Low")) {
                                                        Intent intent = new Intent(getActivity(), PDFActivity.class);
                                                        intent.putExtra("pdfname", row.getString("pdflowname"));
                                                        intent.putExtra("pdfsize", row.getInt("pdflowsize"));
                                                        startActivity(intent);
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        })
                                        .create()
                                        .show();
                            }

                            private String getSizeInString(int pdfhighsize) {
                                DecimalFormat twoDForm = new DecimalFormat("#.#");
                                if (pdfhighsize < 1024) {
                                    return (Integer.toString(pdfhighsize) + " Bytes");
                                } else if (pdfhighsize < 1048576) {
                                    return Double.toString(Double.valueOf(twoDForm.format((double) pdfhighsize / 1024))) + " KB";
                                } else if (pdfhighsize < 1073741824) {
                                    return Double.toString(Double.valueOf(twoDForm.format((double) pdfhighsize / 1048576))) + " MB";
                                } else {
                                    return Double.toString(Double.valueOf(twoDForm.format((double) pdfhighsize / 1073741824))) + " GB";
                                }
                            }


                        });
                    }
                    File file = new File(getActivity().getFilesDir(), row.getString("graphicname"));
                    if (file.exists()) {
                        Bitmap b = BitmapFactory.decodeFile(file.toString());
                        if (b != null) {
                            ((ImageView) pdf.findViewById(R.id.pdf_holder_image)).setImageBitmap(b);
                        }
                    }
                    ((ViewGroup) pdfHeader).addView(pdf);
                }
            } catch (JSONException e) {
                state = "Error";
                v.findViewById(R.id.read_scrollview).setVisibility(View.GONE);
                TextView textView = (TextView) v.findViewById(R.id.read_error_textview);
                textView.setVisibility(View.VISIBLE);
                textView.setText(R.string.read_error);
                e.printStackTrace();
            }
        } else {
            //Show empty
            v.findViewById(R.id.read_scrollview).setVisibility(View.GONE);
            TextView textView = (TextView) v.findViewById(R.id.read_error_textview);
            textView.setVisibility(View.VISIBLE);
            textView.setText("There are no magazines available yet");

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("state", state);
    }

    @Override
    public void onResponse(String response) {
        state = "Loaded";
        populateViews();
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onErrorResponse(Exception e) {
        state = "Error";
        if (dialog != null) {
            dialog.dismiss();
        }
        v.findViewById(R.id.read_scrollview).setVisibility(View.GONE);
        TextView textView = (TextView) v.findViewById(R.id.read_error_textview);
        textView.setVisibility(View.VISIBLE);
        textView.setText(R.string.read_error);
    }
}
