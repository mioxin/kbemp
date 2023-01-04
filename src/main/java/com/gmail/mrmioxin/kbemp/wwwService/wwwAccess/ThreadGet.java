package com.gmail.mrmioxin.kbemp.wwwService.wwwAccess;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;
// import org.apache.http.HttpEntity;
// import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
// import org.apache.http.util.EntityUtils;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by palchuk on 19.04.2018.
 */
public class ThreadGet extends  Thread {
    protected final CloseableHttpClient httpClient;
    protected final HttpContext context;
    protected final HttpGet httpget;
    protected final Card card;
    protected final String thrName;
    protected Logger logger = BaseConst.logg;


    public ThreadGet(CloseableHttpClient site, Card c, String nameThread){
        PoolingHttpClientConnectionManager conMng = new PoolingHttpClientConnectionManager();
        // Increase max total connection to 200
        conMng.setMaxTotal(100);
        // // Increase default max connection per route to 20
        // cm.setDefaultMaxPerRoute(20);
        // // Increase max connections for localhost:80 to 50
        // HttpHost localhost = new HttpHost("locahost", 80);
        // cm.setMaxPerRoute(new HttpRoute(localhost), 50);
        //Create a ClientBuilder Object by setting the connection manager
        HttpClientBuilder clientbuilder = HttpClients.custom().setConnectionManager(conMng);

        //Build the CloseableHttpClient object using the build() method.
        this.httpClient = clientbuilder.build();

        //this.httpClient = site;
        this.card = c;
        this.thrName = nameThread;
        this.context = new BasicHttpContext();
        this.httpget = new HttpGet();
        httpget.addHeader("Connection", "keep-alive");
        httpget.addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
        httpget.addHeader("Upgrade-Insecure-Requests","1");
        httpget.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        httpget.addHeader("DNT","1");
        httpget.addHeader("Accept-Encoding"," gzip, deflate");
        httpget.addHeader("Accept-Language","ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");

    }

    protected static String findPattern(Pattern p, String t, int group){
        Matcher m;
        m = p.matcher(t);
        String s =(m.find())?m.group(group): "";
        if (s == null){
            return "";
        }
        return  s;
    }

}
