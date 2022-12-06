package com.gmail.mrmioxin.kbemp.wwwService.wwwAccess;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Created by palchuk on 19.04.2018.
 */
public class ThreadGetO extends ThreadGet {
    public static List<ThreadGetO> threads = new ArrayList<>();
    protected static Integer count = 0;

    public ThreadGetO(CloseableHttpClient site, Card c, String nameThread) {
        super(site, c, nameThread);
        URI uri = null;
        try {
            uri = new URI(BaseConst.FIOADDR + URLEncoder.encode(c.getName(), "UTF-8") + "&type=1");
        } catch (URISyntaxException e) {
            logger.severe("Get URI fot Img error: " +e );
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            logger.severe("Get URI fot Img error: " +e );
            e.printStackTrace();
        }
        this.httpget.setURI(uri);
    }

    @Override
    public void run() {
        // <td class="s_3"><span class="s_3">вн</span> <b>405-62-1</b></td>
        Pattern p_vn = Pattern.compile("<b>(-?\\d{2,4}-?\\d{0,2}-?\\d{0,2})<\\/b>");//вн телефон
        Pattern p_tab = Pattern.compile("opencard\\(\\'(-?\\d{3,})\\',");// tabnum
        // Pattern p_sn = Pattern.compile("<span.+</span>.* -?(\\S+)</td>");//отчество
        // //
        Pattern p_sn = Pattern
                .compile("(<span( \\S+){1,}>){0,1}" + card.getName().split(" ")[0] + "( \\S+){0,1}( \\S+){0,2}<\\/td>");// (
                                                                                                                      // \S+){0,1}(
                                                                                                                      // \S+){0,1}</td>
        //Pattern p_wordsonly = Pattern.compile("([А-Яа-я]+)',");
        // System.out.println("START ["+name+ "] thread");
        String newname = null;
        // long t = System.nanoTime();

        try {
            newname = httpClient.execute(httpget, response -> {
                String fio = "";
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    if (entity == null) {
                        return null;
                    } else {
                        List<String> aResponse = new ArrayList<>();
                        try {
                            Collections.addAll(aResponse, EntityUtils.toString(entity).split(BaseConst.SEARCHDELIM1));
                        } catch (IOException e) {
                            logger.severe("Add response to list error: " +e );
                            e.printStackTrace();
                        }
                        // удаляем пустые строки
                        aResponse.removeAll(Arrays.asList("", " "));
                        if (aResponse.size() == 0) {
                            logger.warning(card.getName() + ". Response: " + aResponse);
                            return null;
                        }
                        if (aResponse.size() > 1) {
                            for (String s : aResponse) {
                                //if (findPattern(p_vn, s, 1).equals(card.getPhone().toString())) {
                                if (findPattern(p_tab, s, 1).equals(card.getTabnum().toString())) {
                                    fio = findPattern(p_sn, s, 4).trim();//отчество
                                }
                            }
                        } else {
                            fio = findPattern(p_sn, aResponse.get(0), 4).trim();//отчество
                        }
                        if (fio.equals("")) {//если отчество не найдено
                            fio = card.getName().trim();
                            logger.log(Level.WARNING,"fio={0}: отчество не найдено. \r\n>>>>>>>>>>>>>>>\r\n{1}\r\n>>>>>>>>>>>>>>>>\r\n", 
                                    new String[] {fio, aResponse.toString()});
                        } else {
                            fio = card.getName().trim() + " " + fio;
                        }
                    }
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
                return fio;

            });
        } catch (IOException e) {
            logger.severe("HTTPclient ThreadGetO ["+thrName+"] error: " +e );
            e.printStackTrace();
        }
        logger.info(card.getName() + ". Middle name: " + newname);
        // assert newname != null;
        if (!newname.isEmpty() && (newname != null)) {
            card.setname(newname);
        } 
        logger.info("END [" + thrName + "] thread. Count: " + count++);
    }

}
