package net.eulerframework.web.core.base.request;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.core.base.exception.IllegalParamException;

public class PageQueryRequest {
    private final static String PROPERTIES = "properties";
    private final static String OPERATORS = "operators";
    private final static String EXPRESSIONS = "expressions";
    private final static String SPLIT = "split";
    private final static String PAGE_INDEX = "pageIndex";
    private final static String PAGE_SIZE = "pageSize";
    
    
    private final ArrayList<String> properties;
    private final ArrayList<String> operators;
    private final ArrayList<String> expressions;
    private final String split;
    
    private final int pageIndex;
    private final int pageSize;
    
    private PageQueryRequest(HttpServletRequest request) {
        
        this.split = generateSplit(request);
        
        this.pageIndex = generatePageIndex(request);
        this.pageSize = generatePageSize(request);
        
        this.properties = generateProperties(request);
        this.operators = generateOperators(request);
        this.expressions = generateExpressions(request);
    }

    private int generatePageSize(HttpServletRequest request) {
        try {
            return Integer.parseInt(request.getParameter(PAGE_SIZE));
        } catch (NumberFormatException e) {
            throw new IllegalParamException(PAGE_SIZE + " must be int");
        }
    }

    private int generatePageIndex(HttpServletRequest request) {
        try {
            String pageIndex = request.getParameter(PAGE_INDEX);
            
            if(StringTool.isNull(pageIndex)) {
                if(this.pageIndex > 0) {
                    throw new IllegalParamException(PAGE_INDEX + " is required when pageSize > 0");
                } else {
                    return 0;
                }
            }
            
            return Integer.parseInt(request.getParameter(PAGE_INDEX));
        } catch (NumberFormatException e) {
            throw new IllegalParamException(PAGE_SIZE + " must be int");
        }
    }

    private String generateSplit(HttpServletRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    private ArrayList<String> generateExpressions(HttpServletRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    private ArrayList<String> generateOperators(HttpServletRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    private ArrayList<String> generateProperties(HttpServletRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

}
