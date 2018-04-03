package com.gmail.mrmioxin.kbemp.dbService;

import com.gmail.mrmioxin.kbemp.Card;
import com.gmail.mrmioxin.kbemp.dbService.dao.DepDAO;
import com.gmail.mrmioxin.kbemp.dbService.dao.UsersDAO;
import com.gmail.mrmioxin.kbemp.dbService.dataSets.DepDataSet;
import com.gmail.mrmioxin.kbemp.dbService.dataSets.UsersDataSet;
import com.gmail.mrmioxin.kbemp.wwwService.wwwService;
import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author v.chibrikov
 *         <p>
 *         Пример кода для курса на https://stepic.org/
 *         <p>
 *         Описание курса и лицензия: https://github.com/vitaly-chibrikov/stepic_java_webserver
 */
public class DBService {
    private final Connection connection;

    public DBService() {
        this.connection = getH2Connection();
    }

    public UsersDataSet getUser(long id) throws DBException {
        try {
            return (new UsersDAO(connection).get(id));
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public UsersDataSet getUser(String name) throws DBException {
        try {
            return (new UsersDAO(connection).get(new UsersDAO(connection).getUserId(name)));
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public long addUser(Card card) throws DBException {
        try {
            connection.setAutoCommit(false);
            UsersDAO dao = new UsersDAO(connection);
            dao.createTable();//создаем если отсутствует
            dao.insertUser((UsersDataSet) card);
            connection.commit();
            return dao.getUserId(card.toMap().get("tabnum"));
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignore) {
            }
            throw new DBException(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignore) {
            }
        }
    }

    public long addDep(Card card) throws DBException {
        Long oldid;
        DepDataSet oldcard;
        String hist="";
        String depname =card.toMap().get("name");

        try {
            connection.setAutoCommit(false);
            DepDAO dao = new DepDAO(connection);
            dao.createTable();
            if ((oldid = dao.getDepId(depname)) >0) { //карточка уже есть
                oldcard = dao.get(oldid);
                hist = card.compareCard(oldcard);
                if (hist == "") { //изменений нет - выходим
                    throw new SQLException(depname +": Карточка не изменилась.");
                } else {//помечяем старую карточку как deleted
                    String savatar =card.toMap().get("avatar");
                    if (hist.contains("avatar")){//если изменилось фото - скачать
                        (new wwwService()).getApidata().downloadImgFile(savatar);
                    };
                    dao.deleteDep(oldid);
                }
            }
            dao.insertDep(card, hist);
            connection.commit();
            return dao.getDepId(card.getNamePhone().get(0));
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignore) {
            }
            throw new DBException(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignore) {
            }
        }
    }


    public Integer updateDB(ArrayList<Card> cards){
        Integer depcount = 0;
        Integer usercount = 0;

            for (Card c:cards) {
                try {
                    if (c.isParent()) {
                        addDep(c);
                        depcount++;
                    } else {
                        addUser(c);
                        usercount++;
                    }
                } catch (DBException e) {
                    e.printStackTrace();
                }

            }
            return  (depcount + usercount);
    }

    public void cleanUp() throws DBException {
        DepDAO ddao = new DepDAO(connection);
        UsersDAO udao = new UsersDAO(connection);
        try {
            ddao.dropTable();
            udao.dropTable();
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public void printConnectInfo() {
        try {
            System.out.println("DB name: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("DB version: " + connection.getMetaData().getDatabaseProductVersion());
            System.out.println("Driver: " + connection.getMetaData().getDriverName());
            System.out.println("Autocommit: " + connection.getAutoCommit());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getH2Connection() {
        try {
            String url = "jdbc:h2:./h2db";
            String name = "test";
            String pass = "test";

            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL(url);
            ds.setUser(name);
            ds.setPassword(pass);

            Connection connection = DriverManager.getConnection(url, name, pass);
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
