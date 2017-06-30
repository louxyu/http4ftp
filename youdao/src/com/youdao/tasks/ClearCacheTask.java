package com.youdao.tasks;

import com.youdao.constant.CacheConstant;
import com.youdao.utils.FtpUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Map;

/**
 * Created by YW on 2017/6/8.
 * 定时清理缓存数据 deny allow
 */
public class ClearCacheTask implements BaseTask {

    private static Logger log = LogManager.getLogger(CacheConstant.class);
    @Override
    public void run() {
        CacheConstant.cacheMap.clear();
        for (Map.Entry<String,String> entry:CacheConstant.cacheMaxContentMap.entrySet()){
            try {
                boolean flg=new  File(FtpUtils.tempCachePath +entry.getValue() + FtpUtils.SUFFEX_CONTENT).delete();

            } catch (Exception e) {
                log.error("删除本地缓存文件异常：", e);
            }
        }
        CacheConstant.cacheMaxContentMap.clear();
        CacheConstant.cacheRequestMap.clear();
        CacheConstant.cacheCookiesMap.clear();
        CacheConstant.cacheResponseMap.clear();
        CacheConstant.cacheCookieStoreMap.clear();
        log.info("更新了缓存!");
    }

}
