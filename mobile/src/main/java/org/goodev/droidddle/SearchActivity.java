package org.goodev.droidddle;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Spinner;

import org.goodev.droidddle.frag.SearchFragment;
import org.goodev.droidddle.frag.SearchUserFragment;
import org.goodev.droidddle.provider.SearchProvider;
import org.goodev.droidddle.utils.UiUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnLongClick;


public class SearchActivity extends UpActivity implements AdapterView.OnItemSelectedListener {

    Spinner mSpinner;
    SearchView mSearchView;
    SearchFragment mSearchFragment;
    SearchUserFragment mUserFragment;
    String mQuery;
    String mType;
    boolean mIsLarge;
    @InjectView(R.id.fab)
    FloatingActionButton mFab;

    Handler mHandler = new Handler();

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (mSearchView != null) {
                mSearchView.setQuery(query, false);
            }
            goToSearch(query);
        }
    }

    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_search);
        ButterKnife.inject(this);
        setupFab();
        setTitle(null);
        mIsLarge = getResources().getInteger(R.integer.shot_column) > 1;

//        mSearchFragment.attachFab(mFab);
    }

    @Override
    protected void onPostMyCreate(Bundle savedInstanceState) {
        super.onPostMyCreate(savedInstanceState);
        if (mIsLarge) {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            mSpinner = (Spinner) LayoutInflater.from(this)
                    .inflate(R.layout.search_sort_layout, null);
            mSpinner.setOnItemSelectedListener(this);
            getSupportActionBar().setCustomView(mSpinner);
        }
        if (savedInstanceState != null) {
            mQuery = savedInstanceState.getString(UiUtils.ARG_QUERY);
            mType = savedInstanceState.getString(UiUtils.ARG_TYPE);
        }
        mSearchFragment = (SearchFragment) getSupportFragmentManager()
                .findFragmentById(R.id.search_fragment);
    }

    private void setupFab() {
        if (UiUtils.hasLollipop()) {
//            mFab.setColorRippleResId(R.color.ripple_material_light);
//            mFab.setColorPressed(ThemeUtil.getThemeColor(this, R.attr.navdrawerTintColor));
        } else {
//            mFab.setColorPressed(ThemeUtil.getThemeColor(this, R.attr.colorAccent));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(UiUtils.ARG_QUERY, mQuery);
        outState.putString(UiUtils.ARG_TYPE, mType);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem search = menu.findItem(R.id.action_search);
        MenuItem spinner = menu.findItem(R.id.action_search_order);
        if (mIsLarge) {
            spinner.setVisible(false);
        } else {
            mSpinner = (Spinner) MenuItemCompat.getActionView(spinner);
            mSpinner.setOnItemSelectedListener(this);
        }
        mSearchView = (SearchView) MenuItemCompat.getActionView(search);
        MenuItemCompat.expandActionView(search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        mSearchView.setIconifiedByDefault(false);
        mSearchView.setQueryRefinementEnabled(true);
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                saveQuery(s);
                goToSearch(s);
                return true;

            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(position);
                String s = cursor.getString(SearchRecentSuggestions.QUERIES_PROJECTION_QUERY_INDEX);
                mSearchView.setQuery(s, false);
                goToSearch(s);
                return true;
            }
        });
        mSearchView.setQuery(mQuery, false);
        return true;
    }

    private void goToSearch(String s) {
        mSearchView.clearFocus();
        hideIme();
        mQuery = s;
        if (mType.equals("user")) {
            if (mUserFragment != null) {
                mUserFragment.updateSearch(s);
            }
        } else {
            mSearchFragment.updateSearch(s);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSearchView != null) {
            hideIme();
        }
        if (mSpinner != null) {
            mSpinner.requestFocus();
        }
    }

    private void hideIme() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
    }

    private void saveQuery(String query) {
        if (TextUtils.isEmpty(query)) {
            return;
        }
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchProvider.AUTHORITY, SearchProvider.MODE);
        suggestions.saveRecentQuery(query, null);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mType = getFilterType(position);
        //user
        if (position == 2) {
            if (mUserFragment == null) {
                mUserFragment = new SearchUserFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.search_fragment, mUserFragment).commit();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mUserFragment.updateSearch(mQuery);
                }
            }, 500);
        } else {
            if (mSearchFragment != null) {
                Fragment f = getSupportFragmentManager()
                        .findFragmentById(R.id.search_fragment);
                if (f != mSearchFragment) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.search_fragment, mSearchFragment).commit();
                }
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSearchFragment.updateSearch(mQuery);
                        mSearchFragment.updateSearchType(mType);
                    }
                }, 300);

            }
        }
    }

    private String getFilterType(int position) {
        if (position == 0) {
            return "popular";
        } else if (position == 1) {
            return "latest";
        } else if (position == 2) {
            return "user";
        }

        return "popular";
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @OnClick(R.id.fab)
    void onFabClick(View view) {
        UiUtils.launchCreateActivity(this, UiUtils.TYPE_SHOT);

    }

    @OnLongClick(R.id.fab)
    boolean onFabLongClick(View view) {
        UiUtils.launchCreateActivity(this, UiUtils.TYPE_BUCKET);
        return true;
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if(Pref.needShowTagTips(this) || BuildConfig.DEBUG) {
//            View view = findViewById(R.id.main_content1);
//            Snackbar.make(view, R.string.tag_search_tips, Snackbar.LENGTH_INDEFINITE)
//                    .setAction(R.string.got_it, new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Pref.setShowTagStatus(getApplicationContext(), false);
//                        }
//                    })
//                    .show();
//        }
//    }
}
