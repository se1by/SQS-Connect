package ninja.seibert.sqsconnect.api.inbound;

import com.amazonaws.services.sqs.model.Message;

public interface SqsListener {
    void onMessage(Message message);
}
