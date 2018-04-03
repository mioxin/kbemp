package com.gmail.mrmioxin.kbemp.fileService;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by palchuk on 22.02.2018.
 */
public class fileService {
    private static Logger logger;

    public fileService() {
        this.logger = BaseConst.logg;
    }

    public ArrayList<Card> get(Path file){
        ArrayList<Card> arCards = new ArrayList<>();
        String str;
        try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))){
            while((str = br.readLine()) != null) {
                if (str.indexOf("GET http")<0 && str.indexOf("(anonymous) @")<0) {
                    arCards.add(new Card(str));
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return arCards;
    }
}
