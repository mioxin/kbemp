package com.gmail.mrmioxin.kbemp.wwwService;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;
import com.gmail.mrmioxin.kbemp.wwwService.wwwAccess.wwwData;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.WinHttpClients;

import java.io.IOException;
//import java.net.ProxySelector;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
/**
 * Created by palchuk on 19.02.2018.
 */
public class wwwService {
    private final CloseableHttpClient httpclient;
    private final wwwData apidata;
    private Logger logger = BaseConst.logg;

    public wwwService() {

        // SSL context for secure connections can be created either based on
        // system or application specific properties.
        // SSLContext sslcontext = SSLContexts.createSystemDefault();
        // // Create a registry of custom connection socket factories for supported
        // // protocol schemes.
        // Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
        //         .register("http", PlainConnectionSocketFactory.INSTANCE)
        //         .register("https", new SSLConnectionSocketFactory(sslcontext)).build();

        // PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry, new SystemDefaultDnsResolver());
        // // Increase max total connection to 200
        // cm.setMaxTotal(40);
        // // Increase default max connection per route to 20
        // cm.setDefaultMaxPerRoute(20);
        // // Increase max connections for localhost:80 to 50
        // HttpHost localhost = new HttpHost("locahost", 80);
        // cm.setMaxPerRoute(new HttpRoute(localhost), 50);

        // SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());

        // // Use custom cookie store if necessary.
        // CookieStore cookieStore = new BasicCookieStore();
        // // Use custom credentials provider if necessary.
        // CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        // // Create global request configuration
        // RequestConfig defaultRequestConfig = RequestConfig.custom()
        //     .setCookieSpec(CookieSpecs.DEFAULT)
        //     .setExpectContinueEnabled(true)
        //     .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
        //     .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM))
        //     .build();

        // Create an HttpClient with the given custom dependencies and configuration.
        this.httpclient = WinHttpClients.createDefault();
        // this.httpclient = WinHttpClients.custom()
        //     .setConnectionManager(cm)
        //     .setDefaultCookieStore(cookieStore)
        //     .setDefaultCredentialsProvider(credentialsProvider)
        //     .setRoutePlanner(routePlanner)
        //     //.setProxy(new HttpHost(BaseConst.PROXY, 8080))
        //     .setDefaultRequestConfig(defaultRequestConfig)
        //     .build();

        this.apidata = new wwwData(httpclient);
    }

    public wwwData getApidata() {
        return apidata;
    }

    public CloseableHttpClient getHttpclient() {
        return httpclient;
    }

    public Map<String, Card> get(String str) {

        if (!WinHttpClients.isWinAuthAvailable()) {
            logger.warning("Integrated Win auth is not supported!!!");
        }
        // System.out.println("Executing request " + httpget.getRequestLine());
        Map<String, Card> mCards = new HashMap<>(); //все карточки
        ArrayList<Card> atmpCards = new ArrayList<>(); // временная все карточки
        ArrayList<String> aDeps = new ArrayList<>(); // карточки отделов
        ArrayList<String> atmpDeps = new ArrayList<>(); // временная карточки отделов
        aDeps.add(str);

        while (aDeps.size() > 0) {
            for (String id : aDeps) {
                logger.info("--# "+id+" #----------------------------------------------");
                try {
                    atmpCards = apidata.getCards(id);
                    for (Card c : atmpCards) {
                        if (c.isParent()) {
                            if (c.getidr().startsWith("razd")) {
                                atmpDeps.add(c.getidr());
                                logger.info("atmpDeps.add: " + c.toString());
                            } else {
                                logger.info(c.getidr() + " not add in atmpDep.");
                            }
                        }
                    }
                    for (Card c : atmpCards){
                        mCards.put(c.getidr(),c);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
                atmpCards.clear();
            }
            aDeps.clear();
            aDeps.addAll(atmpDeps);
            atmpDeps.clear();
        }
        logger.info("GO OUT from wwwService.");
        return mCards;
    }
}
