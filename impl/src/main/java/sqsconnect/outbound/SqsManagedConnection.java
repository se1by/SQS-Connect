package sqsconnect.outbound;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.Connector;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

import sqsconnect.SqsResourceAdapter;
import ninja.seibert.sqsconnect.api.outbound.SqsConnection;

public class SqsManagedConnection implements ManagedConnection, SqsConnection {
    private AmazonSQS client;
    private String accessKeyId;
    private List<SqsConnection> connections;
    private List<ConnectionEventListener> listeners;
    private PrintWriter logWriter;

    public SqsManagedConnection(SqsManagedConnectionFactory managedConnectionFactory) {
        client = AmazonSQSClientBuilder.standard()
                .withCredentials(managedConnectionFactory)
                .withRegion(managedConnectionFactory.getRegion())
                .build();
        accessKeyId = managedConnectionFactory.getAccessKeyId();
        connections = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        SqsConnectionImpl connection = new SqsConnectionImpl(this);
        connections.add(connection);
        return connection;
    }

    @Override
    public void destroy() throws ResourceException {
        cleanup();
        client = null;
    }

    @Override
    public void cleanup() throws ResourceException {
        for (SqsConnection connection : connections) {
            connection.cleanup();
        }
        connections.clear();
    }

    @Override
    public void associateConnection(Object o) throws ResourceException {
        if (!(o instanceof SqsConnection)) {
            throw new ResourceException("Tried to associate " + o.getClass().getName() + " with SqsManagedConnection!");
        }
        //we guess that there is only that impl
        //TODO: find a nicer solution
        ((SqsConnectionImpl) o).setManagedConnection(this);
        connections.add(((SqsConnection) o));
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener connectionEventListener) {
        listeners.add(connectionEventListener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener connectionEventListener) {
        listeners.remove(connectionEventListener);
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        throw new NotSupportedException("XAResource is not supported!");
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        throw new NotSupportedException("LocalTransaction is not supported!");
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        final Connector connector = SqsResourceAdapter.class.getAnnotation(Connector.class);
        return new ManagedConnectionMetaData() {
            @Override
            public String getEISProductName() throws ResourceException {
                return connector.eisType();
            }

            @Override
            public String getEISProductVersion() throws ResourceException {
                return connector.version();
            }

            @Override
            public int getMaxConnections() throws ResourceException {
                return 0;
            }

            @Override
            public String getUserName() throws ResourceException {
                return accessKeyId;
            }
        };
    }

    @Override
    public void setLogWriter(PrintWriter printWriter) throws ResourceException {
        this.logWriter = printWriter;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    @Override
    public SendMessageResult sendMessage(String queueUrl, String messageBody) {
        return client.sendMessage(queueUrl, messageBody);
    }

    @Override
    public SendMessageResult sendMessage(SendMessageRequest sendMessageRequest) {
        return client.sendMessage(sendMessageRequest);
    }

    @Override
    public SendMessageBatchResult sendMessageBatch(String queueUrl, List<SendMessageBatchRequestEntry> requestEntries) {
        return sendMessageBatch(queueUrl, requestEntries);
    }

    @Override
    public SendMessageBatchResult sendMessageBatch(SendMessageBatchRequest sendMessageBatchRequest) {
        return sendMessageBatch(sendMessageBatchRequest);
    }

    public void removeConnection(SqsConnection connection) {
        connections.remove(connection);
        for (ConnectionEventListener listener : listeners) {
            listener.connectionClosed(new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED));
        }
    }

    @Override
    public void close() throws Exception {
        destroy();
    }
}
