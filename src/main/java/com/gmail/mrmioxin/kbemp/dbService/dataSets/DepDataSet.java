package com.gmail.mrmioxin.kbemp.dbService.dataSets;

import com.gmail.mrmioxin.kbemp.Card;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author v.chibrikov
 *         <p>
 *         Пример кода для курса на https://stepic.org/
 *         <p>
 *         Описание курса и лицензия: https://github.com/vitaly-chibrikov/stepic_java_webserver
 */
@SuppressWarnings("UnusedDeclaration")
public class DepDataSet extends Card {
    private Long id;
    private Date date;
    private Boolean deleted;
    private String history;

    public DepDataSet(){
        this.date = new Date(System.currentTimeMillis());
        this.deleted = false;
    }

    public DepDataSet(Long id, Date date, String idr, String name, String parent, long pid, Boolean deleted, String history, String parentname) {
//    public Card(String idr, String name, String parent, Long pid, Boolean hasChild, String parentname) {
        super(idr,name,parent,pid,true,parentname);
        this.id = id;
//        this.idr = idr;
//        this.parent = parent;
//        this.parentid = pid;
//        this.name = name;
//        this.parentname = parentname;
        this.date =  date;
        this.deleted = deleted;
        this.history = history;
    }

    public void addHistory(String hist) {
        this.history += hist;
    }

    public Map<String,String> toMap(){
        HashMap<String,String> map = (HashMap) super.toMap();
        map.put("date", date.toString());
        if (deleted) {
            map.put("deleted","true");
        } else {
            map.put("deleted","false");
        }
        map.put("history",history);
        return map;
    }
}
