
package nc.jzmobile.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtil {
	
	

	 /**
     * 正则表达式匹配两个指定字符串中间的内容
     * @param soap
     * @return
     */
    public static List<String> getSubUtil(String str, String rgex){
        List<String> list = new ArrayList<String>();
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            int i = 1;
            list.add(m.group(i));
            i++;
        }
        return list;
    }

    /**
     * 返回单个字符串，若匹配到多个的话就返回第一个，方法与getSubUtil一样
     * @param soap
     * @param rgex
     * @return
     */
    public static String getSubUtilOne(String str,String rgex){
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式
        Matcher m = pattern.matcher(str);
        while(m.find()){
            return m.group(1);
        }
        return "";
    }

    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) {
        String str = "#user_name,sm_user,cuserid,billmaker#提交的#name,bd_psndoc,pk_psndoc,pk_psndoc#的混凝土浇筑计划，单据号为：#vbillcode";
        String rgex = "#(.*?)#";
        System.out.println(getSubUtil(str,rgex));
        System.out.println(getSubUtilOne(str, rgex));
    }
}

