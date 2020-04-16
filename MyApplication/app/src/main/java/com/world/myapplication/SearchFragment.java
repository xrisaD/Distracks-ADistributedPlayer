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
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputLayout;


public class SearchFragment extends Fragment {
    // Declare Variables
    View rootView;
    String text;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.search_fragment, container, false);
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
                        //search for arist's songs
                        SearchResult firstFragment = new SearchResult();
                        Bundle args = new Bundle();
                        args.putString("artist", text);
                        firstFragment.setArguments(args);
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.nav_host_fragment, firstFragment);
                        transaction.addToBackStack(null);
                        //hide keyboard before go to the next fragment
                        if(!text.equals("")) {
                            hideSoftKeyboard(getActivity());
                        }
                        transaction.commit();
                    }
                });
        return rootView;
    }
    //hide keybord
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }
}