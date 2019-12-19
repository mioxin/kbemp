package com.gmail.mrmioxin.kbemp.dbService.dao;

import com.gmail.mrmioxin.kbemp.Card;
import com.gmail.mrmioxin.kbemp.IDao;
import com.gmail.mrmioxin.kbemp.dbService.dataSets.DepDataSet;
import com.gmail.mrmioxin.kbemp.dbService.executor.Executor;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Map;

public class DepDAO implements IDao {
    private static final String TABLE = "deps";

    private Executor executor;

    public DepDAO(Connection connection) {
        this.executor = new Executor(connection);
    }

    @Override
    public Card get(long id) throws SQLException {
        return executor.execQuery("select * from " + TABLE + " where id=" + id + " and deleted=FALSE", result -> {
            result.next();
            return new DepDataSet(result.getLong(1), result.getDate(2), result.getString(3),result.getString(4),
                    result.getString(5),result.getLong(6),result.getBoolean(7),result.getString(8), result.getString(9));
        });
    }

    @Override
    public long getId(String name, String parentname) throws SQLException {
        String pnameCampare= " and parentname='"+parentname + "'";
        if (parentname.equals("")) {
            pnameCampare= " and parentname is null";
        }
        return executor.execQuery("select * from " + TABLE + " where name='" + name + "'" + pnameCampare +" and deleted=FALSE", result -> {
            result.next();
            return result.getLong(1);
        });
    }

    @Override
    public long getId(String idr) throws SQLException {
        return executor.execQuery("select * from " + TABLE + " where idr='" + idr +"' and deleted=FALSE", result -> {
            result.next();
            return result.getLong(1);
        });
    }

    @Override
    public long CountNoPid() throws SQLException {
        return executor.execQuery("select count(id) from " + TABLE + " where  parentid is null and deleted=FALSE", result -> {
            result.next();
            return result.getLong(1);
        });
    }

    @Override
    public void delete(long id) throws SQLException {
        executor.execUpdate("update " + TABLE + " set deleted = TRUE where id = " + id);
    }

    public void deleteAll() throws SQLException {
        executor.execUpdate("update " + TABLE + " set deleted = TRUE");
    }
    @Override
    public void insert(Card card, String hist) throws SQLException {
        Map<String,String> cmap = card.toMap();
        //System.out.println("Insert " +cmap.get("name"));
        executor.execUpdate("insert into " + TABLE + " (insdate,idr,name,parent,deleted,history) " +
                "values ('" +
                    new Date(System.currentTimeMillis()).toString() + "','" +
                cmap.get("idr") + "','" +
                cmap.get("name") + "','" +
                    cmap.get("parent") + "',FALSE,'" +
                    hist + "')");
    }

    @Override
    public void  setparentId(long id, long pid) throws  SQLException {
        executor.execUpdate("update " + TABLE + " set parentid = " + pid + " where id = " + id +" and deleted = false");
    }

    @Override
    public void createTable() throws SQLException {
        executor.execUpdate("create table if not exists " + TABLE +
                " (id bigint auto_increment, " +
                "insdate DATE, " +
                "idr varchar(256), " +
                "name varchar(256), " +
                "parent varchar(256), " +
                "parentid bigint, " +
                "deleted boolean, " +
                "history varchar(1024), " +
                "parentname varchar(255), " +
                "primary key (id));");
        executor.execUpdate("create index if not exists name on deps(idr)");
        executor.execUpdate("create index if not exists name on deps(name)");
        executor.execUpdate("create index if not exists parent on deps(parentid)");
    }

    @Override
    public void dropTable() throws SQLException {
        executor.execUpdate("drop table if exists " + TABLE);
    }

    @Override
    public  void dropDoubleRow() throws SQLException {
        //заполнение parentname 3 уровня ('parent name/parent-parent name/parent-parent-parent name')
        executor.execUpdate("update " +TABLE+ " a set a.parentname=" +
                " (SELECT CONCAT(t1.name,'/',t2.name,'/',t3.name)" +
                "   FROM deps AS t1" +
                "   LEFT JOIN deps AS t2 ON t2.id=t1.parentid" +
                "   LEFT JOIN deps AS t3 ON t3.id=t2.parentid" +
                "   where a.parentid=t1.id)" +
                " WHERE a.parentname is null");
        executor.execUpdate("update " +TABLE+ " a set a.parentname= '//' WHERE a.parentname is null");

        //снятие метки об удалении у дубликатов с меньшим ID
        executor.execUpdate("update " +TABLE+ " set deleted=false where id not in " +
                        "(SELECT max(b.id) FROM " +TABLE+ " b GROUP BY b.name, b.parentname)");

        //перед удалением дубликатов с большим ID, проверить есть ли их ID где-то в PARENTID
        //и заменить на id сохраненной копии
        executor.execUpdate("UPDATE " +TABLE+ " v set v.parentid=(" +
                 " CASE WHEN (select z.id FROM" +
                 "         (SELECT* from " +TABLE+ " where id in" +
                 "                 (SELECT min(b.id) FROM " +TABLE+ " b GROUP BY b.name, b.parentNAME)) as a," +
                 " (SELECT* from " +TABLE+ " where id not in" +
                 "         (SELECT max(b.id) FROM " +TABLE+ " b GROUP BY b.name, b.parentNAME)) as z," +
                "  (SELECT* from " +TABLE+ " where id not in" +
                "          (SELECT min(b.id) FROM " +TABLE+ " b GROUP BY b.name, b.parentNAME)) as x" +
                "  WHERE a.parentid=x.id and x.name=z.name and x.parentname=z.parentname and V.ID=a.ID) is null" +
                "  THEN v.parentid" +
                "  ELSE  (select z.id FROM" +
                "          (SELECT* from " +TABLE+ " where id in" +
                "                  (SELECT min(b.id) FROM " +TABLE+ " b GROUP BY b.name, b.parentNAME)) as a," +
                "          (SELECT* from " +TABLE+ " where id not in" +
                "                  (SELECT max(b.id) FROM " +TABLE+ " b GROUP BY b.name, b.parentNAME)) as z," +
                "  (SELECT* from " +TABLE+ " where id not in" +
                "          (SELECT min(b.id) FROM " +TABLE+ " b GROUP BY b.name, b.parentNAME)) as x" +
                "  WHERE a.parentid=x.id and x.name=z.name and x.parentname=z.parentname and V.ID=a.ID)" +
                "  END)");

        //удаление дубликатов с большим ID
        executor.execUpdate("delete from " +TABLE+ " where id not in " +
                "(SELECT min(b.id) FROM " +TABLE+ " b GROUP BY b.name, b.parentname)");

    }
}
