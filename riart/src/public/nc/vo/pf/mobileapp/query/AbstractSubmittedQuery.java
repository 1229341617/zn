package nc.vo.pf.mobileapp.query;

import java.util.List;
import java.util.Map;


import nc.bs.pf.pub.PFRequestDataCacheProxy;
import nc.bs.pf.pub.cache.CondStringKey;
import nc.bs.pf.pub.cache.ICacheDataQueryCallback;
import nc.bs.pf.pub.cache.IRequestDataCacheKey;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ArrayProcessor;
import nc.jdbc.framework.processor.ColumnListProcessor;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pf.mobileapp.MobileAppUtil;
import nc.vo.pf.mobileapp.TaskMetaData;
import nc.vo.pf.mobileapp.exception.TaskNotValidException;
import nc.vo.pub.BusinessException;
import nc.vo.pub.pf.Pfi18nTools;
import nc.vo.jcom.lang.StringUtil;

/**
 * 
 * @author yanke1 2012-7-12
 * 
 */
public abstract class AbstractSubmittedQuery extends TaskQuery {

	protected final static String BILLMAKER = "#billmaker#";
	protected final static String WORKFLOWTYPE = "#workflowtype#";
	protected final static String PROCSTATUS = "#procstatus#";
	protected final static String PK_GROUP = "#pk_group#";
	protected final static String STARTTS = "#startts#";

	/**
	 * 排除掉其他集团产生的，也是改制单人发起的流程实例 modified by liangyub 2013-08-19
	 * */
	protected final static String GROUP_SQL = "select distinct task.pk_wf_instance"
			+ " from pub_workflownote note inner join pub_wf_task task"
			+ " on note.pk_wf_task = task.pk_wf_task where note.pk_group ='"
			+ PK_GROUP + "'";

	private final static String SQL = "select pk_wf_instance,startts from pub_wf_instance where"
			+ " billmaker='"
			+ BILLMAKER
			+ "'"
			+ " and workflow_type in "
			+ WORKFLOWTYPE
			+ " and procstatus="
			+ PROCSTATUS
			+ " and pk_wf_instance in ("
			+ GROUP_SQL
			+ ")"
			+ " and startts < '"
			+ STARTTS + "' " + " order by startts desc";

	protected String getBaseSQL() {
		if(!StringUtil.isEmptyWithTrim(getCondition())){
			String group_sql = "select distinct task.pk_wf_instance" +
					" from pub_wf_task task inner join pub_workflownote note" +
					" on note.pk_wf_task = task.pk_wf_task " +
					" left join sm_msg_content msg on note.pk_checkflow = msg.pk_detail " +
					" where note.pk_group ='"+ PK_GROUP +"'" + 
					" and msg.subject like '%"+getCondition()+"%'";
			String sql = "select pk_wf_instance,startts from pub_wf_instance where" +
					" billmaker='" + BILLMAKER + "'" +
					" and workflow_type in " + WORKFLOWTYPE + 
					" and procstatus=" + PROCSTATUS +
					" and pk_wf_instance in (" + group_sql + ")" +
					" and startts < '" + STARTTS + "' " +
					" order by startts desc";
			return sql;
		}else{
			return SQL;
		}
	}

	@Override
	public List<Map<String, Object>> queryByPks(String[] pks)
			throws BusinessException {
		List<Map<String, Object>> list = MobileAppUtil.createArrayList();

		// TODO: 为了效率可以改为批量查询
		for (String pk : pks) {
			TaskMetaData tmd = queryTaskMetaData(pk);
			Map<String, Object> map = convertToTask(tmd);

			list.add(map);
		}

		return list;
	}

	private Map<String, Object> convertToTask(TaskMetaData tmd) {
		Map<String, Object> map = MobileAppUtil.createHashMap();

		map.put("taskid", tmd.getPk_wf_instance());
		map.put("title", tmd.getTitle());
		map.put("date", tmd.getStartDate());

		// add by liangyub 因为流程中取不到这些值，但是接口要求有，所以放入默认值
		map.put("priority", "");
		map.put("isreminder", "N");
		// add by zhangwxe 移动应用新增
		map.put("senddate", tmd.getStartDate());

		String dealMan = Pfi18nTools.getUserName(tmd.getCuserid());
		if (dealMan == null) {
			dealMan = "";
		}
		map.put("dealman", dealMan);

		String sendMan = Pfi18nTools.getUserName(tmd.getSendmanid());
		if (sendMan == null) {
			sendMan = "";
		}
		map.put("sendman", sendMan);
		map.put("billtypename", Pfi18nTools.i18nBilltypeName(tmd.getBillType()));

		return map;
	}

	@Override
	public TaskMetaData queryTaskMetaData(final String pk)
			throws BusinessException {
		IRequestDataCacheKey key = new CondStringKey(
				IRequestDataCacheKey.CATEGORY_MA_SUBMITTED_QUERY_TASKMETADATA,
				pk);
		ICacheDataQueryCallback<TaskMetaData> callback = new ICacheDataQueryCallback<TaskMetaData>() {

			@Override
			public TaskMetaData queryData() throws BusinessException {
				String sql = "select billmaker, billno, billtype, billversionpk, startts from pub_wf_instance where pk_wf_instance=?";

				SQLParameter param = new SQLParameter();
				param.addParam(pk);

				Object[] result = (Object[]) getQueryService().executeQuery(
						sql, param, new ArrayProcessor());

				if (result == null) {
					throw new TaskNotValidException();
				}

				TaskMetaData tmd = new TaskMetaData();

				tmd.setCuserid((String) result[0]);
				tmd.setBillNo((String) result[1]);
				tmd.setBillType((String) result[2]);
				tmd.setBillId((String) result[3]);
				tmd.setStartDate((String) result[4]);
				tmd.setPk_wf_instance(pk);

				fillTitle(tmd);

				return tmd;
			}
		};

		return PFRequestDataCacheProxy.get(key, callback);
	}

	@SuppressWarnings("rawtypes")
	private void fillTitle(TaskMetaData tmd) throws BusinessException {
		String sql = "select top 2 w.checkman from pub_workflownote w join pub_wf_task k on w.pk_wf_task=k.pk_wf_task  where k.pk_wf_instance=? order by w.senddate asc";

		SQLParameter param = new SQLParameter();
		param.addParam(tmd.getPk_wf_instance());

		List result = (List) getQueryService().executeQuery(sql, param,
				new ColumnListProcessor());

		StringBuffer sb = new StringBuffer();

		String billTypeName = Pfi18nTools.i18nBilltypeName(tmd.getBillType());

		sb.append(billTypeName);
		sb.append(", ");
		sb.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("mobileapp",
				"AbstractSubmittedQuery-000000")/* 单据号 */);
		sb.append(": ");
		sb.append(tmd.getBillNo());

		// yanke1 2012-10-12 去掉标题中的提交时间字样
		// sb.append(", ");
		// sb.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("mobileapp",
		// "AbstractSubmittedQuery-000001")/*提交时间*/);
		// sb.append(": ");
		// sb.append(tmd.getStartDate());

		if (result == null || result.size() == 0) {
			// do nothing
		} else if (result.size() > 1) {
			sb.append(", ");
			String cuserid = (String) result.get(0);
			String text = NCLangRes4VoTransl.getNCLangRes().getStrByID(
					"mobileapp", "AbstractSubmittedQuery-000002", null,
					new String[] { Pfi18nTools.getUserName(cuserid) })/*
																	 * 当前处理人:
																	 * {0}等
																	 */;
			sb.append(text);

		} else {
			sb.append(", ");
			String cuserid = (String) result.get(0);
			String text = NCLangRes4VoTransl.getNCLangRes().getStrByID(
					"mobileapp", "AbstractSubmittedQuery-000003", null,
					new String[] { Pfi18nTools.getUserName(cuserid) })/*
																	 * 当前处理人:
																	 * {0}
																	 */;
			sb.append(text);
		}

		tmd.setTitle(sb.toString());
	}

}
