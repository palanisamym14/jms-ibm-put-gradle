package com.dmbatch.jdf;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import javax.jms.*;
import java.util.UUID;


public class JmsPut implements RequestHandler<RequestQueue, String> {


    // Create variables for the connection to MQ
    @Override
    public String handleRequest(RequestQueue input, Context context) {
        //context.getLogger().log("Input: " + input);

        JMSContext jmscontext = null;
        Destination destination = null;
        Destination source = null;
        JMSProducer producer = null;
        JMSConsumer consumer = null;

        try {
            input.validateInput();
            String HOST = input.connection.getHost();// Host name or IP address
            String PORT_STRING = input.connection.getPort();
            int PORT = Integer.parseInt(PORT_STRING); // Listener port for your queue manager
            String CHANNEL = input.connection.getChannel();
            String QMGR = input.connection.getQueueManagerName(); // Queue manager name
            String APP_USER = input.connection.getAppUser(); // User name that application uses to connect to MQ
            String APP_PASSWORD = input.connection.getAppPassword(); //Password that the application uses to connect to MQ
            String TARGET_QUEUE_NAME = input.connection.getQueueName(); // Queue that the application uses to put and get messages to and from
            String WMQ_APPLICATION_NAME = input.connection.getApplicationName();
            // Create a connection factory

            System.out.println(input.connection.getHost());
            System.out.println(input.connection.getPort());
            System.out.println(input.connection.getChannel());
            System.out.println(input.connection.getQueueManagerName());
            System.out.println(input.connection.getAppUser());
            System.out.println(input.connection.getAppPassword());
            System.out.println(input.connection.getQueueName());
            System.out.println(input.connection.getApplicationName());
            JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
            JmsConnectionFactory cf = ff.createConnectionFactory();

            // Set the properties
            cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, HOST);
            cf.setIntProperty(WMQConstants.WMQ_PORT, PORT);
            cf.setStringProperty(WMQConstants.WMQ_CHANNEL, CHANNEL);
            cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
            cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, QMGR);
            cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, WMQ_APPLICATION_NAME);
            cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, false);
            cf.setStringProperty(WMQConstants.USERID, APP_USER);
            if (APP_PASSWORD != null) {
                cf.setStringProperty(WMQConstants.PASSWORD, APP_PASSWORD);
            }

            // Create JMS objects
            jmscontext = cf.createContext();
            System.out.println(TARGET_QUEUE_NAME);
            destination = jmscontext.createQueue("queue:///" + TARGET_QUEUE_NAME);

            String inputMessage = input.getMessage();
            String jsonbody = "{ \"message\": \"" + inputMessage + "\" }";
            TextMessage message = jmscontext.createTextMessage(jsonbody);

            UUID uuid = UUID.randomUUID();
            String corrid = uuid.toString();
            message.setJMSCorrelationID(corrid);
            System.out.println("\nRest request received with body: " + jsonbody);


            producer = jmscontext.createProducer();
            producer.setTimeToLive(30000);

            producer.send(destination, message);
            System.out.println("\nPut message to STOCK queue: " + jsonbody);

            return message.toString();

        } catch (JMSException jmsex) {
            jmsex.printStackTrace();
            return "THIS FAILED IT SEEMS! Rest body: " + input.toString();
        }
    }
}
