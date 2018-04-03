package com.gmail.mrmioxin.kbemp.wwwService.ececutor;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Executor {
    private final CloseableHttpClient httpclient;
    private HttpGet httpget;
    private ResponseHandler<HttpEntity> responseHandler;


    public Executor(CloseableHttpClient httpclient) {
        httpget = new HttpGet();
        httpget.addHeader("Connection", "keep-alive");
        httpget.addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
        httpget.addHeader("Upgrade-Insecure-Requests","1");
        httpget.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        httpget.addHeader("DNT","1");
        httpget.addHeader("Accept-Encoding"," gzip, deflate");
        httpget.addHeader("Accept-Language","ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");

        this.httpclient = httpclient;

        // Create a custom response handler
        responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity;
//                    if (entity == null) {
//                        return null;
//                    } else {
//                        return EntityUtils.toString(entity);
//                    }
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };


    }

    public <T> T request(String rqst, ResultHandler<T> handler){
        HttpEntity responseBody = null;

        try {
            httpget.setURI(new URI(rqst));
            responseBody = httpclient.execute(httpget, responseHandler);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        T respons = handler.handle(responseBody);

        return respons;
    }
}
