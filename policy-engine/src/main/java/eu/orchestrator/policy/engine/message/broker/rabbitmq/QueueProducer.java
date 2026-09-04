package eu.orchestrator.policy.engine.message.broker.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Panagiotis Parthenis
 */
public class QueueProducer {

  private QueueProducer() {

  }

  public static void pushIntoRabbitMQQueue(String queueName, String message, String rabbitmqUrl, String rabbitmqPort) throws IOException, TimeoutException {
    //socket connection
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(rabbitmqUrl);
    factory.setPort(Integer.valueOf(rabbitmqPort));

    try (Connection connection = factory.newConnection()){

      try (Channel channel = connection.createChannel()){

        //declare queue
        channel.queueDeclare(queueName, false, false, false, null);

        //message content in byte array
        channel.basicPublish("", queueName, null, message.getBytes());
      }

    }

  }
}