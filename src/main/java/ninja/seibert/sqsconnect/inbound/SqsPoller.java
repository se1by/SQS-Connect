package ninja.seibert.sqsconnect.inbound;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

public class SqsPoller implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(SqsPoller.class.getName());
    private static final Method MESSAGE_METHOD;

    static {
        try {
            MESSAGE_METHOD = SqsListener.class.getMethod("onMessage", Message.class);
        } catch (NoSuchMethodException e) {
            LOGGER.severe("Couldn't find onMessage method of SqsListener!");
            throw new RuntimeException(e);
        }
    }

    private MessageEndpointFactory endpointFactory;
    private MessageEndpoint endpoint;
    private SqsActivationSpec activationSpec;
    private AmazonSQS client;

    public SqsPoller(MessageEndpointFactory endpointFactory, SqsActivationSpec activationSpec) {
        this.endpointFactory = endpointFactory;
        this.activationSpec = activationSpec;
        client = AmazonSQSClientBuilder
                .standard()
                .withCredentials(activationSpec)
                .withRegion(activationSpec.getRegion()).build();
    }

    @Override
    public void run() {
        ReceiveMessageRequest request = new ReceiveMessageRequest(activationSpec.getQueueUrl());
        request.setMaxNumberOfMessages(activationSpec.getMaxMessages());
        ReceiveMessageResult result = client.receiveMessage(request);
        if (!result.getMessages().isEmpty()) {
            callOnMessage(result.getMessages());
        }
    }

    private void callOnMessage(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        if (endpoint == null) {
            try {
                endpoint = endpointFactory.createEndpoint(null);
            } catch (UnavailableException e) {
                LOGGER.log(Level.SEVERE, "Couldn't create endpoint: ", e);
            }
        }
        for (Message message : messages) {
            try {
                endpoint.beforeDelivery(MESSAGE_METHOD);
                ((SqsListener) endpoint).onMessage(message);
                endpoint.afterDelivery();
                client.deleteMessage(activationSpec.getQueueUrl(), message.getReceiptHandle());
            } catch (NoSuchMethodException | ResourceException e) {
                LOGGER.log(Level.SEVERE, "Couldn't call beforeDelivery/afterDelivery: ", e);
                return;
            }
        }
    }
}
