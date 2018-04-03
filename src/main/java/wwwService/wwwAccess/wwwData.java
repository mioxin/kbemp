package com.gmail.mrmioxin.kbemp.wwwService.wwwAccess;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;
import com.gmail.mrmioxin.kbemp.Cards;
import com.gmail.mrmioxin.kbemp.wwwService.ececutor.Executor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by palchuk on 19.02.2018.
 */
public class wwwData {
    private final CloseableHttpClient httpclient;
    private HttpGet httpget;
    //private Executor executor;
    private Logger logger;

    public wwwData(CloseableHttpClient httpclient) {
        this.logger = BaseConst.logg;
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

    public ArrayList<Card> getCards(String strreq){
        try {
            httpget.setURI(new URI(BaseConst.BASEADDR + strreq));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return httpclient.execute(httpget, response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return null;
                } else {
                    return EntityUtils.toString(entity);
                }
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }

            ArrayList<Card> aCards = new ArrayList<>();
            if (result == null) {
                return null;
            } else {
                JsonArray jarray = null;
                try {
                    jarray = new JsonParser().parse(EntityUtils.toString(result)).getAsJsonArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JsonObject jo;
                for (JsonElement je : jarray) {
                    jo = (je.isJsonObject()) ? je.getAsJsonObject() : null;
                    if (jo != null) {
                        aCards.add(new Card(jo));
                    }
                }
                return aCards;
            }
        });
    }

    public String getO(ArrayList<String> namephone) throws UnsupportedEncodingException {
        Pattern p_vn = Pattern.compile("<b>(-?\\d{2,4}-?\\d{0,2}-?\\d{0,2})<\\/b>");
        Pattern p_wordsonly = Pattern.compile("([А-Яа-я]+)',");

        return executor.request(BaseConst.FIOADDR + URLEncoder.encode(namephone.get(0), "UTF-8"), result -> {
            if (result == null) {
                return null;
            } else {
                String fio = "";
                String[] aResponse = new String[0];
                try {
                    aResponse = EntityUtils.toString(result).split(BaseConst.SEARCHDELIM);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (String s : aResponse) {
                    if (s.isEmpty()) {
                        //logger.fine("FIO not found.");
                    } else if (findPattern(p_vn, s).equals(namephone.get(1))) {
                        String[] as = s.split(" ");

                        fio = as[0] + " " + as[1] + " " + findPattern(p_wordsonly, as[2]);
                        fio = fio.replaceAll("'", "");
                        fio = fio.replaceAll(",", "");
                    }
                }
                return fio;
            }
        });
    }

    public Integer downloadImgFile(String url) {
        String base="";
        URL imgUrl = null;
        Path imgFile = null;
        Pattern p_host = Pattern.compile("^(http://\\S+?\\.\\S+?\\.\\S+?)");
        if (findPattern(p_host,url) == "") {
            base = BaseConst.WWWINT;
        }
        final String finalUrl = base + url;
        try {
            imgFile = Paths.get(new URI(finalUrl));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (Files.isDirectory(imgFile.getParent())) {
            if  (Files.exists(imgFile)) {//если файл уже есть - trow, иначе скачиваем
                return 0;
            }
        } else {//если нет папки - создаем
            try {
                Files.createDirectories(imgFile.getParent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return executor.request(finalUrl, result -> {
            if (result == null) {
                return null;
            } else {

                return 1;
            }
        });
    }
    private static String findPattern(Pattern p, String t){
        Matcher m;
        m = p.matcher(t);
        return (m.find())?m.group(1): "";
    }

}
