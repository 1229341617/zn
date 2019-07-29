package nc.vo.jzmobile.app;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author wanghui
 * @TODO 如下示例
 *  "tabContent": [
                    {
                        "tabTitle": "人员入职审批",
                        "code": "1",
                        "tabdata": [
                            {
                                "colname": "申请人",
                                "colvalue": "张琨",
                                "colkey": "pk_proposer"
                            },
                            {
                                "colname": "申请日期",
                                "colvalue": "2016-05-10",
                                "colkey": "applydate"
                            }
                        ]
                    },
                    {
                        "tabTitle": "申请日期",
                        "tabdata": [
                            {
                                "colname": "申请日期",
                                "colvalue": "2016-05-10",
                                "colkey": "applydate"
                            }
                        ]
                    }
                ]
 */

public class MobileTabContentVO implements Serializable{
  
	private String tabTitle;
	
	private String datacount;
	
	private String code;
	
	private List<List<MobileTabDataVO>> tabdata;
	
	private int pos;
	
	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public String getTabTitle() {
		return tabTitle;
	}

	public void setTabTitle(String tabTitle) {
		this.tabTitle = tabTitle;
	}

	public String getDatacount() {
		return datacount;
	}

	public void setDatacount(String datacount) {
		this.datacount = datacount;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<List<MobileTabDataVO>> getTabdata() {
		return tabdata;
	}

	public void setTabdata(List<List<MobileTabDataVO>> tabdata) {
		this.tabdata = tabdata;
	}

}
