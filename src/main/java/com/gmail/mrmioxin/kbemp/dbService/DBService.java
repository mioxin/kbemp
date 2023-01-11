package com.gmail.mrmioxin.kbemp.dbService;

import com.gmail.mrmioxin.kbemp.BaseConst;
import com.gmail.mrmioxin.kbemp.Card;
import com.gmail.mrmioxin.kbemp.IDao;
import com.gmail.mrmioxin.kbemp.Main;
import com.gmail.mrmioxin.kbemp.dbService.dao.DepDAO;
import com.gmail.mrmioxin.kbemp.dbService.dao.UsersDAO;
import com.gmail.mrmioxin.kbemp.dbService.dataSets.UsersDataSet;
import com.gmail.mrmioxin.kbemp.wwwService.wwwAccess.ThreadGetImg;
import com.gmail.mrmioxin.kbemp.wwwService.wwwAccess.ThreadGetO;
import org.h2.jdbc.JdbcSQLException;
import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author
 *
 */
public class DBService {
    private static final String THREAD_O = "---# {0} потоков создано в ThreadGetO #------------------";
    private static final String THREAD_I = "---# {0} потоков создано в ThreadGetImg #------------------";
    private static final String ADDED_USERS = "Обработано users: {0}. Не найдено PID: {1}. Time: {2} ms./user. \r\nНе найдены:\r\n----START----\r\n";
    // private static final String WAIT_END_THREAD_I = "Finally {0}: Ждем завершения потоков ThreadGetImg созданных в addUser()";
    // private static final String WAIT_END_THREAD_O = "Finally {0}: Ждем завершения потоков ThreadGetO созданных в addUser()";
    private static final String NOT_FAUND_PID_1 = "Не найден PID для NAME: {0}, PARENT: null";
    private static final String FAUND_PID_1 = "Найден PID для NAME: {0}, PARENT: null, PID={1}";
    private static final String NOT_FAUND_PID = "Не найден PID для NAME: {0}, PARENT: {1}";
    private static final String FOUND_PID = "Найден PID для NAME: {0}, PARENT: {1}, PID={2}";
    private static final String NOT_TABNUM = "{0}: в старой карточке tabnum отсутствует.";
    private static final String CARD_NOT_MOD = "{0}: Карточка не изменилась.";
    private static final String NOT_FOUND_OLD_ID = "{0}: не содержит данных о e-mail, невозможно выявить старую карточку.";
    private static final String COMP_PNAME = "ID старой карты {0} ({1}) в базе DEPS не найден.";
    private static final String PARENT_3LEVEL = "Parent меньше 3х уровней для ";
    private static final String CARD_IS_MOD = "{0}: Карточка изменилась! (hist: {1}).\r\noldcard:{2}\r\nnewcard:{3}";
    private static final String NOT_THIRD_NAME = "{0}: в имени старой карточки отсутствует отчество.";
    private static final String RECREATE_OLD_CARD = "{0}: Изменили старую карточку, добавлено отчество. История: {1}";

    private class PName {// отдел, идентифицируется именем отдела и полным именем parent отдела (3
                         // уровня)
        private String name;
        private String parentname;

        public PName(String name, String parent) {
            this.name = name;
            this.parentname = parent;
        }

        public Boolean equals(PName pn) {
            Boolean camp = false;
            try {
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
    private Map<Long, PName> udepcash; // (deprecated)сохраняем имя отдела и id карточки users если parentid не найден,
                                       // для позднего поиска
    private IDao udao, ddao;

    public DBService() {
        this.connection = getMysqlConnection(); // getH2Connection();
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

    // получить ID отдела
    public Long getDepPid(PName newParentName) {
        Long pid = 0L;
        try {
            pid = ddao.getId(newParentName.getName(), newParentName.getParentName());
            logger.log(Level.INFO, FOUND_PID,
                    new String[] { newParentName.getName(), newParentName.getParentName(), Long.toString(pid) });
        } catch (SQLException e) {
            logger.log(Level.SEVERE, NOT_FAUND_PID,
                    new String[] { newParentName.getName(), newParentName.getParentName() });
            try {
                pid = ddao.getId(newParentName.getName());
                logger.log(Level.INFO, FAUND_PID_1, new String[] { newParentName.getName(), Long.toString(pid) });
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, NOT_FAUND_PID_1, newParentName.getName());
            }
        }
        return pid;
    }

    public List<Card> getDepsUsersByName(String name, Boolean isDeleted) throws DBException {
        List<Card> cards;
        try {
            cards = (ddao.getByField("name", name, isDeleted));
            cards.addAll(udao.getByField("name", name, isDeleted));
        } catch (SQLException e) {
            throw new DBException(e);
        }
        return cards;
    }

    public List<Card> getDepsUsersByPID(long pid, Boolean isDeleted) throws DBException {
        List<Card> cards;
        try {
            cards = (ddao.getByField("parentid", pid, isDeleted));
            cards.addAll(udao.getByField("parentid", pid, isDeleted));
        } catch (SQLException e) {
            throw new DBException(e);
        }
        return cards;
    }

    public Long getUsersIdByField(String field, String val) {
        Long id = 0L;
        try {
            id = udao.getIdByField(field, val, false);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQLException: Не найдено поле {0}={1}.", new String[] {field,val});
            //logger.log(Level.SEVERE, "SQLException: ", e);
        }
        return id;
    }

    private PName getParentName3lev(Map.Entry<String, Card> ecard) {
        String newidr = ecard.getValue().getparent(); // имя parent карточки типа 'razd1234'
        Card parentcard = Main.cards.getcard(newidr);

        // генерируем 3х уровневый parentname из HashMap с катрочками для данной
        // карточки
        String parentname3lev = "";// полное имя parent (3 уровня)
        Card tmpcard = parentcard;
        int level = 0;
        try {
            do {
                level++;
                tmpcard = Main.cards.getcard(tmpcard.getparent());
                parentname3lev += tmpcard.getName();
                if (level < 3) {
                    parentname3lev += "/";
                }
            } while (level < 3);
        } catch (NullPointerException npe) {
            logger.warning(PARENT_3LEVEL + parentcard.toString());
        }
        for (int i = level; i < 3; i++) {
            parentname3lev += "/";
            logger.log(Level.INFO, parentname3lev);
        }
        return new PName(parentcard.getName(), parentname3lev);
    }

    private String compareParentName(Card oldcard, PName newParentName) throws SQLException {
        // сравниваем parentname до 3 уровня
        PName oldParentName;

        if (oldcard.getparentid() == 0L) {
            oldParentName = new PName("#", "//");
        } else {
            try {
                oldParentName = new PName(ddao.get(oldcard.getparentid()).getName(),
                        ddao.get(oldcard.getparentid()).getparentname());
            } catch (SQLException e) {
                logger.log(Level.SEVERE, COMP_PNAME, new String[] {Long.toString(oldcard.getparentid()), oldcard.getName()});
                oldParentName = new PName("None", "root");
            }
        }
        if (!oldParentName.equals(newParentName)) {// изменилось parent name
            return "was change parent;";
        } else {
            return "";
        }

    }

    private Long findOldUsersId(Map.Entry<String, Card> ecard) {
        // поиск в базе старой карты с тем же табельным номером или email
        Long oldidByTab = 0L;
        if (ecard.getValue().getTabnum() != 0) {// указан таб номер
            oldidByTab = getUsersIdByField("tabnum", ecard.getValue().getTabnum().toString());
        }
        if (oldidByTab != 0L) {
            return oldidByTab;
        }
        // Long oldidByMob = 0L;
        // if (!ecard.getValue().getMobile().equals("")) {// указан мобильный
        //     oldidByMob = getIdByField("mobile", ecard.getValue().getMobile());
        // }
        Long oldidByEmail = 0L;
        if (!ecard.getValue().getEmail().equals("")) {// указан email
            oldidByEmail = getUsersIdByField("email", ecard.getValue().getEmail());
        } 
        if (oldidByEmail.equals(0L)) {
                logger.log(Level.WARNING, NOT_FOUND_OLD_ID, ecard.getValue().getName());
                return 0L;
        } else if (ecard.getValue().getTabnum() != 0) {
        // нашли старую карточку по e-mail, исправляем tabnum на новый
            try {
                udao.update(oldidByEmail, "tabnum", Integer.toString(ecard.getValue().getTabnum()));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return oldidByEmail;
    }

    /*
     * addUser(Map.Entry<String, Card> ecard) где ecard - элемент HashMap, в которой
     * ключ - ID типа строка ("sotr3445"), значение - вся карточка из www-int
     * добавляет карточку сотрудника в БД
     *
     */
    public long addUser(Map.Entry<String, Card> ecard) throws DBException, SQLException {
        Card oldcard;
        Long oldid, newid;
        String hist = ""; //результат сравнения старой и новой версии карты - история изменений
        PName newParentName;
        ThreadGetO thro = null;

        newParentName = getParentName3lev(ecard);//объект с именем отдела и полным именем parent отдела (3 уровня)
        udao.createTable();// создаем если отсутствует
        oldid = findOldUsersId(ecard);//ищем в базе есть ли уже эта карточка

        if (oldid > 0) { // карточка users уже есть
            oldcard = udao.get(oldid);
            //если в старой карточке нет отчества то пробуем его обновить с сайта
            if (oldcard.getName().split(" ").length < 3) {
                thro = new ThreadGetO(oldcard, "threadWWWO-" + oldcard.getName());
                ThreadGetO.threads.add(thro);
                thro.start();
            } else {
                logger.log(Level.INFO, "Есть отчество: {0}", ecard.getValue().getName());
            }
            
            hist = compareParentName(oldcard, newParentName);
            hist += ecard.getValue().compareCard(oldcard);
            if (hist.equals("reload foto;")) {//в старой карте был сбой загрузки фото, попытка снова скачать фото
                //убрать error из ссылки в БД, если снова будет сбой error будет добавлен
                udao.update(oldid, "avatar", ecard.getValue().getAvatar());
                //загрузка фото
                ThreadGetImg thr = new ThreadGetImg(ecard.getValue(), "threadImg-" + ecard.getValue().getTabnum().toString());
                ThreadGetImg.threads.add(thr);
                thr.start();
                hist="";
            }
            if (hist.equals("")) { // изменений нет
                logger.log(Level.INFO, CARD_NOT_MOD, ecard.getValue().getName());
                // обновить ldate в карточке users
                udao.setLdate(oldid, new Date(System.currentTimeMillis()));
                //обновить parent, т.к. razdXXX в dep могло измениться
                String prnt = "";
                if ((prnt = ddao.get(getUser(oldid).getparentid()).getidr()).equals(ecard.getValue().getparent())) {//!ecard.getValue().getparent().equals(prnt)) {
                    udao.update(oldid, "parent", prnt);
                }
                return oldid;
            } else { //карточка изменилась
                logger.log(Level.INFO, CARD_IS_MOD, new String[] {ecard.getValue().getName(), hist,oldcard.toString(), ecard.getValue().toString()});
                //переносим tabnum если в новой карте его нет
                if (ecard.getValue().getTabnum() == 0){
                    if (oldcard.getTabnum() == 0) {
                        logger.log(Level.INFO, NOT_TABNUM, ecard.getValue().getName());
                    } else {
                        ecard.getValue().setTabnum(oldcard.getTabnum());
                    }
                }
                // переносим отчество из старой карточки, если оно есть
                if (oldcard.getName().split(" ").length > 2) {
                    ecard.getValue().setname(ecard.getValue().getName() + " " + oldcard.getName().split(" ")[2]);
                } else {
                    logger.log(Level.INFO, NOT_THIRD_NAME, ecard.getValue().getName());
                }
                if (hist.contains("avatar")) {// если изменилось фото - скачать
                    ThreadGetImg thr = new ThreadGetImg(ecard.getValue(), "threadImg-" + ecard.getValue().getTabnum().toString());
                    ThreadGetImg.threads.add(thr);
                    thr.start();
                }
                // помечяем старую карточку как deleted
                udao.delete(oldid);
            }
        } else {// при загрузке из www-int, если карточка новая - добавляем отчество из www-int
            // при загрузке из файла это не требуется - отчество добавдяется ранее
            thro = new ThreadGetO(ecard.getValue(),
                    "threadWWWO-" + ecard.getValue().getName());
            ThreadGetO.threads.add(thro);
            thro.start();
            //скачиваем фото
            ThreadGetImg thr = new ThreadGetImg(ecard.getValue(), "threadImg-" + ecard.getValue().getTabnum().toString());
            ThreadGetImg.threads.add(thr);
            thr.start();

        }
        Long pid = getDepPid(newParentName);
        String parent = getDep(pid).getidr();
        ecard.getValue().setparentid(pid);
        ecard.getValue().setparent(parent);
        udao.insert(ecard.getValue(), hist);
        logger.log(Level.INFO, "Insert user {0}", ecard.getValue().getName());
        
        Card newcard = ecard.getValue();
        newid = getUsersIdByField("email", newcard.getEmail());
        connection.commit();
        if (pid == 0L) {// если карточка родителя не найдена заносим в массив для последующего поиска
            udepcash.put(newid, newParentName);
        }
        return newid;
    }

    /*
     * updateDB(Map<String, Card> cards) где cards - HashMap содержащая карточки из
     * www-int, ключ - id карты типа строка "razd1234" добавление карточек в БД
     */
    public Integer updateDB(Map<String, Card> cards) throws DBException {
        Integer depcount = 0;
        Integer usercount = 0;
        Integer errcount = 0;

        /////////////////////// заполняем deps //////////////////////////
        try {
            ddao.createTable();
            ddao.deleteAll(); // пометим как удаленные все старые записи в deps, т.к. скачали из все заново в
                              // HashMap
            //connection.setAutoCommit(false);
            logger.log(Level.INFO, "Заполняем DEP.........................................."+".");
            for (Map.Entry<String, Card> entry : cards.entrySet()) {
                try {
                    if (entry.getValue().isParent()) {// заполняем deps из HashMap
                        ddao.insert(entry.getValue(), "");
                        depcount++;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.log(Level.SEVERE,"Not insert in deps {0}: {1}", new String[] {entry.getValue().getidr(), entry.getValue().getName()});
                    errcount++;
                }
            }
            logger.log(Level.INFO, "Inserted in DEPS: " + depcount + ". Error: " + errcount);
            // заполнение PID в таблице deps
            int count = 0;
            int er = 0;
            logger.log(Level.INFO, "Заполняем PID в DEP..................................."+".");
            for (Map.Entry<String, Card> entry : cards.entrySet()) {
                long pid;
                long id;
                try {
                    if (entry.getValue().isParent()) {// заполняем pid
                        String pidr = entry.getValue().getparent();
                        if (pidr.equals("#")) {
                            pid = 0L;
                        } else {
                            pid = ddao.getId(entry.getValue().getparent());
                        }
                        id = ddao.getId(entry.getValue().getidr());
                        ddao.setparentId(id, pid);
                        count++;
                    }
                } catch (SQLException e) {
                    logger.log(Level.SEVERE,
                            "Not insert in deps {0} :{1}", new String[]  {entry.getValue().getidr(), entry.getValue().getName()});
                    er++;
                }
            }
            logger.log(Level.INFO, "Fill PID for DEPS in DB: " + count + ". Error: " + er);
            // удаляем из deps дубликаты (некоторые карточки deps не изменились, но были
            // повторно скачаны и внесены в deps)
            ddao.dropDoubleRow();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignore) {
            }
            e.printStackTrace();

        } finally {
            try {
                //connection.commit();
                connection.setAutoCommit(true);
            } catch (SQLException ignore) {
            }
        }

        /////////////////// заполняем users //////////////////////
        long t = System.nanoTime();
        errcount = 0;
        try {
            connection.setAutoCommit(false);
            logger.log(Level.INFO, "Заполняем USERS.................................."+".");
            for (Map.Entry<String, Card> entry : cards.entrySet()) {
                try {
                    if (!entry.getValue().isParent()) {// заполняем users
                        logger.log(Level.INFO, "------------------< {0} >------------------", usercount);
                        addUser(entry);
                        usercount++;
                    }
                } catch (DBException e) {
                    // e.printStackTrace();
                    errcount++;
                    logger.log(Level.SEVERE,"Can not insert user [addUser()]: ", entry.getValue().toString());
                    logger.log(Level.SEVERE,"DBException:", e);
                }
            }
            //помечаем как удаленных всех users кого не получили с сайта
            logger.log(Level.INFO, "Deleted users (don't get from www): " + udao.delOldUsers().toString());
       } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignore) {
                logger.log(Level.SEVERE,"Rollback error.",ignore);
            }
            logger.log(Level.SEVERE, "OOOOOOOOOOOO usercount: ", usercount);
            //logger.log(Level.SEVERE, "OOOOOOOOOOOO userEntry: {0}", );
            throw new DBException(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignore) {
                logger.log(Level.SEVERE,"Set Autocommit back error.", ignore);
            }
            logger.log(Level.INFO, THREAD_I, Integer.toString(ThreadGetImg.threads.size()));
            for (ThreadGetImg th : ThreadGetImg.threads) {// ждем завершения потоков ThreadGetImg созданных в addUser()
                try {
                    th.join();
                    //logger.log(Level.INFO, WAIT_END_THREAD_I, th.getName());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    logger.log(Level.SEVERE,"Join to ThreadGetImg error.",e );
                }
            }
            logger.log(Level.INFO, THREAD_O, Integer.toString(ThreadGetO.threads.size()));
            for (ThreadGetO tho : ThreadGetO.threads) {
                try {
                    tho.join();
                    //logger.log(Level.INFO, WAIT_END_THREAD_O, tho.getName());
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE,"Join to ThreadGetImg error.", e );
                    e.printStackTrace();
                }
            }

            for (Entry<String,String> fio : ThreadGetO.idFio.entrySet()) {
                long id = 0L;
                if (fio.getKey().equals(fio.getValue())) {
                    System.out.println("Abort trying DB update FIO "+ fio.toString());
                } else {
                    try {
                        id = udao.getIdByField("name", fio.getKey(), false);
                        udao.update(id, "name", fio.getValue());
                    } catch (SQLException e1) {
                        if (id != 0L) {
                            logger.log(Level.INFO, "Cant get name {0} from DB.", fio.getKey());
                        } else {
                            logger.log(Level.INFO, "Cant Update DB for FIO {0}.", fio.getValue());
                        }
                        logger.log(Level.INFO, e1.toString());
                        System.out.println("Cant Update DB for FIO. Name: " + fio.getKey() +"; full name: "+fio.getValue());
                    }
                }
            }
            for (Card c : ThreadGetImg.notDownloadImg) {//исправляем в БД карточки со сбойными загрузками фото
                try {
                    logger.log(Level.INFO, "Update DB error download foto {0}", c.toString());
                    long id = udao.getId(String.valueOf(c.getTabnum()));
                    udao.update(id, "avatar", "error: " + c.getAvatar() );
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.log(Level.SEVERE,"Update field \"avatar\" failure.",e );
                } 
            }
            
        }
        if (usercount>0) {
        logger.log(Level.INFO, ADDED_USERS, 
                                new String[] {usercount.toString(), Integer.toString(udepcash.size()), Float.toString((System.nanoTime()-t)/(1000*usercount))});
                            }
        for (Map.Entry<Long, PName> e : udepcash.entrySet()) {
            logger.log(Level.INFO,  "{0}: {1}; {2}", new String[] {Long.toString(e.getKey()), e.getValue().getName(), e.getValue().getParentName()});
        }
        logger.log(Level.INFO, "-----END-----");
        return (depcount + usercount);
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
    public static Connection getMysqlConnection() {
        try {
            DriverManager.registerDriver((Driver) Class.forName("com.mysql.cj.jdbc.Driver").newInstance());

            StringBuilder url = new StringBuilder();

            url.
                    append("jdbc:mysql://").        //db type
                    append("localhost:").           //host name
                    append("3306/").                //port
                    append("wwwint?").          //db name
                    append("user=root&").          //login
                    append("password=mmm");       //password

            System.out.println("Connecting to MySQL serv...\n");

            Connection connection = DriverManager.getConnection(url.toString());
            return connection;
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


}
