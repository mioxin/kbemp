package com.gmail.mrmioxin.kbemp.dbService;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;
import com.gmail.mrmioxin.kbemp.IDao;
import com.gmail.mrmioxin.kbemp.Main;
import com.gmail.mrmioxin.kbemp.dbService.dao.DepDAO;
import com.gmail.mrmioxin.kbemp.dbService.dao.UsersDAO;
import com.gmail.mrmioxin.kbemp.wwwService.wwwAccess.ThreadGetImg;
import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author v.chibrikov
 *         <p>
 *         Пример кода для курса на https://stepic.org/
 *         <p>
 *         Описание курса и лицензия: https://github.com/vitaly-chibrikov/stepic_java_webserver
 */
public class DBService {
    private class PName {//отдел, идентифицируется именем отдела и полным именем parent отдела
        private String name;
        private String parentname;
        public PName(String name, String parent){
            this.name=name;
            this.parentname = parent;
        }
        public Boolean equals(PName pn){
            return (this.name.equals(pn.name) && this.parentname.equals(pn.parentname));
        }

        public String getName() {
            return name;
        }

        public String getParentName() {
            return parentname;
        }
    }
    private final Connection connection;
    private Logger logger = BaseConst.logg;
    private Map<Long, PName> udepcash; //сохраняем имя отдела и id карточки users если parentid не найден, для позднего поиска
    private  IDao udao, ddao;

    public DBService() {
        this.connection = getH2Connection();
        this.udao = new UsersDAO(connection);
        this.ddao = new DepDAO(connection);

        this.udepcash = new HashMap<>();
    }

    public Card getUser(long id) throws DBException {
        try {
            return (udao.get(id));
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public Card getUser(String name) throws DBException {
        try {
            return (udao.get(udao.getId(name)));
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public long addUser(Map.Entry<String, Card> ecard) throws DBException, SQLException {
        Card oldcard;
        Long oldid, newid;
        String hist = "";
        Map<String, String> mapcard = ecard.getValue().toMap();
        String tabnum = ecard.getValue().getTabnum().toString();
        PName newParentName;
        PName oldParentName;
        String newidr = ecard.getValue().getparent();

        if (newidr.equals("root")) {//отделы верхнего уровня - без parent
            newParentName = new PName("root","root");
        }else{
            newParentName = new PName(Main.cards.getcard(newidr).getName(), // полное имя parent
                    (Main.cards.getcard(newidr).getparent().equals("root"))?
                            "root":Main.cards.getcard(Main.cards.getcard(newidr).getparent()).getName()
            ); // полное имя parent's parent
        }

        udao.createTable();//создаем если отсутствует
        ddao.createTable();

        try {
            oldid = udao.getId(tabnum);
        } catch (SQLException e) {
            oldid = 0L;
        }
        if (oldid > 0) { //карточка users уже есть
            oldcard = udao.get(oldid);
            if (oldcard.getparentid() == 0L) {
                oldParentName = new PName("root","root");
            }else {
                oldParentName = new PName(
                        ddao.get(oldcard.getparentid()).getName(),
                        (ddao.get(oldcard.getparentid()).getparentid() == 0L) ?
                                "root":ddao.get(ddao.get(oldcard.getparentid()).getparentid()).getName()
                );
            }
            if (!oldParentName.equals(newParentName)) {
                //изменилось parent name
                hist = "was change parent;";
            }
            hist += ecard.getValue().compareCard(oldcard);
            if (hist == "") { //изменений нет
                logger.fine(tabnum + ": Карточка не изменилась.");
                // обновить ldate  в карточке users
                udao.setLdate(oldid, new Date(System.currentTimeMillis()));
                return oldid;
            } else {//помечяем старую карточку как deleted
//                String savatar = mapcard.get("avatar");
                if (hist.contains("avatar")) {//если изменилось фото - скачать
                    ThreadGetImg thr = new ThreadGetImg(Main.cards.site.getHttpclient() ,ecard.getValue(), "threadImg"+ ecard.getValue().getTabnum().toString());
                    ThreadGetImg.threads.add(thr);
                    thr.start();
//                    try {
//                        (new wwwService()).getApidata().downloadImgFile(savatar);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }
                udao.delete(oldid);
            }
        }
        //Card newcard = ecard.getValue();
        Long pid = 0L;
        Boolean nopid = false;
        try {
            pid = ddao.getId(newParentName.getName(),Main.cards.getcard(newidr).getparent());
        } catch (SQLException e) {
            nopid = true;
        }
        //newcard.setparentid(pid);
        udao.insert(ecard.getValue(), hist);
        System.out.println("Insert user " + mapcard.get("name"));
        newid = udao.getId(mapcard.get("tabnum"));
        udao.setparentId(newid,pid);

        connection.commit();
        if (nopid) {//если карточка родителя не найдена заносим в массив для последующего поиска
            udepcash.put(newid, newParentName);
        }
        return newid;
    }

    public Integer updateDB(Map<String, Card> cards) throws DBException {
        Integer depcount = 0;
        Integer usercount = 0;
        Integer errcount = 0;

/////////////////////// заполняем deps //////////////////////////
        try {
            ddao.deleteAll();
        } catch (SQLException e) {
            logger.warning("Cannot deletedAll deps.");
        }

        try {
            ddao.createTable();
            connection.setAutoCommit(false);
            for (Map.Entry<String, Card> entry : cards.entrySet()) {
                try {
                    if (entry.getValue().isParent()) {//заполняем deps
                        ddao.insert(entry.getValue(), "");
                        depcount++;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.warning("Not insert in deps " + entry.getValue().getidr() + ":" + entry.getValue().getName());
                    errcount++;
                }
            }
            logger.fine("Inserted in DEPS: "+depcount+". Error: "+errcount);
        //заполнение PID в таблице deps
            for (Map.Entry<String, Card> entry : cards.entrySet()) {
                int count=0;
                int er = 0;
                long pid;
                long id;
                try {
                    if (entry.getValue().isParent()) {//заполняем pid
                        String pidr = entry.getValue().getparent();
                        if (pidr.equals("root")) {
                            pid = 0L;
                        } else {
                            pid =  ddao.getId(entry.getValue().getparent());
                        }
                        id = ddao.getId(entry.getValue().getidr());
                        ddao.setparentId(id, pid);
                        count++;
                    }
                } catch (SQLException e) {
                    logger.warning("Not insert in deps " + entry.getValue().getidr() + ":" + entry.getValue().getName());
                    er++;
                }
                System.out.print("\rCount DEPS in DB: " + count + ". Error: " + er);
            }

            System.out.println();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignore) {
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignore) {
            }
        }

///////////////////  заполняем users  //////////////////////
        long t = System.nanoTime();
        try {
            connection.setAutoCommit(false);
            for (Map.Entry<String, Card> entry : cards.entrySet()) {
                try {
                    if (!entry.getValue().isParent()) {//заполняем users
                        addUser(entry);
                        usercount++;
                    }
                } catch (DBException e) {
                    //e.printStackTrace();
                    errcount++;
                    logger.warning("Can not inser user: " + entry.getValue().getName());
                }
            }

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
            for (ThreadGetImg th: ThreadGetImg.threads){
                try {
                    th.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Обработано users: " + usercount+ ". Не найдено PID: " + udepcash.size() +". Time: " + (System.nanoTime()-t)/(usercount) + " ns./user ");
        for (Map.Entry<Long, PName> e:udepcash.entrySet()) {
            System.out.println(e.getKey() +": " + e.getValue().getName());
        }
        return  (depcount + usercount);
    }

    private int findUserParent(String typemap, Map<Long, PName> mapcash){
        int count = 0;
        int err =0;
        Map<Long, PName> tmpcash = new HashMap<>();
        try {
            connection.setAutoCommit(false);
            while (mapcash.size()>0) {
                for (Map.Entry<Long, PName> entry : mapcash.entrySet()) {

                    try {
                        if (typemap.equals("users")) {
                            udao.setparentId(entry.getKey(), ddao.getId(entry.getValue().getName(),entry.getValue().getParentName()));
                        } else {
                            if (entry.getValue().equals("root")) {
                                ddao.setparentId(entry.getKey(), 0L);
                            }else{
                                ddao.setparentId(entry.getKey(), ddao.getId(entry.getValue().getName(),entry.getValue().getParentName()));
                            }
                        }
                        count++;
                    } catch (SQLException e) {
                        tmpcash.put(entry.getKey(),entry.getValue());
                        e.printStackTrace();
                    }
                }
                err++;
                mapcash.clear();
                mapcash.putAll(tmpcash);
                if (err>5){
                    System.out.println("Поиск родителей более " + err + " раз.");
                    System.out.println(mapcash.toString());
                    throw new SQLException();
                }
            }
            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignore) {
            }
        }
        return count;
    }

    public void cleanUp() throws DBException {
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
