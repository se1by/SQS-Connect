# SQS-Connect
is a JCA adapter for Amazon SQS.

---

# Using
To use the connector in your project add the following dependency to your pom.xml:

```xml
<dependency>
    <groupid>ninja.seibert</groupid>
    <artifactId>sqsconnect-api</artifactId>
    <version>1.0</version>
</dependency>
```
**Note: This will work once this project is uploaded to maven central. Until then, you'll have to build it yourself!**

After that you should deploy the rar archive to your application server.

## Inbound
To receive messages you have to create an MDB like this:

```java
@MessageDriven(activationConfig = {
	@ActivationConfigProperty(propertyName = "accessKeyId", propertyValue = "<yourAccessKeyId>"),
   	@ActivationConfigProperty(propertyName = "secretAccessKey", propertyValue = "<yourSecretAccessKey>"),
  	@ActivationConfigProperty(propertyName = "region", propertyValue = "<yourRegion>"),
    @ActivationConfigProperty(propertyName = "queueURL", propertyValue = "<yourQueueUrl>")
})
public class SqsMessageReceiver implements SqsListener {

    @Override
    public void onMessage(Message message) {
    	//Your message processing code here
    }
}
```
If you already set accessKeyId, secretAccessKey or region in the resource adapter config you can omit them here.

## Outbound
You can create an outbound connection by creating an SqsConnectionFactory (either in your application server or by using the `@ConnectionDefinition` annotation). After that you can send messages like this:

```java
try (SqsConnection connection = factory.getConnection()) {
    connection.sendMessage(new SendMessageRequest("<yourQueueUrl>", "<yourContent>"));
} catch (ResourceException e) {
    //Problem while allocating the connection
} catch (Exception e) {
    //General problem while sending the message or closing the connection
}
```

---

# Configuration Properties
You can define the following configuration properties in the resource adapter config or ConnectionFactory setup of your application server to be able to omit them in the annotations.

Property|Type|Default|Description
------- | -- | ----- | ----------
accessKeyId | String | none | The accessKeyId used to connect to SQS.
secretAccessKey | String | none | The secretAccessKey used to connect to SQS.
maxMessages| Integer | 10 | Maximum number of messages to pull at once (min. 1, max. 10).
pollInterval | Integer | 30 | Number of seconds to wait between polls.
region | String | none | Region your queue is hosted in.

Note that `maxMessages` and `pollInterval` only apply to MDB configuration.

---

# Building

You can build the adapter yourself by cloning this repo

```shell
git clone https://github.com/se1by/SQS-Connect.git
```

and executing

```shell
mvn clean package rar
```

in the project root.