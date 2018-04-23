package com.gmail.mrmioxin.kbemp.wwwService;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;
import com.gmail.mrmioxin.kbemp.wwwService.wwwAccess.ThreadGetO;
import com.gmail.mrmioxin.kbemp.wwwService.wwwAccess.wwwData;
import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.WinHttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.IOException;
import java.net.ProxySelector;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by palchuk on 19.02.2018.
 */
public class wwwService {
    private final CloseableHttpClient httpclient;
    private final wwwData apidata;
    private SystemDefaultRoutePlanner routePlanner;
    private SSLConnectionSocketFactory sslsf;
    private Logger logger = BaseConst.logg;


    public wwwService() {
//        ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
//
//            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
//                // Honor 'keep-alive' header
//                HeaderElementIterator it = new BasicHeaderElementIterator(
//                        response.headerIterator(HTTP.CONN_KEEP_ALIVE));
//                while (it.hasNext()) {
//                    HeaderElement he = it.nextElement();
//                    String param = he.getName();
//                    String value = he.getValue();
//                    if (value != null && param.equalsIgnoreCase("timeout")) {
//                        try {
//                            return Long.parseLong(value) * 1000;
//                        } catch(NumberFormatException ignore) {
//                        }
//                    }
//                }
//                HttpHost target = (HttpHost) context.getAttribute(
//                        HttpClientContext.HTTP_TARGET_HOST);
//                return 5 * 1000;
//            }
//
//        };

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
// Increase max total connection to 200
        cm.setMaxTotal(40);
// Increase default max connection per route to 20
        cm.setDefaultMaxPerRoute(20);
// Increase max connections for localhost:80 to 50
        HttpHost localhost = new HttpHost("locahost", 80);
        cm.setMaxPerRoute(new HttpRoute(localhost), 50);

        routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());
        try {
            sslsf = new SSLConnectionSocketFactory(SSLContextBuilder.create()
                    .loadTrustMaterial(new TrustSelfSignedStrategy())
                    .build(),
                    new DefaultHostnameVerifier());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        this.httpclient = WinHttpClients.custom()
                .setRoutePlanner(routePlanner)
                .setConnectionManager(cm)
//                .setSSLSocketFactory(sslsf)
//                .setKeepAliveStrategy(myStrategy)
                .build();
        this.apidata = new wwwData(httpclient);
    }

    public wwwData getApidata() {
        return apidata;
    }

    public  CloseableHttpClient getHttpclient() {
        return httpclient;
    }

    public Map<String,Card> get(String str){

        if (!WinHttpClients.isWinAuthAvailable()) {
            System.out.println("Integrated Win auth is not supported!!!");
        }
//        System.out.println("Executing request " + httpget.getRequestLine());
        Map<String,Card>  mCards = new HashMap<>();
        ArrayList<Card> atmpCards = new ArrayList<>();
        ArrayList<String> aDeps = new ArrayList<>();
        ArrayList<String> atmpDeps = new ArrayList<>();
        aDeps.add(str);

        while (aDeps.size() > 0){
            for (String id : aDeps) {
                try {
                    atmpCards = apidata.getCards(id);
                    for (Card c : atmpCards) {
                        if (c.isParent()) {
//                            mCards.put(c.getidr(),c);
                            atmpDeps.add(c.getidr());
                        } else {//добавляем отчество
//                            try {
//                                c.setname(apidata.getO(c.getNamePhone()));
//                            } catch (UnsupportedEncodingException e) {
//                                e.printStackTrace();
//                            }
                            ThreadGetO thr = new ThreadGetO(httpclient, c, "thread"+c.getTabnum());
                            ThreadGetO.threads.add(thr);
                            thr.start();
                        }
                        logger.fine(c.toString());
                    }
                    for (ThreadGetO th: ThreadGetO.threads){
                        th.join();
                    }

                    for (Card c : atmpCards){
                        mCards.put(c.getidr(),c);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                atmpCards.clear();
            }
            aDeps.clear();
            aDeps.addAll(atmpDeps);
            atmpDeps.clear();
        }
        return mCards;
    }
}
