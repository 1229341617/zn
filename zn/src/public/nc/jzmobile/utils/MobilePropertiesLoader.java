package nc.jzmobile.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import nc.bs.framework.server.BusinessAppServer;
import nc.vo.jzmobile.app.MobilePropModel;

/**
 * @author:liuhm
 */
public class MobilePropertiesLoader {
    public MobilePropModel getViewResolverProperties() {
    	BusinessAppServer baServer = BusinessAppServer.getInstance();
        String proFilePath = baServer.getServerBase() + "/ierp/jz/mobile.properties";;
        if ( !new File(proFilePath).exists() ) {
        	return new MobilePropModel();
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
        
        MobilePropModel propModel = new MobilePropModel();
        propModel.setMaServerMessageURL(getStr(prop, "MAServerMessageURL"));
        propModel.setMaServerURL(getStr(prop, "MAServerURL"));
        propModel.setMaAppId(getStr(prop, "MAAppId"));
        propModel.setAppShowName(getStr(prop, "AppShowName"));
        
        
        propModel.setMobileApproveURL(getStr(prop, "MobileApproveURL"));
        propModel.setMobileApproveHtml(getStr(prop, "MobileApproveHtml"));
        
        propModel.setDataSource(getStr(prop, "DataSource"));
        
        propModel.setFileDataSource(getStr(prop, "FileDataSource"));
        
        propModel.setWeChatServerMessageURL(getStr(prop, "WeChatServerMessageURL"));
        propModel.setWeChatServerURL(getStr(prop, "WeChatServerURL"));
        propModel.setQingTuiServerMessageURL(getStr(prop, "QingTuiServerMessageURL"));
        propModel.setQingTuiServerURL(getStr(prop, "QingTuiServerURL"));
        propModel.setMessageType(getStr(prop, "MessageType"));
        propModel.setImCodePrefix(getStr(prop,"ImCodePrefix"));
        
        return propModel;
    }

    private String getStr(Properties prop, String key) {
        String value = (String) prop.get(key);
        
        try {
        	if (value != null) {
            	value = value.trim();
            	return new String(value.getBytes("ISO-8859-1"), "UTF-8");
            }
        	return "";
            
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    
    private Set<String> getList(Properties prop, String key) {
    	Set<String> rstList = new HashSet<String>();
    	String rst = getStr(prop, key);
    	String[] rsts = rst.split(",");
    	for ( String item : rsts ) {
    		rstList.add(item);
    	}
    	
    	return rstList;
    }

}
