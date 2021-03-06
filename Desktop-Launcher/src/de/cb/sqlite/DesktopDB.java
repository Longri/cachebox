package de.cb.sqlite;

import CB_Core.Database;
import CB_Utils.Log.Log;
import CB_Utils.Log.LogLevel;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;

import java.io.IOException;
import java.sql.*;
import java.util.Map.Entry;

public class DesktopDB extends Database {
    private static final String log = "DesktopDB";

    Connection myDB = null;

    public DesktopDB(DatabaseType databaseType) throws ClassNotFoundException {
        super(databaseType);

        System.setProperty("sqlite.purejava", "true");
        Class.forName("org.sqlite.JDBC");
    }

    @Override
    public void Close() {
        try {
            Log.trace(log, "close DB:" + databasePath);
            myDB.close();
            myDB = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Initialize() {
        if (myDB == null) {
            File dbfile = FileFactory.createFile(databasePath);
            if (!dbfile.exists())
                Reset();

            try {
                Log.trace(log, "open data base: " + databasePath);
                myDB = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            } catch (Exception exc) {
                return;
            }
        }
    }

    @Override
    public void Reset() {
        // if exists, delete old database file
        File file = FileFactory.createFile(databasePath);
        if (file.exists()) {
            Log.trace(log, "RESET DB, delete file: " + databasePath);
            try {
                file.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Log.trace(log, "create data base: " + databasePath);
            myDB = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            myDB.commit();
            myDB.close();

        } catch (Exception exc) {
            Log.err(log, "createDB", exc);
        }
    }

    @Override
    public CoreCursor rawQuery(String sql, String[] args) {
        if (myDB == null)
            return null;

        if (LogLevel.isLogLevel(LogLevel.TRACE)) {
            StringBuilder sb = new StringBuilder("RAW_QUERY :" + sql + " ARGs= ");
            if (args != null) {
                for (String arg : args)
                    sb.append(arg + ", ");
            } else
                sb.append("NULL");
            Log.trace(log, sb.toString());
        }

        ResultSet rs = null;
        PreparedStatement statement = null;
        try {

            statement = myDB.prepareStatement(sql);

            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    statement.setString(i + 1, args[i]);
                }
            }
            rs = statement.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // TODO Hack to get Rowcount
        ResultSet rs2 = null;
        int rowcount = 0;
        PreparedStatement statement2 = null;
        try {

            statement2 = myDB.prepareStatement("select count(*) from (" + sql + ")");

            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    statement2.setString(i + 1, args[i]);
                }
            }
            rs2 = statement2.executeQuery();

            rs2.next();

            rowcount = Integer.parseInt(rs2.getString(1));
            statement2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                statement2.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }

        return new DesktopCursor(rs, rowcount, statement);
    }

    @Override
    public void execSQL(String sql) {
        if (myDB == null)
            return;

        Log.trace(log, "execSQL : " + sql);

        Statement statement = null;
        try {
            statement = myDB.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {

            e.printStackTrace();
        } finally {
            try {
                statement.close();
            } catch (SQLException e) {
            }
        }
    }

    @Override
    public long update(String tablename, Parameters val, String whereClause, String[] whereArgs) {

        if (LogLevel.isLogLevel(LogLevel.TRACE)) {
            StringBuilder sb = new StringBuilder("Update @ Table:" + tablename);
            sb.append("Parameters:" + val.toString());
            sb.append("WHERECLAUSE:" + whereClause);

            if (whereArgs != null) {
                for (String arg : whereArgs) {
                    sb.append(arg + ", ");
                }
            }

            Log.trace(log, sb.toString());
        }

        if (myDB == null)
            return 0;

        StringBuilder sql = new StringBuilder();

        sql.append("update ");
        sql.append(tablename);
        sql.append(" set");

        int i = 0;
        for (Entry<String, Object> entry : val.entrySet()) {
            i++;
            sql.append(" ");
            sql.append(entry.getKey());
            sql.append("=?");
            if (i != val.size()) {
                sql.append(",");
            }
        }

        if (!whereClause.isEmpty()) {
            sql.append(" where ");
            sql.append(whereClause);
        }
        PreparedStatement st = null;
        try {
            st = myDB.prepareStatement(sql.toString());

            int j = 0;
            for (Entry<String, Object> entry : val.entrySet()) {
                j++;
                st.setObject(j, entry.getValue());
            }

            if (whereArgs != null) {
                for (int k = 0; k < whereArgs.length; k++) {
                    st.setString(j + k + 1, whereArgs[k]);
                }
            }

            return st.executeUpdate();

        } catch (SQLException e) {
            return 0;
        } finally {
            try {
                st.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }

    }

    @Override
    public long insert(String tablename, Parameters val) {
        if (myDB == null)
            return 0;
        StringBuilder sql = new StringBuilder();

        sql.append("insert into ");
        sql.append(tablename);
        sql.append(" (");

        int i = 0;
        for (Entry<String, Object> entry : val.entrySet()) {
            i++;
            sql.append(" ");
            sql.append(entry.getKey());
            if (i != val.size()) {
                sql.append(",");
            }
        }

        sql.append(" ) Values(");

        for (int k = 1; k <= val.size(); k++) {
            sql.append(" ");
            sql.append("?");
            if (k < val.size()) {
                sql.append(",");
            }
        }

        sql.append(" )");
        PreparedStatement st = null;
        try {
            st = myDB.prepareStatement(sql.toString());

            int j = 0;
            for (Entry<String, Object> entry : val.entrySet()) {
                j++;
                st.setObject(j, entry.getValue());
            }

            Log.trace(log, "INSERT: " + sql);
            return st.execute() ? 0 : 1;

        } catch (SQLException e) {
            return 0;
        } finally {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public long delete(String tablename, String whereClause, String[] whereArgs) {
        if (LogLevel.isLogLevel(LogLevel.TRACE)) {
            StringBuilder sb = new StringBuilder("Delete@ Table:" + tablename);
            sb.append("WHERECLAUSE:" + whereClause);

            if (whereArgs != null) {
                for (String arg : whereArgs) {
                    sb.append(arg + ", ");
                }
            }

            Log.trace(log, sb.toString());
        }

        if (myDB == null)
            return 0;
        StringBuilder sql = new StringBuilder();

        sql.append("delete from ");
        sql.append(tablename);

        if (!whereClause.isEmpty()) {
            sql.append(" where ");
            sql.append(whereClause);
        }
        PreparedStatement st = null;
        try {
            st = myDB.prepareStatement(sql.toString());

            if (whereArgs != null) {
                for (int i = 0; i < whereArgs.length; i++) {
                    st.setString(i + 1, whereArgs[i]);
                }
            }

            return st.executeUpdate();

        } catch (SQLException e) {
            return 0;
        } finally {
            try {
                st.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }

    }

    @Override
    public void beginTransaction() {
        try {
            Log.trace(log, "begin transaction");
            if (myDB != null)
                myDB.setAutoCommit(false);
        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    @Override
    public void setTransactionSuccessful() {
        try {
            Log.trace(log, "set Transaction Successful");
            if (myDB != null)
                myDB.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endTransaction() {
        try {
            Log.trace(log, "endTransaction");
            if (myDB != null)
                myDB.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public long insertWithConflictReplace(String tablename, Parameters val) {
        if (myDB == null)
            return 0;

        Log.trace(log, "insertWithConflictReplace @Table:" + tablename + "Parameters: " + val.toString());
        StringBuilder sql = new StringBuilder();

        sql.append("insert OR REPLACE into ");
        sql.append(tablename);
        sql.append(" (");

        int i = 0;
        for (Entry<String, Object> entry : val.entrySet()) {
            i++;
            sql.append(" ");
            sql.append(entry.getKey());
            if (i != val.size()) {
                sql.append(",");
            }
        }

        sql.append(" ) Values(");

        for (int k = 1; k <= val.size(); k++) {
            sql.append(" ");
            sql.append("?");
            if (k < val.size()) {
                sql.append(",");
            }
        }

        sql.append(" )");
        PreparedStatement st = null;
        try {
            st = myDB.prepareStatement(sql.toString());

            int j = 0;
            for (Entry<String, Object> entry : val.entrySet()) {
                j++;
                st.setObject(j, entry.getValue());
            }

            return st.executeUpdate();

        } catch (SQLException e) {
            return 0;
        } finally {
            try {
                st.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }

    }

    @Override
    public long insertWithConflictIgnore(String tablename, Parameters val) {
        if (myDB == null)
            return 0;

        Log.trace(log, "insertWithConflictIgnore @Table:" + tablename + "Parameters: " + val.toString());

        StringBuilder sql = new StringBuilder();

        sql.append("insert OR IGNORE into ");
        sql.append(tablename);
        sql.append(" (");

        int i = 0;
        for (Entry<String, Object> entry : val.entrySet()) {
            i++;
            sql.append(" ");
            sql.append(entry.getKey());
            if (i != val.size()) {
                sql.append(",");
            }
        }

        sql.append(" ) Values(");

        for (int k = 1; k <= val.size(); k++) {
            sql.append(" ");
            sql.append("?");
            if (k < val.size()) {
                sql.append(",");
            }
        }

        sql.append(" )");
        PreparedStatement st = null;
        try {
            st = myDB.prepareStatement(sql.toString());

            int j = 0;
            for (Entry<String, Object> entry : val.entrySet()) {
                j++;
                st.setObject(j, entry.getValue());
            }

            return st.executeUpdate();

        } catch (SQLException e) {
            return 0;
        } finally {
            try {
                st.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCacheCountInDB(String filename) {

        if (myDB == null)
            return 0;

        int count = 0;
        Connection myDB = null;
        try {
            myDB = DriverManager.getConnection("jdbc:sqlite:" + filename);

            Statement statement = myDB.createStatement();
            ResultSet result = statement.executeQuery("select count(*) from caches");
            // result.first();
            count = result.getInt(1);
            result.close();
            myDB.close();
        } catch (SQLException e) {
            // String s = e.getMessage();
        }
        return count;
    }

}
