package ninja.seibert.sqsconnect.outbound;

import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

public class SqsConnectionFactoryImpl implements SqsConnectionFactory {
    private static final Logger LOGGER = Logger.getLogger(SqsConnectionFactoryImpl.class.getName());

    private ConnectionManager connectionManager;
    private SqsManagedConnectionFactory managedConnectionFactory;

    public SqsConnectionFactoryImpl(ConnectionManager connectionManager, SqsManagedConnectionFactory managedConnectionFactory) {
        this.connectionManager = connectionManager;
        this.managedConnectionFactory = managedConnectionFactory;
    }

    @Override
    public SqsConnection getConnection() throws ResourceException {
        if (connectionManager != null) {
            try {
                return (SqsConnection) connectionManager.allocateConnection(managedConnectionFactory, null);
            } catch (ResourceException e) {
                LOGGER.warning("Couldn't allocate connection: " + e.getMessage());
                throw e;
            }
        } else {
            try {
                return (SqsConnection) managedConnectionFactory.createManagedConnection(null, null)
                        .getConnection(null, null);
            } catch (ResourceException e) {
                LOGGER.warning("Couldn't allocate connection: " + e.getMessage());
                throw e;
            }
        }
    }
}
