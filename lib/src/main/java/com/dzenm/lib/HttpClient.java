package com.dzenm.lib;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author dzenm
 * @date 2019-08-23 15:51
 */
class HttpClient {

    private static final String HTTPS = "HTTPS";
    private HttpURLConnection mHttpURLConnection;

    private @Method
    String mMethod;


    void setMethod(String method) {
        mMethod = method;
    }

    void setRequestProperty(String key, String value) {
        mHttpURLConnection.setRequestProperty(key, value);
    }

    int getResponseCode() throws IOException {
        return mHttpURLConnection.getResponseCode();
    }

    long getContentLength() {
        return mHttpURLConnection.getContentLength();
    }

    InputStream getInputStream() throws IOException {
        return mHttpURLConnection.getInputStream();
    }

    void disconnect() {
        mHttpURLConnection.disconnect();
    }

    /**
     * 获取Http连接
     *
     * @param url 下载文件URL
     * @return HttpURLConnection
     */
    HttpURLConnection build(String url) throws IOException {
        URL httpURL = new URL(url);
        mHttpURLConnection = (HttpURLConnection) httpURL.openConnection();

        if (url.toUpperCase().startsWith(HTTPS)) {
            URLConnection urlConnection = httpURL.openConnection();
            mHttpURLConnection = (HttpURLConnection) urlConnection;
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) mHttpURLConnection;
            mHttpURLConnection = httpsURLConnection;
        }
        if (mMethod.equals(Method.METHOD_POST)) {
            // 设置是否向从HttpURLConnection输出, Post请求中,
            // 参数要放在http正文内, 因此需要设置为true, 默认情况下是false
            mHttpURLConnection.setDoOutput(true);
            // Post请求不能使用缓存
            mHttpURLConnection.setUseCaches(false);
        }
        // 设置是否向从HttpURLConnection读入, 默认情况下是true
        mHttpURLConnection.setDoInput(true);
        // 设置字符编码
        mHttpURLConnection.setRequestProperty("Charset", "UTF-8");
        mHttpURLConnection.setRequestMethod(mMethod);
        mHttpURLConnection.setConnectTimeout(10000);
        mHttpURLConnection.setReadTimeout(20000);
        return mHttpURLConnection;
    }
}
