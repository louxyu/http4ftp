package com.youdao.api.manager;

import com.youdao.api.runnable.MainRunnable;
import com.youdao.api.utils.FtpUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by YW on 2017/4/5.
 */
public class YouDaoManager {
    public static final Logger log=Logger.getLogger(FtpUtils.class);

    public static void main(String[] args) throws IOException {
        log.info("启动");
        MainRunnable mainRunnable = new MainRunnable();
        Thread thread = new Thread(mainRunnable);
        thread.start();
        System.out.println("输入任意字符退出…………");
        System.in.read();
        mainRunnable.stop();
    }
}
