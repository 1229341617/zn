package nc.jzmobile.handler;

import java.util.List;
import java.util.Map;

import nc.bs.framework.common.NCLocator;
import nc.itf.uap.pf.IWorkflowService;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.pub.fa.common.util.StringUtils;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pf.mobileapp.ITaskType;
import nc.vo.pf.mobileapp.TaskMetaData;
import nc.vo.pf.mobileapp.query.TaskQuery;
import nc.vo.pub.BusinessException;

/**
 * 
 * ��ȡ���������еĿɱ༭��
 * @author mxx
 *
 */
public class GetEditPropertiesHandler implements INCMobileServletHandler{

	@Override
	public Result handler(Map<String, String> map) throws Exception {
		
		
		Result result = Result.instance();
		
		String userid = map.get("userid");
		String taskid = map.get("taskid");
		
		if(StringUtils.isEmpty(userid)){
			throw new BusinessException("�û���Ϣ����Ϊ�գ�");
		}
		
		if(StringUtils.isEmpty(userid)){
			throw new BusinessException("������Ϣ����Ϊ�գ�");
		}
		
		try{
			ITaskType taskType = JZMobileAppUtils.getTaskType("ishandled", "unhandled");
			TaskQuery query = taskType.createNewTaskQuery();
			
			TaskMetaData tmd = query.queryTaskMetaData(taskid);
			
			
			IWorkflowService workflowservice = NCLocator.getInstance().lookup(IWorkflowService.class);
			
			//��ȡ�������еĿɱ༭��
			List<String> editProperties = null;
			
			if(tmd != null){
				editProperties = workflowservice.getEditablePreoperties(userid, tmd.getBillType(), tmd.getBillId(), 2);
			}
			
			result.success().setData(editProperties);
			
		}catch(Exception e){
			e.printStackTrace();
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage("��ȡ���������пɱ༭��Ϣʧ�ܣ�"+e.getMessage());
		}
	
		return result;
	}

}
