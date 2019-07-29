package nc.jzmobile.bill.data.access;

import java.util.List;

public class TempletTabModel {
	
	private String tabCode;
	private String tabName;
	private String tabIndex;
	private List<TempletModel> model;
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
	public String getTabIndex() {
		return tabIndex;
	}
	public void setTabIndex(String tabIndex) {
		this.tabIndex = tabIndex;
	}
	public List<TempletModel> getModel() {
		return model;
	}
	public void setModel(List<TempletModel> model) {
		this.model = model;
	}
	
	
	
	

}
