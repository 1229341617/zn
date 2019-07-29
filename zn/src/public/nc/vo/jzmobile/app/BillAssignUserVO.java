package nc.vo.jzmobile.app;

public class BillAssignUserVO {

	private String userid;
	private String usertype;
	private String username;
	private String actid;
	
	
	public BillAssignUserVO() {
		super();
	}
	public BillAssignUserVO(String userid, String usertype, String username,
			String actid) {
		super();
		this.userid = userid;
		this.usertype = usertype;
		this.username = username;
		this.actid = actid;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getUsertype() {
		return usertype;
	}
	public void setUsertype(String usertype) {
		this.usertype = usertype;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getActid() {
		return actid;
	}
	public void setActid(String actid) {
		this.actid = actid;
	}
	
}
