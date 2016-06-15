package org.goodev.droidddle.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.imagepipeline.core.DefaultExecutorSupplier;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.memory.PooledByteBufferInputStream;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import org.goodev.droidddle.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by yfcheng on 2015/6/6.
 */
public class FrescoUtils {

    //@formatter:off
    public static final void setShotHierarchy(Context context, DraweeView view) {
        setShotHierarchy(context, view, ScalingUtils.ScaleType.FIT_XY);
    }

    public static final void setShotAdHierarchy(Context context, DraweeView view) {
        setShotHierarchy(context, view, ScalingUtils.ScaleType.FIT_START);
    }

    public static final void setShotHierarchy(Context context, DraweeView view, ScalingUtils.ScaleType type) {
        final Resources res = context.getResources();
        final int color = ThemeUtil.getThemeColor(context, R.attr.colorAccent);
        final ProgressBarDrawable progress = new ProgressBarDrawable();
        progress.setBackgroundColor(Color.parseColor("#33000000"));
        progress.setColor(color);
        GenericDraweeHierarchy gdh = new GenericDraweeHierarchyBuilder(res)
                .setPlaceholderImage(res.getDrawable(R.drawable.placeholder))
                .setProgressBarImage(progress)
                .setActualImageScaleType(type)
                .build();
        view.setHierarchy(gdh);
    }

    public static final void downloadImage(final Context ctx, String url) {
        File path = UiUtils.getDownloadFile(ctx, url, 0);

        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .setProgressiveRenderingEnabled(true)
                .build();
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<PooledByteBuffer>>
                dataSource = imagePipeline.fetchEncodedImage(imageRequest, ctx);
        dataSource.subscribe(new BaseDataSubscriber<CloseableReference<PooledByteBuffer>>() {
            @Override
            protected void onNewResultImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                PooledByteBuffer buffer = dataSource.getResult().get();
                PooledByteBufferInputStream is = new PooledByteBufferInputStream(buffer);
                byte[] buf = new byte[10240];
                try {
                    FileOutputStream os = new FileOutputStream(path);
                    int res = 0;
                    int offset = 0;
                    do {
                        res = is.read(buf, offset, 10240);
                        if (res > 0) {
                            os.write(buf, 0, res);
                        } else {
                            os.flush();
                            os.close();
                        }
                    } while (res > 0);

                    String tips = ctx.getString(R.string.download_tips, path.getAbsolutePath());
                    Toast.makeText(ctx, tips, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                Toast.makeText(ctx, R.string.general_error, Toast.LENGTH_SHORT).show();
            }
        }, new DefaultExecutorSupplier(1).forLocalStorageWrite());
    }

    //    public static final void  setShotUrl(DraweeView view, String url, String thumbnail){
//        setShotUrl(view, url, thumbnail, null);
//    }
    public static final void setShotUrl(DraweeView view, String url, String thumbnail/*, BaseControllerListener listener*/) {
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
//                .setResizeOptions(
//                        new ResizeOptions(300, 400))
                .setProgressiveRenderingEnabled(true)
                .build();
        ImageRequest lowRequest = null;
        if (!TextUtils.isEmpty(thumbnail)) {
            lowRequest = ImageRequest.fromUri(thumbnail);
        }
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest)
                .setLowResImageRequest(lowRequest)
                .setOldController(view.getController())
                .setAutoPlayAnimations(true)
//                .setControllerListener(listener)
                .build();
        view.setController(draweeController);
    }

    public static final void setShotImage(DraweeView view, Uri uri) {
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                .setProgressiveRenderingEnabled(true)
//                        .setResizeOptions(new ResizeOptions(1024,1024))
                .build();
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest)
                .setOldController(view.getController())
                .setAutoPlayAnimations(true)
                .build();
        view.setController(draweeController);
    }
    //@formatter:on
}
