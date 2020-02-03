package com.smkh.jive;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;

/**
 * Created by Kumail on 05-Feb-18.
 */

public class FeedbackFragment extends Fragment implements FeedbackRetainedFragment.FeedbackRetainedCallbacks{
    View v;
    String state = "Idle"; //Idle, FeedbackLoading
    Dialog dialog;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.feedback_fragment, container, false);
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("state", state);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button button = (Button) v.findViewById(R.id.feedback_submit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                simulateButtonClick();
            }
        });
        EditText edittext_feedback = (EditText) v.findViewById(R.id.feedback_feedback);
        edittext_feedback.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    simulateButtonClick();
                    InputMethodManager imm = (InputMethodManager) textView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
        });
        FeedbackRetainedFragment retainedFragment = (FeedbackRetainedFragment) getFragmentManager().findFragmentByTag("FEEDBACK_RETAINED_FRAGMENT");
        if (retainedFragment != null) {
            if (savedInstanceState != null) {
                String state = savedInstanceState.getString("state", "Idle");
                this.state = state;
                switch (state) {
                    case "Idle":
                        break;
                    case "FeedbackLoading":
                        dialog = new Dialog(getActivity());
                        dialog.setContentView(R.layout.read_loading_dialog);
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                        break;
                }
            }
        }
    }

    @Override
    public void onResponse(String response) {
        dialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Feedback Sent")
                .setMessage("Thank you for submitting a feedback!")
                .setPositiveButton("OK", null)
                .create()
                .show();
        state = "Idle";
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        dialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Feedback Not Sent")
                .setMessage("Your feedback could not be sent")
                .setPositiveButton("OK", null)
                .create()
                .show();
        state = "Idle";
    }

    void simulateButtonClick() {
        final EditText edit_name = (EditText) v.findViewById(R.id.feedback_name);
        final EditText edit_email = (EditText) v.findViewById(R.id.feedback_email);
        final EditText edit_feedback = (EditText) v.findViewById(R.id.feedback_feedback);
        int totalErrors = 0;
        if (edit_name.getText().toString().equals("")) {
            totalErrors++;
            final TextInputLayout parent = (TextInputLayout) v.findViewById(R.id.feedback_name_til);
            if (parent.getError() == null) {
                parent.setError("Please specify a first name");
                edit_name.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        edit_name.removeTextChangedListener(this);
                        parent.setError(null);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
            }
        }
        if (edit_email.getText().toString().equals("")) {
            totalErrors++;
            final TextInputLayout parent = (TextInputLayout) v.findViewById(R.id.feedback_email_til);
            if (parent.getError() == null) {
                parent.setError("Please specify an email address");
                edit_email.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        edit_email.removeTextChangedListener(this);
                        parent.setError(null);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
            }

        }
        if (edit_feedback.getText().toString().equals("")) {
            totalErrors++;
            final TextInputLayout parent = (TextInputLayout) v.findViewById(R.id.feedback_feedback_til);
            if (parent.getError() == null) {
                parent.setError("Please include a feedback");
                edit_feedback.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        edit_feedback.removeTextChangedListener(this);
                        parent.setError(null);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
            }

        }
        if (totalErrors == 0) {
            FeedbackRetainedFragment retainedFragment = FeedbackRetainedFragment.newInstance(edit_name.getText().toString(), edit_email.getText().toString(), edit_feedback.getText().toString(), 0);
            getFragmentManager().beginTransaction().add(retainedFragment, "FEEDBACK_RETAINED_FRAGMENT").commit();
            state = "FeedbackLoading";
            dialog = new Dialog(getActivity());
            dialog.setContentView(R.layout.read_loading_dialog);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

    }

}
