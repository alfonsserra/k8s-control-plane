package com.systelab.controlplane.service;


import com.systelab.controlplane.model.Tenant;
import com.systelab.controlplane.model.TenantRequestInfo;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class KubernetesService {

    Logger logger = LoggerFactory.getLogger(KubernetesService.class);

    public Tenant newTenant(TenantRequestInfo tenantRequestInfo) throws KubernetesClientException {
        Config config = new ConfigBuilder().build();
        KubernetesClient client = new DefaultKubernetesClient(config);
        try {
            Tenant tenant = new Tenant(tenantRequestInfo);
            Namespace ns = createNameSpace(client, tenantRequestInfo);
            tenant.setNameSpace(ns.getMetadata().getName());
            createJEEDeployment(client, tenantRequestInfo);
            createJEEService(client, tenantRequestInfo);
            createAngularDeployment(client, tenantRequestInfo);
            io.fabric8.kubernetes.api.model.Service service = createAngularService(client, tenantRequestInfo);
            service.getSpec().getExternalIPs().forEach((s1) -> logger.debug(s1));
            return tenant;
        } catch (KubernetesClientException e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            client.close();
        }
    }

    public boolean removeTenant(Tenant tenant) {
        boolean isOK = true;
        Config config = new ConfigBuilder().build();
        KubernetesClient client = new DefaultKubernetesClient(config);
        try {
            client.namespaces().withName(tenant.getNameSpace()).delete();
        } catch (KubernetesClientException e) {
            logger.error(e.getMessage(), e);
            isOK = false;
        } finally {
            client.close();
        }
        return isOK;
    }

    private Namespace createNameSpace(KubernetesClient client, TenantRequestInfo tenantRequestInfo) throws KubernetesClientException {
        Namespace ns = new NamespaceBuilder().withNewMetadata().withName(tenantRequestInfo.getNameSpace()).addToLabels("this", "rocks").endMetadata().build();
        return client.namespaces().create(ns);
    }

    private io.fabric8.kubernetes.api.model.Service createAngularService(KubernetesClient client, TenantRequestInfo tenantRequestInfo) throws KubernetesClientException {
        final Map<String,String> labels= new HashMap<>();
        labels.put("app", "seed-angular");

        return client.services().inNamespace(tenantRequestInfo.getNameSpace()).createNew()
                .withNewMetadata().withName("seed-angular-svc").addToLabels("app", "seed-angular").endMetadata()
                .withNewSpec().withType("NodePort")
                .addNewPort().withPort(80).withNewTargetPort().withIntVal(30001).endTargetPort().withProtocol("TCP").endPort().withSelector(labels)
                .endSpec()
                .done();
    }

    private io.fabric8.kubernetes.api.model.Service createJEEService(KubernetesClient client, TenantRequestInfo tenantRequestInfo) throws KubernetesClientException {
        final Map<String,String> labels= new HashMap<>();
        labels.put("app", "seed-jee");

        return client.services().inNamespace(tenantRequestInfo.getNameSpace()).createNew()
                .withNewMetadata().withName("seed-jee-svc").addToLabels("app", "seed-jee").endMetadata()
                .withNewSpec().withType("NodePort")
                .addNewPort().withPort(8080).withNewTargetPort().withIntVal(30002).endTargetPort().withProtocol("TCP").endPort().withSelector(labels)
                .endSpec()
                .done();
    }


    private Deployment createAngularDeployment(KubernetesClient client, TenantRequestInfo tenantRequestInfo) throws KubernetesClientException {

        return client.apps().deployments().inNamespace(tenantRequestInfo.getNameSpace()).createNew()
                .withNewMetadata().withName("seed-angular-deploy").addToLabels("app", "seed-angular").endMetadata()
                .withNewSpec().withReplicas(2).withMinReadySeconds(10)
                .withNewStrategy().withType("RollingUpdate").endStrategy()
                .withNewTemplate()
                .withNewMetadata().addToLabels("app", "seed-angular").endMetadata()
                .withNewSpec()
                .addNewContainer().withName("seed-angular-pod").withImage("systelab/seed-angular:latest")
                .addNewPort().withContainerPort(80).endPort()
                .addNewEnv().withName("BACKEND").withValue("http://192.168.99.100:30002").endEnv()
                .endContainer()
                .endSpec()
                .endTemplate()
                .withNewSelector()
                .addToMatchLabels("app", "seed-angular")
                .endSelector()
                .endSpec().done();
    }

    private Deployment createJEEDeployment(KubernetesClient client, TenantRequestInfo tenantRequestInfo) throws KubernetesClientException {

        return client.apps().deployments().inNamespace(tenantRequestInfo.getNameSpace()).createNew()
                .withNewMetadata().withName("seed-jee-deploy").addToLabels("app", "seed-jee").endMetadata()
                .withNewSpec().withReplicas(2).withMinReadySeconds(10)
                .withNewStrategy().withType("RollingUpdate").endStrategy()
                .withNewTemplate()
                .withNewMetadata().addToLabels("app", "seed-jee").endMetadata()
                .withNewSpec()
                .addNewContainer().withName("seed-jee-pod").withImage("systelab/seed-jee:latest")
                .addNewPort().withContainerPort(8080).endPort()
                .addNewEnv().withName("MYSQL_URI").withValue("mysql-svc:3306").endEnv()
                .addNewEnv().withName("MYSQL_HOST").withValue("mysql-svc").endEnv()
                .addNewEnv().withName("MYSQL_PORT").withValue("3306").endEnv()
                .addNewEnv().withName("MYSQL_DATABASE").withValue("SEED").endEnv()
                .addNewEnv().withName("MYSQL_USER").withValue("SEED").endEnv()
                .addNewEnv().withName("MYSQL_PASSWORD").withValue("SEED").endEnv()
                .endContainer()
                .endSpec()
                .endTemplate()
                .withNewSelector()
                .addToMatchLabels("app", "seed-jee")
                .endSelector()
                .endSpec().done();
    }
}
