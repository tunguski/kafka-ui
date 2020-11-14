package pl.matsuo.tools.kafka.gui.action;

import static java.util.concurrent.TimeUnit.SECONDS;
import static pl.matsuo.core.util.collection.CollectionUtil.getFirst;
import static pl.matsuo.core.util.collection.CollectionUtil.toMap;
import static pl.matsuo.core.util.collection.Pair.pair;
import static pl.matsuo.core.util.desktop.IRequest.request;
import static pl.matsuo.tools.kafka.gui.KafkaAdminGui.normalizeName;

import com.google.common.collect.ImmutableMap;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.admin.AdminClientConfig;
import pl.matsuo.core.util.desktop.IActionController;
import pl.matsuo.core.util.desktop.IRequest;
import pl.matsuo.core.util.desktop.PersistResult;
import pl.matsuo.tools.kafka.gui.KafkaClient;
import pl.matsuo.tools.kafka.gui.model.KafkaAdminGuiModel;
import pl.matsuo.tools.kafka.gui.model.KafkaInstanceModel;

@PersistResult
public class ConnectToInstanceAction implements IActionController<IRequest, KafkaAdminGuiModel> {

  @Override
  public IRequest execute(IRequest request, KafkaAdminGuiModel model) {
    // lookup existing instance
    KafkaInstanceModel kafkaInstance =
        getFirst(
            model.getKnownInstances(),
            instance -> instance.getName().equalsIgnoreCase(request.getParam("name")));

    try {
      connect(model, kafkaInstance);
    } catch (Exception e) {
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

    // return path to new known instance
    return request(
        "/instance/info", ImmutableMap.of("name", normalizeName(kafkaInstance.getName())));
  }

  public static void connect(KafkaAdminGuiModel model, KafkaInstanceModel instanceName)
      throws InterruptedException, ExecutionException, TimeoutException {
    Properties props = new Properties();
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, instanceName.getUrl());
    props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "3000");
    props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "3000");

    if (model.getKafkaClient() != null) {
      model.getKafkaClient().close();
    }

    KafkaClient kafkaClient = new KafkaClient(props);
    model.setKafkaClient(kafkaClient);
    model.setTopicListingMap(kafkaClient.getAdmin().listTopics().namesToListings().get(3, SECONDS));

    return;
  }
}
