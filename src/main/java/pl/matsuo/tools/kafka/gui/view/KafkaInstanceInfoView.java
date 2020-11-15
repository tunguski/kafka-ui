package pl.matsuo.tools.kafka.gui.view;

import static j2html.TagCreator.a;
import static j2html.TagCreator.attrs;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.TopicPartition;
import pl.matsuo.core.util.desktop.BootstrapIcons;
import pl.matsuo.core.util.desktop.IRequest;
import pl.matsuo.core.util.desktop.IView;
import pl.matsuo.core.util.desktop.component.ViewComponents;
import pl.matsuo.tools.kafka.gui.model.KafkaAdminGuiModel;
import pl.matsuo.tools.kafka.gui.model.OffsetData;

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
            topicsList(model),
            h2("Consumer groups"),
            consumerGroups(model)));
  }

  private DomContent consumerGroups(KafkaAdminGuiModel model) {
    return div(
        each(
            model.getOffsetData(),
            (groupId, topicPartitionOffsetData) ->
                div(h3("Group " + groupId), consumerGroup(topicPartitionOffsetData))));
  }

  private DomContent consumerGroup(Map<TopicPartition, OffsetData> topicPartitionOffsetData) {
    List<TopicPartition> partitions = new ArrayList<>(topicPartitionOffsetData.keySet());

    return table(
        attrs(".table.table-hover"),
        thead(
            tr(
                th("Topic Partition"),
                th("Committed"),
                th("List Offset"),
                th("Log End Position"),
                th("Offset And TestKafkaInstanceInfoView.viewMetadata"))),
        tbody(
            each(
                partitions,
                partition -> {
                  OffsetData offsetData = topicPartitionOffsetData.get(partition);
                  return tr(
                      td("" + partition),
                      td("" + offsetData.getCommitted()),
                      td("" + offsetData.getListOffset()),
                      td("" + offsetData.getLogEndPosition()),
                      td("" + offsetData.getOffsetAndMetadata()));
                })));
  }

  private DomContent topicsList(KafkaAdminGuiModel model) {
    return table(
        attrs(".table.table-hover"),
        thead(tr(th("Topic"), th("Internal"))),
        tbody(each(model.getTopics(), (key, value) -> tr(td(key), td("" + value.isInternal())))));
  }
}
