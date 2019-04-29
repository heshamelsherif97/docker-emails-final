package CommandPattern.Emails;

import CommandPattern.Command;
import CommandPattern.Emails.Controller.PropertiesHandler;
import com.rabbitmq.client.*;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public class RPCServer {

    private static String freeze;

    private static final String RPC_QUEUE_NAME = "emails";
    private static RPCServer instance = new RPCServer();
    public static RPCServer getInstance(){return instance;};
    static ExecutorService executor = AppThreadPool.getInstance();

    public static void main(String[] argv) {


        ConnectionFactory factory = new ConnectionFactory();
        //TODO: Get ip:port from config file
        //factory.setHost("localhost");
        String port = "";
        String url = "" ;
        Properties conf = new Properties();
        try {

            File file = new File("./src/main/java/CommandPattern/Emails/Controller/config.properties");
            FileInputStream confLoc = new FileInputStream(file);
            conf.load(confLoc);
            url = conf.getProperty("UsersApp");
            port = conf.getProperty("UsersPort");
            System.out.print(url + "test");

        }
        catch(Exception e)
        {
            System.out.print("DB ip not found");
            url = "localhost";
            port = "80";

        }
        factory.setHost(url);
        factory.setPort(Integer.parseInt((port)));
        Connection connection = null;
        CommandMap.instantiate();

        Runnable task = new Runnable() {
            public void run() {
                try {
                    (new EmptySpamPeriodicallyCommand()).execute(new JSONObject());
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        };

        executor.submit(task);


        try {
            //set up connection
            connection = factory.newConnection();
            final Channel channel = connection.createChannel();

            //declare queue
            channel.queueDeclare(RPC_QUEUE_NAME, true, false, false, null);

            //attempt to split work between workers evenly
            channel.basicQos(1);

            System.out.println(" [x] Awaiting RPC requests");



            //what the server should do when it receives a message, will be added to channel.basicConsume later
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                   executor = AppThreadPool.update();
                    System.out.println("Threads " + AppThreadPool.getNumberOfThreads());

                 //   System.out.println("Freeze " + freeze);

                    while(true) {
                        freeze = PropertiesHandler.getProperty("freeze");
                        if (freeze.equals("false")) {
                            System.out.println("Freeze " + freeze);
                            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                                    .Builder()
                                    .correlationId(properties.getCorrelationId())
                                    .build();
                            System.out.println("Responding to corrID: " + properties.getCorrelationId());

                            String message = new String(body, "UTF-8");
                            Runnable task = new Runnable() {
                                public void run() {
                                    try {
                                        createResponse(message, channel, properties, replyProps, envelope);
                                    } catch (Exception e) {
                                        System.out.println(e.toString());
                                    }
                                }
                            };

                            Future<?> executorFuture;
                            executorFuture = executor.submit(task);

                            //System.out.println("Not Done");

                        /*while(!executorFuture.isDone()){

                        }*/

                            //System.out.println("Done");
                            //executor.shutdown();
                            // RabbitMq consumer worker thread notifies the RPC server owner thread
                            synchronized (this) {
                                this.notify();
                            }

                            break;
                        }
                    }
                }
            };

            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
            // Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized (consumer) {
                    try {
                        consumer.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                try {
                    connection.close();
                } catch (IOException _ignore) {
                }
        }
    }

    public static void restart(){
        try {
            Runtime.getRuntime().exec("java -jar myApp.jar");
            System.exit(0);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public  static void createResponse(String message, Channel channel, AMQP.BasicProperties properties, AMQP.BasicProperties replyProps, Envelope envelope)  throws IOException{

        JSONObject response = new JSONObject();

        try {
            JSONObject jsonObject;
            System.out.print(message);
            try {
               jsonObject = new JSONObject(message);
            } catch (Exception e){
               jsonObject = new JSONObject("{ \"message\": \"Error\"}");
            }

//                        System.out.println(CommandMap.cmdMap);
            String method = jsonObject.getString("method");
            Command x  = (Command) CommandMap.queryClass(method).newInstance();
            response = x.execute(jsonObject);
            System.out.println("RESPONSE: ");
            System.out.println(response);

            Command command = null;


        } catch (Exception e) {
            e.printStackTrace();
            String reply = e.toString();
            response = new JSONObject(reply);
            System.out.println(" [.] " + e.toString());
        } finally {
            //how and where and what to reply
            channel.basicPublish("", properties.getReplyTo(), replyProps, response.toString().getBytes("UTF-8"));
            channel.basicAck(envelope.getDeliveryTag(), false);
        }
    }
}
