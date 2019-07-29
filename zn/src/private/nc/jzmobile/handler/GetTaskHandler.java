package nc.jzmobile.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.uap.pf.IPFMobileAppService;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.vo.jzmobile.app.MobileBillData;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;

public class GetTaskHandler implements INCMobileServletHandler {
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		try {
			IPFMobileAppService service = NCLocator.getInstance().lookup(
					IPFMobileAppService.class);
			String userid = map.get("userid");
			String taskid = map.get("taskid");
			
			//taskid = getPk_checkFlow(userid, taskid, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			
			InvocationInfoProxy.getInstance().setGroupId(JZMobileAppUtils.getPkGroupByUserId(userid));
			
			Map<String, Object> dataMap = service.getTaskBill("", "", taskid, "ishandled", "unhandled");
			result.setData(Arrays.asList(dataMap.get("taskbill")));
		} catch (Exception e) {
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}

		return result;
	}
	
	private String getPk_checkFlow(String userid,String billid,String date) throws DAOException{
		StringBuffer buffer= new StringBuffer();
		buffer.append("select pk_checkflow from pub_workflownote where actiontype like 'Z%' ");
		buffer.append(" and checkman = '"+userid+"'");
		buffer.append(" and workflow_type in (2, 3, 4, 5, 6)");
		buffer.append(" and approvestatus = 0");
		buffer.append(" and senddate < '"+date+"'");
		buffer.append(" and billid = '"+billid+"'");
		Object result = new BaseDAO().executeQuery(buffer.toString(), new ResultSetProcessor() {
			@Override
			public Object handleResultSet(ResultSet rs) throws SQLException {
				while(rs.next()) {
					return rs.getString("pk_checkflow");
				}
				return null;
			}
		});
		return result != null ? result.toString() : null;
	}

}
