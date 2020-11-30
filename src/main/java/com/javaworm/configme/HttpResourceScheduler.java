package com.javaworm.configme;

import com.javaworm.configme.sources.HttpSourceConfig;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Timer;
import java.util.TimerTask;

public class HttpResourceScheduler {
    private HttpClient client = HttpClient.newHttpClient();
    private KubernetesClient k8sClient;
    private Timer timer = new Timer(HttpResourceScheduler.class.getName());
    private static final Logger log = LoggerFactory.getLogger(HttpResourceScheduler.class);

    public HttpResourceScheduler(KubernetesClient k8sClient) {
        this.k8sClient = k8sClient;
    }

    public void schedule(ConfigSource<HttpSourceConfig> configSource) {
        //        TODO: cancel old scheduled task
        final var configName = configSource.getTargetConfigMapName();
        final var url = configSource.getSourceConfig().getUrl();
        final var namespace = configSource.getNamespace();
        final var intervalSeconds = configSource.getSourceConfig().getIntervalSeconds() * 1000;
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                log.info("Updating the {}", configSource);
                try {
                    final var body = client.send(HttpRequest.newBuilder().uri(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString()).body();

                    final Resource<ConfigMap, DoneableConfigMap> configMapResource = k8sClient.configMaps()
                            .inNamespace(namespace)
                            .withName(configName);


                    ConfigMap configMap = configMapResource.createOrReplace(new ConfigMapBuilder().
                            withNewMetadata().withName(configName).endMetadata().
                            addToData("config", body).
                            build());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(task, 0, intervalSeconds);
    }
}
