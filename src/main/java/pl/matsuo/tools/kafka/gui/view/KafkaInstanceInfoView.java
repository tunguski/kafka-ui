package pl.matsuo.tools.kafka.gui.view;

import static j2html.TagCreator.a;
import static j2html.TagCreator.attrs;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import lombok.RequiredArgsConstructor;
import pl.matsuo.core.util.desktop.BootstrapIcons;
import pl.matsuo.core.util.desktop.IRequest;
import pl.matsuo.core.util.desktop.IView;
import pl.matsuo.core.util.desktop.component.ViewComponents;
import pl.matsuo.tools.kafka.gui.model.KafkaAdminGuiModel;

@RequiredArgsConstructor
public class KafkaInstanceInfoView implements IView<IRequest, KafkaAdminGuiModel> {

  final ViewComponents viewComponents;

  @Override
  public ContainerTag view(IRequest request, KafkaAdminGuiModel model) {
    return viewComponents.pageTemplate(
        "Kafka instance info",
        viewComponents.rowCol(
            h1(
                text("Kafka: " + model.getKafkaInstance().getUrl()),
                a(attrs(".float-right"), BootstrapIcons.arrow_left_circle.svg()).withHref("/")),
            details(model)));
  }

  private DomContent details(KafkaAdminGuiModel model) {
    return table(
        attrs(".table.table-hover"),
        thead(tr(th("Topic"), th("Internal"))),
        tbody(
            each(
                model.getTopicListingMap(),
                (key, value) -> tr(td(key), td("" + value.isInternal())))));
  }
}
