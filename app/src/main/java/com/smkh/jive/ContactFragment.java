package com.smkh.jive;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Kumail on 11-Feb-18.
 */

public class ContactFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v;
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            v = inflater.inflate(R.layout.contact_fragment, container, false);
        } else {
            v = inflater.inflate(R.layout.landscape_contact_fragment, container, false);
        }

        v.findViewById(R.id.contact_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentMaps = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goo.gl/maps/5rQWBwfnDbD2"));
                startActivity(intentMaps);
            }
        });
        v.findViewById(R.id.contact_mail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "editors.jt@gmail.com" });
                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });
        return v;
    }


}

