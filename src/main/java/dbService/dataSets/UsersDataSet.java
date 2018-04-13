package com.gmail.mrmioxin.kbemp.dbService.dataSets;

import com.gmail.mrmioxin.kbemp.Card;
import com.google.gson.JsonObject;

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
public class UsersDataSet extends Card {
    private Long id;
    private Date date;
    private Date ldate;
    private Boolean deleted;
    private String history;

    public UsersDataSet(){
        this.date = new Date(System.currentTimeMillis());
        this.ldate = this.date;
        this.deleted = false;
    }

    public UsersDataSet(Long id, Date date, String name,String parent, Long pid,String phone, String mobile, Integer tabnum,
                        String grade, String avatar, Boolean deleted, String history) {
                        //date,name,parent,phone,mobile,tabnum,grade,avatar,deleted,history
        this.id = id;
        this.date = date;
        this.parent = parent;
        this.parentid = pid;
        this.name = name;
        this.phone = phone;
        this.mobile = mobile;
        this.tabnum = tabnum;
        this.avatar = avatar;
        this.grade = grade;
        this.deleted = false;
        this.history = history;
    }

    public void addHistory(String hist) {
        this.history += hist;
    }

    @Override
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
