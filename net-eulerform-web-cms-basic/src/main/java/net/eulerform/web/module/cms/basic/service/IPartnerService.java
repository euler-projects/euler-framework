package net.eulerform.web.module.cms.basic.service;

import java.util.List;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.core.base.service.IBaseService;
import net.eulerform.web.core.exception.MultipartFileSaveException;
import net.eulerform.web.module.cms.basic.entity.Partner;

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
