package nc.vo.jzmobile.template.comparator;

import java.util.Comparator;

import nc.vo.pub.bill.BillTabVO;

public class BillTabVOComparator implements Comparator<BillTabVO> {

	public int compare(BillTabVO vo1, BillTabVO vo2) {
		return vo1.getTabindex().compareTo(vo2.getTabindex());
	}

}
