package org.goodev.droidddle;

import android.os.Bundle;

import org.goodev.droidddle.frag.ManageSpaceFragment;

import butterknife.ButterKnife;


public class ManageSpaceActivity extends UpActivity {
    @Override
    protected void onMyCreate(Bundle savedInstanceBundle) {
        setContentView(R.layout.activity_manage_space);
        ButterKnife.inject(this);
        if (savedInstanceBundle == null) {
            ManageSpaceFragment fragment = new ManageSpaceFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }
}
