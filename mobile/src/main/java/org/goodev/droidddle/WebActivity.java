package org.goodev.droidddle;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import org.goodev.droidddle.utils.UiUtils;

import butterknife.ButterKnife;


public class WebActivity extends UpActivity {

    private WebView webView;

    @Override
    protected void onMyCreate(Bundle icicle) {
        setContentView(R.layout.activity_web);
        ButterKnife.inject(this);
        webView = (WebView) findViewById(R.id.web);

        if (icicle == null) {
            String url = getIntent().getStringExtra(UiUtils.ARG_URL);
            webView.loadUrl(url);
        } else {
            webView.restoreState(icicle);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
