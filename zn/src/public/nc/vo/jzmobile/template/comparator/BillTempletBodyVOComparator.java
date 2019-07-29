package nc.vo.jzmobile.template.comparator;

import java.util.Comparator;

import nc.vo.pub.bill.BillTempletBodyVO;

public class BillTempletBodyVOComparator implements Comparator<BillTempletBodyVO> {

	public int compare(BillTempletBodyVO vo1, BillTempletBodyVO vo2) {
		return vo1.getShoworder().compareTo(vo2.getShoworder());
	}

}
