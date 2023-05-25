import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.po.MediaFiles;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
public class MinioTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://123.60.189.149:9000/")
                    .credentials("admin", "yzd20217551")
                    .build();



    @Test
    public void testUploadChunk(){
        //块文件目录
        String chunkFolder = "C:\\Users\\YUANYUAN\\Desktop\\day09-课件\\bigFileTest\\chunk";
        File file = new File(chunkFolder);
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            try {
                UploadObjectArgs test = UploadObjectArgs.builder()
                        .bucket("test")
                        .filename(chunkFolder + "\\" + i)
                        .object("chunk/" + i)
                        .build();
                minioClient.uploadObject(test);
                System.out.println("上传成功:" + i);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    //合并文件，要求分块文件最小5M
    @Test
    public void test_merge() throws Exception {
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(11)
                .map(i -> ComposeSource.builder()
                        .bucket("test")
                        .object("chunk/".concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());

        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs
                .builder()
                .bucket("test")
                .object("merge01.mp4")
                .sources(sources)
                .build();
        minioClient.composeObject(composeObjectArgs);

    }
    //清除分块文件
    @Test
    public void test_removeObjects(){
        //合并分块完成将分块文件清除
        List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                .limit(6)
                .map(i -> new DeleteObject("chunk/".concat(Integer.toString(i))))
                .collect(Collectors.toList());

        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("test").objects(deleteObjects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        results.forEach(r->{
            DeleteError deleteError = null;
            try {
                deleteError = r.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


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


    @Test
    public void testAdd2()  {
        UploadObjectArgs test = null;
        try {
            test = UploadObjectArgs.builder()
                    .bucket("test")
                    .object("001/123.jpg")
                    .filename("C:\\Users\\YUANYUAN\\Desktop\\appcenter\\123.png")
                    .build();
            minioClient.uploadObject(test);
            System.out.println("上传成功");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testDelete2()  {
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
