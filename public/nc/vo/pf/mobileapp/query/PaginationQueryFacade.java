package nc.vo.pf.mobileapp.query;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.common.RuntimeEnv;
import nc.impl.pub.util.db.InSqlManager;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.page.LimitSQLBuilder;
import nc.jdbc.framework.page.SQLBuilderFactory;
import nc.jdbc.framework.processor.ColumnListProcessor;
import nc.jzmobile.utils.BillTypeModelTrans;
import nc.jzmobile.utils.FilterConditionUtil;
import nc.vo.jzmobile.app.FilterModel;
import nc.vo.pf.pub.util.ArrayUtil;
import nc.vo.pub.BusinessException;
import nc.vo.to.pub.util.StringUtils;

/**
 * 用于提供分页查询的接口
 * 
 * @author yanke1 2012-7-12
 * 
 */
public class PaginationQueryFacade {

	/**
	 * 加载pk时默认加载系数。加载的总行数=起始行+请求行数*此系数
	 */
	private static int DEFAULT_LOAD_FACTOR = 1;

	private static PaginationQueryFacade instance = new PaginationQueryFacade();

	private Map<String, WeakReference<PksVO>> requestPksMap = new HashMap<String, WeakReference<PksVO>>();
	private IUAPQueryBS qry = NCLocator.getInstance().lookup(IUAPQueryBS.class);

	private PaginationQueryFacade() {
	}

	public static PaginationQueryFacade getInstance() {
		return instance;
	}

	/**
	 * 分页查询的方法：先查询出所有的pk，再根据startline和count对应的pk查出实体
	 * 
	 * 由于手机端两次查询的时间间隔较短(大概在5s左右)，因此方法里以query的id为key，针对pk[]做了一个缓存
	 * 
	 * @param <T>
	 * @param query
	 * @param startline
	 * @param count
	 * @return
	 * @throws BusinessException
	 */
	public <T> List<T> query(IPaginationQuery<T> query, int startline, int count)
			throws BusinessException {
		String requestId = query.getIdentifier();
		PksVO pksvo = null;

		WeakReference<PksVO> ref = requestPksMap.get(requestId);
		if (ref != null) {
			pksvo = ref.get();
		}

		if (pksvo == null || !pksvo.includes(startline, count)) {
			pksvo = queryPks(query, startline, count);

			// yanke1 对于频繁变化的东西，比如我的待审批任务，不宜使用缓存，因为手机上可能随时进行审批
			// 此时如果有缓存的话，审批后的任务还会出现在待审批中
			// 因此先取消掉缓存
			// 考虑后续增加一个requestInterval因素
			// 对于interval小于某个时间的，可以认为是手机上进行分页查询
			// 这时才启用缓存
			// TODO:
			// requestPksMap.put(requestId, new WeakReference<PksVO>(pksvo));
		}

		String[] realPks = pksvo.getPksOf(startline, count);

		return query.queryByPks(realPks);
	}
	
	
	/**
	 * 
	 * 查询当前操作人的所有单据中的组织以及制单人
	 * @param query
	 * @param condition
	 * @return
	 * @throws Exception 
	 */
	public Map<String,List<FilterModel>> queryOrgAndBillmaker(TaskQuery query, String condition,String statuskey) throws Exception{
		Map<String,List<FilterModel>> map=new HashMap<String,List<FilterModel>>();
		String sql = query.getPksSql();
		//根据单据类型进行sql过滤
		List<FilterModel> map1=FilterConditionUtil.getOrgName(sql, statuskey);
		List<FilterModel> map2=FilterConditionUtil.getSubmitName(sql, statuskey);
		List<FilterModel> map3=null;
		map.put("org", map1);
		map.put("billmaker", map2);
		map.put("moudles", map3);
		return map;
	}
	
	public Integer query(TaskQuery query,String condition)
			throws BusinessException {
		
		return queryPks(query, condition);
	}

	private PksVO queryPks(IPaginationQuery query, int startLine, int count)
			throws BusinessException {
		int loadCount = calculateLoadCount(startLine, count);

		if (RuntimeEnv.getInstance().isRunningInServer()) {
			// 2012-9-6
			// 设这个分支主要原因是暂不确定basedao在前台能否获得dbtype
			int dbtype = new BaseDAO(InvocationInfoProxy.getInstance().getUserDataSource()).getDBType();
			LimitSQLBuilder builder = SQLBuilderFactory.getInstance()
					.createLimitSQLBuilder(dbtype);

			String sql = builder.build(query.getPksSql(), 1, loadCount);
			String[] pks = queryPks(sql);

			PksVO pksvo = new PksVO();
			pksvo.setPks(pks);
			pksvo.setStartIdx(0);

			if (pks.length < loadCount) {
				pksvo.setTotalCount(pks.length);
			}

			return pksvo;
		} else {
			// 通常不会走到这
			String sql = query.getPksSql();

			String[] pks = queryPks(sql);

			PksVO pksvo = new PksVO();
			pksvo.setPks(pks);
			pksvo.setStartIdx(0);
			pksvo.setTotalCount(pks.length);

			return pksvo;
		}
	}

	private String[] queryPks(String sql) throws BusinessException {
		List<String> pksList = (List<String>) qry.executeQuery(sql,
				new ColumnListProcessor());

		String[] pks = null;
		if (ArrayUtil.isNull(pksList)) {
			pks = new String[0];
		} else {
			pks = pksList.toArray(new String[pksList.size()]);
		}

		return pks;
	}

	private int calculateLoadCount(int startLine, int count) {
		return startLine + count * DEFAULT_LOAD_FACTOR;
	}

	public List<Map<String, Object>> query(TaskQuery query, String condition,
			int startline, int count) throws BusinessException {
		String requestId = query.getIdentifier();
		PksVO pksvo = null;

		WeakReference<PksVO> ref = requestPksMap.get(requestId);
		if (ref != null) {
			pksvo = ref.get();
		}

		if (pksvo == null || !pksvo.includes(startline, count)) {
			pksvo = queryPks(query, condition, startline, count);

			// yanke1 对于频繁变化的东西，比如我的待审批任务，不宜使用缓存，因为手机上可能随时进行审批
			// 此时如果有缓存的话，审批后的任务还会出现在待审批中
			// 因此先取消掉缓存
			// 考虑后续增加一个requestInterval因素
			// 对于interval小于某个时间的，可以认为是手机上进行分页查询
			// 这时才启用缓存
			// TODO:
			// requestPksMap.put(requestId, new WeakReference<PksVO>(pksvo));
		}

		String[] realPks = pksvo.getPksOf(startline, count);

		return query.queryByPks(realPks);
	}
	
	
	/**
	 * 
	 * 
	 * 2018/03/17 mxx
	 * @param query
	 * @param condition
	 * @param startline
	 * @param count
	 * @param pkOrg
	 * @param billmaker
	 * @return
	 * @throws BusinessException
	 */
	public List<Map<String, Object>> query(TaskQuery query,int startline,int count,String condition,
		String[] pkOrg,String[] billmaker,String[] moudles,String statuskey) throws BusinessException {
		String[] realPks = queryPks(query, condition,statuskey,pkOrg,billmaker,moudles,startline,count);
		if(realPks==null){
			realPks=new String[]{};
		}
		return query.queryByPks(realPks);
	}
	
	
	/**
	 * mxx 2018/03/17
	 * 定义查询审批人的所有待审单据/已办单据
	 * @param query
	 * @param condition
	 * @return
	 * @throws BusinessException
	 */
   private String[] queryPks(TaskQuery query, String condition,String statuskey,String[] pkOrg,String[] billmaker,String[] moudles,int startline,int count) throws BusinessException{
	   
	   String sql = query.getPksSql(); 
	   
	   StringBuffer buffer = new StringBuffer();
	   
	   
	   /**去掉order by*/
	   int strat = sql.indexOf("order by");
	   String querySql = sql.substring(strat, sql.length());
	   sql = sql.substring(0, strat);
	   
	   /**先过滤，再进行分页*/
		sql = this.filterBillTypeSql(query,sql,condition);
		sql=FilterConditionUtil.queryPkByOrgAndBillmaker(statuskey,sql, pkOrg, billmaker,moudles,startline,count);
		buffer.append(sql);
		/**添加排序*/
		buffer.append(querySql);
		LimitSQLBuilder builder = SQLBuilderFactory.getInstance()
				.createLimitSQLBuilder(new BaseDAO().getDBType());
		/**获取当前页数*/
		startline = startline/count + 1;
		/**获得分页sql*/
		sql = builder.build(buffer.toString(),startline,count);
		String[] pks = queryPks(sql);
		return pks; 
   }
   
   /**
    * if(sql.contains("workflow_type=")){
				   sql=sql.replaceAll("workflow_type=", "workflow_type in ");   
			}
    * */
   
   /**
	 * 
	 * 根据标题和制单人筛选出的数量
	 * @param query
	 * @param condition
	 * @param pkOrg
	 * @param billmaker
	 * @param statuskey
	 * @return
	 * @throws BusinessException
	 */
   public Integer query(TaskQuery query,String condition,String[] pkOrg,String[] billmaker,String[] moudles,String statuskey)
			throws BusinessException {
		
		return queryPks(query, condition,pkOrg,billmaker,moudles,statuskey);
	}
   
   private Integer queryPks(TaskQuery query, String condition,String[] pkOrg,String[] billmaker,String[] moudles,String statuskey) throws BusinessException {
		
		String sql = query.getPksSql();
		
		/**去掉order by*/
		int strat = sql.indexOf("order by");
		String querySql = sql.substring(strat, sql.length());
		sql = sql.substring(0, strat);
		sql = this.filterBillTypeSql(query,sql,condition);
		sql=FilterConditionUtil.countqueryPkByOrgAndBillmaker(statuskey,sql, pkOrg, billmaker,moudles);
		querySql=this.filterBillTypeSql(query,sql, condition);
		String[] pks = queryPks(querySql);
		return pks.length;
		
	}
	
	private Integer queryPks(TaskQuery query, String condition) throws BusinessException {
		
		String sql = query.getPksSql();
		String querySql = this.filterBillTypeSql(query,sql, condition);
		String[] pks = queryPks(querySql);

		return pks.length;
		
	}

	private PksVO queryPks(TaskQuery query, String condition, int startLine,
			int count) throws BusinessException {
		int loadCount = calculateLoadCount(startLine, count);

		if (RuntimeEnv.getInstance().isRunningInServer()) {
			// 2012-9-6
			// 设这个分支主要原因是暂不确定basedao在前台能否获得dbtype
			int dbtype = new BaseDAO().getDBType();
			LimitSQLBuilder builder = SQLBuilderFactory.getInstance()
					.createLimitSQLBuilder(dbtype);

			String sql = builder.build(query.getPksSql(), 1, loadCount);

			String querySql = this.filterBillTypeSql(query,sql, condition);
			String[] pks = queryPks(querySql);

			PksVO pksvo = new PksVO();
			pksvo.setPks(pks);
			pksvo.setStartIdx(0);

			if (pks.length < loadCount) {
				pksvo.setTotalCount(pks.length);
			}

			return pksvo;
		} else {
			// 通常不会走到这
			String sql = query.getPksSql();

			String[] pks = queryPks(sql);

			PksVO pksvo = new PksVO();
			pksvo.setPks(pks);
			pksvo.setStartIdx(0);
			pksvo.setTotalCount(pks.length);

			return pksvo;
		}
	}

	private String filterBillTypeSql(TaskQuery query,String sql, String condition)
			throws BusinessException {
		// 手动建的表，为了存哪些做过移动审批的适配
//		String yusql = "select billTypeCode  from uap_mobile_billtype ";
//		List<String> codeList = (List<String>) qry.executeQuery(yusql,
//				new ColumnListProcessor());
		//修改从xml读取单据code
		List<String> codeList = BillTypeModelTrans.getInstance().getMobileApproveBillTypeList();
		if (codeList == null || codeList.size() < 0) {
			return sql;
		}
		if(query.getClass().getName().indexOf("Received")!=-1)
			return filterReceivedBillTypeSql(codeList,sql,condition);
		else
			return filterSubmittedBillTypeSql(codeList,sql,condition);
		
	}
	private String filterReceivedBillTypeSql(List<String> codeList,String sql, String condition) throws BusinessException{
		StringBuffer wherePart = new StringBuffer();
		wherePart.append(" pub_workflownote where pk_billtype in ").append(InSqlManager.getInSQLValue(codeList)).append(" and");

		if (!StringUtils.isEmpty(condition)) {
			String sql1 = "select pk_billtypecode  from bd_billtype where billtypename like '%"+ condition + "%'";
			List<String> pksList = (List<String>) qry.executeQuery(sql1, new ColumnListProcessor());
			wherePart.append(" (");
			wherePart.append(" messagenote like '%").append(condition).append("%'");
			wherePart.append(" or");
			if (pksList != null && pksList.size() > 0) {
				//codeList.addAll(pksList);
				wherePart.append(" pk_billtype in ").append(InSqlManager.getInSQLValue(pksList));
				wherePart.append(" or");
				wherePart.append(" senderman in (select cuserid from sm_user where user_name like '%"+ condition + "%')");
			} else {
				wherePart.append(" senderman in (select cuserid from sm_user where user_name like '%"+ condition + "%')");
			}
			wherePart.append(") and");
		}

		return sql.replace("pub_workflownote where", wherePart.toString());
	}
	
	private String filterSubmittedBillTypeSql(List<String> codeList,String sql, String condition) throws BusinessException{
		StringBuffer wherePart = new StringBuffer();
		wherePart.append(" pub_wf_instance where billtype in ").append(InSqlManager.getInSQLValue(codeList)).append(" and");

		if (!StringUtils.isEmpty(condition)) {
			String sql1 = "select pk_billtypecode  from bd_billtype where billtypename like '%"+ condition + "%'";
			List<String> pksList = (List<String>) qry.executeQuery(sql1, new ColumnListProcessor());
//			wherePart.append(" ( messagenote like '%").append(condition)
//					.append("%' and ");
			if (pksList != null && pksList.size() > 0) {
				//codeList.addAll(pksList);
				wherePart.append("(");
				wherePart.append(" billtype in (").append(InSqlManager.getInSQLValue(pksList)).append(")");
				wherePart.append(" or");
				wherePart.append(" billcommiter in (select cuserid from sm_user where user_name like '%"+ condition + "%')");
				wherePart.append(") and");
			} else {
				wherePart.append(" billcommiter in (select cuserid from sm_user where user_name like '%"+ condition + "%')");
				wherePart.append(" and");
			}
		}

		return sql.replace("pub_wf_instance where", wherePart.toString());
	}

	
}

