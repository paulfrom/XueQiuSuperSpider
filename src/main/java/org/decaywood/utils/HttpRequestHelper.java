package org.decaywood.utils;

import com.google.common.collect.Lists;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private static final Map<String, String> cookie = new ConcurrentHashMap<>();

    public HttpRequestHelper(String webSite) {
        this.config = new HashMap<>();
        this.gzipDecode()
                .addToHeader("Referer", webSite)
                .addToHeader("Cookie", cookie.get("Cookie"))
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
            httpPost.setHeader("Cookie",cookie.get("Cookie"));
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
            if(cookie.get("Cookie")==null){
                updateCookie();
            }
            httpGet.setHeader("Cookie",cookie.get("Cookie"));
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
            updateCookie();
        }
        return null;
    }

    private synchronized void updateCookie() {
        cookie.put("Cookie","aliyungf_tc=AQAAAAzZaQfvpgEA0mxfZYdrOfwVnZ1+; device_id=86ad2391345de2c2af32d9ac2afaad72; remember=1; remember.sig=K4F3faYzmVuqC0iXIERCQf55g2Y; xq_a_token=45c40eafd031280193af7b67a30f18de76f95895; xq_a_token.sig=Zys3J48cId_cBPfOL0t_Daa1Evk; xq_r_token=be73c25af9f7f2c37e469dcf8b287c71e35f8c93; xq_r_token.sig=_oSDX78-xrXO3-uNb6APBveLKOQ; xq_is_login=1; xq_is_login.sig=J3LxgPVPUzbBg3Kee_PquUfih7Q; u=5499852316; u.sig=gDQ4yzeMrcEpajmUsnIeexPeqC8; s=ef12byhkoj; bid=e1597e4c3067dd251c5130e8a0e0e73f_jacbyydz; Hm_lvt_1db88642e346389874251b5a1eded6e3=1511432632; Hm_lpvt_1db88642e346389874251b5a1eded6e3=1511432644");

//        try( CloseableHttpClient httpClient = HttpClients.createDefault()){
//            List<NameValuePair> params = Lists.newArrayList();
//            params.add(new BasicNameValuePair("areaCode", "86"));
//            params.add(new BasicNameValuePair("userID", "15201929970"));
//            params.add(new BasicNameValuePair("password", ""));
//            params.add(new BasicNameValuePair("rememberMe", "true"));
//            String str = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
//            HttpGet httpGet = new HttpGet("http://xueqiu.com/user/login?"+str);
//            CloseableHttpResponse response = httpClient.execute(httpGet);
//            if(response != null) {
//                HeaderIterator it = response.headerIterator("Set-Cookie");
//                Header item;
//                StringBuilder value = new StringBuilder();
//                while (it.hasNext()){
//                    item = it.nextHeader();
//                    value.append(item.getValue());
//                }
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
