package nc.vo.jzmobile.app;

import nc.cmp.tools.StringUtil;

public class BillTypeModel {
	private String billType = null;
	
	private String transToType=null;
	
	private String title=null;
	
	private String isWorkFlow; /**�ж��Ƿ���������*/
	
	private String isImage; /**�����Ƿ���Ӱ��*/
	
	
	/*private Map<String, String> billTypeMapping = new HashMap<String, String>();
	
	private List<String> muiltbodyVoList = new ArrayList<String>();*/
	
	public String getTitle() {
		return title;
	}

	public String getIsImage() {
		return isImage;
	}

	public void setIsImage(String isImage) {
		this.isImage = isImage;
	}

	public String getIsWorkFlow() {
		return isWorkFlow;
	}

	public void setIsWorkFlow(String isWorkFlow) {
		this.isWorkFlow = isWorkFlow;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBillType() {
		return billType;
	}

	public void setBillType(String billType) {
		this.billType = billType;
	}

	public String getTransToType() {
		return transToType;
	}

	public void setTransToType(String transToType) {
		this.transToType = transToType;
	}
	
	public String getBillTypeCode(){
		if(!StringUtil.isEmpty(transToType))
			return transToType;
		return billType;
	}

//	public Map<String, String> getBillTypeMapping() {
//		return billTypeMapping;
//	}
//
//	public void setBillTypeMapping(Map<String, String> billTypeMapping) {
//		this.billTypeMapping = billTypeMapping;
//	}
//
//	public List<String> getMuiltbodyVoList() {
//		return muiltbodyVoList;
//	}
//
//	public void setMuiltbodyVoList(List<String> muiltbodyVoList) {
//		this.muiltbodyVoList = muiltbodyVoList;
//	}
}
