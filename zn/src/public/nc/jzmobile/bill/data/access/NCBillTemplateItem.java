package nc.jzmobile.bill.data.access;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.logging.Logger;
import nc.md.data.access.NCObject;
import nc.md.model.impl.EnumValue;
import nc.md.model.type.impl.EnumType;
import nc.md.model.type.impl.RefType;
import nc.ui.bd.ref.AbstractRefModel;
import nc.ui.bd.ref.RefPubUtil;
import nc.ui.pub.beans.constenum.IConstEnum;
import nc.vo.pub.bill.BillTempletBodyVO;

/**
 * 查询【单据模板】 的详细信息
 * 
 * @author lixyw
 * 
 */

public class NCBillTemplateItem {
	
	public PubBillTempletBModel process(String corp, String userid,
			BillTempletBodyVO billitem,  NCObject ncos) {
		String key = billitem.getItemkey();
//		MobileTabDataVO columnVO = new MobileTabDataVO();
		PubBillTempletBModel btb = new PubBillTempletBModel();
		
		btb.setShowFlag(billitem.getShowflag().booleanValue());
		btb.setDataType(billitem.getDatatype());
		btb.setItemKey(billitem.getItemkey());
		String showname = billitem.getDefaultshowname();
		if (showname == null || "".equals(showname)) {
			if(ncos.getRelatedBean().getAttributeByName(key)!=null)
				showname = ncos.getRelatedBean().getAttributeByName(key).getDisplayName();
		}
		btb.setDefaultShowName(showname);
		btb.setDefaultValue(billitem.getDefaultvalue());
		btb.setInputLength(billitem.getInputlength());
		btb.setItemType(billitem.getItemtype());
		btb.setListShowFlag(billitem.getListshowflag());
		btb.setNullFlag(billitem.getNullflag());
		btb.setOptions(billitem.getOptions());
		btb.setPos(billitem.getPos());
		btb.setShowFlag(billitem.getShowflag());
		btb.setTableCode(billitem.getTable_code());
		btb.setTableName(billitem.getTableName());

		try{
			if (ncos.getRelatedBean().getAttributeByName(key).getDataType() instanceof EnumType) {
//				type = IBillItem.COMBO;
				EnumType type = (EnumType) ncos.getRelatedBean().getAttributeByName(key).getDataType();
				
				List valueList = type.getEnumValues();
				Map<String, String> enumType = new HashMap<String, String>();
				for(int i=0;i<valueList.size();i++){
					EnumValue ev = (EnumValue) valueList.get(i);
					enumType.put(ev.getValue(), ev.getName());
				}
				btb.setComboValues(enumType);
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			Logger.error(e);
		}
		
		return btb;
	}
	
	public String getRefModeName(String itemKey,  NCObject ncos){
		if (ncos.getRelatedBean().getAttributeByName(itemKey).getDataType() instanceof RefType) {
			String refNodeName = ncos.getRelatedBean().getAttributeByName(itemKey).getRefModelName();
			if (refNodeName == null) {
				//开票申请 红票页签 中的 对应蓝字发票字段的特殊处理
				if("nc.vo.jzinv.jzinv0505.InvAppHVO".equals(ncos.getRelatedBean().getFullClassName())&&itemKey.equals("pk_blue"))
					return "nc.ui.jzinv.jzinv0510.ref.InvOpenRefModel";
				else
				    return null;
			}
			AbstractRefModel model = null;
			try {
				return RefPubUtil.getRefModelClassName(refNodeName);
			} catch (Exception e) {
				Logger.error(e);
			}
			return null;
		}
		return null;
	}
}
