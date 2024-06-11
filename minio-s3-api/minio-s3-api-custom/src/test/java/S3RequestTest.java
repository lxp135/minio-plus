import cn.hutool.http.HttpResponse;
import org.liuxp.minioplus.s3.custom.S3Request;

import java.time.ZonedDateTime;

public class S3RequestTest {


//    public static void main(String[] args){
//
//        String accessKey = "minioadmin";
//        String secretKey = "minioadmin";
//        String backend = "http://localhost:9000";
//        String bucket = "document";
////        String url = "http://localhost:9000/document1";
//
////        ZonedDateTime date = ZonedDateTime.parse("2024-05-31T16:31:54Z");
//        // 取得当前时间
//        ZonedDateTime date = ZonedDateTime.now();
//
//        HttpResponse httpResponse = S3Request.request(accessKey,secretKey,backend,backend+"/"+bucket,"","/"+bucket,"HEAD",date);
//
//        System.out.println("httpResponse.isOk()="+httpResponse.isOk());
//        System.out.println("httpResponse.getStatus()="+httpResponse.getStatus());
//        System.out.println("httpResponse.headers()="+httpResponse.headers());
//        System.out.println("httpResponse.body()="+httpResponse.body());
//
//    }

    public static void main(String[] args){

        String accessKey = "minioadmin";
        String secretKey = "minioadmin";
        String backend = "http://localhost:9000";
        String bucket = "document223";
//        String url = "http://localhost:9000/document1";

//        ZonedDateTime date = ZonedDateTime.parse("2024-05-31T16:31:54Z");
        // 取得当前时间
        ZonedDateTime date = ZonedDateTime.now();

        byte[] EMPTY_BODY = new byte[] {};

        HttpResponse httpResponse = S3Request.request(accessKey,secretKey,backend,backend+"/"+bucket,EMPTY_BODY,"/"+bucket,"PUT",date,0);

        System.out.println("httpResponse.isOk()="+httpResponse.isOk());
        System.out.println("httpResponse.getStatus()="+httpResponse.getStatus());
        System.out.println("httpResponse.headers()="+httpResponse.headers());
        System.out.println("httpResponse.body()="+httpResponse.body());

    }


}
