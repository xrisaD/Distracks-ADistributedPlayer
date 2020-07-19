package com.world.myapplication;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class AboutUsPage extends Fragment {
    private View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.about_us_page, container, false);
        TextView dikonimaki = (TextView) rootView.findViewById(R.id.dikonimaki);
        dikonimaki.setMovementMethod(LinkMovementMethod.getInstance());

        TextView konstantopolous = (TextView) rootView.findViewById(R.id.konstantopolous);
        konstantopolous.setMovementMethod(LinkMovementMethod.getInstance());

        TextView lapakis = (TextView) rootView.findViewById(R.id.lapakis);
        lapakis.setMovementMethod(LinkMovementMethod.getInstance());

        TextView smyrnioudis = (TextView) rootView.findViewById(R.id.smyrnioudis);
        smyrnioudis.setMovementMethod(LinkMovementMethod.getInstance());

        return rootView;
    }
}
