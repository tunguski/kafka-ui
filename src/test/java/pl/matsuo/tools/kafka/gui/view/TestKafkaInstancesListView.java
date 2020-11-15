package pl.matsuo.tools.kafka.gui.view;

import static java.util.Collections.emptyMap;
import static pl.matsuo.core.util.desktop.IRequest.request;
import static pl.matsuo.core.util.desktop.ViewTestUtil.storeView;
import static pl.matsuo.tools.kafka.gui.view.KafkaInstancesListView.SHOW_ADD_FORM;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.TopicListing;
import org.junit.Test;
import pl.matsuo.core.util.desktop.component.FormComponents;
import pl.matsuo.core.util.desktop.component.ViewComponents;
import pl.matsuo.tools.kafka.gui.model.KafkaAdminGuiModel;
import pl.matsuo.tools.kafka.gui.model.KafkaInstanceModel;

public class TestKafkaInstancesListView {

  ViewComponents viewComponents = new ViewComponents();
  FormComponents formComponents = new FormComponents();

  @Test
  public void viewNoForm() {
    KafkaInstancesListView view = new KafkaInstancesListView(viewComponents, formComponents);

    KafkaAdminGuiModel model = new KafkaAdminGuiModel();

    Map<String, TopicListing> topicListingMap = new HashMap<>();
    topicListingMap.put("topic", new TopicListing("topic", false));
    model.setTopics(topicListingMap);
    model.setKafkaInstance(new KafkaInstanceModel("test", "localhost:9092"));
    model.getKnownInstances().add(model.getKafkaInstance());

    String rendered =
        view.view(request("/kafkaInstancesList", emptyMap()), model).renderFormatted();

    storeView("kafkaInstancesList.html", rendered);
  }

  @Test
  public void viewWithForm() {
    KafkaInstancesListView view = new KafkaInstancesListView(viewComponents, formComponents);

    KafkaAdminGuiModel model = new KafkaAdminGuiModel();

    Map<String, TopicListing> topicListingMap = new HashMap<>();
    topicListingMap.put("topic", new TopicListing("topic", false));
    model.setTopics(topicListingMap);
    model.setKafkaInstance(new KafkaInstanceModel("Localhost 9092", "localhost:9092", 7));
    model.getKnownInstances().add(model.getKafkaInstance());
    model.getKnownInstances().add(new KafkaInstanceModel("Very far", "ohio.com:9092", 12));
    model.getKnownInstances().add(new KafkaInstanceModel("Not so far", "kaszuby.pl:9092"));

    String rendered =
        view.view(
                request(
                    "/kafkaInstancesListWithForm",
                    ImmutableMap.<String, String>builder().put(SHOW_ADD_FORM, "true").build()),
                model)
            .renderFormatted();

    storeView("kafkaInstancesListWithForm.html", rendered);
  }
}
