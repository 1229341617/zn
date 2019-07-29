package nc.jzmobile.bill.data.access;

import java.util.List;
import java.util.Map;

public class TempletModel {
	
	private String itemkey;//×Ö¶ÎÖ÷¼ü
	private String itemName;
	private Integer dataTye;
	private Boolean isShow;
	private Boolean isEdit;
	private Boolean isRequired;
	private Integer showorder;
	
	private List<EnumModel> values;
	
	
	
	
	
	
	public List<EnumModel> getValues() {
		return values;
	}
	public void setValues(List<EnumModel> values) {
		this.values = values;
	}
	public Integer getShoworder() {
		return showorder;
	}
	public void setShoworder(Integer showorder) {
		this.showorder = showorder;
	}
	public Integer getDataTye() {
		return dataTye;
	}
	public void setDataTye(Integer dataTye) {
		this.dataTye = dataTye;
	}
	public String getItemkey() {
		return itemkey;
	}
	public void setItemkey(String itemkey) {
		this.itemkey = itemkey;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public Boolean getIsShow() {
		return isShow;
	}
	public void setIsShow(Boolean isShow) {
		this.isShow = isShow;
	}
	public Boolean getIsEdit() {
		return isEdit;
	}
	public void setIsEdit(Boolean isEdit) {
		this.isEdit = isEdit;
	}
	public Boolean getIsRequired() {
		return isRequired;
	}
	public void setIsRequired(Boolean isRequired) {
		this.isRequired = isRequired;
	}
	
	
}
