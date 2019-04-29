import CommandPattern.Emails.Controller.Controller;
import CommandPattern.Emails.Controller.PropertiesHandler;
import CommandPattern.Emails.RPCServer;

public class Microservice {
public static void main(String [] args)
{
    RPCServer x = RPCServer.getInstance();
    Controller r = Controller.getInstance();
    PropertiesHandler.loadPropertiesHandler();
    try{

        x.main(args);
        r.main(args);

    }
    catch(Exception e){
        e.printStackTrace();
    }
    //Thread.sleep(60000);



}

}

