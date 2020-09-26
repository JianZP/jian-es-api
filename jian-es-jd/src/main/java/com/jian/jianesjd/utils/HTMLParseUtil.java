package com.jian.jianesjd.utils;

import com.jian.jianesjd.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import javax.swing.text.html.HTML;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
@Component
public class HTMLParseUtil {
    public List<Content> parse(String keywords) throws IOException {
        // 获取请求 https://search.jd.com/Search?keyword=java
        // 前提：需要联网  不能获取到Ajax的数据
        String url="https://search.jd.com/Search?keyword="+keywords;
        // 解析网页  Jsoup返回的就是Document对象就是JS页面对象，也就是浏览器的Document对象
        Document document = Jsoup.parse(new URL(url), 300000);
        // 所有在JS中使用的方法在这里都可以使用
        Element element = document.getElementById("J_goodsList");
        // System.out.println(element.html());
        //获取所有的li标签
        Elements elements = element.getElementsByTag("li");
        // 获取元素中的内容 这里的el就是每一个li标签
        List<Content> goodsList = new ArrayList<>();
        for (Element el : elements) {
            // 关于特别多图片的网站 所有图片都是延时加载的
            // 图片存于 source-data-lazy-img
            String img = el.getElementsByTag("img").eq(0).attr("source-data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title=el.getElementsByClass("p-name").eq(0).text();
            Content content = new Content();
            content.setTitle(title);
            content.setImg(img);
            content.setPrice(price);
            goodsList.add(content);

        }
        return goodsList;
    }
}
