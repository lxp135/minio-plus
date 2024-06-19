# 客户端直连

当用户进行文件流的上传和下载时，直接访问MinIO服务器（可配置Nginx代理）。

使用minioclient的GetPresignedObjectUrlArgs方法，入参是一个GetPresignedObjectUrlArgs对象，该对象包含了以下属性：

* bucketName：要访问的桶名。
* objectName：要访问的对象名。
* expires：URL的过期时间，单位为秒。

该方法的返回值是一个字符串类型的URL，可以用于访问指定的对象，示例：

```
http://127.0.0.1:9000/test/test123
?response-content-type=application%2Fmsword%22&response-content-disposition=attachment%3Bfilename%3D%22xxx.doc%22
&X-Amz-Algorithm=AWS4-HMAC-SHA256
&X-Amz-Credential=minioadmin%2F20230620%2Fus-east-1%2Fs3%2Faws4_request
&X-Amz-Date=20230620T071735Z
&X-Amz-Expires=60&X-Amz-SignedHeaders=host
&X-Amz-Signature=5be3535042ffe72fedee8a283e7a5afbc2b068c595c16800cf57f089ed891cc5
```

每次前端请求文件时，都会生成预签名文件地址，文件地址中，有日期、时效、签名。MinIO会进行验签，保证安全性。