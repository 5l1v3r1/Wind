package org.wind.common;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.Random;

public class HttpReq {
    public static Integer[] sendGet(String url) {
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(4000)
                .setConnectTimeout(4000)
                .setConnectionRequestTimeout(4000)
                .build();
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response;
        String content;
        Integer[] result = new Integer[2];
        try {
            response = httpclient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            content = EntityUtils.toString(response.getEntity(), "UTF-8");
            result[0] = statusCode;
            result[1] = content.length();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getRandomString(int length){
        String str="abcdefghijklmnopqrstuvwxyz";
        Random random=new Random();
        StringBuffer sb =new StringBuffer();
        for(int i=0;i<length;i++){
            int number=random.nextInt(24);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
