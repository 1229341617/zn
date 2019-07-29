package nc.jzmobile.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.logging.Logger;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pf.mobileapp.ITaskType;
import nc.vo.pf.mobileapp.TaskMetaData;
import nc.vo.pf.mobileapp.query.TaskQuery;
import nc.vo.sm.UserVO;

public class GetTaskUserListHandler implements INCMobileServletHandler {
	/**
	 * 加载pk时默认加载系数。加载的总行数=起始行+请求行数*此系数
	 */
	public Result handler(Map<String, String> map) throws Exception {
		Logger.info("==========GetTaskUserListHandler start==========");
		Result result = Result.instance();
		try{
			
			String condition = map.get("condition");
			int start=1;
			int count=10;
			try{
				start = Integer.parseInt(map.get("startline"));
			}catch (Exception e) {}
			try{
				count = Integer.parseInt(map.get("count"));
			}catch (Exception e) {}
			String statuskey = map.get("statuskey");
			String statuscode = map.get("statuscode");
			String taskid = map.get("taskid");
			ITaskType taskType = JZMobileAppUtils.getTaskType(statuskey, statuscode);
			TaskQuery query = taskType.createNewTaskQuery();

			// 查询单据类型名称
			TaskMetaData tmd = query.queryTaskMetaData(taskid);
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			Integer sum = JZMobileAppUtils.findSendToUserCount(tmd.getCuserid(), condition);
			List<UserVO> userList = JZMobileAppUtils.findSendToUserList(tmd.getCuserid());
			resultMap.put("count", sum);
			resultMap.put("userlist", userList);
			result.setData(resultMap);
		}catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		Logger.info("==========GetTaskUserListHandler end==========");
		return result;
	}
	
	
	
}
