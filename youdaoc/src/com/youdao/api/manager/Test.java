package com.youdao.api.manager;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by YW on 2017/5/24.
 */
public class Test {

    public static void main(String[] args) throws IOException {


        RandomAccessFile file = new RandomAccessFile("D:\\back\\test.txt","rwd");

        int i=0;
        while (true) {
            i++;
            file.write((i+"\r\n").getBytes("UTF-8"));
            if(i>=10000){
                file.close();
                break;
            }
        }
    }

}
