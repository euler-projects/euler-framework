package net.eulerframework.web.module.cms.basic.service;

import java.util.List;
import java.util.Set;

import net.eulerframework.web.module.cms.basic.entity.Partner;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.core.exception.MultipartFileSaveException;

@PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('CMS_ADMIN','ADMIN','SYSTEM')")
public interface IPartnerService extends IBaseService {
    
    @PreAuthorize("permitAll")
    public PageResponse<Partner> findPartnerByPage(QueryRequest queryRequest, int pageIndex, int pageSize);

    @PreAuthorize("permitAll")
    public List<Partner> loadPartners(boolean onlyShow);

    @PreAuthorize("permitAll")
    public List<Partner> findPartners(Set<String> ids);

    @PreAuthorize("permitAll")
    public Partner findPartner(String partnerId);
    
    @PreAuthorize("permitAll")
    public List<Partner> findPartnerByNameFuzzy(String name);
    
    public void savePartner(Partner partner, MultipartFile logo) throws MultipartFileSaveException;

    public void deleteLogo(String partnerId);

    public void deletePartners(String[] idArray);

}
