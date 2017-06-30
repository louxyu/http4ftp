package com.youdao.filter;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

/**
 * Created by YW on 2017/4/12.
 */
public class FTPFileSuffexFilter implements FTPFileFilter {
    private String[] suffexs;

    public FTPFileSuffexFilter(String[] suffexs) {
        this.suffexs = suffexs;
    }
    public void setSuffexs(String[] suffexs) {
        this.suffexs = suffexs;
    }

    @Override
    public boolean accept(FTPFile ftpFile) {
        if(suffexs==null||suffexs.length==0){
            return ftpFile.isFile();
        }
        boolean flg=false;
        for (String suffex:suffexs) {
            if(ftpFile.getName().contains(suffex)){
                flg=true;
                break;
            }
        }
        return ftpFile.isFile()&&flg;
    }
}
