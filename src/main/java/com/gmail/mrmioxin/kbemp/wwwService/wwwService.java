package com.gmail.mrmioxin.kbemp.wwwService;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;
import com.gmail.mrmioxin.kbemp.wwwService.wwwAccess.ThreadApiData;
import com.gmail.mrmioxin.kbemp.wwwService.wwwAccess.wwwData;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.WinHttpClients;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
/**
 * Created by palchuk on 19.02.2018.
 */
public class wwwService {
    private final CloseableHttpClient httpclient;
    //private final CloseableHttpClient httpclient_t;
    private final wwwData apidata;
    private Logger logger = BaseConst.logg;
    //private List<String> AtmpDeps = Collections.synchronizedList(new ArrayList<>()); // временная карточки отделов
    public Map<String, Card> mCards = new ConcurrentHashMap<>();//Collections.synchronizedMap(new HashMap<>()); //все карточки
    public Queue<String> aDeps = new ConcurrentLinkedQueue<>(); //LinkedList<>();
    static ExecutorService execServ = Executors.newFixedThreadPool(10);

    public void addDeps(String idr) {
        aDeps.add(idr);
    }

    public String removeDep() {
        return aDeps.remove();
    }

    public wwwService() {
        // PoolingHttpClientConnectionManager conMng = new PoolingHttpClientConnectionManager();
        // // Increase max total connection to 200
        // conMng.setMaxTotal(100);
        // HttpClientBuilder clientbuilder = HttpClients.custom().setConnectionManager(conMng);
        // //Build the CloseableHttpClient object using the build() method.
        // this.httpclient_t = clientbuilder.build();

        this.httpclient = WinHttpClients.createDefault();
        this.apidata = new wwwData(httpclient);
    }

    public wwwData getApidata() {
        return apidata;
    }

    public CloseableHttpClient getHttpclient() {
        return httpclient;
    }

    public Map<String, Card> get(String str) throws InterruptedException {

        if (!WinHttpClients.isWinAuthAvailable()) {
            logger.warning("Integrated Win auth is not supported!!!");
        }
        aDeps.add(str);
        System.out.println("Get data from www.");
        int waits = 0;
        while (waits < 5) { //ожидаем получение данных для обработки от потоков
            if (aDeps.size() ==0) {
                System.out.print(".");
                waits++;
                Thread.sleep(2000);
                continue;
            }
            waits = 0;
            System.out.print("-");
            String id = removeDep();
            logger.info("--# "+id+" #----------------------------------------------");
            ThreadApiData thrWWW = new ThreadApiData(this, id, "threadWWW-" + id);
            execServ.execute(thrWWW);
        }
        execServ.shutdown();
        try {
            execServ.awaitTermination(20, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("GO OUT from wwwService.");
        return mCards;
    }


    // public Map<String, Card> get(String str) {

    //     if (!WinHttpClients.isWinAuthAvailable()) {
    //         logger.warning("Integrated Win auth is not supported!!!");
    //     }
    //     // System.out.println("Executing request " + httpget.getRequestLine());
    //     Map<String, Card> mCards = new HashMap<>(); //все карточки
    //     ArrayList<Card> atmpCards = new ArrayList<>(); // временная все карточки
    //     ArrayList<String> aDeps = new ArrayList<>(); // карточки отделов
    //     ArrayList<String> atmpDeps = new ArrayList<>(); // временная карточки отделов
    //     aDeps.add(str);

    //     while (aDeps.size() > 0) {
    //         for (String id : aDeps) {
    //             logger.info("--# "+id+" #----------------------------------------------");
    //             try {
    //                 atmpCards = apidata.getCards(id);
    //                 for (Card c : atmpCards) {
    //                     if (c.isParent()) {
    //                         if (c.getidr().startsWith("razd")) {
    //                             atmpDeps.add(c.getidr());
    //                             logger.info("atmpDeps.add: " + c.toString());
    //                         } else {
    //                             logger.info(c.getidr() + " not add in atmpDep.");
    //                         }
    //                     }
    //                 }
    //                 for (Card c : atmpCards){
    //                     mCards.put(c.getidr(),c);
    //                 }
    //             } catch (IOException e) {
    //                 e.printStackTrace();
    //                 // } catch (InterruptedException e) {
    //                 // e.printStackTrace();
    //             }
    //             atmpCards.clear();
    //         }
    //         aDeps.clear();
    //         aDeps.addAll(atmpDeps);
    //         atmpDeps.clear();
    //     }
    //     logger.info("GO OUT from wwwService.");
    //     return mCards;
    // }
}
