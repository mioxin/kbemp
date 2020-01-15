package com.gmail.mrmioxin.kbemp.wwwService.wwwAccess;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Logger;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;


/**
 * Created by palchuk on 19.02.2018.
 */
public class wwwData {
    private final CloseableHttpClient httpclient;
    private HttpGet httpget;
    //private Executor executor;
    private Logger logger = BaseConst.logg;

    public wwwData(CloseableHttpClient httpclient) {
        //this.executor = new Executor(httpclient);
        httpget = new HttpGet();
        httpget.addHeader("Connection", "keep-alive");
        httpget.addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
        httpget.addHeader("Upgrade-Insecure-Requests","1");
        httpget.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        httpget.addHeader("DNT","1");
        httpget.addHeader("Accept-Encoding"," gzip, deflate");
        httpget.addHeader("Accept-Language","ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");

        this.httpclient = httpclient;
    }

    public ArrayList<Card> getCards(String strreq) throws IOException {
        try {
            httpget.setURI(new URI(BaseConst.BASEADDR + strreq));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return httpclient.execute(httpget, response -> {
            ArrayList<Card> aCards = new ArrayList<>();
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    logger.warning("Ответ не содержит данных: " + response.toString());
                    return null;
                } else {
                    //return EntityUtils.toString(entity);
                    JsonArray jarray = null;
                    try {
                        jarray = new JsonParser().parse(EntityUtils.toString(entity)).getAsJsonArray();
                    } catch (IOException e) {
                        logger.severe("Don't parse entity: " + EntityUtils.toString(entity));
                        e.printStackTrace();
                    }
                    JsonObject jo;
                    for (JsonElement je : jarray) {
                        jo = (je.isJsonObject()) ? je.getAsJsonObject() : null;
                        if (jo != null) {
                            aCards.add(new Card(jo));
                        } else {
                            logger.warning("Not JSON Object: " + je.getAsString());
                        }
                    }
                }
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            return aCards;
        });
    }

}
