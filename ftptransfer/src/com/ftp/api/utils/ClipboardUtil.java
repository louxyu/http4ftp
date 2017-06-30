package com.ftp.api.utils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * Created by YW on 2017/4/10.
 */
public class ClipboardUtil {
    public static void setClipboard(String contents){
        StringSelection stringSelection = new StringSelection(contents);

        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        systemClipboard.setContents(stringSelection,null);
    }
}
