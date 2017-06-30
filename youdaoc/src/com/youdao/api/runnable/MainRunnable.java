package com.youdao.api.runnable;

import com.youdao.api.filter.FTPFileSuffexFilter;
import com.youdao.api.utils.FtpUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by YW on 2017/6/14.
 */
public class MainRunnable implements Runnable {

    public static final Logger log= Logger.getLogger(MainRunnable.class);
    private static ExecutorService executor= Executors.newFixedThreadPool(FtpUtils.threadSize);

    private boolean flag;
    @Override
    public void run() {
        flag=true;
        while(flag){
            //获取所有begin后缀的文件名
            List<String> listNames = FtpUtils.getFtpFileNames(null, new FTPFileSuffexFilter(new String[]{FtpUtils.SUFFEX_BEGIN}));
            if(listNames!=null && !listNames.isEmpty()){
                for (String fileName:listNames) {
                    //去除后缀的
                    String tempFileName=fileName.substring(0,fileName.indexOf(FtpUtils.SUFFEX_BEGIN));
                    //删除已获取的文件
                    boolean flg = FtpUtils.deleteFtpFile(tempFileName + FtpUtils.SUFFEX_BEGIN);
                    if(!flg){
                        FtpUtils.deleteFtpFile(tempFileName + FtpUtils.SUFFEX_BEGIN);
                    }
                    //创建线程执行
                    executor.execute(new ManagerRunnable(tempFileName));
                }
            }
        }
    }

    public void stop(){
        this.flag = false;
        executor.shutdownNow();
        while (executor.isTerminated()) {
        }
    }
}
