package com.jian.jianesjd.controller;

import com.jian.jianesjd.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

//业务编写  请求编写
@RestController
public class ContentController {
    @Autowired
    private ContentService contentService;
    @GetMapping("/parse/{keywords}")
    public Boolean parse(@PathVariable("keywords") String keywords) throws IOException {
        return  contentService.parseContent(keywords);
    }
    @GetMapping("/search/{keywords}/{pageNo}/{pageSize}")
    public List<Map<String ,Object>> search(@PathVariable("keywords") String keywords,
                                            @PathVariable("pageNo")int pageNo,
                                            @PathVariable("pageSize")int pageSize){
        return contentService.searchPage(keywords, pageNo, pageSize);
    }
}
