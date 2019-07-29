
package nc.jzmobile.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtil {
	
	

	 /**
     * ������ʽƥ������ָ���ַ����м������
     * @param soap
     * @return
     */
    public static List<String> getSubUtil(String str, String rgex){
        List<String> list = new ArrayList<String>();
        Pattern pattern = Pattern.compile(rgex);// ƥ���ģʽ
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            int i = 1;
            list.add(m.group(i));
            i++;
        }
        return list;
    }

    /**
     * ���ص����ַ�������ƥ�䵽����Ļ��ͷ��ص�һ����������getSubUtilһ��
     * @param soap
     * @param rgex
     * @return
     */
    public static String getSubUtilOne(String str,String rgex){
        Pattern pattern = Pattern.compile(rgex);// ƥ���ģʽ
        Matcher m = pattern.matcher(str);
        while(m.find()){
            return m.group(1);
        }
        return "";
    }

    /**
     * ����
     * @param args
     */
    public static void main(String[] args) {
        String str = "#user_name,sm_user,cuserid,billmaker#�ύ��#name,bd_psndoc,pk_psndoc,pk_psndoc#�Ļ����������ƻ������ݺ�Ϊ��#vbillcode";
        String rgex = "#(.*?)#";
        System.out.println(getSubUtil(str,rgex));
        System.out.println(getSubUtilOne(str, rgex));
    }
}

