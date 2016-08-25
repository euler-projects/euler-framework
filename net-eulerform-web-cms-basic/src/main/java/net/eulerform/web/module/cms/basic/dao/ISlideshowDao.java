package net.eulerform.web.module.cms.basic.dao;

import java.util.List;

import net.eulerform.web.core.base.dao.IBaseDao;
import net.eulerform.web.module.cms.basic.entity.Slideshow;

public interface ISlideshowDao extends IBaseDao<Slideshow> {

    public Slideshow findSlideshowByOrder(int order);

    public List<Slideshow> loadSlideshow();

}
