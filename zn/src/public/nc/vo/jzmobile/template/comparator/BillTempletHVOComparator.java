package nc.vo.jzmobile.template.comparator;

import java.util.Comparator;

import nc.jzmobile.bill.data.access.TempletModel;

public class BillTempletHVOComparator implements Comparator<TempletModel> {

	@Override
	public int compare(TempletModel o1, TempletModel o2) {
		// TODO Auto-generated method stub
		return o1.getShoworder().compareTo(o2.getShoworder());
	}

	

}
