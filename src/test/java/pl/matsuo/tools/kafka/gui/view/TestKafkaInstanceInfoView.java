package pl.matsuo.tools.kafka.gui.view;

import static java.util.Collections.emptyMap;
import static pl.matsuo.core.util.desktop.IRequest.request;
import static pl.matsuo.core.util.desktop.ViewTestUtil.storeView;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.TopicListing;
import org.junit.Test;
import pl.matsuo.core.util.desktop.component.ViewComponents;
import pl.matsuo.tools.kafka.gui.model.KafkaAdminGuiModel;
import pl.matsuo.tools.kafka.gui.model.KafkaInstanceModel;

public class TestKafkaInstanceInfoView {

  @Test
  public void view() {
    ViewComponents viewComponents = new ViewComponents();
    KafkaInstanceInfoView view = new KafkaInstanceInfoView(viewComponents);

    KafkaAdminGuiModel model = new KafkaAdminGuiModel();

    Map<String, TopicListing> topicListingMap = new HashMap<>();
    topicListingMap.put("topic", new TopicListing("topic", false));
    model.setTopicListingMap(topicListingMap);
    model.setKafkaInstance(new KafkaInstanceModel());
    model.getKafkaInstance().setUrl("localhost:9092");
    String rendered = view.view(request("/kafkaInstanceInfo", emptyMap()), model).renderFormatted();

    storeView("kafkaInstanceInfo.html", rendered);
  }
}
