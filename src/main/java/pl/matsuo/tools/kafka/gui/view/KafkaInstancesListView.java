package pl.matsuo.tools.kafka.gui.view;

import static j2html.TagCreator.a;
import static j2html.TagCreator.attrs;
import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.form;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.i;
import static j2html.TagCreator.input;
import static j2html.TagCreator.label;
import static j2html.TagCreator.li;
import static j2html.TagCreator.span;
import static j2html.TagCreator.text;
import static j2html.TagCreator.ul;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.matsuo.core.util.desktop.BootstrapIcons;
import pl.matsuo.core.util.desktop.IRequest;
import pl.matsuo.core.util.desktop.IView;
import pl.matsuo.core.util.desktop.ViewComponents;
import pl.matsuo.tools.kafka.gui.model.KafkaAdminGuiModel;
import pl.matsuo.tools.kafka.gui.model.KafkaInstanceModel;

@Slf4j
@RequiredArgsConstructor
public class KafkaInstancesListView implements IView<IRequest, KafkaAdminGuiModel> {

  public static final String SHOW_ADD_FORM = "showAddForm";
  final ViewComponents viewComponents;

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
            maybeShowErrorMessage(request),
            newKafkaInstanceForm(request, model),
            showAddForm ? h2("Saved Kafka instances") : null,
            createInstancesList(model)));
  }

  private DomContent maybeShowErrorMessage(IRequest request) {
    if (request.hasParam("error_message")) {
      return div(attrs(".alert.alert-danger"), text(request.getParam("error_message")));
    } else {
      return null;
    }
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
        text(instance.getName()),
        span(attrs(".text-muted.ml-3"), instance.getUrl()));
  }

  private DomContent newKafkaInstanceForm(IRequest request, KafkaAdminGuiModel model) {
    if (showAddForm(request, model)) {
      return form(
              textField("Instance name", "name", request),
              textField("Url", "url", request),
              button(attrs(".btn.btn-primary"), text("Create")).withType("submit"))
          .withAction("/instance/create");
    } else {
      return null;
    }
  }

  private boolean showAddForm(IRequest request, KafkaAdminGuiModel model) {
    return request.hasParam(SHOW_ADD_FORM) || model.getKnownInstances().isEmpty();
  }

  private ContainerTag textField(String s, String name, IRequest request) {
    log.info(name + ": " + request.getParam(name));
    return div(
        attrs(".form-group"),
        label(s).attr("for", "form-" + name),
        input(attrs(".form-control"))
            .withType("text")
            .withName(name)
            .withId("form-" + name)
            .withCondValue(request.hasParam(name), request.getParam(name)));
  }
}
