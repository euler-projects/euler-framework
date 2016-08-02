package net.eulerform.web.module.oauth2.service;


import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import net.eulerform.web.core.base.entity.PageResponse;
import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.core.base.service.IBaseService;
import net.eulerform.web.module.oauth2.entity.Client;
import net.eulerform.web.module.oauth2.entity.Resource;
import net.eulerform.web.module.oauth2.entity.Scope;

public interface IClientService extends IBaseService {

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    PageResponse<Client> findClientByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    PageResponse<Resource> findResourceByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    PageResponse<Scope> findScopeByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    /**
     * 保存或更新Client,如果关联表对应属性为空不删除关联关系
     * @param client
     */
    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    void saveClient(Client client);

    /**
     * 保存或更新Client以及关联信息
     * @param client
     */
    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    void saveClient(Client client, String[] grantType, String scopesIds, String resourceIds, String redirectUris);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    void saveResource(Resource resource);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    void saveScope(Scope scope);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    void enableClientsRWT(String[] idArray);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    void disableClientsRWT(String[] idArray);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    void deleteResources(String[] idArray);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    void deleteScopes(String[] idArray);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    List<Scope> findScopesByIdArray(String[] idArray);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    List<Resource> findResourceByIdArray(String[] idArray);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
    Client findClientByClientId(String clientId);    
}
