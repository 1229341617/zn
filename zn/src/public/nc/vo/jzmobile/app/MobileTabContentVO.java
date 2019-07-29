package nc.vo.jzmobile.app;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author wanghui
 * @TODO ����ʾ��
 *  "tabContent": [
                    {
                        "tabTitle": "��Ա��ְ����",
                        "code": "1",
                        "tabdata": [
                            {
                                "colname": "������",
                                "colvalue": "����",
                                "colkey": "pk_proposer"
                            },
                            {
                                "colname": "��������",
                                "colvalue": "2016-05-10",
                                "colkey": "applydate"
                            }
                        ]
                    },
                    {
                        "tabTitle": "��������",
                        "tabdata": [
                            {
                                "colname": "��������",
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
