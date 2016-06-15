package org.goodev.droidddle.download;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by goodev on 2015/1/19.
 */
public class DownloadService extends IntentService {
    public static final int UPDATE_PROGRESS = 8344;
    public static boolean sStop = false;

    public DownloadService() {
        super("DownloadService");
    }

    public static String getShotImageFile(Context context, String url) {
        String fileName = Utils.getFileName(url);
        File dir = Utils.getImageDir(context);
        File path = new File(dir, fileName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return path.getAbsolutePath();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sStop = false;
        String urlToDownload = intent.getStringExtra("url");
        ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");
        String file = getShotImageFile(this, urlToDownload);
        long fileLength = 0;
        try {
            OkHttpClient client = ApiFactory.getOkHttpClient(this);
            Request request = new Request.Builder()
                    .url(urlToDownload)
                    .build();
            Response response = client.newCall(request).execute();

            if (response.code() == 200) {
                InputStream inputStream = null;
                try {
                    inputStream = response.body().byteStream();
                    byte[] buff = new byte[1024];
                    long downloaded = 0;
                    fileLength = response.body().contentLength();

                    File imageFile = new File(file);
                    if (imageFile.exists() && imageFile.length() == fileLength) {
                        Bundle resultData = new Bundle();
                        resultData.putInt("progress", 100);
                        resultData.putString("file", file);
                        receiver.send(UPDATE_PROGRESS, resultData);
                        return;
                    }
                    OutputStream output = new FileOutputStream(file);

                    publishProgress(receiver, 0L, fileLength, file);
                    while (true) {
                        int readed = inputStream.read(buff);
                        if (readed == -1) {
                            break;
                        }
                        //write buff
                        output.write(buff, 0, readed);
                        downloaded += readed;
                        publishProgress(receiver, downloaded, fileLength, file);
                    }
                    output.flush();
                    output.close();
                } catch (IOException ignore) {
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Bundle resultData = new Bundle();
        resultData.putInt("progress", 100);
        resultData.putLong("total", fileLength);
        resultData.putLong("current", fileLength);
        resultData.putString("file", file);
        receiver.send(UPDATE_PROGRESS, resultData);
    }

    private void publishProgress(ResultReceiver receiver, long current, long total, String file) {
        // publishing the progress....
        Bundle resultData = new Bundle();
        resultData.putInt("progress", (int) (current * 100 / total));
        resultData.putLong("current", current);
        resultData.putLong("total", total);
        resultData.putString("file", file);
        receiver.send(UPDATE_PROGRESS, resultData);
    }


}
