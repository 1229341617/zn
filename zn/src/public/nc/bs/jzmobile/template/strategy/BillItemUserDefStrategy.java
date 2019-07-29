package nc.bs.jzmobile.template.strategy;

import nc.itf.jzmobile.template.strategy.IBillItemStrategy;
import nc.md.data.access.NCObject;
import nc.ui.bd.ref.DefaultRefModel;
import nc.ui.pub.beans.constenum.DefaultConstEnum;
import nc.vo.pmpub.common.utils.StringUtil;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.bill.BillTempletBodyVO;

/**
 * 自定义档案的处理
 * 
 * @author mazhyb
 *
 */
public class BillItemUserDefStrategy implements IBillItemStrategy{

	public Object process(String corp,String userid,BillTempletBodyVO item, Object value, NCObject ncos) {
		String key = item.getItemkey();
		DefaultRefModel model = new DefaultRefModel();
		if( !StringUtil.isEmpty(corp) ){
			model.setPk_corp(corp);
		}
		if( !StringUtil.isEmpty(userid) ){
			model.setPk_user(userid);
		}
		model.matchPkData(value.toString());
		DefaultConstEnum refValue = new DefaultConstEnum(value, model.getRefNameValue());
		if(refValue!=null){
			//Object pk_value = refValue.getValue();
			return refValue.getName();
		}
		return value;// 未转换成功
	}

}
