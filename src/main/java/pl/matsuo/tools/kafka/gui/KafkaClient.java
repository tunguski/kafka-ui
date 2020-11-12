package pl.matsuo.tools.kafka.gui;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.kafka.common.config.TopicConfig.CLEANUP_POLICY_COMPACT;
import static org.apache.kafka.common.config.TopicConfig.CLEANUP_POLICY_CONFIG;

import java.io.Closeable;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;

@Slf4j
@Getter
public class KafkaClient implements Closeable {

  Properties props;
  Admin admin;

  public KafkaClient(Properties props) {
    log.info("Creating admin client");
    this.props = props;
    admin = Admin.create(props);
  }

  public static void main(String[] args) throws Exception {
    Properties props = new Properties();
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094");
    props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "3000");
    props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "3000");

    try (KafkaClient client = new KafkaClient(props)) {
      client.createTopic();
      client.printAll();
    }
  }

  private void printAll() throws InterruptedException, ExecutionException, TimeoutException {
    log.info("Topics list\n");
    admin
        .listTopics()
        .namesToListings()
        .get(3, SECONDS)
        .forEach(
            (name, listing) -> {
              log.info("Topic: " + name);
              log.info("Listing: " + listing);
            });

    log.info("Partition reassignments\n");
    admin
        .listPartitionReassignments()
        .reassignments()
        .get(3, SECONDS)
        .forEach(
            (partition, reassignment) -> {
              log.info("Partition: " + partition);
              log.info("Reassignment: " + reassignment);
            });

    log.info("Topics descriptions\n");
    admin
        .describeTopics(admin.listTopics().names().get())
        .all()
        .get(3, SECONDS)
        .forEach(
            (name, topicDescription) -> {
              log.info("Topic: " + name);
              log.info("Description: " + topicDescription);
            });

    log.info("Consumer groups\n");
    admin
        .listConsumerGroups()
        .all()
        .get(3, SECONDS)
        .forEach(
            consumerGroupListing -> {
              log.info("Consumer group listing: " + consumerGroupListing);
            });
  }

  public void createTopic() throws InterruptedException, ExecutionException {
    String topicName = "my-topic-" + new Random().nextInt();
    int partitions = 12;
    short replicationFactor = 1;
    // Create a compacted topic
    NewTopic newTopic =
        new NewTopic(topicName, partitions, replicationFactor)
            .configs(singletonMap(CLEANUP_POLICY_CONFIG, CLEANUP_POLICY_COMPACT));
    CreateTopicsResult result = admin.createTopics(singleton(newTopic));

    // Call values() to get the result for a specific topic
    KafkaFuture<Void> future = result.values().get(topicName);

    // Call get() to block until the topic creation is complete or has failed
    // if creation failed the ExecutionException wraps the underlying cause.
    future.get();
  }

  @Override
  public void close() {
    if (admin != null) {
      admin.close();
    }
  }
}
