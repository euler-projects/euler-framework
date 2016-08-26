package net.eulerform.web.module.cms.basic.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import net.eulerform.web.core.base.entity.PageResponse;
import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.core.base.service.IBaseService;
import net.eulerform.web.module.cms.basic.entity.Partner;

public interface IPartnerService extends IBaseService {

    public PageResponse<Partner> findPartnerByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    public List<Partner> loadPartners();

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','DEMO','SYSTEM')")
    public void savePartner(Partner partner);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','DEMO','SYSTEM')")
    public void deleteLogo(String partnerId);

    @PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','DEMO','SYSTEM')")
    public void deletePartners(String[] idArray);

}
