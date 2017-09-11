package sqsconnect;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import lombok.Getter;
import lombok.Setter;

import sqsconnect.inbound.SqsActivationSpec;
import sqsconnect.inbound.SqsPoller;

@Getter
@Setter
@Connector(
        displayName = "Amazon SQS JCA Adapter",
        vendorName = "Jonas Seibert",
        version = "1.0-SNAPSHOT",
        eisType = "Amazon SQS",
        transactionSupport = TransactionSupport.TransactionSupportLevel.NoTransaction
)
public class SqsResourceAdapter implements ResourceAdapter {
    private static final Logger LOGGER = Logger.getLogger(SqsResourceAdapter.class.getName());

    private ScheduledExecutorService executor;
    private Map<MessageEndpointFactory, ScheduledFuture> pollerMap;

    @ConfigProperty(description = "Access Key ID", type = String.class)
    private String accessKeyId;

    @ConfigProperty(description = "Secret Access Key", type = String.class, confidential = true)
    private String secretAccessKey;

    @ConfigProperty(description = "The maximum number of messages to pull", type = Integer.class, defaultValue = "10")
    private Integer maxMessages;

    @ConfigProperty(description = "Poll interval in seconds", type = Integer.class, defaultValue = "30")
    private Integer pollInterval = 30;

    @ConfigProperty(description = "Region the queue is hosted in", type = String.class)
    private String region;

    public void start(BootstrapContext context) throws ResourceAdapterInternalException {
        ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(10);
        threadPoolExecutor.setRemoveOnCancelPolicy(true);
        executor = Executors.unconfigurableScheduledExecutorService(threadPoolExecutor);
        pollerMap = new HashMap<>();
        LOGGER.info("Amazon SQS JCA Adapter started");
    }

    public void stop() {
        for (ScheduledFuture future : pollerMap.values()) {
            future.cancel(false);
        }
        executor.shutdown();
        LOGGER.info("Amazon SQS JCA Adapter stopped");
    }

    public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) throws ResourceException {
        if (!(activationSpec instanceof SqsActivationSpec)) {
            throw new ResourceException("Received unknown ActivationSpec " + activationSpec.getClass().getName() + "!");
        }
        SqsActivationSpec sqsActivationSpec = ((SqsActivationSpec) activationSpec);
        Runnable poller = new SqsPoller(messageEndpointFactory, sqsActivationSpec);
        ScheduledFuture future = executor.scheduleAtFixedRate(poller, sqsActivationSpec.getPollInterval(),
                sqsActivationSpec.getPollInterval(), TimeUnit.SECONDS);
        pollerMap.put(messageEndpointFactory, future);
    }

    public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
        ScheduledFuture future = pollerMap.get(messageEndpointFactory);
        future.cancel(false);
        pollerMap.remove(messageEndpointFactory);
    }

    public XAResource[] getXAResources(ActivationSpec[] activationSpecs) throws ResourceException {
        return null;
    }
}
