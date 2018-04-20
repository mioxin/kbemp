package com.gmail.mrmioxin.kbemp.dbService.dao;

import com.gmail.mrmioxin.kbemp.Card;
import com.gmail.mrmioxin.kbemp.IDao;
import com.gmail.mrmioxin.kbemp.dbService.dataSets.UsersDataSet;
import com.gmail.mrmioxin.kbemp.dbService.executor.Executor;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author v.chibrikov
 *         <p>
 *         Пример кода для курса на https://stepic.org/
 *         <p>
 *         Описание курса и лицензия: https://github.com/vitaly-chibrikov/stepic_java_webserver
 */
public class UsersDAO  implements IDao {
    private static final String TABLE = "users";
    private Executor executor;

    public UsersDAO(Connection connection) {
        this.executor = new Executor(connection);
    }

    public Card get(long id) throws SQLException {
        return executor.execQuery("select * from " + TABLE + " where id=" + id + " and deleted=FALSE", result -> {
            if (result.isLast()) {
                return null;
            }
            result.next();
            return new UsersDataSet(result.getLong(1), result.getDate(2), result.getString(3), result.getString(4),result.getLong(5),
                    result.getString(6), result.getString(7), result.getInt(8), result.getString(9), result.getString(10),
                    result.getBoolean(11), result.getString(12));
        });
    }

    @Override
    public long getId(String tabnum) throws SQLException {
        String q = "select * from " + TABLE + " where tabnum=" + tabnum + " and deleted=FALSE";
        return executor.execQuery(q, result -> {
            if (result.isLast()) {
                return null;
            }
            result.next();
            return result.getLong(1);
        });
    }

    public void delete(long id) throws SQLException {
        executor.execUpdate("update " + TABLE + " set deleted = TRUE where id = " + id);
    }

    public void  setparentId(long id, long pid) throws  SQLException {
        executor.execUpdate("update " + TABLE + " set parentid = " + pid + " where id = " + id +" and deleted = false");
    }

    public void  setLdate(long id, Date ldate) throws  SQLException{
        executor.execUpdate("update " + TABLE + " set ldate = " + ldate.toString() + " where id = " + id +" and deleted = false");
    }


    public long CountNoPid() throws SQLException {
        return executor.execQuery("select count(id) from " + TABLE + " where  parentid is null and deleted=FALSE", result -> {
            result.next();
            return result.getLong(1);
        });
    }

    public void insert(Card card, String hist) throws SQLException {
        Map<String,String> mapcard = card.toMap();
        String date = new Date(System.currentTimeMillis()).toString();

        executor.execUpdate("insert into " + TABLE + " (date,ldate,name,parent,parentid,phone,mobile,tabnum,grade,avatar,deleted,history) "+
                "values ('" + 
                date + "','" +
                date + "','" +
                mapcard.get("name") + "','" +
                mapcard.get("parent") + "'," +
                mapcard.get("parentid") + ",'" +
                mapcard.get("phone") + "','" +
                mapcard.get("mobile") + "'," +
                mapcard.get("tabnum") + ",'" +
                mapcard.get("grade") + "','" +
                mapcard.get("avatar") + "'," +
                "FALSE,'" +
                hist + "')");
    }


    public void createTable() throws SQLException {
        executor.execUpdate("create table if not exists " + TABLE +
                " (id bigint auto_increment, " +
                "date DATE," +
                "ldate DATE," +
                "name   varchar(256), " +
                "parent varchar(256), " +
                "phone  varchar(256), " +
                "mobile varchar(256), " +
                "tabnum int, " +
                "grade  varchar(256), " +
                "avatar varchar(256), " +
                "deleted boolean, " +
                "history varchar(1024), " +
                "parentid bigint, " +
                "primary key (id))");
        executor.execUpdate("create index if not exists name on users(name)");
        executor.execUpdate("create index if not exists tabnum on users(tabnum)");
        executor.execUpdate("create index if not exists mobile on users(mobile)");
        //("create table if not exists users (id bigint auto_increment, user_name varchar(256), primary key (id))");
    }

    public void dropTable() throws SQLException {
        executor.execUpdate("drop table if exists " + TABLE);
    }

}
