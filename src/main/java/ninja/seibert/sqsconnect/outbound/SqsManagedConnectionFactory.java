package ninja.seibert.sqsconnect.outbound;

import java.io.PrintWriter;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

import lombok.Getter;
import lombok.Setter;

import ninja.seibert.sqsconnect.SqsResourceAdapter;

@Getter
@Setter
@ConnectionDefinition(
        connectionFactory = SqsConnectionFactory.class,
        connectionFactoryImpl = SqsConnectionFactoryImpl.class,
        connection = SqsConnection.class,
        connectionImpl = SqsConnectionImpl.class
)
public class SqsManagedConnectionFactory implements ManagedConnectionFactory, AWSCredentialsProvider, ResourceAdapterAssociation {
    private SqsResourceAdapter resourceAdapter;
    private PrintWriter logWriter;

    @ConfigProperty(description = "Access Key ID", type = String.class)
    private String accessKeyId;

    @ConfigProperty(description = "Secret Access Key", type = String.class, confidential = true)
    private String secretAccessKey;

    @ConfigProperty(description = "Region the queue is hosted in", type = String.class)
    private String region;

    @Override
    public Object createConnectionFactory(ConnectionManager connectionManager) throws ResourceException {
        return new SqsConnectionFactoryImpl(connectionManager, this);
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        return new SqsConnectionFactoryImpl(null, this);
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        return new SqsManagedConnection(this);
    }

    @Override
    public ManagedConnection matchManagedConnections(Set set, Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        return (ManagedConnection) set.toArray()[0];
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
    public AWSCredentials getCredentials() {
        return new BasicAWSCredentials(accessKeyId, secretAccessKey);
    }

    @Override
    public void refresh() {

    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter resourceAdapter) throws ResourceException {
        if (!(resourceAdapter instanceof SqsResourceAdapter)) {
            throw new ResourceException("Tried to associate with unknown ResourceAdapter " + resourceAdapter.getClass().getName());
        }
        this.resourceAdapter = (SqsResourceAdapter) resourceAdapter;
        accessKeyId = ((SqsResourceAdapter) resourceAdapter).getAccessKeyId();
        secretAccessKey = ((SqsResourceAdapter) resourceAdapter).getSecretAccessKey();
        region = ((SqsResourceAdapter) resourceAdapter).getRegion();
    }
}
