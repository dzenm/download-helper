package com.dzenm.downloadhelper;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.dzenm.downloadhelper.databinding.ActivityMainBinding;
import com.dzenm.lib.HttpHelper;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private HttpHelper http;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        http = new HttpHelper();
        http.setFilePath(Environment.getExternalStorageDirectory().getPath() + "/helper/file")
                .setFileName("app_release.apk")
                .setOnDownloadListener(new HttpHelper.DownloadListener() {
                    @Override
                    public void onProgress(final long percent) {
                        append(percent + "%  ");
                        Log.d("TAG", "下载的百分比: " + percent + "%");
                    }

                    @Override
                    public void onSuccess() {
                        append("\n下载成功: 100%");
                        Log.d("TAG", "下载成功");
                    }

                    @Override
                    public void onError(final String errorMsg) {
                        append("\n下载错误: " + errorMsg);
                        Log.d("TAG", "下载错误: " + errorMsg);
                    }
                });
    }

    public void onClick(View view) {
        if (view.getId() == R.id.tv_start) {
            new Thread(moreThread).start();
            append("\n开始下载: ");
        } else if (view.getId() == R.id.tv_stop) {
            http.pause();
            append("\n停止下载");
        } else if (view.getId() == R.id.tv_delete) {
            if (http.deleteFile()) {
                append("\n删除成功");
            } else {
                append("\n删除失败");
            }
        }
    }

    private Thread moreThread = new Thread() {
        @Override
        public void run() {
            http.setUrl("http://x7.197746.com/yhhgw.apk")
                    .start();
        }
    };

    private void append(final String log) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.tvLog.append(log);
            }
        });
    }
}
