package pl.matsuo.tools.kafka.gui.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.TopicPartition;
import pl.matsuo.tools.kafka.gui.KafkaClient;

@Data
public class KafkaAdminGuiModel {

  List<KafkaInstanceModel> knownInstances = new ArrayList<>();

  transient KafkaInstanceModel kafkaInstance;
  transient KafkaClient kafkaClient;

  transient Map<String, TopicListing> topics;
  transient Collection<ConsumerGroupListing> consumerGroups;
  transient Map<String, ConsumerGroupDescription> consumerGroupDescriptions;

  transient Map<String, Map<TopicPartition, OffsetData>> offsetData;
}
