package pl.matsuo.tools.kafka.gui.action;

import static java.util.concurrent.TimeUnit.SECONDS;
import static pl.matsuo.core.util.collection.CollectionUtil.flatMap;
import static pl.matsuo.core.util.collection.CollectionUtil.getFirst;
import static pl.matsuo.core.util.collection.CollectionUtil.map;
import static pl.matsuo.core.util.collection.CollectionUtil.toMap;
import static pl.matsuo.core.util.collection.Pair.pair;
import static pl.matsuo.core.util.desktop.IRequest.request;
import static pl.matsuo.tools.kafka.gui.KafkaAdminGui.normalizeName;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import pl.matsuo.core.util.desktop.IActionController;
import pl.matsuo.core.util.desktop.IRequest;
import pl.matsuo.core.util.desktop.PersistResult;
import pl.matsuo.tools.kafka.gui.KafkaClient;
import pl.matsuo.tools.kafka.gui.model.KafkaAdminGuiModel;
import pl.matsuo.tools.kafka.gui.model.KafkaInstanceModel;
import pl.matsuo.tools.kafka.gui.model.OffsetData;

@Slf4j
@PersistResult
public class ConnectToInstanceAction implements IActionController<IRequest, KafkaAdminGuiModel> {

  @Override
  public IRequest execute(IRequest request, KafkaAdminGuiModel model) {
    // lookup existing instance
    KafkaInstanceModel kafkaInstance =
        getFirst(
            model.getKnownInstances(),
            instance -> instance.getName().equalsIgnoreCase(request.getParam("name")));

    return connectAndRedirect(model, kafkaInstance);
  }

  public static IRequest connectAndRedirect(
      KafkaAdminGuiModel model, KafkaInstanceModel kafkaInstance) {
    try {
      connect(model, kafkaInstance);
    } catch (Exception e) {
      log.error("Error during kafka connection", e);
      if (model.getKafkaClient() != null) {
        model.getKafkaClient().close();
      }
      return request(
          "/",
          toMap(
              pair("name", kafkaInstance.getName()),
              pair("url", kafkaInstance.getUrl()),
              pair("error_message", "Exception while connecting to Kafka server"),
              pair("exception", e.toString())));
    }

    model.setKafkaInstance(kafkaInstance);
    if (!model.getKnownInstances().contains(kafkaInstance)) {
      model.getKnownInstances().add(kafkaInstance);
    }

    // return path to new known instance
    return request(
        "/instance/info", ImmutableMap.of("name", normalizeName(kafkaInstance.getName())));
  }

  public static void connect(KafkaAdminGuiModel model, KafkaInstanceModel instance)
      throws InterruptedException, ExecutionException, TimeoutException {
    Properties props = new Properties();
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, instance.getUrl());
    props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "3000");
    props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "3000");

    if (model.getKafkaClient() != null) {
      model.getKafkaClient().close();
    }

    KafkaClient kafkaClient = new KafkaClient(props);
    model.setKafkaClient(kafkaClient);

    model.setTopics(kafkaClient.getAdmin().listTopics().namesToListings().get(3, SECONDS));

    log.info("Topics");
    model.getTopics().forEach((name, topicListing) -> log.info(topicListing.name()));

    log.info("ConsumerGroups");

    model.setConsumerGroups(kafkaClient.getAdmin().listConsumerGroups().all().get(3, SECONDS));
    model.getConsumerGroups().forEach(listing -> log.info(listing.groupId()));
    model.setConsumerGroupDescriptions(
        kafkaClient
            .getAdmin()
            .describeConsumerGroups(map(model.getConsumerGroups(), ConsumerGroupListing::groupId))
            .all()
            .get(3, SECONDS));

    log.info("TopicPartitionOffsetAndMetadataMap");

    model.setOffsetData(new HashMap<>());

    model
        .getConsumerGroups()
        .forEach(
            consumerGroup -> {
              log.info("Processing consumer group " + consumerGroup.groupId());
              try {
                ConsumerGroupDescription consumerGroupDescription =
                    model.getConsumerGroupDescriptions().get(consumerGroup.groupId());
                Set<TopicPartition> topicPartitions =
                    new HashSet<>(
                        flatMap(
                            consumerGroupDescription.members(),
                            member -> member.assignment().topicPartitions()));
                HashMap<TopicPartition, OffsetData> groupPartitionsData = new HashMap<>();
                model.getOffsetData().put(consumerGroup.groupId(), groupPartitionsData);
                topicPartitions.forEach(
                    topicPartition -> groupPartitionsData.put(topicPartition, new OffsetData()));

                Map<TopicPartition, OffsetAndMetadata> offsetAndMetadataMap =
                    kafkaClient
                        .getAdmin()
                        .listConsumerGroupOffsets(consumerGroup.groupId())
                        .partitionsToOffsetAndMetadata()
                        .get(3, SECONDS);

                log.info("OffsetAndMetadata size " + offsetAndMetadataMap.size());
                offsetAndMetadataMap.forEach(
                    (topicPartition, offsetAndMetadata) -> {
                      groupPartitionsData
                          .get(topicPartition)
                          .setOffsetAndMetadata(offsetAndMetadata);
                      log.info(topicPartition.topic());
                      log.info("" + topicPartition.partition());
                      log.info("" + offsetAndMetadata.offset());
                      log.info(offsetAndMetadata.metadata());
                    });

                log.info("Offsets for topic partitions");
                kafkaClient
                    .getAdmin()
                    .listOffsets(toMap(topicPartitions, tp -> tp, tp -> OffsetSpec.latest()))
                    .all()
                    .get(3, SECONDS)
                    .forEach(
                        (topicPartition, listOffset) -> {
                          groupPartitionsData.get(topicPartition).setListOffset(listOffset);
                          log.info("" + topicPartition + " " + listOffset.offset());
                        });

                try (KafkaConsumer<String, String> consumer =
                    kafkaConsumer(instance.getUrl(), consumerGroup.groupId())) {
                  Map<TopicPartition, OffsetAndMetadata> committed =
                      consumer.committed(topicPartitions);
                  committed.forEach(
                      (topicPartition, offsetAndMetadata) -> {
                        groupPartitionsData.get(topicPartition).setCommitted(offsetAndMetadata);
                        log.info(topicPartition.topic() + " " + offsetAndMetadata);
                      });
                  consumer.assign(topicPartitions);
                  consumer.seekToEnd(topicPartitions);

                  topicPartitions.forEach(
                      topicPartition -> {
                        long position = consumer.position(topicPartition);
                        groupPartitionsData.get(topicPartition).setLogEndPosition(position);
                        log.info("LogEndOffset: " + position);
                      });
                }
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });

    log.info("ConsumerGroupDescriptions");
    model
        .getConsumerGroupDescriptions()
        .forEach(
            (groupId, groupDescription) -> {
              log.info("" + groupDescription.members());
              log.info("" + groupDescription.isSimpleConsumerGroup());
              groupDescription
                  .members()
                  .forEach(
                      memberDescription -> {
                        log.info("Consumer id: " + memberDescription.consumerId());
                        memberDescription
                            .assignment()
                            .topicPartitions()
                            .forEach(
                                topicPartition ->
                                    log.info("Topic partition " + topicPartition.partition()));
                      });
            });
  }

  private static KafkaConsumer<String, String> kafkaConsumer(String brokers, String group) {
    Properties properties = new Properties();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, group);
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
    properties.put(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringDeserializer");
    properties.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringDeserializer");
    return new KafkaConsumer<>(properties);
  }
}
