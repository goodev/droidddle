package org.goodev.droidddle;

import android.os.Bundle;
import android.widget.Toast;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.api.ErrorCallback;
import org.goodev.droidddle.api.SucessCallback;
import org.goodev.droidddle.frag.user.UserBucketFragment;
import org.goodev.droidddle.holder.OnClickListener;
import org.goodev.droidddle.pojo.Bucket;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;

import butterknife.ButterKnife;
import retrofit.client.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class AddToBucketActivity extends UpActivity implements OnClickListener<Bucket> {

    private long mShotId;

    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_add_to_bucket);
        ButterKnife.inject(this);
        mShotId = getIntent().getLongExtra(UiUtils.ARG_SHOT_ID, UiUtils.NO_ID);

        if (savedInstanceState == null) {
            UserBucketFragment fragment = UserBucketFragment.newInstance(App.getUser());
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }
    }

    @Override
    public void onClicked(Bucket data) {
        if (!Utils.hasInternet(this)) {
            Toast.makeText(this, R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        Observable<Response> observable = ApiFactory.getService(this).addShotToBucket(data.id, mShotId);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SucessCallback<Response>(this, R.string.shot_add_to_bucket_success) {
                    @Override
                    public void call(Response o) {
                        super.call(o);
                        finish();
                    }
                }, new ErrorCallback(this));
    }
}
