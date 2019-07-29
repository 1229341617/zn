package nc.bs.pub.taskmanager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfo;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.BusinessAppServer;
import nc.bs.logging.Logger;
import nc.bs.ml.NCLangResOnserver;
import nc.bs.pf.pub.PFRequestDataCacheProxy;
import nc.bs.pf.pub.PfDataCache;
import nc.bs.pf.pub.cache.ICacheDataQueryCallback;
import nc.bs.pub.taskmanager.racestrategy.RacemodalStrateyFactory;
import nc.bs.pub.taskmanager.task.RecurrenceFilter;
import nc.bs.pub.taskmanager.task.WFTaskRecorder;
import nc.bs.pub.taskmanager.workitem.ApprovableSMSDistributor;
import nc.bs.pub.taskmanager.workitem.EmailDistributor;
import nc.bs.pub.taskmanager.workitem.IWorkitemDistributor;
import nc.bs.pub.taskmanager.workitem.NoticeSMSDistributor;
import nc.bs.pub.taskmanager.workitem.PushableMsgDistributor;
import nc.bs.pub.taskmanager.workitem.V63ApprovableEmailDistributor;
import nc.bs.pub.taskmanager.workitem.V63ApprovableSMSDistributor;
import nc.bs.pub.taskmanager.workitem.V63NoticeEmailDistributor;
import nc.bs.pub.taskmanager.workitem.V63NoticeSMSDistributor;
import nc.bs.pub.wfengine.impl.ActionEnvironment;
import nc.bs.pub.wfengine.impl.WfDispatchUtils;
import nc.bs.trade.business.HYPubBO;
import nc.bs.wfengine.engine.EngineService;
import nc.bs.wfengine.engine.ProcessInstance;
import nc.bs.wfengine.engine.WFActivityContext;
import nc.bs.wfengine.engine.WorkflowRunner;
import nc.bs.wfengine.engine.ext.IPfParticipantService;
import nc.bs.wfengine.engine.persistence.EnginePersistence;
import nc.jdbc.framework.exception.DbException;
import nc.jdbc.framework.generator.SequenceGenerator;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.jzmobile.utils.BillTypeModelTrans;
import nc.jzmobile.utils.MobileMessageUtil;
import nc.vo.jzpm.jzsub10.SubContractVO;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pf.change.PfUtilBaseTools;
import nc.vo.pf.pub.util.ArrayUtil;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.mobile.MobileMsg;
import nc.vo.pub.msg.EmailMsg;
import nc.vo.pub.msg.MessageVO;
import nc.vo.pub.pf.AssignableInfo;
import nc.vo.pub.pf.AssignedUserList;
import nc.vo.pub.workflownote.WorkitemMsgConfigContext;
import nc.vo.uap.pf.FlowNextException;
import nc.vo.uap.pf.IRaceModalStrategy;
import nc.vo.uap.pf.PFBusinessException;
import nc.vo.uap.pf.PFRuntimeException;
import nc.vo.wfengine.core.activity.Activity;
import nc.vo.wfengine.core.activity.GenericActivityEx;
import nc.vo.wfengine.core.workflow.BasicWorkflowProcess;
import nc.vo.wfengine.core.workflow.MailModal;
import nc.vo.wfengine.core.workflow.MobileModal;
import nc.vo.wfengine.core.workflow.WorkflowProcess;
import nc.vo.wfengine.engine.ExecuteResult;
import nc.vo.wfengine.engine.exception.EngineException;
import nc.vo.wfengine.pub.WFTask;
import nc.vo.wfengine.pub.WfTaskOrInstanceStatus;
import nc.vo.wfengine.pub.WfTaskType;

import com.thimda.trans.A8SenderReceiver;

/**
 * 任务管理类
 * 
 * @author wzhy 2004-1-29
 * @modifier guowl 2008-4 重构任务管理器
 * @modifier leijun 2008-5 使用统一的参与者限定机制
 */
@SuppressWarnings("deprecation")
public class WfTaskManager {
	// 单例模式，全局实例
	private static WfTaskManager instance = new WfTaskManager();

	public static WfTaskManager getInstance() {
		return instance;
	}

	private WFTaskRecorder recorder = WFTaskRecorder.getInstance();

	private WfTaskManager() {
	}

	/**
	 * 初始化抢占的策略模式
	 * 
	 * @param task
	 * @return
	 * */
	private IRaceModalStrategy getRaceModalStrategy(WFTask task) {
		return RacemodalStrateyFactory.newinstance().getRaceModalStrategy(task);
	}

	/**
	 * 新建一个任务对象
	 * 
	 * @param pk_org
	 * @param iWftype
	 * @return
	 */
	public WFTask createTask(String pk_org, int iWftype) {
		WFTask task = new WFTask();
		task.setTaskPK(new SequenceGenerator().generate());
		task.setPk_org(pk_org);
		task.setCreateTime(new UFDateTime(InvocationInfoProxy.getInstance()
				.getBizDateTime()));
		task.setWorkflowType(iWftype);
		return task;
	}

	/**
	 * 从业务系统接收任务,并将任务发送给工作流系统
	 * 
	 * @param task
	 *            任务
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ExecuteResult acceptTaskFromBusi(WFTask task)
			throws BusinessException {
		Logger.info("###WfTaskManager acceptTaskFromBusi 开始 "
				+ System.currentTimeMillis() + "ms");
		Logger.info("WfTaskManager.acceptTaskFromBusi 开始从业务系统接收任务,taskPK="
				+ task.getTaskPK());
		Logger.info(">>billNo=" + task.getBillNO());

		ExecuteResult execResult = null;
		label429:
		try {
			recorder.setAcceptedTask(task);

			Integer taskType = task.getTaskType();
			// 从业务系统接收任务,更新任务状态,如果任务完成,将任务发送给工作流系统
			if (taskType == WfTaskType.Backward.getIntValue()) {
				// 驳回任务
				acceptBackwardTask(task);
				closeOaTask(task);
			} else if (taskType == WfTaskType.Withdraw.getIntValue()) {
				// 弃审任务
				acceptWithdrawTask(task);
			} else {
				// 正常任务
				acceptNormalTask(task);
			}

			if (task.getStatus() == WfTaskOrInstanceStatus.Finished
					.getIntValue()) {
				// 任务已完成(即本活动完成)，通知引擎流转
				execResult = syncDispatchTask(task);
				// 更新任务的一些信息，如新触发的流程实例PK等
				new TaskManagerDMO().saveOrUpdateTask(task, false);

				if (execResult != null) {
					// 将执行结果中无效活动的任务置为无效
					Vector vecInefficientActInstPKs = new Vector();
					execResult
							.getInefficientActivityInstances(vecInefficientActInstPKs);
					
					
					 //将执行结果中无效活动的任务对应的erp待办链接删除
			          if (taskType.intValue() == WfTaskType.Withdraw.getIntValue()) {
			        	  clearInValidateTasksLink(task, execResult);
			          }
					
					
					invalidTaskOfActInstPKs(vecInefficientActInstPKs);
					break label429;
				}
			} else {
				// 任务未完成(尚有工作项未完成)，将指派信息持久化到流程实例表的相关数据中
				Iterator<String> anokeys = task.getAssignNextOperatorsKeys();
				if (anokeys != null && anokeys.hasNext()) {
					EnginePersistence persistenceDmo = new EnginePersistence();
					ProcessInstance instance = persistenceDmo
							.loadProcessInstance(task.getWfProcessInstancePK());
					while (anokeys.hasNext()) {
						String key = anokeys.next();
						instance.getRelevantDatas().put(key,
								task.getAssignNextOperators(key));
					}
					persistenceDmo.saveGlobalVariables(
							instance.getProcessInstancePK(),
							instance.getRelevantDatas());
				}
				// 手工选择的转移信息
				Iterator<String> antKeys = task.getAssignNextTransitionKeys();
				if (antKeys != null && antKeys.hasNext()) {
					EnginePersistence persistenceDmo = new EnginePersistence();
					ProcessInstance instance = persistenceDmo
							.loadProcessInstance(task.getWfProcessInstancePK());
					while (antKeys.hasNext()) {
						String key = antKeys.next();
						instance.getRelevantDatas().put(key,
								task.getAssignNextTransition(key));
					}
					persistenceDmo.saveGlobalVariables(
							instance.getProcessInstancePK(),
							instance.getRelevantDatas());
				}
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new PFBusinessException(e.getMessage(), e);
		} finally {
			recorder.removeAcceptedTask(task);
		}

		Logger.info("WfTaskManager.acceptTaskFromBusi 结束从业务系统接收任务,taskPK="
				+ task.getTaskPK());
		Logger.info("###WfTaskManager acceptTaskFromBusi 结束  "
				+ System.currentTimeMillis() + "ms");

		return execResult;
	}
	
	private void clearInValidateTasksLink(WFTask task, ExecuteResult execResult) {
	  	List<WFTask> preTaskList = getPreTask(new TaskManagerDMO(), task, execResult);
	  	  if(preTaskList != null && preTaskList.size() != 0){
	  		  for(WFTask pretask : preTaskList){
	  	    	  closeOaTask(pretask);
	  	      } 
	  	  }
	  }

	/**
	 * 将无效活动的任务及其工作项置为无效
	 * 
	 * @param vecInefficientActInstPKs
	 * @throws BusinessException
	 */
	private void invalidTaskOfActInstPKs(Vector vecInefficientActInstPKs)
			throws DbException, BusinessException {
		TaskManagerDMO dmo = new TaskManagerDMO();

		// 1.置为无效状态
		if (vecInefficientActInstPKs.size() > 0) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < vecInefficientActInstPKs.size(); i++) {
				sb.append("'" + vecInefficientActInstPKs.get(i) + "',");
			}
			sb.deleteCharAt(sb.length() - 1);
			dmo.inefficientTasksByActInstPKs(sb.toString());
		}

		// 2.置为终止状态
	}

	/**
	 * 检查任务状态，是否可执行 <li>完成态、终止态的任务不可执行
	 * 
	 * @param task
	 * @return
	 * @throws BusinessException
	 */
	private boolean canTaskExecutable(WFTask task) throws BusinessException {
		WfTaskOrInstanceStatus tStatus = WfTaskOrInstanceStatus
				.fromIntValue(task.getStatus());
		switch (tStatus) {
		case Finished:
			throw new PFBusinessException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "UPPpfworkflow-000344")/*
																	 * @ res
																	 * "任务已完成"
																	 */);
		case Terminated:
			throw new PFBusinessException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "UPPpfworkflow-000345")/*
																	 * @ res
																	 * "任务已被终止"
																	 */);
		case Suspended:
			throw new PFBusinessException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "UPPpfworkflow-000346")/*
																	 * @ res
																	 * "任务已被挂起"
																	 */);
		default:
			break;
		}
		return true;
	}

	/**
	 * 处理正常任务
	 * 
	 * @param task
	 * @return
	 * @throws DbException
	 * @throws PFBusinessException
	 */
	private void acceptNormalTask(WFTask task) throws DbException,
			BusinessException {
		Logger.debug("****acceptNormalTask 开始处理正常任务****");

		TaskManagerDMO dmo = new TaskManagerDMO();
		WFTask oldTask = dmo.getTaskByPK(task.getTaskPK());

		boolean isFirstTask = false;
		if (oldTask == null) {
			// 新任务

			// yanke1 2013-4-18 对于新任务，将senderMan赋为operator是没有合理依据的
			// 因此注释掉
			// task.setSenderman(task.getOperator());
			isFirstTask = true;
			task.setTaskType(WfTaskType.Makebill.getIntValue());
			dmo.saveOrUpdateTask(task, true);
		} else if (canTaskExecutable(oldTask)) {
			// 正常处理任务
			if (!task.getAutoCompleted().booleanValue())
				dmo.updateWorkitemByTask(task);
		}

		// 判断该任务是否可完成，如果完成则对其工作项进行删除、或无效处理
		boolean isFinished = getRaceModalStrategy(task).checkTaskFinished(task);
		if (isFinished) {
			if (!isFirstTask)
				dmo.saveOrUpdateTask(task, false);
				/**清除erp待办任务*/
		        this.closeOaTask(task);
		} else {
			// XXX:该任务并未完成，则重新置为Started
			task.setStatus(WfTaskOrInstanceStatus.Started.getIntValue());
		}
		this.closeOaTask(task);
		
		Logger.debug("****acceptNormalTask 结束处理正常任务****");
	}

	/**
	 * 处理驳回任务
	 * 
	 * @param task
	 */
	private void acceptBackwardTask(WFTask task) throws BusinessException,
			DbException {
		Logger.debug("****acceptBackwardTask 开始处理驳回任务****");
		TaskManagerDMO dmo = new TaskManagerDMO();
		WFTask oldTask = dmo.getTaskByPK(task.getTaskPK());

		if (oldTask == null) {
			throw new PFBusinessException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "UPPpfworkflow-000256")/*
																	 * 流程任务不存在 ，
																	 * 无法驳回
																	 */);
		} else if (oldTask.getStatus() == WfTaskOrInstanceStatus.Finished
				.getIntValue()) {
			// 对于已完成的任务,无法驳回
			throw new PFBusinessException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "UPPpfworkflow-000257")/*
																	 * 任务已完成 ，
																	 * 无法驳回
																	 */);
		} else {
			// 更新任务
			dmo.saveOrUpdateTask(task, false);
			// 更新工作项
			dmo.inefficientWorkitemsOfTask(task);
			dmo.updateChecknote(task.getWorknoteVO().getPk_checkflow(),
					task.getNote());
		}
		Logger.debug("****acceptBackwardTask 结束处理驳回任务****");
	}

	/**
	 * 处理弃审任务
	 * 
	 * @param task
	 */
	private void acceptWithdrawTask(WFTask task) throws Exception {
		Logger.debug("****acceptWithdrawTask 开始处理弃审任务****");
		TaskManagerDMO dmo = new TaskManagerDMO();

		// 查出原有任务，及其工作项条数
		WFTask oldTask = dmo.getTaskByPK(task.getTaskPK());
		int count = dmo.queryWorkitemCountOfTask(task.getTaskPK());
		if (oldTask.getStatus() != WfTaskOrInstanceStatus.Finished
				.getIntValue() && count < 2) {
			// 单人任务未完成，不可弃审
			throw new PFBusinessException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "UPPpfworkflow-000258")/*
																	 * 该任务未完成 ，
																	 * 无法回退
																	 */);
		} else if (count > 1) {
			if (oldTask.getStatus() == WfTaskOrInstanceStatus.Finished
					.getIntValue()) {
				// 多人任务已完成
				// 更新任务，并无效该任务的工作项
				dmo.saveOrUpdateTask(task, false);
				dmo.inefficientWorkitemsOfTask(task);
			} else {
				// 多人任务未完成
				// XXX:该任务并未完成，则重新置为Started
				task.setStatus(WfTaskOrInstanceStatus.Started.getIntValue());
				task.setApproveResult(null);

				// 更新任务的该工作项->XXX:leijun@2009-9 废弃已办工作项，为本人新产生工作项
				// dmo.updateWorkitemByTask(task);
				dmo.inefficientWorkitemByPK(task.getWorknoteVO()
						.getPk_checkflow());
				dmo.renewWorkitem(task);
			}
		} else {
			// 单人任务已完成
			// 更新任务，并无效该任务的工作项
			dmo.saveOrUpdateTask(task, false);
			dmo.inefficientWorkitemsOfTask(task);
		}
		Logger.debug("****acceptWithdrawTask 结束处理弃审任务****");
	}
	
	 @SuppressWarnings("rawtypes")
	  private List<WFTask> getPreTask(TaskManagerDMO dmo, WFTask task, ExecuteResult execResult){
		  List<WFTask> preTasks = null;
		  
		  if(task == null || execResult == null){
			  return preTasks;
		  }
		  
		  Vector vecInefficientActInstPKs = new Vector();
		  execResult.getInefficientActivityInstances(vecInefficientActInstPKs);
	      try {
	      	List<String> pkTasks = getPkTask(vecInefficientActInstPKs, task);
	      	preTasks = new ArrayList<WFTask>();
	      	for(String pkTask : pkTasks){
	      		WFTask tempTask = dmo.getTaskByPK(pkTask);
	      		tempTask.setBillType(task.getBillType());
	      		preTasks.add(tempTask);
	      	}
	      	
	      	return preTasks;
	      } catch (Exception e) {
	      	e.printStackTrace();
	      	Logger.error("获取上一任务出错，详细信息如下："+e.getMessage());
	      }
	      
		  return preTasks;
	  }
	 
	 @SuppressWarnings("unchecked")
	  private List<String> getPkTask(Vector vecInefficientActInstPKs,WFTask task){
		  List<String> pkTasks = new ArrayList<String>();
		  StringBuffer sb = new StringBuffer();
		  
		  sb.append("select pk_wf_task from pub_wf_task where pk_wf_actinstance in ( ");
		  if (vecInefficientActInstPKs.size() > 0) {
			for (int i = 0; i < vecInefficientActInstPKs.size(); i++) {
				sb.append("'" + vecInefficientActInstPKs.get(i) + "',");
			}
			sb.deleteCharAt(sb.length() - 1);
		  }
		  sb.append(")");
		  
	      try {
	      	List<Map<String, Object>> dataList = (List<Map<String, Object>>) new BaseDAO()
	      												.executeQuery(sb.toString(), new MapListProcessor());
	      	for(Map<String, Object> map : dataList){
	      		if(!map.get("pk_wf_task").toString().equals(task.getTaskPK())){
	      			pkTasks.add(map.get("pk_wf_task").toString());
	      		}
	      	}
	      }catch (DAOException e) {
	      	e.printStackTrace();
	      }
	      return pkTasks;
	  }

	/**
	 * 产生新的任务和工作项
	 * 
	 * @throws BusinessException
	 * @modifier leijun 2007-3-8 处理"上级限定"的情形
	 */
	protected void sendTaskToBusi(WFTask task) throws BusinessException {
		try {
			// 1.save task first
			TaskManagerDMO dmo = new TaskManagerDMO();
			dmo.saveOrUpdateTask(task, true);

			// 2.create workitems
			if ((recorder.getAcceptedTask().getTaskType() == WfTaskType.Backward
					.getIntValue() || recorder.getAcceptedTask().getTaskType() == WfTaskType.Withdraw
					.getIntValue())
					&& task.getTaskType() != WfTaskType.Makebill.getIntValue()) {
				// yanke1 2013-3-1
				// 如果是驳回或弃审的任务，且至非制单环节
				// 那么取该环节上一次的处理人
				String[] checkmans = recorder.getPreviousReceivers(task);

				if (ArrayUtil.isNotNull(checkmans)) {
					createWorkitemsOfTask(task, checkmans);
				} else {
					getRaceModalStrategy(task).distributeWorkitems(task);
				}
			} else {
				getRaceModalStrategy(task).distributeWorkitems(task);
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new PFBusinessException(NCLangRes4VoTransl.getNCLangRes()
					.getStrByID("pfworkflow", "WfTaskManager-0001", null,
							new String[] { e.getMessage() })/*
															 * 持久化任务和工作项出现异常 ： {
															 * 0 }
															 */, e);
		}
	}

	/**
	 * 发送指派任务 <li>指派时已经做了参与者限定，所以这里不再做。但上级限定除外。
	 * 
	 * @throws DbException
	 * @throws BusinessException
	 */
	public void distributeAssignApproveTask(WFTask task) throws Exception {
		String[] checkmans = copyAssignedCheckmans(task);

		// 保存工作项
		createWorkitemsOfTask(task, checkmans);
	}

	/**
	 * @param task
	 * @return
	 */
	private String[] copyAssignedCheckmans(WFTask task) {
		String[] checkmans;
		// 如果活动找不到任何执行者，则抛出异常
		if (task.getAssignOperators() == null
				|| task.getAssignOperators().size() == 0)
			throw new PFRuntimeException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "UPPpfworkflow-000259")/*
																	 * @ res
																	 * "找不到执行人"
																	 */);

		AssignedUserList auList = new AssignedUserList();
		auList.addAssignedUsers(task.getAssignOperators());

		checkmans = auList.getUserIds();
		return checkmans;
	}

	/**
	 * 发送定制任务(操作人已确定)
	 * 
	 * @throws Exception
	 */
	public void distributeCustomApproveTask(WFTask task) throws Exception {
		if (task.getOperator() == null || task.getOperator().length() == 0)
			throw new PFRuntimeException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "UPPpfworkflow-000259")/*
																	 * @ res
																	 * "找不到执行人"
																	 */);

		createWorkitemsOfTask(task, task.parseOperators(task.getOperator()));
	}

	private InvocationInfo getInvocationInfo() {
		InvocationInfo info = new InvocationInfo();

		info.setBizDateTime(InvocationInfoProxy.getInstance().getBizDateTime());
		info.setGroupId(InvocationInfoProxy.getInstance().getGroupId());
		info.setGroupNumber(InvocationInfoProxy.getInstance().getGroupNumber());
		info.setLangCode(InvocationInfoProxy.getInstance().getLangCode());
		info.setUserDataSource(InvocationInfoProxy.getInstance()
				.getUserDataSource());
		info.setUserId(InvocationInfoProxy.getInstance().getUserId());

		return info;
	}

	private boolean needSendMobile(MobileModal mobileModal) {
		if (mobileModal == null)
			return false;
		int mobileModalValue = mobileModal.getValue();
		return mobileModalValue != MobileModal.NO_MOBILE_INT
				&& mobileModalValue != MobileModal.BLANK_INT;
	}

	private boolean needSendMail(MailModal mailModal) {
		if (mailModal == null)
			return false;
		int mailModalValue = mailModal.getValue();
		return mailModalValue != MailModal.NO_MAIL_INT
				&& mailModalValue != MailModal.BLANK_INT;
	}

	/**
	 * 发送会签任务
	 * 
	 * @throws DbException
	 * @throws BusinessException
	 */
	public void distributeAllApproveTask(WFTask task) throws Exception {
		String[] checkmans = null;

		try {
			IPfParticipantService ps = NCLocator.getInstance().lookup(
					IPfParticipantService.class);
			Set<String> ret = ps.getCheckmans(task);
			checkmans = ret == null ? null : ret.toArray(new String[0]);
		} catch (FlowNextException e) {
			// 该任务自动完成,流程继续流转
			autoCompleteTask(task);
			return;
		}

		// 保存工作项
		createWorkitemsOfTask(task, checkmans);

	}

	private void autoCompleteTask(WFTask task) {
		task.setStatus(WfTaskOrInstanceStatus.Finished.getIntValue());
		// task.setApproveResult("Y");
		task.setModifyTime(new UFDateTime(InvocationInfoProxy.getInstance()
				.getBizDateTime()));
		task.setOperator(task.getSenderman());
		task.setAutoCompleted(UFBoolean.TRUE);

		try {
			acceptTaskFromBusi(task);
		} catch (BusinessException ex) {
			Logger.error(ex.getMessage(), ex);
			String message = NCLangRes4VoTransl.getNCLangRes().getStrByID(
					"pfworkflow", "WfTaskManager-0003", null,
					new String[] { ex.getMessage() })/** 本人自动审批（自动流转 ）出现异常 ={0} */
			;
			throw new PFRuntimeException(message, ex);
		}
	}

	/**
	 * 为某个任务产生工作项，并发送邮件或短信
	 * 
	 * @param task
	 * @param userIds
	 * @throws DbException
	 * @throws BusinessException
	 */
	public void createWorkitemsOfTask(WFTask task, String[] userIds)
			throws Exception {
		recorder.recordReceivers(task.getTaskPK(), userIds);
		// 是否本人自动审批
		if (needIgnore(task)) {
			// 忽略重复审核人
			userIds = new RecurrenceFilter().doFilter(task, userIds);
		}
		if (userIds == null || userIds.length == 0) {
			return;
		}
		String BelongOrg = task.getParticipantBelongOrg();
		
		IWorkitemDistributor[] distributors = getDistributors(task, userIds);

		TaskManagerDMO dmo = new TaskManagerDMO();
		dmo.insertWorkitemsOfTask(userIds, task, distributors);
		if (("mock".equals(BelongOrg)) && ((task.getApproveResult() == null) || (!"R".equals(task.getApproveResult()))))
	    {
	      A8SenderReceiver sender = new A8SenderReceiver();
	      sender.send2A8(task.getTaskPK(), task.getBillType(), "", task.getBillID(), task.getSenderman(), task.getPk_org());
	    }
		// yanke1 2012-8-11 邮件和短信改用IWorkitemDistributor发送

		/*********************************** 添加代码开始 ******************************************/
		/**
		 * wss 添加发送移动消息推送代码
		 */
		this.sendToMobileApp(task, userIds);
		/*********************************** 添加代码结束 ******************************************/

	}

	private boolean needIgnore(WFTask task) throws BusinessException {
		if (!isAutoApprove(task)) {
			return false;
		}

		if (nextActNeedAssign(task)) {
			return false;
		}

		return true;
	}

	private boolean nextActNeedAssign(WFTask task) throws BusinessException {
		try {
			// yanke1 2013-3-21 后续环节需要指派的情况下，不忽略
			PfParameterVO paraVO = ActionEnvironment.getInstance().getParaVo(
					task.getBillversionPK());
			BasicWorkflowProcess process = PfDataCache.getWorkflowProcess(
					task.getWfProcessDefPK(), task.getWfProcessInstancePK());

			List<AssignableInfo> aiList = WfDispatchUtils.getAfterAssignInfos(
					task, process, (AggregatedValueObject) paraVO.m_billEntity);

			if (ArrayUtil.isNotNull(aiList)) {
				return true;
			}
		} catch (Exception e) {
			throw new BusinessException(e.getMessage(), e);
		}

		return false;
	}

	private IWorkitemDistributor[] getDistributors(WFTask task, String[] userIds) {
		List<IWorkitemDistributor> list = new ArrayList<IWorkitemDistributor>();

		try {
			if (isTaskPushable(task)) {
				list.add(new PushableMsgDistributor());
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}

		try {
			needEmailAndSMS(task, list);

			// wss
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}

		return list.toArray(new IWorkitemDistributor[0]);
	}

	private boolean isTaskPushable(WFTask task) throws DbException,
			BusinessException {
		WorkitemMsgConfigContext context = getConfigContext(task);
		return context.isWorkitemPushable();
	}

	public WorkitemMsgConfigContext getConfigContext(final WFTask task)
			throws BusinessException {
		return PFRequestDataCacheProxy.get(
				new WorkitemMsgConfigContext.CacheKey(task.getTaskPK()),
				new ICacheDataQueryCallback<WorkitemMsgConfigContext>() {

					@Override
					public WorkitemMsgConfigContext queryData()
							throws BusinessException {
						EngineService wfQry = new EngineService();
						BasicWorkflowProcess bwp = wfQry
								.findParsedMainWfProcessByInstancePK(task
										.getWfProcessInstancePK());

						WorkflowProcess wp = wfQry.queryWfProcess(
								task.getWfProcessDefPK(),
								task.getWfProcessInstancePK());
						Activity act = wp.findActivityByID(task.getActivityID());

						return new WorkitemMsgConfigContext(bwp,
								(GenericActivityEx) act);
					}
				});
	}

	@SuppressWarnings("deprecation")
	private void needEmailAndSMS(WFTask task,
			List<IWorkitemDistributor> distributorList)
			throws BusinessException {
		// yanke1 list直接用参数传进来, 这个设计很烂, 需要修正
		// 查询审批流定义
		// EngineService wfQry = new EngineService();
		// BasicWorkflowProcess bwp = wfQry
		// .findParsedMainWfProcessByInstancePK(task
		// .getWfProcessInstancePK());
		// String ptId = bwp.getMailPrintTemplet().getTempletid();
		// MailModal mailModal = bwp.getMailModal();
		// MobileModal mobileModal = bwp.getMobileModal();
		//
		// // 环节上定义的工作项接收方式
		// WorkflowProcess wp = wfQry.queryWfProcess(task.getWfProcessDefPK(),
		// task.getWfProcessInstancePK());
		// Activity act = wp.findActivityByID(task.getActivityID());
		// Object mailModalObj = CoreUtilities.getValueOfExtendedAttr(act,
		// XPDLNames.MAIL_MODAL);
		// Object mobileModalObj = CoreUtilities.getValueOfExtendedAttr(act,
		// XPDLNames.MOBILE_MODAL);
		// if (mailModalObj!=null &&
		// !MailModal.BLANK.getTag().equals(mailModalObj)) {
		// mailModal = MailModal.fromString((String) mailModalObj);
		// }
		// if (mobileModalObj!=null &&
		// !MobileModal.BLANK.getTag().equals(mobileModalObj)) {
		// mobileModal = MobileModal.fromString((String) mobileModalObj);
		// }

		WorkitemMsgConfigContext context = getConfigContext(task);

		MailModal mailModal = context.getMailModal();
		MobileModal mobileModal = context.getMobileModal();
		String ptId = context.getV61MailApproveTemplateId();

		if (context.isV63Mode()) {
			if (mailModal == MailModal.MAIL_APPROVE) {
				if (task.getTaskType() != WfTaskType.Makebill.getIntValue()) {
					distributorList
							.add(new V63ApprovableEmailDistributor(task));
				} else {
					distributorList.add(new V63NoticeEmailDistributor(task));
				}
			} else if (mailModal == MailModal.MAIL_INFO) {
				distributorList.add(new V63NoticeEmailDistributor(task));
			}

			if (mobileModal == MobileModal.MOBILE_APPROVE) {
				if (task.getTaskType() != WfTaskType.Makebill.getIntValue()) {
					distributorList.add(new V63ApprovableSMSDistributor(task));
				} else {
					distributorList.add(new V63NoticeSMSDistributor(task));
				}
			} else if (mobileModal == MobileModal.MOBILE_INFO) {
				distributorList.add(new V63NoticeSMSDistributor(task));
			}
		} else {
			if (needSendMail(mailModal)) {
				int iLastTaskType = task.getTaskType();
				String currentCheckman = iLastTaskType == WfTaskType.Makebill
						.getIntValue() ? null : task.getSenderman();
				EmailMsg em = new EmailMsg();
				em.setMailModal(mailModal);
				em.setBillId(task.getBillID());
				em.setBillNo(task.getBillNO());
				em.setBillType(PfUtilBaseTools.getRealBilltype(task
						.getBillType()));
				em.setPrintTempletId(ptId);
				em.setTopic(task.getTopic());
				em.setSenderman(currentCheckman);
				em.setTasktype(task.getTaskType());
				em.setInvocationInfo(getInvocationInfo());

				distributorList.add(new EmailDistributor(em, task));
			}
			if (needSendMobile(mobileModal)) {
				MobileMsg mm = new MobileMsg();
				mm.setMsg(MessageVO.getMessageNoteAfterI18N(task.getTopic()));

				if (mobileModal.getValue() == MobileModal.MOBILE_APPROVE_INT) {
					distributorList.add(new ApprovableSMSDistributor(task));
				} else {
					distributorList.add(new NoticeSMSDistributor(task));
				}
			}
		}
	}
	
	/**
	 * liuhuam 2019-03-04 审批流集成结束
	 */
	/**
	 * wss 2016-07-28 添加给移动端发送消息
	 */
	private void sendToMobileApp(WFTask task, String[] userIds) {
		try {
			// 是否是已经开发完成的移动审批单据 
			if (!MobileMessageUtil.judgeIsOABill(task.getBillType())) {
				return;
			}
			
			if(!judgeSendToOA(BillTypeModelTrans.getInstance().getModelByBillType(task.getBillType()).getBillTypeCode())) {
				return;
			}
			
			Logger.error("==========这里开始给移动端发消息==========");
			
			for(String userid : userIds){
				
				List<String> pk_checkflow = getSendPkCheckFlow(task.getTaskPK(),userid);
				if (pk_checkflow == null || pk_checkflow.size() == 0) {
					Logger.error("pk_checkflow 值为空！");
					return;
				}
				Properties prop = getOaUrlProp();
				if(prop == null) {
					return;
				}
				sendToOA(task,new String[]{userid}, prop,pk_checkflow.get(0));
			}
			
			//发送待办到oa
		} catch (Exception ex) {
			Logger.error("发送移动审批消息异常:\r\n" + ex.toString());
		}
		Logger.error("==========给移动端发消息结束==========");
	}
	
	  public String getBillTypeName(String billtype){
			try {
				String sql = "select billtypename from bd_billtype where pk_billtypecode='"+billtype+"'";
				Object billtypename = new BaseDAO().executeQuery(sql, new ColumnProcessor());
				if(billtypename == null || "".equals(billtypename)){
					return null;
				}else{
					return billtypename.toString();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	  
	
	/**
	 * liuhuam 2019-03-04 清除oa的待办任务,前提是经过判断,特定类型的才清除
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void closeOaTask(WFTask task) {
		try {
			//String pk_checkflow = getPk_checkflowByPk_wf_task(task.getTaskPK());
			
			// 是否是已经开发完成的移动审批单据 
			if (!MobileMessageUtil.judgeIsOABill(task.getBillType())) {
				return;
			}
			
			if(!judgeSendToOA(BillTypeModelTrans.getInstance().getModelByBillType(task.getBillType()).getBillTypeCode())) {
				return;
			}
			
			Logger.error("==========这里开始给移动端发消息==========");
			
			//处理驳回任务
			if(Integer.valueOf(task.getTaskType()).intValue() == WfTaskType.Backward.getIntValue()){
				
				List<String> pk_checkflow_temp = getClosePkCheckFlow(task.getBillID());
				
				if (pk_checkflow_temp == null || pk_checkflow_temp.size() == 0) {
					Logger.error("pk_checkflow 值为空！");
					return;
				}
				for(String pk:pk_checkflow_temp){
					clearTask(pk);
				}
				/*String sqlCond = "pk_wf_task=?"; 
				
				SQLParameter param = new SQLParameter();
				param.addParam(task.getTaskPK());
				BaseDAO dao = new BaseDAO();
				Collection<WorkflownoteVO> colWorknote = dao.retrieveByClause(WorkflownoteVO.class, sqlCond, param);
				for(Iterator iterator = colWorknote.iterator(); iterator.hasNext();){
					WorkflownoteVO workflownoteVO = (WorkflownoteVO)iterator.next();
					clearTask(workflownoteVO.getPk_checkflow());
				}*/
				return;
			}
			
			if(task.getWorknoteVO() == null){
				List<String> pk_checkflow_temp = getClosePkCheckFlow2(task.getTaskPK());
				if (pk_checkflow_temp == null || pk_checkflow_temp.size() == 0) {
					Logger.error("pk_checkflow 值为空！");
					return;
				}
				for(String pk:pk_checkflow_temp){
					clearTask(pk);
				}
				return ;
			}
			
			String pk_checkflow = task.getWorknoteVO().getPk_checkflow();
			if (pk_checkflow == null || pk_checkflow == "") {
				Logger.error("pk_checkflow 值为空！");
				return;
			}
			clearTask(pk_checkflow);
			/*for(String pk : pk_checkflow_temp){
				clearTask(task.getTaskPK());//发送待办到oa
				
			}*/
			//clearTask(task.getWorknoteVO().getPk_checkflow());//消除oa里面的待办任务
			/*for (String userid : userIds) {
				UserVO user = new UserVO();
				user.setCuserid(userid);
				user.setUser_code(MobileAppUtils.getUserCodeById(userid));
				MobileMessageUtil.sendMobileMessage(MessageVO.getMessageNoteAfterI18N(message.toString()), user, "0", messageUrl.toString());
			}*/
		} catch (Exception ex) {
			Logger.error("发送移动审批消息异常:\r\n" + ex.toString());
		}
		Logger.error("==========给移动端发消息结束==========");
	}
	
	private void clearTask(String pk_flow) {
		Properties prop = getOaUrlProp();
		String uri=getStr(prop, "oaWebserviceUrl2");//oa的erp待办服务地址
		try {
	        URL url = new URL(uri + "?id="+pk_flow+"");
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	
	        connection.setDoOutput(true); // 设置该连接是可以输出的
	        connection.setRequestMethod("GET"); // 设置请求方式
	        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
	        connection.setConnectTimeout(30000);//30秒
	        connection.setReadTimeout(30000);//30秒
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
	        String line = null;
	        StringBuilder result = new StringBuilder();
	        while ((line = br.readLine()) != null) { // 读取数据
	            result.append(line + "\n");
	        }
	        connection.disconnect();
	
	        System.out.println(result.toString());
		 } catch (Exception e) {
	         e.printStackTrace();
	         Logger.error("==========cleartask-网络连接异常==========" + e.getMessage());
	         Logger.info("==========cleartask-网络连接异常==========" + e.getMessage());
	         Logger.debug("==========cleartask-网络连接异常==========" + e.getMessage());
	     }
	}
	
	
/***************************************** 添加代码开始 ***********************************************/
	
	/**
	 * liuhuam 2019-03-04 审批流集成开始
	 */
	
	/**
	 * liuhuam 2019-03-04 发送审批流到oa,前提是经过判断,特定类型的才发送
	 * 注意:任何参数不能带有空格,需要使用replaceAll("\\s*", "")去除空格
	 */
	
	private void sendToOA(WFTask task, String[] userIds, Properties prop,String pk_checkflow) {

		String uri=getStr(prop, "oaWebserviceUrl");//oa的erp待办服务地址
		String iuapuri=getStr(prop, "iuapWebService"); //iuap项目弹出页面的服务器所在地址
		String mobileuri=getStr(prop, "mobileurl"); //iuap项目弹出页面的服务器所在地址
		Map<String,String> content=new HashMap<String, String>();
		try {
			
			//content = getWfnote(task.getSenderman(), task.getBillType(),task.getParticipantID());
			content = getWfnote(task.getSenderman(), task.getBillType(),userIds[0]);
		} catch (BusinessException e1) {
			e1.printStackTrace();
		}
		try {
			String topic = task.getTopic();
			String name = MessageVO.getMessageNoteAfterI18N(topic);
			/*String name=content.get("perName").toString()+","+task.getContext().getResult()+"{billno}:"+task.getBillNO()+",{please}"+task.getContext().getActionType();//待办任务名称*/			
			name=name.replaceAll("\\s*", "");
			String sendusername=content.get("perName").toString();//发送人名称
			String checkman=content.get("checkname").toString();//当前接收人
			String createdate=task.getCreateTime().getDate().toString().replace("\\s*", "-");//创建时间
			String empid=content.get("pkpsndoc").toString();//当前接收人bd_psndoc主键
			if ("".equals(empid)) {
				Logger.error("当前接收人为空！");
				return;
			}
			String type="ncsp";
			String username=content.get("usercode").toString();//当前人工号
			String company=content.get("orgname").toString();//当前人所在公司
			String staffcode=content.get("usercode").toString();//当前人工号
			String billid=task.getTaskPK();//当前任务id,只是在oa待办当作唯一标识
			/*String billurl=iuapuri + "?billtype="+task.getBillType()+"&billid="+task.getBillID()+"&checkman="+task.getParticipantID()+"&pk_flow="+billid+"&mobilebilltype="+task.getBillType()+
	        		"&workflowtype="+task.getTaskType()+"&pk_sender="+task.getSenderman();*/
			String billtypename = getBillTypeName(task.getBillType());
			billtypename = URLEncoder.encode(billtypename,"utf-8");
			checkman = URLEncoder.encode(checkman,"utf-8");
			createdate = URLEncoder.encode(createdate,"utf-8");
			String billurl=iuapuri + "?billtype="+task.getBillType()+"&billid="+task.getBillID()+"&checkman="+userIds[0]+"&pk_flow="+billid+"&mobilebilltype="+task.getBillType()+
	        		"&workflowtype="+task.getTaskType()+"&pk_sender="+task.getSenderman();
			String phoneUrl=mobileuri + "?billtype="+task.getBillType()+"&billid="+task.getBillID()+"&checkman="+userIds[0]+"&pk_flow="+billid+"&mobilebilltype="+task.getBillType()+
	        		"&workflowtype="+task.getTaskType()+"&billtypename="+billtypename+"&senderName="+checkman+"&senddate="+createdate+"&code="+username;
			name=URLEncoder.encode(name, "utf-8");
			sendusername = URLEncoder.encode(sendusername,"utf-8");
			company = URLEncoder.encode(company,"utf-8");
			billurl = URLEncoder.encode(billurl,"utf-8");
			phoneUrl = URLEncoder.encode(phoneUrl,"utf-8");
			URL url = new URL(uri + "?name="+name+"&createname="+sendusername+"&sendusername="+checkman+"&createdate="+createdate+"&empid="+empid+
	            		"&type="+type+"&username="+username+"&url="+billurl+"&company="+company+"&staffcode="+staffcode+"&billid="+billid+"&pk_checkflow="+pk_checkflow+"&phoneUrl="+phoneUrl+"");//"&pk_checkflow="+pk_checkflow+
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true); // 设置该连接是可以输出的
            connection.setRequestMethod("GET"); // 设置请求方式
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line = null;
            StringBuilder result = new StringBuilder();
            while ((line = br.readLine()) != null) { // 读取数据
                result.append(line + "\n");
            }
            connection.disconnect();

            System.out.println(result.toString()+"11");
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	
	
	private Properties getOaUrlProp() {
		BusinessAppServer baServer = BusinessAppServer.getInstance();
		String proFilePath = baServer.getServerBase() + "/ierp/jz/mobile.properties";
		if ( !new File(proFilePath).exists() ) {
			return null;
		}
		InputStream inputStream;
		try {
		    inputStream = new BufferedInputStream(new FileInputStream(proFilePath));
		} catch (FileNotFoundException e1) {
		    throw new RuntimeException(e1);
		}  
		Properties prop = new Properties();
		try {
		    prop.load(inputStream);
		} catch (IOException e) {
		    throw new RuntimeException(e);
		}
		return prop;
	}
	
	/**
	 * 根据sql查询单个值
	 */
	private String getUniqueValue(String sql, BaseDAO dao) throws DAOException {
		Object value = dao.executeQuery(sql, new ColumnProcessor());
		if(value != null) {
			return value.toString();
		}
		return "";
	}
	
	@SuppressWarnings({ "serial", "unchecked" })
	private List<String> getSendPkCheckFlow(String pk_task,String checkman){
		List<String> pkCheckFlows = new ArrayList<String>();
		BaseDAO dao = new BaseDAO();
		String sql = "select pk_checkflow from PUB_WORKFLOWNOTE where PK_WF_TASK = '"
				+ pk_task + "' and approvestatus  = 0 and checkman = '"+checkman+"'";
		try {
			pkCheckFlows = (List<String>)dao.executeQuery(sql.toString(),
					new ResultSetProcessor() {
				public Object handleResultSet(ResultSet rs) throws SQLException {
					List<String> temp_pkCheckFlows = new ArrayList<String>();
					while (rs.next()) {
						temp_pkCheckFlows.add(rs.getString("pk_checkflow"));
						return temp_pkCheckFlows;
					}
					return temp_pkCheckFlows;
				}
			});
		} catch (DAOException e) {
			Logger.error(e.getMessage());
		}
		return pkCheckFlows;
	}
	
	
	@SuppressWarnings({ "serial", "unchecked" })
	private List<String> getClosePkCheckFlow(String billid){
		
		List<String> pkCheckFlows = new ArrayList<String>();
		BaseDAO dao = new BaseDAO();
		String sql = "select pk_checkflow from pub_workflownote where billid = '"
				+ billid+"'";
		try {
			pkCheckFlows = (List<String>)dao.executeQuery(sql.toString(),
					new ResultSetProcessor() {
				public Object handleResultSet(ResultSet rs) throws SQLException {
					List<String> temp_pkCheckFlows = new ArrayList<String>();
					while (rs.next()) {
						temp_pkCheckFlows.add(rs.getString("pk_checkflow"));
						//return temp_pkCheckFlows;
					}
					return temp_pkCheckFlows;
				}
			});
		} catch (DAOException e) {
			Logger.error(e.getMessage());
		}
		return pkCheckFlows;
	}
	
	@SuppressWarnings({ "serial", "unchecked" })
	private List<String> getClosePkCheckFlow2(String taskPk){
		
		List<String> pkCheckFlows = new ArrayList<String>();
		BaseDAO dao = new BaseDAO();
		String sql = "select pk_checkflow from pub_workflownote where pk_wf_task = '"
				+ taskPk+"'";
		try {
			pkCheckFlows = (List<String>)dao.executeQuery(sql.toString(),
					new ResultSetProcessor() {
				public Object handleResultSet(ResultSet rs) throws SQLException {
					List<String> temp_pkCheckFlows = new ArrayList<String>();
					while (rs.next()) {
						temp_pkCheckFlows.add(rs.getString("pk_checkflow"));
						//return temp_pkCheckFlows;
					}
					return temp_pkCheckFlows;
				}
			});
		} catch (DAOException e) {
			Logger.error(e.getMessage());
		}
		return pkCheckFlows;
	}
	
	 public Boolean judgeSendToOA(String billtype) {
		   try {
			   String findpramsql = " select sp.paramname paramname,sp.paramvalue paramvalue from sm_paramregister sp "
					   				+ " left join sm_funcregister sf on sp.parentid=sf.cfunid " 
					   				+ " left join bd_billtype bd on bd.nodecode=sf.funcode "
					   				+ " where bd.pk_billtypecode='"+billtype+"' ";
				Object pmaplistobj = new BaseDAO().executeQuery(findpramsql, new MapListProcessor());
				if(pmaplistobj != null) {
					List<Map<String,String>> pmaplist = (List<Map<String,String>>)pmaplistobj;
					  for (int i = 0; i < pmaplist.size(); i++) {
						  String paramname = pmaplist.get(i).get("paramname");
						  if(paramname != null && "NCToOA".equals(paramname)) {
							  String paramvalue = pmaplist.get(i).get("paramvalue");
							  if(paramvalue != null && "Y".equals(paramvalue)) {
								  return true;
							  }
						  }
					}
				}
			} catch (DAOException e) {
				e.printStackTrace();
			}
		   	return false;
	   }
	 
	 private String getStr(Properties prop, String key) {
	        String value = (String) prop.get(key);
	        
	        try {
	        	if (value != null) {
	            	value = value.trim();
	            	return new String(value.getBytes("ISO-8859-1"), "UTF-8");
	            }
	        	return "";
	            
	        } catch (UnsupportedEncodingException e) {
	            return null;
	        }
	    }
	
	
	/**
	 * 根据人员id和类型查询人员名称和单据类型
	 * @param pk_task
	 * @return
	 */
	private Map<String,String> getWfnote(String sendman,String billtype) throws BusinessException{
		BaseDAO dao = new BaseDAO();
		
		Map<String, String> dataMap = new HashMap<String, String>();
		
		String sqlPerName="select USER_NAME from sm_user where CUSERID ='" +sendman+"'";
		
		String sqlBill="select billtypename from BD_BILLTYPE where pk_billtypecode='"+billtype+"'";
		
		String personName = (dao.executeQuery(sqlPerName.toString(),new ColumnProcessor())).toString();
		
		String billName = (dao.executeQuery(sqlBill.toString(),new ColumnProcessor())).toString();
		
		dataMap.put("perName", personName);
		
		dataMap.put("billName", billName);
		
		return dataMap;
	}
	
	private Map<String,String> getWfnote(String sendman,String billtype,String checknameid) throws BusinessException{
		BaseDAO dao = new BaseDAO();
		
		String sqlPerName="select USER_NAME from sm_user where CUSERID ='" +sendman+"'";
		String sqlBill="select billtypename from BD_BILLTYPE where pk_billtypecode='"+billtype+"'";
		String sqlcheckname="select USER_NAME from sm_user where CUSERID ='" +checknameid+"'";
		String sqlUsercode="select user_code from sm_user where CUSERID ='" +checknameid+"'";
		String sqlPkPsndoc="select pk_psndoc from sm_user where CUSERID ='" +checknameid+"'";
		String sqlOrgname="select name from org_orgs   where pk_org=(select pk_org from sm_user where CUSERID ='" +checknameid+"')";
		
		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("perName", getUniqueValue(sqlPerName.toString(), dao));
		dataMap.put("billName", getUniqueValue(sqlBill.toString(), dao));
		dataMap.put("checkname", getUniqueValue(sqlcheckname.toString(), dao));
		dataMap.put("usercode", getUniqueValue(sqlUsercode.toString(), dao));
		dataMap.put("orgname", getUniqueValue(sqlOrgname.toString(), dao));
		dataMap.put("pkpsndoc", getUniqueValue(sqlPkPsndoc.toString(), dao));
		
		return dataMap;
	}
	
	
	private String getPk_checkflowByPk_wf_task(String pk_task) {
		BaseDAO dao = new BaseDAO();
		String sql = "select PK_CHECKFLOW from PUB_WORKFLOWNOTE where PK_WF_TASK = '"
				+ pk_task + "'";
		String pk_checkflow = "";
		try {
			Object result = dao.executeQuery(sql.toString(),
					new ColumnProcessor());
			if (result != null) {
				pk_checkflow = result.toString();
			}
		} catch (DAOException e) {
			Logger.error(e.getMessage());
		}
		return pk_checkflow;
	}

	/***************************************** 添加代码开始 ***********************************************/

	private boolean isAutoApprove(WFTask task) {
		return new EngineService().isAutoApprove(task);
	}

	/**
	 * 同步处理任务，并返回执行结果 与syncDispatchChildTasks一起完成深度递归，并将执行结果放入结果链
	 */
	private ExecuteResult syncDispatchTask(WFTask task)
			throws BusinessException {
		Logger.info("###WfTaskManager syncDispatchTask 开始 "
				+ System.currentTimeMillis() + "ms");
		// 处理任务
		ExecuteResult result = process(task);

		// 处理子任务
		ExecuteResult childResult = syncDispatchChildTasks(task.getTaskPK());

		// 链接结果
		if (result == null && childResult != null) {
			result = childResult;
		} else if (result != null && childResult != null) {
			result.setNextResult(childResult);
		}

		Logger.info("###WfTaskManager syncDispatchTask 结束 "
				+ System.currentTimeMillis() + "ms");
		return result;
	}

	/**
	 * 处理子任务（深度递归），并将执行结果放入结果链
	 */
	private ExecuteResult syncDispatchChildTasks(String srcTaskPK)
			throws BusinessException {
		Logger.info("###WfTaskManager syncDispatchChildTasks 开始 "
				+ System.currentTimeMillis() + "ms");
		// 得到子任务
		WFTask[] childTasks = SynTaskTree.getChildTasks(srcTaskPK);
		ExecuteResult result = null;
		if (childTasks != null) {
			ExecuteResult tmpResult = null;
			try {
				for (int i = 0; i < childTasks.length; i++) {
					// 处理子任务
					tmpResult = syncDispatchTask(childTasks[i]);
					if (result == null && tmpResult != null) {
						result = tmpResult;
					} else if (result != null && tmpResult != null) {
						result.setNextResult(tmpResult);
					}
				}
			} finally {
				SynTaskTree.removeChildTasks(srcTaskPK);
			}
		}
		Logger.info("###WfTaskManager syncDispatchChildTasks 结束 "
				+ System.currentTimeMillis() + "ms");
		return result;
	}

	/**
	 * 处理任务，同步执行的需要返回执行结果
	 * 
	 * @param task
	 * @return
	 */
	private ExecuteResult process(WFTask task) throws BusinessException {
		Logger.info("###WfTaskManager process 开始 "
				+ System.currentTimeMillis() + "ms");
		// 构造上下文
		WFActivityContext wfActContext = new WFActivityContext(task);
		// 判断是否可执行
		String canNotReason = canExecute(wfActContext);
		if (canNotReason != null && canNotReason.length() > 0)
			throw new EngineException(canNotReason);

		WorkflowRunner runner = new WorkflowRunner(wfActContext);
		runner.execute();

		// 第一个任务要补齐信息
		if (task.getActivityInstancePK() == null) {
			task.setActivityInstancePK(wfActContext
					.getCurrentActivityInstance().getActivityInstancePK());
			task.setWfProcessInstancePK(wfActContext.getWfProcessInstancePK());
			// task.setActivityName(context.getCurrentActivity().getName());
		}

		if (runner.getInefficientActInstancePKs().size() > 0) {
			wfActContext.addExecuteResult(ExecuteResult.createResult(
					ExecuteResult.Result_Inefficient_ActivityInstance,
					runner.getInefficientActInstancePKs()));
		}

		// 处理WorkflowRunner运行后的任务
		dealInoutTasksAfterRunner(task, runner);

		Logger.info("###WfTaskManager process 结束 "
				+ System.currentTimeMillis() + "ms");
		return wfActContext.getExecuteResult();
	}

	private void dealInoutTasksAfterRunner(WFTask task, WorkflowRunner runner)
			throws BusinessException {
		WFTask[] outTasks = runner.getOutputTaskList().toArray(new WFTask[0]);
		WFTask[] inTasks = runner.getInputTaskList().toArray(new WFTask[0]);
		
		/**分包合同审批生效*/
	    if(outTasks == null || outTasks.length == 0) {
			HYPubBO bo = new HYPubBO();
			if(task.getBillType().contains("H5A1")) {
				SubContractVO subcontractvo = (SubContractVO)bo.queryByPrimaryKey(SubContractVO.class, task.getBillID());
				subcontractvo.setIcontstatus(1);
				bo.update(subcontractvo);
			}
		}
		
		// 保存新的任务和工作项
		for (int i = 0; i < (outTasks == null ? 0 : outTasks.length); i++) {
			outTasks[i].setInObject(task.getOutObject());
			sendTaskToBusi(outTasks[i]);
			if(i == (outTasks.length - 1)) {
				this.closeOaTask(task);
		    }
		}

		// 子任务，压入堆栈后继继续处理
		SynTaskTree.putChildTasks(task.getTaskPK(), inTasks);
	}

	/**
	 * 判断任务是否可以执行 如果是抽回任务，且活动实例为完成状态，处理此任务 如果流程实例已经处于结束或者驳回状态,则不处理此任务
	 * 如果活动实例已经处于结束、驳回或者抽回状态，则不处理此任务
	 */
	private String canExecute(WFActivityContext context) {
		String result = null;

		if (WfTaskOrInstanceStatus.Suspended.getIntValue() == context
				.getWfProcessInstance().getStatus()) {
			return NCLangResOnserver.getInstance().getStrByID("pfworkflow",
					"UPPpfworkflow-000317")/*
											 * @ res "流程已被挂起"
											 */;
		}

		if (context.getCurrentTask().getTaskType() == WfTaskType.Withdraw
				.getIntValue()) {
			// 弃审任务
			if (context.getCurrentActivityInstance().getStatus() == WfTaskOrInstanceStatus.Finished
					.getIntValue()) {
				return null;
			} else {
				// FIXME::??会签下的弃审岂不满足？
				return NCLangResOnserver.getInstance().getStrByID("pfworkflow",
						"UPPpfworkflow-000290")/*
												 * @ res "活动没有完成时,抽回没有意义"
												 */;
			}
		}

		result = context.getWfProcessInstance().getNotExecutableDesc();
		if (result != null)
			return result;

		// 判断活动实例状态是否可执行
		result = context.getCurrentActivityInstance().getNotExecutableDesc();
		if (result != null)
			return result;

		// if (context.getCurrentTask().getTaskType() ==
		// WfTaskType.Backward.getIntValue()) {
		// // 驳回任务
		// if (context.getCurrentActivityInstance().getStatus() ==
		// WfTaskOrInstanceStatus.Finished.getIntValue()) {
		// return null;
		// } else {
		// // FIXME::??会签下的弃审岂不满足？
		// return NCLangResOnserver.getInstance().getStrByID("pfworkflow",
		// "UPPpfworkflow-000290")/*
		// * @res
		// * "活动没有完成时,抽回没有意义"
		// */;
		// }
		// }
		// 判断流程实例状态是否可执行

		return null;
	}

}