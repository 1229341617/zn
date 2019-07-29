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
 * �����ṩ��ҳ��ѯ�Ľӿ�
 * 
 * @author yanke1 2012-7-12
 * 
 */
public class PaginationQueryFacade {

	/**
	 * ����pkʱĬ�ϼ���ϵ�������ص�������=��ʼ��+��������*��ϵ��
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
	 * ��ҳ��ѯ�ķ������Ȳ�ѯ�����е�pk���ٸ���startline��count��Ӧ��pk���ʵ��
	 * 
	 * �����ֻ������β�ѯ��ʱ�����϶�(�����5s����)����˷�������query��idΪkey�����pk[]����һ������
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

			// yanke1 ����Ƶ���仯�Ķ����������ҵĴ��������񣬲���ʹ�û��棬��Ϊ�ֻ��Ͽ�����ʱ��������
			// ��ʱ����л���Ļ�������������񻹻�����ڴ�������
			// �����ȡ��������
			// ���Ǻ�������һ��requestInterval����
			// ����intervalС��ĳ��ʱ��ģ�������Ϊ���ֻ��Ͻ��з�ҳ��ѯ
			// ��ʱ�����û���
			// TODO:
			// requestPksMap.put(requestId, new WeakReference<PksVO>(pksvo));
		}

		String[] realPks = pksvo.getPksOf(startline, count);

		return query.queryByPks(realPks);
	}
	
	
	/**
	 * 
	 * ��ѯ��ǰ�����˵����е����е���֯�Լ��Ƶ���
	 * @param query
	 * @param condition
	 * @return
	 * @throws Exception 
	 */
	public Map<String,List<FilterModel>> queryOrgAndBillmaker(TaskQuery query, String condition,String statuskey) throws Exception{
		Map<String,List<FilterModel>> map=new HashMap<String,List<FilterModel>>();
		String sql = query.getPksSql();
		//���ݵ������ͽ���sql����
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
			// �������֧��Ҫԭ�����ݲ�ȷ��basedao��ǰ̨�ܷ���dbtype
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
			// ͨ�������ߵ���
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

			// yanke1 ����Ƶ���仯�Ķ����������ҵĴ��������񣬲���ʹ�û��棬��Ϊ�ֻ��Ͽ�����ʱ��������
			// ��ʱ����л���Ļ�������������񻹻�����ڴ�������
			// �����ȡ��������
			// ���Ǻ�������һ��requestInterval����
			// ����intervalС��ĳ��ʱ��ģ�������Ϊ���ֻ��Ͻ��з�ҳ��ѯ
			// ��ʱ�����û���
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
	 * �����ѯ�����˵����д��󵥾�/�Ѱ쵥��
	 * @param query
	 * @param condition
	 * @return
	 * @throws BusinessException
	 */
   private String[] queryPks(TaskQuery query, String condition,String statuskey,String[] pkOrg,String[] billmaker,String[] moudles,int startline,int count) throws BusinessException{
	   
	   String sql = query.getPksSql(); 
	   
	   StringBuffer buffer = new StringBuffer();
	   
	   
	   /**ȥ��order by*/
	   int strat = sql.indexOf("order by");
	   String querySql = sql.substring(strat, sql.length());
	   sql = sql.substring(0, strat);
	   
	   /**�ȹ��ˣ��ٽ��з�ҳ*/
		sql = this.filterBillTypeSql(query,sql,condition);
		sql=FilterConditionUtil.queryPkByOrgAndBillmaker(statuskey,sql, pkOrg, billmaker,moudles,startline,count);
		buffer.append(sql);
		/**�������*/
		buffer.append(querySql);
		LimitSQLBuilder builder = SQLBuilderFactory.getInstance()
				.createLimitSQLBuilder(new BaseDAO().getDBType());
		/**��ȡ��ǰҳ��*/
		startline = startline/count + 1;
		/**��÷�ҳsql*/
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
	 * ���ݱ�����Ƶ���ɸѡ��������
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
		
		/**ȥ��order by*/
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
			// �������֧��Ҫԭ�����ݲ�ȷ��basedao��ǰ̨�ܷ���dbtype
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
			// ͨ�������ߵ���
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
		// �ֶ����ı�Ϊ�˴���Щ�����ƶ�����������
//		String yusql = "select billTypeCode  from uap_mobile_billtype ";
//		List<String> codeList = (List<String>) qry.executeQuery(yusql,
//				new ColumnListProcessor());
		//�޸Ĵ�xml��ȡ����code
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

