package nc.vo.jzmobile.app;

import java.io.Serializable;
import java.util.Set;

public class MobilePropModel implements Serializable {
	
	private Set<String> billFileDataSource = null;
	
	
	
	private String maServerURL;
	private String maServerMessageURL;
	private String maAppId;
	private String appShowName;
	private String tenantid;
	
	private String fileDataSource;
	
	private String dataSource;
	
	private String mobileApproveURL;
	
	private String mobileApproveHtml;

	
	/**企业微信发送消息*/
	private String WeChatServerMessageURL;
	private String WeChatServerURL;
	
	/**轻推发送消息*/
	private String QingTuiServerMessageURL;
	private String QingTuiServerURL;
	
	/**消息类型*/
	private String MessageType;
	
	
	/**企业消息前缀*/
	private String ImCodePrefix;
	

	public String getQingTuiServerMessageURL() {
		return QingTuiServerMessageURL;
	}

	public void setQingTuiServerMessageURL(String qingTuiServerMessageURL) {
		QingTuiServerMessageURL = qingTuiServerMessageURL;
	}

	public String getQingTuiServerURL() {
		return QingTuiServerURL;
	}

	public void setQingTuiServerURL(String qingTuiServerURL) {
		QingTuiServerURL = qingTuiServerURL;
	}

	public String getImCodePrefix() {
		return ImCodePrefix;
	}

	public void setImCodePrefix(String imCodePrefix) {
		ImCodePrefix = imCodePrefix;
	}
	
	
	
	
	public String getTenantid() {
		return tenantid;
	}

	public void setTenantid(String tenantid) {
		this.tenantid = tenantid;
	}

	public String getWeChatServerMessageURL() {
		return WeChatServerMessageURL;
	}

	public void setWeChatServerMessageURL(String weChatServerMessageURL) {
		WeChatServerMessageURL = weChatServerMessageURL;
	}

	public String getWeChatServerURL() {
		return WeChatServerURL;
	}

	public void setWeChatServerURL(String weChatServerURL) {
		WeChatServerURL = weChatServerURL;
	}

	public String getMessageType() {
		return MessageType;
	}

	public void setMessageType(String messageType) {
		MessageType = messageType;
	}

	public String getMaAppId() {
		return maAppId;
	}

	public void setMaAppId(String maAppId) {
		this.maAppId = maAppId;
	}

	public String getAppShowName() {
		return appShowName;
	}

	public void setAppShowName(String appShowName) {
		this.appShowName = appShowName;
	}

	public Set<String> getBillFileDataSource() {
		return billFileDataSource;
	}

	public void setBillFileDataSource(Set<String> billFileDataSource) {
		this.billFileDataSource = billFileDataSource;
	}

	public String getMaServerURL() {
		return maServerURL;
	}

	public void setMaServerURL(String maServerURL) {
		this.maServerURL = maServerURL;
	}

	public String getMaServerMessageURL() {
		return maServerMessageURL;
	}

	public void setMaServerMessageURL(String maServerMessageURL) {
		this.maServerMessageURL = maServerMessageURL;
	}

	public String getFileDataSource() {
		return fileDataSource;
	}

	public void setFileDataSource(String fileDataSource) {
		this.fileDataSource = fileDataSource;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public String getMobileApproveURL() {
		return mobileApproveURL;
	}

	public void setMobileApproveURL(String mobileApproveURL) {
		this.mobileApproveURL = mobileApproveURL;
	}

	public String getMobileApproveHtml() {
		return mobileApproveHtml;
	}

	public void setMobileApproveHtml(String mobileApproveHtml) {
		this.mobileApproveHtml = mobileApproveHtml;
	}
	
	
}
