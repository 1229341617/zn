package nc.vo.jzmobile.app;

import java.util.List;

public class AssignableUserGroup {
	private String desc;
	private List<AssignableUserVO> assignableUserList;
	
	public AssignableUserGroup(String desc,List<AssignableUserVO> assignableUserList){
		this.desc = desc;
		this.assignableUserList = assignableUserList;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public List<AssignableUserVO> getAssignableUserList() {
		return assignableUserList;
	}
	public void setAssignableUserList(List<AssignableUserVO> assignableUserList) {
		this.assignableUserList = assignableUserList;
	}
	
}
