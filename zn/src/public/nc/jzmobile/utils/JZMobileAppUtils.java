package nc.jzmobile.utils;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.comn.NetStreamContext;
import nc.bs.framework.core.service.IFwLogin;
import nc.bs.logging.Logger;
import nc.bs.mapp.conf.MappConfAccessor;
import nc.bs.pf.pub.PFRequestDataCacheProxy;
import nc.bs.pf.pub.PfDataCache;
import nc.bs.pf.pub.WorkflowProcessCache;
import nc.bs.pf.pub.cache.CondStringKey;
import nc.bs.pf.pub.cache.ICacheDataQueryCallback;
import nc.bs.pf.pub.cache.IRequestDataCacheKey;
import nc.bs.wfengine.engine.ActivityInstance;
import nc.itf.uap.IUAPQueryBS;
import nc.itf.uap.billtemplate.IBillTemplateQry;
import nc.itf.uap.pf.IPFConfig;
import nc.itf.uap.pf.IPFTemplate;
import nc.itf.uap.pf.IWorkflowDefine;
import nc.itf.uap.pf.IWorkflowMachine;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.page.LimitSQLBuilder;
import nc.jdbc.framework.page.SQLBuilderFactory;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.ui.pf.multilang.PfMultiLangUtil;
import nc.vo.approve.ActivityVo;
import nc.vo.bd.psn.PsnjobVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.ml.MultiLangUtil;
import nc.vo.org.DeptVO;
import nc.vo.org.JobVO;
import nc.vo.pf.mobileapp.ITaskType;
import nc.vo.pf.mobileapp.MobileAppUtils;
import nc.vo.pf.mobileapp.TaskMetaData;
import nc.vo.pf.mobileapp.TaskTypeFactory;
import nc.vo.pf.mobileapp.query.BillVORowCountKey;
import nc.vo.pf.mobileapp.query.TaskQuery;
import nc.vo.pf.pub.util.ArrayUtil;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.bill.BillTempletVO;
import nc.vo.pub.billtype.BilltypeVO;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.msg.MessageVO;
import nc.vo.pub.pf.AssignableInfo;
import nc.vo.pub.pf.PFClientBizRetVO;
import nc.vo.pub.template.ITemplateStyle;
import nc.vo.pub.workflownote.WorkflownoteVO;
import nc.vo.sm.UserVO;
import nc.vo.to.pub.util.StringUtils;
import nc.vo.uap.pf.TemplateParaVO;
import nc.vo.uap.wfmonitor.ProcessRouteRes;
import nc.vo.wfengine.core.parser.XPDLNames;
import nc.vo.wfengine.core.workflow.BasicWorkflowProcess;
import nc.vo.wfengine.definition.WorkflowTypeEnum;
import nc.vo.wfengine.pub.WFTask;
import nc.vo.wfengine.pub.WfTaskOrInstanceStatus;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 流程平台移动应用工具类
 * 
 * @author yanke1
 * 
 */
public class JZMobileAppUtils {

	public static final Integer TRIM_TO_COUNT = 50;

	/**
	 * 创建一个ArrayList
	 * 
	 * @return
	 */
	public static ArrayList<Map<String, Object>> createArrayList() {
		return new ArrayList<Map<String, Object>>();
	}

	/**
	 * 创建一个HashMap
	 * 
	 * @return
	 */
	public static HashMap<String, Object> createHashMap() {
		return new HashMap<String, Object>();
	}

	public static LinkedHashMap<String, Object> createLinkedHashMap() {
		return new LinkedHashMap<String, Object>();
	}

	public static Map<Object, Map<String, Object>> convertToMap(
			List<Map<String, Object>> list, String key) {
		Map<Object, Map<String, Object>> resultMap = new HashMap<Object, Map<String, Object>>();

		for (Map<String, Object> map : list) {
			resultMap.put(map.get(key), map);
		}

		return resultMap;
	}

	/**
	 * 将UFDouble取两位精度，超过两位截零
	 * 
	 * @example -3.14150000->-3.1415<br>
	 *          -3.14000000->-3.14<br>
	 *          -3.10000000->-3.10<br>
	 *          300.00000000->300.00<br>
	 *          1.00000000->1.00<br>
	 *          0.00000000->0.00<br>
	 * @author zhaoyha 感谢赵兄！！
	 */
	public static UFDouble adjust2Scale(UFDouble value) {
		value.setTrimZero(true);
		String struf = value.toString();
		if (struf.lastIndexOf(".") > 0
				&& struf.length() - (struf.lastIndexOf(".") + 1) >= 2) {
			return value;
		}
		value.setTrimZero(false);
		return value.setScale(0 - 2, UFDouble.ROUND_HALF_UP);
	}

	/**
	 * 获取文件长度 add by liangyub 2013-08-08
	 * */
	public static String getFileSize(int filesize) {
		if (filesize < 1024) {
			return filesize + "B";
		}
		if (filesize >= 1024 && filesize < 1048576) {
			return filesize / 1024 + "KB";
		} else {
			return filesize / 1048576 + "MB";
		}

	}

	/**
	 * 根据任务类别和任务编码获取任务类型实体
	 * 
	 * @param category
	 * @param code
	 * @return
	 */
	public static ITaskType getTaskType(String category, String code) {
		ITaskType taskType = TaskTypeFactory.getInstance().get(category, code);

		if (taskType == null) {
			throw new IllegalArgumentException("invalid category or code: "
					+ category + ", " + code);
		}

		return taskType;
	}

	/**
	 * 从Object数组（通常来源于jdbcframework的ArrayListProcessor）中取字符串
	 * 
	 * @param objs
	 * @param idx
	 * @return
	 */
	public static String getStringFromObjects(Object[] objs, int idx) {
		if (objs == null) {
			return null;
		}

		if (idx >= objs.length) {
			return null;
		}

		return objs[idx] == null ? null : String.valueOf(objs[idx]);
	}

	/**
	 * 根据任务类别、编码、任务id查询任务实体
	 * 
	 * @param category
	 * @param code
	 * @param taskid
	 * @return
	 * @throws BusinessException
	 */
	public static TaskMetaData queryTaskMetaData(final String category,
			final String code, final String taskid) throws BusinessException {
		IRequestDataCacheKey key = new CondStringKey(
				IRequestDataCacheKey.CATEGORY_MA_QUERY_TASK_METADATA,
				new String[] { category, code, taskid });
		ICacheDataQueryCallback<TaskMetaData> callback = new ICacheDataQueryCallback<TaskMetaData>() {

			@Override
			public TaskMetaData queryData() throws BusinessException {
				ITaskType taskType = getTaskType(category, code);
				TaskQuery query = taskType.createNewTaskQuery();

				TaskMetaData tmd = query.queryTaskMetaData(taskid);
				return tmd;
			}
		};

		return PFRequestDataCacheProxy.get(key, callback);
	}

	/**
	 * 获取一个工作项pk对应的WorkflownoteVO实体，其中除了包含工作项本身的信息 还包含了工作流上的一些信息，比如指派信息等
	 * 
	 * @param pk_checkflow
	 * @return
	 * @throws BusinessException
	 */
	public static WorkflownoteVO checkWorkflow(String pk_checkflow)
			throws BusinessException {
		TaskMetaData tmd = MobileAppUtils.queryTaskMetaData(
				ITaskType.CATEGORY_RECEIVED, ITaskType.RECEIVED_UNHANDLED,
				pk_checkflow);
		return checkWorkflow(tmd);
	}

	/**
	 * 获取一个工作项pk对应的WorkflownoteVO实体，其中除了包含工作项本身的信息 还包含了工作流上的一些信息，比如指派信息等
	 * 
	 * @param tmd
	 * @param billvo
	 * @return
	 * @throws BusinessException
	 */
	public static WorkflownoteVO checkWorkflow(TaskMetaData tmd)
			throws BusinessException {
		IWorkflowMachine srv = NCLocator.getInstance().lookup(
				IWorkflowMachine.class);
		WorkflownoteVO note = srv.checkWorkflowActions(tmd.getBillType(),
				tmd.getBillId());

		return note;
	}

	public static PFClientBizRetVO executeClientBiz(
			AggregatedValueObject aggvo, WorkflownoteVO wfvo)
			throws BusinessException {
		IWorkflowMachine srv = NCLocator.getInstance().lookup(
				IWorkflowMachine.class);
		return srv.executeClientBizProcess(aggvo, wfvo, false);
	}

	/**
	 * 查询移动应用模板id
	 * 
	 * @param tmd
	 * @return
	 * @throws BusinessException
	 */
	public static String queryTemplateId(TaskMetaData tmd)
			throws BusinessException {
		String billtype = tmd.getBillType();
		String cuserid = tmd.getCuserid();
		String pk_group = InvocationInfoProxy.getInstance().getGroupId();

		BilltypeVO btvo = PfDataCache.getBillTypeInfo(billtype);

		String funnode = btvo.getNodecode();

		TemplateParaVO para = new TemplateParaVO();

		para.setFunNode(funnode);
		para.setOperator(cuserid);
		para.setPk_Corp(pk_group);
		para.setTemplateType(ITemplateStyle.mobileAppTemplate);

		IPFTemplate srv = NCLocator.getInstance().lookup(IPFTemplate.class);
		String templateid = srv.getTemplateId(para);

		return templateid;
	}

	/**
	 * 查询移动应用模板实体
	 * 
	 * @param pk_template
	 * @return
	 * @throws BusinessException
	 */
	public static BillTempletVO queryTemplate(String pk_template)
			throws BusinessException {
		IBillTemplateQry qry = (IBillTemplateQry) NCLocator.getInstance()
				.lookup(IBillTemplateQry.class.getName());
		BillTempletVO vo = qry.findTempletData(pk_template);

		return vo;
	}

	/**
	 * 查询单据聚合vo
	 * 
	 * @param billtype
	 * @param billid
	 * @return
	 * @throws BusinessException
	 */
	public static AggregatedValueObject queryBillEntity(final String billtype,
			final String billid) throws BusinessException {
		IRequestDataCacheKey key = new CondStringKey(
				IRequestDataCacheKey.CATEGORY_MA_QUERY_BILLENTITY,
				new String[] { billtype, billid });
		ICacheDataQueryCallback<AggregatedValueObject> callback = new ICacheDataQueryCallback<AggregatedValueObject>() {

			@Override
			public AggregatedValueObject queryData() throws BusinessException {
				AggregatedValueObject busiObj = NCLocator.getInstance()
						.lookup(IPFConfig.class)
						.queryBillDataVO(billtype, billid);
				return busiObj;
			}
		};

		return PFRequestDataCacheProxy.get(key, callback);
	}

	/**
	 * @param note
	 *            来自IWorkflowMachine.checkWorkflow()
	 * @return
	 */
	public static boolean canAddApprover(WorkflownoteVO note) {
		Object value = note.getRelaProperties().get(XPDLNames.CAN_ADDAPPROVER);
		if (value != null && "true".equalsIgnoreCase(value.toString())) {
			if (note.actiontype
					.endsWith(WorkflownoteVO.WORKITEM_ADDAPPROVER_SUFFIX))
				return false;
			else
				return true;
		} else
			return false;
	}

	/**
	 * @param note
	 *            来自IWorkflowMachine.checkWorkflow()
	 * @return
	 */
	public static boolean canReject(PFClientBizRetVO bizret, WorkflownoteVO note) {
		if (bizret != null && !bizret.isShowReject()) {
			return false;
		}
		// 加签的用户不允许驳回
		return !note.getActiontype().endsWith(
				WorkflownoteVO.WORKITEM_ADDAPPROVER_SUFFIX);
	}

	public static boolean canAgree(PFClientBizRetVO bizret, WorkflownoteVO note) {
		if (bizret != null && !bizret.isShowPass()) {
			return false;
		}

		return true;
	}

	/**
	 * @param note
	 *            来自IWorkflowMachine.checkWorkflow()
	 * @return
	 */
	public static boolean canDisAgree(PFClientBizRetVO bizret,
			WorkflownoteVO note) {
		if (bizret != null && !bizret.isShowNoPass()) {
			return false;
		}

		try {
			String pk_wf_def = getPk_wf_def(note);
			BasicWorkflowProcess process = WorkflowProcessCache.getInstance()
					.getWorkflowProcess(pk_wf_def);

			if (process != null) {
				return !process.isHideNoPassing();
			} else {
				return false;
			}
		} catch (BusinessException e) {
			throw new BusinessRuntimeException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static String getPk_wf_def(WorkflownoteVO note)
			throws BusinessException {
		if (note.getTaskInfo() != null && note.getTaskInfo().getTask() != null) {
			return note.getTaskInfo().getTask().getWfProcessDefPK();
		} else {
			String cond = "pk_wf_task=?";
			SQLParameter param = new SQLParameter();
			param.addParam(note.getPk_wf_task());

			IUAPQueryBS qry = NCLocator.getInstance().lookup(IUAPQueryBS.class);
			Collection<WFTask> taskCol = qry.retrieveByClause(WFTask.class,
					WFTask.mappingMeta, cond, new String[] { "pk_wf_task",
							"processdefid" }, param);

			if (ArrayUtil.isNull(taskCol)) {
				return null;
			} else {
				return taskCol.iterator().next().getWfProcessDefPK();
			}
		}
	}

	/**
	 * @param note
	 *            来自IWorkflowMachine.checkWorkflow()
	 * @return
	 */
	public static boolean canReassign(WorkflownoteVO note) {
		Object value = note.getRelaProperties().get(XPDLNames.CAN_TRANSFER);
		if (value != null && "true".equalsIgnoreCase(value.toString())) {
			if (note.actiontype
					.endsWith(WorkflownoteVO.WORKITEM_ADDAPPROVER_SUFFIX)) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * @param note
	 *            来自IWorkflowMachine.checkWorkflow()
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean canAssignWhenNoPass(WorkflownoteVO note) {
		if (note.getActiontype().endsWith(
				WorkflownoteVO.WORKITEM_ADDAPPROVER_SUFFIX))
			return false;

		Vector<AssignableInfo> assignInfos = note.getTaskInfo()
				.getAssignableInfos();
		if (assignInfos != null && assignInfos.size() > 0) {
			String strCriterion = null;
			for (AssignableInfo ai : assignInfos) {
				strCriterion = ai.getCheckResultCriterion();
				if (AssignableInfo.CRITERION_NOTGIVEN.equals(strCriterion)
						|| AssignableInfo.CRITERION_NOPASS.equals(strCriterion))
					return true;
			}
		}
		return false;
	}

	/**
	 * @param note
	 *            来自IWorkflowMachine.checkWorkflow()
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean canAssignWhenPassing(WorkflownoteVO note) {
		if (note.getActiontype().endsWith(
				WorkflownoteVO.WORKITEM_ADDAPPROVER_SUFFIX))
			return false;

		Vector<AssignableInfo> assignInfos = note.getTaskInfo()
				.getAssignableInfos();
		if (assignInfos != null && assignInfos.size() > 0) {
			String strCriterion = null;
			for (AssignableInfo ai : assignInfos) {
				strCriterion = ai.getCheckResultCriterion();
				if (AssignableInfo.CRITERION_NOTGIVEN.equals(strCriterion)
						|| AssignableInfo.CRITERION_PASS.equals(strCriterion))
					return true;
			}
		}
		return false;
	}

	public static <T> List<T> subList(List<T> list, int startIndex, int count) {
		// List.subList的结果List不是serializable的
		// 因此用一个ArrayList包装一下
		List<T> resultList = new ArrayList<T>();

		int size = list.size();
		int endIndex = startIndex + count;

		if (startIndex >= size) {
			return resultList;
		}

		if (endIndex > size) {
			endIndex = size;
		}

		List<T> subList = list.subList(startIndex, endIndex);

		resultList.addAll(subList);

		return resultList;
	}

	public static String getStack(Throwable e) {
		CharArrayWriter cw = null;
		PrintWriter pw = null;

		try {
			cw = new CharArrayWriter();
			pw = new PrintWriter(cw);

			e.printStackTrace(pw);

			String msg = cw.toString();
			return msg;
		} catch (Exception ex) {
			Logger.error(ex.getMessage(), ex);
			return null;
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	public static String getMaPushServlet() {
		String servlet = MappConfAccessor.getInstance().getProperty("mapurl");
		String url = "/" + servlet;

		return url;
	}

	public static String getMaHost() {
		return MappConfAccessor.getInstance().getMappHost();
	}

	public static int getMaPort() {
		try {
			return Integer.parseInt(MappConfAccessor.getInstance()
					.getMappPort());
		} catch (Throwable e) {
			Logger.error(e.getMessage(), e);
			return 8090;
		}
	}

	public static String getMaPushServiceCode() {
		return MappConfAccessor.getInstance()
				.getProperty("mapmessageserviceid");
	}

	public static String getMaPushToken() {
		return MappConfAccessor.getInstance().getProperty("maservertoken");
	}

	/**
	 * 获取一个人员档案的职位信息
	 * 
	 * @param pk_psndoc
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public static String getPsnJobInfo(String pk_psndoc)
			throws BusinessException {
		IUAPQueryBS qry = NCLocator.getInstance().lookup(IUAPQueryBS.class);
		String cond = PsnjobVO.PK_PSNDOC + "=?";

		SQLParameter param = new SQLParameter();
		param.addParam(pk_psndoc);

		String[] fields = new String[] { PsnjobVO.PK_JOB, PsnjobVO.PK_DEPT };

		Collection<PsnjobVO> col = qry.retrieveByClause(PsnjobVO.class, cond,
				fields, param);

		StringBuffer sb = new StringBuffer();
		if (col != null && col.size() > 0) {
			PsnjobVO pjv = col.iterator().next();

			if (!StringUtil.isEmptyWithTrim(pjv.getPk_dept())) {
				cond = DeptVO.PK_DEPT + "=?";

				param = new SQLParameter();
				param.addParam(pjv.getPk_dept());

				fields = new String[] { DeptVO.NAME,
						DeptVO.NAME + MultiLangUtil.getCurrentLangSeqSuffix() };

				Collection<DeptVO> deptCol = qry.retrieveByClause(DeptVO.class,
						cond, fields, param);

				if (deptCol != null && deptCol.size() > 0) {
					DeptVO dvo = deptCol.iterator().next();

					String deptName = PfMultiLangUtil
							.getSuperVONameOfCurrentLang(dvo, DeptVO.NAME);
					sb.append(deptName);
				}
			}

			if (!StringUtil.isEmptyWithTrim(pjv.getPk_job())) {
				cond = JobVO.PK_JOB + "=?";

				param = new SQLParameter();
				param.addParam(pjv.getPk_job());

				fields = new String[] { JobVO.JOBNAME,
						JobVO.JOBNAME + MultiLangUtil.getCurrentLangSeqSuffix() };

				Collection<JobVO> jobCol = qry.retrieveByClause(JobVO.class,
						cond, fields, param);

				if (jobCol != null && jobCol.size() > 0) {
					JobVO jvo = jobCol.iterator().next();

					String jobName = PfMultiLangUtil
							.getSuperVONameOfCurrentLang(jvo, JobVO.JOBNAME);

					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(jobName);
				}
			}
		}

		return sb.toString();
	}

	/**
	 * 同一线程中有效
	 * 
	 * @param cnt
	 */
	public static void setRowCount(Integer cnt) {
		IRequestDataCacheKey key = new BillVORowCountKey();
		PFRequestDataCacheProxy.put(key, cnt);
	}

	/**
	 * 同一线程中有效
	 * 
	 * @return
	 */
	public static Integer getRowCount() {
		Object rowCount = PFRequestDataCacheProxy.get(new BillVORowCountKey());
		if (rowCount != null && rowCount instanceof Integer) {
			return (Integer) rowCount;
		}
		return Integer.valueOf(0);
	}

	public static void handleException(Exception e) throws BusinessException {
		if (e instanceof BusinessException) {
			throw (BusinessException) e;
		} else {
			throw new BusinessException(e.getMessage(), e);
		}
	}

	public static Map createOutValue(String flag, String desc, List data) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("flag", flag);
		map.put("desc", desc);
		map.put("data", data);
		return map;
	}

	public static Map createOutValue(String flag, String message, String string) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("flag", flag);
		map.put("desc", message);
		map.put("data", string);
		return map;
	}

	public static Map createOutValue(String flag, String desc,
			Map<String, Object> outMap) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("flag", flag);
		map.put("desc", desc);

		for (String key : outMap.keySet()) {
			if ("taskbill".equals(key)) {
				dealTaskDetail(outMap.get(key));
			}
		}
		map.put("data", outMap);
		return map;
	}

	/**
	 * 按移动要求处理单据详情
	 * 
	 * @param obj
	 */
	private static void dealTaskDetail(Object obj) {

	}

	public static Map<String, Object> createOutValue(String flag, String desc,
			LinkedHashMap<String, String> flowImgMap) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("flag", flag);
		map.put("desc", desc);
		map.put("data", flowImgMap);
		return map;
	}

	public static void setToken() {
		String nctoken = "";
		if (StringUtils.isEmpty(nctoken)) {

			try {
				String username = getSysParam("GrpUserId");
				String password = decodeByBase64(getSysParam("GrpPasswrd"));

				IFwLogin loginService = (IFwLogin) NCLocator.getInstance()
						.lookup(IFwLogin.class.getName());
				byte[] token = loginService.login(username, password, null);
				/*
				 * String strtoken = new BASE64Encoder().encode(token); byte[]
				 * bts = null; try { bts = new
				 * BASE64Decoder().decodeBuffer(strtoken); } catch (IOException
				 * e) { LfwLogger.error(e); } LfwLogger.error("mytoken:::" +
				 * token);
				 */
				// NetStreamContext.setToken(bts);

				String bts = new BASE64Encoder().encode(token);
				bts = bts.replaceAll("\r\n", "");

				NetStreamContext
						.setToken(new BASE64Decoder().decodeBuffer(bts));
			} catch (Exception e) {
				Logger.error(e);
			}
			// String userCode =
			// InvocationInfoProxy.getInstance().getUserCode();
			// InvocationInfoProxy.getInstance().setUserCode("lws");
		} else {
			byte[] token;
			try {
				token = new BASE64Decoder().decodeBuffer(nctoken);
				// byte[] token = nctoken.getBytes();
				Logger.info("mytoken:::" + nctoken);
				NetStreamContext.setToken(token);
			} catch (IOException e) {
				Logger.error(e);
			}
		}
	}

	/************************************************ wss添加代码开始 **************************************************/
	/**
	 * 获取系统参数值 wss 2016-07-28
	 * 
	 * @param syscode
	 * @return
	 * @throws Exception
	 */
	public static String getSysParam(String syscode) throws Exception {
		BaseDAO dao = new BaseDAO();
		String sql = "select value from pub_sysinit where initcode='" + syscode
				+ "' and rownum=1 ";
		String value = "";
		try {
			Object result = dao.executeQuery(sql.toString(),
					new ColumnProcessor());
			if (result != null) {
				value = result.toString();
			}
		} catch (Exception e) {
			throw e;
		}
		return value;
	}

	/**
	 * 加密算法
	 * 
	 * @param value
	 * @return
	 */
	public static String encodeByBase64(String value) {
		byte[] bytes = value.getBytes();
		return (new BASE64Encoder()).encodeBuffer(bytes);
	}

	/**
	 * 解密算法
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public static String decodeByBase64(String value) throws Exception {
		byte[] bytes = (new BASE64Decoder()).decodeBuffer(value);
		return new String(bytes);
	}

	/**
	 * 通过人员ID获取人员编码
	 * 
	 * @param userid
	 * @return
	 * @throws Exception
	 */
	public static String getUserCodeById(String userid) throws Exception {
		BaseDAO dao = new BaseDAO();
		String sql = "select user_code from sm_user where cuserid='" + userid
				+ "'";
		String value = "";
		try {
			Object result = dao.executeQuery(sql.toString(),
					new ColumnProcessor());
			if (result != null) {
				value = result.toString();
			}
		} catch (Exception e) {
			throw e;
		}
		return value;
	}

	/**
	 * 通过人员ID获取集团pk
	 * 
	 * @param userid
	 * @return
	 * @throws Exception
	 */
	private static IUAPQueryBS iuapQueryBS = null;

	public static IUAPQueryBS getQueryBS() {
		if (iuapQueryBS == null) {
			iuapQueryBS = NCLocator.getInstance().lookup(IUAPQueryBS.class);
		}
		return iuapQueryBS;
	}

	static HashMap hashmap = new HashMap();

	public static String getPkGroupByUserId(String userid) throws Exception {
		if (hashmap.get(userid) == null) {
			String sql = "select pk_group from sm_user where cuserid='"
					+ userid + "'";
			ArrayList arraylist = (ArrayList) getQueryBS().executeQuery(
					String.valueOf(sql), new MapListProcessor());
			String pk_group = null;
			if (arraylist != null && arraylist.size() > 0) {
				HashMap map = (HashMap) arraylist.get(0);
				pk_group = String.valueOf(map.get("pk_group"));
				hashmap.put(userid, pk_group);
				return pk_group;
			}
		}
		return String.valueOf(hashmap.get(userid));
	}

	public static String getPkOrgByUserId(String userid) throws Exception {
		if (hashmap.get(userid + "pk_org") == null) {
			String sql = "select pk_org from sm_user where cuserid='" + userid
					+ "'";
			ArrayList arraylist = (ArrayList) getQueryBS().executeQuery(
					String.valueOf(sql), new MapListProcessor());
			String pk_org = null;
			if (arraylist != null && arraylist.size() > 0) {
				HashMap map = (HashMap) arraylist.get(0);
				pk_org = String.valueOf(map.get("pk_org"));
				hashmap.put(userid + "pk_org", pk_org);
				return pk_org;
			}
		}
		return String.valueOf(hashmap.get(userid + "pk_org"));
	}

	public static Map<String, String> getOrgInfoByOrgName(String orgName)
			throws Exception {
		Map<String, String> valMap = new HashMap<>();

		if (hashmap.get(orgName + "pk_org") == null) {
			String sql = "select pk_group,pk_org from org_orgs_v where name='"
					+ orgName + "'";
			ArrayList arraylist = (ArrayList) getQueryBS().executeQuery(
					String.valueOf(sql), new MapListProcessor());
			String pk_org = null;
			String pk_group = null;
			if (arraylist != null && arraylist.size() > 0) {
				HashMap map = (HashMap) arraylist.get(0);
				pk_group = String.valueOf(map.get("pk_group"));
				pk_org = String.valueOf(map.get("pk_org"));
				hashmap.put(orgName + "pk_group", pk_group);
				hashmap.put(orgName + "pk_org", pk_org);
				valMap.put("pk_group", pk_group);
				valMap.put("pk_org", pk_org);
				return valMap;
			}
		} else {
			valMap.put("pk_group",
					String.valueOf(hashmap.get(orgName + "pk_group")));
			valMap.put("pk_org",
					String.valueOf(hashmap.get(orgName + "pk_org")));
		}
		return valMap;
	}

	/**
	 * 是否是移动审批已经开发完成的单据
	 * 
	 * @param billType
	 * @return
	 */
	public static boolean isMobileAppBillType(String billType) {

		// String sql =
		// "select count(*) from uap_mobile_billtype where billtypecode = '"
		// + billType + "'";
		// Object value = getSingleValue(sql);
		// if (value == null) {
		// return false;
		// } else {
		// return Integer.parseInt(value.toString()) > 0;
		// }
		try {
			BillTypeModelTrans.getInstance().getModelByBillType(billType);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static Object getSingleValue(String sql) {
		BaseDAO dao = new BaseDAO();
		try {
			Object result = dao.executeQuery(sql.toString(),
					new ColumnProcessor());
			return result;
		} catch (Exception e) {
			Logger.error(e);
		}
		return null;
	}

	/************************************************ wss添加代码结束 **************************************************/
	public static List<UserVO> findSendToUserList(String userId)
			throws Exception {
		String pk_group = getPkGroupByUserId(userId);
		String pk_org = getPkOrgByUserId(userId);
		String sql = "select user_code,user_name,pk_usergroupforcreate,pk_org,cuserid from sm_user  where pk_group = '"
				+ pk_group + "' and pk_org ='"+pk_org+"' and pk_usergroupforcreate <> '~'";
		BaseDAO dao = new BaseDAO();
		List<UserVO> userVoList = (List<UserVO>) dao.executeQuery(sql,
				new ResultSetProcessor() {
					public Object handleResultSet(ResultSet rs)
							throws SQLException {
						List<UserVO> userVoList = new ArrayList<UserVO>();
						UserVO userVo = null;
						while (rs.next()) {
							userVo = new UserVO();
							userVo.setCuserid(rs.getString("cuserid"));
							userVo.setUser_code(rs.getString("user_code"));
							userVo.setUser_name(rs.getString("user_name"));

							userVoList.add(userVo);
						}
						return userVoList;
					}
				});
		return userVoList;
	}

	public static List<UserVO> findSendToUserList(String userId,
			String condition, int start, int count) throws Exception {
		String pk_group = MobileAppUtils.getPkGroupByUserId(userId);

		int dbtype = new BaseDAO().getDBType();
		LimitSQLBuilder builder = SQLBuilderFactory.getInstance()
				.createLimitSQLBuilder(dbtype);
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer
				.append("select user_code,user_name,pk_usergroupforcreate,pk_org,cuserid from sm_user  where pk_group = '");
		sqlBuffer.append(pk_group);
		sqlBuffer.append("' and pk_usergroupforcreate <> '~'");

		if (condition != null && !"".equals(condition)) {
			sqlBuffer.append(" and (");
			sqlBuffer.append(" user_code like '%" + condition
					+ "%' or user_name like '" + condition + "'");
			sqlBuffer.append(" )");
		}
		String sql = builder.build(sqlBuffer.toString(), start, count);
		BaseDAO dao = new BaseDAO();
		List<UserVO> userVoList = (List<UserVO>) dao.executeQuery(sql,
				new ResultSetProcessor() {
					public Object handleResultSet(ResultSet rs)
							throws SQLException {
						List<UserVO> userVoList = new ArrayList<UserVO>();
						UserVO userVo = null;
						while (rs.next()) {
							userVo = new UserVO();
							userVo.setCuserid(rs.getString("cuserid"));
							userVo.setUser_code(rs.getString("user_code"));
							userVo.setUser_name(rs.getString("user_name"));

							userVoList.add(userVo);
						}
						return userVoList;
					}
				});
		return userVoList;
	}

	public static Integer findSendToUserCount(String userId, String condition)
			throws Exception {
		String pk_group = MobileAppUtils.getPkGroupByUserId(userId);

		int dbtype = new BaseDAO().getDBType();
		LimitSQLBuilder builder = SQLBuilderFactory.getInstance()
				.createLimitSQLBuilder(dbtype);
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer
				.append("select count(cuserid) from sm_user  where pk_group = '");
		sqlBuffer.append(pk_group);
		sqlBuffer.append("' and pk_usergroupforcreate <> '~'");

		if (condition != null && !"".equals(condition)) {
			sqlBuffer.append(" and (");
			sqlBuffer.append(" user_code like '%" + condition
					+ "%' or user_name like '" + condition + "'");
			sqlBuffer.append(" )");
		}
		BaseDAO dao = new BaseDAO();
		Integer count = (Integer) dao.executeQuery(sqlBuffer.toString(),
				new ResultSetProcessor() {
					public Object handleResultSet(ResultSet rs)
							throws SQLException {
						Integer c = 0;
						while (rs.next()) {
							c = rs.getInt(1);
						}
						return c;
					}
				});
		return count;
	}

	public static List<ActivityVo> findActivate(String billId,
			String pk_billtype) throws Exception {
		ProcessRouteRes processRoute = null;
		IWorkflowDefine wfDefine = (IWorkflowDefine) NCLocator.getInstance()
				.lookup(IWorkflowDefine.class.getName());

		// TODO: 没有考虑子流程的问题,第三个参数固定传null
		processRoute = wfDefine.queryProcessRoute(billId, pk_billtype, null,
				WorkflowTypeEnum.Approveflow.getIntValue());

		String activityXml = (String) processRoute.getXpdlString();
		Map<String, String> activityMap = new HashMap<String, String>();
		Document document = null;
		document = DocumentHelper.parseText(activityXml);

		Map<String, ActivityVo> actMap = new HashMap<String, ActivityVo>();
		ActivityVo activity = null;
		List<Element> activities = document.getRootElement()
				.element("Activities").elements("Activity");
		for (Element act : activities) {
			activity = new ActivityVo();
			activity.setActivityID(act.attributeValue("Id"));
			activity.setActivityName(act.attributeValue("Name"));

			actMap.put(activity.getActivityID(), activity);
		}

		List<ActivityVo> activityList = new ArrayList<ActivityVo>();
		for (ActivityInstance instance : processRoute.getActivityInstance()) {
			if (instance.getStatus() == WfTaskOrInstanceStatus.Finished
					.getIntValue()) {

				activityList.add(actMap.get(instance.getActivityID()));
			}
		}

		return activityList;
	}
	
	
	public static List<ActivityVo> findActivateForWork(String billId,
			String pk_billtype) throws Exception {
		ProcessRouteRes processRoute = null;
		IWorkflowDefine wfDefine = (IWorkflowDefine) NCLocator.getInstance()
				.lookup(IWorkflowDefine.class.getName());

		// TODO: 没有考虑子流程的问题,第三个参数固定传null
		processRoute = wfDefine.queryProcessRoute(billId, pk_billtype, null,
				WorkflowTypeEnum.SubWorkflow.getIntValue());

		String activityXml = (String) processRoute.getXpdlString();
		Map<String, String> activityMap = new HashMap<String, String>();
		Document document = null;
		document = DocumentHelper.parseText(activityXml);

		Map<String, ActivityVo> actMap = new HashMap<String, ActivityVo>();
		ActivityVo activity = null;
		List<Element> activities = document.getRootElement()
				.element("Activities").elements("Activity");
		for (Element act : activities) {
			activity = new ActivityVo();
			activity.setActivityID(act.attributeValue("Id"));
			activity.setActivityName(act.attributeValue("Name"));

			actMap.put(activity.getActivityID(), activity);
		}

		List<ActivityVo> activityList = new ArrayList<ActivityVo>();
		for (ActivityInstance instance : processRoute.getActivityInstance()) {
			if (instance.getStatus() == WfTaskOrInstanceStatus.Finished
					.getIntValue()) {

				activityList.add(actMap.get(instance.getActivityID()));
			}
		}

		return activityList;
	}

	public static TaskMetaData convertToMeta(WorkflownoteVO note) {
		TaskMetaData tmd = new TaskMetaData();

		tmd.setBillType(note.getPk_billtype());
		tmd.setBillId(note.getBillVersionPK());
		tmd.setBillNo(note.getBillno());

		tmd.setCuserid(note.getCheckman());
		tmd.setPk_checkflow(note.getPrimaryKey());
		tmd.setTitle(MessageVO.getMessageNoteAfterI18N(note.getMessagenote()));
		tmd.setStartDate(note.getSenddate().toString());

		tmd.setSendmanid(note.getSenderman());

		return tmd;
	}

}
