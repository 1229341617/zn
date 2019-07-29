package nc.bs.jzmobile.template.strategy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.logging.Logger;
import nc.itf.jzmobile.template.strategy.IBillItemStrategy;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.jzmobile.bill.data.access.PubBillTempletModel;
import nc.md.data.access.NCObject;
import nc.ui.bd.ref.AbstractRefModel;
import nc.ui.bd.ref.RefPubUtil;
import nc.ui.pub.beans.constenum.DefaultConstEnum;
import nc.vo.pmpub.common.utils.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.bill.BillTempletBodyVO;

/**
 * 参照处理类
 * 
 * @author mazhyb
 * 
 */
public class BillItemRefStrategy implements IBillItemStrategy {
	
	
	@SuppressWarnings({ "unchecked", "serial" })
	private String getProjectName(String value){
	List<String> name = new ArrayList<String>();
		
		try {
			name = (List<String>) new BaseDAO()
					.executeQuery(
							"select project_name from bd_project where pk_project='"+value+"'",new ResultSetProcessor() {
								public Object handleResultSet(ResultSet rs)
										throws SQLException {
									while (rs.next()) {
										List<String> name = new ArrayList<String>();
										name.add(rs.getString("project_name"));
										return name;
									}
									return null;
								}
							});
		} catch (DAOException e) {
			Logger.error(e);
		}
		if(name==null||name.size()==0){
			return "";
		}
		return name.get(0);
	}
	
	@SuppressWarnings({ "unchecked", "serial" })
	private String getRpName(String value){
	List<String> name = new ArrayList<String>();
		
		try {
			name = (List<String>) new BaseDAO()
					.executeQuery(
							"select vname from jzsec_rptype where pk_rptype='"+value+"'",new ResultSetProcessor() {
								public Object handleResultSet(ResultSet rs)
										throws SQLException {
									while (rs.next()) {
										List<String> name = new ArrayList<String>();
										name.add(rs.getString("vname"));
										return name;
									}
									return null;
								}
							});
		} catch (DAOException e) {
			Logger.error(e);
		}
		if(name==null||name.size()==0){
			return "";
		}
		return name.get(0);
	}

	public Object process(String corp,String userid,BillTempletBodyVO item, Object value, NCObject ncos) {
		String key = item.getItemkey();
		ncos.getRelatedBean().getAttributeByName(key).getRefModelName();

		/**项目*/
		if("pk_project".equals(key)){
			return getProjectName(value.toString());
		}
		String refNodeName = ncos.getRelatedBean().getAttributeByName(key).getRefModelName();
		String billname = ncos.getRelatedBean().getDisplayName();
		if (refNodeName == null || null == value) {
			return value;
		}
		AbstractRefModel model = null;
		try {
			model = RefPubUtil.getRefModel(refNodeName);
			if("安全奖罚处理单".equals(billname)){
				model = RefPubUtil.getRefModel("安全奖罚激励类型");
			}
			if("质量奖罚处理单".equals(billname)){
				model = RefPubUtil.getRefModel("质量奖罚激励类型");
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		if(model==null){
			String refModelClassName = RefPubUtil.getRefModelClassName(refNodeName);
			String refModelClassChangeName = "nc.jzmobile.refmodel"
					+ refModelClassName.substring(refModelClassName
							.lastIndexOf("."));
			try {
				Object obj = Class.forName(refModelClassChangeName)
						.newInstance();
				// 通过反射为有pk_project属性的参照类进行pk_project属性赋值
//				setProjectByReflect(obj, refModelClassChangeName,
//						pk_project);

				model = (AbstractRefModel) obj;
			} catch (Exception ex) {
				
			}
				
		}
		if (model != null) {
			if( !StringUtil.isEmpty(corp) ){
				model.setPk_corp(corp);
			}
			if( !StringUtil.isEmpty(userid) ){
				model.setPk_user(userid);
			}
			
			model.matchPkData(value.toString());
			DefaultConstEnum refValue = new DefaultConstEnum(value, model
					.getRefNameValue());
			if (refValue != null) {
				// Object pk_value = refValue.getValue();
				return refValue.getName() != null ? refValue.getName() : value;
				
				//return refValue.getName();
			}
			return value;// 未转换成功
		}
		// 为自定义参照
		model = getModel(refNodeName);
		if (model == null)
			return value;// 转换失败
		if( !StringUtil.isEmpty(corp) ){
			model.setPk_corp(corp);
		}
		if( !StringUtil.isEmpty(userid) ){
			model.setPk_user(userid);
		}
		Object obj = model.getValue(value.toString());
		if (obj == null)
			return value;// 转换失败
		return value;
	}

	/**
	 * 获取自定义参照
	 * 
	 * @param refNodeName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private AbstractRefModel getModel(String refNodeName) {
		if (refNodeName == null)
			return null;
		refNodeName = refNodeName.substring(refNodeName.indexOf("<") + 1,
				refNodeName.indexOf(">"));
		Class refModel = null;
		try {
			refModel = Class.forName(refNodeName);
			return (AbstractRefModel) refModel.newInstance();
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			return null;
		}

	}

}
