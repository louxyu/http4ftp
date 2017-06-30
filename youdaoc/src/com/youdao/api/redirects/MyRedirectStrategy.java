package com.youdao.api.redirects;

import com.youdao.api.utils.FtpUtils;
import com.youdao.api.utils.SSLUtils;
import org.apache.http.*;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;

/**
 * Created by YW on 2017/6/6.
 */
public class MyRedirectStrategy implements RedirectStrategy{

    public static final Logger log = Logger.getLogger(RedirectStrategy.class);

    private String menthod;
    private Header[] heads;

    public MyRedirectStrategy(String menthod, Header[] heads) {
        this.menthod=menthod;
        this.heads=heads;
    }

    private static final String[] REDIRECT_METHODS = new String[]{"GET", "HEAD","POST"};
    @Override
    public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        Args.notNull(request, "HTTP request");
        Args.notNull(response, "HTTP response");
        int statusCode = response.getStatusLine().getStatusCode();
        String method = request.getRequestLine().getMethod();
        Header locationHeader = response.getFirstHeader("location");
        log.info("locationHeader---:" + locationHeader.getName() + ":" + locationHeader.getValue());
        switch(statusCode) {
            case 301:
                return this.isRedirectable(method) && locationHeader != null;
            case 307:
                return this.isRedirectable(method);
            case 302:
                return this.isRedirectable(method) && locationHeader != null;
            case 303:
                return true;
            case 304:
            case 305:
            case 306:
            default:
                return false;
        }
    }

    @Override
    public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        HttpUriRequest httpUriRequest=null;
        try {
            log.info("------------locationHeader:"+response.getFirstHeader("location").getValue());
            if (FtpUtils.METHOD_GET.equals(menthod)) {
                httpUriRequest = new HttpGet(response.getFirstHeader("location").getValue());
            }else if(FtpUtils.METHOD_HEAD.equals(menthod)){
                httpUriRequest = new HttpHead(response.getFirstHeader("location").getValue());
            } else if (FtpUtils.METHOD_POST.equals(menthod)) {
                httpUriRequest = new HttpPost(response.getFirstHeader("location").getValue());
            }else{
                throw new RuntimeException("error:咱不支持的请求类型："+menthod);
            }
            httpUriRequest.setHeaders(heads);
            httpUriRequest.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
            log.info("------------URI:"+httpUriRequest.getURI());

            RequestConfig requestConfig = RequestConfig.custom()
                    .setCircularRedirectsAllowed(true)
                    .setMaxRedirects(5)
                    .setRedirectsEnabled(true)
                    .build();

            SSLContext sslContext = SSLUtils.createSSLContext();
            Registry<ConnectionSocketFactory> socketFactoryRegistry= RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https",new SSLConnectionSocketFactory(sslContext)).build();
            PoolingHttpClientConnectionManager connManager=new PoolingHttpClientConnectionManager(socketFactoryRegistry);

            RedirectStrategy myRedirectStrategy=new MyRedirectStrategy(menthod,heads);

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setRedirectStrategy(myRedirectStrategy)
                    .setDefaultRequestConfig(requestConfig)
                    .setConnectionManager(connManager)
                    .build();
            response = httpClient.execute(httpUriRequest);

        } catch (Exception e) {
            log.error(e);
        }
        return  httpUriRequest;
    }

    protected boolean isRedirectable(String method) {
        String[] arr$ = REDIRECT_METHODS;
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String m = arr$[i$];
            if(m.equalsIgnoreCase(method)) {
                return true;
            }
        }

        return false;
    }
}
