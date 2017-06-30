package com.youdao.servlet;

import com.youdao.constant.CacheConstant;
import com.youdao.filter.FTPFileSuffexFilter;
import com.youdao.utils.FtpUtils;
import net.sf.json.JSONObject;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

;

/**
 * Created by YW on 2017/4/1.
 */
public class OpenApiServlet extends HttpServlet {

    public static final Logger log= LogManager.getLogger(OpenApiServlet.class);

    private static final long serialVersionUID = 1L;


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String urlName=null;
        try{

            StringBuffer sbUrl = request.getRequestURL();
            String method = request.getMethod().toLowerCase();

            String queryString = request.getQueryString();
            if(queryString!=null && !queryString.isEmpty()){
                sbUrl.append("?").append(queryString);
            }

            //参数信息处理
            Map<String, String[]> params = request.getParameterMap();
            String jsonParams = JSONObject.fromObject(params).toString();

            Map<String,String> cookieMap=new HashMap<String,String>();
            //cookie信息处理
            Cookie[] cookies = request.getCookies();
            if(cookies!=null){
                for (Cookie cookie:cookies){
                    cookieMap.put(cookie.getName(),cookie.getValue());
                }
            }
            //session信息 org.apache.catalina.session.StandardSessionFacade
           /* HttpSession session = request.getSession();
            Enumeration<String> attrNames = session.getAttributeNames();
            if(attrNames.hasMoreElements()){
                String name = attrNames.nextElement();
                Object value = session.getAttribute(name);
            }*/

            String url=sbUrl.toString();

            String cacheKey = url + "_" + method;

            if(url.contains("://localhost")||url.contains("192.168.6.95")){
               log.info("message:localhost访问");
                request.getRequestDispatcher("/system.jsp").forward(request, response);
            } else if (url.contains("http://repo2.maven.org/maven2/.index/nexus-maven-repository-index.gz")) {
                FileInputStream in = null;
                try {
                    File file = new File("D:\\back\\nexus-maven-repository-index.gz");
                    in = new FileInputStream(file);
                    response.setContentLength(369399677);
                    response.setContentType("application/x-gzip");

                    response.reset();
                    OutputStream out = response.getOutputStream();
                    byte[] bytes = new byte[1024 * 1024 * 3];
                    int len = -1;
                    while ((len = in.read(bytes)) != -1) {
                        out.write(bytes,0,len);
                    }
                    out.flush();
                } catch (Exception e) {
                    log.error("nexus-maven-repository-index 异常！");
                } finally{
                    if(in!=null){
                        in.close();
                    }
                }


            } else {
                log.info("cacheKey:" + cacheKey);
                byte[] context = CacheConstant.cacheMap.get(cacheKey);
                String temPath = CacheConstant.cacheMaxContentMap.get(cacheKey);
                String oldJsonParams = CacheConstant.cacheRequestMap.get(cacheKey);

                if (((context != null && context.length > 0)|| temPath!=null) && jsonParams.equals(oldJsonParams)) {
                    Map<String, String> headerMapr = CacheConstant.cacheResponseMap.get(cacheKey);
                    if(context != null && context.length > 0){
                        response.getOutputStream().write(context);
                        setResponse(response, headerMapr);
                        log.info("使用内存缓存数据contextlength:" + context.length);
                    }else{
                        try (FileInputStream fileInputStream=new FileInputStream(FtpUtils.tempCachePath + temPath+FtpUtils.SUFFEX_CONTENT)){
                            String contextLength = headerMapr.get(HttpHeaders.CONTENT_LENGTH);
                            if(contextLength!=null){
                                setResponse(response, headerMapr);
                                response.reset();

                                OutputStream out = response.getOutputStream();

                                byte[] buff = new byte[FtpUtils.maxResponseBufferedSize];
                                int len = -1;
                                while ((len = fileInputStream.read(buff)) != -1) {
                                    out.write(buff, 0, len);
                                }
                                out.flush();
                            }
                        }
                        log.info("使用文件缓存数据!");
                    }
                } else {
                    //生成URL文件名
                    urlName = FtpUtils.romandUUIDFileName();
                    //写入参数
                    if (writeRequestParam(request, jsonParams, params, cookieMap, urlName, cacheKey)) {
                        //创建文件并将URL写入文件
                        if (FtpUtils.createFtpFile(urlName + FtpUtils.SUFFEX_URL, url)) {
                            //写入URL完成标志
                            boolean flg = FtpUtils.createFtpFile(urlName + FtpUtils.SUFFEX_BEGIN, "");
                            if (!flg) {
                                log.info(url + "::::begin创建失败");
                                return;
                            }
                        } else {
                            log.info(url + "::::request创建失败");
                            return;
                        }
                    } else {
                        log.info(url + "::::url创建失败");
                        return;
                    }

                    //判断数据是否传输完成
                    isComplete(urlName);
                    //获取响应头信息
                    String strHeader = FtpUtils.getFtpTxt(urlName + FtpUtils.SUFFEX_RESPONSE_PARAM);
                    Map<String, String> headerMapr = (Map<String, String>) JSONObject.toBean(JSONObject.fromObject(strHeader), HashMap.class);
                    //缓存response信息
                    CacheConstant.cacheResponseMap.put(cacheKey, headerMapr);

                    if (headerMapr.get("Location") != null) {
                        log.info("重定向：" + headerMapr.get("Location"));
                        System.out.println("----------------------LOUXUEYU,当前类=OpenApiServlet.doPost()," + "Location=" + headerMapr.get("Location"));
                        response.sendRedirect(headerMapr.get("Location"));
                    } else {
                        boolean flag = false;
                        String contextLength = headerMapr.get(HttpHeaders.CONTENT_LENGTH);
                        if (contextLength == null) {
                            flag = FtpUtils.pushData(urlName, FtpUtils.SUFFEX_CONTENT, contextLength, response,cacheKey);
                            setResponse(response, headerMapr);
                        } else {
                            setResponse(response, headerMapr);
                            flag = FtpUtils.pushData(urlName, FtpUtils.SUFFEX_CONTENT, contextLength, response,cacheKey);
                        }
                        if (!flag) {
                            log.error("获取响应数据异常！");
                        }

                    }
                }
            }

        }catch (Exception e){
            log.error(e);
            if(urlName!=null){
                //删除返回结果文件
                FtpUtils.deleteFtpFile(urlName+FtpUtils.SUFFEX_BEGIN);
                FtpUtils.deleteFtpFile(urlName+FtpUtils.SUFFEX_URL);
                FtpUtils.deleteFtpFile(urlName+FtpUtils.SUFFEX_REQUEST_PARAM);
            }
        }finally {
            if(urlName!=null){
                //删除返回结果文件
                FtpUtils.deleteFtpFile(urlName+FtpUtils.SUFFEX_CONTENT);
                FtpUtils.deleteFtpFile(urlName+FtpUtils.SUFFEX_RESPONSE_PARAM);
                FtpUtils.deleteFtpFile(urlName+FtpUtils.SUFFEX_END);
            }
        }
    }

    private void setResponse(HttpServletResponse response, Map<String, String> headerMapr) {
        for (Map.Entry<String,String> entry:headerMapr.entrySet()) {
            if(entry.getKey().equalsIgnoreCase(HttpHeaders.CONNECTION) || entry.getValue()==null || "".equals(entry.getValue())) continue;
            response.setHeader(entry.getKey(),entry.getValue());
        }
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONNECTION,"keep-alive");
        if (headerMapr.get(HttpHeaders.CONTENT_TYPE) != null) {
            response.setContentType(headerMapr.get(HttpHeaders.CONTENT_TYPE));

        }

        String contextLength = headerMapr.get(HttpHeaders.CONTENT_LENGTH);
        if (contextLength != null) {
            log.info("-------------------contextLength:" + contextLength);
            response.setContentLengthLong(Long.parseLong(contextLength));
        }
    }

    private void isComplete(String urlName) {
        long keepTime=new Date().getTime();
        List<String> listNames=null;
        boolean flg=true;
        while (flg){
            listNames = FtpUtils.getFtpFileNames(urlName + FtpUtils.SUFFEX_END,new FTPFileSuffexFilter(new String[]{FtpUtils.SUFFEX_END}));
            flg=listNames==null||listNames.isEmpty();
            if(flg && (new Date().getTime()-keepTime)>FtpUtils.waitTime){
                throw new RuntimeException("等待超时 waitTime："+FtpUtils.waitTime);
            }
        }
    }

    private boolean writeRequestParam(HttpServletRequest request,String jsonParams,Map<String, String[]> params,Map<String,String> cookies, String urlName,String cacheKey) throws IOException {
        ByteArrayOutputStream byteOut=new ByteArrayOutputStream();
        ObjectOutputStream out=null;
        try {
            //参数处理
            //请求头信息
            Map<String,Object> requestMap=new HashMap<String,Object>();
            //请求头参数信息
            Map<String, String[]> tempParams=new HashMap<String, String[]>();
            if(params!=null && !params.isEmpty()){
                for (Map.Entry<String,String[]> entry:params.entrySet()){
                    tempParams.put(entry.getKey(),entry.getValue());
                }
            }

            requestMap.put(FtpUtils.parameterMap, tempParams);

            requestMap.put(FtpUtils.parameterJson, jsonParams);
            //缓存param信息
            CacheConstant.cacheRequestMap.put(cacheKey,jsonParams);

            //请求头cookie信息full
            requestMap.put(FtpUtils.cookie, cookies);
            //缓存cookie信息
            CacheConstant.cacheCookiesMap.put(cacheKey,cookies);
            //请求头头部信息
            Enumeration<String> headerNames = request.getHeaderNames();
            List<Header> headers=new ArrayList<>();
            requestMap.put(FtpUtils.headerMap,headers);
            while (headerNames.hasMoreElements()) {
                String headerName=headerNames.nextElement();
                if(!HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(headerName)){
                    headers.add(new BasicHeader(headerName,request.getHeader(headerName)));
                }
            }
            //请求方式
            requestMap.put(FtpUtils.METHOD,request.getMethod().toLowerCase());
            //domain
            requestMap.put(FtpUtils.DOMAIN,request.getRequestURL().toString());
            //将参数写入FTP
            out=new ObjectOutputStream(byteOut);
            out.writeObject(requestMap);
            out.flush();
        }finally {
            out.close();
        }

       return FtpUtils.createFtpFile(urlName+FtpUtils.SUFFEX_REQUEST_PARAM,byteOut.toByteArray());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
}
