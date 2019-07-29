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
	 * 后台审批一张单据，支持了预算控制的判断逻辑
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
		Logger.debug("******进入PfUtilTools.approveSilently方法*************************");
		Logger.debug("* billType=" + billType);
		Logger.debug("* billId=" + billId);
		Logger.debug("* checkResult=" + checkResult);
		Logger.debug("* checkNote=" + checkNote);
		Logger.debug("* checkman=" + checkman);

		// 1.获得单据聚合VO
		IPFConfig bsConfig = (IPFConfig) NCLocator.getInstance().lookup(
				IPFConfig.class.getName());
		AggregatedValueObject billVo = bsConfig.queryBillDataVO(billType,
				billId);
		if (billVo == null)
			throw new PFBusinessException(NCLangRes4VoTransl.getNCLangRes()
					.getStrByID("busitype", "busitypehint-000063")/*
																 * 错误：
																 * 根据单据类型和单据ID获取不到单据聚合VO
																 */);

		// 2.获得工作项并设置审批意见
		IWorkflowMachine bsWorkflow = (IWorkflowMachine) NCLocator
				.getInstance().lookup(IWorkflowMachine.class.getName());
		HashMap hmPfExParams = new HashMap();
		WorkflownoteVO worknoteVO = bsWorkflow.checkWorkFlow(
				IPFActionName.APPROVE + checkman, billType, billVo,
				hmPfExParams);
		if (worknoteVO != null) {
			worknoteVO.setChecknote(checkNote);
			// 获取审批结果-通过/不通过/驳回
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
						"busitypehint-000064")/* 错误：消息格式不对 */;

			// 指派信息
			if (dispatched_ids != null && dispatched_ids.length > 0) {
				// 分离活动与其指派的参与者
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
				// 填写到活动的指派信息中
				Vector vecDispatch = worknoteVO.getTaskInfo()
						.getAssignableInfos();
				for (int i = 0; i < vecDispatch.size(); i++) {
					AssignableInfo ai = (AssignableInfo) vecDispatch.get(i);

					// yanke1 2012-7-21
					// 此处checkWorkflow读出来的assignableInfo中可能含有此环节之前的指派信息
					// （比如曾经指派过，又弃审了，或曾经指派过，又被驳回回来了）
					// 因此手工清理一下
					if (ai.getAssignedOperatorPKs() != null) {
						ai.getAssignedOperatorPKs().clear();
					}
					if (ai.getOuAssignedUsers() != null) {
						ai.getOuAssignedUsers().clear();
					}

					HashSet hs = (HashSet) hm.get(ai.getActivityDefId());
					if (hs != null) {
						// XXX:要避免添加重复的指派用户PK
						for (Iterator iter = hs.iterator(); iter.hasNext();) {
							String userId = (String) iter.next();
							if (!ai.getAssignedOperatorPKs().contains(userId))
								ai.getAssignedOperatorPKs().add(userId);
						}
					}
				}
			}
		} else
			Logger.debug("checkWorkflow返回的结果为null");
		
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