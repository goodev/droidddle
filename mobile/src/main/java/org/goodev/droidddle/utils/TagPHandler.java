package org.goodev.droidddle.utils;

import android.text.Editable;
import android.text.Html;

import org.xml.sax.XMLReader;

/**
 * Created by ADMIN on 2014/12/26.
 */
public class TagPHandler implements Html.TagHandler {
    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if ("p".equalsIgnoreCase(tag)) {
        }
    }
}
