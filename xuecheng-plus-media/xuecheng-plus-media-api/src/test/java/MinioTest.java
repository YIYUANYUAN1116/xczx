import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.po.MediaFiles;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class MinioTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://123.60.189.149:9000/")
                    .credentials("admin", "yzd20217551")
                    .build();

    @Test
    public void testAdd()  {
        UploadObjectArgs test = null;
        try {
            test = UploadObjectArgs.builder()
                    .bucket("test")
                    .object("001/1.jpg")
                    .filename("C:\\Users\\YIYUANYUAN\\Desktop\\图\\1.jpg")
                    .build();
            minioClient.uploadObject(test);
            System.out.println("上传成功");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testDelete()  {
        RemoveObjectArgs test = RemoveObjectArgs.builder()
                .bucket("test")
                .object("001/1.jpg")
                .build();

        try {
            minioClient.removeObject(test);
            System.out.println("删除成功");
        } catch (Exception e) {

        }

    }


    @Resource
    MediaFilesMapper mediaFilesMapper;
    @Test
    public void testDataBase()  {
        if (mediaFilesMapper == null){
            System.out.println("null");
        }else {
            MediaFiles mediaFiles = mediaFilesMapper.selectById("1137f04b2f44d1b2c37bcb73608864da");
            System.out.println(mediaFiles);
        }

    }
}
