package nc.vo.pf.mobileapp.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.pf.pub.PFRequestDataCacheProxy;
import nc.bs.pf.pub.cache.CondStringKey;
import nc.bs.pf.pub.cache.ICacheDataQueryCallback;
import nc.bs.pf.pub.cache.IRequestDataCacheKey;
import nc.vo.pf.mobileapp.TaskMetaData;
import nc.vo.pf.mobileapp.exception.TaskNotValidException;
import nc.vo.pub.BusinessException;
import nc.vo.pub.msg.MessageVO;
import nc.vo.pub.pf.Pfi18nTools;
import nc.vo.pub.workflownote.WorkflownoteVO;

/**
 * 
 * @author yanke1 2012-7-12
 *
 */
public abstract class AbstractReceivedQuery extends TaskQuery {
	
	protected final static String CHECKMAN = "#checkman#";
	protected final static String WORKFLOWTYPE_IN = "#workflowtype#";
	protected final static String APPROVRESTATUS = "#approvestatus#";
	protected final static String PK_GROUP = "#pk_group#";
	protected final static String SENDDATE = "#senddate#";
	
	private final static String SQL = "select pk_checkflow,senddate from pub_workflownote where actiontype like 'Z%'" +
			" and checkman='" + CHECKMAN + "' " +
			" and workflow_type in "+WORKFLOWTYPE_IN+
			" and approvestatus=" + APPROVRESTATUS +
			//加入pk_goup 避免跨集团审批的情况，用户设为集团共享，不会查询原集团产生的工作项
			//modified by liangyub 2013-08-19
			//wss 去处跨集团处理 20160801
			//" and pk_group ='" + PK_GROUP + "' " +
			" and senddate<'" + SENDDATE + "'" +
			" order by senddate desc";
	
	protected String getBaseSql() {
		return SQL;
	}
	
	
	@Override
	public List<Map<String, Object>> queryByPks(String[] pks) throws BusinessException {
		
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		
		// TODO: 为了效率可以合并为批量查询
		for (String pk : pks) {
			TaskMetaData tmd = queryTaskMetaData(pk);
			Map<String, Object> map = convertToTask(tmd);
			
			list.add(map);
		}
		
		return list;
	}
	
	private Map<String, Object> convertToTask(TaskMetaData tmd) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("taskid", tmd.getPk_checkflow());
		map.put("senddate", tmd.getStartDate());
		map.put("title", tmd.getTitle());
		map.put("dealman", Pfi18nTools.getUserName(tmd.getCuserid()));
		map.put("sendman", Pfi18nTools.getUserName(tmd.getSendmanid()));
		map.put("billtypename", Pfi18nTools.i18nBilltypeName(tmd.getBillType()));
		
		return map;
	}

	@Override
	public TaskMetaData queryTaskMetaData(final String pk) throws BusinessException {
		
		IRequestDataCacheKey key = new CondStringKey(IRequestDataCacheKey.CATEGORY_MA_RECEIVED_QUERY_TASKMETADATA, pk);
		
		ICacheDataQueryCallback<TaskMetaData> callback = new ICacheDataQueryCallback<TaskMetaData>() {

			@Override
			public TaskMetaData queryData() throws BusinessException {
				// TODO: 为了效率可以改为只查询所需字段
				WorkflownoteVO note = (WorkflownoteVO) getQueryService().retrieveByPK(WorkflownoteVO.class, pk);
				
				if (note == null) {
					throw new TaskNotValidException();
				}
				
				return convertToMeta(note);
			}
		};
		
		return PFRequestDataCacheProxy.get(key, callback);
	}
	
	private TaskMetaData convertToMeta(WorkflownoteVO note) {
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
