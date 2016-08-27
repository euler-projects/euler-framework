package net.eulerform.web.module.cms.basic.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.core.base.service.IBaseService;
import net.eulerform.web.module.cms.basic.entity.Partner;

@PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('CMS_ADMIN','ADMIN','SYSTEM')")
public interface IPartnerService extends IBaseService {
    
    @PreAuthorize("permitAll")
    public PageResponse<Partner> findPartnerByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    @PreAuthorize("permitAll")
    public List<Partner> loadPartners();

    public void savePartner(Partner partner);

    public void deleteLogo(String partnerId);

    public void deletePartners(String[] idArray);

}
