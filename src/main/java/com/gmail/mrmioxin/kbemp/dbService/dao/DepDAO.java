package com.gmail.mrmioxin.kbemp.dbService.dao;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;
import com.gmail.mrmioxin.kbemp.IDao;
import com.gmail.mrmioxin.kbemp.dbService.dataSets.DepDataSet;
import com.gmail.mrmioxin.kbemp.dbService.dataSets.UsersDataSet;
import com.gmail.mrmioxin.kbemp.dbService.executor.Executor;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DepDAO implements IDao {
    private Logger logger = BaseConst.logg;
    private static final String TABLE = "deps";

    private Executor executor;

    public DepDAO(Connection connection) {
        this.executor = new Executor(connection);
    }

    @Override
    public Card get(long id) throws SQLException {
        return executor.execQuery("select * from " + TABLE + " where id=" + id + " and deleted=FALSE", result -> {
            result.next();
            return new DepDataSet(result.getLong(1), result.getDate(2), result.getString(3), result.getString(4),
                    result.getString(5), result.getLong(6), result.getBoolean(7), result.getString(8),
                    result.getString(9));
        });
    }

    @Override
    public long getId(String name, String parentname) throws SQLException {
        String pnameCampare = " and parentname='" + parentname + "'";
        if (parentname.equals("")) {
            pnameCampare = " and parentname is null";
        }
        return executor.execQuery(
                "select * from " + TABLE + " where name='" + name + "'" + pnameCampare + " and deleted=FALSE",
                result -> {
                    result.next();
                    return result.getLong(1);
                });
    }

    @Override
    public long getId(String idr) throws SQLException {
        return executor.execQuery("select * from " + TABLE + " where idr='" + idr + "' and deleted=FALSE", result -> {
            result.next();
            return result.getLong(1);
        });
    }

    @Override
    public long CountNoPid() throws SQLException {
        return executor.execQuery("select count(id) from " + TABLE + " where  parentid is null and deleted=FALSE",
                result -> {
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
        Map<String, String> cmap = card.toMap();
        String tdate = new Date(System.currentTimeMillis()).toString();
        //logger.log(Level.INFO, "Insert {0}, {1}", new String[] { tdate, card.toString()});
        executor.execUpdate(new StringBuilder("insert into ").append(TABLE).
                            append(" (insdate,idr,name,parent,deleted,history) values ('").
                            append(tdate).append("','").
                            append(cmap.get("idr")).append("','").
                            append(cmap.get("name")).append("','").
                            append(cmap.get("parent")).append("',FALSE,'").
                            append(hist).append("')").toString());
    }

    @Override
    public void setparentId(long id, long pid) throws SQLException {
        executor.execUpdate(
                "update " + TABLE + " set parentid = " + pid + " where id = " + id + " and deleted = false");
    }

    @Override
    public void createTable() throws SQLException {
        executor.execUpdate("create table if not exists " + TABLE + " (id bigint auto_increment, insdate DATE, "
                + "idr varchar(256), name varchar(256), parent varchar(256), parentid bigint, "
                + "deleted boolean, history varchar(1024), parentname varchar(255), " 
                + "primary key (id), index(idr,name,parentid));");
    }

    @Override
    public void dropTable() throws SQLException {
        executor.execUpdate("drop table if exists " + TABLE);
    }

    @Override
    public void dropDoubleRow() throws SQLException {
        executor.execUpdate("DROP VIEW IF EXISTS v_deps");
        // заполнение parentname 3 уровня ('parent name/parent-parent
        // name/parent-parent-parent name')

        executor.execUpdate("create view v_deps as select *, " +
                "(SELECT CONCAT(t1.name,'/',ifnull(t2.name,''),'/',ifnull(t3.name,'')) " +
                "FROM deps AS t1 LEFT JOIN deps AS t2 ON t2.id=t1.parentid LEFT JOIN deps AS t3 ON t3.id=t2.parentid " +
                "where t1.id=a.PARENTID) par3 " +
            "from " + TABLE + " a where a.parentname is null");
        executor.execUpdate("update " + TABLE + " a, v_deps set a.parentname = v_deps.par3"
                 + " WHERE a.id = v_deps.ID");
        executor.execUpdate("DROP VIEW `v_deps`");
        executor.execUpdate("update " + TABLE + " a set a.parentname= '//' WHERE a.parentname is null");


        // снятие метки об удалении у дубликатов с меньшим ID
        executor.execUpdate("create view v_deps as SELECT max(b.id) FROM " + TABLE + " b GROUP BY b.name, b.parentname");
        executor.execUpdate("update " + TABLE + " set deleted=false where id not in (select * from v_deps)");
        executor.execUpdate("DROP VIEW `v_deps`");

        // перед удалением дубликатов с большим ID, проверить есть ли их ID где-то в
        // PARENTID
        // и заменить на id сохраненной копии
        executor.execUpdate("UPDATE " + TABLE + " v set v.parentid= " + 
            "(CASE WHEN " + 
                "(select z.id FROM " +
                    "(SELECT * from " + TABLE + 
                        " where id in (SELECT min(b.id) FROM "+ TABLE + " b GROUP BY b.name, b.parentNAME)) as a, " + 
                    "(SELECT* from " + TABLE + 
                        " where id not in (SELECT max(b.id) FROM " + TABLE + " b GROUP BY b.name, b.parentNAME)) as z, " +
                    "(SELECT* from " + TABLE + 
                        " where id not in (SELECT min(b.id) FROM " + TABLE + " b GROUP BY b.name, b.parentNAME)) as x " +
                "WHERE a.parentid=x.id and x.name=z.name and x.parentname=z.parentname and V.ID=a.ID) is null "+ 
            "THEN v.parentid " + 
            "ELSE  (select z.id FROM " + 
                    "(SELECT* from " + TABLE+ 
                        " where id in (SELECT min(b.id) FROM " + TABLE + " b GROUP BY b.name, b.parentNAME)) as a," + 
                    "(SELECT* from " + TABLE + 
                        " where id not in (SELECT max(b.id) FROM " + TABLE + " b GROUP BY b.name, b.parentNAME)) as z,"+ 
                    "(SELECT* from " + TABLE + 
                        " where id not in (SELECT min(b.id) FROM " + TABLE+ " b GROUP BY b.name, b.parentNAME)) as x "+ 
                "WHERE a.parentid=x.id and x.name=z.name and x.parentname=z.parentname and V.ID=a.ID) " + 
            "END)");

        // удаление дубликатов с большим ID
        executor.execUpdate("create view v_deps as SELECT min(b.id) min_id FROM " + TABLE + " b GROUP BY b.name, b.parentname");
        executor.execUpdate("delete from " + TABLE + " where id not in (SELECT min_id FROM v_deps)");
        executor.execUpdate("DROP VIEW IF EXISTS `v_deps`");

        // заполнение parent в соответствие с parentid
        executor.execUpdate("create view v_deps as "+ 
        "SELECT b.id b_id, b.idr b_idr, a.id a_id, a.idr, a.PARENT a_parent, a.PARENTID a_parentid "+
        "FROM " + TABLE + " a left join " + TABLE + " b on a.parentid = b.id "+
        "where a.parent != b.idr");
    
        executor.execUpdate("update " + TABLE + " a, v_deps v set a.parent = v.b_idr where a.id = v.a_id");
    }

    @Override
    public void insert(UsersDataSet card, String hist) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public void update(long id, String field, String val) throws SQLException {
        // TODO Auto-generated method stub

    }
}
