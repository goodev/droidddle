package org.goodev.droidddle;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.pojo.Bucket;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class CreateBucketActivity extends UpActivity {

    @InjectView(R.id.edit_text_name)
    EditText mNameView;
    @InjectView(R.id.edit_text_description)
    EditText mDescriptionView;

    MenuItem mSendMenu;

    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_create_bucket);
        ButterKnife.inject(this);

        mNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateMenuStatus(s.toString().trim());
            }
        });
    }

    private void updateMenuStatus(String text) {
        if (mSendMenu != null)
            mSendMenu.setEnabled(!TextUtils.isEmpty(text));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_comment, menu);
        mSendMenu = menu.findItem(R.id.action_send);
        mSendMenu.setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            addBucket();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addBucket() {
        String text = mNameView.getText().toString();
        String des = mDescriptionView.getText().toString();
        if (!Utils.hasInternet(this)) {
            Toast.makeText(this, R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        Observable<Bucket> observable = ApiFactory.getService(this).postBucket(text, des);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe((bucket) -> {
                    bucketCreated(bucket);
                }, new ErrorCallback(this));

    }

    private void bucketCreated(Bucket bucket) {
        UiUtils.showToast(this, R.string.bucket_created);
        setResult(RESULT_OK);
        finish();
    }
}
