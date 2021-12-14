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
                        propertyValue = EgrulEgripProcessingMDB.EGRUL_EGRIP_PROCESSING_QUEUE_NAME)
        })

public class MessageProcessingMDB implements MessageListener {
    private static final Log LOG = LogFactory.getLog(EgrulEgripProcessingMDB.class);
    protected static final String EGRUL_EGRIP_PROCESSING_QUEUE_NAME = "jms/EgrulEgripProcessingQueue";

    @EJB
    private MessageProcessLocal processEjb;

    @Override
    public void onMessage(Message message) {
        if (LOG.isInfoEnabled()) {
            LOG.info("EgrulEgripProcessingMDB message received.");
        }

        if (!(message instanceof TextMessage)) {
            LOG.warn("Wrong message type '" + message.getClass().getName() + "'." +
                    "Type 'TextMessage' is expected.");
            return;
        }

        final TextMessage textMessage = (TextMessage) message;

        try {
            String fileStorageId = textMessage.getStringProperty("FILE_STORAGE_ID");
            String isEgrul = textMessage.getStringProperty("IS_EGRUL");
            processEjb.process(new Long(fileStorageId), "1".equals(isEgrul) ? true : false);
        } catch (JMSException e) {
            LOG.error("EgrulEgripProcessingMDB Error while getting object from message.", e);
        } catch (Exception e) {
            LOG.error("EgrulEgripProcessingMDB Error while getting object from message.", e);
        }
    }
}
