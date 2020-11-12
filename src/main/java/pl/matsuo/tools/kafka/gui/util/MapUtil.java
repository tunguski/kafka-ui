package pl.matsuo.tools.kafka.gui.util;

import java.util.HashMap;
import java.util.Map;
import pl.matsuo.core.util.collection.Pair;

public class MapUtil {

  public static <E, F> Map<E, F> toMap(Pair<E, F>... entries) {
    Map<E, F> result = new HashMap<>();

    for (Pair<E, F> entry : entries) {
      result.put(entry.getKey(), entry.getValue());
    }

    return result;
  }
}
