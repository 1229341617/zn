package nc.itf.jzmobile.template.strategy;

import nc.md.data.access.NCObject;
import nc.vo.pub.bill.BillTempletBodyVO;

public interface IBillItemStrategy {

	public Object process(String corp,String userid,BillTempletBodyVO billitem, Object value, NCObject ncos);
	
}
