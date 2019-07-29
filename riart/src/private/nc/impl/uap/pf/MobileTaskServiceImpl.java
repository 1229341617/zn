package nc.impl.uap.pf;

import java.util.List;

import nc.bs.logging.Logger;
import nc.bs.pf.pub.PfDataCache;
import nc.bs.pub.taskmanager.TaskManagerDMO;
import nc.itf.uap.pf.IMobileTaskService;
import nc.vo.pub.BusinessException;
import nc.vo.wfengine.core.transition.BasicTransitionEx;
import nc.vo.wfengine.core.workflow.WorkflowProcess;
import nc.vo.wfengine.pub.WFTask;

public class MobileTaskServiceImpl implements IMobileTaskService {

	@Override
	public String getNextApprover(String taskId) throws BusinessException {
		String name = null;
		try{
			WFTask currentTask = new TaskManagerDMO().getTaskByPK(taskId);
			if(null != currentTask){
				WorkflowProcess wp = PfDataCache.getWorkflowProcess(currentTask.getWfProcessDefPK(), currentTask.getWfProcessInstancePK());
				 for(int i=0;i<wp.getTransitions().size();i++){
					 BasicTransitionEx be=(BasicTransitionEx) wp.getTransitions().get(i);
					 if(currentTask.getActivityID().equals(be.getFrom())){
						  name=wp.findActivityByID(be.getTo()).getName();
					 }
					 be=null;
				 }
			}
		}catch(Exception ex){
			Logger.error("获取下一个审批人出错:" + ex.toString());
			throw new BusinessException("获取下一个审批人出错:" + ex.toString());
			
		}
		return name;
	}

}
