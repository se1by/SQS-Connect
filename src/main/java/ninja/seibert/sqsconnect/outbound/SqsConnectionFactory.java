package ninja.seibert.sqsconnect.outbound;

public interface SqsConnectionFactory {
    SqsConnection getConnection();
}
