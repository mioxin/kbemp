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
public class UsersDataSet extends Card {
    private Long id;
    private Date ldate;
    private Boolean deleted;
    private String history;

    public UsersDataSet() {
        this.ldate = this.date;
        this.deleted = false;
    }

    public UsersDataSet(Long id, Date date, String name, String parent, String phone, String mobile, String email,
            Integer tabnum, String grade, String avatar, Boolean deleted, String history, Long pid) {
        this.id = id;
        this.date = date;
        this.ldate = date;
        this.name = name;
        this.parent = parent;
        this.phone = phone;
        this.mobile = mobile;
        this.email = email;
        this.tabnum = tabnum;
        this.grade = grade;
        this.avatar = avatar;
        this.deleted = false;
        this.history = history;
        this.parentid = pid;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
