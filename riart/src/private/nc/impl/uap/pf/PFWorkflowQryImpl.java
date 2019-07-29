package nc.impl.uap.pf;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.ml.NCLangResOnserver;
import nc.bs.pf.pub.BillTypeCacheKey;
import nc.bs.pf.pub.PfDataCache;
import nc.bs.pub.taskmanager.TaskManagerDMO;
import nc.bs.pub.workflownote.WorknoteManager;
import nc.bs.pub.workflowqry.impl.WorkflowRefactorCache;
import nc.bs.uap.pf.overdue.CalculatorContext;
import nc.bs.wfengine.engine.EngineService;
import nc.itf.bd.workcalendar.IWorkCalendarInnerService;
import nc.itf.pub.workflowqry.IWorkflowRefactor;
import nc.itf.uap.IUAPQueryBS;
import nc.itf.uap.pf.IPFMessageMetaService;
import nc.itf.uap.pf.IPFWorkflowQry;
import nc.itf.uap.pf.IWorkflowAdmin;
import nc.itf.uap.pf.IWorkflowDefine;
import nc.itf.uap.rbac.IUserManageQuery;
import nc.jdbc.framework.PersistenceManager;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.exception.DbException;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.ColumnListProcessor;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.message.vo.AttachmentVO;
import nc.pubitf.rbac.IUserPubService;
import nc.ui.pub.print.IDataSource;
import nc.vo.bd.pub.BDCacheQueryUtil;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.ml.MultiLangUtil;
import nc.vo.pf.msg.MessageMetaVO;
import nc.vo.pf.pub.util.ArrayUtil;
import nc.vo.pf.pub.util.SQLUtil;
import nc.vo.pf.pub.util.UserUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.billtype.BilltypeVO;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.msg.MessageVO;
import nc.vo.pub.pf.IPfRetCheckInfo;
import nc.vo.pub.pf.IWorkFlowStatus;
import nc.vo.pub.pf.Pfi18nTools;
import nc.vo.pub.pf.WfTaskInfo;
import nc.vo.pub.workflownote.WorkflownoteAttVO;
import nc.vo.pub.workflownote.WorkflownoteVO;
import nc.vo.pub.workflowqry.FlowAdminVO;
import nc.vo.pub.workflowqry.FlowHistoryPrintDataSource;
import nc.vo.pub.workflowqry.FlowHistoryQryResult;
import nc.vo.pub.workflowqry.FlowHistoryUtil;
import nc.vo.pub.workflowqry.WorkflowQueryResult;
import nc.vo.sm.UserVO;
import nc.vo.trade.summarize.Hashlize;
import nc.vo.trade.summarize.VOHashKeyAdapter;
import nc.vo.uap.historymsg.HistorymsgVO;
import nc.vo.uap.pf.PFBusinessException;
import nc.vo.uap.pf.WorkitemPrintDataOfBill;
import nc.vo.uap.rbac.util.RbacPubUtil;
import nc.vo.uap.wfmonitor.ProcessRouteRes;
import nc.vo.wfengine.core.activity.Activity;
import nc.vo.wfengine.core.activity.GenericActivityEx;
import nc.vo.wfengine.core.application.WfGadgetBodyVO;
import nc.vo.wfengine.core.application.WorkflowgadgetVO;
import nc.vo.wfengine.core.parser.XPDLNames;
import nc.vo.wfengine.core.parser.XPDLParserException;
import nc.vo.wfengine.core.util.CoreUtilities;
import nc.vo.wfengine.core.util.DurationUnit;
import nc.vo.wfengine.core.workflow.BasicWorkflowProcess;
import nc.vo.wfengine.core.workflow.WorkflowProcess;
import nc.vo.wfengine.definition.WorkflowDefinitionVO;
import nc.vo.wfengine.definition.WorkflowTypeEnum;
import nc.vo.wfengine.pub.WFTask;
import nc.vo.wfengine.pub.WfTaskOrInstanceStatus;
import nc.vo.wfengine.pub.WfTaskType;
import nc.vo.workflow.admin.FlowInstanceHistoryVO;
import nc.vo.workflow.admin.FlowInstanceOperation;

import org.apache.commons.lang.ArrayUtils;

/**
 * 工作流运行 查询接口
 * 
 * @author 雷军 2005-8-18
 *
 */
public class PFWorkflowQryImpl implements IPFWorkflowQry {

	private static Comparator<WorkflownoteVO> comparator = new Comparator<WorkflownoteVO>(){
		@Override
		public int compare(WorkflownoteVO o1, WorkflownoteVO o2) {
			  UFDateTime time1 = o1.getSenddate();
		      UFDateTime time2 = o2.getSenddate();
		      boolean isAfter = false;
		      if ((time2 != null) && (time1 != null)) {
		        isAfter = time2.after(time1);
		      }
		      if (isAfter) {
		        return -1;
		      }
		      if (time2.compareTo(time1) == 0) {
		        UFDateTime time3 = o1.getDealdate();
		        UFDateTime time4 = o2.getDealdate();
		        boolean isDTAfter = false;
		        if ((time4 != null) && (time3 != null)) {
		          isDTAfter = time4.after(time3);
		        }
		        if (isDTAfter) {
		          return -1;
		        }
		        return 1;
		      }
		      return 1;
		}
		
	};
	  
	
	public boolean isExistWorkflowDefinition(String billOrTranstype,
			String pkOrg, String operator, int iWorkflowOrApproveflow)
			throws BusinessException {
		String pk_group = InvocationInfoProxy.getInstance().getGroupId();
		WorkflowProcess workflowDef = new EngineService()
				.queryWfProcessCanStart2(billOrTranstype, pk_group, pkOrg,
						operator, -1,iWorkflowOrApproveflow);
		if (workflowDef == null) {
			return false;
		} else {
			return true;
		}
	}
	

	public Vector queryDataBySQL(String strSQL, int intCols, int[] FieldType)
			throws BusinessException {
		try {
			WorknoteManager noteDAO = new WorknoteManager();
			return noteDAO.queryDataBySQL(strSQL, intCols, FieldType);
		} catch (DbException e) {
			Logger.error(e.getMessage(), e);
			throw new PFBusinessException(NCLangResOnserver.getInstance().getStrByID("pfworkflow", "PFwfQryImpl-0000", null, new String[]{e.getMessage()})/*查询工作项表出现数据库异常：{0}*/);
		}
	}

	@Deprecated
	@Override
	public Vector queryAndRefactorDataBySql(String strSQL, int intCols,
			int[] fieldType, int qryType) throws BusinessException {
		Vector vt = queryDataBySQL(strSQL, intCols, fieldType);

//		if (vt != null && vt.size() > 0) {
//			new WFQryResultRefactorUtil().refactorQryResult(vt, qryType);
//		}
		return vt;
	}

	public boolean isCheckman(String billId, String billType, String userId)
			throws BusinessException {
		// 判断用户是否为当前单据的审批人
		EngineService wfQry = new EngineService();
		try {
			return wfQry.isCheckman(billId, billType, userId);
		} catch (DbException e) {
			Logger.error(e.getMessage(), e);
			throw new PFBusinessException(NCLangResOnserver.getInstance().getStrByID("pfworkflow", "PFwfQryImpl-0001", null, new String[]{e.getMessage()})/*判断用户是否为当前单据的审批人出现数据库异常：{0}*/);
		}
	}

	public String[] isCheckmanAry(String[] billIdAry, String billType,
			String userId) throws BusinessException {
		if (billIdAry == null || billIdAry.length == 0)
			return null;

		// 判断用户是否为某些单据的审批人
		EngineService wfQry = new EngineService();
		try {
			return wfQry.isCheckmanAry(billIdAry, billType, userId);
		} catch (DbException e) {
			Logger.error(e.getMessage(), e);
			throw new PFBusinessException(NCLangResOnserver.getInstance().getStrByID("pfworkflow", "PFwfQryImpl-0002", null, new String[]{e.getMessage()})/*判断用户是否为某些单据的审批人出现数据库异常：{0}*/);
		}
	}

	public WorkflownoteVO[] queryWorkitems(String billId, String billType,
			int iWfType, int allOrFinished) throws BusinessException {
		WorknoteManager noteMgr = new WorknoteManager();
		try {
			return noteMgr.queryAllByBillId(billId, billType, allOrFinished,
					iWfType);
		} catch (DbException e) {
			Logger.error(e.getMessage(), e);
			throw new BusinessException(e);
		}
	}

	public Object getApproveWorkitemPrintDs(String billId, String billType)
			throws BusinessException {
		return getApproveWorkitemPrintDs(billId,billType,true);
	}
	
	
//	public Object getApproveWorkitemPrintDs(String billId, String billType,boolean isAllorValidity)
//	        throws BusinessException {
//		WorkflownoteVO[] notes = queryWorkitems(billId, billType,
//				WorkflowTypeEnum.Approveflow.getIntValue(), 0);
//		if (notes == null || notes.length == 0)
//			return null;
//		if(isAllorValidity)
//			return generateDataSource(notes);
//		else{
//			//由于流程流转没有方向性。经讨论决定：环节中最后一次产生的wftask对应的workflownote为有效工作项(按ts判断)
//			List<String> taskPks =new ArrayList<String>();
//			Map<String,List<WorkflownoteVO>> taks2NoteMap =new HashMap<String,List<WorkflownoteVO>>();
//			for(WorkflownoteVO note:notes){
//				//作废的不显示。
//				if(note.getApprovestatus()==WfTaskOrInstanceStatus.Inefficient.getIntValue())
//					continue;
//				taskPks.add(note.getPk_wf_task());
//				if(taks2NoteMap.containsKey(note.getPk_wf_task())){
//					taks2NoteMap.get(note.getPk_wf_task()).add(note);
//				}else{
//					List<WorkflownoteVO> list =new ArrayList<WorkflownoteVO>();
//					list.add(note);
//					taks2NoteMap.put(note.getPk_wf_task(),list);
//				}
//			}
//			try {
//				Collection<WFTask> tasks =new TaskManagerDMO().queryTaskCollectionByCondition(SQLUtil.buildSqlForIn("pk_wf_task", taskPks.toArray(new String[0]))+" order by createTime");
//				Map<String,String> activityId2TaskMap =new LinkedHashMap<String,String>();
//				for(WFTask task:tasks){
//					if(activityId2TaskMap.containsKey(task.getActivityID())){
//						continue;
//					}else{
//						activityId2TaskMap.put(task.getActivityID(), task.getTaskPK());
//					}
//				}
//				List<WorkflownoteVO> result =new ArrayList<WorkflownoteVO>();
//				
//				for(String taskPK: activityId2TaskMap.values()){
//					result.addAll(taks2NoteMap.get(taskPK));
//				}
//				if(result.size()==0)
//					return null;
//				else 
//					return generateDataSource(result.toArray(new WorkflownoteVO[0]));
//				
//				
//			} catch (DbException e) {
//				throw new BusinessException(e);
//			}
//		}
//		
//	}
	


	/**
	 * yanke1 2011-10-21
	 * 根据查询到的数据填充打印数据源 对于UFDateTime格式的数据，此处不再处理为String，而是直接放到DS中
	 * 当客户端调用DS的getItemValueByExpress时，再根据客户端时区来处理
	 * 
	 * @param notes
	 * @return
	 */
//	private IDataSource generateDataSource(WorkflownoteVO[] notes) {
//		HashMap<String, Object[]> hmDatas = new HashMap<String, Object[]>();
//
//		int rowCount = notes.length;
//
//		// 单据类型名 - 从缓存中获取
//		BilltypeVO billtypeVO = PfDataCache.getBillType(new BillTypeCacheKey()
//				.buildBilltype(notes[0].getPk_billtype()).buildPkGroup(
//						InvocationInfoProxy.getInstance().getGroupId()));
//		String billtypeName = Pfi18nTools.i18nBilltypeNameByVO(
//				billtypeVO.getPk_billtypecode(), billtypeVO);
//		Object[] colValues = new String[rowCount];
//		for (int j = 0; j < rowCount; j++) {
//			colValues[j] = billtypeName;
//		}
//		hmDatas.put(WorkitemPrintDataOfBill.DATAITEM_BILLTYPE, colValues);
//
//		// 单据号
//		colValues = new String[rowCount];
//		for (int j = 0; j < rowCount; j++) {
//			colValues[j] = notes[0].getBillno();
//		}
//		hmDatas.put(WorkitemPrintDataOfBill.DATAITEM_BILLNO, colValues);
//
//		// 公司名称 - FIXME:直接取空？
//		colValues = new String[rowCount];
//		for (int j = 0; j < rowCount; j++) {
//			colValues[j] = "";
//		}
//		hmDatas.put(WorkitemPrintDataOfBill.DATAITEM_CORP, colValues);
//		
//		//审批状态
//		colValues = new String[rowCount];
//		for (int j = 0; j < rowCount; j++) {
//			colValues[j] = WfTaskOrInstanceStatus.fromIntValue(notes[j].getApprovestatus()).toString();
//		}
//		hmDatas.put(WorkitemPrintDataOfBill.DATAITEM_STATUS, colValues);
//		
//		// yanke1 2011-10-21
//		// 对于UFDateTime格式的数据，此处不再处理为String，而是直接放到DS中
//		// 当客户端调用DS的getItemValueByExpress时，再根据客户端时区来处理
//		// 发送时间
//		colValues = new Object[rowCount];
//		for (int j = 0; j < rowCount; j++) {
//			colValues[j] = notes[j].getSenddate();
//		}
//		hmDatas.put(WorkitemPrintDataOfBill.DATAITEM_SENDDATE, colValues);
//
//		// 发送人名称
//		colValues = new String[rowCount];
//		for (int j = 0; j < rowCount; j++) {
//			Object value = notes[j].getSendername();
//			colValues[j] = String.valueOf(value == null ? "" : value);
//		}
//		hmDatas.put(WorkitemPrintDataOfBill.DATAITEM_SENDMAN, colValues);
//
//		// 处理时间
//		colValues = new Object[rowCount];
//		for (int j = 0; j < rowCount; j++) {
//			colValues[j] = notes[j].getDealdate();
//		}
//		hmDatas.put(WorkitemPrintDataOfBill.DATAITEM_DEALDATE, colValues);
//
//		// @modifier yanke1 2011-4-1
//		// 历时
//		colValues = new String[rowCount];
//		IWorkCalendarInnerService wciService = NCLocator.getInstance().lookup(
//				IWorkCalendarInnerService.class);
//
//		for (int j = 0; j < rowCount; j++) {
//			UFDateTime beginTime = notes[j].getSenddate();
//			UFDateTime endTime = notes[j].getDealdate();
//			if (endTime == null) {
//				endTime = new UFDateTime(System.currentTimeMillis());
//			}
//			String elapsed = DurationUnit.getElapsedTimeInWorkCalendar(
//					notes[j].getPk_org(), beginTime, endTime);
//			colValues[j] = (elapsed == null ? "" : elapsed);
//		}
//		hmDatas.put(WorkitemPrintDataOfBill.DATAITEM_DURATION, colValues);
//
//		// 审批人名称
//		colValues = new String[rowCount];
//		for (int j = 0; j < rowCount; j++) {
//			Object value = notes[j].getCheckname();
//			colValues[j] = String.valueOf(value == null ? "" : value);
//		}
//		hmDatas.put(WorkitemPrintDataOfBill.DATAITEM_CHECKMAN, colValues);
//
//		// 审批人PK
//		colValues = new String[rowCount];
//		for (int j = 0; j < rowCount; j++) {
//			Object value = notes[j].getCheckman();
//			colValues[j] = String.valueOf(value == null ? "" : value);
//		}
//		hmDatas.put(WorkitemPrintDataOfBill.DATAITEM_PK_CHECKMAN, colValues);
//
//		// 审批批语
//		colValues = new String[rowCount];
//		for (int j = 0; j < rowCount; j++) {
//			Object value = MessageVO.getMessageNoteAfterI18N(notes[j]
//					.getChecknote());
//			colValues[j] = String.valueOf(value == null ? "" : value);
//		}
//		hmDatas.put(WorkitemPrintDataOfBill.DATAITEM_NOTE, colValues);
//
//		// 审批意见
//		colValues = new String[rowCount];
//		for (int j = 0; j < rowCount; j++) {
//			// 检查该工作项是否为修单
//			boolean isMakebill = false;
//			if (WorkflownoteVO.WORKITEM_TYPE_MAKEBILL.equalsIgnoreCase(notes[j]
//					.getActiontype()))
//				isMakebill = true;
//			Object value = WFTask.resolveApproveResult(isMakebill ? null
//					: notes[j].getApproveresult());
//			colValues[j] = String.valueOf(value == null ? "" : value);
//		}
//		hmDatas.put(WorkitemPrintDataOfBill.DATAITEM_APPROVERESULT, colValues);
//
//		return new WorkitemPrintDataOfBill(hmDatas);
//	}

	public boolean isApproveFlowStartup(String billId, String billType)
			throws BusinessException {
		// 判定单据是否启动了审批流实例，即处于有效状态的，即启动或完成状态
		HashSet<String> hsRet = new EngineService().hasInstanceOfValid(
				new String[] { billId }, billType,
				WorkflowTypeEnum.Approveflow.getIntValue());
		return hsRet.contains(billId);

	}

	public int queryFlowStatus(String billType, String billId,
			int iWorkflowOrApproveflow) throws BusinessException {
		boolean isWorkflow = iWorkflowOrApproveflow == WorkflowTypeEnum.Workflow
				.getIntValue();
		// 查询单据的工作流状态
		try {
			EngineService wfQry = new EngineService();
			int status = isWorkflow ? wfQry.queryWorkflowStatus(billId,
					billType) : wfQry.queryApproveflowStatus(billId, billType);
			switch (status) {
			case IPfRetCheckInfo.COMMIT:
				// 提交态
				return IWorkFlowStatus.NOT_STARTED_IN_WORKFLOW;
			case IPfRetCheckInfo.GOINGON:
				// 行中
				return IWorkFlowStatus.WORKFLOW_ON_PROCESS;
			case IPfRetCheckInfo.NOPASS:
				// 结束-不通过
				return IWorkFlowStatus.NOT_APPROVED_IN_WORKFLOW;
			case IPfRetCheckInfo.PASSING:
				// 结束-通过
				return IWorkFlowStatus.WORKFLOW_FINISHED;
			case IPfRetCheckInfo.NOSTATE:
				// 自由态
				if (!wfQry.isBilltypeHasFlowDef(billType,
						iWorkflowOrApproveflow)) {
					// 单据类型没有定义流程
					return IWorkFlowStatus.BILLTYPE_NO_WORKFLOW;
				} else {
					// 单据类型定义了流程，但本单据却没有走流程
					return IWorkFlowStatus.BILL_NOT_IN_WORKFLOW;
				}
			default:
				// 非法状态
				return IWorkFlowStatus.ABNORMAL_WORKFLOW_STATUS;
			}
		} catch (DbException e) {
			Logger.error(e.getMessage(), e);
			throw new PFBusinessException(NCLangResOnserver.getInstance().getStrByID("pfworkflow", "PFwfQryImpl-0003", null, new String[]{e.getMessage()})/*查询单据的流程状态出现数据库异常：{0}*/);
		}
	}

	@Override
	public String[] queryRealValuesOfGadgetParam(String billId,
			String billType, String varCode, int workflowOrApproveflow)
			throws BusinessException {
		EngineService wfQry = new EngineService();

		ArrayList<String> alRet = new ArrayList<String>();
		try {
			// 查询出某个单据走的审批流或工作流定义对象
			WorkflowProcess wp = wfQry.findProcessOfBill(billId, billType,
					workflowOrApproveflow);
			if (wp != null) {
				// 遍历其所有活动
				List listAct = wp.getActivities();
				for (Object obj : listAct) {
					Activity act = (Activity) obj;
					if (!(act instanceof GenericActivityEx))
						continue;
					// 找到某活动关联的单据组件，及其实参
					WorkflowgadgetVO gadgetVO = wfQry.findGadget(act, billType);
					if (gadgetVO == null)
						continue;

					// 根据参数编码，查找匹配的参数值
					for (WfGadgetBodyVO gBodyVO : gadgetVO.getBodys()) {
						if (gBodyVO.getVarcode().equals(varCode))
							alRet.add(gBodyVO.getValue());
					}
				}
			}
		} catch (DbException e) {
			Logger.error(e.getMessage(), e);
			throw new PFBusinessException(NCLangResOnserver.getInstance().getStrByID("pfworkflow", "PFwfQryImpl-0004", null, new String[]{e.getMessage()})/*查询单据的流程实例出现数据库异常：{0}*/);
		}

		return alRet.toArray(new String[0]);
	}

	@Override
	public WorkflownoteVO[] queryWorkitemsByUser(String userId,
			int iWorkflowOrApproveflow, int allOrFinished)
			throws BusinessException {
		String sql = "select a.checkman, a.pk_checkflow,a.pk_billtype,a.billno,a.actiontype,a.senderman,"
				+ "c.user_name,a.ischeck,a.checknote,a.senddate,a.dealdate,a.messagenote,a.pk_org,a.billid,a.priority,a.approvestatus,a.workflow_type"
				+ " from pub_workflownote a left join sm_user c on a.senderman=c.cuserid"
				+ " where a.checkman ='"
				+ userId
				+ "' and (a.approvestatus!="
				+ WfTaskOrInstanceStatus.Inefficient.getIntValue()
				+ ") and a.receivedeleteflag='N' ";
		if (iWorkflowOrApproveflow != 0)
			sql += "and a.workflow_type=" + iWorkflowOrApproveflow;
		if (allOrFinished == 1) {
			// 查未处理的
			sql += " and a.approvestatus="
					+ WfTaskOrInstanceStatus.Started.getIntValue()
					+ " and ischeck='N'";
		} else if (allOrFinished == 2) {
			sql += " and (a.approvestatus="
					+ WfTaskOrInstanceStatus.Finished.getIntValue()
					+ " or ischeck='Y')";
		}

		sql += " order by senddate desc";

		PersistenceManager persist = null;
		try {
			persist = PersistenceManager.getInstance();
			nc.jdbc.framework.JdbcSession jdbc = persist.getJdbcSession();
			ArrayList<WorkflownoteVO> lResult = (ArrayList<WorkflownoteVO>) jdbc
					.executeQuery(sql, new BeanListProcessor(
							WorkflownoteVO.class));
			return lResult == null ? null : lResult
					.toArray(new WorkflownoteVO[0]);
		} catch (DbException e) {
			Logger.error(e.getMessage(), e);
			throw new BusinessException(e);
		} finally {
			if (persist != null)
				persist.release();
		}
	}

	@Override
	public String[] queryWFPKsBySql(String sql) throws BusinessException {
		String[] pks = null;
		try {
			pks = (String[]) new BaseDAO().executeQuery(sql,
					new ResultSetProcessor() {

						@Override
						public Object handleResultSet(ResultSet rs)
								throws SQLException {
							List<String> list = new ArrayList<String>();
							while (rs.next()) {
								String pk = rs.getString(1);
								list.add(pk);
							}

							if (list.size() > 0) {
								return list.toArray(new String[0]);
							} else {
								return null;
							}
						}
					});
		} catch (DAOException e) {
			Logger.error(e.getMessage(), e);
			throw new BusinessException(e);
		}
		return pks;
	}

	@Override
	public boolean isWorkFlowStartup(String billId, String billType)
			throws BusinessException {
		// 判定单据是否启动了工作流实例，即处于有效状态的，即启动或完成状态
		HashSet<String> hsRet = new EngineService().hasInstanceOfValid(
				new String[] { billId }, billType,
				WorkflowTypeEnum.Workflow.getIntValue());
		return hsRet.contains(billId);
	}

	
	@Override
	public String[] getElapsedTimeInWorkCalendarBatch(String[] pk_orgs,
			UFDateTime[] beginTimes, UFDateTime[] endTimes)
			throws BusinessException {
		if (pk_orgs.length != beginTimes.length
				|| pk_orgs.length != endTimes.length) {
			throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("pfworkflow", "PFwfQryImpl-0005")/*参数不正确！ pks length: */ + pk_orgs.length
					+ ", beginTimes length: " + beginTimes.length
					+ ", endTimes length: " + endTimes.length);
		}

		String[] result = new String[pk_orgs.length];

		// yanke1 2012-8-2 历时的缓存
		// 仅对起始时间相同、结束时间相近的情况下有效
		CalculatorContext context = new CalculatorContext();
		
		for (int i = 0; i < pk_orgs.length; i++) {

			String duration = "";
			String pk_org = pk_orgs[i];
			UFDateTime begin = beginTimes[i];
			UFDateTime end = endTimes[i];

			if (begin == null || end == null) {
				duration = "";
			} else {

				if (StringUtil.isEmptyWithTrim(pk_org)) {
					duration = DurationUnit.getElapsedTime(begin, end);
				} else {
					
					// 从缓存中取一下
					int[] rawDuration = context.getDuration(pk_org, begin, end);
					
					if (rawDuration == null) {
						// 缓存中没有，那么调用工作日历计算
						rawDuration = DurationUnit.getElapsedTimeInWorkCalendarRaw(pk_org, begin, end);
						
						if (rawDuration != null && rawDuration.length == 4) {
							// 放到缓存中
							context.putDuration(pk_org, begin, end, rawDuration);
						}
					}
					
					if (rawDuration != null && rawDuration.length == 4) {
						// 根据int[4]构造字符串
						duration = DurationUnit.getStrElapsed(rawDuration[0], rawDuration[1], rawDuration[2], rawDuration[3]);
					} else {
						// 缓存中取到的rawDuration不符合规则
						// 那么调用工作日历计算
						duration = DurationUnit.getElapsedTimeInWorkCalendar(pk_org, begin, end);
					}
					
				}
			}
			result[i] = duration;
		}

		return result;
	}
	
	@Override
	public List<WorkflowQueryResult> queryFieldValues(String sql, String refactorClass) 
		throws BusinessException {
		BaseDAO dao = new BaseDAO();

		List<Object[]> list = (List<Object[]>) dao.executeQuery(sql, new ArrayListProcessor());

		IWorkflowRefactor refactor = WorkflowRefactorCache.getRefactorInstance(refactorClass);
		
		return refactor.refactorBS(list);
	}

	@Override
	public FlowInstanceHistoryVO[] queryFlowInstanceHistory(String pk_wf_instance) throws BusinessException {
		BaseDAO dao = new BaseDAO();
		
		String sfx =  MultiLangUtil.getCurrentLangSeqSuffix();
		
		String sql = "select h.*, u.user_name" + sfx + " as operatorname from pub_wf_instance_h h join sm_user u on h.operator=u.cuserid where h.pk_wf_instance=? order by operationdate desc";
		
		SQLParameter param = new SQLParameter();
		param.addParam(pk_wf_instance);
		
		List<FlowInstanceHistoryVO> list = (List<FlowInstanceHistoryVO>) dao.executeQuery(sql, param, new BeanListProcessor(FlowInstanceHistoryVO.class));
		
		
		for (FlowInstanceHistoryVO history : list) {
			Integer prevStatus = history.getPreviousStatus();
			Integer operation = history.getOperation();
			
			if (prevStatus != null) {
				history.setPreviousStatusEnum(WfTaskOrInstanceStatus.fromIntValue(prevStatus));
			}

			if (operation != null) {
				history.setOperationEnum(FlowInstanceOperation.fromIntValue(operation));
			}
		}
		
		
		return list.toArray(new FlowInstanceHistoryVO[0]);
	}

	@Override
	public boolean isExistWorkflowDefinitionWithEmend(String billOrTranstype,
			String pkOrg, String operator, int emendEnum,
			int iWorkflowOrApproveflow) throws BusinessException {
		String pk_group = InvocationInfoProxy.getInstance().getGroupId();
		WorkflowProcess workflowDef = new EngineService().queryWfProcessCanStart2(billOrTranstype, pk_group, pkOrg,operator, emendEnum,iWorkflowOrApproveflow);
		if (workflowDef == null) {
			return false;
		} else {
			return true;
		}
	}


	@Override
	public Map<String, List<WorkflownoteAttVO>> queryWorkitemAttBatch(String[] pk_checkflows) throws BusinessException {
		
		if (pk_checkflows == null || pk_checkflows.length == 0)
			return null;
		
		StringBuffer sb = new StringBuffer();
		
		
		for (String pk_checkflow : pk_checkflows) {
			sb.append(",");
			sb.append("'");
			sb.append(pk_checkflow);
			sb.append("'");
		}
		
		String sql = "pk_checkflow in (" + sb.substring(1) + ")";
		
		Collection<WorkflownoteAttVO> col = new BaseDAO().retrieveByClause(WorkflownoteAttVO.class, sql);
		
		if (col == null || col.size() == 0)
			return null;
		
		return Hashlize.hashlizeObjects(col.toArray(new WorkflownoteAttVO[0]), new VOHashKeyAdapter(new String[] {"pk_checkflow"}));
	}


	@Override
	public WFTask queryWFTaskByPk(String pk_wf_task) throws BusinessException {
		try {
			TaskManagerDMO dmo = new TaskManagerDMO();
			return dmo.getTaskByPK(pk_wf_task);
		} catch (DbException e) {
			throw new BusinessException(e);
		}
	}


	@Override
	public FlowAdminVO queryWorkitemForAdmin(String billtype, String billId, int flowType) throws BusinessException {
		WorkflownoteVO[] worknotes = queryWorkitems(billId, billtype, flowType,0);
		///////////////////////////////////////////
		this.setActivityIDs(worknotes);
		//////////////////////////////////////////
		
		String[] pks = ArrayUtil.getFieldValues(String.class, worknotes, "pk_checkflow");
		Map<String, List<WorkflownoteAttVO>> attMap = queryWorkitemAttBatch(pks);
		
		List<MessageMetaVO> metaList = NCLocator.getInstance().lookup(IPFMessageMetaService.class).queryMessageOfBill(billtype, billId);
		
		FlowAdminVO adminVo = new FlowAdminVO();
		adminVo.setWorkflowNotes(worknotes);
		adminVo.setWorkflowNoteAttVOs(attMap);
		adminVo.setMessageMetaVOs(metaList.toArray(new MessageMetaVO[0]));
		
		return adminVo;
	}
	
	@SuppressWarnings("unchecked")
	public void setActivityIDs(WorkflownoteVO[] worknotes) throws BusinessException{
		if(!ArrayUtils.isEmpty(worknotes)){
			StringBuffer sb = new StringBuffer();
			Map<String,String> activityIds = null;
			String pkTask = "";
			sb.append("select pk_wf_task,activitydefid from pub_wf_task where  ");
			List<String> pk_tasks =new ArrayList<String>();
			for(int start =0,end =worknotes.length;start<end;start++){
				if(!StringUtil.isEmpty(worknotes[start].getPk_wf_task())){
					pk_tasks.add(worknotes[start].getPk_wf_task());
				}
			}
			if(pk_tasks.size() >0){
				sb.append(SQLUtil.buildSqlForIn("pk_wf_task", pk_tasks.toArray(new String[0])));
				String sql = sb.toString();
				activityIds = (Map<String,String>) NCLocator.getInstance().lookup(IUAPQueryBS.class).executeQuery(sql, new ResultSetProcessor() {
					/**
					 * 
					 */
					private static final long serialVersionUID = -1708017425307675535L;

					@Override
					public Object handleResultSet(ResultSet rs)
					throws SQLException {
						Map<String,String> map = new HashMap<String,String>();
						while (rs.next()) {
							String key = rs.getString(1);
							String value = rs.getString(2);
							map.put(key, value);
						}
						if (map.size() > 0) {
							return map;
						} else {
							return null;
						}
					}
				});
			}
			for(WorkflownoteVO worknote : worknotes){
				//XXX TODO 非BIZ类型的workflownote.getPk_wf_task()可能为null么？
				String pk_task =worknote.getPk_wf_task();
				if(StringUtil.isEmptyWithTrim(pk_task))
					continue;
				String activityId = null;
				if(activityIds != null){
					activityId = activityIds.get(pk_task);
				}
				if(activityId!=null){
					worknote.setActivityID(activityId);
				}
			} 
		}
	}
	
	private Map findRelaProps(Activity act, String strBilltype) {
		HashMap map = new HashMap();
		// 单据组件及其实参
		map.put(XPDLNames.WORKFLOW_GADGET, new EngineService().findGadget(act, strBilltype));
		// 可编辑属性
		Object editableProps = CoreUtilities.getValueOfExtendedAttr(act,
				XPDLNames.EDITABLE_PROPERTIES);
		map.put(XPDLNames.EDITABLE_PROPERTIES, editableProps);
		// 可用按钮
		Object enableBtns = CoreUtilities.getValueOfExtendedAttr(act,
				XPDLNames.ENABLE_BUTTON);
		map.put(XPDLNames.ENABLE_BUTTON, enableBtns);
		// 业务参数
		if (act instanceof GenericActivityEx) {
			map.put(XPDLNames.APPLICATION_ARGS,
					((GenericActivityEx) act).getApplicationArgs());
		}
		// 是否可指派
		map.put(XPDLNames.IS_ASSIGN, CoreUtilities.canAssign(act));
		// 是否可改派
		map.put(XPDLNames.CAN_TRANSFER, CoreUtilities.canTransfer(act));
		// 是否可加签
		map.put(XPDLNames.CAN_ADDAPPROVER, CoreUtilities.canAddAprover(act));
		//是否CA认证
		map.put(XPDLNames.ELECSIGNATURE, ((GenericActivityEx)act).isElectSignature());
		
		return map;
	}


	@Override
	public WorkflownoteVO[] queryWorkitemsWithAttach(String billId,
			String billOrTranstype, int iWorkflowOrApproveflow,
			int allOrFinished) throws BusinessException {
		WorkflownoteVO[] noteVOs = queryWorkitems(billId, billOrTranstype,
				iWorkflowOrApproveflow, allOrFinished);
		if (noteVOs == null || noteVOs.length == 0)
			return noteVOs;
		
		//如果有代办工作项，进行特殊处理，附带wftask信息，避免侧边栏连接数超标
		//一个单据对于一个人，同一时刻只能有一个代办项？
		//判读流程是否被监控
		String userPk =InvocationInfoProxy.getInstance().getUserId();
		for(WorkflownoteVO vo:noteVOs){
			if(vo.getApprovestatus()==WfTaskOrInstanceStatus.Started.getIntValue()&&vo.getCheckman().equals(userPk)){
				TaskManagerDMO taskDmo = new TaskManagerDMO();
				try {
					WFTask currentTask = taskDmo.getTaskByPK(vo.getPk_wf_task());
					boolean flag = NCLocator.getInstance().lookup(IWorkflowAdmin.class).isAlreadyTracked(currentTask.getWfProcessInstancePK(), InvocationInfoProxy.getInstance().getUserId());
					currentTask.setBillType(vo.getPk_billtype());
					vo.setTaskInfo(new WfTaskInfo(currentTask));
					BasicWorkflowProcess wf =PfDataCache.getWorkflowProcess(currentTask.getWfProcessDefPK(), currentTask.getWfProcessInstancePK());
					vo.setWorkflow_type(wf.getWorkflowType());
					vo.setHideNoPassing(wf.isHideNoPassing());
					vo.getRelaProperties().putAll(findRelaProps(wf.findActivityByID(currentTask.getActivityID()),vo.getPk_billtype()));
					vo.setTrack(flag);
				} catch (Exception e) {
					Logger.error(e);
					throw new BusinessException(e);
				}
				break;
			}
		}
		
		
		Set<String> pks = new HashSet<String>();

		for (WorkflownoteVO vo : noteVOs) {
			pks.add(vo.getPrimaryKey());
		}

		Map<String, List<WorkflownoteAttVO>> attMap = queryWorkitemAttBatch(pks.toArray(new String[0]));

		if (attMap == null)
			return noteVOs;

		for (WorkflownoteVO vo : noteVOs) {
			List<WorkflownoteAttVO> noteAttVOs = attMap.get(vo.getPrimaryKey());

			if (noteAttVOs != null && noteAttVOs.size() > 0) {
				List<AttachmentVO> attVOList = new ArrayList<AttachmentVO>();

				// 将WorkflownoteAttVO转换为AttachmentVO放在WorkflownoteVO中
				for (WorkflownoteAttVO noteAttVO : noteAttVOs) {
					AttachmentVO attVO = new AttachmentVO();

					attVO.setPk_file(noteAttVO.getPk_file());
					attVO.setFilename(noteAttVO.getFilename());
					attVO.setFilesize(noteAttVO.getFilesize());

					attVOList.add(attVO);
				}

				vo.setAttachmentSetting(attVOList);
			}
		}

		return noteVOs;
	}


	@Override
	public String getStartWorkflowDef(String billOrTranstype, String pkOrg,
			String operator, int emendEnum, int iWorkflowOrApproveflow)
			throws BusinessException {
		String pk_group = InvocationInfoProxy.getInstance().getGroupId();
	       
		EngineService srv = new EngineService();
	       
		WorkflowProcess workflowDef = srv.queryWfProcessCanStart2(
				billOrTranstype, 
				pk_group, 
				pkOrg, 
				operator, 
				emendEnum, 
				iWorkflowOrApproveflow
			);
	        
        if(workflowDef!=null) {
	        return workflowDef.getPrimaryKey();
        } else {
            return null;
        }
	}
	
	@Override
	public boolean isNeedCASign4Batch(String userPK,String[] billorTranstype,
			String[] billIds) throws BusinessException {

		String  querySQL = " select pk_wf_instance,processdefid,activitydefid from pub_wf_task u1 join pub_workflownote  u2 on u1.pk_wf_task =u2.pk_wf_task  "
			              +" where  "+SQLUtil.buildSqlForIn("u2.pk_billtype", billorTranstype)+" and  "+SQLUtil.buildSqlForIn("u2.billversionpk", billIds)+" and checkman=? and ismsgbind='N'"
			              + " and u2.actiontype<>'"
						  + WorkflownoteVO.WORKITEM_TYPE_BIZ
						  + "' and u2.approvestatus in("
						  + WfTaskOrInstanceStatus.getUnfinishedStatusSet()
						  + ") order by u2.senddate";
	  BaseDAO dao =new BaseDAO();
	  SQLParameter param =new SQLParameter();
	  param.addParam(userPK);
	  ArrayList<Object[]> results = (ArrayList<Object[]>) dao.executeQuery(querySQL,param, new ArrayListProcessor());
	  if(ArrayUtil.isNull(results))
		  return false;
	  for(Object[] result :results){
		  String wf_instance =result[0].toString();
		  String wf_def =result[1].toString();
		  String act_def =result[2].toString();
		  try {
			BasicWorkflowProcess process =PfDataCache.getWorkflowProcess(wf_def, wf_instance);
			Activity act= process.findActivityByID(act_def);
			if(act instanceof GenericActivityEx){
				if(((GenericActivityEx)act).isElectSignature())
					return true;
			}
		} catch (XPDLParserException e) {
			Logger.error(e);
		}
	  }
	  
	  return false;
	  
	}


	@Override
	public String[] findLastPostTacheInfo(PfParameterVO paraVo)
			throws BusinessException {
	
		if(paraVo.m_workFlow==null)
			return null;
			
			String billVersionPK =paraVo.m_billVersionPK;
			String processInstance=paraVo.m_workFlow.getTaskInfo().getTask().getWfProcessInstancePK();
			BaseDAO dao =new BaseDAO();
			SQLParameter param  =new SQLParameter();
			String checkManSQL =" select senderman from pub_workflownote u1 join pub_wf_task u2 on u2.pk_wf_task =u1.pk_wf_task "
				            +" where u2.tasktype=? and u2.pk_wf_instance=? and u1.messagenote not like ? order by u2.ts desc";
			param.addParam(WfTaskType.Withdraw.getIntValue());
			param.addParam(processInstance);
			param.addParam("%unapproveBill%");
			ArrayList<String> checkManList =(ArrayList<String>) dao.executeQuery(checkManSQL, param, new ColumnListProcessor());
			if(checkManList == null || checkManList.size() == 0){
				return null;
			}
			
			String checkMan=checkManList.get(0);
			
			/**查询上个环节工作项的逻辑：
			 * 1以当前工作项的发送人为审核人，
			 * 2按单据VID和流程实例PK过滤，
			 * 3工作项作态为完成态，
			 * 4.
			 * 4按时间降序排列的第一个工作项。
			 * */
			String sql ="  select u1.dealdate ,u1.checknote from pub_workflownote u1  " 
					    +" where u1.billVersionPK =?  and u1.approvestatus =? and u1.checkman=? " 
					    +" and u1.actiontype=?  order by u1.ts desc";
			param =new SQLParameter();
			param.addParam(billVersionPK);
			param.addParam(WfTaskOrInstanceStatus.Finished.getIntValue());
			param.addParam(checkMan);
			param.addParam(WorkflownoteVO.WORKITEM_TYPE_APPROVE);
			
			ArrayList resultList =(ArrayList) dao.executeQuery(sql, param,new ArrayListProcessor());
			if(!ArrayUtil.isNull(resultList)){
				//按时间降序排列，只取最后一个
				Object[] result =(Object[]) resultList.get(0);
				String[] return2 =new String[2];
				return2[0] =result[0].toString();
				return2[1] =result[1].toString();
				return return2;
			}
			return null;		
	}
	
	@Override
	public Map<String,UserVO> findBillMakersByBillid(String[] billids)
			throws BusinessException {
		BaseDAO dao =new BaseDAO();
		String sql= "select billid,billmaker from pub_wf_instance where PROCSTATUS!="+WfTaskOrInstanceStatus.Inefficient.getIntValue()+" and "+SQLUtil.buildSqlForIn("billid", billids);
		ArrayList billMakerList = (ArrayList) dao.executeQuery(sql,new ArrayListProcessor());
		Map<String,List<String>> map =new HashMap<String,List<String>>();
		if(!ArrayUtil.isNull(billMakerList)){
			for(Object billMaker:billMakerList){
				Object[] result =(Object[]) billMaker;
				String billid =result[0]==null?null:result[0].toString();
				String billmaker =result[1]==null?null:result[1].toString();
				if(StringUtil.isEmptyWithTrim(billid)||StringUtil.isEmptyWithTrim(billmaker)){
					continue;
				}
				if(map.get(billmaker)==null){
					List<String> ids =new ArrayList<String>();
					ids.add(billid);
					map.put(billmaker, ids);
				}else{
					List<String> ids =map.get(billmaker);
					ids.add(billid);
				}
			}
		}
		Map<String ,UserVO> result =new HashMap<String ,UserVO>();
		if(map.keySet()!=null){
			UserVO[] uservos =NCLocator.getInstance().lookup(IUserPubService.class).getUsersByPKs(map.keySet().toArray(new String[0]));
			for(UserVO uservo:uservos){
				if(map.keySet().contains(uservo.getPrimaryKey())){
					List<String> billidArr =map.get(uservo.getPrimaryKey());
					for(String billid:billidArr){
						result.put(billid, uservo);
					}
				}
					
			}
		}
		
		return result;
	}


	@Override
	public WorkflownoteVO[] queryAllCheckInfo(String billid, String billtype) throws BusinessException {
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

	private static WorkflownoteVO[] sortWorkflownoteByDealTime(WorkflownoteVO[] noteVOs)
	  {
	    Arrays.sort(noteVOs, comparator);
	    return noteVOs;
	  }
	@Override
	public FlowHistoryQryResult queryFlowHistoryQryResult(String billtype,
			String billId, int flowType) throws BusinessException {
		FlowHistoryQryResult result = new FlowHistoryQryResult();

	    ProcessRouteRes processRoute = ((IWorkflowDefine)NCLocator.getInstance().lookup(IWorkflowDefine.class)).queryProcessRoute(billId, billtype, null, flowType);

	    result.setProcessRoute(processRoute);

	    int iBillStatus = queryFlowStatus(billtype, billId, flowType);
	    result.setiBillStatus(iBillStatus);

	    WorkflownoteVO[] noteVOs = queryWorkitems(billId, billtype, flowType, 0);
	    sortWorkflownoteByDealTime(noteVOs);
	    setActivityIDs(noteVOs);
	    HistorymsgVO[] historynsfVOs = FlowHistoryUtil.convertHistorymsgVOs(billtype, noteVOs);

	    result.setHistorymsgVOs(historynsfVOs);

	    Map attMap = queryWorkitemAttBatch((String[])ArrayUtil.getFieldValues(String.class, historynsfVOs, "pk_historymsg"));

	    result.setWorkflowNoteAttVOs(attMap);

	    List metaList = ((IPFMessageMetaService)NCLocator.getInstance().lookup(IPFMessageMetaService.class)).queryMessageOfBill(billtype, billId);

	    result.setMessageMetaVOs((MessageMetaVO[])metaList.toArray(new MessageMetaVO[metaList.size()]));

	    return result;
	}


	@Override
	public boolean isApproveFlowInstance(String billId, String billType) throws BusinessException {
		HashSet hsRet = new EngineService().hasApproveInstance(billId, billType, WorkflowTypeEnum.Approveflow.getIntValue());

	    Logger.debug("hsRetReturn : " + hsRet.contains(billId));
	    return hsRet.contains(billId);
	}


	@Override
	public List<String> queryFlowApprovers(String billId, String billType) throws BusinessException {
		 String sql = "select distinct checkman from pub_workflownote where billversionpk = ? and pk_billtype = ? and actiontype <> 'BIZ'";
		    SQLParameter param = new SQLParameter();
		    param.addParam(billId);
		    param.addParam(billType);
		    return ((List)new BaseDAO().executeQuery(sql, param, new ColumnListProcessor()));
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public HashMap<String, ArrayList<UserVO>> queryWfProcessForSupervisors(
			HashSet<String> pks) throws BusinessException {
		BaseDAO dao = new BaseDAO();
	    Collection<WorkflowDefinitionVO> vos = dao.retrieveByClause(WorkflowDefinitionVO.class, SQLUtil.buildSqlForIn("pk_wf_def", (String[])pks.toArray(new String[pks.size()])));
	    if ((null == vos) || (vos.size() == 0)) return null;
	    HashMap<String,String[]> supervisormap = new HashMap();
	    HashSet superSet = new HashSet();
	    for (WorkflowDefinitionVO vo : vos) {
	      String[] supervisor = null;
	      if (null != vo.getSupervisor()) {
	        if (vo.getSupervisor().startsWith("[U]")) {
	          vo.setSupervisor(vo.getSupervisor().replace("[U]", ""));
	        }
	        if (vo.getSupervisor().indexOf(",") > 1) {
	          supervisor = vo.getSupervisor().split(",");
	          for (String k : supervisor)
	            superSet.add(k);
	        }
	        else {
	          supervisor = new String[] { vo.getSupervisor() };
	          superSet.add(vo.getSupervisor());
	        }
	        supervisormap.put(vo.getPk_wf_def(), supervisor);
	      }
	    }
	    Object[] uservos = BDCacheQueryUtil.queryVOsByIDs(UserVO.class, "cuserid", (String[])superSet.toArray(new String[superSet.size()]), null);
	    UserVO[] users = (UserVO[])RbacPubUtil.convert(uservos, UserVO.class);
	    HashMap resultmap = new HashMap();

	    for (String key : supervisormap.keySet()) {
	      String supervisormapvalue = Arrays.toString((Object[])supervisormap.get(key));
	      ArrayList UserVOlist = new ArrayList();
	      for (UserVO vo : users) {
	        if (supervisormapvalue.contains(vo.getCuserid())) {
	          UserVOlist.add(vo);
	        }

	      }
	      resultmap.put(key, UserVOlist);
	    }
	    return resultmap;
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<UserVO> queryWfProcess(String processdefid, String pk_wf_instance)
			throws BusinessException {
		ArrayList userArr = new ArrayList();
	    WorkflowProcess wps = new EngineService().queryWfProcess(processdefid, pk_wf_instance);

	    ArrayList<String> supervisors = ((BasicWorkflowProcess)wps).getSupervisor();

	    if (supervisors != null) {
	      for (String pk : supervisors) {
	        if (pk.startsWith("[U]")) {
	          Object[] vos = BDCacheQueryUtil.queryVOsByIDs(UserVO.class, "cuserid", new String[] { pk.substring(3) }, null);

	          UserVO[] uservos = (UserVO[])RbacPubUtil.convert(vos, UserVO.class);
	          UserVO receiver = (ArrayUtils.getLength(uservos) == 0) ? null : uservos[0];

	          userArr.add(receiver);
	        } else if (pk.startsWith("[R]"))
	        {
	          IUserManageQuery qry = (IUserManageQuery)NCLocator.getInstance().lookup(IUserManageQuery.class);

	          UserVO[] reveivers = qry.queryUserByRole(pk.substring(3), null);
	          reveivers = UserUtil.filtDisableUsers(reveivers);
	          int i = 0; for (int end = (reveivers == null) ? 0 : reveivers.length; i < end; ++i) {
	            userArr.add(reveivers[i]);
	          }
	        }
	      }
	    }
	    return userArr;
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getApproveWorkitemPrintDs(String billId, String billType,
			boolean isAllorValidity) throws BusinessException {
		WorkflownoteVO[] notes = queryWorkitems(billId, billType, WorkflowTypeEnum.Approveflow.getIntValue(), 0);

	    if ((notes == null) || (notes.length == 0))
	      return null;
	    if (isAllorValidity) {
	      return new FlowHistoryPrintDataSource(notes);
	    }

	    List taskPks = new ArrayList();
	    Map taks2NoteMap = new HashMap();
	    for (WorkflownoteVO note : notes)
	    {
	      if (note.getApprovestatus().intValue() == WfTaskOrInstanceStatus.Inefficient.getIntValue()) {
	        continue;
	      }
	      taskPks.add(note.getPk_wf_task());
	      if (taks2NoteMap.containsKey(note.getPk_wf_task())) {
	        ((List)taks2NoteMap.get(note.getPk_wf_task())).add(note);
	      } else {
	        List list = new ArrayList();
	        list.add(note);
	        taks2NoteMap.put(note.getPk_wf_task(), list);
	      }
	    }
	    try
	    {
	      if (ArrayUtil.isNull(taskPks)) {
	        return null;
	      }

	      Collection<WFTask> tasks = new TaskManagerDMO().queryTaskCollectionByCondition(SQLUtil.buildSqlForIn("pk_wf_task", (String[])taskPks.toArray(new String[0])) + " order by createTime");

	      Map<String,String> activityId2TaskMap = new LinkedHashMap<String,String>();
	      for (WFTask task : tasks) {
	        if (activityId2TaskMap.containsKey(task.getActivityID())) {
	          continue;
	        }
	        activityId2TaskMap.put(task.getActivityID(), task.getTaskPK());
	      }

	      List result = new ArrayList();

	      for (String taskPK : activityId2TaskMap.values()) {
	        result.addAll((Collection)taks2NoteMap.get(taskPK));
	      }
	      if (result.size() == 0) {
	        return null;
	      }
	      return new FlowHistoryPrintDataSource((WorkflownoteVO[])result.toArray(new WorkflownoteVO[0]));
	    }
	    catch (DbException e)
	    {
	      throw new BusinessException(e);
	    }
	}

	
}

