package com.gmail.mrmioxin.kbemp;

import com.gmail.mrmioxin.kbemp.dbService.dataSets.DepDataSet;

import java.sql.Date;
import java.sql.SQLException;

/**
 * Created by palchuk on 12.04.2018.
 */
public interface IDao {
    Card get(long id) throws SQLException;

    default long getId(String name, String pidr) throws SQLException{
        return 0L;
    };
    default long getId(String name) throws SQLException{
        return 0L;
    };

    long CountNoPid() throws SQLException;

    void delete(long id) throws SQLException;

    default void deleteAll() throws SQLException{};

    void insert(Card card, String hist) throws SQLException;

    void  setparentId(long id, long pid) throws  SQLException;

    default void  setLdate(long id, Date ldate) throws  SQLException{};

    void createTable() throws SQLException;

    void dropTable() throws SQLException;
}
