package nc.jzmobile.app.impl;

import nc.bs.pub.workflownote.WorknoteManager;
import nc.itf.jzmobile.IPFWorkflowQuery;
import nc.vo.pub.BusinessException;
import nc.vo.pub.pf.Pfi18nTools;
import nc.vo.pub.workflownote.WorkflownoteVO;

public class PFWorkflowQueryImpl implements IPFWorkflowQuery{

	@Override
	public WorkflownoteVO[] queryAllCheckInfo(String billid, String billtype)
			throws BusinessException {
		WorknoteManager manager = new WorknoteManager();
		WorkflownoteVO[] vos = manager.queryAllMessageChecked(billid, billtype);
		if(vos != null && vos.length > 0)
		{
			for(WorkflownoteVO vo : vos)
			{
				if(vo.getApprovestatus() != 0){
					vo.setCheckname(Pfi18nTools.getUserName(vo.getCheckman()));
				}
			}
		}
		return vos;
	}

}
