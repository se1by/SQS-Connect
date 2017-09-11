package ninja.seibert.sqsconnect.api.outbound;

import javax.resource.ResourceException;

public interface SqsConnectionFactory {
    SqsConnection getConnection() throws ResourceException;
}
