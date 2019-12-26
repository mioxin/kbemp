package com.gmail.mrmioxin.kbemp;

import java.util.logging.Logger;

/**
 * Created by palchuk on 31.01.2018.
 */
public final class BaseConst {
    public static final Logger logg = Logger.getLogger(Main.class.getName());
    public static final String WWWINT="http://www-int";
    public static final String PROXY="http://proxy-all.hq.bc";

    //получить разделы
    public static final String BASEADDR="http://www-int/modules/sotr_view/give_me_deps.php?id=";
    // поиск по имени или телефону для получения отчества
    public static final String FIOADDR="http://www-int/modules/sotr_view/give_me_search_list.php?search=";
                                        //"http://www-int/modules/sotr_view/give_me_search_list.php?search=&type=1"
    //regexp разделитель для разбиения результата (предыдущего поиска) в случае нескольких совпадений
    public static final String SEARCHDELIM0="<div class=sotr_td3 onclick=\"searchG\\(";
    //regexp разделитель для разбиения результата поиска (при добывлении "&type=1") в случае нескольких совпадений
    public static final String SEARCHDELIM1="<div class=sotr_td3 onclick=\" Search = 1;";
}
