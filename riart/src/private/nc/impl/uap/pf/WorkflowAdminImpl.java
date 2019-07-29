package nc.impl.uap.pf;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import nc.bs.pf.pub.cache.CondStringKey;
import nc.bs.pf.pub.cache.IRequestDataCacheKey;
import nc.bs.pf.pub.cache.WFTaskCacheKey;
import nc.bs.pub.mobile.PfEmailSendTask;
import nc.bs.pub.pf.MobileApproveTools;
import nc.bs.pub.pf.PfMailAndSMSUtil;
import nc.bs.pub.pf.PfMessageUtil;
import nc.bs.pub.pf.busistate.AbstractBusiStateCallback;
import nc.bs.pub.pf.busistate.PFBusiStateOfMeta;
import nc.bs.pub.taskmanager.TaskManagerDMO;
import nc.bs.pub.workflownote.WorknoteManager;
import nc.bs.pub.workflowpsn.WorkflowPersonDAO;
import nc.bs.uap.lock.PKLock;
import nc.bs.uap.oid.OidGenerator;
import nc.bs.uap.pf.overdue.WorkflowOverdueCalculator;
import nc.bs.wfengine.engine.EngineService;
import nc.bs.wfengine.engine.ext.IOrgFilter4Responsibility;
import nc.bs.wfengine.engine.ext.TaskTopicResolver;
import nc.bs.wfengine.engine.ext.org.filter.PfOrg4ResponsibilityFactory;
import nc.itf.uap.IUAPQueryBS;
import nc.itf.uap.pf.IPFConfig;
import nc.itf.uap.pf.IPFWorkflowQry;
import nc.itf.uap.pf.IWorkflowAdmin;
import nc.itf.uap.rbac.IUserManageQuery;
import nc.itf.uap.rbac.IUserManageQuery_C;
import nc.jdbc.framework.JdbcSession;
import nc.jdbc.framework.PersistenceManager;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.exception.DbException;
import nc.jdbc.framework.processor.BaseProcessor;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.jzmobile.utils.BillTypeModelTrans;
import nc.jzmobile.utils.MobileMessageUtil;
import nc.message.vo.MessageVO;
import nc.message.vo.NCMessage;
import nc.pubitf.rbac.IUserPubService;
import nc.uap.ws.message.MessageUtil;
import nc.ui.ml.NCLangRes;
import nc.ui.pf.multilang.PfMultiLangUtil;
import nc.vo.bd.psn.PsndocVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pf.msg.MessageMetaType;
import nc.vo.pf.msg.MessageMetaVO;
import nc.vo.pf.pub.util.ArrayUtil;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.msg.AbstractMsgL10NCallback;
import nc.vo.pub.msg.EmailMsg;
import nc.vo.pub.msg.FlowInstanceSettingVO;
import nc.vo.pub.msg.MessageTypes;
import nc.vo.pub.msg.MessageinfoVO;
import nc.vo.pub.pf.IPfRetCheckInfo;
import nc.vo.pub.pf.IWorkFlowStatus;
import nc.vo.pub.pf.Pfi18nTools;
import nc.vo.pub.pf.plugin.ReceiverVO;
import nc.vo.pub.pf.workflow.IPFActionName;
import nc.vo.pub.workflownote.WorkflownoteVO;
import nc.vo.pub.workflownote.WorkitemappointVO;
import nc.vo.sm.UserVO;
import nc.vo.uap.pf.OrganizeUnitTypes;
import nc.vo.uap.pf.PFBusinessException;
import nc.vo.wfengine.core.activity.GenericActivityEx;
import nc.vo.wfengine.core.parser.XPDLParserException;
import nc.vo.wfengine.core.util.DurationUnit;
import nc.vo.wfengine.core.workflow.BasicWorkflowProcess;
import nc.vo.wfengine.core.workflow.MailModal;
import nc.vo.wfengine.core.workflow.WorkflowProcess;
import nc.vo.wfengine.definition.WorkflowTypeEnum;
import nc.vo.wfengine.engine.ProcessInstanceAVO;
import nc.vo.wfengine.engine.exception.EngineException;
import nc.vo.wfengine.pub.ProcessInsSupervisorType;
import nc.vo.wfengine.pub.WFTask;
import nc.vo.wfengine.pub.WfTaskOrInstanceStatus;
import nc.vo.wfengine.pub.WfTaskType;
import nc.vo.wfengine.pub.WorkitemMsgContext;
import nc.vo.workflow.admin.FlowInstanceHistoryVO;
import nc.vo.workflow.admin.FlowInstanceOperation;
import nc.vo.workflow.admin.FlowOverdueVO;
import nc.vo.workflow.admin.FlowTimeSettingVO;
import nc.vo.workflow.admin.WorkflowManageContext;

/**
 * ������������������ӿ�ʵ����
 * 
 * @author dingxm 2009-5
 * @since 6.0
 */
public class WorkflowAdminImpl implements IWorkflowAdmin {

	public void appointWorkitem(String billId, String pkMsg, String checkman,
			String userID) throws BusinessException {
		this.appointWorkitem(billId, pkMsg, checkman, userID, null);
	}

	/**
	 * ����
	 * 
	 * checkman: ��ǰ������ userID�� ���ɽ�����
	 */
	public void appointWorkitem(String billId, String pkMsg, String checkman,
			String userID, String checkNote) throws BusinessException {
		// ���ɹ�������Ҫ�ȶԵ���ID���м�������
		boolean isNeedUnLock = PKLock.getInstance().addDynamicLock(billId);

		// ����ʧ�ܣ������쳣
		if (!isNeedUnLock)
			throw new PFBusinessException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "UPPpfworkflow-000602")/*
																	 * @res
																	 * "��ǰ�����ѽ��м�������"
																	 */);

		// ��鵱ǰ�������״̬
		final WorkflownoteVO noteVO = WorknoteManager.queryWorkitemByPK(pkMsg);
		/** yuyonga */
		if (noteVO == null
				|| noteVO.getApprovestatus() != WfTaskOrInstanceStatus.Started
						.getIntValue()) {
			throw new PFBusinessException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "wfAdminImpl-0000")/*
																 * �ô��칤�����Ѿ�ʧЧ,
																 * ��ˢ�½���
																 */);
		}
		/** end */

		// У���Ƿ���ɸ��û��ڵ�������������
		try {
			String[] users = filtrateUsers(noteVO.getPk_wf_task(),
					new String[] { userID });
			if (users == null || users.length == 0) {
				throw new PFBusinessException(NCLangResOnserver.getInstance()
						.getStrByID("pfworkflow", "wfAdminImpl-0001")/*
																	 * �����ɵ��û��Ѿ��Ǵ�������
																	 * ��
																	 */);
			}
		} catch (DbException e) {
			handleException(e, null);
		}

		WorkflownoteVO newNoteVO = (WorkflownoteVO) noteVO.clone();
		newNoteVO.setPrimaryKey(null);

		IUserManageQuery_C userManageQuery_C = NCLocator.getInstance().lookup(
				IUserManageQuery_C.class);
		UserVO senderMan = userManageQuery_C.getUser(newNoteVO.getSenderman());
		UserVO originCheckman = userManageQuery_C.getUser(checkman);
		final UserVO appointedCheckman = userManageQuery_C.getUser(userID);

		// ɾ��������Ϣ
		// WorknoteManager.deleteWorknote(noteVO);
		// yanke1 2013-3-25
		// ����ɾ����Ϣ�����ǽ�����Ϊ��Ч̬
		noteVO.setApprovestatus(WfTaskOrInstanceStatus.Inefficient
				.getIntValue());
		noteVO.setApproveresult(WFTask.APPROVERESULT_TRANSFER);
		noteVO.setChecknote(checkNote);
		noteVO.setDealdate(new UFDateTime(InvocationInfoProxy.getInstance()
				.getBizDateTime()));
		PfMultiLangUtil.doInDefaultLangBs(new Runnable() {

			@Override
			public void run() {
				// �޸�Ϊ����ռλ��
				// modified by liangyub 2013-06-06
				noteVO.setMessagenote("{0pfworkflow630098}"/* "[�Ѹ�����" */
						+ PfMultiLangUtil.getSuperVONameOfCurrentLang(
								appointedCheckman, "user_name") + "]"
						+ noteVO.getMessagenote());
			}
		});

		new BaseDAO().updateVO(noteVO);
		PfMessageUtil.setHandled(noteVO);

		String originActionType = newNoteVO.getActiontype();

		// �����и��ɵĲ����˷��ʹ�����Ϣ�͸���֪ͨ��Ϣ
		// ������Ϊ���������� �ռ���Ϊ��ǰ������
		// yanke1 2013-3-27 observer��Ҫ���ڴ�����
		// ���ﲻ����observer
		// newNoteVO.setObserver(noteVO.getCheckman());
		newNoteVO.setCheckman(checkman);
		
		Integer status = newNoteVO.getApprovestatus();
		newNoteVO.setApprovestatus(WfTaskOrInstanceStatus.Inefficient
				.getIntValue());

		newNoteVO.setActiontype(WorkflownoteVO.WORKITEM_TYPE_BIZ);

		String msgFormat = newNoteVO.getMessagenote();
		// ��Ϊ��ʽ���ܺ�custrationTopicһ�£���������
		String message = originCheckman.getUser_name() + "{appoint}"
				+ newNoteVO.billno + "{appointto}"
				+ appointedCheckman.getUser_name();
		newNoteVO.setMessagenote(message);
		newNoteVO.setSenddate(new UFDateTime(InvocationInfoProxy.getInstance()
				.getBizDateTime()));
		try {
			WorknoteManager.insertWorknote(newNoteVO);
		} catch (Exception e) {
			handleException(e, null);
		}

		// ����ָ�ɵ��˷��ʹ�����Ϣ��֪ͨ��Ϣ
		newNoteVO.setCheckman(userID);
		//�ķ�����Ϊ���ɷ�����
		newNoteVO.setSenderman(checkman);
		newNoteVO.setApprovestatus(status);

		NCMessage ncmsg = constructNCMsgOfAppointedNote(newNoteVO,
				senderMan.getCuserid(), userID);

		if (ncmsg != null) {
			MessageVO messageVO = ncmsg.getMessage();
			newNoteVO.setMessagenote(messageVO.getSubject());
			newNoteVO.setNcMsg(ncmsg);
		} else {
			//���¸���ʱ����Ϣ����
			String[] subTopics = msgFormat.split(",");
			String newMsgFormat = Pfi18nTools.getUserName(checkman) + " "
			+ "{appoint}" + "," + subTopics[1] + ","
			+ subTopics[2];
			newNoteVO.setMessagenote(newMsgFormat);
		}

		newNoteVO
				.setActiontype(originActionType == null ? WorkflownoteVO.WORKITEM_APPOINT_SUFFIX
						: originActionType
								+ (originActionType
										.endsWith(WorkflownoteVO.WORKITEM_APPOINT_SUFFIX) ? ""
										: WorkflownoteVO.WORKITEM_APPOINT_SUFFIX)); // ���ɱ��
		newNoteVO.setSenddate(new UFDateTime(InvocationInfoProxy.getInstance()
				.getBizDateTime()));
		String newPK = null;
		try {
			newPK = WorknoteManager.insertWorknote(newNoteVO);
		} catch (Exception e) {
			handleException(e, null);
		}

		// ������ɼ�¼
		WorkitemappointVO waVO = new WorkitemappointVO();
		waVO.setAppointee(userID);
		waVO.setAppointer(checkman);
		waVO.setPk_workitem(newPK);
		waVO.setOld_pk_workitem(pkMsg);
		new BaseDAO().insertVO(waVO);
		
		/**����oa������Ϣ*/
		TaskManagerDMO dmo = new TaskManagerDMO();
	    WFTask task = null;
		try {
			task = dmo.getTaskByPK(newNoteVO.getPk_wf_task());
			if(task != null){
				task.setBillType(newNoteVO.getPk_billtype());
				task.setTopic(newNoteVO.getMessagenote());
				task.setSenderman(checkman);
				task.setBillID(newNoteVO.getBillid());
			}
			sendToMobileApp(task,new String[]{userID},newNoteVO.getPk_checkflow(),noteVO.getPk_checkflow());
			
		} catch (DbException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		
		
	}
	
	private void clearTask(String pk_flow) {
		Properties prop = getOaUrlProp();
		String uri=getStr(prop, "oaWebserviceUrl2");//oa��erp��������ַ
		try {
	        URL url = new URL(uri + "?id="+pk_flow+"");
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	
	        connection.setDoOutput(true); // ���ø������ǿ��������
	        connection.setRequestMethod("GET"); // ��������ʽ
	        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
	        connection.setConnectTimeout(30000);//30��
	        connection.setReadTimeout(30000);//30��
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
	        String line = null;
	        StringBuilder result = new StringBuilder();
	        while ((line = br.readLine()) != null) { // ��ȡ����
	            result.append(line + "\n");
	        }
	        connection.disconnect();
	
	        System.out.println(result.toString());
		 } catch (Exception e) {
	         e.printStackTrace();
	     }
	}

	private NCMessage constructNCMsgOfAppointedNote(WorkflownoteVO noteVO,
			String senderId, String appointedCheckerID) {
		String tempcode = "";

		try {
			WFTask task = new TaskManagerDMO().getTaskByPK(noteVO
					.getPk_wf_task());
			task.setBillType(noteVO.getPk_billtype());

			String wfProcessDefPK = task.getWfProcessDefPK();
			WorkflowProcess wf = PfDataCache.getWorkflowProcess(wfProcessDefPK);

			tempcode = TaskTopicResolver.getMsgTempCode(wf, task);

		} catch (Exception e) {
			// �˴����ҵ�������Ĭ����Ϣģ�����
			// ���Ҳ�������ʹ��Ĭ����Ϣ��ʽ
			Logger.error(e.getMessage(), e);
			return null;
		}

		if (!StringUtil.isEmptyWithTrim(tempcode)) {

			try {
				WorkitemMsgContext context = new WorkitemMsgContext();

				context.setActionType("{appoint}");

				context.setBillid(noteVO.getBillid());
				context.setBillno(noteVO.getBillno());
				context.setBillType(noteVO.getPk_billtype());

				Object busiObj = NCLocator
						.getInstance()
						.lookup(IPFConfig.class)
						.queryBillDataVO(noteVO.getPk_billtype(),
								noteVO.getBillid());

				context.setBusiObj(busiObj);
				context.setCheckman(appointedCheckerID);
				context.setCheckNote(noteVO.getChecknote());

				context.setMsgtempcode(tempcode);
				context.setSender(senderId);
				context.setResult(getCheckResultOfNoteSenderman(
						noteVO.getBillVersionPK(), noteVO.getSenderman()));

				NCMessage ncmsg = TaskTopicResolver.constructNCMsg(context);

				return ncmsg;
			} catch (BusinessException e) {
				Logger.error(e.getMessage(), e);
				return null;
			}

		} else {
			return null;
		}
	}

	private String getCheckResultOfNoteSenderman(String billid, String senderman)
			throws BusinessException {
		String sql = "select approveresult from pub_workflownote where checkman=?"
				+ " and billversionpk=?"
				+ " and approvestatus=? order by senddate desc";

		SQLParameter param = new SQLParameter();
		param.addParam(senderman);
		param.addParam(billid);
		param.addParam(1);

		Object approveResult = new BaseDAO().executeQuery(sql, param,
				new ColumnProcessor("approveresult"));

		if (approveResult == null) {
			return "{commitBill}";
		} else {
			String result = String.valueOf(approveResult);

			if (result.equals("Y"))
				return "{checkPass}";
			if (result.equals("N"))
				return "{checkNoPass}";
			if (result.equals("R"))
				return "{rejectBill}";
		}

		return "{commitBill}";

	}

	@Override
	public void addApprover(final WorkflownoteVO worknoteVO)
			throws BusinessException {
		try {
			// ���˼�ǩѡ����û�
			String[] selectUsers = worknoteVO.getExtApprovers().toArray(
					new String[0]);
			String[] userIds = filtrateUsers(worknoteVO.getPk_wf_task(),
					selectUsers);
			if (userIds == null || userIds.length == 0
					|| selectUsers.length != userIds.length)
				throw new BusinessException(NCLangResOnserver.getInstance()
						.getStrByID("pfworkflow", "wfAdminImpl-0002")/* ��������û������ڻ��Ѿ��Ǵ������� */);

			WFTask task = null;
			TaskManagerDMO dmo = new TaskManagerDMO();
			if (worknoteVO.getTaskInfo() == null
					|| worknoteVO.getTaskInfo().getTask() == null) {
				// ���²�ѯtask(���̹����ﴦ���ǩ��ʱ�򴫵�workflownote����û��task)
				task = dmo.getTaskByPK(worknoteVO.getPk_wf_task());
			} else {
				task = worknoteVO.getTaskInfo().getTask();
			}
			// ����task��modifyTime �� ����ģʽ
			String modifyTime = new UFDateTime(System.currentTimeMillis())
					.toString();
			int processMode = task.getParticipantProcessMode();
			// ֻ�е��˲���Ҫ�ı�Task�Ĵ���ģʽ
			if (task.getParticipantProcessMode() == WFTask.ProcessMode_Single_Together) {
				processMode = WFTask.ProcessMode_Together;
			} else if (task.getParticipantProcessMode() == WFTask.ProcessMode_Single_Race) {
				processMode = WFTask.ProcessMode_Race;
			}

			// ����task����Ҫ�Ǹ��´���ģʽ ���޸�ʱ��
			String updateSql = "update  pub_wf_task  set   modifyTime = '"
					+ modifyTime + "' , processMode = " + processMode
					+ " where  pk_wf_task = '" + worknoteVO.getPk_wf_task()
					+ "'";
			new BaseDAO().executeUpdate(updateSql);

			// ���빤����
			List<WorkflownoteVO> noteVOList = new ArrayList<WorkflownoteVO>();
			for (int i = 0; i < userIds.length; i++) {
				WorkflownoteVO noteVO = (WorkflownoteVO) worknoteVO.clone();
				String[] checkManInfo = queryDynamicAgentOfCheckman(userIds[i],
								worknoteVO.getPk_billtype());
				noteVO.setActiontype(noteVO.getActiontype()
						+ WorkflownoteVO.WORKITEM_ADDAPPROVER_SUFFIX);
				noteVO.setCheckman(checkManInfo[0]);
				noteVO.setPk_checkflow(null);
				noteVO.setObserver(worknoteVO.getCheckman());

				String originTopic = noteVO.getMessagenote();
				String userID = noteVO.getSenderman();
				final String newTopic = uapdateMsgTopic(task, userID, originTopic);
				noteVO.setMessagenote(new AbstractMsgL10NCallback() {

					@Override
					public String getMessage() throws BusinessException {
						StringBuffer sb = new StringBuffer();
						sb.append("[");
						sb.append(NCLangResOnserver.getInstance()
								.getStrByID(
										"pfworkflow63",
										"WorkflowAdminImpl-0000",
										null,
										new String[] { Pfi18nTools
												.getUserName(worknoteVO
														.getCheckman()) })/*
																		 * ��ǩ��{0}
																		 */);
						sb.append("]");
						sb.append(newTopic);

						return newTopic;
					}
				}.getLocalizedMsg(noteVO.getCheckman()));

				noteVOList.add(noteVO);
			}

			PFRequestDataCacheProxy.put(new WFTaskCacheKey(task.getTaskPK()),
					task);
			WorknoteManager.insertWorknoteList(noteVOList);
			
			for(int i =0;i<userIds.length;i++){
				task.setTopic(noteVOList.get(i).getMessagenote());
				sendToMobileApp(task,new String[]{userIds[i]},noteVOList.get(i).getPk_checkflow(),null);
			}
			
		} catch (Exception e) {
			handleException(e, e.getMessage());
		}
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
	
	
	private String getUniqueValue(String sql, BaseDAO dao) throws DAOException {
		Object value = dao.executeQuery(sql, new ColumnProcessor());
		if(value != null) {
			return value.toString();
		}
		return "";
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
	
	private void sendToMobileApp(WFTask task, String[] userIds,String pk_checkflow,String flag) {
		try {
			// �Ƿ����Ѿ�������ɵ��ƶ��������� 
			if (!MobileMessageUtil.judgeIsOABill(task.getBillType())) {
				return;
			}
			
			if(!judgeSendToOA(BillTypeModelTrans.getInstance().getModelByBillType(task.getBillType()).getBillTypeCode())) {
				return;
			}
			
			Logger.error("==========���￪ʼ���ƶ��˷���Ϣ==========");
			
			for(String userid : userIds){
				
				/*List<String> pk_checkflow = getSendPkCheckFlow(task.getTaskPK(),userid);*/
				if (pk_checkflow == null || pk_checkflow == "") {
					Logger.error("pk_checkflow ֵΪ�գ�");
					return;
				}
				Properties prop = getOaUrlProp();
				if(prop == null) {
					return;
				}
				sendToOA(task,new String[]{userid}, prop,pk_checkflow);
				
				/**����ɾ��*/
				if(flag != null && !"".equals(flag)){
					clearTask(flag);
				}
				
			}
			
			//���ʹ��쵽oa
		} catch (Exception ex) {
			Logger.error("�����ƶ�������Ϣ�쳣:\r\n" + ex.toString());
		}
		Logger.error("==========���ƶ��˷���Ϣ����==========");
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
	
    private void sendToOA(WFTask task, String[] userIds, Properties prop,String pk_checkflow) {
	    
		String uri=getStr(prop, "oaWebserviceUrl");//oa��erp��������ַ
		String iuapuri=getStr(prop, "iuapWebService"); //iuap��Ŀ����ҳ��ķ��������ڵ�ַ
		String mobileuri=getStr(prop, "mobileurl"); //iuap��Ŀ����ҳ��ķ��������ڵ�ַ
		Map<String,String> content=new HashMap<String, String>();
		try {
			
			//content = getWfnote(task.getSenderman(), task.getBillType(),task.getParticipantID());
			content = getWfnote(task.getSenderman(), task.getBillType(),userIds[0]);
		} catch (BusinessException e1) {
			e1.printStackTrace();
		}
		try {
			String topic = task.getTopic();
			String name = nc.vo.pub.msg.MessageVO.getMessageNoteAfterI18N(topic);
			/*String name=content.get("perName").toString()+","+task.getContext().getResult()+"{billno}:"+task.getBillNO()+",{please}"+task.getContext().getActionType();//������������*/			
			name=name.replaceAll("\\s*", "");
			String sendusername=content.get("perName").toString();//����������
			String checkman=content.get("checkname").toString();//��ǰ������
			String createdate=task.getCreateTime().getDate().toString().replace("\\s*", "-");//����ʱ��
			String empid=content.get("pkpsndoc").toString();//��ǰ������bd_psndoc����
			if ("".equals(empid)) {
				Logger.error("��ǰ������Ϊ�գ�");
				return;
			}
			String type="ncsp";
			String username=content.get("usercode").toString();//��ǰ�˹���
			String company=content.get("orgname").toString();//��ǰ�����ڹ�˾
			String staffcode=content.get("usercode").toString();//��ǰ�˹���
			String billid=task.getTaskPK();//��ǰ����id,ֻ����oa���쵱��Ψһ��ʶ
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
	            		"&type="+type+"&username="+username+"&url="+billurl+"&company="+company+"&staffcode="+staffcode+"&billid="+billid+"&pk_checkflow="+pk_checkflow+"&phoneUrl="+phoneUrl+"");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true); // ���ø������ǿ��������
            connection.setRequestMethod("GET"); // ��������ʽ
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line = null;
            StringBuilder result = new StringBuilder();
            while ((line = br.readLine()) != null) { // ��ȡ����
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
	
	public static String[] queryDynamicAgentOfCheckman(String checkman,
			String pkBilltype) throws DbException {

		IRequestDataCacheKey key = new CondStringKey(IRequestDataCacheKey.CATEGORY_DYNAMICAGENT, new String[] { checkman, pkBilltype });
		Object cachedObj = PFRequestDataCacheProxy.get(key);

		String[] retStr = new String[] { checkman, "" };
		if (cachedObj == PFRequestDataCacheProxy.NULL) {
			// donothing
		} else if (cachedObj != null && cachedObj instanceof String[]) {
			retStr = (String[]) cachedObj;
		} else {
			WorkflowPersonDAO wpDao = new WorkflowPersonDAO();

			if (wpDao.isUserOut(checkman)) {
				// ��ȡ��̬������
				ArrayList dynAgentUsers = getDynamicAgents(checkman, pkBilltype);
				String agentUserId = wpDao.findFirstUserNotOut(dynAgentUsers,
						pkBilltype);
				if (!StringUtil.isEmptyWithTrim(agentUserId)) {
					retStr[0] = agentUserId;
					retStr[1] = "{agent}" + queryOperatorName(agentUserId);
				}
				// else
				// throw new
				// EngineException(NCLangResOnserver.getInstance().getStrByID("pfworkflow",
				// "UPPpfworkflow-000356")/* ��̻�������˲����ڲ����޴����˻�����˶����ڣ����ݲ��ܱ�������� */);
			}

			PFRequestDataCacheProxy.put(key, retStr);
		}

		return retStr;
	}
	/**
	 * �����û�ID����ѯ���û�����
	 *
	 * @param userId
	 * @return
	 * @throws EngineException
	 */
	private static String queryOperatorName(String userId) {
		nc.vo.sm.UserVO userVo = null;
		try {
			// ���Ҳ����˵�����
			userVo = NCLocator.getInstance().lookup(IUserManageQuery.class)
					.getUser(userId);
		} catch (BusinessException ex) {
			Logger.error(ex.getMessage(), ex);
			throw new EngineException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "UPPpfworkflow-000358" /*
																	 * @res
																	 * "���Ҳ����˴���:"
																	 */)
					+ ex.getMessage());
		}

		String userName = NCLangResOnserver.getInstance().getStrByID(
				"pfworkflow", "UPPpfworkflow-000359" /*
													 * @res "�޷�ʶ�����"
													 */);
		if (userVo != null)
			userName = userVo.getUser_name();

		return userName;
	}
	private static ArrayList getDynamicAgents(String userId, String billType)
			throws DbException {
		WorkflowPersonDAO wpDao = new WorkflowPersonDAO();
		return (ArrayList) wpDao.queryDynamicAgentVOs(userId, billType);
	}
	/**
	 * ��ǩʱͨ��ԭ����Ϣ���⣬������Ϣ����,���ݵ�ǰ
	 * add by liangyub 2013-08-27
	 * @param task ��ǰ����
	 * @param senderMan ��Ϣ������
	 * @param originalMsgTopic Դ��Ϣ����
	 * */
	private String uapdateMsgTopic(WFTask task, String senderMan,
			String originalMsgTopic) {
		// �ж��Ƿ�����Ϣģ�幹���˹�������Ϣ
		// �˴����ҵ�������Ĭ����Ϣģ�����
		// ���Ҳ�������ʹ��Ĭ����Ϣ��ʽ
		String tempcode = null;
		String newTopic = null;
		try {
			String wfProcessDefPK = task.getWfProcessDefPK();
			WorkflowProcess wf = PfDataCache.getWorkflowProcess(wfProcessDefPK);
			tempcode = TaskTopicResolver.getMsgTempCode(wf, task);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
		if (tempcode == null) {
			String[] subTopics = originalMsgTopic.split(",");
			// ������Ϣ����
			newTopic = Pfi18nTools.getUserName(senderMan) + " "
					+ "{addApprover}" + "," + subTopics[1] + "," + subTopics[2];
		} else {
			newTopic = Pfi18nTools.getUserName(senderMan) + " "
					+ "{addApprover}" + "," + "{billno}: " + task.getBillNO() 
					+ ",{please}{checkBill}";
		}
		return newTopic;
	}

	/**
	 * ���˼�ǩѡ����û������˵��ڵ�ǰtask��δ������������û�
	 * 
	 * @param worknoteVO
	 * @param dao
	 * @return
	 * @throws DbException
	 * @throws BusinessException
	 */
	private String[] filtrateUsers(String taskPk, String[] users)
			throws DbException, BusinessException {
		List<String> resultUsers = new ArrayList<String>();
		if (users == null || users.length == 0)
			return null;

		// ��ѯ���д�����������Ĵ�����
		// String sql =
		// "select checkman from  pub_workflownote where pk_wf_task = '"
		// + worknoteVO.getPk_wf_task() + "' and approvestatus = "
		// + WfTaskOrInstanceStatus.Started.getIntValue();
		TaskManagerDMO dmo = new TaskManagerDMO();
		WorkflownoteVO[] worknotes = dmo.queryStartedWorkitemsOfTask(taskPk);
		// WorkflownoteVO[] worknotes =
		// PfMessageUtil.queryWorkitemsByPropertyCond(new String[]
		// {"pk_wf_task", "approvestatus"}, new String[] {taskPk,
		// String.valueOf(WfTaskOrInstanceStatus.Started.getIntValue())});
		if (worknotes == null || worknotes.length == 0)
			return users;

		List<String> checkMans = new ArrayList<String>();
		for (WorkflownoteVO worknote : worknotes) {
			checkMans.add(worknote.getCheckman());
		}
		for (String approverUser : users) {
			if (!checkMans.contains(approverUser)) {
				resultUsers.add(approverUser);
			}
		}
		return resultUsers.toArray(new String[0]);
	}

	public void terminateWorkflow(String billid, String pkBilltype,
			String billNo, int iWorkflowtype) throws BusinessException {

		// �������������ǹ������������Թҵ�����������Զ���Ҫ����
		// add lock according to billid
		PKLock.getInstance().addDynamicLock(billid);

		// terminate process instance and rollback business
		try {
			if (iWorkflowtype == WorkflowTypeEnum.Approveflow.getIntValue()) {
				terminateApproveflow(billid, pkBilltype, billNo, null, null,
						false);
			} else if (iWorkflowtype == WorkflowTypeEnum.Workflow.getIntValue()) {
				terminateWorkflow(billid, pkBilltype, billNo, null, null);
			} else
				throw new PFBusinessException(NCLangResOnserver.getInstance()
						.getStrByID("pfworkflow", "wfAdminImpl-0004")/* �����˴���Ĳ���ֵiWorkflowtype */);

		} catch (Exception e) {
			handleException(e, null);
		}
	}

	/**
	 * ��ֹ������ʵ����������ҵ��ع� 2011.09.20 xry ���ӣ��ύ���������������ͣ�֧����ֹ
	 * 
	 * @param billid
	 * @param pkBilltype
	 * @param billNo
	 * @throws Exception
	 */
	private void terminateApproveflow(String billid, String pkBilltype,
			String billNo, String reason, AggregatedValueObject billVO,
			boolean autoApproveAfterCommit) throws Exception {
		// 1.�ж��õ����Ƿ��Ѿ�������ɣ�����ǣ����׳��쳣����
		EngineService es = new EngineService();
		int status = es.queryApproveflowStatus(billid, pkBilltype);
		if ((status == IPfRetCheckInfo.PASSING || status == IPfRetCheckInfo.NOPASS)
				&& !autoApproveAfterCommit)
			throw new PFBusinessException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "wfAdminImpl-0005")/*
																 * ���������������ѽ������޷���ֹ
																 * ��
																 */);

		// 2.�޸ĵ������ݿ�״̬Ϊ����̬
		// boolean hasMeta =
		// PfMetadataTools.checkBilltypeRelatedMeta(pkBilltype);
		AbstractBusiStateCallback absc = new PFBusiStateOfMeta();

		// ��ѯ������VOʵ��
		if (billVO == null) {
			IPFConfig pfcfg = (IPFConfig) NCLocator.getInstance().lookup(
					IPFConfig.class.getName());
			billVO = pfcfg.queryBillDataVO(pkBilltype, billid);
		}

		// ���칤��������VO
		PfParameterVO paraVo = construtParamVO(billid, pkBilltype, billNo,
				WorkflowTypeEnum.Approveflow.getIntValue());
		paraVo.m_workFlow = new WorkflownoteVO();
		paraVo.m_workFlow.setChecknote(WfTaskOrInstanceStatus.Terminated
				.toString());
		paraVo.m_workFlow.setActiontype(IPFActionName.TERMINATE);
		paraVo.m_preValueVo = billVO;
		WorknoteManager manager = new WorknoteManager();
		String processId = manager.getProcessId(paraVo,
				WorkflowTypeEnum.Approveflow.getIntValue());
		if (billVO != null) { // ����billVO == null�������Ѿ���ɾ����
			absc.execUnApproveState(paraVo, null, IPfRetCheckInfo.NOSTATE);
			// ҵ��ع�
			es.rollbackWorkflow(billid, pkBilltype, billVO,
					WorkflowTypeEnum.Approveflow.getIntValue());
		}

		// 3.��ѯ����״̬����pub_wf_instance_h�м�¼�˴β�������ʷ��¼
		List<Object[]> statusList = es.queryProcessStatus(billid, pkBilltype,
				WorkflowTypeEnum.Approveflow.getIntValue());
		List<FlowInstanceHistoryVO> historyList = new ArrayList<FlowInstanceHistoryVO>();

		for (Object[] statusRow : statusList) {
			String pk_wf_instance = String.valueOf(statusRow[0]);
			Integer procStatus = Integer.parseInt(String.valueOf(statusRow[1]));

			FlowInstanceHistoryVO history = new FlowInstanceHistoryVO();
			history.setPk_wf_instance(pk_wf_instance);
			history.setPreviousStatus(procStatus);
			history.setOperation(FlowInstanceOperation.TERMINATE.getIntValue());
			history.setReason(reason);
			history.setOperator(InvocationInfoProxy.getInstance().getUserId());
			history.setBilltype(pkBilltype);
			history.setBillid(billid);
			history.setBillno(billNo);

			// FIXME: �Ƿ��¼ҵ������?
			history.setOperationDate(new UFDateTime());

			historyList.add(history);
		}
		// 5.�������Ա������Ϣ(�ȷ���Ϣ�ɣ�������Ϣȡ����ʵ����Ϣ��ȡ����zhouwba��
		manager.sendMsgWhenWFstateChanged(paraVo, processId,
				WfTaskOrInstanceStatus.Terminated.getIntValue(),
				WorkflowTypeEnum.Approveflow.getIntValue());

		// 4.ɾ���õ�����ص�������Ϣ
		es.deleteWorkflow(billid, pkBilltype, false,
				WorkflowTypeEnum.Approveflow.getIntValue());

	}

	/**
	 * ��ֹ������ʵ����������ҵ��ع�
	 * 
	 * @param billid
	 * @param pkBilltype
	 * @param billNo
	 * @throws Exception
	 */
	private void terminateWorkflow(String billid, String pkBilltype,
			String billNo, String reason, AggregatedValueObject billVO)
			throws Exception {
		// 1.�ж��õ����Ƿ��ڹ����������У�������ǣ����׳��쳣����
		EngineService es = new EngineService();
		int status = es.queryWorkflowStatus(billid, pkBilltype);
		if (status == IPfRetCheckInfo.PASSING)
			throw new PFBusinessException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "wfAdminImpl-0005")/*
																 * ���������������ѽ������޷���ֹ
																 * ��
																 */);

		// 2.�޸ĵ������ݿ�״̬Ϊ����̬
		// boolean hasMeta =
		// PfMetadataTools.checkBilltypeRelatedMeta(pkBilltype);
		AbstractBusiStateCallback absc = new PFBusiStateOfMeta();

		// ��ѯ������VOʵ��
		if (billVO == null) {
			IPFConfig pfcfg = (IPFConfig) NCLocator.getInstance().lookup(
					IPFConfig.class.getName());
			billVO = pfcfg.queryBillDataVO(pkBilltype, billid);
		}
		// ���칤��������VO
		PfParameterVO paraVo = construtParamVO(billid, pkBilltype, billNo,
				WorkflowTypeEnum.Workflow.getIntValue());
		paraVo.m_workFlow = new WorkflownoteVO();
		paraVo.m_workFlow.setChecknote(WfTaskOrInstanceStatus.Terminated
				.toString());
		paraVo.m_preValueVo = billVO;
		absc.execUnApproveState(paraVo, null, IPfRetCheckInfo.NOSTATE);
		WorknoteManager manager = new WorknoteManager();
		String processId = manager.getProcessId(paraVo,
				WorkflowTypeEnum.Workflow.getIntValue());

		// 3.��ѯ����״̬����pub_wf_instance_h�м�¼�˴β�������ʷ��¼
		List<Object[]> statusList = es.queryProcessStatus(billid, pkBilltype,
				WorkflowTypeEnum.Workflow.getIntValue());
		List<FlowInstanceHistoryVO> historyList = new ArrayList<FlowInstanceHistoryVO>();

		for (Object[] statusRow : statusList) {
			String pk_wf_instance = String.valueOf(statusRow[0]);
			Integer procStatus = Integer.parseInt(String.valueOf(statusRow[1]));

			FlowInstanceHistoryVO history = new FlowInstanceHistoryVO();
			history.setPk_wf_instance(pk_wf_instance);
			history.setPreviousStatus(procStatus);
			history.setOperation(FlowInstanceOperation.TERMINATE.getIntValue());
			history.setReason(reason);
			history.setOperator(InvocationInfoProxy.getInstance().getUserId());
			history.setBilltype(pkBilltype);
			history.setBillid(billid);
			history.setBillno(billNo);

			// FIXME: �Ƿ��¼ҵ������?
			history.setOperationDate(new UFDateTime());

			historyList.add(history);
		}

		new BaseDAO().insertVOList(historyList);

		// 4.ɾ���õ�����ص�������Ϣ��������ҵ��ع�
		es.rollbackWorkflow(billid, pkBilltype, billVO,
				WorkflowTypeEnum.Workflow.getIntValue());
		es.deleteWorkflow(billid, pkBilltype, false,
				WorkflowTypeEnum.Workflow.getIntValue());

		// 5.�������Ա������Ϣ���������Ѵ����˷�����Ϣ
		manager.sendMsgWhenWFstateChanged(paraVo, processId,
				WfTaskOrInstanceStatus.Terminated.getIntValue(),
				WorkflowTypeEnum.Workflow.getIntValue());
	}

	public void resumeWorkflow(String billid, String pkBilltype, String billNo,
			int iWorkflowtype) throws BusinessException {
		try {
			if (iWorkflowtype == WorkflowTypeEnum.Approveflow.getIntValue()) {
				resumeApproveflow(billid, pkBilltype, billNo, null);
			} else if (iWorkflowtype == WorkflowTypeEnum.Workflow.getIntValue()) {
				resumeWorkflow(billid, pkBilltype, billNo, null);
			} else
				throw new PFBusinessException(NCLangResOnserver.getInstance()
						.getStrByID("pfworkflow", "wfAdminImpl-0004")/* �����˴���Ĳ���ֵiWorkflowtype */);

		} catch (Exception e) {
			handleException(e, null);
		}
	}

	private void resumeWorkflow(String billid, String pkBilltype,
			String billNo, String reason) throws Exception {
		// 1.�ж��õ����Ƿ������������У�������ǣ����׳��쳣����
		EngineService queryDMO = new EngineService();
		int status = queryDMO.queryApproveflowStatus(billid, pkBilltype);
		if (status == IPfRetCheckInfo.PASSING
				|| status == IPfRetCheckInfo.NOPASS)
			throw new PFBusinessException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "wfAdminImpl-0006")/*
																 * ���������������ѽ������޷��ָ�
																 * ��
																 */);

		// 2.��ѯ����״̬����pub_wf_instance_h�м�¼�˴β�������ʷ��¼
		List<Object[]> statusList = queryDMO.queryProcessStatus(billid,
				pkBilltype, WorkflowTypeEnum.Workflow.getIntValue());
		List<FlowInstanceHistoryVO> historyList = new ArrayList<FlowInstanceHistoryVO>();

		for (Object[] statusRow : statusList) {
			String pk_wf_instance = String.valueOf(statusRow[0]);
			Integer procStatus = Integer.parseInt(String.valueOf(statusRow[1]));

			FlowInstanceHistoryVO history = new FlowInstanceHistoryVO();
			history.setPk_wf_instance(pk_wf_instance);
			history.setPreviousStatus(procStatus);
			history.setOperation(FlowInstanceOperation.RESUME.getIntValue());
			history.setReason(reason);
			history.setOperator(InvocationInfoProxy.getInstance().getUserId());
			history.setBilltype(pkBilltype);
			history.setBillid(billid);
			history.setBillno(billNo);

			// FIXME: �Ƿ��¼ҵ������?
			history.setOperationDate(new UFDateTime());

			historyList.add(history);
		}

		// 3.�޸ĸõ�����ص�������Ϣ,��������ʵ��״̬����Ϊ��ʼ
		queryDMO.updateProcessStatus(billid, pkBilltype,
				WorkflowTypeEnum.Workflow.getIntValue(),
				WfTaskOrInstanceStatus.Started.getIntValue());
		// 3.����ʵ��״̬�仯,�������Ա������Ϣ
		PfParameterVO paramVo = construtParamVO(billid, pkBilltype, billNo,
				WorkflowTypeEnum.Workflow.getIntValue());
		WorknoteManager manager = new WorknoteManager();
		String processId = manager.getProcessId(paramVo,
				WorkflowTypeEnum.Workflow.getIntValue());
		manager.sendMsgWhenWFstateChanged(paramVo, processId, 10,
				WorkflowTypeEnum.Workflow.getIntValue());
	}

	public void suspendWorkflow(String billid, String pkBilltype,
			String billNo, int iWorkflowtype) throws BusinessException {
		try {
			if (iWorkflowtype == WorkflowTypeEnum.Approveflow.getIntValue()) {
				suspendApproveflow(billid, pkBilltype, billNo, null);
			} else if (iWorkflowtype == WorkflowTypeEnum.Workflow.getIntValue()) {
				suspendWorkflow(billid, pkBilltype, billNo, null);
			} else
				throw new PFBusinessException(NCLangResOnserver.getInstance()
						.getStrByID("pfworkflow", "wfAdminImpl-0004")/* �����˴���Ĳ���ֵiWorkflowtype */);

		} catch (Exception e) {
			handleException(e, null);
		}
	}

	private void handleException(Exception e, String message)
			throws BusinessException {
		if (e == null) {
			return;
		}

		Logger.error(e.getMessage(), e);
		;

		if (e instanceof BusinessException
				&& StringUtil.isEmptyWithTrim(message)) {
			throw (BusinessException) e;
		} else if (StringUtil.isEmptyWithTrim(message)) {
			throw new PFBusinessException(e);
		} else {
			throw new PFBusinessException(message, e);
		}
	}

	private void resumeApproveflow(String billid, String pkBilltype,
			String billNo, String reason) throws Exception {
		// 1.�ж��õ����Ƿ������������У�������ǣ����׳��쳣����
		EngineService queryDMO = new EngineService();
		int status = queryDMO.queryApproveflowStatus(billid, pkBilltype);
		if (status == IPfRetCheckInfo.PASSING
				|| status == IPfRetCheckInfo.NOPASS)
			throw new PFBusinessException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "wfAdminImpl-0006")/*
																 * ���������������ѽ������޷��ָ�
																 * ��
																 */);
		// 2.��ѯ����״̬����pub_wf_instance_h�м�¼�˴β�������ʷ��¼
		List<Object[]> statusList = queryDMO.queryProcessStatus(billid,
				pkBilltype, WorkflowTypeEnum.Approveflow.getIntValue());
		List<FlowInstanceHistoryVO> historyList = new ArrayList<FlowInstanceHistoryVO>();

		for (Object[] statusRow : statusList) {
			String pk_wf_instance = String.valueOf(statusRow[0]);
			Integer procStatus = Integer.parseInt(String.valueOf(statusRow[1]));

			FlowInstanceHistoryVO history = new FlowInstanceHistoryVO();
			history.setPk_wf_instance(pk_wf_instance);
			history.setPreviousStatus(procStatus);
			history.setOperation(FlowInstanceOperation.RESUME.getIntValue());
			history.setReason(reason);
			history.setOperator(InvocationInfoProxy.getInstance().getUserId());
			history.setBilltype(pkBilltype);
			history.setBillid(billid);
			history.setBillno(billNo);

			// FIXME: �Ƿ��¼ҵ������?
			history.setOperationDate(new UFDateTime());

			historyList.add(history);
		}

		new BaseDAO().insertVOList(historyList);

		// // 2.�޸ĵ������ݿ�״̬Ϊ����̬
		// //boolean hasMeta =
		// PfMetadataTools.checkBilltypeRelatedMeta(pkBilltype);
		// AbstractBusiStateCallback absc = new PFBusiStateOfMeta();
		//
		// //��ѯ������VOʵ��
		// IPFConfig pfcfg = (IPFConfig)
		// NCLocator.getInstance().lookup(IPFConfig.class.getName());
		// AggregatedValueObject billVO = pfcfg.queryBillDataVO(pkBilltype,
		// billid);
		// //���칤��������VO
		// PfParameterVO paraVo = new PfParameterVO();
		// paraVo.m_workFlow = new WorkflownoteVO();
		// paraVo.m_workFlow.setChecknote("�������ָ̻�");
		// paraVo.m_billId = billid;
		// paraVo.m_billType = pkBilltype;
		// paraVo.m_preValueVo = billVO;
		// absc.execUnApproveState(paraVo, null, IPfRetCheckInfo.GOINGON);

		// FIXME: �������ɵ������̣��˴�Ӧ��ԭΪ�����̹���֮ǰ��״̬�������л�����ɣ�
		// 3.�޸ĸõ�����ص�������Ϣ,��������ʵ��״̬����Ϊ��ʼ
		queryDMO.updateProcessStatus(billid, pkBilltype,
				WorkflowTypeEnum.Approveflow.getIntValue(),
				WfTaskOrInstanceStatus.Started.getIntValue());

		// 4.����ʵ��״̬�仯,�������Ա������Ϣ
		PfParameterVO paramVo = construtParamVO(billid, pkBilltype, billNo,
				WorkflowTypeEnum.Approveflow.getIntValue());
		WorknoteManager manager = new WorknoteManager();
		String processId = manager.getProcessId(paramVo,
				WorkflowTypeEnum.Approveflow.getIntValue());
		manager.sendMsgWhenWFstateChanged(paramVo, processId, 10,
				WorkflowTypeEnum.Approveflow.getIntValue());
	}

	/**
	 * ��������ʵ��
	 * 
	 * @param billid
	 * @param pkBilltype
	 * @param billNo
	 * @throws Exception
	 */
	private void suspendWorkflow(String billid, String pkBilltype,
			String billNo, String reason) throws Exception {
		// 1.�ж��õ����Ƿ������������У�������ǣ����׳��쳣����
		EngineService queryDMO = new EngineService();
		int status = queryDMO.queryApproveflowStatus(billid, pkBilltype);
		if (status == IPfRetCheckInfo.PASSING
				|| status == IPfRetCheckInfo.NOPASS)
			throw new PFBusinessException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "wfAdminImpl-0007")/*
																 * ���������������ѽ������޷�����
																 * ��
																 */);

		// 2.��ѯ����״̬����pub_wf_instance_h�м�¼�˴β�������ʷ��¼
		List<Object[]> statusList = queryDMO.queryProcessStatus(billid,
				pkBilltype, WorkflowTypeEnum.Workflow.getIntValue());
		List<FlowInstanceHistoryVO> historyList = new ArrayList<FlowInstanceHistoryVO>();

		for (Object[] statusRow : statusList) {
			String pk_wf_instance = String.valueOf(statusRow[0]);
			Integer procStatus = Integer.parseInt(String.valueOf(statusRow[1]));

			FlowInstanceHistoryVO history = new FlowInstanceHistoryVO();
			history.setPk_wf_instance(pk_wf_instance);
			history.setPreviousStatus(procStatus);
			history.setOperation(FlowInstanceOperation.SUSPEND.getIntValue());
			history.setReason(reason);
			history.setOperator(InvocationInfoProxy.getInstance().getUserId());
			history.setBilltype(pkBilltype);
			history.setBillid(billid);
			history.setBillno(billNo);

			// FIXME: �Ƿ��¼ҵ������?
			history.setOperationDate(new UFDateTime());

			historyList.add(history);
		}

		new BaseDAO().insertVOList(historyList);

		// 3.�޸ĸõ�����ص�������Ϣ,��������ʵ��״̬����Ϊ����
		queryDMO.updateProcessStatus(billid, pkBilltype,
				WorkflowTypeEnum.Workflow.getIntValue(),
				WfTaskOrInstanceStatus.Suspended.getIntValue());
		// 4.����ʵ��״̬�仯,�������Ա������Ϣ
		PfParameterVO paramVo = construtParamVO(billid, pkBilltype, billNo,
				WorkflowTypeEnum.Workflow.getIntValue());
		WorknoteManager manager = new WorknoteManager();
		String processId = manager.getProcessId(paramVo,
				WorkflowTypeEnum.Workflow.getIntValue());
		manager.sendMsgWhenWFstateChanged(paramVo, processId,
				WfTaskOrInstanceStatus.Suspended.getIntValue(),
				WorkflowTypeEnum.Workflow.getIntValue());
	}

	/**
	 * ����������ʵ��
	 * 
	 * @param billid
	 * @param pkBilltype
	 * @param billNo
	 * @throws Exception
	 */
	private void suspendApproveflow(String billid, String pkBilltype,
			String billNo, String reason) throws Exception {
		// 1.�ж��õ����Ƿ������������У�������ǣ����׳��쳣����
		EngineService queryDMO = new EngineService();
		int status = queryDMO.queryApproveflowStatus(billid, pkBilltype);
		if (status == IPfRetCheckInfo.PASSING
				|| status == IPfRetCheckInfo.NOPASS)
			throw new PFBusinessException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "wfAdminImpl-0007")/*
																 * ���������������ѽ������޷�����
																 * ��
																 */);

		// 2.��ѯ����״̬����pub_wf_instance_h�м�¼�˴β�������ʷ��¼
		List<Object[]> statusList = queryDMO.queryProcessStatus(billid,
				pkBilltype, WorkflowTypeEnum.Approveflow.getIntValue());
		List<FlowInstanceHistoryVO> historyList = new ArrayList<FlowInstanceHistoryVO>();

		for (Object[] statusRow : statusList) {
			String pk_wf_instance = String.valueOf(statusRow[0]);
			Integer procStatus = Integer.parseInt(String.valueOf(statusRow[1]));

			FlowInstanceHistoryVO history = new FlowInstanceHistoryVO();
			history.setPk_wf_instance(pk_wf_instance);
			history.setPreviousStatus(procStatus);
			history.setOperation(FlowInstanceOperation.SUSPEND.getIntValue());
			history.setReason(reason);
			history.setOperator(InvocationInfoProxy.getInstance().getUserId());
			history.setBilltype(pkBilltype);
			history.setBillid(billid);
			history.setBillno(billNo);

			// FIXME: �Ƿ��¼ҵ������?
			history.setOperationDate(new UFDateTime());

			historyList.add(history);
		}

		new BaseDAO().insertVOList(historyList);

		// 2.�޸ĸõ�����ص�������Ϣ,��������ʵ��״̬����Ϊ����
		queryDMO.updateProcessStatus(billid, pkBilltype,
				WorkflowTypeEnum.Approveflow.getIntValue(),
				WfTaskOrInstanceStatus.Suspended.getIntValue());
		// 3.����ʵ��״̬�仯,�������Ա������Ϣ
		PfParameterVO paramVo = construtParamVO(billid, pkBilltype, billNo,
				WorkflowTypeEnum.Approveflow.getIntValue());
		WorknoteManager manager = new WorknoteManager();
		String processId = manager.getProcessId(paramVo,
				WorkflowTypeEnum.Approveflow.getIntValue());
		manager.sendMsgWhenWFstateChanged(paramVo, processId,
				WfTaskOrInstanceStatus.Suspended.getIntValue(),
				WorkflowTypeEnum.Approveflow.getIntValue());
	}

	/**
	 * �ʼ��߰�
	 * 
	 * @param workitemnote
	 * @throws BusinessException
	 */
	public void mailUrgency(WorkflownoteVO workitemnote)
			throws BusinessException {
		/** 1.��ָ���Ĺ�����ʹ����ʼ� */
		EngineService wfQry = new EngineService();
		LinkedHashMap<String, BasicWorkflowProcess> lhm = new LinkedHashMap();
		/** ͨ��ָ���Ĺ������PK��ѯ�����ڵ�����ʵ���� PK */

		TaskManagerDMO dmo = new TaskManagerDMO();
		WFTask task = null;
		try {
			task = dmo.getTaskByPK(workitemnote.getPk_wf_task());
		} catch (DbException e) {
			handleException(e, null);
		}

		if (task == null)
			return;

		String strProcInstPK = task.getWfProcessInstancePK();
		// ��ѯ����������
		BasicWorkflowProcess bwp = null;
		if (!lhm.containsKey(strProcInstPK)) {
			try {
				bwp = wfQry.findParsedMainWfProcessByInstancePK(strProcInstPK);
				lhm.put(strProcInstPK, bwp);
			} catch (Exception e) {
				handleException(e, null);
			}
		}
		if (bwp == null)
			return;

		String strCheckman = workitemnote.getCheckman();

		String pk_checkflow = workitemnote.getPk_checkflow();
		FlowOverdueVO overdue = getWorknoteOverdueBatch(
				new String[] { pk_checkflow }).get(pk_checkflow);

		String strMessagenote = null;
		if (overdue.isOverdue()) {
			strMessagenote = NCLangResOnserver.getInstance().getStrByID(
					"pfworkflow",
					"wfAdminImpl-0008",
					null,
					new String[] {
							String.valueOf(overdue.getOverdueDays())
									+ DurationUnit.DAY.toString(),
							workitemnote.getMessagenote() })/*
															 * �������� �� ���� { 0 } {
															 * 1 } , { 2 }
															 */;
		} else {
			strMessagenote = workitemnote.getMessagenote();
		}

		/*
		 * added yanke1 2011-3-30 �����ʼ�ǰ�ж��û��Ƿ�������������Ϣ
		 */
		try {
			IUserPubService userService = NCLocator.getInstance().lookup(
					IUserPubService.class);
			IUAPQueryBS uapQry = NCLocator.getInstance().lookup(
					IUAPQueryBS.class);
			UserVO[] users = userService
					.getUsersByPKs(new String[] { strCheckman });
			if (users == null) {
				throw new BusinessException(NCLangResOnserver.getInstance()
						.getStrByID("pfworkflow", "wfAdminImpl-0012")/*
																	 * δ�ҵ��������
																	 */);
			}
			String pk_psn_doc = users[0].getPk_base_doc();
			if (StringUtil.isEmpty(pk_psn_doc)) {
				throw new BusinessException(NCLangResOnserver.getInstance()
						.getStrByID("pfworkflow", "wfAdminImpl-0010")/*
																	 * ������δ������Ա
																	 */);
			}
			PsndocVO psndoc = (PsndocVO) uapQry.retrieveByPK(PsndocVO.class,
					userService.queryPsndocByUserid(strCheckman));
			String email = psndoc == null ? null : psndoc.getEmail();
			if (StringUtil.isEmptyWithTrim(email)) {
				throw new BusinessException(NCLangResOnserver.getInstance()
						.getStrByID("pfworkflow", "wfAdminImpl-0011")/*
																	 * �����Աδ���õ����ʼ�
																	 */);
			}
		} catch (Exception e) {
			handleException(e, null);
		}

		MessageMetaVO meta = PfMessageUtil.createMessageMeta(workitemnote,
				task, null);
		meta.setReceiver(strCheckman);
		meta.setSenddate(new UFDateTime());
		meta.setMessage_type(MessageMetaType.EMAIL_NOTICE);
		meta.setTitle(strMessagenote);

		Map<String, MessageMetaVO> userMetaMap = new HashMap<String, MessageMetaVO>();
		userMetaMap.put(strCheckman, meta);

		EmailMsg em = new EmailMsg();
		// �����ʼ�֪ͨ
		em.setMailModal(MailModal.MAIL_INFO);
		em.setUserIds(new String[] { strCheckman });
		em.setBillId(task.getBillID());
		em.setBillNo(task.getBillNO());
		em.setBillType(task.getBillType());
		em.setPrintTempletId(bwp.getMailPrintTemplet().getTempletid());
		em.setTopic(strMessagenote);
		em.setSenderman(task.getSenderman());
		em.setTasktype(task.getTaskType());
		em.setLangCode(InvocationInfoProxy.getInstance().getLangCode());
		em.setDatasource(InvocationInfoProxy.getInstance().getUserDataSource());
		em.setInvocationInfo(getInvocationInfo());
		em.setUserMetaMap(userMetaMap);

		// yanke1 �˴�ͬ�������ʼ�
		new PfEmailSendTask(em).getTaskBody().execute();
	}

	@Override
	public UFBoolean hasRunningProcess(String billId, String billType,
			String flowType) {
		String sql = "select pk_wf_instance from pub_wf_instance"
				// +
				// " where billid=? and billtype=? and isnull(src_pk_actinstance,'~')='~' and procstatus="
				// //ȥ��isnull(src_pk_actinstance,'~')='~'����Ϊ������Ҳ����������
				+ " where billversionpk=? and billtype=? and procstatus="
				+ WfTaskOrInstanceStatus.Started.getIntValue()
				+ " and workflow_type=?";

		PersistenceManager persist = null;
		try {
			persist = PersistenceManager.getInstance();
			JdbcSession jdbc = persist.getJdbcSession();
			SQLParameter para = new SQLParameter();
			para.addParam(billId);
			para.addParam(billType);
			para.addParam(Integer.parseInt(flowType));// 2011-7-12 wcj
														// ��������ת������postgresql
														// ������ת������

			Object obj = jdbc.executeQuery(sql, para, new ColumnProcessor(1));
			return obj == null ? UFBoolean.FALSE : UFBoolean.TRUE;
		} catch (DbException e) {
			Logger.error(e.getMessage(), e);
		} finally {
			if (persist != null)
				persist.release();
		}
		return UFBoolean.FALSE;
	}

	@Deprecated
	@Override
	public void saveFlowInstanceSetting(String pk_wf_instance,
			FlowInstanceSettingVO[] settings) throws BusinessException {
		// TODO Auto-generated method stub

		String sql = " delete from pub_wf_ist where pk_wf_instance = ? ";
		PersistenceManager persist = null;
		try {
			persist = PersistenceManager.getInstance();
			JdbcSession jdbc = persist.getJdbcSession();
			SQLParameter para = new SQLParameter();
			para.addParam(pk_wf_instance);
			jdbc.executeUpdate(sql, para);

			String insertSql = "insert into pub_wf_ist(pk_wf_ist,pk_wf_instance,activitydefid,timelimit,timeremind) values (?,?,?,?,?)";
			for (FlowInstanceSettingVO vo : settings) {
				SQLParameter para1 = new SQLParameter();
				para1.addParam(OidGenerator.getInstance().nextOid());
				para1.addParam(pk_wf_instance);// /
				para1.addParam(vo.getId());
				para1.addParam(vo.getTimeLimit());
				para1.addParam(vo.getTimeRemind());
				jdbc.addBatch(insertSql, para1);
			}

			jdbc.executeBatch();

		} catch (DbException e) {
			Logger.error(e.getMessage(), e);
		} finally {
			if (persist != null)
				persist.release();
		}

	}

	@Deprecated
	@SuppressWarnings("unchecked")
	@Override
	public FlowInstanceSettingVO[] getFlowInstanceSetting(String pk_wf_instance)
			throws BusinessException {
		// TODO Auto-generated method stub
		String sql = " select activitydefid , timelimit, timeremind  from pub_wf_ist where pk_wf_instance = ? ";
		//
		ArrayList<FlowInstanceSettingVO> ret = null;
		PersistenceManager persist = null;
		try {
			persist = PersistenceManager.getInstance();
			JdbcSession jdbc = persist.getJdbcSession();
			SQLParameter para = new SQLParameter();
			para.addParam(pk_wf_instance);
			ret = (ArrayList<FlowInstanceSettingVO>) jdbc.executeQuery(sql,
					para, new BaseProcessor() {

						@Override
						public Object processResultSet(ResultSet rs)
								throws SQLException {
							// TODO Auto-generated method stub
							ArrayList<FlowInstanceSettingVO> l = new ArrayList<FlowInstanceSettingVO>();
							while (rs.next()) {
								FlowInstanceSettingVO vo = new FlowInstanceSettingVO();
								vo.setId(rs.getString(1));
								vo.setTimeLimit(rs.getInt(2));
								vo.setTimeRemind(rs.getInt(3));
								l.add(vo);
							}
							return l;
						}
					});

		} catch (DbException e) {
			Logger.error(e.getMessage(), e);
		} finally {
			if (persist != null)
				persist.release();
		}
		return ret == null ? null : ret.toArray(new FlowInstanceSettingVO[0]);

	}

	@Override
	public void cpySendByMailAndMsg(WorkflownoteVO worknoteVO,
			String[] titleAndnote) throws BusinessException {
		doCpySendByMsg(worknoteVO, titleAndnote);
		doCpySendByMail(worknoteVO, titleAndnote);
	}

	// ��Ϣ��ʽ����
	private void doCpySendByMsg(WorkflownoteVO worknoteVO, String[] titleAndnote)
			throws BusinessException {
		List<String> target = worknoteVO.getMsgExtCpySenders();

		if (ArrayUtil.isNull(target)) {
			return;
		}
		String[] msgExtCpySenders = target.toArray(new String[0]);

		List<MessageinfoVO> msgInfoVOs = new ArrayList<MessageinfoVO>();
		String[] checkerNames = getUserNameByPK(msgExtCpySenders);

		String[] senderNames = getUserNameByPK(new String[] { worknoteVO
				.getCheckman() });

		if (checkerNames == null || checkerNames.length == 0)
			return;
		for (int start = 0, end = msgExtCpySenders.length; start < end; start++) {
			MessageinfoVO msgVO = new MessageinfoVO();
			msgVO = new MessageinfoVO();
			msgVO.setBillid(worknoteVO.getBillid());

			msgVO.setSenderman(worknoteVO.getCheckman());

			msgVO.setBillno(worknoteVO.getBillno());
			msgVO.setCheckman(msgExtCpySenders[start]);
			msgVO.setCheckmanName(checkerNames[start]);
			msgVO.setContent(titleAndnote[1]);
			msgVO.setPk_billtype(worknoteVO.getPk_billtype());

			msgVO.setTitle(senderNames[0] + "{UPPpfworkflow-000154}"/*
																	 * @res "����"
																	 */+ " "
					+ titleAndnote[0] + " " + "{UPPpfworkflow-000194}" /* "���ݺ�: " */
					+ worknoteVO.getBillno());

			msgVO.setSenddate(new UFDateTime());
			msgVO.setDealdate(worknoteVO.getDealdate());
			msgVO.setDr(worknoteVO.getDr());
			msgVO.setType(MessageTypes.MSG_TYPE_INFO);
			msgVO.setPk_corp(worknoteVO.getPk_group());
			msgInfoVOs.add(msgVO);
		}
		PfMessageUtil.insertBizMessages(msgInfoVOs
				.toArray(new MessageinfoVO[msgInfoVOs.size()]));
		
		MobileApproveTools.sendCpyToMobileApp(msgInfoVOs);
	}

	// �ʼ���ʽ����
	private void doCpySendByMail(WorkflownoteVO worknoteVO,
			String[] titleAndnote) throws BusinessException {
		List<String> target = worknoteVO.getMailExtCpySenders();

		if (ArrayUtil.isNull(target)) {
			return;
		}

		String[] mailExtCpySenders = target.toArray(new String[0]);

		EngineService wfQry = new EngineService();
		WFTask currentTask = worknoteVO.getTaskInfo().getTask();
		BasicWorkflowProcess bwp = wfQry
				.findParsedMainWfProcessByInstancePK(currentTask
						.getWfProcessInstancePK());
		String ptId = bwp.getMailPrintTemplet().getTempletid();

		MessageMetaVO meta = PfMessageUtil.createMessageMeta(worknoteVO,
				currentTask, null);
		meta.setMessage_type(MessageMetaType.EMAIL_NOTICE);

		Map<String, MessageMetaVO> userMetaMap = new HashMap<String, MessageMetaVO>();

		for (String receiver : mailExtCpySenders) {
			MessageMetaVO cloned = (MessageMetaVO) meta.clone();
			cloned.setReceiver(receiver);

			userMetaMap.put(receiver, meta);
		}

		EmailMsg em = new EmailMsg();
		em.setMailModal(MailModal.MAIL_INFO);
		em.setUserIds(mailExtCpySenders);
		em.setBillId(worknoteVO.getBillid());
		em.setBillNo(worknoteVO.getBillno());
		em.setBillType(worknoteVO.getPk_billtype());
		em.setPrintTempletId(ptId);
		em.setTopic(titleAndnote[0]);
		em.setSenderman(worknoteVO.getSenderman());
		// 2009-5 ��Ҫ���¸�ֵ
		em.setTasktype(WfTaskType.Makebill.getIntValue());
		em.setUserMetaMap(userMetaMap);
		em.setLangCode(InvocationInfoProxy.getInstance().getLangCode());
		em.setDatasource(InvocationInfoProxy.getInstance().getUserDataSource());
		em.setInvocationInfo(getInvocationInfo());
		PfMailAndSMSUtil.sendEMS(em);
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

	/**
	 * ���û�PK�õ��û���
	 * */
	private String[] getUserNameByPK(String[] pks) {
		StringBuffer clause = new StringBuffer("cuserid in ( ");
		List<String> userNames = new ArrayList<String>();
		for (String pk : pks)
			clause.append("'" + pk + "', ");
		String where = clause.substring(0, clause.lastIndexOf(",")) + ")";
		try {
			BaseDAO dao = new BaseDAO();
			Collection<UserVO> users = dao
					.retrieveByClause(UserVO.class, where);
			for (UserVO user : users)
				userNames.add(user.getUser_name());
			return userNames.toArray(new String[userNames.size()]);

		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public boolean isAlreadyTracked(String pk_wf_instance, String supervisor)
			throws BusinessException {
		BaseDAO dao = new BaseDAO();

		String sql = "pk_wf_instance='" + pk_wf_instance + "' and supervisor='"
				+ supervisor + "' and type="
				+ ProcessInsSupervisorType.TRACKER.getIntValue();

		Collection<ProcessInstanceAVO> obj = (Collection<ProcessInstanceAVO>) dao
				.retrieveByClause(ProcessInstanceAVO.class, sql);
		return obj != null && obj.size() != 0;
	}

	/**
	 * �������������PfParameterVO
	 * 
	 * @param billid
	 *            ����id
	 * @param pkBilltype
	 *            ��������
	 * @param billNo
	 *            ���ݺ�
	 * @return PfParameterVO
	 * */
	private PfParameterVO construtParamVO(String billid, String pkBilltype,
			String billNo, int workflow_type) {
		PfParameterVO paramVO = new PfParameterVO();
		paramVO.m_billVersionPK = billid;
		paramVO.m_billType = pkBilltype;
		paramVO.m_billNo = billNo;
		paramVO.m_operator = InvocationInfoProxy.getInstance().getUserId();
		paramVO.m_pkGroup = InvocationInfoProxy.getInstance().getGroupId();
		EngineService es = new EngineService();
		try {
			String strMakerId = es.queryBillmakerOfInstance(billid, pkBilltype,
					workflow_type);
			paramVO.m_makeBillOperator = strMakerId;
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			Logger.error(e.getMessage(), e);
		}
		return paramVO;
	}

	@Override
	public void trackWFinstance(WorkflownoteVO worknoteVO, String supervisor,
			boolean isTrack) throws BusinessException {
		// TODO Auto-generated method stub
		String pk_wf_instance = worknoteVO.getTaskInfo().getTask()
				.getWfProcessInstancePK();
		BaseDAO dao = new BaseDAO();
		if (!isTrack) {
			Logger.debug("WorkflowAdminImpl.trackWFInstance: delete supervisor="
					+ supervisor + ", pk_wf_instance=" + pk_wf_instance);

			SQLParameter param = new SQLParameter();
			param.addParam(pk_wf_instance);
			param.addParam(supervisor);

			dao.deleteByClause(ProcessInstanceAVO.class,
					"pk_wf_instance=? and supervisor=?", param);
		} else {
			ProcessInstanceAVO pvo = new ProcessInstanceAVO();
			pvo.setPk_wf_instance(pk_wf_instance);
			pvo.setSupervisor(supervisor);
			pvo.setType(ProcessInsSupervisorType.TRACKER.getIntValue());
			dao.insertVO(pvo);
		}
	}

	public void terminateWorkflow(PfParameterVO paraVo, int wftype)
			throws PFBusinessException {
		// �������������ǹ������������Թҵ�����������Զ���Ҫ����
		// add lock according to billid
		PKLock.getInstance().addDynamicLock(paraVo.m_billVersionPK);

		// terminate process instance and rollback business
		try {
			if (wftype == WorkflowTypeEnum.Approveflow.getIntValue()) {
				terminateApproveflow(paraVo.m_billVersionPK, paraVo.m_billType,
						paraVo.m_billNo, null, paraVo.m_preValueVo,
						paraVo.m_autoApproveAfterCommit);
			} else if (wftype == WorkflowTypeEnum.Workflow.getIntValue()) {
				terminateWorkflow(paraVo.m_billVersionPK, paraVo.m_billType,
						paraVo.m_billNo, null, paraVo.m_preValueVo);
			} else
				throw new PFBusinessException(NCLangResOnserver.getInstance()
						.getStrByID("pfworkflow", "wfAdminImpl-0004")/* �����˴���Ĳ���ֵiWorkflowtype */);

		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new PFBusinessException(e);
		}
	}

	@Override
	public void suspendWorkflow(WorkflowManageContext context)
			throws BusinessException {

		String billId = context.getBillId();
		Integer approvestatus = context.getApproveStatus();
		String billtype = context.getBillType();
		Integer workflow_type = context.getFlowType();
		String billNo = context.getBillNo();
		String reason = context.getManageReason();

		// XXX:��ѯ�����������е�״̬��Ϊ�˴����ص��Ƶ��˺󣬵��ݳ�������̬�������
		// ��ʱ�޵��������ύ������һ������ʵ�������ԶԾ�ʵ���Ĺ���û������
		int iBillStatus = NCLocator.getInstance().lookup(IPFWorkflowQry.class)
				.queryFlowStatus(billtype, billId, workflow_type);

		if (approvestatus == WfTaskOrInstanceStatus.Started.getIntValue()
				&& iBillStatus != IWorkFlowStatus.BILL_NOT_IN_WORKFLOW) {
			// WARN::ֻ�����������е������̲ſɹ���

			try {
				if (workflow_type == WorkflowTypeEnum.Approveflow.getIntValue()) {
					suspendApproveflow(billId, billtype, billNo, reason);
				} else if (workflow_type == WorkflowTypeEnum.Workflow
						.getIntValue()) {
					suspendWorkflow(billId, billtype, billNo, reason);
				} else
					throw new Exception(nc.vo.ml.NCLangRes4VoTransl
							.getNCLangRes().getStrByID("pfworkflow61_0",
									"0pfworkflow61-0079")/* @res "ֻ�������̲ſɽ��й��������" */);
			} catch (Exception e) {
				handleException(e, null);
			}
		} else {
			throw new BusinessException(NCLangRes.getInstance().getStrByID(
					"pfworkflow", "UPPpfworkflow-000822") /*
														 * @ res
														 * "ֻ�����������У����ݲ�������̬�������̲ſɹ���"
														 */);
		}
	}

	@Override
	public void resumeWorkflow(WorkflowManageContext context)
			throws BusinessException {

		String billId = context.getBillId();
		Integer approvestatus = context.getApproveStatus();
		String billtype = context.getBillType();
		Integer workflow_type = context.getFlowType();
		String billNo = context.getBillNo();

		String reason = context.getManageReason();

		/** �ָ���ǰ������Ǹ�����ʵ����״̬Ϊ����״̬ */
		if (approvestatus == WfTaskOrInstanceStatus.Suspended.getIntValue()) {
			try {

				if (workflow_type == WorkflowTypeEnum.Approveflow.getIntValue()) {
					resumeApproveflow(billId, billtype, billNo, reason);
				} else if (workflow_type == WorkflowTypeEnum.Workflow
						.getIntValue()) {
					resumeWorkflow(billId, billtype, billNo, reason);
				} else
					throw new PFBusinessException(nc.vo.ml.NCLangRes4VoTransl
							.getNCLangRes().getStrByID("pfworkflow61_0",
									"0pfworkflow61-0080")/* @res "ֻ�������̲ſɽ��лָ�����" */);

			} catch (Exception e) {
				handleException(e, null);
			}
		} else {
			throw new BusinessException(NCLangRes.getInstance().getStrByID(
					"pfworkflow", "UPPpfworkflow-000817") /*
														 * @ res
														 * "ֻ�����ڹ����е������̲ſɻָ���"
														 */);
		}

	}

	@Override
	public void terminateWorkflow(WorkflowManageContext context)
			throws BusinessException {
		boolean isSucess = false;
		String billId = context.getBillId();
		Integer approvestatus = context.getApproveStatus();
		String billtype = context.getBillType();
		Integer workflow_type = context.getFlowType();
		String billNo = context.getBillNo();
		String reason = context.getManageReason();

		if (approvestatus == WfTaskOrInstanceStatus.Started.getIntValue()) {
			// WARN::ֻ�����������е������̲ſ���ֹ

			try {
				if (workflow_type == WorkflowTypeEnum.Approveflow.getIntValue()) {
					terminateApproveflow(billId, billtype, billNo, reason,
							null, false);
				} else if (workflow_type == WorkflowTypeEnum.Workflow
						.getIntValue()) {
					terminateWorkflow(billId, billtype, billNo, reason, null);
				} else
					throw new PFBusinessException(nc.vo.ml.NCLangRes4VoTransl
							.getNCLangRes().getStrByID("pfworkflow61_0",
									"0pfworkflow61-0080")/* @res "ֻ�������̲ſɽ��лָ�����" */);
			} catch (Exception e) {
				handleException(e, null);
			}

		} else {
			throw new BusinessException(NCLangResOnserver.getInstance()
					.getStrByID("pfworkflow", "UPPpfworkflow-000535")/*
																	 * @res
																	 * "ֻ�����������е������̲ſ���ֹ��"
																	 */);
		}
	}

	@Override
	public void updateFlowTimeSetting(String mainPk_wf_instance,
			FlowTimeSettingVO[] settings) throws BusinessException {
		BaseDAO dao = new BaseDAO();

		dao.deleteByClause(FlowTimeSettingVO.class, "mainPk_wf_instance='"
				+ mainPk_wf_instance + "'");

		if (settings != null && settings.length > 0) {
			dao.insertVOArray(settings);
		}

	}

	@Override
	public FlowTimeSettingVO[] getFlowTimeSetting(String mainPk_wf_instance)
			throws BusinessException {
		BaseDAO dao = new BaseDAO();
		Collection<FlowTimeSettingVO> col = dao.retrieveByClause(
				FlowTimeSettingVO.class, "mainPk_wf_instance='"
						+ mainPk_wf_instance + "' order by type desc");

		return col.toArray(new FlowTimeSettingVO[0]);
	}

	@Override
	public Map<String, FlowOverdueVO> getWorknoteOverdueBatch(
			String[] pk_checkflows) throws BusinessException {
		WorkflowOverdueCalculator calculator = new WorkflowOverdueCalculator();
		Map<String, FlowOverdueVO> map = new HashMap<String, FlowOverdueVO>();

		for (String pk : pk_checkflows) {
			FlowOverdueVO overdue = calculator.getWorknoteOverdue(pk);
			map.put(pk, overdue);
		}

		return map;
	}

	@Override
	public Map<String, FlowOverdueVO> getFlowInstanceOverdue(
			String[] pk_wf_instances) throws BusinessException {
		WorkflowOverdueCalculator calculator = new WorkflowOverdueCalculator();
		Map<String, FlowOverdueVO> map = new HashMap<String, FlowOverdueVO>();

		for (String pk : pk_wf_instances) {
			FlowOverdueVO overdue = calculator.getFlowInstanceOverdue(pk);
			map.put(pk, overdue);
		}

		return map;
	}

	@Override
	public ArrayList<String> findFilterOrgs4Responsibility(WFTask task)
			throws BusinessException {
		if (task.getParticipantType().equals(
				OrganizeUnitTypes.RESPONSIBILITY.toString())) {
			BasicWorkflowProcess processdef;
			try {
				processdef = PfDataCache.getWorkflowProcess(task
						.getWfProcessDefPK());
				GenericActivityEx activity = (GenericActivityEx) processdef
						.findActivityByID(task.getActivityID());
				Object participantFilterMode = activity
						.getParticipantFilterMode().getValue();
				IOrgFilter4Responsibility orgFilter = PfOrg4ResponsibilityFactory
						.getInstance().getFilterByCode(
								participantFilterMode.toString(),
								task.getBillType());
				return orgFilter.execute(task);
			} catch (XPDLParserException e) {
				Logger.error(e.getMessage());
				throw new BusinessException(e.getCause());
			}

		}
		return null;
	}

	@Override
	public String[] getMessageReceivers(ReceiverVO[] arg0, WFTask arg1)
			throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}
}