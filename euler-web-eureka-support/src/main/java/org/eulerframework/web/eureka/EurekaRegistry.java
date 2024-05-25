/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

        EurekaClient eurekaClient = new DiscoveryClient(applicationInfoManager, clientConfig, /*TODO*/null);
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