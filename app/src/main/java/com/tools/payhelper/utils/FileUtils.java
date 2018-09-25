package com.tools.payhelper.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

    // 写一个文件到SDCard
    public static void writeFileToSDCard(String contentData) throws IOException {
        // 比如可以将一个文件作为普通的文档存储，那么先获取系统默认的文档存放根目录
        File parent_path = Environment.getExternalStorageDirectory();

        // 可以建立一个子目录专门存放自己专属文件
        File dir = new File(parent_path.getAbsoluteFile(), "wusi");
        dir.mkdir();

        File file = new File(dir.getAbsoluteFile(), "myfile.txt");

        Log.d("文件路径", file.getAbsolutePath());

        // 创建这个文件，如果不存在
        file.createNewFile();

        FileOutputStream fos = new FileOutputStream(file);

//        String data = "hello,world! Zhang Phil @ CSDN";
        byte[] buffer = contentData.getBytes();

        // 开始写入数据到这个文件。
        fos.write(buffer, 0, buffer.length);
        fos.flush();
        fos.close();

        Log.d("文件写入", "成功");
    }

}
