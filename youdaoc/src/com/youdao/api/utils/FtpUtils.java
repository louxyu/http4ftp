package com.youdao.api.utils;

import org.apache.commons.net.ftp.*;
import org.apache.http.HttpEntity;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by YW on 2017/4/1.
 */
public class FtpUtils {

    public static final Logger log=Logger.getLogger(FtpUtils.class);
    public static final String UTF8 = "UTF-8";

    public static final String SUFFEX_URL = ".url";

    public static final String SUFFEX_CONTENT = ".content";

    public static final String SUFFEX_CONTENT_END = ".contentend";

    public static final String SUFFEX_END = ".end";

    public static final String SUFFEX_BEGIN = ".begin";

    public static final String SUFFEX_REQUEST_PARAM = ".request";

    public static final String SUFFEX_RESPONSE_PARAM = ".response";


    public static final String parameterMap = "parameterMap";

    public static final String parameterJson = "parameterJson";

    public static final String headerMap = "headerMap";

    public static final String cookie = "cookie";

    public static final String session = "headerMap";

    public static final String METHOD_POST = "post";

    public static final String METHOD_HEAD = "head";

    public static final String METHOD_GET = "get";

    public static final String METHOD = "method";

    public static final String DOMAIN = "domain";

    private static String hostname;

    private static int port;

    public static int threadSize;

    public static int bufferedSize;

    private static String username;

    private static String password;

    private static String path;

    private static String encoding;

    public static Long sleepTime;

    //初始化配置信息
    static {
        try {
            Properties p = new Properties();
            p.load(FtpUtils.class.getClassLoader().getResourceAsStream("config/ftpconfig.properties"));
            hostname = p.getProperty("hostname");
            port = Integer.parseInt(p.getProperty("port"));
            threadSize = Integer.parseInt(p.getProperty("threadSize"));
            bufferedSize = Integer.parseInt(p.getProperty("bufferedSize"));
            sleepTime = Long.parseLong(p.getProperty("sleepTime"));
            username = p.getProperty("username");
            password = p.getProperty("password");
            encoding = p.getProperty("encoding");
            path = p.getProperty("path");
        } catch (IOException e) {
            log.error("加载ftpconfig.properties配置异常！",e);
        }
    }

    private static FTPClient createFTPClient() {
        boolean flag=true;
        FTPClient ftpClient = null;
        while (flag) {
            try {
                ftpClient = new FTPClient();
                ftpClient.connect(hostname, port);
                if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                    ftpClient.disconnect();
                    log.warn("FTP链接失败");
                }
                boolean result = ftpClient.login(username, password);
                if (!result) {
                    log.warn("FTP登录失败");
                }
                ftpClient.changeWorkingDirectory(path);
                ftpClient.setKeepAlive(true);
                ftpClient.setBufferSize(bufferedSize);
                //ftpClient.setConnectTimeout(Integer.MAX_VALUE);
                ftpClient.setControlEncoding(encoding);
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
                ftpClient.enterLocalPassiveMode();
                flag=false;
            } catch (Exception e) {
                log.error("createFTPClient异常！",e);
                log.error("FTP连接异常，将重新连接!"+e.getMessage());
                closeFTPClient(ftpClient);
            }
        }
        return ftpClient;
    }

    private static void closeFTPClient(FTPClient ftpClient) {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
            }
        } catch (Exception e) {
            log.error("closeFTPClient异常！",e);
        } finally {
            try {
                ftpClient.disconnect();
                ftpClient = null;
            } catch (IOException e) {
                log.error("closeFTPClient异常！",e);
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
    public static String getFtpTxt(String fileName) {
        StringBuilder sb = new StringBuilder();
        InputStream in = null;
        FTPClient ftpClient = createFTPClient();
        try {
            in = ftpClient.retrieveFileStream(fileName);
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(in, FtpUtils.UTF8));
            String str = null;
            while ((str = buffReader.readLine()) != null) {
                sb.append(str);
            }

        } catch (Exception e) {
            log.error("读取FTP文本内容异常！",e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                log.error("读取FTP文本内容异常！",e);
            } finally {
                try {
                    ftpClient.completePendingCommand();
                } catch (IOException e) {
                    log.error("读取FTP文本内容异常！",e);
                } finally {
                    closeFTPClient(ftpClient);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 向FTP文件写入文本内容
     *
     * @param fileName 文件名称
     * @return 文本内容
     * @throws IOException
     */
    public static void setFtpTxt(String fileName, String context) {
        OutputStream out = null;
        FTPClient ftpClient = createFTPClient();
        try {
            out = ftpClient.storeFileStream(fileName);

            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out, encoding));
            bufferedWriter.write(context);
            bufferedWriter.flush();
            out.flush();
        } catch (Exception e) {
            log.error("写入FTP文本内容异常！",e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                log.error("写入FTP文本内容异常！",e);
            } finally {
                try {
                    ftpClient.completePendingCommand();
                } catch (IOException e) {
                    log.error("写入FTP文本内容异常！",e);
                } finally {
                    closeFTPClient(ftpClient);
                }
            }
        }
    }

    /**
     * 写入二进制数据到FTP文件
     *
     * @param fileName
     * @param context
     */
    public static void setFtpByte(String fileName, String context) {

        OutputStream out = null;
        FTPClient ftpClient = createFTPClient();
        try {
            out = ftpClient.storeFileStream(fileName);
            out.write(context.getBytes(FtpUtils.UTF8));
            out.flush();
        } catch (Exception e) {
            log.error("setFtpByte异常！",e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                log.error("setFtpByte异常！",e);
            } finally {
                try {
                    ftpClient.completePendingCommand();
                } catch (IOException e) {
                    log.error("setFtpByte异常！",e);
                } finally {
                    closeFTPClient(ftpClient);
                }
            }
        }
    }

    /**
     * 获取字节码文件数据
     *
     * @param fileName
     * @return
     */
    public static byte[] getFtpByte(String fileName) {

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        InputStream in = null;
        FTPClient ftpClient = createFTPClient();
        try {
            in = ftpClient.retrieveFileStream(fileName);
            byte[] b = new byte[bufferedSize];
            int len = -1;
            while ((len = in.read(b)) != -1) {
                bout.write(b, 0, len);
            }
            bout.close();
        } catch (Exception e) {
            log.error("getFtpByte异常！",e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                log.error("getFtpByte异常！",e);
            } finally {
                try {
                    ftpClient.completePendingCommand();
                } catch (IOException e) {
                    log.error("getFtpByte异常！",e);
                } finally {
                    closeFTPClient(ftpClient);
                }
            }
        }
        return bout.toByteArray();
    }

    /**
     * 获取文件数据通过输出流传输
     *
     * @param fileName
     * @param out
     */
    public static void getFtpByte(String fileName, OutputStream out) {

        InputStream in = null;
        FTPClient ftpClient = createFTPClient();
        try {
            in = ftpClient.retrieveFileStream(fileName);
            byte[] b = new byte[bufferedSize];
            int len = -1;
            while ((len = in.read(b)) != -1) {
                out.write(b, 0, len);
            }
            out.flush();

        } catch (Exception e) {
            log.error("getFtpByte异常！",e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                log.error("getFtpByte异常！",e);
            } finally {
                try {
                    ftpClient.completePendingCommand();
                } catch (IOException e) {
                    log.error("getFtpByte异常！",e);
                } finally {
                    closeFTPClient(ftpClient);
                }
            }
        }
    }

    /**
     * 写入二进制数据到FTP文件
     *
     * @param fileName
     * @param context
     */
    public static void setFtpByte(String fileName, byte[] context) {

        OutputStream out = null;
        FTPClient ftpClient = createFTPClient();
        try {
            out = ftpClient.storeFileStream(fileName);
            out.write(context);
            out.flush();
        } catch (Exception e) {
            log.error("setFtpByte异常！",e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                log.error("setFtpByte异常！",e);
            } finally {
                try {
                    ftpClient.completePendingCommand();
                } catch (IOException e) {
                    log.error("setFtpByte异常！",e);
                } finally {
                    closeFTPClient(ftpClient);
                }
            }
        }
    }

    /**
     * 创建文件并写入二进制数据
     *
     * @param fileName 文件名
     * @param context  数据
     * @return
     */
    public static boolean createFtpFile(String fileName, byte[] context) {
        boolean flag = false;
        ByteArrayInputStream in = new ByteArrayInputStream(context);
        FTPClient ftpClient = createFTPClient();
        try {
            flag = ftpClient.storeFile(fileName, in);
        } catch (Exception e) {
            log.error("createFtpFile异常！",e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                log.error("createFtpFile异常！",e);
            } finally {
                closeFTPClient(ftpClient);
            }
        }
        return flag;
    }

    /**
     * 创建文件并写入二进制数据
     *
     * @param fileName 文件名
     * @return
     */
    public static boolean createFtpFile(String fileName,InputStream inputStream) {
        boolean flag = false;
        FTPClient ftpClient = createFTPClient();
        try {
            flag = ftpClient.storeFile(fileName, inputStream);
        } catch (Exception e) {
            log.error("createFtpFile异常！",e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("createFtpFile异常！",e);
            } finally {
                closeFTPClient(ftpClient);
            }
        }
        return flag;
    }

    /**
     * 创建文件并写入二进制数据
     *
     * @param fileName 文件名
     * @return
     */
    public static boolean createFtpFile(String fileName,String suffix,HttpEntity entity){
        boolean flag = false;
        FTPClient ftpClient = createFTPClient();
        OutputStream outputStream = null;
        try {
            InputStream inputStream = entity.getContent();
            byte[] buffByte = new byte[FtpUtils.bufferedSize];
            int len=-1;
            int n1=0;
            int n2=0;
            int count=0;
            while ((len = inputStream.read(buffByte,0,FtpUtils.bufferedSize)) != -1) {
                if(outputStream==null){
                    n1++;
                    try {
                        ftpClient = createFTPClient();
                        outputStream = ftpClient.storeFileStream(fileName + suffix + n1);
                    } catch (Exception e) {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        closeFTPClient(ftpClient);
                        ftpClient = createFTPClient();
                        outputStream = ftpClient.storeFileStream(fileName + suffix + n1);
                    }
                }
                outputStream.write(buffByte,0,len);
                count+=len;
                if(count>=FtpUtils.bufferedSize){
                    n2++;
                    outputStream.close();
                    outputStream = null;
                    closeFTPClient(ftpClient);
                    flag = FtpUtils.createFtpFile(fileName + SUFFEX_CONTENT_END + n2, "");
                    count = 0;
                }
            }
            if (n1 != n2) {
                flag = FtpUtils.createFtpFile(fileName + SUFFEX_CONTENT_END + n1, "");
            }
            flag = FtpUtils.createFtpFile(fileName+suffix, "");
        } catch (Exception e) {
            log.error("createFtpFile异常！",e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                log.error("outputStream 关闭异常！",e);
            }
            closeFTPClient(ftpClient);
        }
        return flag;
    }

    /**
     * 创建文件并写入二进制数据
     *
     * @param fileName
     * @param context
     * @return
     */
    public static boolean createFtpFile(String fileName, String context) {
        boolean flag = false;
        ByteArrayInputStream in = null;
        FTPClient ftpClient = createFTPClient();
        try {
            in = new ByteArrayInputStream(context.getBytes(FtpUtils.UTF8));
            flag = ftpClient.storeFile(fileName, in);
        } catch (Exception e) {
            log.error("createFtpFile异常！",e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                log.error("createFtpFile异常！",e);
            } finally {
                closeFTPClient(ftpClient);
            }
        }
        return flag;
    }

    /**
     * 根据文件名称删除文件
     *
     * @param fileName
     * @return
     */
    public static boolean deleteFtpFile(String fileName) {
        boolean flag = false;
        FTPClient ftpClient = createFTPClient();
        try {
            flag = ftpClient.deleteFile(fileName);
        } catch (Exception e) {
            log.error("deleteFtpFile异常！",e);
        } finally {
            closeFTPClient(ftpClient);
        }
        return flag;
    }

    /**
     * 获取目录下的文件名
     *
     * @param filter   过滤器
     * @param pathname 过滤器
     * @return
     */
    public static List<String> getFtpFileNames(String pathname, FTPFileFilter filter) {
        List<String> fileNames = new ArrayList<String>();
        FTPFile[] files = null;
        FTPClient ftpClient = createFTPClient();
        try {
            if (filter == null && pathname == null) {
                files = ftpClient.listFiles();
            } else if (filter == null) {
                files = ftpClient.listFiles(pathname);
            } else {
                files = ftpClient.listFiles(pathname, filter);
            }

            if (files != null) {
                for (FTPFile item : files) {
                    fileNames.add(item.getName());
                }
            }
        } catch (Exception e) {
            log.error("getFtpFileNames异常！",e);
        } finally {
            closeFTPClient(ftpClient);
        }
        return fileNames;
    }

    /**
     * 随机产生UUID 指定文件名
     *
     * @return
     */
    public static String romandUUIDFileName() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    public static Object getFtpObject(String fileName) {

        Object obj = null;
        InputStream in = null;
        ObjectInputStream oin = null;
        FTPClient ftpClient = createFTPClient();
        try {
            in = ftpClient.retrieveFileStream(fileName);

            oin = new ObjectInputStream(in);
            obj = oin.readObject();
        } catch (Exception e) {
            log.error("getFtpObject异常！",e);
        } finally {
            try {
                oin.close();
            } catch (IOException e) {
                log.error("getFtpObject异常！",e);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("getFtpObject异常！",e);
                } finally {
                    try {
                        ftpClient.completePendingCommand();
                    } catch (IOException e) {
                        log.error("getFtpObject异常！",e);
                    } finally {
                        closeFTPClient(ftpClient);
                    }
                }
            }
        }
        return obj;
    }



    public static void main(String[] args) throws UnsupportedEncodingException {
        //FtpUtils.createFtpFile("test1.content","你好1".getBytes(FtpUtils.UTF8));
        //FtpUtils.createFtpFile("test2.content","你好2".getBytes(FtpUtils.UTF8));
        //FtpUtils.createFtpFile("test3.content","你好3".getBytes(FtpUtils.UTF8));
        //FtpUtils.createFtpFile("test4.content","你好4".getBytes(FtpUtils.UTF8));
        //FtpUtils.createFtpFile("test5.content","你好5".getBytes(FtpUtils.UTF8));
        //FtpUtils.createFtpFile("test6.content","你好6".getBytes(FtpUtils.UTF8));
        // List<String> list = FtpUtils.getFtpFileNames(null, null);
        // for (String fileName : list) {
        //     FtpUtils.deleteFtpFile(fileName);
        // }


    }

}
