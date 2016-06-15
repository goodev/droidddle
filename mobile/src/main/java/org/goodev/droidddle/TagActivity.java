package org.goodev.droidddle;

import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.TextUtils;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.frag.SearchBaseFragment;
import org.goodev.droidddle.provider.SearchProvider;
import org.goodev.droidddle.utils.UiUtils;

import butterknife.ButterKnife;

/**
 * Created by yfcheng on 2015/8/10.
 */
public class TagActivity extends UpActivity {
    SearchBaseFragment mSearchFragment;
    String mQuery;
    String mType;
    boolean mIsLarge;

    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_tag);
        ButterKnife.inject(this);
        mQuery = getIntent().getStringExtra(UiUtils.ARG_QUERY);
        if (mQuery == null) {
            finish();
            return;
        }
        setTitle(getString(R.string.title_activity_tag, mQuery));
        mIsLarge = getResources().getInteger(R.integer.shot_column) > 1;

    }

    @Override
    protected void onPostMyCreate(Bundle savedInstanceState) {
        super.onPostMyCreate(savedInstanceState);
        if (mIsLarge) {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
        }
        mSearchFragment = (SearchBaseFragment) getSupportFragmentManager()
                .findFragmentById(R.id.search_fragment);
        mSearchFragment.updateSearch(ApiFactory.TAG_SEARCH + mQuery);
    }

    private void goToSearch(String s) {
        mQuery = s;
        mSearchFragment.updateSearch(s);
    }


    private void saveQuery(String query) {
        if (TextUtils.isEmpty(query)) {
            return;
        }
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchProvider.AUTHORITY, SearchProvider.MODE);
        suggestions.saveRecentQuery(query, null);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
}
