package nc.vo.jzmobile.app;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class MobileBillData implements Serializable {
	private static final long serialVersionUID = 1L;

	private String filecount;

	private String billtypename;
    
	private String id;//主键
	
	private String ts;

	private Map<String, List<MobileTabContentVO>> data;
	
//	private boolean canReject;//驳回
//	private boolean canTransfer;//指派
//	private boolean canAddApprover;//加签
//	
//	private List<ActivityVo> activityList;
//	
//	private List<UserVO> userList;
//	
//	public List<UserVO> getUserList() {
//		return userList;
//	}
//
//	public void setUserList(List<UserVO> userList) {
//		this.userList = userList;
//	}
//
//	public List<ActivityVo> getActivityList() {
//		return activityList;
//	}
//
//	public void setActivityList(List<ActivityVo> activityList) {
//		this.activityList = activityList;
//	}
//
//	public boolean isCanReject() {
//		return canReject;
//	}
//
//	public void setCanReject(boolean canReject) {
//		this.canReject = canReject;
//	}
//
//	public boolean isCanTransfer() {
//		return canTransfer;
//	}
//
//	public void setCanTransfer(boolean canTransfer) {
//		this.canTransfer = canTransfer;
//	}
//
//	public boolean isCanAddApprover() {
//		return canAddApprover;
//	}
//
//	public void setCanAddApprover(boolean canAddApprover) {
//		this.canAddApprover = canAddApprover;
//	}

	public String getFilecount() {
		return filecount;
	}

	public void setFilecount(String filecount) {
		this.filecount = filecount;
	}

	public String getBilltypename() {
		return billtypename;
	}

	public void setBilltypename(String billtypename) {
		this.billtypename = billtypename;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public Map<String, List<MobileTabContentVO>> getData() {
		return data;
	}

	public void setData(Map<String, List<MobileTabContentVO>> data) {
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
