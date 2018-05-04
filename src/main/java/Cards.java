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
        logger.fine("Cards: new wwwService");
    }

    public void add(ArrayList<Card> cards) {

    }

    public void add(Card card){

    }

    public void load(String razd) throws DBException {
//        DBService dbService = new DBService();
//        dbService.printConnectInfo();

//      public Card(String idr, String name, String parent, Long pid, Boolean hasChild, String parentname) {
//        this.mCards.put(razd,new Card("#","Корневой","root",0L,true,"//"));

        this.mCards.putAll(site.get(razd));
        dbService.updateDB(this.mCards);

    }

    public void load(Path file) throws DBException, InterruptedException {
//        DBService dbService = new DBService();
//        dbService.printConnectInfo();

        this.mCards.put ("razd2",new Card("razd2","Головной Банк","root",0L,true,""));
        this.mCards.put ("razd154",new Card("razd154","Филиальная сеть","root",0L, true,""));
        this.mCards.putAll ((new fileService()).get(file));
        logger.fine("Get " + mCards.size() + " cards");

        //добавить отчество к name wwwData.getO()
        int count = 0;
//        long t = System.nanoTime();
        for (Map.Entry<String, Card> entry : mCards.entrySet()){
            Card c = entry.getValue();
            if (!c.isParent()) {
                ThreadGetO thr = new ThreadGetO(site.getHttpclient(),c, "threadO"+count);
                ThreadGetO.threads.add(thr);
                thr.start();
//                thr.join();
//                System.out.println(thr.toString());
//                ArrayList<String> nph = c.getNamePhone();
//                try {
//                    c.setname(site.getApidata().getO(nph));
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
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
        logger.fine("Get " + dbcount + " cards");
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
