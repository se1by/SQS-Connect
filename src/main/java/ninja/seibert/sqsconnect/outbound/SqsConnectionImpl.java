package ninja.seibert.sqsconnect.outbound;

import java.util.List;

import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

public class SqsConnectionImpl implements SqsConnection {
    private SqsManagedConnection managedConnection;

    public SqsConnectionImpl(SqsManagedConnection managedConnection) {
        this.managedConnection = managedConnection;
    }

    @Override
    public SendMessageResult sendMessage(String queueUrl, String messageBody) {
        if (managedConnection == null) {
            throw new IllegalStateException("ManagedConnection is null (called after cleanup?)!");
        }
        return managedConnection.sendMessage(queueUrl, messageBody);
    }

    @Override
    public SendMessageResult sendMessage(SendMessageRequest sendMessageRequest) {
        if (managedConnection == null) {
            throw new IllegalStateException("ManagedConnection is null (called after cleanup?)!");
        }
        return managedConnection.sendMessage(sendMessageRequest);
    }

    @Override
    public SendMessageBatchResult sendMessageBatch(String queueUrl, List<SendMessageBatchRequestEntry> requestEntries) {
        if (managedConnection == null) {
            throw new IllegalStateException("ManagedConnection is null (called after cleanup?)!");
        }
        return managedConnection.sendMessageBatch(queueUrl, requestEntries);
    }

    @Override
    public SendMessageBatchResult sendMessageBatch(SendMessageBatchRequest sendMessageBatchRequest) {
        if (managedConnection == null) {
            throw new IllegalStateException("ManagedConnection is null (called after cleanup?)!");
        }
        return managedConnection.sendMessageBatch(sendMessageBatchRequest);
    }

    @Override
    public void cleanup() {
        managedConnection = null;
    }

    @Override
    public void close() throws Exception {
        managedConnection.removeConnection(this);
        managedConnection = null;
    }

    public void setManagedConnection(SqsManagedConnection managedConnection) {
        this.managedConnection = managedConnection;
    }
}
