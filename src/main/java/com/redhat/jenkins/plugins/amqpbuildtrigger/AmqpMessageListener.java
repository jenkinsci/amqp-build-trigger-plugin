package com.redhat.jenkins.plugins.amqpbuildtrigger;

import java.util.Set;
import java.util.logging.Logger;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.BytesMessage;

public class AmqpMessageListener implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(AmqpBuildTrigger.class.getName());
    private final AmqpBrokerParams brokerParams;
    private final Set<AmqpBuildTrigger> triggers;

    public AmqpMessageListener(AmqpBrokerParams brokerParams, Set<AmqpBuildTrigger> triggers) {
        this.brokerParams = brokerParams;
        this.triggers = triggers;
    }

    @Override
    public void onMessage(Message message) {
        try {
            LOGGER.info("Message received on broker " + brokerParams.toString() + "; msg=" + message.toString());
            for (AmqpBuildTrigger t : triggers) {
                LOGGER.info("Remote build triggered: " + t.getProjectName());
                t.scheduleBuild(brokerParams.toString(), getMessageContent(message));
            }
        } catch (Exception e) {
            LOGGER.warning("Exception thrown in RemoteBuildListener.onMessage(): " + e.getMessage());
        }
    }
    // method if message is a BytesMessage, then convert it to a String
    private String getMessageContent(Message message) {
        if (message instanceof BytesMessage) {
            try {
                BytesMessage bytesMessage = (BytesMessage) message;
                byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                bytesMessage.readBytes(bytes);
                return new String(bytes, "UTF-8");
            } catch (Exception e) {
                LOGGER.warning("Exception thrown in RemoteBuildListener.getMessageContent(): " + e.getMessage());
            }
        }
        return message.toString();
    }
}
