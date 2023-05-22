import com.j256.simplemagic.ContentType;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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
                    .object("001/123.png")
                    .filename("C:\\Users\\YUANYUAN\\Desktop\\appcenter\\123.png")
                    .contentType(String.valueOf(ContentType.PNG))
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
                .object("001/123.png")
                .build();

        try {
            minioClient.removeObject(test);
            System.out.println("删除成功");
        } catch (Exception e) {

        }

    }
}
