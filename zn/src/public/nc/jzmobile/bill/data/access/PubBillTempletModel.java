package nc.jzmobile.bill.data.access;

import java.util.List;


public class PubBillTempletModel{
	private String billTempletId;
	private String billTempletCaption;
	private String billTempletName;
	private String billTypeCode;
	private List<PubBillTempletTModel> billTempletTList;
	private List<PubBillTempletBModel> billTempletBList;
	
	private String orgId;

	public String getBillTempletId() {
		return billTempletId;
	}
	public void setBillTempletId(String billTempletId) {
		this.billTempletId = billTempletId;
	}
	public List<PubBillTempletBModel> getBillTempletBList() {
		return billTempletBList;
	}
	public void setBillTempletBList(List<PubBillTempletBModel> billTempletBList) {
		this.billTempletBList = billTempletBList;
	}
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	public List<PubBillTempletTModel> getBillTempletTList() {
		return billTempletTList;
	}
	public void setBillTempletTList(List<PubBillTempletTModel> billTempletTList) {
		this.billTempletTList = billTempletTList;
	}
	public String getBillTempletCaption() {
		return billTempletCaption;
	}
	public void setBillTempletCaption(String billTempletCaption) {
		this.billTempletCaption = billTempletCaption;
	}
	public String getBillTempletName() {
		return billTempletName;
	}
	public void setBillTempletName(String billTempletName) {
		this.billTempletName = billTempletName;
	}
	public String getBillTypeCode() {
		return billTypeCode;
	}
	public void setBillTypeCode(String billTypeCode) {
		this.billTypeCode = billTypeCode;
	}
}
