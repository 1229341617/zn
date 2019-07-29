package nc.bs.jzmobile.template.strategy;

import nc.itf.jzmobile.template.strategy.IBillItemStrategy;
import nc.md.data.access.NCObject;
import nc.md.model.type.impl.EnumType;
import nc.vo.pub.bill.BillTempletBodyVO;

/**
 * 下拉的处理
 * 
 * @author mazhyb
 *
 */
public class BillItemComboStrategy implements IBillItemStrategy{

	public Object process(String corp,String userid,BillTempletBodyVO item, Object value, NCObject ncos){
		if( value == null )return null;
		
		String key = item.getItemkey();
		EnumType type = (EnumType) ncos.getRelatedBean().getAttributeByName(key).getDataType();
		return type.getConstEnum(value).getName();
	}

}
