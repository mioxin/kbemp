package com.gmail.mrmioxin.kbemp.dbService.dao;

import com.gmail.mrmioxin.kbemp.Card;
import com.gmail.mrmioxin.kbemp.IDao;
import com.gmail.mrmioxin.kbemp.dbService.dataSets.UsersDataSet;
import com.gmail.mrmioxin.kbemp.dbService.executor.Executor;

import org.h2.jdbc.JdbcSQLException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 
 *         
 */
public class UsersDAO  implements IDao {
    private static final String TABLE = "users";
    private Executor executor;

    public UsersDAO(Connection connection) {
        this.executor = new Executor(connection);
    }

    public Card get(long id) throws SQLException {
        return executor.execQuery(new StringBuilder().append("select * from ").append(TABLE).
                                                    append(" where id=").append(id).append(" and deleted=FALSE").toString(), result -> {
            if (result.isLast()) {
                return null;
            }
            result.next();
            return new UsersDataSet(result.getLong(1), //id
                                    result.getDate(3), //ldate
                                    result.getString(4),//name
                                    result.getString(5),//parent
                                    result.getString(6),//phone
                                    result.getString(7), //mobile
                                    result.getString(8), //email
                                    result.getInt(9), //tabnum
                                    result.getString(10), //grade
                                    result.getString(11),//avatar
                                    result.getBoolean(12), //deleted
                                    result.getString(13),//history
                                    result.getLong(14));//parentid
        });
    }

    @Override
    public long getId(String tabnum) throws SQLException {
        String q = new StringBuilder().append("select * from ").append(TABLE).
                                        append(" where tabnum=").append(tabnum).append(" and deleted=FALSE").toString();
        //"select * from " + TABLE + " where tabnum=" + tabnum + " and deleted=FALSE";
        return executor.execQuery(q, result -> {
            if (result.isLast()) {
                return null;
            }
            result.next();
            return result.getLong(1);
        });
    }

    @Override
    public long getId(String name, String pname) throws SQLException {
        String q = new StringBuilder().append("select * from ").append(TABLE).
                                        append(" where parent='").append(pname).append("' and name='").append(name).append("' and deleted=FALSE").toString();
        //"select * from " + TABLE + " where name='" + name + "' and pid=" + Long.toString(pid) + " and deleted=FALSE";
        return executor.execQuery(q, result -> {
            if (result.isLast()) {
                return null;
            }
            result.next();
            return result.getLong(1);
        });
    }

    @Override
    public List<Card> getByField(String field, String val, Boolean isDel) throws SQLException {
        String q = new StringBuilder().append("select * from ").append(TABLE).
                                    append(" where ").append(field).append("='").append(val).append("' and deleted=").append(isDel?"true":"false").
                                    append(" order by ldate").toString();
        //"select * from " + TABLE + " where " + field + "='" + val + "' and deleted=" + isDel;
        List<Card> lCards = new ArrayList<>();
        ResultSet rs =  executor.execQuery(q, result -> {
            if (result.isLast()) {
                return null;
            }
            return result;
        });
        if (rs == null) {
            return null;
        }
        while (!rs.isLast()){
            rs.next();
            lCards.add(new UsersDataSet(rs.getLong(1), //id
                                        rs.getDate(3), //date
                                        rs.getString(4),//name
                                        rs.getString(5),//parent
                                        rs.getString(6),//phone
                                        rs.getString(7), //mobile
                                        rs.getString(8), //email
                                        rs.getInt(9), //tabnum
                                        rs.getString(10), //grade
                                        rs.getString(11),//avatar
                                        rs.getBoolean(12), //deleted
                                        rs.getString(13),//history
                                        rs.getLong(14))//parentid
            );
        }
        return lCards;
    }

    @Override
    public List<Card> getByField(String field, Long val, Boolean isDel) throws SQLException {
        //String q = "select * from " + TABLE + " where " + field + "=" + Long.toString(val) + " and deleted=" + isDel;
        String q = new StringBuilder().append("select * from ").append(TABLE).
                                    append(" where ").append(field).append("=").append(val).append(" and deleted=").append(isDel?"true":"false").
                                    append(" order by ldate").toString();

        List<Card> lCards = new ArrayList<>();
        ResultSet rs =  executor.execQuery(q, result -> {
            if (result.isLast()) {
                return null;
            }
            return result;
        });
        if (rs == null) {
            return null;
        }
        while (!rs.isLast()){
            rs.next();
            lCards.add(new UsersDataSet(rs.getLong(1), //id
                                        rs.getDate(3), //date
                                        rs.getString(4),//name
                                        rs.getString(5),//parent
                                        rs.getString(6),//phone
                                        rs.getString(7), //mobile
                                        rs.getString(8), //email
                                        rs.getInt(9), //tabnum
                                        rs.getString(10), //grade
                                        rs.getString(11),//avatar
                                        rs.getBoolean(12), //deleted
                                        rs.getString(13),//history
                                        rs.getLong(14))//parentid
            );
        }
        return lCards;
    }

    @Override
    public long getIdByField(String field, String val, Boolean isDel) throws SQLException,JdbcSQLException {
        String q = new StringBuilder().append("select * from ").append(TABLE).
                                    append(" where ").append(field).append("='").append(val).append("' and deleted=").append(isDel?"true":"false").
                                    append(" order by ldate").toString();
        return executor.execQuery(q, result -> {
            if (result.isLast()) {
                return null;
            }
            //result.next();
            result.last();
            return result.getLong(1);
        });
    }

    public void delete(long id) throws SQLException {
        executor.execUpdate(new StringBuilder("update ").append(TABLE).append(" set deleted = TRUE where id = ").append(id).toString());
    }

    public void  setparentId(long id, long pid) throws  SQLException {
        executor.execUpdate(new StringBuilder("update ").append(TABLE).append(" set parentid = ").append(pid).
                                                        append(" where id = ").append(id).append(" and deleted = false").toString());
    }

    public void  setLdate(long id, Date ldate) throws  SQLException{
        executor.execUpdate(new StringBuilder("update ").append(TABLE).append(" set ldate = '").append(ldate.toString()).
                                                        append("' where id = ").append(id).append(" and deleted = false").toString());
    }


    public long CountNoPid() throws SQLException {
        return executor.execQuery(new StringBuilder("select count(id) from ").append(TABLE).append(" where  parentid is null and deleted=FALSE").toString(), 
            result -> {
                result.next();
                return result.getLong(1);
        });
    }

    public void insert(Card card, String hist) throws SQLException {
        Map<String,String> mapcard = card.toMap();
        String date = new Date(System.currentTimeMillis()).toString();

        executor.execUpdate(new StringBuilder("insert into ").append(TABLE).append(" (date,ldate,name,parent,phone,mobile,email,tabnum,grade,avatar,deleted,history,parentid) ").
                append("values ('"). 
                append(date).append("','").
                append(date).append("','").
                append(mapcard.get("name")).append("','").
                append(mapcard.get("parent")).append("','").
                append(mapcard.get("phone")).append("','").
                append(mapcard.get("mobile")).append("','").
                append(mapcard.get("email")).append("',").
                append(mapcard.get("tabnum")).append(",'").
                append(mapcard.get("grade")).append("','").
                append(mapcard.get("avatar")).append("',").
                append("FALSE,'").
                append(hist).append("',").
                append(mapcard.get("parentid")).
                append(")").toString());
    }

    public void insert(UsersDataSet card, String hist) throws SQLException {
        Map<String,String> mapcard = card.toMap();
        String date = new Date(System.currentTimeMillis()).toString();

        executor.execUpdate(new StringBuilder("insert into ").append(TABLE).append(" (date,ldate,name,parent,phone,mobile,email,tabnum,grade,avatar,deleted,history,parentid) ").
                append("values ('"). 
                append(mapcard.get("date")).append("','").
                append(date).append("','").
                append(mapcard.get("name")).append("','").
                append(mapcard.get("parent")).append("','").
                append(mapcard.get("phone")).append("','").
                append(mapcard.get("mobile")).append("','").
                append(mapcard.get("email")).append("',").
                append(mapcard.get("tabnum")).append(",'").
                append(mapcard.get("grade")).append("','").
                append(mapcard.get("avatar")).append("',").
                append("FALSE,'").
                append(hist).append("',").
                append(mapcard.get("parentid")).
                append(")").toString());
    }

    public void update(long id, String field, String val) throws SQLException {
        executor.execUpdate(new StringBuilder("UPDATE ").append(TABLE).
                                                        append(" SET ").
                                                        append(field).append("='").append(val).
                                                        append("' WHERE ID=").append(id).toString());
    }

    public void createTable() throws SQLException {
        executor.execUpdate(new StringBuilder("create table if not exists ").append(TABLE).
                append(" (id bigint auto_increment, ").
                append("date DATE,").
                append("ldate DATE,").
                append("name   varchar(256), ").
                append("parent varchar(256), ").
                append("phone  varchar(256), ").
                append("mobile varchar(256), ").
                append("email varchar(256), ").
                append("tabnum int, ").
                append("grade  varchar(256), ").
                append("avatar varchar(256), ").
                append("deleted boolean, ").
                append("history varchar(1024), ").
                append("parentid bigint, ").
                append("primary key (id), ").
                append("index (name,email,mobile, tabnum))").toString());
        //executor.execUpdate("create index if not exists name on users(name)");
        //executor.execUpdate("create index if not exists tabnum on users(email)");
        //executor.execUpdate("create index if not exists mobile on users(mobile)");
        //("create table if not exists users (id bigint auto_increment, user_name varchar(256), primary key (id))");
    }

    public void dropTable() throws SQLException {
        executor.execUpdate("drop table if exists " + TABLE);
    }

    public Integer delOldUsers() throws SQLException {
        String ldate = new Date(System.currentTimeMillis()).toString();
        return executor.execUpdate("update users set deleted = 1 where ldate < '"+ ldate +"' and deleted = 0");
    };


}
