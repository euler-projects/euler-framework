package net.eulerform.web.module.demo.task;

import javax.annotation.Resource;

import net.eulerform.web.module.demo.service.IBlogService;

public class Job {
    
    @Resource
    private IBlogService blogService;

    public void job(){
        System.out.println(blogService.find(1L).getName());
        throw new RuntimeException("test");
    }
}
