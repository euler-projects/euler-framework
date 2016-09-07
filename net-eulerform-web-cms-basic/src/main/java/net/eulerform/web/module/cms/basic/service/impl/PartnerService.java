package net.eulerform.web.module.cms.basic.service.impl;

import java.io.File;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.eulerform.common.util.BeanTool;
import net.eulerform.common.util.FileReader;
import net.eulerform.common.util.StringTool;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.core.exception.MultipartFileSaveException;
import net.eulerform.web.core.util.WebConfig;
import net.eulerform.web.core.util.WebFileTool;
import net.eulerform.web.module.cms.basic.dao.IPartnerDao;
import net.eulerform.web.module.cms.basic.entity.Partner;
import net.eulerform.web.module.cms.basic.service.IPartnerService;

@Service
public class PartnerService extends BaseService implements IPartnerService {

    @Resource IPartnerDao partnerDao;

    @Override
    public void savePartner(Partner partner, MultipartFile logo) throws MultipartFileSaveException {
        BeanTool.clearEmptyProperty(partner);

        if(logo != null && logo.getSize() > 0){
            File savedFile = WebFileTool.saveMultipartFile(logo);
            partner.setLogoFileName(savedFile.getName()); 
        }
        
        if(partner.getId() != null) {
            if(StringTool.isNull(partner.getLogoFileName())){
                Partner oldPartner = this.partnerDao.load(partner.getId());
                if(oldPartner != null)
                    partner.setLogoFileName(oldPartner.getLogoFileName());
            } else {
                this.deleteLogo(partner.getId());
            }            
        }
        
        if(partner.getOrder() == null){
            int maxOrder = this.partnerDao.findMaxOrder();
            partner.setOrder(++maxOrder);
        }
        
        this.partnerDao.saveOrUpdate(partner);
    }

    @Override
    public PageResponse<Partner> findPartnerByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        return this.partnerDao.findPartnerByPage(queryRequest, pageIndex, pageSize);
    }

    @Override
    public void deleteLogo(String partnerId) {
        Partner partner = this.partnerDao.load(partnerId);
        if(partner != null) {
            String filePath = this.getServletContext().getRealPath(WebConfig.getUploadPath())+"/"+partner.getLogoFileName();
    
            FileReader.deleteFile(new File(filePath));
        }
        
    }

    @Override
    public void deletePartners(String[] idArray) {
        if(idArray.length > 0) {
            for(String id : idArray) {
                this.deleteLogo(id);
            }
        }
        this.partnerDao.deleteByIds(idArray);
    }

    @Override
    public List<Partner> loadPartners(boolean onlyShow) {
        return this.partnerDao.loadPartners(onlyShow);
    }

    @Override
    public List<Partner> findPartners(Set<String> ids) {
        if(ids == null || ids.isEmpty())
            return null;
        
        String[] idArray = ids.toArray(new String[0]);
        return this.partnerDao.load(idArray);
    }

    @Override
    public Partner findPartner(String partnerId) {
        return this.partnerDao.load(partnerId);
    }

    @Override
    public List<Partner> findPartnerByNameFuzzy(String name) {
        return this.partnerDao.findPartnerByNameFuzzy(name);
    }
}
