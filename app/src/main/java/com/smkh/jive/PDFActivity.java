package com.smkh.jive;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.File;
import java.io.FileNotFoundException;


/**
 * Created by Kumail on 31-Jan-18.
 */

public class PDFActivity extends AppCompatActivity implements RetainedFragment.TaskCallbacks {
    FitPolicy fitPolicy = FitPolicy.HEIGHT;
    TextView textView;
    PDFView pdfView;
    String pdfName;
    int totalSize;
    String downloadStatus = "NotStarted"; //NotStarted, Downloading, Downloaded, ErrorDownloading, ErrorOpening

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.activity_fadein, R.anim.activity_fadeout);
        setContentView(R.layout.activity_pdf);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            findViewById(R.id.pdf_activity_parent).setBackgroundResource(R.mipmap.home_background_portrait);
        } else {
            findViewById(R.id.pdf_activity_parent).setBackgroundResource(R.mipmap.home_background_landscape);
        }
        Intent intent = getIntent();
        pdfName = intent.getStringExtra("pdfname");
        totalSize = intent.getIntExtra("pdfsize", -1);
        File file = new File(getFilesDir(), pdfName);
        textView = (TextView) findViewById(R.id.pdf_activity_textview);
        pdfView = (PDFView) findViewById(R.id.pdf_view);
        RetainedFragment fragment = (RetainedFragment) getFragmentManager().findFragmentByTag("RETAINED_FRAGMENT");
        if (fragment == null) {
            if (!file.exists()) {
                RetainedFragment mTaskFragment = RetainedFragment.newInstance(pdfName, totalSize);
                getFragmentManager().beginTransaction().add(mTaskFragment, "RETAINED_FRAGMENT").commit();
                textView.setOnClickListener(null);
                textView.setText("Downloading");
                downloadStatus = "Downloading";
            } else {
                pdfView = (PDFView) findViewById(R.id.pdf_view);
                pdfView.fromFile(new File(getFilesDir(), pdfName))
                        .pageFitPolicy(fitPolicy)
                        .enableSwipe(true)
                        .onError(new OnErrorListener() {
                            @Override
                            public void onError(Throwable t) {
                                RetainedFragment mTaskFragment = RetainedFragment.newInstance(pdfName, totalSize);
                                getFragmentManager().beginTransaction().add(mTaskFragment, "RETAINED_FRAGMENT").commit();
                                textView.setOnClickListener(null);
                                textView.setText("Downloading");
                                downloadStatus = "Downloading";
                                pdfView.setVisibility(View.GONE);
                            }
                        })
                        .swipeHorizontal(true)
                        .load();
                Log.d("JIVEDEBUG", "Loaded");
                pdfView.setVisibility(View.VISIBLE);
            }
        } else {
            if (savedInstanceState != null) {
                switch (savedInstanceState.getString("downloadStatus", "NotStarted")) {
                    case "NotStarted":
                        downloadStatus = "NotStarted";
                        RetainedFragment mTaskFragment = RetainedFragment.newInstance(pdfName, totalSize);
                        getFragmentManager().beginTransaction().add(mTaskFragment, "RETAINED_FRAGMENT").commit();
                        textView.setOnClickListener(null);
                        textView.setText("Downloading");
                        downloadStatus = "Downloading";
                        pdfView.setVisibility(View.GONE);
                        break;
                    case "Downloading":
                        downloadStatus = "Downloading";
                        break;
                    case "Downloaded":
                        downloadStatus = "Downloaded";
                        textView.setOnClickListener(null);
                        pdfView.fromFile(new File(getFilesDir(), pdfName))
                                .pageFitPolicy(fitPolicy)
                                .enableSwipe(true)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        textView.setText("There was an error opening the magazine.\nTap here to try again");
                                        textView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                RetainedFragment mTaskFragment = RetainedFragment.newInstance(pdfName, totalSize);
                                                getFragmentManager().beginTransaction().add(mTaskFragment, "RETAINED_FRAGMENT").commit();
                                                textView.setOnClickListener(null);
                                                textView.setText("Downloading");
                                                downloadStatus = "Downloading";
                                                pdfView.setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                })
                                .swipeHorizontal(true)
                                .load();
                        pdfView.setVisibility(View.VISIBLE);
                        break;
                    case "ErrorDownloading":
                        downloadStatus = "ErrorDownloading";
                        textView.setText("There was an error downloading the magazine due to a network issue. Tap here to try again");
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                RetainedFragment mTaskFragment = RetainedFragment.newInstance(pdfName, totalSize);
                                getFragmentManager().beginTransaction().add(mTaskFragment, "RETAINED_FRAGMENT").commit();
                                textView.setOnClickListener(null);
                                textView.setText("Downloading");
                                downloadStatus = "Downloading";
                                pdfView.setVisibility(View.GONE);
                            }
                        });
                        pdfView.setVisibility(View.GONE);
                        break;
                    case "ErrorOpening":
                        downloadStatus = "ErrorOpening";
                        textView.setText("There was an error opening the magazine.\nTap here to try again");
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                RetainedFragment mTaskFragment = RetainedFragment.newInstance(pdfName, totalSize);
                                getFragmentManager().beginTransaction().add(mTaskFragment, "RETAINED_FRAGMENT").commit();
                                textView.setOnClickListener(null);
                                textView.setText("Downloading");
                                downloadStatus = "Downloading";
                                pdfView.setVisibility(View.GONE);
                            }
                        });
                        pdfView.setVisibility(View.GONE);
                        break;
                }
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("downloadStatus", downloadStatus);
    }

    @Override
    public void onProgressUpdate(Object[] values) {
        if (textView != null) {
            textView.setText("Downloading " + (values[0]) + "%");
        }
    }

    @Override
    public void onPostExecute(Object value) {
        if (value != null) {
            downloadStatus = "ErrorDownloading";
            textView.setText("There was an error downloading the magazine due to a network issue. Tap here to try again");
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RetainedFragment mTaskFragment = RetainedFragment.newInstance(pdfName, totalSize);
                    getFragmentManager().beginTransaction().add(mTaskFragment, "RETAINED_FRAGMENT").commit();
                    textView.setOnClickListener(null);
                    textView.setText("Downloading");
                    downloadStatus = "Downloading";
                    pdfView.setVisibility(View.GONE);
                }
            });
            pdfView.setVisibility(View.GONE);
        } else {
            final PDFView pdfView = (PDFView) findViewById(R.id.pdf_view);
            pdfView.fromFile(new File(getFilesDir(), pdfName))
                    .pageFitPolicy(fitPolicy)
                    .enableSwipe(true)
                    .onError(new OnErrorListener() {
                        @Override
                        public void onError(Throwable t) {
                            textView.setText("There was an error opening the magazine.\nTap here to try again");
                            textView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    RetainedFragment mTaskFragment = RetainedFragment.newInstance(pdfName, totalSize);
                                    getFragmentManager().beginTransaction().add(mTaskFragment, "RETAINED_FRAGMENT").commit();
                                    textView.setOnClickListener(null);
                                    textView.setText("Downloading");
                                    downloadStatus = "Downloading";
                                    pdfView.setVisibility(View.GONE);
                                }
                            });
                            pdfView.setVisibility(View.GONE);
                            downloadStatus = "ErrorOpening";
                        }
                    })
                    .swipeHorizontal(true)
                    .load();
            Log.d("JIVEDEBUG", "Loaded");
            downloadStatus = "Downloaded";
            pdfView.setVisibility(View.VISIBLE);
        }

    }
}
