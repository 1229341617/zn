package nc.itf.jzmobile;

import nc.vo.pub.BusinessException;
import nc.vo.pub.workflownote.WorkflownoteVO;

public interface IPFWorkflowQuery {
	public WorkflownoteVO[] queryAllCheckInfo(String billid, String billtype) throws BusinessException;
}
