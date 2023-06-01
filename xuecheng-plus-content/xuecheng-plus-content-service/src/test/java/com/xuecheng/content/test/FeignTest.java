package com.xuecheng.content.test;

import com.xuecheng.content.ContentApplication;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@SpringBootTest(classes = ContentApplication.class)
@RunWith(SpringRunner.class)
public class FeignTest {
    @Autowired
    MediaServiceClient mediaServiceClient;
    @Test
    public void test(){
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("C:\\Users\\YUANYUAN\\Desktop\\test.html"));
        mediaServiceClient.upload(multipartFile,"course","test.html");

    }
}
