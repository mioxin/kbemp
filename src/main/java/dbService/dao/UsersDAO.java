package com.gmail.mrmioxin.kbemp.dbService.dao;

import com.gmail.mrmioxin.kbemp.Card;
import com.gmail.mrmioxin.kbemp.dbService.DBException;
import com.gmail.mrmioxin.kbemp.dbService.dataSets.UsersDataSet;
import com.gmail.mrmioxin.kbemp.dbService.executor.Executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * @author v.chibrikov
 *         <p>
 *         Пример кода для курса на https://stepic.org/
 *         <p>
 *         Описание курса и лицензия: https://github.com/vitaly-chibrikov/stepic_java_webserver
 */
public class UsersDAO {

    private Executor executor;

    public UsersDAO(Connection connection) {
        this.executor = new Executor(connection);
    }

    public UsersDataSet get(long id) throws SQLException {
        return executor.execQuery("select * from users where id=" + id, result -> {
            if (result.isLast()) {
                return null;
            }
            result.next();
            return new UsersDataSet(result.getLong(1), result.getDate(2), result.getString(3), result.getString(4),
                    result.getString(5), result.getString(6), result.getInt(7), result.getString(8), result.getString(9),
                    result.getBoolean(10), result.getString(11));
        });
    }

    public Long getUserId(String tabnum) throws SQLException {
        String q = "select * from users where tabnum=" + tabnum;
        return executor.execQuery(q, result -> {
            if (result.isLast()) {
                return null;
            }
            result.next();
            return result.getLong(1);
        });
    }

    public void insertUser(UsersDataSet card) throws SQLException {
        Long oldid;
        UsersDataSet oldcard;
        String hist="";
        Map<String,String> mapcard = card.toMap();
        if ((oldid = getUserId(mapcard.get("tabnum"))) >0) { //карточка уже есть
            oldcard = get(oldid);
            hist = card.compareCard(oldcard);
            if (hist == "") { //изменений нет
                throw new SQLException(mapcard.get("tabnum").toString()+". "+mapcard.get("name")+": Карточка не изменилась.");
            } else {//изменяем старую
                executor.execUpdate("update users set deleted = TRUE where id = " + oldid.toString());
            }
        }

        executor.execUpdate("insert into users (date,name,parent,phone,mobile,tabnum,grade,avatar,deleted,history) "+
                "values ('" + 
                new Date(System.currentTimeMillis()).toString() + "','" +
                mapcard.get("name") + "','" +
                mapcard.get("parent") + "','" +
                mapcard.get("phone") + "','" +
                mapcard.get("mobile") + "'," +
                mapcard.get("tabnum") + ",'" +
                mapcard.get("grade") + "','" +
                mapcard.get("avatar") + "','" +
                "FALSE,'" +
                hist + "')");
    }


    public void createTable() throws SQLException {
        executor.execUpdate("create table if not exists users " +
                "(id bigint auto_increment, " +
                "date DATE," +
                "name   varchar(256), " +
                "parent varchar(256), " +
                "phone  varchar(256), " +
                "mobile varchar(256), " +
                "tabnum int, " +
                "grade  varchar(256), " +
                "avatar varchar(256), " +
                "deleted boolean, " +
                "history varchar(1024), " +
                "primary key (id)," +
                "key (name)," +
                "key (tabnum)," +
                "key (phone)");
        //("create table if not exists users (id bigint auto_increment, user_name varchar(256), primary key (id))");
    }

    public void dropTable() throws SQLException {
        executor.execUpdate("drop table users");
    }

}
