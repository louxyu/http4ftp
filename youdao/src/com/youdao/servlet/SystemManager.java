package com.youdao.servlet;

import com.youdao.constant.CacheConstant;
import com.youdao.utils.FtpUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by YW on 2017/4/11.
 */
public class SystemManager extends HttpServlet {

    public static final Logger log= LogManager.getLogger(SystemManager.class);

    private static final long serialVersionUID = 466406558358633024L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String result="";
        try {
            String  type=request.getParameter("type");
            if ("clear".equalsIgnoreCase(type)) {
                clearTempFile();
            } else if ("clearCache".equalsIgnoreCase(type)) {
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
            }
            result = "执行成功！";
        } catch (Exception e) {
            log.error("SystemManager异常：",e);
            result = "执行失败！";
        }
        request.setAttribute("message", result);
        request.getRequestDispatcher("/system.jsp").forward(request, response);
    }

    /**
     * 清除生成的临时文件
     */
    private void clearTempFile() {
        List<String> list = FtpUtils.getFtpFileNames(null, null);
        for (String fileName : list) {
            FtpUtils.deleteFtpFile(fileName);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }
}
