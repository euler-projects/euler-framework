package net.eulerframework.web.module.basic.htservice;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.basic.dao.ConfigDao;
import net.eulerframework.web.module.basic.entity.Config;

@Service
public class ConfigService extends BaseService {

    @Resource private ConfigDao configDao;
    
    public Config findConfig(String key) {
        return this.configDao.load(key);
    }
}
