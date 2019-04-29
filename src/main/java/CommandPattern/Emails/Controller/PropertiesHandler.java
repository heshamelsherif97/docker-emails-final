package CommandPattern.Emails.Controller;

import CommandPattern.Command;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class PropertiesHandler implements Command {
    static Properties prop = new Properties();
    static OutputStream output;
    static InputStream input;

    public static void loadPropertiesHandler(){
        try {

            output = new FileOutputStream("./src/main/java/CommandPattern/Emails/Controller/config.properties");
            input = new FileInputStream("./src/main/java/CommandPattern/Emails/Controller/config.properties");

            // set the properties value
            prop.setProperty("max_db_threads", "20");
            prop.setProperty("max_app_threads", "20");
            prop.setProperty("freeze", "false");
            //prop.setProperty("Db_URL", "jdbc:postgresql://192.168.0.111:5432/");
            //prop.setProperty("Db_URL2","192.168.0.111");
            prop.setProperty("CDatabase_url","localhost");
            prop.setProperty("Db_URL2","localhost");
            prop.setProperty("restart", "false");
            prop.setProperty("UsersApp","35.188.200.142");
            prop.setProperty("UsersPort","80");

            prop.store(output, null);
        } catch (Exception e) {
        }
    }
    public JSONObject execute(JSONObject o){
        String key = o.getString("key");
        String value = o.getString("value");
        return addProperty(key, value);
    }




    public static JSONObject addProperty(String key, String val) {
        JSONObject result ;
        try {
            input = new FileInputStream("./src/main/java/CommandPattern/Emails/Controller/config.properties");
            output = new FileOutputStream("./src/main/java/CommandPattern/Emails/Controller/config.properties");
            prop.load(input);
            prop.setProperty(key, val);
            System.out.println("in addProperty");
            prop.store(output, null);
            result = new JSONObject("{ \"message\" : \"Added Property\" }");
        } catch(Exception e) {
            System.out.println("error in addProperty");
            System.out.println(e);
            result=  new JSONObject("{ \"message\" : \"Error in adding property\" }");
        }
        return result;
    }

    public static String getProperty(String key) {
        try{
            input = new FileInputStream("./src/main/java/CommandPattern/Emails/Controller/config.properties");
            prop.load(input);
            return prop.getProperty(key);
        } catch(Exception e) {
            return "error";
        }
    }
}
