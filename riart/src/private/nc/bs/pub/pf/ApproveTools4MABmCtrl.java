package nc.bs.pub.pf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.uap.pf.IPFConfig;
import nc.itf.uap.pf.IWorkflowMachine;
import nc.itf.uap.pf.IplatFormEntry;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.jzpm.budgetctrl.BudgetCtrlUserObject;
import nc.vo.pub.pf.AssignableInfo;
import nc.vo.pub.pf.workflow.IPFActionName;
import nc.vo.pub.workflownote.WorkflownoteVO;
import nc.vo.pubapp.pflow.PfUserObject;
import nc.vo.uap.pf.PFBusinessException;
import nc.vo.wfengine.pub.WfTaskType;

public class ApproveTools4MABmCtrl {
	
	/**
	 * ��̨����һ�ŵ��ݣ�֧����Ԥ����Ƶ��ж��߼�
	 * @param billType
	 * @param billId
	 * @param checkResult
	 * @param checkNote
	 * @param checkman
	 * @param dispatched_ids
	 * @param isredo
	 * @return
	 * @throws Exception
	 */
	public static String approveSilently(String billType, String billId,
			String checkResult, String checkNote, String checkman,
			String[] dispatched_ids,boolean isredo) throws Exception {
		Logger.debug("******����PfUtilTools.approveSilently����*************************");
		Logger.debug("* billType=" + billType);
		Logger.debug("* billId=" + billId);
		Logger.debug("* checkResult=" + checkResult);
		Logger.debug("* checkNote=" + checkNote);
		Logger.debug("* checkman=" + checkman);

		// 1.��õ��ݾۺ�VO
		IPFConfig bsConfig = (IPFConfig) NCLocator.getInstance().lookup(
				IPFConfig.class.getName());
		AggregatedValueObject billVo = bsConfig.queryBillDataVO(billType,
				billId);
		if (billVo == null)
			throw new PFBusinessException(NCLangRes4VoTransl.getNCLangRes()
					.getStrByID("busitype", "busitypehint-000063")/*
																 * ����
																 * ���ݵ������ͺ͵���ID��ȡ�������ݾۺ�VO
																 */);

		// 2.��ù���������������
		IWorkflowMachine bsWorkflow = (IWorkflowMachine) NCLocator
				.getInstance().lookup(IWorkflowMachine.class.getName());
		HashMap hmPfExParams = new HashMap();
		WorkflownoteVO worknoteVO = bsWorkflow.checkWorkFlow(
				IPFActionName.APPROVE + checkman, billType, billVo,
				hmPfExParams);
		if (worknoteVO != null) {
			worknoteVO.setChecknote(checkNote);
			// ��ȡ�������-ͨ��/��ͨ��/����
			if ("Y".equalsIgnoreCase(checkResult)) {
				worknoteVO.setApproveresult("Y");
			} else if ("N".equalsIgnoreCase(checkResult)) {
				worknoteVO.setApproveresult("N");
			} else if ("R".equalsIgnoreCase(checkResult)) {
				worknoteVO.getTaskInfo().getTask()
						.setTaskType(WfTaskType.Backward.getIntValue());
				worknoteVO.getTaskInfo().getTask().setBackToFirstActivity(true);
			} else
				return NCLangRes4VoTransl.getNCLangRes().getStrByID("busitype",
						"busitypehint-000064")/* ������Ϣ��ʽ���� */;

			// ָ����Ϣ
			if (dispatched_ids != null && dispatched_ids.length > 0) {
				// ��������ָ�ɵĲ�����
				HashMap hm = new HashMap();
				for (int i = 0; i < dispatched_ids.length; i++) {
					int index = dispatched_ids[i].indexOf("#");
					if(index < 0 || index > dispatched_ids[i].length() -2){
						continue;
					}
					String userid = dispatched_ids[i].substring(0, index);
					String actDefid = dispatched_ids[i].substring(index + 1);
					if (hm.get(actDefid) == null)
						hm.put(actDefid, new HashSet());
					((HashSet) hm.get(actDefid)).add(userid);
				}
				// ��д�����ָ����Ϣ��
				Vector vecDispatch = worknoteVO.getTaskInfo()
						.getAssignableInfos();
				for (int i = 0; i < vecDispatch.size(); i++) {
					AssignableInfo ai = (AssignableInfo) vecDispatch.get(i);

					// yanke1 2012-7-21
					// �˴�checkWorkflow��������assignableInfo�п��ܺ��д˻���֮ǰ��ָ����Ϣ
					// ����������ָ�ɹ����������ˣ�������ָ�ɹ����ֱ����ػ����ˣ�
					// ����ֹ�����һ��
					if (ai.getAssignedOperatorPKs() != null) {
						ai.getAssignedOperatorPKs().clear();
					}
					if (ai.getOuAssignedUsers() != null) {
						ai.getOuAssignedUsers().clear();
					}

					HashSet hs = (HashSet) hm.get(ai.getActivityDefId());
					if (hs != null) {
						// XXX:Ҫ��������ظ���ָ���û�PK
						for (Iterator iter = hs.iterator(); iter.hasNext();) {
							String userId = (String) iter.next();
							if (!ai.getAssignedOperatorPKs().contains(userId))
								ai.getAssignedOperatorPKs().add(userId);
						}
					}
				}
			}
		} else
			Logger.debug("checkWorkflow���صĽ��Ϊnull");
		
		if (isredo) {
			PfUserObject userObj = new PfUserObject();
			BudgetCtrlUserObject ctrlObj = new BudgetCtrlUserObject();
			ctrlObj.setReDoAction(true);
			userObj.setUserObject(ctrlObj);
			IplatFormEntry pff = (IplatFormEntry) NCLocator.getInstance()
					.lookup(IplatFormEntry.class.getName());
			pff.processAction(IPFActionName.APPROVE + checkman, billType,
					worknoteVO, billVo, userObj, hmPfExParams);
		} else {
			IplatFormEntry pff = (IplatFormEntry) NCLocator.getInstance()
					.lookup(IplatFormEntry.class.getName());
			pff.processAction(IPFActionName.APPROVE + checkman, billType,
					worknoteVO, billVo, null, hmPfExParams);

		}
		
		return null;
	}
}