package com.ftp.api.manager;

import com.ftp.api.utils.ClipboardUtil;
import com.ftp.api.utils.FtpUtils;
import org.apache.commons.net.ftp.FTP;

import java.io.*;

/**
 * Created by YW on 2017/4/5.
 */
public class FtpManager {
    public static void main(String[] args) {
        try {
            while(true){
                System.out.println("请输入指令：1：写入内容；2：读取内容；3:退出程序。");
                BufferedReader bufferIn = new BufferedReader(new InputStreamReader(System.in));
                String command=bufferIn.readLine();
                while (!"1".equals(command)&&!"2".equals(command)&&!"3".equals(command)){
                    System.out.println("请输入指令：1：写入内容；2：读取内容；3:退出程序。");
                    command=bufferIn.readLine();
                }
                //写入内容
                if("1".equals(command)){
                    System.out.println("请输入要写入的内容,输入end_mark结束输入。");
                    StringBuilder sb=new StringBuilder();
                    while((command=bufferIn.readLine())!=null){
                        if("end_mark".equals(command)){
                            break;
                        }else{
                            sb.append(command);
                            sb.append("\r\n");
                        }
                    }
                    FtpUtils.setFtpTxt(FtpUtils.dataFileName,sb.toString());

                    System.out.println("已写入");
                }else if("2".equals(command)){
                    //读取内容
                    System.out.println("--------------------------获取内容如下(已放入剪贴板中)---------------------");
                    String content=FtpUtils.getFtpTxt(FtpUtils.dataFileName);
                    System.out.println(content);
                    ClipboardUtil.setClipboard(content);
                    System.out.println("-------------------------------------------------------------------------");
                }else{
                    System.exit(0);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
