package nc.jzmobile.handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.processor.ColumnListProcessor;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pf.mobileapp.ITaskType;
import nc.vo.pf.mobileapp.MobileAppUtils;
import nc.vo.pf.mobileapp.query.TaskQuery;
/**
 * 
 * 
 * 判断任务是否是代办任务
 * @author mxx
 *
 */
public class TaskUnhandleHandler implements INCMobileServletHandler{

	@Override
	public Result handler(Map<String, String> map) throws Exception {
		
		Logger.info("==========TaskUnhandleHandler start==========");
		Result result = Result.instance();
		try{
			
			
			String userid = map.get("userid");
			String taskid = map.get("taskid");
			
			boolean flag = isUnhandled(userid,taskid);
			result.success().setData(flag);
			
		}catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(3000);
			result.setErrorMessage("不在任务列表内-详细信息如下:"+e.getMessage());
		}
		Logger.info("==========TaskUnhandleHandler end==========");
		return result;
	}
	
	
	
    @SuppressWarnings("unchecked")
	private boolean isUnhandled(String userid,String taskid) throws Exception{
    	
    	String pkGroup = JZMobileAppUtils.getPkGroupByUserId(userid);
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String date = format.format(new Date());
    	ITaskType taskType = MobileAppUtils.getTaskType("ishandled", "unhandled");
		// 查询单据类型名称
        
		TaskQuery query = taskType.createNewTaskQuery();
		query.setPk_group(pkGroup);
		query.setCuserid(userid);
		query.setDate(date);
		query.setTaskType(taskType);
		String sql = query.getPksSql();
		int strat = sql.indexOf("order by");
		sql = sql.substring(0, strat);
		StringBuffer buffer = new StringBuffer();
		buffer.append(sql);
		buffer.append(" AND pk_checkflow = '"+taskid+"'");
		IUAPQueryBS qry = NCLocator.getInstance().lookup(IUAPQueryBS.class);
		List<String> pksList = (List<String>) qry.executeQuery(buffer.toString(),
				new ColumnListProcessor());
		
		if(pksList != null && pksList.size() != 0){
			return true;
		}
    	return false;
    }

}
