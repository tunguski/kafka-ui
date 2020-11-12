package pl.matsuo.tools.kafka.gui.model;

import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KafkaInstanceModel {

  String url = "";
  String name = "";
  String tags = "";
  Integer topics;
  Instant lastAccessTime = Instant.now();

  public KafkaInstanceModel(String name, String url) {
    this.name = name;
    this.url = url;
  }

  public KafkaInstanceModel(String name, String url, Integer topics) {
    this.name = name;
    this.url = url;
    this.topics = topics;
  }
}
