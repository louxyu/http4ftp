package com.youdao.api.manager;

import java.io.RandomAccessFile;

/**
 * Created by YW on 2017/6/13.
 */
public class TestRead {

    public static void main(String[] args) throws Exception {

        RandomAccessFile file = new RandomAccessFile("D:\\back\\test.txt","r");
        byte[] bytes = new byte[1024 * 1024 * 5];
        int len = -1;
        while (true) {
            len = file.read(bytes);
            if (len==-1) continue;
            System.out.println(new String(bytes,0,len,"UTF-8"));
        }
    }
}
