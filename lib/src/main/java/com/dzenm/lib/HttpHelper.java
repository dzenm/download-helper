package com.dzenm.lib;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpRetryException;
import java.net.UnknownHostException;

/**
 * @author dzenm
 * @date 2019-08-22 14:15
 */
public class HttpHelper {

    private static final String TAG = HttpHelper.class.getSimpleName();

    private boolean isRunning = false;
    private DownloadListener mDownloadListener;
    private String mFilePath = Environment.getExternalStorageDirectory().getPath();
    private String mFileName = "app_release.apk";
    private File mDownloadFile;
    private String mUrl;

    public HttpHelper setOnDownloadListener(DownloadListener DownloadListener) {
        mDownloadListener = DownloadListener;
        return this;
    }

    public HttpHelper setFilePath(String filePath) {
        mFilePath = filePath;
        return this;
    }

    public HttpHelper setFileName(String fileName) {
        mFileName = fileName;
        return this;
    }

    public HttpHelper setUrl(String url) {
        mUrl = url;
        return this;
    }

    public HttpHelper start() {
        download();
        return this;
    }

    public HttpHelper pause() {
        isRunning = false;
        return this;
    }

    public HttpHelper cancel() {
        isRunning = false;
        deleteFile();
        return this;
    }

    public boolean deleteFile() {
        try {
            isRunning = false;
            if (mDownloadFile.exists()) {
                return mDownloadFile.delete();
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "删除失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isDownloading() {
        return isRunning;
    }

    private void download() {
        if (isRunning) return;

        HttpClient httpClient = new HttpClient();
        try {
            mDownloadFile = createDownloadFile(mFilePath, mFileName);
            long fileSize = mDownloadFile.length();
            Log.d(TAG, "already download file size: " + fileSize);

            httpClient.setMethod(Method.METHOD_GET);
            httpClient.build(mUrl);
            // 设置开始下载的位置, 单位为字节
            httpClient.setRequestProperty("Range", "bytes=" + (mDownloadFile.exists() ? fileSize : 0) + "-");

            startDownload(httpClient, fileSize);
        } catch (IOException e) {
            isRunning = false;
            if (e instanceof UnknownHostException) {
                mDownloadListener.onError("未知的服务器");
            } else if (e instanceof HttpRetryException) {
                mDownloadListener.onError("正在重试下载");
            }
            e.printStackTrace();
        } finally {
            httpClient.disconnect();
        }
    }

    /**
     * 开始下载
     *
     * @param client   获取请求结果
     * @param fileSize 文件大小
     */
    private void startDownload(HttpClient client, long fileSize) throws IOException {
        int responseCode = client.getResponseCode();
        Log.d(TAG, "http response code: " + responseCode);

        long contentLength = client.getContentLength();
        Log.d(TAG, "remaining size: " + contentLength);

        isRunning = true;
        if (responseCode == 200 || responseCode == 206) {
            long totalSize = fileSize + contentLength;
            writeStreamToFile(client, totalSize);
        } else if (responseCode == 416) {
            if (contentLength == 0) {
                mDownloadListener.onSuccess();
            } else {
                mDownloadListener.onError("超出文件范围");
            }
            isRunning = false;
        } else {
            mDownloadListener.onError("Http请求错误");
            isRunning = false;
        }
    }

    /**
     * 创建下载文件
     *
     * @param parent   文件夹路径
     * @param fileName 文件名称
     * @return File
     */
    private File createDownloadFile(String parent, String fileName) {
        File folder = new File(parent);
        if (!folder.exists()) folder.mkdirs();
        Log.d(TAG, "下载文件路径: " + folder.getPath());
        File file = new File(folder, fileName);
        if (file.isDirectory()) file.delete();
        return file;
    }

    /**
     * 保存文件
     *
     * @param client    获取下载文件流
     * @param totalSize 总下载文件大小
     */
    private void writeStreamToFile(HttpClient client, long totalSize) {
        long fileSize = mDownloadFile.length();
        try (InputStream is = client.getInputStream();
             FileOutputStream fos = new FileOutputStream(mDownloadFile, true)) {
            int length;
            byte[] buffer = new byte[102400];
            while (isRunning && (length = is.read(buffer)) != -1) {
                if (!isRunning) break;
                fos.write(buffer, 0, length);
                fileSize = fileSize + length;
                callbackProgress(totalSize, fileSize);
                callbackSuccess(totalSize, fileSize);
            }
        } catch (IOException e) {
            isRunning = false;
            Log.e(TAG, "下载失败: " + e.getMessage());
            mDownloadListener.onError("下载失败:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private long mCurrentPercent = 0;

    /**
     * 下载进度回调
     *
     * @param totalSize 文件的总大小
     * @param fileSize  已下载文件的大小
     */
    private void callbackProgress(long totalSize, long fileSize) {
        mDownloadListener.onProgress(totalSize, fileSize);
        // 计算下载的百分比
        long percent = fileSize * 100 / totalSize;
        if (percent != mCurrentPercent) {
            mCurrentPercent = percent;
            mDownloadListener.onProgress(percent);
        }
    }

    /**
     * 下载成功回调
     *
     * @param totalSize 文件的总大小
     * @param fileSize  已下载文件的大小
     */
    private void callbackSuccess(long totalSize, long fileSize) {
        if (totalSize == fileSize) {
            mDownloadListener.onSuccess();
            isRunning = false;
        }
    }

    public static class DownloadListener {

        /**
         * 下载文件成功
         */
        public void onSuccess() {

        }

        /**
         * 下载文件进度
         *
         * @param totalValue   文件总大小
         * @param currentValue 当前下载的文件大小
         */
        public void onProgress(long totalValue, long currentValue) {

        }

        /**
         * 下载文件进度
         *
         * @param percent 文件下载的百分比
         */
        public void onProgress(long percent) {

        }

        /**
         * 下载出错
         *
         * @param errorMsg 错误信息
         */
        public void onError(String errorMsg) {

        }
    }
}
