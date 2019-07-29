package nc.vo.pf.mobileapp;


/**
 * 任务实体
 * 可能来自我提交的任务（流程实例），也可能来自我接收到的任务（工作项）
 * @author yanke1
 *
 */
public class TaskMetaData {
	
	private String billType;
	private String billId;
	private String billNo;
	
	private String pk_wf_instance;
	private String pk_checkflow;
	private String cuserid;
	
	private String title;
	private String startDate;
	
	public String getSendmanid() {
		return sendmanid;
	}
	public void setSendmanid(String sendmanid) {
		this.sendmanid = sendmanid;
	}
	private String sendmanid;
	
	
	public String getBillType() {
		return billType;
	}
	public void setBillType(String billType) {
		this.billType = billType;
	}
	public String getBillId() {
		return billId;
	}
	public void setBillId(String billId) {
		this.billId = billId;
	}
	public String getBillNo() {
		return billNo;
	}
	public void setBillNo(String billNo) {
		this.billNo = billNo;
	}
	public String getPk_wf_instance() {
		return pk_wf_instance;
	}
	public void setPk_wf_instance(String pk_wf_instance) {
		this.pk_wf_instance = pk_wf_instance;
	}
	public String getPk_checkflow() {
		return pk_checkflow;
	}
	public void setPk_checkflow(String pk_checkflow) {
		this.pk_checkflow = pk_checkflow;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getCuserid() {
		return cuserid;
	}
	public void setCuserid(String cuserid) {
		this.cuserid = cuserid;
	}

}
