package com.gmail.mrmioxin.kbemp.fileService;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
}
