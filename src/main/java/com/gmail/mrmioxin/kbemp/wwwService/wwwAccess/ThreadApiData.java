package com.gmail.mrmioxin.kbemp.wwwService.wwwAccess;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.sound.sampled.SourceDataLine;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;
import com.gmail.mrmioxin.kbemp.wwwService.wwwService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ThreadApiData extends ThreadGet{

    //private List<String> atmpDeps;
    //private Map<String, Card> mCards;
    private wwwService wwwServ;


    public ThreadApiData(wwwService wwwServ, String str_url, String nameThread) {
        super( null, nameThread);
        this.wwwServ = wwwServ;
        try {
            this.httpget.setURI(new URI(BaseConst.BASEADDR + str_url));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        ArrayList<Card> atmpCards = new ArrayList<>(); // временная все карточки

        try {
            atmpCards = httpClient.execute(httpget, response -> {
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
                    logger.severe("Don't get data from WWW in " + this.thrName + "\n" + httpget.getURI().toString() + "\n" + response.toString());
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
                return aCards;
            });

            for (Card c : atmpCards) {
                if (c.isParent()) {
                    if (c.getidr().startsWith("razd")) {
                        wwwServ.addDeps(c.getidr());
                        logger.info("atmpDeps.add: " + c.toString());
                        System.out.print("+");
                    } else {
                        logger.info(c.getidr() + " not add in atmpDep.");
                    }
                }
            }
            for (Card c : atmpCards){
                wwwServ.mCards.put(c.getidr(),c);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            atmpCards.clear();
        }
        //ThreadApiData.threads.remove();
        logger.info("END [" + thrName + "] task.") ;
    }
    
}
