package com.gmail.mrmioxin.kbemp.dbService;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;
import com.gmail.mrmioxin.kbemp.IDao;
import com.gmail.mrmioxin.kbemp.Main;
import com.gmail.mrmioxin.kbemp.dbService.dao.DepDAO;
import com.gmail.mrmioxin.kbemp.dbService.dao.UsersDAO;
import com.gmail.mrmioxin.kbemp.wwwService.wwwAccess.ThreadGetImg;
import com.gmail.mrmioxin.kbemp.wwwService.wwwAccess.ThreadGetO;
import org.h2.jdbc.JdbcSQLException;
import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author
 *
 */
public class DBService {
    private class PName {//отдел, идентифицируется именем отдела и полным именем parent отдела (3 уровня)
        private String name;
        private String parentname;
        public PName(String name, String parent){
            this.name=name;
            this.parentname = parent;
        }
        public Boolean equals(PName pn){
            Boolean camp= false;
            try{
                camp = (this.name.equals(pn.name) && this.parentname.equals(pn.parentname));
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return camp;
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
    private Map<Long, PName> udepcash; //(deprecated)сохраняем имя отдела и id карточки users если parentid не найден, для позднего поиска
    private  IDao udao, ddao;

    public DBService() {
        this.connection = getH2Connection();
        this.udao = new UsersDAO(connection);
        this.ddao = new DepDAO(connection);
        this.udepcash = new HashMap<>();
    }

    public Card getDep(long id) throws DBException {
        try {
            return (ddao.get(id));
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public Card getUser(long id) throws DBException {
        try {
            return (udao.get(id));
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public List<Card> getByName(String name, Boolean noDeleted) throws DBException {
        List<Card> cards;
        try {
            cards = (ddao.getByField("name",name,noDeleted));
            cards.addAll(udao.getByField("name",name,noDeleted));
        } catch (SQLException e) {
            throw new DBException(e);
        }
        return  cards;
    }

    public List<Card> getByPID(long pid, Boolean noDeleted) throws DBException {
        List<Card> cards;
        try {
            cards = (ddao.getByField("parentid",pid,noDeleted));
            cards.addAll(udao.getByField("parentid",pid,noDeleted));
        } catch (SQLException e) {
            throw new DBException(e);
        }
        return  cards;
    }

    public List<Card> getByMobile(String mobile, Boolean noDeleted) throws DBException {
        try {
            return udao.getByField("mobile",mobile,noDeleted);
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }


/*
*   addUser(Map.Entry<String, Card> ecard)
*   где ecard - элемент HashMap, в которой ключ - ID типа строка ("sotr3445"), значение - вся карточка из www-int
*   добавляет карточку сотрудника в БД
*
*/
    public long addUser(Map.Entry<String, Card> ecard) throws DBException, SQLException {
        Card oldcard;
        Long oldid, newid;
        String hist = "";
        String tabnum = ecard.getValue().getTabnum().toString();
        PName newParentName;
        PName oldParentName;
        String newidr = ecard.getValue().getparent(); //имя parent карточки типа 'razd1234'

    //генерируем 3х уровневый parentname из HashMap с катрочками для данной карточки
        String parentname3lev="";// полное имя parent (3 уровня)
        Card parentcard=Main.cards.getcard(newidr);
        Card tmpcard = parentcard;
        int level = 0;
        try {
            do {
                level++;
                tmpcard = Main.cards.getcard(tmpcard.getparent());
                parentname3lev += tmpcard.getName();
                if (level<3) {parentname3lev += "/";}
            } while (level<3);
        } catch (NullPointerException npe){
            logger.warning("Parent для ["+parentcard.getName()+"] меньше 3х уровней.");
        }
        for (int i=level; i<3; i++){parentname3lev += "/";}
        newParentName = new PName(parentcard.getName(), parentname3lev);

        udao.createTable();//создаем если отсутствует
        try {
            oldid = udao.getId(tabnum);
        } catch (SQLException e) {
            oldid = 0L;
        }
        if (oldid > 0) { //карточка users уже есть
            oldcard = udao.get(oldid);
            if (oldcard.getparentid() == 0L) {
                oldParentName = new PName("#","//");
            }else {
                try {
                    oldParentName = new PName(
                            ddao.get(oldcard.getparentid()).getName(),
                            ddao.get(oldcard.getparentid()).getparentname());
                } catch (JdbcSQLException e) {
                    logger.warning("ID старой карты " + oldcard.getparentid() + " в базе DEPS не найден.");
                    oldParentName = new PName("None","root");
                }
            }
            if (!oldParentName.equals(newParentName)) {//изменилось parent name
                hist = "was change parent;";
            }
            hist += ecard.getValue().compareCard(oldcard);
            if (hist.equals("")) { //изменений нет
                logger.fine(tabnum + ": Карточка не изменилась.");
                // обновить ldate  в карточке users
                udao.setLdate(oldid, new Date(System.currentTimeMillis()));
                return oldid;
            } else {
                //переносим отчество из старой карточки, если оно есть
                if (oldcard.getName().split(" ").length>2) {
                    ecard.getValue().setname(ecard.getValue().getName() + " " + oldcard.getName().split(" ")[2]);
                }
                if (hist.contains("avatar")) {//если изменилось фото - скачать
                    ThreadGetImg thr = new ThreadGetImg(Main.cards.site.getHttpclient() ,ecard.getValue(), "threadImg-"+ ecard.getValue().getTabnum().toString());
                    ThreadGetImg.threads.add(thr);
                    thr.start();
                }
                //помечяем старую карточку как deleted
                udao.delete(oldid);
            }
        } else {//при загрузке из www-int, если карточка новая - добавляем отчество из www-int
            //при загрузке из файла это не требуется - отчество добавдяется ранее
            ThreadGetO thr = new ThreadGetO(Main.cards.site.getHttpclient(),ecard.getValue(), "threadWWWO-"+ecard.getValue().getName());
            ThreadGetO.threads.add(thr);
            thr.start();
            try {
                thr.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Card newcard = ecard.getValue();
        Long pid = 0L;
        Boolean nopid = false;
        try {
            pid = ddao.getId(newParentName.getName(),newParentName.getParentName());
        } catch (SQLException e) {
            nopid = true;
        }
        //newcard.setparentid(pid);
        udao.insert(ecard.getValue(), hist);
        System.out.println("Insert user " + ecard.getValue().getName());
        newid = udao.getId(ecard.getValue().getTabnum().toString());
        udao.setparentId(newid,pid);

        connection.commit();
        if (nopid) {//если карточка родителя не найдена заносим в массив для последующего поиска
            udepcash.put(newid, newParentName);
        }
        return newid;
    }
/*
*   updateDB(Map<String, Card> cards)
*   где cards - HashMap содержащая карточки из www-int, ключ - id карты типа строка "razd1234"
*   добавление карточек в БД
*/
    public Integer updateDB(Map<String, Card> cards) throws DBException {
        Integer depcount = 0;
        Integer usercount = 0;
        Integer errcount = 0;

/////////////////////// заполняем deps //////////////////////////
        try {
            ddao.createTable();
            ddao.deleteAll(); //пометим как удаленные все старые записи в deps, т.к. скачали из все заново в HashMap
            connection.setAutoCommit(false);
            for (Map.Entry<String, Card> entry : cards.entrySet()) {
                try {
                    if (entry.getValue().isParent()) {//заполняем deps из HashMap
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
            int count=0;
            int er = 0;
            for (Map.Entry<String, Card> entry : cards.entrySet()) {
                long pid;
                long id;
                try {
                    if (entry.getValue().isParent()) {//заполняем pid
                        String pidr = entry.getValue().getparent();
                        if (pidr.equals("#")) {
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
                System.out.print("\rFill PID for DEPS in DB: " + count + ". Error: " + er);
            }
            System.out.println();
//удаляем из deps дубликаты (некоторые карточки deps не изменились, но были повторно скачаны и внесены в deps)
            ddao.dropDoubleRow();

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
            for (ThreadGetImg th: ThreadGetImg.threads){//ждем завершения потоков ThreadGetImg созданных в addUser()
                try {
                    th.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
        System.out.println("users.ID : deps.name");
        for (Map.Entry<Long, PName> e:udepcash.entrySet()) {
            System.out.println(e.getKey() +": " + e.getValue().getName());
        }
        return  (depcount + usercount);
    }

//    private int findUserParent(String typemap, Map<Long, PName> mapcash){
//        int count = 0;
//        int err =0;
//        Map<Long, PName> tmpcash = new HashMap<>();
//        try {
//            connection.setAutoCommit(false);
//            while (mapcash.size()>0) {
//                for (Map.Entry<Long, PName> entry : mapcash.entrySet()) {
//
//                    try {
//                        if (typemap.equals("users")) {
//                            udao.setparentId(entry.getKey(), ddao.getId(entry.getValue().getName(),entry.getValue().getParentName()));
//                        } else {
//                            if (entry.getValue().equals("root")) {
//                                ddao.setparentId(entry.getKey(), 0L);
//                            }else{
//                                ddao.setparentId(entry.getKey(), ddao.getId(entry.getValue().getName(),entry.getValue().getParentName()));
//                            }
//                        }
//                        count++;
//                    } catch (SQLException e) {
//                        tmpcash.put(entry.getKey(),entry.getValue());
//                        e.printStackTrace();
//                    }
//                }
//                err++;
//                mapcash.clear();
//                mapcash.putAll(tmpcash);
//                if (err>5){
//                    System.out.println("Поиск родителей более " + err + " раз.");
//                    System.out.println(mapcash.toString());
//                    throw new SQLException();
//                }
//            }
//            connection.commit();
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                connection.setAutoCommit(true);
//            } catch (SQLException ignore) {
//            }
//        }
//        return count;
//    }

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
