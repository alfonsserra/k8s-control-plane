package com.systelab.controlplane.service;


import com.systelab.controlplane.model.Tenant;
import com.systelab.controlplane.model.TenantRequestInfo;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class KubernetesService {

    Logger logger = LoggerFactory.getLogger(KubernetesService.class);

    public Tenant newTenant(TenantRequestInfo tenantRequestInfo) throws KubernetesClientException {
        Tenant tenant=new Tenant(tenantRequestInfo);
        Config config = new ConfigBuilder().build();
        KubernetesClient client = new DefaultKubernetesClient(config);
        try {

            Namespace ns = createNameSpace(client,tenantRequestInfo);
            tenant.setNameSpace(ns.getMetadata().getName());
            io.fabric8.kubernetes.api.model.Service service=createService(client,tenantRequestInfo);
            service.getSpec().getExternalIPs().forEach((s1)->logger.debug(s1));
        } catch (KubernetesClientException e) {
            logger.error(e.getMessage(), e);
        }
        client.close();
        return tenant;
    }

    private Namespace createNameSpace(KubernetesClient client,TenantRequestInfo tenantRequestInfo) throws KubernetesClientException {
        client.namespaces().withName("thisisatest").delete();

        Namespace ns = new NamespaceBuilder().withNewMetadata().withName(tenantRequestInfo.getNameSpace()).addToLabels("this", "rocks").endMetadata().build();
        return client.namespaces().create(ns);
    }

    private io.fabric8.kubernetes.api.model.Service createService(KubernetesClient client,TenantRequestInfo tenantRequestInfo) throws KubernetesClientException {
        return client.services().inNamespace(tenantRequestInfo.getNameSpace()).createNew()
                .withNewMetadata().withName("testservice").endMetadata()
                .withNewSpec()
                .addNewPort().withPort(80).withNewTargetPort().withIntVal(80).endTargetPort().endPort()
                .endSpec()
                .done();
    }
}
