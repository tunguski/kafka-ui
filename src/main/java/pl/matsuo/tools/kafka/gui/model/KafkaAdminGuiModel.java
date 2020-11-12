package pl.matsuo.tools.kafka.gui.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.apache.kafka.clients.admin.TopicListing;
import pl.matsuo.tools.kafka.gui.KafkaClient;

@Data
public class KafkaAdminGuiModel {

  List<KafkaInstanceModel> knownInstances = new ArrayList<>();

  transient KafkaInstanceModel kafkaInstance;
  transient KafkaClient kafkaClient;
  transient Map<String, TopicListing> topicListingMap;
}
