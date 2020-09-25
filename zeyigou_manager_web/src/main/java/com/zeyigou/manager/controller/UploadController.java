package com.zeyigou.manager.controller;

import com.zeyigou.pojo.Result;
import com.zeyigou.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by WF on 2020/9/16 11:04
 */
@RestController
public class UploadController {
    //0.读取图片服务器的地址
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;
    //1.文件上传
    @RequestMapping("upload")
    public Result upload(MultipartFile file)  {
        try {
            //1.1)得到上传工具类对象
            FastDFSClient dfsClient = new FastDFSClient("classpath:properties/fdfs_client.conf");
            //1.2)得到要上传的文件数据
            byte[] bytes = file.getBytes();
            //1.3)进行文件上传,返回的是组名及文件名

            //1.4) 处理文件后缀名
            //1.4.1)得到原始文件名
            String filename = file.getOriginalFilename();
            //1.4.2)处理得到后缀名
            String suffixName = filename.substring(filename.lastIndexOf(".") + 1);
            //1.4.3)进行文件上传
            String uploadFile = dfsClient.uploadFile(bytes, suffixName);

            //1.5)得到最终的图片路径
            String url = FILE_SERVER_URL + uploadFile;
            //1.6)返回
            return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败！");
        }
    }
}
