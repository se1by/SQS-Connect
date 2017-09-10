package ninja.seibert.sqsconnect.inbound;

import com.amazonaws.services.sqs.model.Message;

public interface SqsListener {
    void onMessage(Message message);
}
