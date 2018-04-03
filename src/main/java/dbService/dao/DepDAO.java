package com.gmail.mrmioxin.kbemp.dbService.dao;

import com.gmail.mrmioxin.kbemp.Card;
import com.gmail.mrmioxin.kbemp.dbService.dataSets.DepDataSet;
import com.gmail.mrmioxin.kbemp.dbService.executor.Executor;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author v.chibrikov
 *         <p>
 *         Пример кода для курса на https://stepic.org/
 *         <p>
 *         Описание курса и лицензия: https://github.com/vitaly-chibrikov/stepic_java_webserver
 */
public class DepDAO {

    private Executor executor;

    public DepDAO(Connection connection) {
        this.executor = new Executor(connection);
    }

    public DepDataSet get(long id) throws SQLException {
        return executor.execQuery("select * from deps where id=" + id, result -> {
            result.next();
            return new DepDataSet(result.getLong(1), result.getDate(2), result.getString(3), result.getString(4),
                    result.getBoolean(5),  result.getString(6));
        });
    }

    public long getDepId(String name) throws SQLException {
        return executor.execQuery("select * from deps where name='" + name + "'", result -> {
            result.next();
            return result.getLong(1);
        });
    }

    public void deleteDep(long id) throws SQLException {
        executor.execUpdate("update dpes set deleted = TRUE where id = " + id);
    }

    public void insertDep(Card card, String hist) throws SQLException {
        Map<String,String> cmap = card.toMap();
        executor.execUpdate("insert into deps (date,name,parent,deleted,history) " +
                "values ('" +
                    new Date(System.currentTimeMillis()).toString() + "','" +
                    cmap.get("name") + "','" +
                    cmap.get("parent") + "','" +
                    "FALSE,'" +
                    hist + "')");
    }


    public void createTable() throws SQLException {
        executor.execUpdate("create table if not exists deps " +
                "(id bigint auto_increment, " +
                "date DATE," +
                "name varchar(256), " +
                "parent varchar(256), " +
                "deleted boolean, " +
                "history varchar(1024), " +
                "key (name)," +
                "primary key (id));");
        //("create table if not exists users (id bigint auto_increment, user_name varchar(256), primary key (id))");
    }

    public void dropTable() throws SQLException {
        executor.execUpdate("drop table deps");
    }
}
