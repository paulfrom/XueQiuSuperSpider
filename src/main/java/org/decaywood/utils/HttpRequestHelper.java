package org.decaywood.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/**
 * @author: decaywood
 * @date: 2015/11/23 14:27
 */
public class HttpRequestHelper {

    private Map<String, String> config;
    private boolean post;
    private boolean gzip;

    public HttpRequestHelper(String webSite) {
        this.config = new HashMap<>();
        this.gzipDecode()
                .addToHeader("Referer", webSite)
                .addToHeader("Cookie", FileLoader.loadCookie(webSite))
                .addToHeader("Host", "xueqiu.com")
                .addToHeader("Accept-Encoding", "gzip,deflate,sdch");
    }

    public HttpRequestHelper post() {
        this.post = true;
        return this;
    }

    public HttpRequestHelper gzipDecode() {
        this.gzip = true;
        return this;
    }

    public HttpRequestHelper addToHeader(String key, String val) {
        this.config.put(key, val);
        return this;
    }

    public HttpRequestHelper addToHeader(String key, int val) {
        this.config.put(key, String.valueOf(val));
        return this;
    }

    public String request(URL url) throws IOException {
        return request(url, this.config);
    }


    public String request(URL url, Map<String, String> config) throws IOException {
        HttpURLConnection httpURLConn = null;
        try {
            httpURLConn = (HttpURLConnection) url.openConnection();
            if (post) httpURLConn.setRequestMethod("POST");
            httpURLConn.setDoOutput(true);
            for (Map.Entry<String, String> entry : config.entrySet())
                httpURLConn.setRequestProperty(entry.getKey(), entry.getValue());
            httpURLConn.connect();
            InputStream in = httpURLConn.getInputStream();
            try {
                if (gzip) in = new GZIPInputStream(in);
            }catch (ZipException ex){
                in = new ZipInputStream(in);
            }

            BufferedReader bd = new BufferedReader(new InputStreamReader(in));
            StringBuilder builder = new StringBuilder();
            String text;
            while ((text = bd.readLine()) != null) builder.append(text);
            return builder.toString();
        } finally {
            if (httpURLConn != null) httpURLConn.disconnect();
        }
    }

    public String clientPostRequest(URL url){
        try( CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost();
            httpPost.setHeader("Cookie",FileLoader.loadCookie("xxxx"));
            httpPost.setURI(url.toURI());
            CloseableHttpResponse response = httpClient.execute(httpPost);
            if(response != null) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity);
            }
            return null;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String clientGetRequest(URL url){
        try( CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet();
            httpGet.setHeader("Cookie",FileLoader.loadCookie("xxxx"));
            httpGet.setURI(url.toURI());
            CloseableHttpResponse response = httpClient.execute(httpGet);
            if(response != null) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity);
            }
            return null;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
