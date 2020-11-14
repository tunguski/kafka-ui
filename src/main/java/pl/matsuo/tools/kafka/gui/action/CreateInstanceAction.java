package pl.matsuo.tools.kafka.gui.action;

import static pl.matsuo.core.util.collection.CollectionUtil.toMap;
import static pl.matsuo.core.util.collection.Pair.pair;
import static pl.matsuo.core.util.desktop.IRequest.request;
import static pl.matsuo.tools.kafka.gui.KafkaAdminGui.normalizeName;
import static pl.matsuo.tools.kafka.gui.action.ConnectToInstanceAction.connect;

import com.google.common.collect.ImmutableMap;
import pl.matsuo.core.util.desktop.IActionController;
import pl.matsuo.core.util.desktop.IRequest;
import pl.matsuo.core.util.desktop.PersistResult;
import pl.matsuo.tools.kafka.gui.model.KafkaAdminGuiModel;
import pl.matsuo.tools.kafka.gui.model.KafkaInstanceModel;

@PersistResult
public class CreateInstanceAction implements IActionController<IRequest, KafkaAdminGuiModel> {

  @Override
  public IRequest execute(IRequest request, KafkaAdminGuiModel model) {
    // create new known instance
    KafkaInstanceModel kafkaInstance =
        new KafkaInstanceModel(request.getParam("name"), request.getParam("url"));

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

    model.getKnownInstances().add(kafkaInstance);
    model.setKafkaInstance(kafkaInstance);

    // return path to new known instance
    return request(
        "/instance/info", ImmutableMap.of("name", normalizeName(kafkaInstance.getName())));
  }
}
