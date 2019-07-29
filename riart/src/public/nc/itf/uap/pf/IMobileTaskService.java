package nc.itf.uap.pf;

import nc.vo.pub.BusinessException;

public interface IMobileTaskService {

	String getNextApprover(String taskId) throws BusinessException;
}
