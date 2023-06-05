package com.xuecheng.content.feignclient.fallback;

import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.model.dto.CourseIndex;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {
    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {

            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.debug("调用搜索发生熔断走降级方法,熔断异常:", throwable.getMessage());

                return false;

            }
        };
    }
}
