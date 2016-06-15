package org.goodev.droidddle.frag;

import android.support.v4.app.Fragment;

/**
 * Created by ADMIN on 2015/2/4.
 */
public class StatFragment extends Fragment {
    @Override
    public void onResume() {
        super.onResume();
//        StatService.onPageStart(getActivity(), getClass().getSimpleName());
    }

    @Override
    public void onPause() {
        super.onPause();
//        StatService.onPageEnd(getActivity(), getClass().getSimpleName());
    }
}
