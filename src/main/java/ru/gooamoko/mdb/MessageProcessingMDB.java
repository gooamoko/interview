package ru.gooamoko.mdb;

import ru.gooamoko.ejb.MessageProcessLocal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(
        activationConfig = {
                @ActivationConfigProperty(propertyName = "destinationType",
                        propertyValue = "javax.jms.Queue"),
                @ActivationConfigProperty(propertyName = "destination",
                        propertyValue = MessageProcessingMDB.QUEUE_NAME)
        })

public class MessageProcessingMDB implements MessageListener {
    private static final Log LOG = LogFactory.getLog(MessageProcessingMDB.class);
    protected static final String QUEUE_NAME = "jms/MessageProcessingQueue";

    @EJB
    private MessageProcessLocal processEjb;

    @Override
    public void onMessage(Message message) {
        if (LOG.isInfoEnabled()) {
            LOG.info("MessageProcessingMDB message received.");
        }

        if (!(message instanceof TextMessage)) {
            LOG.warn("Wrong message type '" + message.getClass().getName() + "'." +
                    "Type 'TextMessage' is expected.");
            return;
        }

        final TextMessage textMessage = (TextMessage) message;

        try {
            String fileStorageId = textMessage.getStringProperty("FILE_STORAGE_ID");
            String isJson = textMessage.getStringProperty("IS_JSON");
            processEjb.process(new Long(fileStorageId), "1".equals(isJson) ? true : false);
        } catch (JMSException e) {
            LOG.error("MessageProcessingMDB Error while getting object from message.", e);
        } catch (Exception e) {
            LOG.error("MessageProcessingMDB Error while getting object from message.", e);
        }
    }
}
