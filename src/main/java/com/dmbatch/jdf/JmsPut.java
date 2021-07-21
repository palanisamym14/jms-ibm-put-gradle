package com.dmbatch.jdf;

import java.util.Map;
import java.util.UUID;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.TextMessage;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class JmsPut implements RequestHandler<Map<String,String>,String> {
    String HOST = System.getenv("QMGR_HOST");// Host name or IP address
    String PORT_STRING = System.getenv("QMGR_PORT");
    int PORT = Integer.parseInt(PORT_STRING); // Listener port for your queue manager
    String CHANNEL = System.getenv("QMGR_CHANNEL");
    String QMGR = System.getenv("QMGR_NAME"); // Queue manager name
    String APP_USER = System.getenv("APP_USER"); // User name that application uses to connect to MQ
    String APP_PASSWORD = System.getenv("APP_PASSWORD"); //Password that the application uses to connect to MQ
    String TARGET_QUEUE_NAME = System.getenv("TARGET_QUEUE_NAME"); // Queue that the application uses to put and get messages to and from
    String WMQ_APPLICATION_NAME = System.getenv("WMQ_APPLICATION_NAME"); // Queue that the application uses to put and get messages to and from
//    String RESPONSE_QUEUE_NAME = System.getenv("RESPONSE_QUEUE_NAME"); // Queue that the application uses to put and get messages to and fro



    // Create variables for the connection to MQ
    @Override
    public String handleRequest(Map<String, String> input, Context context) {
        //context.getLogger().log("Input: " + input);

        JMSContext jmscontext = null;
        Destination destination = null;
        Destination source = null;
        JMSProducer producer = null;
        JMSConsumer consumer = null;

        try {
            // Create a connection factory
            System.out.println("\n\n}===========JMS================={\n");

            JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
            JmsConnectionFactory cf = ff.createConnectionFactory();
            System.out.println(CHANNEL);
            System.out.println(HOST);
            System.out.println(PORT);
            System.out.println(QMGR);
            System.out.println(APP_USER);
            System.out.println(APP_PASSWORD);

            // Set the properties
            cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, HOST);
            cf.setIntProperty(WMQConstants.WMQ_PORT, PORT);
            cf.setStringProperty(WMQConstants.WMQ_CHANNEL, CHANNEL);
            cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
            cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, QMGR);
            cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, WMQ_APPLICATION_NAME);
            cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, false);
            cf.setStringProperty(WMQConstants.USERID, APP_USER);
            if(APP_PASSWORD !=null){
                cf.setStringProperty(WMQConstants.PASSWORD, APP_PASSWORD);
            }

            // Create JMS objects
            jmscontext = cf.createContext();
            System.out.println(TARGET_QUEUE_NAME);
            destination = jmscontext.createQueue("queue:///" + TARGET_QUEUE_NAME);

            String inputMessage = input.get("message");
            String jsonbody = "{ \"message\": \"" + inputMessage + "\" }";
            TextMessage message = jmscontext.createTextMessage(jsonbody);

            UUID uuid = UUID.randomUUID();
            String corrid = uuid.toString();
            message.setJMSCorrelationID(corrid);

            System.out.println("\n\n}============================{\n");

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
