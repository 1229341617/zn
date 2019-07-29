package nc.vo.jzmobile.template;

import nc.vo.pub.lang.UFBoolean;

public class MobileTemplateBVO extends MobileSuperVO{
	
	private static final long serialVersionUID = 1L;
	private String pk_mobiletemplate;
	private String itemkey;
	private String itemshowname;
	private String datatype;
	private String refdoc;
	private UFBoolean isreturncode;
	private Integer showorder;
	private UFBoolean isenable;
	private String formula;
	private String ts;
	private int pos;
	private String billtype;
	private int dr=0;
	private UFBoolean isdigest;
	private String reftype;
	
	
	
	@Override
	public String getPKFieldName() {
		
		return "pk_mobiletemplate";
	}

	@Override
	public String getParentPKFieldName() {
	
		return "billtype";
	}

	@Override
	public String getTableName() {
		
		return "pub_mobiletemplate";
	}


	public int getDr() {
		return dr;
	}

	public void setDr(int dr) {
		this.dr = dr;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	

	public String getPk_mobiletemplate() {
		return pk_mobiletemplate;
	}

	public void setPk_mobiletemplate(String pk_mobiletemplate) {
		this.pk_mobiletemplate = pk_mobiletemplate;
	}

	
	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public String getItemkey() {
		return itemkey;
	}

	public void setItemkey(String itemkey) {
		this.itemkey = itemkey;
	}

	public String getItemshowname() {
		return itemshowname;
	}

	public void setItemshowname(String itemshowname) {
		this.itemshowname = itemshowname;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public String getRefdoc() {
		return refdoc;
	}

	public void setRefdoc(String refdoc) {
		this.refdoc = refdoc;
	}

	public UFBoolean getIsreturncode() {
		return isreturncode;
	}

	public void setIsreturncode(UFBoolean isreturncode) {
		this.isreturncode = isreturncode;
	}

	public Integer getShoworder() {
		return showorder;
	}

	public void setShoworder(Integer showorder) {
		this.showorder = showorder;
	}

	public UFBoolean getIsenable() {
		return isenable;
	}

	public void setIsenable(UFBoolean isenable) {
		this.isenable = isenable;
	}

	public String getBilltype() {
		return billtype;
	}

	public void setBilltype(String billtype) {
		this.billtype = billtype;
	}

	public UFBoolean getIsdigest() {
		return isdigest;
	}

	public void setIsdigest(UFBoolean isdigest) {
		this.isdigest = isdigest;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setReftype(String reftype) {
		this.reftype = reftype;
	}

	public String getReftype() {
		return reftype;
	}


}
