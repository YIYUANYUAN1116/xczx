package com.xuecheng.content.feignclient;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.fallback.MediaServiceClientFallbackFactory;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description 媒资管理服务远程接口
 * @date 2022/9/20 20:29
 * @version 1.0
 */
@FeignClient(value = "media-service",configuration = MultipartSupportConfig.class,fallbackFactory = MediaServiceClientFallbackFactory.class)
@RequestMapping("/media")
public interface MediaServiceClient {

    @RequestMapping(value = "/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    UploadFileResultDto upload(@RequestPart("filedata") MultipartFile filedata, @RequestParam(value = "folder",required=false) String folder, @RequestParam(value = "objectName",required=false) String objectName);
}
