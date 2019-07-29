package nc.itf.uap.pf;

import java.util.List;
import java.util.Map;

import nc.vo.jzmobile.app.FilterModel;
import nc.vo.pf.mobileapp.TaskMetaData;
import nc.vo.pub.BusinessException;

/**
 * @author yanke1
 *
 */
@ComponentMetaInfo( CMT = true, remote = true )
public interface IPFMobileAppService {

	public Map<String, Object> getTaskButtonList(
			String status
		) throws BusinessException;
	public Map<String,List<FilterModel>> getOrgAndBillmaker(String groupid, String userid, String date,
			String statuskey, String statuscode,String condition) throws BusinessException;
	public Integer getTaskListCount(String groupid, String userid, String date,
			String statuskey, String statuscode,
			String condition,String[] pkOrg,String[] billmaker,String[] moudles,String datetype) throws BusinessException;
	
	public Integer getTaskListCount(String groupid, String userid, String date,
			String statuskey, String statuscode,
			String condition,String[] pkOrg,String[] billmaker,String[] moudles) throws BusinessException;
	public List<Map<String, Object>> getTaskList(String groupid, String userid, String date,
			String statuskey, String statuscode, int startline,int count,
			String condition,String[] pkOrg,String[] billmaker,String[] moudles,String datetype) throws BusinessException;
	
	public List<Map<String, Object>> getTaskList(String groupid, String userid, String date,
			String statuskey, String statuscode, int startline,int count,
			String condition,String[] pkOrg,String[] billmaker,String[] moudles) throws BusinessException;
	
	public Map<String, Object> getTaskList(
			String groupid, 
			String userid,
			String date, 
			String statuskey, 
			String statuscode, 
			Integer startline, 
			Integer count
		) throws BusinessException;
	
	public Map<String, Object> getTaskList(
			String groupid, 
			String userid,
			String date, 
			String statuskey, 
			String statuscode, 
			String condition,
			Integer startline, 
			Integer count
			) throws BusinessException;
	
	public Map<String, Object> getTaskBill(
			String groupid,
			String userid,
			String taskid,
			String statuskey,
			String statuscode
		) throws BusinessException;
	
	
	public Map<String, Object> getTaskAction(
			String groupid,
			String taskid,
			String statuskey,
			String statuscode
		) throws BusinessException;
	
	public TaskMetaData doAgree(
			String groupid,
			String userid,
			String taskid,
			String note,
			List<String> cuserids,
			List<String> mesSenders
		) throws BusinessException;
	
	public TaskMetaData doAgree(
			String groupid,
			String userid,
			String taskid,
			String note,
			List<String> cuserids,
			List<String> mesSenders,String isWorkFlow
		) throws BusinessException;
	
	public TaskMetaData  doDisAgree(
			String groupid,
			String userid,
			String taskid,
			String note,
			List<String> cuserids,
			List<String> mesSenders
		) throws BusinessException;
	
	public TaskMetaData  doDisAgree(
			String groupid,
			String userid,
			String taskid,
			String note,
			List<String> cuserids,
			List<String> mesSenders,String isWorkFlow
		) throws BusinessException;
	
	public TaskMetaData doAddApprover(
			String groupid,
			String userid,
			String taskid,
			String note,
			List<String> userids,
			List<String> mesSenders
		) throws BusinessException;
	
	public TaskMetaData doAddApprover(
			String groupid,
			String userid,
			String taskid,
			String note,
			List<String> userids,
			List<String> mesSenders,String isWorkFlow
		) throws BusinessException;
	
	public TaskMetaData doReject(
			String groupid,
			String userid,
			String taskid,
			String note,
			String nodeid,
			List<String> mesSenders
		) throws BusinessException;
	
	public TaskMetaData doReject(
			String groupid,
			String userid,
			String taskid,
			String note,
			String nodeid,
			List<String> mesSenders,String isWorkFlow
		) throws BusinessException;
	
	
	public TaskMetaData doReassign(
			String groupid,
			String userid,
			String taskid,
			String note,
			String targetUserId,
			List<String> mesSenders
		) throws BusinessException;
	
	
	
	public Map<String, Object> getUserList(
			String groupid, 
			String userid, 
			String taskid, 
			int startline, 
			int count, 
			String condition
		) throws BusinessException;
	
	public Map<String, Object> getRejectNodeList(
			String groupid,
			String userid,
			String taskid,
			int startline,
			int count,
			String condition
		) throws BusinessException;
	
	public Map<String, Object> getAssignPsnList(
			String groupid,
			String userid,
			String taskid,
			String isagree,
		    int startline,
			int count,
			String condition
		) throws BusinessException;
	
	public Map<String, Object> getApprovedDetail(
			String groupid,
			String userid,
			String taskid,
			String statuskey,
			String statuscode,
			int startline,
			int count
		) throws BusinessException;
	
	public Map<String, Object> getPsnDetail(
			String groupid,
			String userid,
			String psnid
		) throws BusinessException;
	
	public Map<String, Object> getMessageAttachmentList(
			String groupid,
			String userid,
			String taskid,
			String statuskey,
			String statuscode
		) throws BusinessException;
	
	public Map<String, Object> getMessageAttachment(
			String groupid,
			String userid,
			String fileid,
			String downflag,
			String startposition
		) throws BusinessException;
	
	public Map<String, Object> getConditionDescription(
			String groupid,
			String userid
		) throws BusinessException;
	
	public Map<String, Object> getTaskStatusList(
			String groupid,
			String userid
		)throws BusinessException;
	
	public Map<String, Object> getDefaultValueOfAction(
			String groupid,
			String userid,
			String taskid,
			String statuskey,
			String statuscode,
			String actioncode
		)throws BusinessException;
	
	public Map<String, Object> doAction(
			String groupid,
			String userid,
			List<Map<String, Object>> list
		)throws BusinessException;
	
	public Map<String, Object> uploadFile(
			String groupid,
			String userid,
			String taskid,
			String actioncode,
			List<Map<String, Object>> filelist
		)throws BusinessException;
	
	
	/**
	 * 只是服务器端在查询相应工作项所关联的附件时调用
	 * 移动端请勿调用
	 * */
	public Map<String, Object> getMessageAttachmentListForReceived(String taskid) 
		throws BusinessException;

	public List<Map<String, Object>> getTaskList(String groupid, String userid, String date,
			String statuskey, String statuscode, int startline, int count,
			String condition) throws BusinessException;
	
	public Integer getTaskListCount(String groupid, String userid, String date,
			String statuskey, String statuscode,
			String condition) throws BusinessException;
	

	/**
	 * 适配建筑预算控制提示后继续审批
	 * @param groupid
	 * @param userid
	 * @param taskid
	 * @param note
	 * @param cuserids
	 * @return
	 * @throws BusinessException
	 */
	public TaskMetaData redoAgree(
			String groupid,
			String userid,
			String taskid,
			String note
		) throws BusinessException;
	
	public TaskMetaData doBack(
			String userid,
			String taskid
	) throws BusinessException;
	
	public TaskMetaData doBack(
			String userid,
			String taskid,String isWorkFlow
	) throws BusinessException;
}

