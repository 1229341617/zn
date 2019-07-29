package nc.vo.jzmobile.template;


public class MobileTemplateVO extends MobileSuperVO{
	
	private static final long serialVersionUID = 1L;
	
	private String billtype;
	private String billtypename;
	private String vostyle;
	private String billpattern;
	private String ts;
	private String transformclass;
	private int dr=0;
	private MobileTemplateBVO[] children;
	private String handleExceptionClass;
	private String approveTransferClass;
	

	public MobileTemplateBVO[] getChildren() {
		return children;
	}

	public void setChildren(MobileTemplateBVO[] children) {
		this.children = children;
	}

	@Override
	public String getPKFieldName() {
		
		return "billtype";
	}

	@Override
	public String getParentPKFieldName() {
	
		return null;
	}

	@Override
	public String getTableName() {
		
		return null;
	}

	

	public int getDr() {
		return dr;
	}

	public void setDr(int dr) {
		this.dr = dr;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public void setBilltypename(String billtypename) {
		this.billtypename = billtypename;
	}

	public String getBilltypename() {
		return billtypename;
	}

	public void setBilltype(String billtype) {
		this.billtype = billtype;
	}

	public String getBilltype() {
		return billtype;
	}

	public void setVostyle(String vostyle) {
		this.vostyle = vostyle;
	}

	public String getVostyle() {
		return vostyle;
	}

	public void setBillpattern(String billpattern) {
		this.billpattern = billpattern;
	}

	public String getBillpattern() {
		return billpattern;
	}

	public void setTransformclass(String transformclass) {
		this.transformclass = transformclass;
	}

	public String getTransformclass() {
		return transformclass;
	}

	public void setHandleExceptionClass(String handleExceptionClass) {
		this.handleExceptionClass = handleExceptionClass;
	}

	public String getHandleExceptionClass() {
		return handleExceptionClass;
	}

	public void setApproveTransferClass(String approveTransferClass) {
		this.approveTransferClass = approveTransferClass;
	}

	public String getApproveTransferClass() {
		return approveTransferClass;
	}

	

}
