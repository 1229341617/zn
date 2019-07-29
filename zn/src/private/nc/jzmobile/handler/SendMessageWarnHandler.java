package nc.jzmobile.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
 * 催办：在我提交的里面给审批人发送审批消息
 * 
 * @author wss
 * 
 */
public class SendMessageWarnHandler implements INCMobileServletHandler {
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		Logger.info("==========SendMessageWarnHandler start==========");
		String taskid = map.get("taskid");
		String[] taskids = taskid.split(",");
		Logger.info("taskid:" + taskid);
		try {
			ITaskType taskType = MobileAppUtils.getTaskType("submit", map.get("statuscode"));
			TaskQuery query = taskType.createNewTaskQuery();
			for(String t : taskids){
				TaskMetaData tmd = query.queryTaskMetaData(t);
				List<String> list = queryCheckmanAndCheckflow(tmd);
				MobilePropModel propModel = new MobilePropertiesLoader().getViewResolverProperties();
				for (String s : list) {
					String[] mess = s.split(",");
					StringBuffer messageUrl = new StringBuffer(propModel.getMobileApproveURL());
					messageUrl.append("?appid="+propModel.getMaAppId());
					messageUrl.append("&statuskey=submit");
					messageUrl.append("&statuscode="+map.get("statuscode"));
					messageUrl.append("&taskid="+mess[1]);
					messageUrl.append(propModel.getMobileApproveHtml());
					UserVO user = new UserVO();
					user.setCuserid(mess[0]);
					user.setUser_code(MobileAppUtils.getUserCodeById(mess[0]));
					String message = "审批提醒："+Pfi18nTools.getUserName(mess[2])+"提醒审批单据，单据号："+tmd.getBillNo()+"，请审批。";
					MobileMessageUtil.sendMobileMessage(MessageVO.getMessageNoteAfterI18N(message), user, "0", messageUrl.toString());
				}
			}
			
		}catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage("发送消息报错，错误详情参考NC端日志："+e.getMessage());
		}
		Logger.info("==========SendMessageWarnHandler end==========");
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
