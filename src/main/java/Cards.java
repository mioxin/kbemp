package com.gmail.mrmioxin.kbemp;

import com.gmail.mrmioxin.kbemp.dbService.DBException;
import com.gmail.mrmioxin.kbemp.dbService.DBService;
import com.gmail.mrmioxin.kbemp.fileService.fileService;
import com.gmail.mrmioxin.kbemp.wwwService.wwwService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by palchuk on 31.01.2018.
 */
public class Cards {
    private ArrayList<Card> arCards;
    //private Site site;
    private static Logger logger;

    public Cards() {
        this.arCards = new ArrayList<>();
        this.logger = BaseConst.logg;
    }

    public void add(ArrayList<Card> cards) {
        this.arCards.addAll(cards);
    }

    public void add(Card card){
        arCards.add(card);
    }

    public void load(String razd) throws DBException {
        DBService dbService = new DBService();
        dbService.printConnectInfo();

        this.arCards = (new wwwService()).get(razd);
        dbService.updateDB(arCards);

    }

    public void load(Path file) throws DBException {
        DBService dbService = new DBService();
        dbService.printConnectInfo();

        this.arCards = (new fileService()).get(file);
        logger.fine("Get " + arCards.size() + " cards");

        Integer dbcount = dbService.updateDB(arCards);
        logger.fine("Get " + dbcount + " cards");


    }

    @Override
    public String toString() {
        StringBuilder sCards = new StringBuilder();
        for (Card c :arCards) {
            sCards.append(c.toString()).append("\r\n");
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
        for (Card c :arCards) {
            sCards.append(c.toCSV(del)).append("\r\n");
        }
        return sCards.toString();
    }

    public String toCSV() {
        return toCSV(';');
    }
}
