package com.world.myapplication;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputLayout;


public class SearchFragment extends Fragment {

    View rootView;
    String text;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.search_fragment, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        //search button
        Button mButton = (Button)rootView.findViewById(R.id.button);
        text = "";
        mButton.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        //get users input
                        TextInputLayout textInputLayout = rootView.findViewById(R.id.textField);
                        text = textInputLayout.getEditText().getText().toString();
                        //new fragment
                        //search for artist's songs
                        Bundle bundle = new Bundle();
                        bundle.putString("artist", text);
                        Navigation.findNavController(view).navigate(R.id.search_to_result, bundle);
                    }
                });
    }

}