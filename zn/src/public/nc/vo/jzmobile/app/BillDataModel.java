package nc.vo.jzmobile.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BillDataModel {
	/*判断移动审批单据是否漏掉表头表体*/	

	private List<Map<String,String>> datalist = new ArrayList<Map<String,String>>();

	public List<Map<String,String>> getDatalist() {
		return datalist;
	}

	public void setDatalist(List<Map<String, String>> datalist) {
		this.datalist = datalist;
	}

	

	
	
	
}
