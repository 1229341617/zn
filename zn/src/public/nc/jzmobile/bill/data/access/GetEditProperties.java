package nc.jzmobile.bill.data.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import nc.bs.framework.common.NCLocator;
import nc.itf.uap.pf.IWorkflowService;
import nc.md.model.AssociationPoint;
import nc.md.model.IAttribute;
import nc.md.model.IBusinessEntity;
import nc.md.model.IEnumValue;
import nc.md.model.impl.Association;
import nc.md.model.impl.BusinessEntity;
import nc.md.model.impl.EnumValue;
import nc.md.model.impl.Attribute;
import nc.md.model.type.IType;
import nc.md.model.type.impl.EnumType;
import nc.md.util.MDUtil;
import nc.pub.fa.common.util.StringUtils;
import nc.uap.pf.metadata.PfMetadataTools;
import nc.vo.jzmobile.app.MobileTabDataVO;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pub.BusinessException;

/*
 * 
 * 获取可编辑属性
 */
public class GetEditProperties {
	
	public static List<String> getEditProperties(String userid,String billid,String billtype) {
		
		
		try{
			IWorkflowService workflowservice = NCLocator.getInstance().lookup(IWorkflowService.class);
			List<String> editProperties = workflowservice.getEditablePreoperties(userid, billtype,billid, 2);
			return editProperties;
		}catch(Exception e){
			
		}
		
		return null;
		
	}
	
	/**
	 *      0:字符
            1:整数
            2:小数
            3:日期
            4:逻辑
            5:参照
            6:下拉
            7:自定义项档案
            8:时间
            9:大文本
            10:图片
            11:对象
            12:占位块
            13:密码框
            14:email 报表专用
	 * @param type
	 * @return
	 */
	public static int propertiesType(IAttribute attr){
		
		IType type = attr.getDataType();
		
		//参照
		if(MDUtil.isRefType(type)){
			
			return 5;
			
		//枚举
		}else if(MDUtil.isEnumType(type)){
			
			return 6;
			
	    //日期
		}else if(MDUtil.isDate(type)){
			
			return 8;
			//布尔
		}else if(type.getTypeType() == 32){
			
			return 4;
			
		}else{
			
			
			//字符串类型
			return -1;
			
		}
		
		
	}
	
	
	public static MobileTabDataVO getBody(String billtype,String properties,MobileTabDataVO column,String tabCode){
		
		IBusinessEntity be;
		try {
			be = PfMetadataTools.queryMetaOfBilltype(billtype);
			
			List<IAttribute> alAttr = be.getAttributes();
			
			
			IAttribute att  = null;
			for(IAttribute attr:alAttr){
				
				if(attr.getName().equals(tabCode)){
					att = attr;
					
					break;
				}
			}
			
			List<IAttribute> bodys = getChildAttributes(att);
			for(IAttribute body:bodys){
				
				if(properties.equals(body.getName())){
					att = body;
					
					break;
				}
				
			}
			
			int type = propertiesType(att);
			
			if(6 == type){
				
				Map<String,String> map = getEnumValue(att);
				column.setValues(map);
			}
			
			column.setType(type);
			return column;
			
		} catch (BusinessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return column;
		
	}
	
	
	private static Map<String,String> getEnumValue(IAttribute attr){
		
		Map<String,String> map = new HashMap<String,String>();
		
		//如果是枚举类型，带出返回值w
		EnumType e = (EnumType) attr.getDataType();
		List<IEnumValue> values = e.getEnumValues();
		for(IEnumValue value:values){
			EnumValue enumvalue = (EnumValue) value;
			map.put(enumvalue.getValue(),enumvalue.getName());
		}
		
		return map;
	}
	
	
	
	public static MobileTabDataVO getHead(String billtype,String properties,MobileTabDataVO column){
		IBusinessEntity be;
		try {
			be = PfMetadataTools.queryMetaOfBilltype(billtype);
			List<IAttribute> alAttr = be.getAttributes();
			
			IAttribute att  = null;
			for(IAttribute attr:alAttr){
						
				if(properties.equals(attr.getName())){		
					att = attr;	
					break;	
				}	
			}
			int type = propertiesType(att);
			if(6 == type){
				
				Map<String,String> map = getEnumValue(att);
				column.setValues(map);
			}
			
			column.setType(type);
			return column;
			
		} catch (BusinessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return column;
	}
	
	//获取子表属性
	private static List<IAttribute> getChildAttributes(IAttribute child){
		
		List<IAttribute> attributes = new ArrayList<IAttribute>();
		
		Attribute attr = (Attribute)child;
		
		Association ass = (Association)attr.getAssociation();
		
		AssociationPoint point = ass.getEndElement();
		
		BusinessEntity element = (BusinessEntity)point.getAssElement();
		
		attributes = element.getAttributes();
		
		return attributes;
		
	}
	
	
	
	
	

}
