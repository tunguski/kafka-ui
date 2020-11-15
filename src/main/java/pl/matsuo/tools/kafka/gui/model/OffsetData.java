package pl.matsuo.tools.kafka.gui.model;

import lombok.Data;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;

@Data
public class OffsetData {

  OffsetAndMetadata offsetAndMetadata;
  ListOffsetsResult.ListOffsetsResultInfo listOffset;
  OffsetAndMetadata committed;
  long logEndPosition;
}
