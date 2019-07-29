package nc.impl.uap.pf;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.ml.NCLangResOnserver;
import nc.bs.pub.pf.ApproveTools4MABmCtrl;
import nc.bs.pub.pf.MobileApproveTools;
import nc.bs.pub.wfengine.impl.ActionEnvironment;
import nc.bs.wfengine.engine.ActivityInstance;
import nc.bs.wfengine.engine.EngineService;
import nc.document.pub.itf.INCFileSystem;
import nc.itf.mobile.app.IMobileBillDetailQuery;
import nc.itf.uap.IUAPQueryBS;
import nc.itf.uap.pf.IPFChecknoteService;
import nc.itf.uap.pf.IPFMobileAppService;
import nc.itf.uap.pf.IWorkflowAdmin;
import nc.itf.uap.pf.IWorkflowDefine;
import nc.itf.uap.pf.IWorkflowMachine;
import nc.itf.uap.pf.IplatFormEntry;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.ArrayProcessor;
import nc.jdbc.framework.processor.BeanProcessor;
import nc.jzmobile.utils.TitleDefUtil;
import nc.message.Attachment;
import nc.message.ByteArrayAttachment;
import nc.message.vo.AttachmentVO;
import nc.ui.pf.checknote.PfChecknoteEnum;
import nc.ui.pf.multilang.PfMultiLangUtil;
import nc.vo.am.common.util.StringUtils;
import nc.vo.bd.psn.PsndocVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.jzmobile.app.FilterModel;
import nc.vo.ml.MultiLangUtil;
import nc.vo.pf.change.PfUtilBaseTools;
import nc.vo.pf.mobileapp.ICategory;
import nc.vo.pf.mobileapp.ITaskType;
import nc.vo.pf.mobileapp.MobileAppUtil;
import nc.vo.pf.mobileapp.MobileAppUtils;
import nc.vo.pf.mobileapp.TaskMetaData;
import nc.vo.pf.mobileapp.TaskTypeFactory;
import nc.vo.pf.mobileapp.query.ActionCodeConst;
import nc.vo.pf.mobileapp.query.ApproveDetailQuery;
import nc.vo.pf.mobileapp.query.PaginationQueryFacade;
import nc.vo.pf.mobileapp.query.TaskQuery;
import nc.vo.pf.mobileapp.query.UserMatcher;
import nc.vo.pf.mobileapp.query.UserQuery;
import nc.vo.pf.pub.util.ArrayUtil;
import nc.vo.pf.term.ApproveTermConfig;
import nc.vo.pf.term.IApproveTerm;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.checknote.PfChecknoteVO;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.pf.AssignableInfo;
import nc.vo.pub.pf.Pfi18nTools;
import nc.vo.pub.pf.workflow.IPFActionName;
import nc.vo.pub.workflownote.WorkflownoteAttVO;
import nc.vo.pub.workflownote.WorkflownoteVO;
import nc.vo.sm.UserVO;
import nc.vo.uap.pf.OrganizeUnit;
import nc.vo.uap.wfmonitor.ProcessRouteRes;
import nc.vo.wfengine.core.activity.Activity;
import nc.vo.wfengine.core.activity.GenericActivityEx;
import nc.vo.wfengine.core.activity.SubFlow;
import nc.vo.wfengine.core.parser.UfXPDLParser;
import nc.vo.wfengine.core.participant.Participant;
import nc.vo.wfengine.core.workflow.WorkflowProcess;
import nc.vo.wfengine.definition.WorkflowTypeEnum;
import nc.vo.wfengine.pub.WfTaskOrInstanceStatus;
import nc.vo.wfengine.pub.WfTaskType;

import org.apache.commons.codec.binary.Base64;

public class PFMobileAppServiceImpl implements IPFMobileAppService {
	
	/**
	 * 上传附件的持久化上传记录的DAO
	 * */
	private BaseDAO persistentDAO;
	
	/**
	 * 工作项查询接口
	 * */
	private IUAPQueryBS qry;
	
	/**
	 * 自定义批语的查询服务
	 * */
	private IPFChecknoteService checknoteService;
	
	private BaseDAO getPersistentDAO(){
		if(persistentDAO == null){
			persistentDAO = new BaseDAO();
		}
		return persistentDAO; 
	}
	
	private IPFChecknoteService getChecknoteService(){
		if(checknoteService == null){
			checknoteService = NCLocator.getInstance().lookup(IPFChecknoteService.class);
		}
		return checknoteService;
	}
	
	private IUAPQueryBS getQueryService() {
		if (qry == null) {
			qry = NCLocator.getInstance().lookup(IUAPQueryBS.class);
		}
		return qry;
	}

	
	@Override
	public Map<String, Object> getTaskButtonList(String status) throws BusinessException {
		
		//默认情况的处理,默认处理的方式是status的值默认为getTaskStatusList接口返回的第一个状态的key值
		if(status == null || status.equals("")){
			status = ITaskType.CATEGORY_RECEIVED;;
		}
		
		ITaskType[] types = TaskTypeFactory.getInstance().get(status);
		if (ArrayUtil.isNull(types)) {
			throw new IllegalArgumentException("Unsupported status: " + status);
		} 
		List<Map<String, Object>> list = MobileAppUtils.createArrayList();

		for (ITaskType t : types) {
			Map<String, Object> map = MobileAppUtils.createHashMap();
			map.put("statuscode", t.getCode());
			map.put("statusname", t.getName());

			list.add(map);
		}

		Map<String, Object> resultMap = MobileAppUtils.createHashMap();

		resultMap.put("statuskey", status);
		resultMap.put("statusstructlist", list);

		return resultMap;
	}
	
	@Override
	public Map<String, Object> getTaskList(
			String groupid, 
			String userid,
			String date, 
			String statuskey, 
			String statuscode, 
			Integer startline, 
			Integer count
	) throws BusinessException {
		ITaskType taskType = MobileAppUtils.getTaskType(statuskey, statuscode);
		
		TaskQuery query = taskType.createNewTaskQuery();

		// 设置TaskQuery的查询参数
		// 以便能生成id和pkSql
		query.setPk_group(groupid);
		query.setCuserid(userid);
		query.setDate(date);
		query.setTaskType(taskType);

		List<Map<String, Object>> list = PaginationQueryFacade.getInstance().query(query, startline, count);
		
		Map<String, Object> map = MobileAppUtils.createHashMap();
		map.put("taskstructlist", list);
		
		return map;
	}
	
	@Override
	public Map<String, Object> getTaskList(
			String groupid, 
			String userid,
			String date, 
			String statuskey, 
			String statuscode,
			String condition,
			Integer startline, 
			Integer count
			) throws BusinessException {
		ITaskType taskType = MobileAppUtils.getTaskType(statuskey, statuscode);
		
		TaskQuery query = taskType.createNewTaskQuery();
		
		// 设置TaskQuery的查询参数
		// 以便能生成id和pkSql
		query.setPk_group(groupid);
		query.setCuserid(userid);
		query.setDate(date);
		query.setTaskType(taskType);
		
		List<Map<String, Object>> list = PaginationQueryFacade.getInstance().query(query, startline, count);
		
		Map<String, Object> map = MobileAppUtils.createHashMap();
		map.put("taskstructlist", list);
		
		return map;
	}

	@Override
	public Map<String, Object> getTaskBill(
			String groupid,
			String userid,
			String taskid,
			String statuskey,
			String statuscode
	) throws BusinessException {
		ITaskType taskType = MobileAppUtils.getTaskType(statuskey, statuscode);
		TaskQuery query = taskType.createNewTaskQuery();

		// 查询单据类型名称
		TaskMetaData tmd = query.queryTaskMetaData(taskid);

		String pk_billtype = tmd.getBillType();
//		String billTypeName = Pfi18nTools.i18nBilltypeName(pk_billtype);
		
		IMobileBillDetailQuery service = NCLocator.getInstance().lookup(IMobileBillDetailQuery.class);
		// 查询单据明细
//		Object obj = query.queryTaskBill(taskid);
		Map<String, Object> map = service.getMobileBillDetail(groupid, tmd, pk_billtype, statuskey,statuscode,taskid);

		return map;
	}

	@Override
	public Map<String, Object> getTaskAction(
			String groupid,
			String taskid,
			String statuskey,
			String statuscode
	) throws BusinessException 	{
		ITaskType taskType = MobileAppUtils.getTaskType(statuskey, statuscode);
		TaskQuery query = taskType.createNewTaskQuery();
		
		return query.queryTaskActions(taskid);
	}

	@Override
	public TaskMetaData doAgree(
			String groupid,
			String userid,
			String taskid,
			String note,
			List<String> cuserids,
			List<String> mesSenders
	) throws BusinessException {
		TaskMetaData tmd = MobileAppUtils.queryTaskMetaData(ITaskType.CATEGORY_RECEIVED, ITaskType.RECEIVED_UNHANDLED, taskid);
		
		String billtype =tmd.getBillType(); 
		String billid = tmd.getBillId();
		String result = "Y";
		
		
		String[] assigned = null;
		if (!ArrayUtil.isNull(cuserids)) {
			assigned = cuserids.toArray(new String[0]);
		}

		
		try {
//			PfUtilTools.approveSilently(billtype, billid, result, note, userid, assigned);
			MobileApproveTools.approveSilently(billtype, billid, result, note, userid, assigned, mesSenders);
		} catch (Exception e) {
			MobileAppUtils.handleException(e);
		}
		
		return tmd;
	}
	
	@Override
	public TaskMetaData doAgree(
			String groupid,
			String userid,
			String taskid,
			String note,
			List<String> cuserids,
			List<String> mesSenders,String isWorkFlow
	) throws BusinessException {
		TaskMetaData tmd = MobileAppUtils.queryTaskMetaData(ITaskType.CATEGORY_RECEIVED, ITaskType.RECEIVED_UNHANDLED, taskid);
		
		String billtype =tmd.getBillType(); 
		String billid = tmd.getBillId();
		String result = "Y";
		
		
		String[] assigned = null;
		if (!ArrayUtil.isNull(cuserids)) {
			assigned = cuserids.toArray(new String[0]);
		}
		try {
			String action = "APPROVE";
			if(StringUtils.isNotEmpty(isWorkFlow) && "Y".equals(isWorkFlow)){
				action = "SIGNAL";
			}else{
				action = "APPROVE";
			}
			MobileApproveTools.approveSilently(billtype, billid, result, note, userid, assigned, mesSenders,action);
		} catch (Exception e) {
			MobileAppUtils.handleException(e);
		}
		return tmd;
	}

	
	
	@Override
	public TaskMetaData  doDisAgree(
			String groupid,
			String userid,
			String taskid,
			String note,
			List<String> cuserids,
			List<String> mesSenders
	) throws BusinessException {
		TaskMetaData tmd = MobileAppUtils.queryTaskMetaData(ITaskType.CATEGORY_RECEIVED, ITaskType.RECEIVED_UNHANDLED, taskid);
		
		String billtype =tmd.getBillType(); 
		String billid = tmd.getBillId();
		String result = "N";
		
		
		String[] assigned = null;
		if (!ArrayUtil.isNull(cuserids)) {
			assigned = cuserids.toArray(new String[0]);
		}
		
		try {
//			PfUtilTools.approveSilently(billtype, billid, result, note, userid, assigned);
			MobileApproveTools.approveSilently(billtype, billid, result, note, userid, assigned, mesSenders);
		} catch (Exception e) {
			MobileAppUtils.handleException(e);
		}
		
		return tmd;
	}
	
	
	@Override
	public TaskMetaData  doDisAgree(
			String groupid,
			String userid,
			String taskid,
			String note,
			List<String> cuserids,
			List<String> mesSenders,String isWorkFlow
	) throws BusinessException {
		TaskMetaData tmd = MobileAppUtils.queryTaskMetaData(ITaskType.CATEGORY_RECEIVED, ITaskType.RECEIVED_UNHANDLED, taskid);
		
		String billtype =tmd.getBillType(); 
		String billid = tmd.getBillId();
		String result = "N";
		
		
		String[] assigned = null;
		if (!ArrayUtil.isNull(cuserids)) {
			assigned = cuserids.toArray(new String[0]);
		}
		
		String action = "APPROVE";
		if(StringUtils.isNotEmpty(isWorkFlow) && "Y".equals(isWorkFlow)){
			action = "SIGNAL";
		}else{
			action = "APPROVE";
		}
		
		try {
//			PfUtilTools.approveSilently(billtype, billid, result, note, userid, assigned);
			MobileApproveTools.approveSilently(billtype, billid, result, note, userid, assigned, mesSenders,action);
		} catch (Exception e) {
			MobileAppUtils.handleException(e);
		}
		
		return tmd;
	}
	
	public TaskMetaData doBack(
			String userid,
			String taskid
	) throws BusinessException {
		TaskMetaData tmd = MobileAppUtils.queryTaskMetaData(ITaskType.CATEGORY_RECEIVED, ITaskType.RECEIVED_HANDLED, taskid);
		String billtype = tmd.getBillType();
		String billid = tmd.getBillId();
		AggregatedValueObject billvo = MobileAppUtils.queryBillEntity(billtype, billid);
		
		//弃审
		IplatFormEntry srv = NCLocator.getInstance().lookup(IplatFormEntry.class);
		srv.processAction(IPFActionName.UNAPPROVE, billtype, null, billvo, null, null);
		
		return tmd;
	}
	
	public TaskMetaData doBack(
			String userid,
			String taskid,String isWorkFlow
	) throws BusinessException {
		TaskMetaData tmd = MobileAppUtils.queryTaskMetaData(ITaskType.CATEGORY_RECEIVED, ITaskType.RECEIVED_HANDLED, taskid);
		String billtype = tmd.getBillType();
		String billid = tmd.getBillId();
		AggregatedValueObject billvo = MobileAppUtils.queryBillEntity(billtype, billid);
		
		String action = "UNAPPROVE";
		if(StringUtils.isNotEmpty(isWorkFlow) && "Y".equals(isWorkFlow)){
			action = "ROLLBACK";
		}else{
			action = "UNAPPROVE";
		}
		//弃审
		IplatFormEntry srv = NCLocator.getInstance().lookup(IplatFormEntry.class);
		srv.processAction(action, billtype, null, billvo, null, null);
		
		return tmd;
	}
	
	@Override
	public TaskMetaData doReject(
			String groupid,
			String userid,
			String taskid,
			String note,
			String nodeid,
			List<String> mesSenders
	) throws BusinessException {
		TaskMetaData tmd = MobileAppUtils.queryTaskMetaData(ITaskType.CATEGORY_RECEIVED, ITaskType.RECEIVED_UNHANDLED, taskid);
		
		String billtype = tmd.getBillType();
		String billid = tmd.getBillId();
		AggregatedValueObject billvo = MobileAppUtils.queryBillEntity(billtype, billid);
		
		IWorkflowMachine srv = NCLocator.getInstance().lookup(IWorkflowMachine.class);
		WorkflownoteVO worknoteVO = srv.checkWorkFlow(IPFActionName.APPROVE, tmd.getBillType(), billvo, null);
		
		// 构造一个驳回的worknoteVO
		worknoteVO.setChecknote(note);
		worknoteVO.setApproveresult("R");
		worknoteVO.getTaskInfo().getTask().setTaskType(WfTaskType.Backward.getIntValue());
		worknoteVO.getTaskInfo().getTask().setSubmit2RjectTache(false);
		worknoteVO.setMsgExtCpySenders(mesSenders);
		
		if (StringUtil.isEmptyWithTrim(nodeid)) {
			worknoteVO.getTaskInfo().getTask().setBackToFirstActivity(true);
			worknoteVO.getTaskInfo().getTask().setJumpToActivity(null);
		} else {
			worknoteVO.getTaskInfo().getTask().setBackToFirstActivity(false);
			worknoteVO.getTaskInfo().getTask().setJumpToActivity(nodeid);
		}
		
		
		// 调用服务进行驳回操作
		IplatFormEntry entry = NCLocator.getInstance().lookup(IplatFormEntry.class);
		entry.processAction(IPFActionName.APPROVE, billtype, worknoteVO, billvo, null, (HashMap) MobileAppUtils.createHashMap());
		
		return tmd;
	}
	
	
	@Override
	public TaskMetaData doReject(
			String groupid,
			String userid,
			String taskid,
			String note,
			String nodeid,
			List<String> mesSenders,String isWorkFlow
	) throws BusinessException {
		TaskMetaData tmd = MobileAppUtils.queryTaskMetaData(ITaskType.CATEGORY_RECEIVED, ITaskType.RECEIVED_UNHANDLED, taskid);
		
		String billtype = tmd.getBillType();
		String billid = tmd.getBillId();
		AggregatedValueObject billvo = MobileAppUtils.queryBillEntity(billtype, billid);
		
		IWorkflowMachine srv = NCLocator.getInstance().lookup(IWorkflowMachine.class);
		
		String action = "APPROVE";
		if(StringUtils.isNotEmpty(isWorkFlow) && "Y".equals(isWorkFlow)){
			action = "SIGNAL";
		}else{
			action = "APPROVE";
		}
		
		WorkflownoteVO worknoteVO = srv.checkWorkFlow(action, tmd.getBillType(), billvo, null);
		
		// 构造一个驳回的worknoteVO
		worknoteVO.setChecknote(note);
		worknoteVO.setApproveresult("R");
		worknoteVO.getTaskInfo().getTask().setTaskType(WfTaskType.Backward.getIntValue());
		worknoteVO.getTaskInfo().getTask().setSubmit2RjectTache(false);
		worknoteVO.setMsgExtCpySenders(mesSenders);
		
		if (StringUtil.isEmptyWithTrim(nodeid)) {
			worknoteVO.getTaskInfo().getTask().setBackToFirstActivity(true);
			worknoteVO.getTaskInfo().getTask().setJumpToActivity(null);
		} else {
			worknoteVO.getTaskInfo().getTask().setBackToFirstActivity(false);
			worknoteVO.getTaskInfo().getTask().setJumpToActivity(nodeid);
		}
		
		
		// 调用服务进行驳回操作
		IplatFormEntry entry = NCLocator.getInstance().lookup(IplatFormEntry.class);
		entry.processAction(action, billtype, worknoteVO, billvo, null, (HashMap) MobileAppUtils.createHashMap());
		
		return tmd;
	}

	@Override
	public Map<String, Object> getUserList(
			String groupid, 
			String userid, 
			String taskid, 
			int startline, 
			int count, 
			String condition
	) throws BusinessException {
		
		List<Map<String, Object>> list = null;

		if (StringUtil.isEmptyWithTrim(condition)) {
			UserQuery query = new UserQuery(groupid);
			list = PaginationQueryFacade.getInstance().query(query, startline, count);
		} else {
			// TODO: 可以用IPaginationedQuery实现
			UserMatcher matcher = new UserMatcher();
			List<UserVO> matched = matcher.matchAll("pk_group='" + groupid + "'", condition);
			List<UserVO> paginated = MobileAppUtils.subList(matched, startline, count);
			
			list = MobileAppUtils.createArrayList();
			
			for (UserVO uvo : paginated) {
				Map<String, Object> map = MobileAppUtils.createHashMap();
				
				map.put("id", uvo.getCuserid());
				map.put("code", uvo.getUser_code());
				map.put("name", PfMultiLangUtil.getSuperVONameOfCurrentLang(uvo, "user_name"));
				
				list.add(map);
			}
		}

		Map<String, Object> result = MobileAppUtils.createHashMap();
		result.put("psnstructlist", list);

		return result;
	}
	

	@Override
	public TaskMetaData doAddApprover(
			String groupid,
			String userid,
			String taskid,
			String note,
			List<String> userids,
			List<String> mesSenders
	) throws BusinessException {
		if (ArrayUtil.isNull(userids)) {
			throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceFacadeImpl-000029")/*加签人员不能为空*/);
		}
//		BaseDAO dao = new BaseDAO();
		IWorkflowAdmin srv = NCLocator.getInstance().lookup(IWorkflowAdmin.class);
		
//		WorkflownoteVO worknote = (WorkflownoteVO) dao.retrieveByPK(WorkflownoteVO.class, taskid);
		
		
		WorkflownoteVO worknote = MobileAppUtils.checkWorkflow(taskid);

		
		worknote.setExtApprovers(userids);
		worknote.setChecknote(note);
		
		worknote.setMsgExtCpySenders(mesSenders);
		
		srv.addApprover(worknote);
	
		return nc.vo.pf.mobileapp.MobileAppUtils.convertToMeta(worknote);
	}
	
	
	public WorkflownoteVO checkWorkflowActions(String billType,
			String originBillId) throws BusinessException {
		String billid = null;
		try {
			AggregatedValueObject billvo = MobileAppUtil.queryBillEntity(
					billType, originBillId);

			PfParameterVO paraVO = PfUtilBaseTools.getVariableValue(billType,
					IPFActionName.SIGNAL, billvo, null, null, null, null,
					new HashMap(), new Hashtable());
			billid = paraVO.m_billVersionPK;
			ActionEnvironment.getInstance().putParaVo(billid, paraVO);

			return new EngineService().checkUnfinishedWorkitem(paraVO,
					WorkflowTypeEnum.SubWorkflow.getIntValue());
		} catch (Exception e) {
			if (e instanceof BusinessException) {
				throw (BusinessException) e;
			} else {
				throw new BusinessException(e.getMessage(), e);
			}
		} finally {
			ActionEnvironment.getInstance().putParaVo(billid, null);
		}
	}
	
	@Override
	public TaskMetaData doAddApprover(
			String groupid,
			String userid,
			String taskid,
			String note,
			List<String> userids,
			List<String> mesSenders,String isWorkFlow
	) throws BusinessException {
		if (ArrayUtil.isNull(userids)) {
			throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceFacadeImpl-000029")/*加签人员不能为空*/);
		}
//		BaseDAO dao = new BaseDAO();
		IWorkflowAdmin srv = NCLocator.getInstance().lookup(IWorkflowAdmin.class);
		
//		WorkflownoteVO worknote = (WorkflownoteVO) dao.retrieveByPK(WorkflownoteVO.class, taskid);
		
		TaskMetaData tmd = MobileAppUtils.queryTaskMetaData(
				ITaskType.CATEGORY_RECEIVED, ITaskType.RECEIVED_UNHANDLED,
				taskid);
		WorkflownoteVO worknote = checkWorkflowActions(tmd.getBillType(),tmd.getBillId());

		
		worknote.setExtApprovers(userids);
		worknote.setChecknote(note);
		
		worknote.setMsgExtCpySenders(mesSenders);
		
		
		srv.addApprover(worknote);
	
		return nc.vo.pf.mobileapp.MobileAppUtils.convertToMeta(worknote);
	}
	

	@Override
	public TaskMetaData doReassign(
			String groupid,
			String userid,
			String taskid,
			String note,
			String targetUserId,
			List<String> mesSenders
	) throws BusinessException {
		TaskMetaData tmd = MobileAppUtils.queryTaskMetaData(ITaskType.CATEGORY_RECEIVED, ITaskType.RECEIVED_UNHANDLED, taskid);
		
		IWorkflowAdmin srv = NCLocator.getInstance().lookup(IWorkflowAdmin.class);
		srv.appointWorkitem(tmd.getBillId(), taskid, userid, targetUserId);
//		MobileApproveTools.appointWorkitem(tmd.getBillId(), taskid, userid, targetUserId, null, mesSenders);
		return tmd;
	}
	
	
	

	@Override
	public Map<String, Object> getRejectNodeList(
			String groupid,
			String userid,
			String taskid,
			int startline,
			int count,
			String condition
	) throws BusinessException {
		// TODO: 待优化
		// 1. 性能优化
		// 2. 要取最终的用户?
		TaskMetaData tmd = MobileAppUtils.queryTaskMetaData(ITaskType.CATEGORY_RECEIVED, ITaskType.RECEIVED_UNHANDLED, taskid);
		WorkflownoteVO worknoteVO = MobileAppUtils.checkWorkflow(taskid);
		
		IWorkflowDefine srv = NCLocator.getInstance().lookup(IWorkflowDefine.class);
		
		// 基本规则：在主流程中不能驳回到子流程里的环节，只能驳回到主流程上的子流程关节
		// 规则1: 如果是工作审批子流程，那么只显示这个子流程实例的所有环节
		// 规则2：如果是审批流子流程，那么显示主流程中所有已办（不包含虚活动节点，包含子流程节点）和本子流程中所有已办
		// 规则3：如果是审批主流程，那么显示所有已办活动、子流程节点（不包含虚节点）
		
		List<ProcessRouteRes> prrList = new ArrayList<ProcessRouteRes>();
		
		String billid = tmd.getBillId();
		String billtype = tmd.getBillType();
		
		int workflowType = worknoteVO.getWorkflow_type();
		if (workflowType == WorkflowTypeEnum.SubWorkApproveflow.getIntValue()) {
			String pk_wf_instance = worknoteVO.getTaskInfo().getTask().getWfProcessInstancePK();
			ProcessRouteRes prr = srv.queryProcessRoute(tmd.getBillId(), tmd.getBillType(), pk_wf_instance, workflowType);
			
			prrList.add(prr);
		} else if (workflowType == WorkflowTypeEnum.SubApproveflow.getIntValue()) {
			ProcessRouteRes prr = srv.queryProcessRoute(tmd.getBillId(), tmd.getBillType(), null, WorkflowTypeEnum.Approveflow.getIntValue());
			prrList.add(prr);
			
			String pk_wf_instance = worknoteVO.getTaskInfo().getTask().getWfProcessInstancePK();
			prr = srv.queryProcessRoute(billid, billtype, pk_wf_instance, workflowType);
			prrList.add(prr);
		} else {
			ProcessRouteRes prr = srv.queryProcessRoute(tmd.getBillId(), tmd.getBillType(), null, WorkflowTypeEnum.Approveflow.getIntValue());
			prrList.add(prr);
		}
		

		
		try {
			List<Map<String, Object>> resultList = getCheckedActivities(prrList.toArray(new ProcessRouteRes[0]), condition);

			// pagination
			resultList = MobileAppUtils.subList(resultList, startline, count);

			Map<String, Object> map = MobileAppUtils.createHashMap();
			map.put("psnstructlist", resultList);

			return map;
		} catch (Exception e) {
			MobileAppUtils.handleException(e);
			return null;
		}
	}
	
	private List<Map<String, Object>> getCheckedActivities(ProcessRouteRes[] prs, String matchString) throws Exception {
		List<Map<String, Object>> resultList = MobileAppUtils.createArrayList();
		
		if (prs != null) {
			for (ProcessRouteRes p : prs) {
				ActivityInstance[] ais = p.getActivityInstance();
				WorkflowProcess wp = null;
				if (p.getXpdlString() != null) {
					String def_xpdl = p.getXpdlString().toString();
					wp = UfXPDLParser.getInstance().parseProcess(def_xpdl);
				}

				if (wp != null) {
					for (ActivityInstance inst : ais) {
						if (inst.getStatus() != WfTaskOrInstanceStatus.Finished.getIntValue()) {
							continue;
						}

						Activity act = wp.findActivityByID(inst.getActivityID());

						if (act instanceof GenericActivityEx) {
							GenericActivityEx gae = (GenericActivityEx) act;
							Participant parti = wp.findParticipantByID(gae.getPerformer());

							String name = parti.getName();

							// 在名称中匹配
							if (!StringUtil.isEmptyWithTrim(matchString) && !name.contains(matchString)) {
								continue;
							}

							String id = gae.getId();
//							String code = gae.getPerformer();

							Map<String, Object> actEntry = MobileAppUtils.createHashMap();

							actEntry.put("id", id);
							// yanke1 跟ma需求商讨过 由于activity是没有code的，这里传空就可以
							actEntry.put("code", "");
//							actEntry.put("code", code);
							actEntry.put("name", name);

							resultList.add(actEntry);
						} else if (act.getImplementation() instanceof SubFlow) {
							String id = act.getId();
							String code = act.getId();
							String name = act.getName();
							
							Map<String, Object> actEntry = MobileAppUtils.createHashMap();
							
							actEntry.put("id", id);
//							actEntry.put("code", code);
							actEntry.put("code", "");
							actEntry.put("name", name);
							
							resultList.add(actEntry);
						}
					}
				}

				// yanke1 不再找子流程的了
//				resultList.addAll(getCheckedActivities(p.getSubProcessRoute(), matchString));
			}
		}
		return resultList;
	}
	
	@Override
	public Map<String, Object> getAssignPsnList(
			String groupid,
			String userid,
			String taskid,
			String isagree,
		    int startline,
			int count,
			String condition
	) throws BusinessException {
		WorkflownoteVO note = MobileAppUtils.checkWorkflow(taskid);
		Vector<AssignableInfo> assignInfos = note.getTaskInfo().getAssignableInfos();
		
		List<Map<String, Object>> resultList = MobileAppUtils.createArrayList();
		Map<String, String> useridDispatchIdMap = new HashMap<String, String>();

		if (assignInfos != null && assignInfos.size() > 0) {
			String strCriterion = null;
			for (AssignableInfo ai : assignInfos) {
				strCriterion = ai.getCheckResultCriterion();
				
				if (
						(
							// 若审批意见为通过
							UFBoolean.valueOf(isagree).booleanValue()	
								&&
							( // 且
								// ai的条件为通过或无
								AssignableInfo.CRITERION_NOTGIVEN.equals(strCriterion)
									||
								AssignableInfo.CRITERION_PASS.equals(strCriterion)
							
							)
						)
							||
							// 或者
						(
							// 审批意见为未通过
							!UFBoolean.valueOf(isagree).booleanValue()
								&&
							(	// 且
								// ai条件为未通过或无
								AssignableInfo.CRITERION_NOTGIVEN.equals(strCriterion)
									||
								AssignableInfo.CRITERION_NOPASS.equals(strCriterion)
							)
						)
				) {
					Vector<OrganizeUnit> vt = ai.getOuUsers();

					if (vt != null && vt.size() > 0) {
						for (OrganizeUnit ou : vt) {
							Map<String, Object> map = MobileAppUtils.createHashMap();
							
							String id = ou.getPk() + "#" + ai.getActivityDefId();
							
							useridDispatchIdMap.put(ou.getPk(), id);

							map.put("id", id);
							map.put("code", ou.getCode());
							map.put("name", ou.getName());

							resultList.add(map);
						}
					}
				}
			}
		}
		
		if (!StringUtil.isEmptyWithTrim(condition)) {
			// 模糊查询
			Map<Object, Map<String, Object>> converted = MobileAppUtils.convertToMap(resultList, "id");
			UserMatcher matcher = new UserMatcher();
			
			Set<String> cuseridSet = new HashSet<String>();
			
			for (Iterator<Object> it = converted.keySet().iterator(); it.hasNext();) {
				String id = (String) it.next();
				id = id.substring(0, id.indexOf("#"));
				
				cuseridSet.add(id);
			}
			
			List<UserVO> matched = matcher.matchWithin(cuseridSet.toArray(new String[0]), condition);
			
			resultList = MobileAppUtils.createArrayList();
			
			for (UserVO uvo : matched) {
				Map<String, Object> map = MobileAppUtils.createHashMap();
				
				map.put("id", useridDispatchIdMap.get(uvo.getCuserid()));
				map.put("code", uvo.getUser_code());
				map.put("name", PfMultiLangUtil.getSuperVONameOfCurrentLang(uvo, "user_name"));
				
				resultList.add(map);
			}
		}
		
		if (resultList != null && resultList.size() > 0) {
			try {
				Map<String, Object>[] array = resultList.toArray(new Map[0]);

				Arrays.sort(array, new Comparator() {

					@Override
					public int compare(Object o1, Object o2) {
						if (o1 instanceof Map && o2 instanceof Map) {
							Object name1 = ((Map) o1).get("name");
							Object name2 = ((Map) o2).get("name");

							if (name1 instanceof String && name2 instanceof String) {
								return ((String) name1).compareToIgnoreCase((String) name2);
							}
						}

						return 0;
					}

				});

				List<Map<String, Object>> tempList = MobileAppUtils.createArrayList();
				
				for (Map<String, Object> row : array) {
					tempList.add(row);
				}

				resultList = tempList;
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
			}
		}
		
		
		
		
		resultList = MobileAppUtils.subList(resultList, startline, count);
		
		
		Map<String, Object> resultMap = MobileAppUtils.createHashMap();
		resultMap.put("psnstructlist", resultList);
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> getApprovedDetail(
			String groupid,
			String userid,
			String taskid,
			String statuskey,
			String statuscode,
			int startline,
			int count
	) throws BusinessException {
		TaskMetaData tmd = MobileAppUtils.queryTaskMetaData(statuskey, statuscode, taskid);
		
		ApproveDetailQuery query = new ApproveDetailQuery(tmd);
		List<Map<String, Object>> detailList = PaginationQueryFacade.getInstance().query(query, startline, count);
		
		StringBuffer sb = new StringBuffer();
		
		String suffix = MultiLangUtil.getCurrentLangSeqSuffix();
		
		sb.append("select ");
		sb.append("u_t.user_name, ");
		sb.append("u_t.user_name");
		sb.append(suffix);
		sb.append(", ");
		sb.append("i_t.billmaker, i_t.startts from pub_wf_instance i_t left join sm_user u_t on i_t.billmaker=u_t.cuserid  where i_t.billtype=? and i_t.billversionpk=? and i_t.workflow_type=?");
		
		SQLParameter param = new SQLParameter();
		param.addParam(tmd.getBillType());
		param.addParam(tmd.getBillId());
		// 是否考虑工作审批子流程？
		param.addParam(WorkflowTypeEnum.Approveflow.getIntValue());
		
		BaseDAO dao = new BaseDAO();
		Object[] summaries = (Object[]) dao.executeQuery(sb.toString(), param, new ArrayProcessor());
		
		Map<String, Object> resultMap = MobileAppUtils.createHashMap();
		
		if (summaries != null) {
			String billmakerName = summaries[0] == null ? null : String.valueOf(summaries[0]);
			String billmakerNameMl = summaries[1] == null ? null : String.valueOf(summaries[1]);
			String billmaker = summaries[2] == null ? null : String.valueOf(summaries[2]);
			String startts = summaries[3] == null ? null : String.valueOf(summaries[3]);

			if (!StringUtil.isEmptyWithTrim(billmakerNameMl)) {
				resultMap.put("makername", billmakerNameMl);
			} else {
				resultMap.put("makername", billmakerName);
			}
			resultMap.put("psnid", billmaker);
			resultMap.put("submitdate", startts);
			resultMap.put("billname", tmd.getBillNo());
			resultMap.put("billtypename", Pfi18nTools.i18nBilltypeName(tmd.getBillType()));
		}

		resultMap.put("approvehistorylinelist", detailList);
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> getPsnDetail(
			String groupid,
			String userid,
			String psnid
	) throws BusinessException {
		BaseDAO dao = new BaseDAO();
		
		String sql = "select bd_psndoc_t.* from bd_psndoc bd_psndoc_t join sm_user sm_user_t on bd_psndoc_t.pk_psndoc=sm_user_t.pk_base_doc where sm_user_t.cuserid=?";
		
		SQLParameter param = new SQLParameter();
		param.addParam(psnid);
		
		PsndocVO doc = (PsndocVO) dao.executeQuery(sql, param, new BeanProcessor(PsndocVO.class));
		
		if (doc == null) {
			throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceImpl-000000", null, new String[] { Pfi18nTools.getUserName(psnid) })/*无法找到用户: {0}的人员档案*/);
		}
		
		Map<String, Object> result = MobileAppUtils.createHashMap();
		
		doc.getMobile();
		
		result.put("pname", PfMultiLangUtil.getSuperVONameOfCurrentLang(doc, "name"));
		result.put("pdes", MobileAppUtils.getPsnJobInfo(doc.getPk_psndoc()));
		
		
		List<Map<String, Object>> list = MobileAppUtils.createArrayList();
		
		if (!StringUtil.isEmptyWithTrim(doc.getMobile())) {
			Map<String, Object> entry = new HashMap<String, Object>();
			
			entry.put("msgtype", "0");
			entry.put("propname", NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceImpl-000001")/*手机*/);
			entry.put("propvalue", doc.getMobile());

			list.add(entry);
		}
		
		if (!StringUtil.isEmptyWithTrim(doc.getOfficephone())) {
			Map<String, Object> entry = new HashMap<String, Object>();
			
			entry.put("msgtype", "1");
			entry.put("propname", NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceImpl-000002")/*办公电话*/);
			entry.put("propvalue", doc.getOfficephone());

			list.add(entry);
		}
		
		if (!StringUtil.isEmptyWithTrim(doc.getHomephone())) {
			Map<String, Object> entry = new HashMap<String, Object>();
			
			entry.put("msgtype", "2");
			entry.put("propname", NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceImpl-000003")/*家庭电话*/);
			entry.put("propvalue", doc.getHomephone());

			list.add(entry);
		}
		
		if (!StringUtil.isEmptyWithTrim(doc.getEmail())) {
			Map<String, Object> entry = new HashMap<String, Object>();
			
			entry.put("msgtype", "3");
			entry.put("propname", NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceImpl-000004")/*电子邮件*/);
			entry.put("propvalue", doc.getEmail());

			list.add(entry);
		}
		
		result.put("contactinfolist", list);
			
		return result;
	}
	
	@Override
	
	public Map<String, Object> getMessageAttachmentList(
			String groupid,
			String userid,
			String taskid,
			String statuskey,
			String statuscode
	) throws BusinessException {
		TaskMetaData tmd = MobileAppUtils.queryTaskMetaData(statuskey, statuscode, taskid);
		String sql = "select t_t.pk_file, t_t.filename, t_t.filesize from pub_workflownote_att t_t join pub_workflownote w_t on t_t.pk_checkflow=w_t.pk_checkflow where w_t.pk_billtype=? and w_t.billversionpk=?";
		
		SQLParameter param = null;
		param = new SQLParameter();
		param.addParam(tmd.getBillType());
		param.addParam(tmd.getBillId());

		BaseDAO dao = new BaseDAO();
		Collection<Object[]> col = (Collection<Object[]>) dao.executeQuery(sql, param, new ArrayListProcessor());
		
		List<Map<String, Object>> list = MobileAppUtils.createArrayList();
		
		if (col != null && col.size() > 0) {
			for (Object[] entry : col) {
				Map<String, Object> entryMap = MobileAppUtils.createHashMap();
				
				String pk_file = MobileAppUtils.getStringFromObjects(entry, 0);
				String filename = MobileAppUtils.getStringFromObjects(entry, 1);
				String filesize = MobileAppUtils.getStringFromObjects(entry, 2);
				entryMap.put("fileid", pk_file);
				entryMap.put("filename", filename);
				//根据移动应用要求，文件大小显示为KB
				String filesize_convert = MobileAppUtils.getFileSize(new Integer(filesize).intValue());
				entryMap.put("filesize", filesize_convert);
				entryMap.put("downflag", "1");
				list.add(entryMap);
			}
		}
		
		Map<String, Object> resultMap = MobileAppUtils.createHashMap();
		if(col != null && col.size() > 0){
			resultMap.put("count", String.valueOf(col.size()));
		} else{
			resultMap.put("count", String.valueOf(0));
		}
		resultMap.put("attachstructlist", list);
		return resultMap;
	}
	
	@Override
	public Map<String, Object> getMessageAttachment(
			String groupid,
			String userid,
			String fileid,
			String downflag,
			String startposition
	) throws BusinessException {
		// 目前downflag都会传1
		// startposition暂无用处
		
		INCFileSystem srv = NCLocator.getInstance().lookup(INCFileSystem.class);
		
		ByteArrayOutputStream output = null;
		try {
			output = new ByteArrayOutputStream();
			srv.doDownload(fileid, output);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		} finally {
			try {
				output.close();
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
			}
		}
		
		byte[] downloaded = output.toByteArray();
		
		Map<String, Object> map = MobileAppUtils.createHashMap();
		map.put("downloaded", downloaded);
		
		return map;
	}
	

	@Override
	public Map<String, Object> getConditionDescription(
			String groupid,
			String userid
	) throws BusinessException {
		
		String[] supportedFields = new UserMatcher().getSupportedMatchField();
		
		StringBuffer sb = new StringBuffer();
		
		for (String f : supportedFields) {
			sb.append("/");
			sb.append(f);
		}
		
		String resultValue = null;
		if (sb.length() > 0) {
			resultValue = sb.substring(1);
		}
		
		Map<String, Object> map = new MobileAppUtils().createHashMap();
		
		map.put("conditiondesc", resultValue);
		
		return map;
	}
	
	@Override
	public Map<String, Object> getTaskStatusList(String groupid, String userid)
			throws BusinessException {
		//返回初始的二种状态（我的任务，我的发起）
		ICategory taskType1 = new ICategory() {

			@Override
			public String getCategory() {
				// TODO Auto-generated method stub
				return ITaskType.CATEGORY_RECEIVED;
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceImpl-000005")/*我的任务*/;
			}
			
		};
		ICategory taskType2 = new ICategory(){

			@Override
			public String getCategory() {
				// TODO Auto-generated method stub
				return ITaskType.CATEGORY_SUBMITTED;
			}
			
			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceImpl-000006")/*我的发起*/;
			}
		};
		
		Map<String, Object> resultMap = MobileAppUtils.createHashMap();
		List<Map<String, Object>> list = MobileAppUtils.createArrayList();
		
		//我的任务
		Map<String, Object> entryMap1 = MobileAppUtils.createHashMap();
		entryMap1.put("id", taskType1.getCategory());
		entryMap1.put("name", taskType1.getName());
		list.add(entryMap1);
		
		//我的发起
		Map<String, Object> entryMap2 = MobileAppUtils.createHashMap();
		entryMap2.put("id", taskType2.getCategory());
		entryMap2.put("name", taskType2.getName());
		list.add(entryMap2);
		
		resultMap.put("list", list);
		return resultMap;
	}
	
	@Override
	public Map<String, Object> getDefaultValueOfAction(String groupid,
			String userid, String taskid, String statuskey, String statuscode,
			String actioncode) throws BusinessException {
		ITaskType taskType = MobileAppUtils.getTaskType(statuskey, statuscode);
		TaskQuery query = taskType.createNewTaskQuery();
		Map<String, Object> result = query.queryTaskActionDefaultValue(taskid, actioncode);
		
		//加入用户自定义审批语
		PfChecknoteVO[] noteVOs = null;
		if (ActionCodeConst.DOAGREE.equalsIgnoreCase(actioncode)) {
			noteVOs = getChecknoteService().queryCheckNoteByUserpk(userid, PfChecknoteEnum.PASS.toInt());
		} else if (ActionCodeConst.DODISAGREE.equalsIgnoreCase(actioncode)) {
			noteVOs = getChecknoteService().queryCheckNoteByUserpk(userid, PfChecknoteEnum.NOPASS.toInt());
		} else if(ActionCodeConst.DOREJECT.equalsIgnoreCase(actioncode)){
			noteVOs = getChecknoteService().queryCheckNoteByUserpk(userid, PfChecknoteEnum.REJECT.toInt());
		} else if(ActionCodeConst.DOADDAPPROVE.equalsIgnoreCase(actioncode)){
			String txtAddApprove = NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceFacadeImpl-000027")/*加签*/;
			result.put("opinion", txtAddApprove);
		} else if(ActionCodeConst.DOREASSIGN.equalsIgnoreCase(actioncode)){
			String txtReassign = NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceFacadeImpl-000028")/*改派*/;
			result.put("opinion", txtReassign);
		}
		
		if(noteVOs != null && noteVOs.length > 0){
			result.put("opinion", noteVOs[0].getNote());
		} else {
			
			//加入批语的默认值，与审批意见保持一致
			String opinion = "";
			
			if(ActionCodeConst.DOAGREE.equalsIgnoreCase(actioncode)){
				opinion = ApproveTermConfig.getInstance().getText(IApproveTerm.PASS);
			} else if (ActionCodeConst.DODISAGREE.equalsIgnoreCase(actioncode)) {
				opinion = ApproveTermConfig.getInstance().getText(IApproveTerm.NO_PASS);
			} else if(ActionCodeConst.DOREJECT.equalsIgnoreCase(actioncode)){
				opinion = ApproveTermConfig.getInstance().getText(IApproveTerm.REJECT);
			}
			result.put("opinion", opinion);
		}
		
		return result;
	}

	@Override
	public Map<String, Object> doAction(String groupid, String userid,
			List<Map<String, Object>> list) throws BusinessException {
		for(Map<String, Object> doActionVO : list){
			String actioncode = "";
			if(doActionVO.get("actioncode") != null){
				actioncode = String.valueOf(doActionVO.get("actioncode"));
			} 
			dispatchAction(actioncode, groupid, userid, doActionVO);
		}
		return MobileAppUtils.createHashMap();
	}

	/**
	 * 根据相应动作编码，分发到相应处理动作
	 * */
	@SuppressWarnings("unchecked")
	private void dispatchAction(String actioncode, String groupid,
			String userid, Map<String, Object> doActionVO) throws BusinessException{
		if(doActionVO.get("taskid") == null){
			throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceFacadeImpl-000020")/*taskid为空*/);
		}
		String taskid = String.valueOf(doActionVO.get("taskid"));
		if(StringUtil.isEmpty(taskid)){
			throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceFacadeImpl-000020")/*taskid为空*/);
		}
		
		String note = "";
		if(doActionVO.get("note") != null){
			note = String.valueOf(doActionVO.get("note"));
		}
		List<String> cuserids = new ArrayList<String>();
		if(doActionVO.get("ccusers") != null){
			cuserids = (List<String>)doActionVO.get("ccusers");
		}
		//转发动作
		if(ActionCodeConst.DOAGREE.equals(actioncode)){
			doAgree(groupid, userid, taskid, note, cuserids,null);
		}else if(ActionCodeConst.DODISAGREE.equals(actioncode)){
			doDisAgree(groupid, userid, taskid, note, cuserids,null);
		}/*else if(ActionCodeConst.DOBACK.endsWith(actioncode)){
			doBack( userid, taskid);
		}*/else if(ActionCodeConst.DOREJECT.equals(actioncode)){
			if(doActionVO.get("rejectmarks") == null){
				throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceFacadeImpl-000021")/*驳回的活动标识为空*/);
			}
			List<String> rejectmarks = (List<String>)doActionVO.get("rejectmarks");
			if(rejectmarks.size() == 0){
				throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceFacadeImpl-000021")/*驳回的活动标识为空*/);
			}
			if(rejectmarks.size() > 1){
				throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceFacadeImpl-000022")/*驳回的活动标识个数大于1*/);
			}
			doReject(groupid, userid, taskid, note, rejectmarks.get(0),null);
		}else if(ActionCodeConst.DOREASSIGN.equals(actioncode)){
			List<String> userids = null;
			if(doActionVO.get("usrids") != null){
				userids = (List<String>)doActionVO.get("usrids");
			}
			if(userids == null){
				if(doActionVO.get("userids") != null){
					userids = (List<String>)doActionVO.get("userids");
				}
			}
			if(userids == null){
				throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceFacadeImpl-000023")/*指派的用户ID为空*/);
			}
			if(userids.size() > 1){
				throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceFacadeImpl-000024")/*指派的用户ID个数大于1*/);
			}
			doReassign(groupid, userid, taskid, note, userids.get(0),null);
		}else if(ActionCodeConst.DOADDAPPROVE.equals(actioncode)){
			List<String> userids = null;
			
			if(doActionVO.get("usrids") != null)
				userids = (List<String>)doActionVO.get("usrids");
			if(doActionVO.get("userids") != null)
				userids = (List<String>)doActionVO.get("userids");
			
			if(userids == null){
				throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("mobileapp", "PFMobileAppServiceFacadeImpl-000025")/*加签的用户ID为空*/);
			}
			
			doAddApprover(groupid, userid, taskid, note, userids,null);
		}else
			return;
	}

	@Override
	public Map<String, Object> uploadFile(String groupid, String userid,
			String taskid, String actioncode,List<Map<String, Object>> filelist) throws BusinessException {
		try{
			WorkflownoteVO note = (WorkflownoteVO) getQueryService().retrieveByPK(WorkflownoteVO.class, taskid);
			if(note == null){
				throw new BusinessException("workflownote is null");
			}
			//获取pk_checkflow与pk_wf_task
			String pk_checkflow = taskid;
			String pk_wf_task = note.getPk_wf_task();
			List<WorkflownoteAttVO> noteAttVOs = new ArrayList<WorkflownoteAttVO>();
			if(filelist.size() == 0){
				return MobileAppUtils.createHashMap();
			}
			for(Map<String, Object> file : filelist){
				byte[] data = String.valueOf(file.get("content")).getBytes();
				//base64解码
				byte[] byteArray = new Base64().decode(data);
				String name = String.valueOf(file.get("name"));
				//校验文件名称，文件名为空，就直接略过
				if(StringUtil.isEmpty(name)){
					continue;
				}
				//获取消息中心attachment
				Attachment attach = new ByteArrayAttachment(name, byteArray);
				//获取attachmentVO
				AttachmentVO attachVO = attach.uploadToFileServer();
				String pk_file = attachVO.getPk_file();
				
				WorkflownoteAttVO noteAttVO = new WorkflownoteAttVO();
				noteAttVO.setPk_file(pk_file);
				noteAttVO.setPk_wf_task(pk_wf_task);
				noteAttVO.setPk_checkflow(pk_checkflow);
				noteAttVO.setFilename(attach.getName());
				noteAttVO.setFilesize(byteArray.length);
				noteAttVOs.add(noteAttVO);
			}
			
			//附件信息数据库持久化,与工作相关联
			if(noteAttVOs.size() > 0){
				getPersistentDAO().insertVOList(noteAttVOs);
			}
		} catch(Exception ex){
			throw new BusinessException(ex.getMessage());
		}
		return MobileAppUtils.createHashMap();
	}

	@Override
	public Map<String, Object> getMessageAttachmentListForReceived(String taskid) throws BusinessException {
		
		String sql = "select t_t.pk_file, t_t.filename, t_t.filesize from pub_workflownote_att t_t join pub_workflownote w_t on t_t.pk_checkflow=w_t.pk_checkflow where w_t.pk_checkflow=?";
		SQLParameter param = new SQLParameter();
		param.addParam(taskid);
		
		BaseDAO dao = new BaseDAO();
		Collection<Object[]> col = (Collection<Object[]>) dao.executeQuery(sql, param, new ArrayListProcessor());
		
		List<Map<String, Object>> list = MobileAppUtils.createArrayList();
		
		if (col != null && col.size() > 0) {
			for (Object[] entry : col) {
				Map<String, Object> entryMap = MobileAppUtils.createHashMap();
				
				String pk_file = MobileAppUtils.getStringFromObjects(entry, 0);
				String filename = MobileAppUtils.getStringFromObjects(entry, 1);
				String filesize = MobileAppUtils.getStringFromObjects(entry, 2);
				entryMap.put("fileid", pk_file);
				entryMap.put("filename", filename);
				//根据移动应用要求，文件大小显示为KB
				String filesize_convert = MobileAppUtils.getFileSize(new Integer(filesize).intValue());
				entryMap.put("filesize", filesize_convert);
				entryMap.put("downflag", "1");
				list.add(entryMap);
			}
		}
		
		Map<String, Object> resultMap = MobileAppUtils.createHashMap();
		if(col != null && col.size() > 0){
			resultMap.put("count", String.valueOf(col.size()));
		} else{
			resultMap.put("count", String.valueOf(0));
		}
		resultMap.put("attachstructlist", list);
		return resultMap;
	}

	@Override
	public List<Map<String, Object>> getTaskList(String groupid, String userid, String date,
			String statuskey, String statuscode, int startline, int count,
			String condition) throws BusinessException {
		ITaskType taskType = MobileAppUtils.getTaskType(statuskey, statuscode);
		
		TaskQuery query = taskType.createNewTaskQuery();

		// 设置TaskQuery的查询参数
		// 以便能生成id和pkSql
		query.setPk_group(groupid);
		query.setCuserid(userid);
		query.setDate(date);
		query.setTaskType(taskType);

		List<Map<String, Object>> list = PaginationQueryFacade.getInstance().query(query, condition, startline, count);
		
//		Map<String, Object> map = MobileAppUtils.createHashMap();
//		map.put("taskstructlist", list);
		
//		Integer taskcount = PaginationQueryFacade.getInstance().query(query,condition);
//		map.put("taskCount", taskcount);

		return list;
	}
	
	
	/**2018/03/17
	 * mxx
	 * 
	 * 增加自定义标题代码,筛选功能
	 */
	/**
	 * 
	 * 有筛选功能的getTaskList 筛选条件pk_org,billmaker
	 * mxx
	 * 
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> getTaskList(String groupid, String userid, String date,
			String statuskey, String statuscode, int startline,int count,
			String condition,String[] pkOrg,String[] billmaker,String[] moudles,String datetype) throws BusinessException {
		ITaskType taskType = MobileAppUtils.getTaskType(statuskey, statuscode);
		// 查询单据类型名称
        
		TaskQuery query = taskType.createNewTaskQuery();
		// 设置TaskQuery的查询参数
		// 以便能生成id和pkSql
		query.setPk_group(groupid);
		query.setCuserid(userid);
		query.setDate(date);
		query.setTaskType(taskType);
		List<Map<String, Object>> list = PaginationQueryFacade.getInstance().query(query,startline,count, condition,pkOrg,billmaker,moudles,statuskey,datetype);
		return TitleDefUtil.getList(list, query);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> getTaskList(String groupid, String userid, String date,
			String statuskey, String statuscode, int startline,int count,
			String condition,String[] pkOrg,String[] billmaker,String[] moudles) throws BusinessException {
		ITaskType taskType = MobileAppUtils.getTaskType(statuskey, statuscode);
		// 查询单据类型名称
        
		TaskQuery query = taskType.createNewTaskQuery();
		// 设置TaskQuery的查询参数
		// 以便能生成id和pkSql
		query.setPk_group(groupid);
		query.setCuserid(userid);
		query.setDate(date);
		query.setTaskType(taskType);
		List<Map<String, Object>> list = PaginationQueryFacade.getInstance().query(query,startline,count, condition,pkOrg,billmaker,moudles,statuskey);
		return TitleDefUtil.getList(list, query);
	}
	
	 /**
     * 
     * 查询当前操作人的所有单据中的组织以及制单人
     * @param groupid
     * @param userid
     * @param date
     * @param statuskey
     * @param statuscode
     * @param condition
     * @return
     * @throws BusinessException
     */
	@Override
	public Map<String,List<FilterModel>> getOrgAndBillmaker(String groupid, String userid, String date,
			String statuskey, String statuscode,String condition) throws BusinessException{
		ITaskType taskType = MobileAppUtils.getTaskType(statuskey, statuscode);
		// 查询单据类型名称
        
		TaskQuery query = taskType.createNewTaskQuery();
		// 设置TaskQuery的查询参数
		// 以便能生成id和pkSql
		query.setPk_group(groupid);
		query.setCuserid(userid);
		query.setDate(date);
		query.setTaskType(taskType);
		Map<String, List<FilterModel>> list=null;
		try {
			list = PaginationQueryFacade.getInstance().queryOrgAndBillmaker(query, condition,statuskey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	
	
	
	@Override
	public Integer getTaskListCount(String groupid, String userid, String date,
			String statuskey, String statuscode,
			String condition) throws BusinessException {
		ITaskType taskType = MobileAppUtils.getTaskType(statuskey, statuscode);
		
		TaskQuery query = taskType.createNewTaskQuery();

		// 设置TaskQuery的查询参数
		// 以便能生成id和pkSql
		query.setPk_group(groupid);
		query.setCuserid(userid);
		query.setDate(date);
		query.setTaskType(taskType);

//		List<Map<String, Object>> list = PaginationQueryFacade.getInstance().query(query, condition, startline, count);
		
		
		Integer taskcount = PaginationQueryFacade.getInstance().query(query,condition);
		

		return taskcount;
	}
	
	@Override
	public Integer getTaskListCount(String groupid, String userid, String date,
			String statuskey, String statuscode,
			String condition,String[] pkOrg,String[] billmaker,String[] moudles,String datetype) throws BusinessException {
		ITaskType taskType = MobileAppUtils.getTaskType(statuskey, statuscode);
		
		TaskQuery query = taskType.createNewTaskQuery();

		// 设置TaskQuery的查询参数
		// 以便能生成id和pkSql
		query.setPk_group(groupid);
		query.setCuserid(userid);
		query.setDate(date);
		query.setTaskType(taskType);

//		List<Map<String, Object>> list = PaginationQueryFacade.getInstance().query(query, condition, startline, count);
		
		
		Integer taskcount = PaginationQueryFacade.getInstance().query(query,condition,pkOrg,billmaker,moudles,statuskey,datetype);
		

		return taskcount;
	}
	
	@Override
	public Integer getTaskListCount(String groupid, String userid, String date,
			String statuskey, String statuscode,
			String condition,String[] pkOrg,String[] billmaker,String[] moudles) throws BusinessException {
		ITaskType taskType = MobileAppUtils.getTaskType(statuskey, statuscode);
		
		TaskQuery query = taskType.createNewTaskQuery();

		// 设置TaskQuery的查询参数
		// 以便能生成id和pkSql
		query.setPk_group(groupid);
		query.setCuserid(userid);
		query.setDate(date);
		query.setTaskType(taskType);

//		List<Map<String, Object>> list = PaginationQueryFacade.getInstance().query(query, condition, startline, count);
		
		
		Integer taskcount = PaginationQueryFacade.getInstance().query(query,condition,pkOrg,billmaker,moudles,statuskey);
		

		return taskcount;
	}

	@Override
	public TaskMetaData redoAgree(String groupid, String userid,
			String taskid, String note)
			throws BusinessException {
		TaskMetaData tmd = MobileAppUtils.queryTaskMetaData(ITaskType.CATEGORY_RECEIVED, ITaskType.RECEIVED_UNHANDLED, taskid);
		
		String billtype =tmd.getBillType(); 
		String billid = tmd.getBillId();
		String result = "Y";
		
		
		String[] assigned = null;
		try {
			ApproveTools4MABmCtrl.approveSilently(billtype, billid, result, note, userid, assigned,true);
		} catch (Exception e) {
			MobileAppUtils.handleException(e);
		}
		return tmd;
	}
	
}
