package com.gmail.mrmioxin.kbemp.fileService;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;
//import java.io.
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by palchuk on 22.02.2018.
 */
public class fileService {
    private static Logger logger;

    public fileService() {
        this.logger = BaseConst.logg;
    }

    public Map<String,Card> get(Path file){
        Map<String,Card> mCards = new HashMap<>();
        String str;
        int count = 1;
        try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))){
            long t = System.nanoTime();
            while((str = br.readLine()) != null) {
                try {
                    if (str.indexOf("GET http")<0 && str.indexOf("(anonymous) @")<0) {
                        Card card = new Card(str);
                        mCards.put(card.getidr(),card);
                }
                System.out.print("\rCount: " + count++ + ". Time: " + (System.nanoTime()-t)/(count-1) + " ns. " + mCards.get("razd66").getparent());
                }catch (IOException er){
                    er.printStackTrace();
                }
            }
            System.out.println("");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mCards;
    }

    public Map<String,Card> getSerialCards(Path file){
        Map<String,Card> mCards = new HashMap<>();
        String str;
        int count = 1;
        //int ch = 0;
        //int[] ar_str;
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file.toFile()),"UTF8"))){
            long t = System.nanoTime();
            while((str = br.readLine()) != null) {
                String[] a_str = str.split("\\=Card\\{");
                a_str[1] = a_str[1].replaceAll("[\\{\\}]", "");
                //a_str[1] = a_str[1].replaceAll("(grade='.*),(.*')$", "$1$2").replaceAll("(name='.*?),(.*?', p)","$1$2");
                Map<String, String> CardMap = Arrays.stream(a_str[1].split(";"))
                    .map(s -> s.split("="))
                    .collect(Collectors.toMap(s -> s[0].trim(), s -> s[1].trim().replaceAll("'", "")));
                Card card = new Card(CardMap);
                mCards.put(a_str[0],card);
            
                System.out.print("\rCount: " + count++ + ". Time: " + (System.nanoTime()-t)/(count-1) + " ns.; " + a_str[0] +" / "+ mCards.get(a_str[0]).getparent());
            }
            System.out.println("");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mCards;
    }
}
