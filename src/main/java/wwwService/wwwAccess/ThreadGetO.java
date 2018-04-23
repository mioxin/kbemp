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
import java.util.regex.Pattern;

/**
 * Created by palchuk on 19.04.2018.
 */
public class ThreadGetO extends  ThreadGet {
    public static List<ThreadGetO> threads = new ArrayList<>();

    public  ThreadGetO(CloseableHttpClient site, Card c, String nameThread){
        super(site,c,nameThread);
        URI uri = null;
        try {
            uri = new URI(BaseConst.FIOADDR + URLEncoder.encode(c.getName(), "UTF-8") +"&type=1");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.httpget.setURI(uri);
    }

    @Override
    public void run(){
//        Pattern p_vn = Pattern.compile("<b>(-?\\d{2,4}-?\\d{0,2}-?\\d{0,2})<\\/b>");
        Pattern p_tab = Pattern.compile("opencard\\(\\'(-?\\d{3,})\\',");//tabnum
//        Pattern p_sn = Pattern.compile("<span.+</span>.* -?(\\S+)</td>");//отчество //
        Pattern p_sn = Pattern.compile("(<span( \\S+){1,}>){0,1}"+ card.getName().split(" ")[0] + "( \\S+){0,1}( \\S+){0,1}</td>");//( \S+){0,1}( \S+){0,1}</td>
        Pattern p_wordsonly = Pattern.compile("([А-Яа-я]+)',");
//        System.out.println("START ["+name+ "] thread");
        String newname = null;
//        long t = System.nanoTime();

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
                            e.printStackTrace();
                        }
                        //удаляем пустые строки
                        aResponse.removeAll(Arrays.asList("", " "));
                        if (aResponse.size() == 0) {
                            System.out.println(card.getName() + ". Response: " + aResponse);
                            return null;
                        }
                        if (aResponse.size() > 1) {
                            for (String s : aResponse) {
                                if (findPattern(p_tab, s, 1).equals(card.getTabnum().toString())) {
                                    fio = card.getName() + findPattern(p_sn, s, 4);
                                }
                            }
                        } else {
                            fio = card.getName() + findPattern(p_sn, aResponse.get(0),4);
                        }
                    }
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
                return fio;

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
//        assert newname != null;
        if (!newname.isEmpty() && (newname != null)) {
            card.setname(newname);
        }else {
            System.out.println(card.getName() + ". Middle name: " + newname);
        }
        System.out.print("\rEND ["+ thrName + "] thread. Count: " + count++ +" ");
    }

}
