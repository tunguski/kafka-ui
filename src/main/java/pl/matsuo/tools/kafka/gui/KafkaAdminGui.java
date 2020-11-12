package pl.matsuo.tools.kafka.gui;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import pl.matsuo.core.util.desktop.DesktopUI;
import pl.matsuo.core.util.desktop.DesktopUIData;
import pl.matsuo.core.util.desktop.IActionController;
import pl.matsuo.core.util.desktop.IRequest;
import pl.matsuo.core.util.desktop.IView;
import pl.matsuo.core.util.desktop.ViewComponents;
import pl.matsuo.tools.kafka.gui.action.CreateInstanceAction;
import pl.matsuo.tools.kafka.gui.model.KafkaAdminGuiModel;
import pl.matsuo.tools.kafka.gui.view.KafkaInstanceInfoView;
import pl.matsuo.tools.kafka.gui.view.KafkaInstancesListView;

@Slf4j
public class KafkaAdminGui extends DesktopUI<KafkaAdminGuiModel> {

  public KafkaAdminGui() {
    super(desktopUiConfig());
  }

  private static DesktopUIData<KafkaAdminGuiModel> desktopUiConfig() {
    KafkaAdminGuiModel model = new KafkaAdminGuiModel();
    return new DesktopUIData<>(views(), controllers(), model);
  }

  public static String normalizeName(String name) {
    return name.replaceAll("[^a-zA-Z0-9]", "_");
  }

  private static Map<String, IView<IRequest, KafkaAdminGuiModel>> views() {
    Map<String, IView<IRequest, KafkaAdminGuiModel>> views = new HashMap<>();

    ViewComponents viewComponents = new ViewComponents();

    views.put("/", new KafkaInstancesListView(viewComponents));
    views.put("/instance/info", new KafkaInstanceInfoView(viewComponents));

    return views;
  }

  private static Map<String, IActionController<IRequest, KafkaAdminGuiModel>> controllers() {
    Map<String, IActionController<IRequest, KafkaAdminGuiModel>> controllerMap = new HashMap<>();

    controllerMap.put("/instance/create", new CreateInstanceAction());

    return controllerMap;
  }
}
