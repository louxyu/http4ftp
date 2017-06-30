package com.youdao.constant;

import org.apache.http.client.CookieStore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by YW on 2017/4/5.
 */
public interface CacheConstant {
    Map<String,byte[]> cacheMap= Collections.synchronizedMap(new HashMap<String, byte[]>());
    Map<String,String> cacheMaxContentMap=Collections.synchronizedMap(new HashMap<String, String>());
    Map<String,String> cacheRequestMap=Collections.synchronizedMap(new HashMap<String, String>());
    Map<String,Map<String,String>> cacheCookiesMap=Collections.synchronizedMap(new HashMap<String, Map<String,String>>());
    Map<String,Map<String,String>> cacheResponseMap=Collections.synchronizedMap(new HashMap<String, Map<String,String>>());
    Map<String,CookieStore> cacheCookieStoreMap=Collections.synchronizedMap(new HashMap<String, CookieStore>());
}
