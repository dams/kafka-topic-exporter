package jp.gr.java_conf.ogibayashi.prometheus;

import io.prometheus.client.Collector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A registry of Collectors.
 * <p>
 * The majority of users should use the {@link #defaultRegistry}, rather than instantiating their own.
 * <p>
 * Creating a registry other than the default is primarily useful for unittests, or
 * pushing a subset of metrics to the <a href="https://github.com/prometheus/pushgateway">Pushgateway</a>
 * from batch jobs.
 */
public class MyCollectorRegistry {
  /**
   * The default registry.
   */
  public static final MyCollectorRegistry defaultRegistry = new MyCollectorRegistry();

  private final Set<MyCollector> collectors =
      Collections.newSetFromMap(new ConcurrentHashMap<MyCollector, Boolean>());

  /**
   * Register a Collector.
   * <p>
   * A collector can be registered to multiple CollectorRegistries.
   */
  public void register(MyCollector m) {
    collectors.add(m);
  }
  
  /**
   * Unregister a Collector.
   */
  public void unregister(MyCollector m) {
    collectors.remove(m);
  }
  /**
   * Unregister all Collectors.
   */
  public void clear() {
    collectors.clear();
  }

  /**
   * Enumeration of metrics of all registered collectors.
   */
  public Enumeration<MyCollector.MetricFamilySamples> metricFamilySamples() {
    return new MetricFamilySamplesEnumeration();
  }
  class MetricFamilySamplesEnumeration implements Enumeration<MyCollector.MetricFamilySamples> {

    private final Iterator<MyCollector> collectorIter = collectors.iterator();
    private Iterator<MyCollector.MetricFamilySamples> metricFamilySamples;
    private MyCollector.MetricFamilySamples next;

    MetricFamilySamplesEnumeration() {
      findNextElement();
    }
    
    private void findNextElement() {
      if (metricFamilySamples != null && metricFamilySamples.hasNext()) {
        next = metricFamilySamples.next();
      } else {
        while (collectorIter.hasNext()) {
          metricFamilySamples = collectorIter.next().collect().iterator();
          if (metricFamilySamples.hasNext()) {
            next = metricFamilySamples.next();
            return;
          }
        }
        next = null;
      }
    }

    public MyCollector.MetricFamilySamples nextElement() {
      MyCollector.MetricFamilySamples current = next;
      if (current == null) {
        throw new NoSuchElementException();
      }
      findNextElement();
      return current;
    }
    
    public boolean hasMoreElements() {
      return next != null;
    }
  }

  /**
   * Returns the given value, or null if it doesn't exist.
   * <p>
   * This is inefficient, and intended only for use in unittests.
   */
  public Double getSampleValue(String name) {
    return getSampleValue(name, new String[]{}, new String[]{});
  }

  /**
   * Returns the given value, or null if it doesn't exist.
   * <p>
   * This is inefficient, and intended only for use in unittests.
   */
  public Double getSampleValue(String name, String[] labelNames, String[] labelValues) {
    for (MyCollector.MetricFamilySamples metricFamilySamples: Collections.list(metricFamilySamples())) {
      for (MyCollector.MetricFamilySamples.Sample sample: metricFamilySamples.samples) {
        if (sample.name.equals(name)
            && Arrays.equals(sample.labelNames.toArray(), labelNames)
            && Arrays.equals(sample.labelValues.toArray(), labelValues)) {
          return sample.value;
        }
      }
    }
    return null;
  }

}
