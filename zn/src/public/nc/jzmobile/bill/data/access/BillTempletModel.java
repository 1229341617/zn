package nc.jzmobile.bill.data.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillTempletModel {
	
	private List<TempletTabModel> heads = new ArrayList<TempletTabModel>();//表头信息
	private List<TempletTabModel> bodys = new ArrayList<TempletTabModel>();//表体信息
	public List<TempletTabModel> getHeads() {
		return heads;
	}
	public void setHeads(List<TempletTabModel> heads) {
		this.heads = heads;
	}
	public List<TempletTabModel> getBodys() {
		return bodys;
	}
	public void setBodys(List<TempletTabModel> bodys) {
		this.bodys = bodys;
	}
	
	
	
	

}
