package org.goodev.droidddle.frag.shot;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.goodev.droidddle.R;
import org.goodev.droidddle.frag.StatFragment;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShotTagFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ShotTagFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShotTagFragment extends StatFragment {
    private static final String ARG_ID = "extra_shot_id";
    private static final String ARG_TAG = "extra_tag";

    private long mId;
    private ArrayList<String> mTags;

    private OnFragmentInteractionListener mListener;

    public ShotTagFragment() {
        // Required empty public constructor
    }

    public static ShotTagFragment newInstance(long param1, ArrayList<String> tags) {
        ShotTagFragment fragment = new ShotTagFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ID, param1);
        args.putStringArrayList(ARG_TAG, tags);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mId = getArguments().getLong(ARG_ID);
            mTags = getArguments().getStringArrayList(ARG_TAG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shot_tag, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
