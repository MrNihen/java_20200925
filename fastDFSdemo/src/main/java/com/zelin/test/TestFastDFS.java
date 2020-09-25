package com.zelin.test;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

/**
 * Created by WF on 2020/9/16 10:30
 * 测试FASTDFS进行文件上传
 */
public class TestFastDFS {
    public static void main(String[] args) throws Exception {
        //1.初始化fastdfs环境
        ClientGlobal.init("D:\\idea-projects\\20200629\\zeyigou_parent\\fastDFSdemo\\src\\main\\resources\\fdfs_client.conf");
        //2.构造一个trackerClient
        TrackerClient trackerClient = new TrackerClient();
        //3.得到一个trackerServer服务端
        TrackerServer trackerServer = trackerClient.getConnection();
        //4.构造一个storageClient
        StorageClient storageClient = new StorageClient(trackerServer,null);
        //5.进行文件上传
        String[] fileInfo = storageClient.upload_file("F:\\备课资料\\泽易购-备课资料\\网站前台\\img\\banner3.jpg", "jpeg", null);
        //6.遍历数组
        for (String s : fileInfo) {
            System.out.println(s);
        }

    }
}
