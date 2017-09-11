package ninja.seibert.sqsconnect.outbound;

import javax.resource.ResourceException;

public interface SqsConnectionFactory {
    SqsConnection getConnection() throws ResourceException;
}
