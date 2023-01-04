package com.gmail.mrmioxin.kbemp;

import com.gmail.mrmioxin.kbemp.dbService.DBException;
import com.gmail.mrmioxin.kbemp.dbService.DBService;
import com.gmail.mrmioxin.kbemp.fileService.fileService;
import com.gmail.mrmioxin.kbemp.wwwService.wwwAccess.ThreadGetO;
import com.gmail.mrmioxin.kbemp.wwwService.wwwService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by palchuk on 31.01.2018.
 */
public class Cards {
    private Map<String, Card> mCards;
    public wwwService site;
    private static Logger logger= BaseConst.logg;
    
    private DBService dbService;

    public Cards(DBService dbServ) {
        this.mCards = new HashMap<>();
        this.site =new wwwService();
        this.dbService = dbServ;
        logger.info("Cards: new wwwService");
    }

    public void add(ArrayList<Card> cards) {

    }

    public void add(Card card){

    }

    public void load(String razd) throws DBException {
        this.mCards.putAll(site.get(razd));
        logger.info("END of site.get.");
        dbService.updateDB(this.mCards);
    }
    public void loadSerialCards(Path file) throws DBException {
        this.mCards.putAll((new fileService()).getSerialCards(file));
        logger.info("END of cards.txt.");
        dbService.updateDB(this.mCards);
    }

    public void load(Path file) throws DBException, InterruptedException {
//        DBService dbService = new DBService();
//        dbService.printConnectInfo();

        this.mCards.put ("razd2",new Card("razd2","Головной Банк","root",0L,true,""));
        this.mCards.put ("razd154",new Card("razd154","Филиальная сеть","root",0L, true,""));
        this.mCards.putAll ((new fileService()).get(file));
        logger.info("Get " + mCards.size() + " cards");

        //добавить отчество к name wwwData.getO()
        int count = 0;
//        long t = System.nanoTime();
        for (Map.Entry<String, Card> entry : mCards.entrySet()){
            Card c = entry.getValue();
            if (!c.isParent()) {
                ThreadGetO thr = new ThreadGetO(site.getHttpclient(),c, "threadO"+count);
                ThreadGetO.threads.add(thr);
                thr.start();
            }
            count++;
        }
//        count=0;
        for (ThreadGetO th: ThreadGetO.threads){
            th.join();
        }
        System.out.println("End of update FIO.");
        dbService.cleanUp();
        Integer dbcount = dbService.updateDB(mCards);
        logger.info("Get " + dbcount + " cards");
    }

    public Card getcard(String idr){
        return mCards.get(idr);
    }

    @Override
    public String toString() {
        StringBuilder sCards = new StringBuilder();
        for (Map.Entry<String, Card> entry : mCards.entrySet()) {
            sCards.append(entry.getValue().toString()).append("\r\n");
        }
        return sCards.toString();
    }

    public String toCSV(Character del) {
        StringBuilder sCards = new StringBuilder();
        sCards.append("id").append(del).
                append("parent").append(del).
                append("name").append(del).
                append("phone").append(del).
                append("mobile").append(del).
                append("tabnum").append(del).
                append("avatar").append(del).
                append("grade").append(del).append("\r\n");
        for (Map.Entry<String, Card> entry : mCards.entrySet()) {
            sCards.append(entry.getValue().toCSV(del)).append("\r\n");
        }
        return sCards.toString();
    }

    public String toCSV() {
        return toCSV(';');
    }
}
