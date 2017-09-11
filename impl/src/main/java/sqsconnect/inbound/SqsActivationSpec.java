package sqsconnect.inbound;

import javax.resource.ResourceException;
import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

import lombok.Getter;
import lombok.Setter;

import ninja.seibert.sqsconnect.api.inbound.SqsListener;
import sqsconnect.SqsResourceAdapter;

@Getter
@Setter
@Activation(messageListeners = SqsListener.class)
public class SqsActivationSpec implements ActivationSpec, AWSCredentialsProvider {

    private SqsResourceAdapter resourceAdapter;

    private String region;
    private String queueUrl;
    private String accessKeyId;
    private String secretAccessKey;

    private int pollInterval;
    private int maxMessages;


    public void validate() throws InvalidPropertyException {
        if (queueUrl == null || queueUrl.isEmpty()) {
            throw new InvalidPropertyException("queueUrl not set or empty!");
        }
        if (accessKeyId == null || accessKeyId.isEmpty()) {
            throw new InvalidPropertyException("accessKeyId not set or empty!");
        }
        if (secretAccessKey == null || secretAccessKey.isEmpty()) {
            throw new InvalidPropertyException("secretKeyId not set or empty!");
        }
    }

    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    public void setResourceAdapter(ResourceAdapter resourceAdapter) throws ResourceException {
        if (!(resourceAdapter instanceof SqsResourceAdapter)) {
            throw new ResourceException("Tried to associate with unknown ResourceAdapter " + resourceAdapter.getClass().getName());
        }
        this.resourceAdapter = (SqsResourceAdapter) resourceAdapter;
        region = ((SqsResourceAdapter) resourceAdapter).getRegion();
        accessKeyId = ((SqsResourceAdapter) resourceAdapter).getAccessKeyId();
        secretAccessKey = ((SqsResourceAdapter) resourceAdapter).getSecretAccessKey();
        pollInterval = ((SqsResourceAdapter) resourceAdapter).getPollInterval();
        maxMessages = ((SqsResourceAdapter) resourceAdapter).getMaxMessages();
    }

    public AWSCredentials getCredentials() {
        return new BasicAWSCredentials(accessKeyId, secretAccessKey);
    }

    public void refresh() {

    }
}
