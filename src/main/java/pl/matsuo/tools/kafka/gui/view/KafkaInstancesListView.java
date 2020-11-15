package pl.matsuo.tools.kafka.gui.view;

import static j2html.TagCreator.a;
import static j2html.TagCreator.attrs;
import static j2html.TagCreator.each;
import static j2html.TagCreator.form;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.i;
import static j2html.TagCreator.li;
import static j2html.TagCreator.span;
import static j2html.TagCreator.text;
import static j2html.TagCreator.ul;
import static pl.matsuo.core.util.collection.Pair.pair;
import static pl.matsuo.core.util.desktop.component.AlertType.danger;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.matsuo.core.util.desktop.BootstrapIcons;
import pl.matsuo.core.util.desktop.IRequest;
import pl.matsuo.core.util.desktop.IView;
import pl.matsuo.core.util.desktop.component.FormComponents;
import pl.matsuo.core.util.desktop.component.ViewComponents;
import pl.matsuo.tools.kafka.gui.model.KafkaAdminGuiModel;
import pl.matsuo.tools.kafka.gui.model.KafkaInstanceModel;

@Slf4j
@RequiredArgsConstructor
public class KafkaInstancesListView implements IView<IRequest, KafkaAdminGuiModel> {

  public static final String SHOW_ADD_FORM = "showAddForm";

  final ViewComponents viewComponents;
  final FormComponents formComponents;

  @Override
  public ContainerTag view(IRequest request, KafkaAdminGuiModel model) {
    boolean showAddForm = showAddForm(request, model);
    return viewComponents.pageTemplate(
        "Kafka instance selection",
        viewComponents.rowCol(
            h1(
                text(showAddForm ? "New Kafka instance" : "Select Kafka instance"),
                !showAddForm
                    ? a(attrs(".float-right"), BootstrapIcons.plus_circle.svg())
                        .withHref("/?" + SHOW_ADD_FORM)
                    : null),
            viewComponents.maybeShowAlert(request, "error_message", danger),
            newKafkaInstanceForm(request, model),
            showAddForm ? h2("Saved Kafka instances") : null,
            createInstancesList(model)));
  }

  private ContainerTag createInstancesList(KafkaAdminGuiModel model) {
    if (model.getKnownInstances().isEmpty()) {
      return ul(
          attrs(".list-group"),
          li(attrs(".list-group-item.list-group-item-action"), i("No stored instances")));
    } else {
      return ul(
          attrs(".list-group"), each(model.getKnownInstances(), this::createInstanceListItem));
    }
  }

  private ContainerTag createInstanceListItem(KafkaInstanceModel instance) {
    return li(
        attrs(".list-group-item.list-group-item-action"),
        formComponents.formAsLink(
            instance.getName(), "/instance/connect", pair("name", instance.getName())),
        span(attrs(".text-muted.ml-3"), instance.getUrl()));
  }

  private DomContent newKafkaInstanceForm(IRequest request, KafkaAdminGuiModel model) {
    if (showAddForm(request, model)) {
      return form(
              formComponents.textField("Instance name", "name", request),
              formComponents.textField("Url", "url", request),
              formComponents.submitButton("Create"))
          .withAction("/instance/create");
    } else {
      return null;
    }
  }

  private boolean showAddForm(IRequest request, KafkaAdminGuiModel model) {
    return request.hasParam(SHOW_ADD_FORM) || model.getKnownInstances().isEmpty();
  }
}
