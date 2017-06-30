package com.youdao.api.utils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by YW on 2017/5/24.
 */
public class SSLUtils {

    //创建ssl上下文
    public static SSLContext createSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc=SSLContext.getInstance("SSLv3");
        X509TrustManager trustManager=new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sc.init(null,new TrustManager[]{trustManager},null);
        return sc;
    }
}
