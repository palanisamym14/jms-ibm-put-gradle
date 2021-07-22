package com.dmbatch.jdf;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import javax.jms.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JmsPut implements RequestHandler<RequestQueue, Object> {

    // Create variables for the connection to MQ
    @Override
    public Object handleRequest(RequestQueue input, Context context) {
        //context.getLogger().log("Input: " + input);

        JMSContext jmscontext = null;
        Destination destination = null;
        Destination source = null;
        JMSProducer producer = null;
        JMSConsumer consumer = null;

        try {
            input.validateInput();
            String HOST = input.connection.getHost(); // Host name or IP address
            String PORT_STRING = input.connection.getPort();
            int PORT = Integer.parseInt(PORT_STRING); // Listener port for your queue manager
            String CHANNEL = input.connection.getChannel();
            String QMGR = input.connection.getQueueManagerName(); // Queue manager name
            String APP_USER = input.connection.getAppUser(); // User name that application uses to connect to MQ
            String APP_PASSWORD = input.connection.getAppPassword(); //Password that the application uses to connect to MQ
            String TARGET_QUEUE_NAME = input.connection.getQueueName(); // Queue that the application uses to put and get messages to and from
            String WMQ_APPLICATION_NAME = input.connection.getApplicationName();
            // Create a connection factory
            JmsFactoryFactory ff = JmsFactoryFactory.getInstance(
                    WMQConstants.WMQ_PROVIDER
            );
            JmsConnectionFactory cf = ff.createConnectionFactory();

            // Set the properties
            cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, HOST);
            cf.setIntProperty(WMQConstants.WMQ_PORT, PORT);
            cf.setStringProperty(WMQConstants.WMQ_CHANNEL, CHANNEL);
            cf.setIntProperty(
                    WMQConstants.WMQ_CONNECTION_MODE,
                    WMQConstants.WMQ_CM_CLIENT
            );
            cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, QMGR);
            cf.setStringProperty(
                    WMQConstants.WMQ_APPLICATIONNAME,
                    WMQ_APPLICATION_NAME
            );
            cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, false);
            cf.setStringProperty(WMQConstants.USERID, APP_USER);
            if (APP_PASSWORD != null) {
                cf.setStringProperty(WMQConstants.PASSWORD, APP_PASSWORD);
            }

            // Create JMS objects
            jmscontext = cf.createContext();
            destination = jmscontext.createQueue("queue:///" + TARGET_QUEUE_NAME);

            String[] inputMessage = input.getMessage();
            List<Object> response = null;
            for (String tempMsg : inputMessage) {
                Map<String, Object> responseObj = null;
                System.out.println("\nRest request received with body: " + tempMsg);
                TextMessage message = jmscontext.createTextMessage(tempMsg);

                UUID uuid = UUID.randomUUID();
                String corrid = uuid.toString();
                message.setJMSCorrelationID(corrid);
                responseObj.put("message", tempMsg);
                try {
                    producer = jmscontext.createProducer();
                    // producer.setTimeToLive(30000);
                    producer.send(destination, message);
                    System.out.println("\nPut message to STOCK queue: " + tempMsg);
                    responseObj.put("status", "success");
                    responseObj.put("stack", message.toString());
                } catch (Exception jmsex) {
                    jmsex.printStackTrace();
                    System.out.println("\nPut message Failed STOCK queue: " + tempMsg);
                    responseObj.put("stack", message.toString());
                }
                response.add(responseObj);
            }
            Map<String, Object> res = null;
            res.put("status", "success");
            res.put("body", response);
            return res;
        } catch (JMSException jmsex) {
            jmsex.printStackTrace();
            Map<String, Object> res = null;
            res.put("requestBody", input);
            res.put("status", "failure");
            return res;
        }
    }
}
