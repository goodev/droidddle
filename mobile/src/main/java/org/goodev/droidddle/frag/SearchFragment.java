package org.goodev.droidddle.frag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.goodev.droidddle.R;

import butterknife.ButterKnife;

/**
 * Created by goodev on 2015/1/8.
 */
public class SearchFragment extends SearchBaseFragment {

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        ButterKnife.inject(this, view);

        return view;
    }
}
