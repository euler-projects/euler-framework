package net.eulerform.web.module.authentication.service;

import net.eulerform.web.core.base.service.IBaseService;

public interface IAuthorityService extends IBaseService {

    public void createAuthority(String authority, String description);

    public void createGroup(String name, String description);
}
