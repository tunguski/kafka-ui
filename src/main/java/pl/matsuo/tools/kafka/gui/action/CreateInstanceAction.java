package pl.matsuo.tools.kafka.gui.action;

import static pl.matsuo.core.util.desktop.IRequest.request;
import static pl.matsuo.tools.kafka.gui.action.ConnectToInstanceAction.connectAndRedirect;

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

    return connectAndRedirect(model, kafkaInstance);
  }
}
