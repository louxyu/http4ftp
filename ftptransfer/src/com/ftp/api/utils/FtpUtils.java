package com.ftp.api.utils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.Properties;

/**
 * Created by YW on 2017/4/1.
 */
public class FtpUtils {
    public static final String UTF8="UTF-8";

    public static String type;
    private static String hostname;
    private static int port;
    private static String username;
    private static String password;
    private static String path;
    public static String dataFileName;
    private static String encoding;
    public static int bufferedSize;

    //初始化配置信息
    static {
        try {
            Properties p=new Properties();
            p.load(FtpUtils.class.getClassLoader().getResourceAsStream("config/ftpconfig.properties"));
            hostname=p.getProperty("hostname");
            port=Integer.parseInt(p.getProperty("port"));
            username=p.getProperty("username");
            password=p.getProperty("password");
            encoding=p.getProperty("encoding");
            path=p.getProperty("path");
            dataFileName=p.getProperty("dataFileName");
            type=p.getProperty("type");
            bufferedSize=Integer.parseInt(p.getProperty("bufferedSize"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static FTPClient createFTPClient(){
        FTPClient ftpClient = new FTPClient();
        try {
            if(ftpClient.isConnected()){
                ftpClient.logout();
            }
            ftpClient.disconnect();
            ftpClient.connect(hostname,port);
            if(!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())){
                ftpClient.disconnect();
                System.out.println("链接失败");
            }
            boolean result = ftpClient.login(username, password);
            if(!result){
                System.out.println("登录失败");
            }
            ftpClient.changeWorkingDirectory(path);
            ftpClient.setKeepAlive(true);
            ftpClient.setBufferSize(bufferedSize);
            ftpClient.setConnectTimeout(Integer.MAX_VALUE);
            ftpClient.setControlEncoding(encoding);
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
            ftpClient.enterLocalPassiveMode();
        }catch (Exception e){
            e.printStackTrace();
        }
        return ftpClient;
    }

    private static void closeFTPClient(FTPClient ftpClient){
        try {
            if(ftpClient.isConnected()){
                ftpClient.logout();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取FTP上的文本文件内容
     *
     * @param fileName 文件名称
     * @return 文本内容
     * @throws IOException
     */
    /**
     * 读取FTP上的文本文件内容
     *
     * @param fileName 文件名称
     * @return 文本内容
     * @throws IOException
     */
    public static String getFtpTxt(String fileName) {
        StringBuilder sb=new StringBuilder();
        InputStream in =null;
        FTPClient ftpClient=createFTPClient();
        try {
            in = ftpClient.retrieveFileStream(fileName);
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(in,FtpUtils.UTF8));
            String str=null;
            while((str=buffReader.readLine())!=null){
                sb.append(str);
            }

        } catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    ftpClient.completePendingCommand();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    closeFTPClient(ftpClient);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 向FTP文件写入文本内容
     * @param fileName 文件名称
     * @return 文本内容
     * @throws IOException
     */
    public static void setFtpTxt(String fileName,String context) {
        OutputStream out=null;
        FTPClient ftpClient=createFTPClient();
        try {
            out = ftpClient.storeFileStream(fileName);

            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out,encoding));
            bufferedWriter.write(context);
            bufferedWriter.flush();
            out.flush();
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    ftpClient.completePendingCommand();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    closeFTPClient(ftpClient);
                }
            }
        }
    }

}
