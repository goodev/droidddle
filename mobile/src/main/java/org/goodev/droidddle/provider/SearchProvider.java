package org.goodev.droidddle.provider;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by goodev on 2015/1/8.
 */
public class SearchProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "org.goodev.droidddle.SuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SearchProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
