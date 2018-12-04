package org.eulerframework.web.eureka;

import java.io.IOException;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;

public class EurekaRegistry {


    public static void main(String[] args) throws IOException, InterruptedException {
        EurekaInstanceConfig eurekaInstanceConfig = new EulerWebEurekaInstanceConfig();
        InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(eurekaInstanceConfig).get();
        ApplicationInfoManager applicationInfoManager = new ApplicationInfoManager(eurekaInstanceConfig, instanceInfo);
        
        DefaultEurekaClientConfig clientConfig = new DefaultEurekaClientConfig();

        EurekaClient eurekaClient = new DiscoveryClient(applicationInfoManager, clientConfig);
        String instanceId = instanceInfo.getInstanceId();
        
        System.out.println(instanceId);
        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.STARTING);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.UP);
        
        Thread.sleep(10000);
        eurekaClient.shutdown();
    }

}