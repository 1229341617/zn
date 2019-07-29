package nc.jzmobile.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.logging.Logger;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.jzmobile.utils.MobileMessageUtil;
import nc.jzmobile.utils.MobilePropertiesLoader;
import nc.jzmobile.utils.TitleDefUtil;
import nc.vo.jzmobile.app.MobilePropModel;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pf.mobileapp.ITaskType;
import nc.vo.pf.mobileapp.MobileAppUtils;
import nc.vo.pf.mobileapp.TaskMetaData;
import nc.vo.pf.mobileapp.query.TaskQuery;
import nc.vo.pub.msg.MessageVO;
import nc.vo.pub.pf.Pfi18nTools;
import nc.vo.sm.UserVO;

import org.apache.commons.lang.StringUtils;

/**
 * 获取NC设置的默认审批批语,根据当前登录用户id
 * 
 * @author liuyhza
 * 
 */
public class GetApproveOptHandler implements INCMobileServletHandler {
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		Logger.info("==========GetApproveOptHandler start==========");
		String userid = map.get("userid");
		String notetype = map.get("condition");
		Logger.info("userid:" + userid);
		Logger.info("notetype:" + notetype);
		List<Map<String,String>> rs = new ArrayList<>();
		try {
			BaseDAO dao = new BaseDAO();
			String sql = "select isdefault , note from pub_wf_checknote where " +
			" pk_user = '"+userid+"' and notetype = '" +notetype+ "'";
			
			rs = (List<Map<String,String>>) dao.executeQuery(sql.toString(), new ResultSetProcessor() {

				@Override
				public Object handleResultSet(ResultSet rs) throws SQLException {
					List<Map<String,String>> pks = new ArrayList<>();
					while (rs.next()) {
						Map<String, String> map = new HashMap<>();
						map.put("isdefault", rs.getString(1));
						map.put("note", rs.getString(2));
						pks.add(map);
					}
					return pks;
				}
			});
		}catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage("获取NC设置的默认审批批语报错，错误详情参考NC端日志："+e.getMessage());
		}
		result.addAttr("data", rs);
		Logger.info("==========GetApproveOptHandler end==========");
		return result;
	}


	/**
	 * 查询需要审批的人和流
	 * @throws Exception 
	 */
	private List<String> queryCheckmanAndCheckflow(TaskMetaData tmd)
			throws Exception {
		String billType = tmd.getBillType();
		//或者单据的表
		BaseDAO dao = new BaseDAO();
		String parentBillType = getBillType(billType, dao);
		if(StringUtils.isNotEmpty(parentBillType)){
			billType = parentBillType;
		}
		String tableName = TitleDefUtil.getTablename(billType);
		String keyName = TitleDefUtil.getPrimKey(billType);
		List<String> list  = new ArrayList<String>();
		String sql = "select p.checkman,p.PK_CHECKFLOW ," +
						 " (select creator from "+ tableName +
						 " where "+keyName+" = p.billid)" +
				         " from pub_workflownote p where p.billId =　'"  
		                 +tmd.getBillId()+ 
		                 "' AND p.actiontype LIKE 'Z%' " +
						 " AND p.workflow_type IN (2, 3, 6) " +
						 " AND p.approvestatus = 0";
		list = (List<String>) dao.executeQuery(sql, new ResultSetProcessor() {

			@Override
			public Object handleResultSet(ResultSet rs) throws SQLException {
				List<String> pks = new ArrayList<String>();
				while (rs.next()) {
					String pks2 = new String();
					for (int i = 1; i < 4; i++) {
						pks2 = pks2 + rs.getString(i)+",";
					}
					pks.add(pks2.substring(0, pks2.length()-1));
				}
				return pks;
			}
		});
		return list;
	}
	
	
	private String getBillType(String billType, BaseDAO dao)
			throws DAOException {
		String parentBillType = "";
		String billTypeSql = "select parentbilltype from bd_billtype where" +
				" pk_billtypecode = '"+billType+"'";
		parentBillType = (String) dao.executeQuery(billTypeSql, new ResultSetProcessor() {

			@Override
			public Object handleResultSet(ResultSet rs) throws SQLException {
				while (rs.next()) {
					return rs.getString(1);
				}
				return null;
			}
		});
		return parentBillType;
	}
}
