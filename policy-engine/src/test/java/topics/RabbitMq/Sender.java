package topics.RabbitMq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Panagiotis Parthenis
 */
public class Sender {

  public static void main(String[] args) throws IOException, TimeoutException {

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost"); // otherwise IP address
    factory.setPort(5672);
    Connection connection = factory.newConnection();

    //channel with API
    Channel channel = connection.createChannel();

    //declare queue
    //1. queue name
    //2.

    String msg="this is test";
    channel.queueDeclare("myQueue", false, false, false, null);

    //message content in byte array
    channel.basicPublish("", "myQueue", null, msg.getBytes());

    //print info
    System.out.println(" [x] Sent '" + msg + "'");

    //close channel
    channel.close();
    //close connection
    connection.close();
  }
}

