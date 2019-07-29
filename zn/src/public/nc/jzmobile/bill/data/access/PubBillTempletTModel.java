package nc.jzmobile.bill.data.access;


/**
 * 模版页签
 * **/
public class PubBillTempletTModel{
	
	private String baseTab;
	private String billTempletId;
	private Integer pos;
	private String tabCode;
	private Integer tabIndex;
	private String tabName;
	
	public String getBaseTab() {
		return baseTab;
	}
	public void setBaseTab(String baseTab) {
		this.baseTab = baseTab;
	}
	
	public String getBillTempletId() {
		return billTempletId;
	}
	public void setBillTempletId(String billTempletId) {
		this.billTempletId = billTempletId;
	}
	public Integer getPos() {
		return pos;
	}
	public void setPos(Integer pos) {
		this.pos = pos;
	}
	public String getTabCode() {
		return tabCode;
	}
	public void setTabCode(String tabCode) {
		this.tabCode = tabCode;
	}
	public String getTabName() {
		return tabName;
	}
	public void setTabName(String tabName) {
		this.tabName = tabName;
	}
	public Integer getTabIndex() {
		return tabIndex;
	}
	public void setTabIndex(Integer tabIndex) {
		this.tabIndex = tabIndex;
	}
	
	
}
