package com.gmail.mrmioxin.kbemp;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by palchuk on 10.01.2018.
 */

public class Main {
    public static Cards cards;
    private static Logger logg;

    public static void main(String... args) throws Exception {
        logg = BaseConst.logg;
        logg.addHandler(new ConsoleHandler());
        logg.setUseParentHandlers(false);
        logg.getHandlers()[0].setLevel(Level.FINE);
        logg.setLevel(Level.FINE);
        Path filecsv = Paths.get("data.txt");

        cards = new Cards();
        cards.load("razd339");
//        cards.load(filecsv);
//        System.out.println(cards.toString());
    }


}
