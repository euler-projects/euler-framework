package net.eulerform.web.module.basedata.service;

import java.io.IOException;

import net.eulerform.web.core.base.service.IBaseService;

public interface IBaseDataService extends IBaseService {

    public void createCodeDict() throws IOException;
}
