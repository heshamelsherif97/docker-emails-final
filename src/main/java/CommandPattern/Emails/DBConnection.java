package CommandPattern.Emails;

import CommandPattern.Emails.Controller.PropertiesHandler;
import org.postgresql.ds.PGPoolingDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Properties;

public class DBConnection {
    private static DBConnection dbInstance = new DBConnection();
    private static Connection con;
    private static PGPoolingDataSource source;
   private static int maxNumber;

    private DBConnection() {


        String url = "" ;
        Properties conf = new Properties();
        try {

            File file = new File("./src/main/java/CommandPattern/Emails/Controller/config.properties");
            FileInputStream confLoc = new FileInputStream(file);
            conf.load(confLoc);
            url = conf.getProperty("CDatabase_url");
            System.out.print(url + "test");
        }
        catch(Exception e)
        {
            System.out.print("DB ip not found");
            url = " ";

        }
        // private constructor //
        source = new PGPoolingDataSource();
        source.setServerName(url);
//        source.setServerName("156.223.15.15");
        source.setPortNumber(26257);
        source.setDatabaseName("emails?sslmode=disable");
        source.setUser("testuser");
        source.setPassword("");
        maxNumber = Integer.parseInt(PropertiesHandler.getProperty("max_db_threads"));
        source.setMaxConnections(maxNumber);
    }
    public static DBConnection getInstance() {
        return dbInstance;
    }
    public Connection getConnection() {
        try {
            return source.getConnection();
        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }
    }

    public static void setMaxNumber(int x)
    {
        PropertiesHandler.addProperty("max_db_threads", x+"");
        maxNumber = x;
        source.setMaxConnections(maxNumber);
    }
}