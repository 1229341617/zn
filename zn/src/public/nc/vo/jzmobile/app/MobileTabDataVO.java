package nc.vo.jzmobile.app;

import java.io.Serializable;
import java.util.Map;

/**
 * 
 * @author wanghui
 * @TODO  如下示例
  "colname": "申请日期",
  "colvalue": "2016-05-10",
  "colkey": "applydate"
 * 
 */
public class MobileTabDataVO implements Serializable{

	private String colkey;
	
	private String colname;
	
	private Object colvalue;
	
	private Object colPkvalue;
	
	private String isEdit; //是否可编辑
	
	private int type;//数据类型
	
	
	private Map<String,String> values;
	
	
	
	
	
	
	public Map<String, String> getValues() {
		return values;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getIsEdit() {
		return isEdit;
	}

	public void setIsEdit(String isEdit) {
		this.isEdit = isEdit;
	}

	public Object getColPkvalue() {
		return colPkvalue;
	}

	public void setColPkvalue(Object colPkvalue) {
		this.colPkvalue = colPkvalue;
	}

	public String getColkey() {
		return colkey;
	}

	public void setColkey(String colkey) {
		this.colkey = colkey;
	}

	public String getColname() {
		return colname;
	}

	public void setColname(String colname) {
		this.colname = colname;
	}

	public Object getColvalue() {
		return colvalue;
	}

	public void setColvalue(Object colvalue) {
		this.colvalue = colvalue;
	}

	
}
