package nc.jzmobile.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import nc.bs.framework.server.BusinessAppServer;

/**
 * @author:liuhm
 */
public class MobileBillTypePropertiesLoader {
    public String getBillTypeCode(String key) {
    	BusinessAppServer baServer = BusinessAppServer.getInstance();
        String proFilePath = baServer.getServerBase() + "/ierp/jz/mobilebilltype.properties";;
        if ( !new File(proFilePath).exists() ) {
        	return null;
        }
        
        InputStream inputStream;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(proFilePath));
        } catch (FileNotFoundException e1) {
            throw new RuntimeException(e1);
        }  

        Properties prop = new Properties();
        try {
            prop.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        Set keySet = prop.keySet();
//        for(Iterator it = keySet.iterator();it.hasNext();){
//        	String key = (String) it.next();
//        	String value = prop.getProperty(key);
//        }
        String value = prop.getProperty(key);
        if(value==null)
        	return key;
        else 
        	return value;
    }

}
