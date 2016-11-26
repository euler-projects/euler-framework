package net.eulerframework.web.module.cms.basic.dao.impl;

import java.util.Date;

import net.eulerframework.web.module.cms.basic.dao.INewsDao;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import net.eulerframework.common.util.CalendarTool;
import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.extend.hibernate5.RestrictionsX;
import net.eulerframework.web.module.cms.basic.entity.News;

public class NewsDao extends BaseDao<News> implements INewsDao {

    @Override
    public PageResponse<News> findNewsByPage(QueryRequest queryRequest, int pageIndex, int pageSize, boolean loadText, boolean enableTop) {

//        String alias = "news_"; // 查詢時的table別名
//        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass, alias);
//
//        ProjectionList projection = Projections.projectionList();
//        projection.add(Projections.property(alias + "." + "id").as("id"));
//        projection.add(Projections.property(alias + "." + "title").as("title"));
//        projection.add(Projections.property(alias + "." + "author").as("author"));
//        projection.add(Projections.property(alias + "." + "top").as("top"));
//        projection.add(Projections.property(alias + "." + "summary").as("summary"));
//        projection.add(Projections.property(alias + "." + "pubDate").as("pubDate"));
//        if (loadText)
//            projection.add(Projections.property(alias + "." + "text").as("text"));
//        projection.add(Projections.property(alias + "." + "imageFileName").as("imageFileName"));
        
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        
        try {
            String queryValue = null;
            queryValue = queryRequest.getQueryValue("title");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(RestrictionsX.like("title", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("author");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(RestrictionsX.like("author", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("summary");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(RestrictionsX.like("summary", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("text");
            if (!StringTool.isNull(queryValue)) {
                detachedCriteria.add(RestrictionsX.like("text", queryValue, MatchMode.ANYWHERE).ignoreCase());
            }
            queryValue = queryRequest.getQueryValue("pubDate");
            if (!StringTool.isNull(queryValue)) {
                Date pubDate = CalendarTool.parseDate(queryValue, "yyyy-MM-dd");
                Date begin = CalendarTool.beginningOfTheDay(pubDate).getTime();
                Date end = CalendarTool.endingOfTheDay(pubDate).getTime();
                detachedCriteria.add(Restrictions.between("pubDate", begin, end));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(enableTop)
            detachedCriteria.addOrder(Order.desc("top"));
        
        detachedCriteria.addOrder(Order.desc("pubDate"));
        
        ProjectionList projection = Projections.projectionList();
        
        projection.add(Projections.property("id").as("id"));
        projection.add(Projections.property("title").as("title"));
        projection.add(Projections.property("author").as("author"));
        projection.add(Projections.property("top").as("top"));
        projection.add(Projections.property("summary").as("summary"));
        projection.add(Projections.property("pubDate").as("pubDate"));
        projection.add(Projections.property("imageFileName").as("imageFileName"));
        if (loadText)
            projection.add(Projections.property("text").as("text"));
        
        return this.findPageBy(detachedCriteria, pageIndex, pageSize, projection);
    }

}
