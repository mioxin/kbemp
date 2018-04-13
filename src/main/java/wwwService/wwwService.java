package com.gmail.mrmioxin.kbemp.wwwService;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;
import com.gmail.mrmioxin.kbemp.wwwService.wwwAccess.wwwData;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.WinHttpClients;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    private Logger logger;


    public wwwService() {
        this.logger = BaseConst.logg;
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
                .setSSLSocketFactory(sslsf)
                .build();
        this.apidata = new wwwData(httpclient);
    }

    public wwwData getApidata() {
        return apidata;
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
                        mCards.put(c.getidr(),c);
                        atmpDeps.add(c.getidr());
                    } else {
                        try {
                            c.setname(apidata.getO(c.getNamePhone()));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        mCards.put(c.getidr(),c);
                    }
                    logger.fine(c.toString());
                }
                } catch (IOException e) {
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
