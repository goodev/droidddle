package org.goodev.droidddle;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.widget.GridSpacesItemDecoration;
import org.goodev.droidddle.widget.ImageGalleryAdapter;
import org.goodev.droidddle.widget.ImageGalleryUtils;

import java.io.File;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by yfcheng on 2015/10/20.
 * https://github.com/lawloretienne/ImageGallery/blob/master/library/src/main/java/com/etiennelawlor/imagegallery/library/activities/ImageGalleryActivity.java
 */
public class DownloadedActivity extends UpActivity implements ImageGalleryAdapter.OnImageClickListener {

    @InjectView(R.id.rv)
    RecyclerView mRecyclerView;
    @InjectView(R.id.empty)
    TextView mEmptyView;
    private ImageGalleryAdapter mImageGalleryAdapter;
    // region Member Variables
    private ArrayList<String> mImages;

    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_downloaded);
        ButterKnife.inject(this);
        setNavDrawerItem(R.id.drawer_dl);
        loadImages();
        setUpRecyclerView();
    }

    private void loadImages() {
        mImages = new ArrayList<String>();
        File dir = UiUtils.getDownloadFilePath(this);
        if (!dir.exists()) {
            mEmptyView.setVisibility(View.VISIBLE);
            return;
        }
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
            return;
        }
        mEmptyView.setVisibility(View.GONE);
        for (int i = 0; i < files.length; i++) {
            String uri = Uri.fromFile(files[i]).toString();
            mImages.add(uri);
        }
    }


//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        setUpRecyclerView();
//    }

    // region ImageGalleryAdapter.OnImageClickListener Methods
    @Override
    public void onImageClick(View view, int position) {
        String image = mImages.get(position);
        String fileName = UiUtils.getFileName(image);
        int index = fileName.indexOf("-");
        String idStr = index == -1 ? null : fileName.substring(0, index);

        if (TextUtils.isEmpty(idStr)) {
            Toast.makeText(this, R.string.launch_shot_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        long id = 0;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.launch_shot_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        if (id == 0) {
            Toast.makeText(this, R.string.launch_shot_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        UiUtils.launchShot(this, id);

    }
    // endregion

    // region Helper Methods

    private void setUpRecyclerView() {
        int numOfColumns;
        if (ImageGalleryUtils.isInLandscapeMode(this)) {
            numOfColumns = 4;
        } else {
            numOfColumns = 3;
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numOfColumns));
        mRecyclerView.addItemDecoration(new GridSpacesItemDecoration(ImageGalleryUtils.dp2px(this, 2), numOfColumns));
        mImageGalleryAdapter = new ImageGalleryAdapter(mImages);
        mImageGalleryAdapter.setOnImageClickListener(this);

        mRecyclerView.setAdapter(mImageGalleryAdapter);
    }
    // endregion
}
