package com.youdao.api.runnable;

import com.youdao.api.constant.CacheConstant;
import com.youdao.api.utils.FtpUtils;
import com.youdao.api.utils.SSLUtils;
import net.sf.json.JSONObject;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YW on 2017/4/13.
 */
public class ManagerRunnable implements Runnable {

    public static final Logger log = Logger.getLogger(ManagerRunnable.class);

    public static CloseableHttpClient httpClient = null;

    private String fileName;

    public ManagerRunnable(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void run() {
        //返回的响应头信息
        Map<String, String> responseHeads = new HashMap<String, String>();
        //请求头信息
        String jsonParams = null;
        Map<String,String[]> mapParams=null;

        CloseableHttpClient httpClient = null;
        HttpUriRequest request=null;
        try {
            //请求地址信息
            String url = FtpUtils.getFtpTxt(this.fileName + FtpUtils.SUFFEX_URL);
            //获取request参数
            ByteArrayInputStream byteIn = new ByteArrayInputStream(FtpUtils.getFtpByte(this.fileName + FtpUtils.SUFFEX_REQUEST_PARAM));
            ObjectInputStream objectin = new ObjectInputStream(byteIn);
            Map<String, Object> requestMap = (Map<String, Object>) objectin.readObject();

            //请求头信息
            List<Header> headerMap = (List<Header>) requestMap.get(FtpUtils.headerMap);
            Header[] heads = new Header[headerMap.size()];
            headerMap.toArray(heads);
            //请求方式
            String menthod = (String) requestMap.get(FtpUtils.METHOD);
            //domain
            String domain = (String) requestMap.get(FtpUtils.DOMAIN);
            //请求参数信息
            jsonParams = (String) requestMap.get(FtpUtils.parameterJson);
            mapParams = (Map<String, String[]>) requestMap.get(FtpUtils.parameterMap);

            log.info("不存在的记录：" + url);
            log.info("----------------------------------开始-------------------------------------");

            httpClient = getHttpClient(domain);

            HttpResponse response = null;

            if (FtpUtils.METHOD_GET.equals(menthod)) {
                request = new HttpGet(url);
            }else if(FtpUtils.METHOD_HEAD.equals(menthod)){
                request = new HttpHead(url);
            } else if (FtpUtils.METHOD_POST.equals(menthod)) {
                request = new HttpPost(url);

                List<NameValuePair> formParams=new ArrayList<NameValuePair>();
                if(mapParams!=null && !mapParams.isEmpty()){
                    for (Map.Entry<String,String[]> entry:mapParams.entrySet()){
                        String name=entry.getKey();
                        String[] values=entry.getValue();
                        if(values==null||values.length==0){
                            formParams.add(new BasicNameValuePair(name,null));
                        }else{
                            for (String val:values){
                                formParams.add(new BasicNameValuePair(name,val));
                            }
                        }
                    }
                }
                Header contentHead=null;
                for (Header header:heads) {
                    if(header.getName().equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)){
                        contentHead=header;
                    }
                }
                UrlEncodedFormEntity entity=new UrlEncodedFormEntity(formParams,FtpUtils.UTF8);
                entity.setContentType(contentHead);
                ((HttpPost)request).setEntity(entity);

            }else{
                throw new RuntimeException("error:咱不支持的请求类型："+menthod);
            }

            request.setHeaders(heads);

            response = httpClient.execute(request);
            //响应头信息
            log.info("###############################menthod###########"+menthod);
            log.info("###############################response###########"+response);
            heads = response.getAllHeaders();
            if (heads != null) {
                for (Header h : heads) {
                    responseHeads.put(h.getName(), h.getValue());
                }
            }
            Header contentLength = response.getFirstHeader(HttpHeaders.CONTENT_LENGTH);
            long contentLen=0;
            if(contentLength!=null){
                responseHeads.put(contentLength.getName(), contentLength.getValue());
                contentLen = Long.parseLong(contentLength.getValue());
            }

            //写入响应头
            if (FtpUtils.createFtpFile(this.fileName + FtpUtils.SUFFEX_RESPONSE_PARAM, JSONObject.fromObject(responseHeads).toString())) {
                if (FtpUtils.createFtpFile(this.fileName + FtpUtils.SUFFEX_END, "")) {
                }else{
                    log.warn(url + "::::end创建失败");
                }
            } else {
                log.warn(url + "::::response创建失败");
            }
            //相应数据
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //采用分部传输数据
                if(!FtpUtils.createFtpFile(this.fileName,FtpUtils.SUFFEX_CONTENT,entity)){
                    log.error("context数据写入异常！");
                }
                if (request != null)
                    request.abort();
            }
            log.info("----------------------------------结束-------------------------------------");

        } catch (Exception e) {
            log.error("ManagerRunnable异常", e);
        } finally {
            if (request != null) request.abort();
            //删除对应URL文件
            FtpUtils.deleteFtpFile(this.fileName + FtpUtils.SUFFEX_URL);
            FtpUtils.deleteFtpFile(this.fileName + FtpUtils.SUFFEX_REQUEST_PARAM);
        }
    }

    private synchronized CloseableHttpClient getHttpClient(String domain) throws NoSuchAlgorithmException, KeyManagementException {
        if(httpClient==null){
            CookieStore cookieStore = CacheConstant.cacheCookieStoreMap.get(domain);
            if (cookieStore == null) {
                cookieStore = new BasicCookieStore();
                CacheConstant.cacheCookieStoreMap.put(domain, cookieStore);
            }
        /*Map<String,String> cookies = (Map<String,String>)requestMap.get(FtpUtils.cookie);
        for (Map.Entry<String,String> entry:cookies.entrySet()){
            Cookie cookie=new BasicClientCookie(entry.getKey(),entry.getValue());
            cookieStore.addCookie(cookie);
        }*/
            //创建请求链接
            //hc = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

            RequestConfig requestConfig = RequestConfig.custom()
                    .setCircularRedirectsAllowed(true)
                    .setMaxRedirects(5)
                    .setRedirectsEnabled(false)
                    .build();

            SSLContext sslContext = SSLUtils.createSSLContext();
            Registry<ConnectionSocketFactory> socketFactoryRegistry= RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https",new SSLConnectionSocketFactory(sslContext)).build();
            PoolingHttpClientConnectionManager connManager=new PoolingHttpClientConnectionManager(socketFactoryRegistry);

            httpClient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setDefaultRequestConfig(requestConfig)
                    .setConnectionManager(connManager)
                    .build();
        }
        return httpClient;
    }
}
