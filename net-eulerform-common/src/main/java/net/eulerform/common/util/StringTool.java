package net.eulerform.common.util;

public abstract class StringTool {

    public final static boolean isNull(String inputStr){
        return ((inputStr == null) || (inputStr.trim().equals("")) || (inputStr.trim().toLowerCase().equals("null")));
    }
}
