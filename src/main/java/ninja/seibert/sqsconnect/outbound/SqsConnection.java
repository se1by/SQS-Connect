package ninja.seibert.sqsconnect.outbound;

import java.util.List;

import javax.resource.ResourceException;

import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

public interface SqsConnection extends AutoCloseable {
    SendMessageResult sendMessage(String queueUrl, String messageBody);

    SendMessageResult sendMessage(SendMessageRequest sendMessageRequest);

    SendMessageBatchResult sendMessageBatch(String queueUrl, List<SendMessageBatchRequestEntry> requestEntries);

    SendMessageBatchResult sendMessageBatch(SendMessageBatchRequest sendMessageBatchRequest);

    void cleanup() throws ResourceException;
}
