package nc.jzmobile.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.logging.Logger;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jzmobile.consts.FilterConditionConsts;
import nc.vo.jzmobile.app.FilterModel;
import nc.vo.pub.BusinessException;

public class FilterConditionUtil {
	
	
	/**
	 * 返回单据的所属模块
	 * @param pks
	 * @param statuskey
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public static List<FilterModel> getModelName(String[] pks,String statuskey) throws BusinessException{
		  List<FilterModel> list=null;//用于返回提交人
		  if(pks.length==0||pks==null){
			   return list;
		  }
		  StringBuffer sql=new StringBuffer();
		  if("ishandled".equals(statuskey)){
			  sql.append("select menuitemcode id,menuitemname name from sm_menuitemreg where menuitemcode in(select DISTINCT substr(menuitemcode,1,length(menuitemcode)-4) parentcode from sm_menuitemreg where funcode in(select nodecode from bd_billtype where pk_billtypecode in(select DISTINCT pk_billtype from pub_workflownote where pk_checkflow in(");
		  }
		  if("submit".equals(statuskey)){
			  sql.append("select menuitemcode id,menuitemname name from sm_menuitemreg where menuitemcode in(select DISTINCT substr(menuitemcode,1,length(menuitemcode)-4) parentcode from sm_menuitemreg where funcode in(select nodecode from bd_billtype where pk_billtypecode in(select DISTINCT billtype from pub_wf_instance WHERE pk_wf_instance in(");
		  }
		  for(int i=0;i<pks.length;i++){
			  sql.append("'"+pks[i]+"',");
		  }
		  sql.deleteCharAt(sql.length()-1);
		  sql.append("))))");
		  try{
			  BaseDAO dao = new BaseDAO();
				list = (List<FilterModel>) dao.executeQuery(sql.toString(),
						new BeanListProcessor(FilterModel.class));
		  }catch(DAOException e){
			  Logger.error(e);
				throw new BusinessException(e); 
		  }
		  return list;
	  }
	
	
	
	   /**
	    * 返回单据的提交人
	    * @param whereSql
	    * @param statuskey
	    * @return
	    * @throws Exception
	    */
	   @SuppressWarnings({ "unchecked" })
	   public static List<FilterModel> getSubmitName(String whereSql,String statuskey) throws Exception{
		   
		   List<FilterModel> list = null; //用于返回提交人
		   StringBuffer sql = new StringBuffer();
		   
		   if("ishandled".equals(statuskey)){
			   sql.append("select sm_user.cuserid id,sm_user.user_name name from( ");
			   sql.append("select instance.billmaker FROM( ");
			   sql.append(whereSql );
			   sql.append(") note ");
			   sql.append("LEFT JOIN PUB_WORKFLOWNOTE workflownote ON note.PK_CHECKFLOW = workflownote.PK_CHECKFLOW ");
			   sql.append("LEFT JOIN PUB_WF_TASK task ON task.pk_wf_task = workflownote.pk_wf_task ");
			   sql.append("LEFT JOIN PUB_WF_INSTANCE instance ON task.pk_wf_instance = instance.pk_wf_instance ");
			   sql.append("GROUP BY instance.billmaker) note ");
			   sql.append("LEFT JOIN SM_USER sm_user ON sm_user.cuserid = note.billmaker ");
		   }
		   
		   if("submit".equals(statuskey)){
			   sql.append("select smuser.cuserid id,smuser.user_name name from ( ");
			   sql.append("select PUBINSTANCE.BILLMAKER from( ");
			   sql.append(whereSql );
			   sql.append(") instance ");
			   sql.append("LEFT JOIN pub_wf_instance pubinstance ON instance.PK_WF_INSTANCE = pubinstance.PK_WF_INSTANCE GROUP BY pubinstance.billmaker ");
			   sql.append(") instance ");
			   sql.append("LEFT JOIN SM_USER smuser ON instance.billmaker = smuser.cuserid ");
		   }
		  
		   try {
				BaseDAO dao = new BaseDAO();
				list = (List<FilterModel>) dao.executeQuery(sql.toString(),
						new BeanListProcessor(FilterModel.class));
			} catch (DAOException e) {
				Logger.error(e);
				throw new BusinessException(e);
			}
		   return list;
	   }
	
	
	
	   /**
	    * 返回单据的项目组织
	    * @param wheresql
	    * @param statuskey
	    * @return
	    * @throws BusinessException
	    */
	   @SuppressWarnings("unchecked")
	   public static List<FilterModel> getOrgName(String wheresql,String statuskey) throws BusinessException{
		   List<FilterModel> list = null;
		   
		   StringBuffer sql = new StringBuffer();
		   
		   if("ishandled".equals(statuskey)){
			   sql.append("select org.pk_org id,CASE WHEN org.shortname IS NOT NULL THEN org.shortname ELSE org.name END name from( ");
			   sql.append("select workflownote.pk_org FROM( ");
			   sql.append(wheresql );
			   sql.append(") note ");
			   sql.append("LEFT JOIN PUB_WORKFLOWNOTE workflownote ");
			   sql.append("ON note.PK_CHECKFLOW = workflownote.PK_CHECKFLOW ");
			   sql.append("GROUP BY workflownote.PK_ORG ");
			   sql.append(") note ");
			   sql.append("LEFT JOIN ORG_ORGS org ");
			   sql.append("ON note.PK_ORG = ORG.PK_ORG ");
			   sql.append("ORDER BY name ");
		   }
		   
		   if("submit".equals(statuskey)){
			   sql.append("select org.pk_org id,CASE WHEN org.shortname IS NOT NULL THEN org.shortname ELSE org.name END name from ( ");
			   sql.append("select PUBINSTANCE.pk_org from( ");
			   sql.append(wheresql );
			   sql.append(") instance ");
			   sql.append("LEFT JOIN pub_wf_instance pubinstance ON instance.PK_WF_INSTANCE = pubinstance.PK_WF_INSTANCE GROUP BY pubinstance.PK_ORG ");
			   sql.append(") instance ");
			   sql.append("LEFT JOIN ORG_ORGS org ON instance.pk_org = org.pk_org ");
		   }
		   
		   try {
				BaseDAO dao = new BaseDAO();
				list = (List<FilterModel>) dao.executeQuery(sql.toString(),
						new BeanListProcessor(FilterModel.class));
			} catch (DAOException e) {
				Logger.error(e);
				throw new BusinessException(e);
			}
		   return list;
	   }
	
	/**
	 * 筛选单据数量
	 * @param statuskey
	 * @param sqlQuery
	 * @param pkOrg
	 * @param billmaker
	 * @param moudles
	 * @return
	 */
	public static String countqueryPkByOrgAndBillmaker(String statuskey,String sqlQuery,String[] pkOrg,String[] billmaker,String[] moudles,String datetype){
		StringBuffer sql=new StringBuffer();
		sql.append(sqlQuery);
		if(pkOrg!=null&&pkOrg.length!=0){
			sql.append(" AND pk_org in(");
			for(int i=0;i<pkOrg.length;i++){
				sql.append("'"+pkOrg[i]+"',");
			}
			sql.deleteCharAt(sql.length()-1);
			sql.append(")");
		}
		/**我提交的*/
		if("submit".equals(statuskey)){
			if(billmaker!=null&&billmaker.length!=0){
				sql.append(" AND billmaker in (");
				for(int i=0;i<billmaker.length;i++){
					sql.append("'"+billmaker[i]+"',");
				}
				sql.deleteCharAt(sql.length()-1);
				sql.append(")");
			}
		/**我审批的*/
		}else{
			if(billmaker!=null&&billmaker.length!=0){
				sql.append(" AND pk_WF_TASK in(select pk_WF_TASK from pub_WF_TASK WHERE PK_WF_INSTANCE in(select PK_WF_INSTANCE from pub_WF_INSTANCE WHERE billmaker in (");
				for(int i=0;i<billmaker.length;i++){
					sql.append("'"+billmaker[i]+"',");
				}
				sql.deleteCharAt(sql.length()-1);
				sql.append(")))");
			}
		}
		/*去掉模块筛选
		if(moudles!=null&&moudles.length!=0){
			sql.append(" And pk_billtype in(select pk_billtypecode from bd_billtype where nodecode in(select funcode from sm_menuitemreg WHERE ismenutype='N' AND(");
			for(int i=0;i<moudles.length;i++){
				sql.append(" menuitemcode LIKE '"+moudles[i]+"%' OR");
			}
			sql.delete(sql.length()-2,sql.length() );
			sql.append(")))");
		}
		*/
		/**筛选当天数据*/
		if(FilterConditionConsts.CURRENTDAY.equals(datetype)){
			String preDay  = getPastDay(0);
			buildSql(sql,preDay);
		/**筛选一周内数据*/
		}else if(FilterConditionConsts.WEEKDAY.equals(datetype)){
			String preDay  = getPastDay(6);
			buildSql(sql,preDay);
		/**筛选一个月内数据*/
		}else if(FilterConditionConsts.MONTHDAY.equals(datetype)){
			String preDay  = getPastDay(29);
			buildSql(sql,preDay);
		/**筛选三个月内数据*/
		}else if(FilterConditionConsts.THREEMONTHDAY.equals(datetype)){
			String preDay  = getPastDay(89);
			buildSql(sql,preDay);
		}
		
		return sql.toString();
	}
	
	
	public static String countqueryPkByOrgAndBillmaker(String statuskey,String sqlQuery,String[] pkOrg,String[] billmaker,String[] moudles){
		StringBuffer sql=new StringBuffer();
		sql.append(sqlQuery);
		if(pkOrg!=null&&pkOrg.length!=0){
			sql.append(" AND pk_org in(");
			for(int i=0;i<pkOrg.length;i++){
				sql.append("'"+pkOrg[i]+"',");
			}
			sql.deleteCharAt(sql.length()-1);
			sql.append(")");
		}
		/**我提交的*/
		if("submit".equals(statuskey)){
			if(billmaker!=null&&billmaker.length!=0){
				sql.append(" AND billmaker in (");
				for(int i=0;i<billmaker.length;i++){
					sql.append("'"+billmaker[i]+"',");
				}
				sql.deleteCharAt(sql.length()-1);
				sql.append(")");
			}
		/**我审批的*/
		}else{
			if(billmaker!=null&&billmaker.length!=0){
				sql.append(" AND pk_WF_TASK in(select pk_WF_TASK from pub_WF_TASK WHERE PK_WF_INSTANCE in(select PK_WF_INSTANCE from pub_WF_INSTANCE WHERE billmaker in (");
				for(int i=0;i<billmaker.length;i++){
					sql.append("'"+billmaker[i]+"',");
				}
				sql.deleteCharAt(sql.length()-1);
				sql.append(")))");
			}
		}
		/*去掉模块筛选
		if(moudles!=null&&moudles.length!=0){
			sql.append(" And pk_billtype in(select pk_billtypecode from bd_billtype where nodecode in(select funcode from sm_menuitemreg WHERE ismenutype='N' AND(");
			for(int i=0;i<moudles.length;i++){
				sql.append(" menuitemcode LIKE '"+moudles[i]+"%' OR");
			}
			sql.delete(sql.length()-2,sql.length() );
			sql.append(")))");
		}
		*/
		
		return sql.toString();
	}
	
	
	/**
	 * 筛选单据列表
	 * @param statuskey
	 * @param sqlQuery
	 * @param pkOrg
	 * @param billmaker
	 * @param moudles
	 * @param startline
	 * @param count
	 * @return
	 * @throws BusinessException
	 */
	public static String queryPkByOrgAndBillmaker(String statuskey,String sqlQuery,String[] pkOrg,String[] billmaker,String[] moudles,String datetype) throws BusinessException{
		
		//StringBuffer sql=new StringBuffer("select * from ( select row_.*, rownum rownum_ from ( ");
		StringBuffer sql = new StringBuffer();
		sql.append(sqlQuery);
		if(pkOrg!=null&&pkOrg.length!=0){
			sql.append(" AND pk_org in(");
			for(int i=0;i<pkOrg.length;i++){
				sql.append("'"+pkOrg[i]+"',");
			}
			sql.deleteCharAt(sql.length()-1);
			sql.append(")");
		}
		/**我提交的*/
		if("submit".equals(statuskey)){
			if(billmaker!=null&&billmaker.length!=0){
				sql.append(" AND billmaker in (");
				for(int i=0;i<billmaker.length;i++){
					sql.append("'"+billmaker[i]+"',");
				}
				sql.deleteCharAt(sql.length()-1);
				sql.append(")");
			}
		/**我审批的*/
		}else{
			if(billmaker!=null&&billmaker.length!=0){
				sql.append(" AND pk_WF_TASK in(select pk_WF_TASK from pub_WF_TASK WHERE PK_WF_INSTANCE in(select PK_WF_INSTANCE from pub_WF_INSTANCE WHERE billmaker in (");
				for(int i=0;i<billmaker.length;i++){
					sql.append("'"+billmaker[i]+"',");
				}
				sql.deleteCharAt(sql.length()-1);
				sql.append(")))");
			}
		}
		/*
		 * 去掉模块筛选
		if(moudles!=null&&moudles.length!=0){
			sql.append(" And pk_billtype in(select pk_billtypecode from bd_billtype where nodecode in(select funcode from sm_menuitemreg WHERE ismenutype='N' AND(");
			for(int i=0;i<moudles.length;i++){
				sql.append(" menuitemcode LIKE '"+moudles[i]+"%' OR");
			}
			sql.delete(sql.length()-2,sql.length() );
			sql.append(")))");
		}*/
		
		/**筛选当天数据*/
		if(FilterConditionConsts.CURRENTDAY.equals(datetype)){
			String preDay  = getPastDay(0);
			buildSql(sql,preDay);
		/**筛选一周内数据*/
		}else if(FilterConditionConsts.WEEKDAY.equals(datetype)){
			String preDay  = getPastDay(6);
			buildSql(sql,preDay);
		/**筛选一个月内数据*/
		}else if(FilterConditionConsts.MONTHDAY.equals(datetype)){
			String preDay  = getPastDay(29);
			buildSql(sql,preDay);
		/**筛选三个月内数据*/
		}else if(FilterConditionConsts.THREEMONTHDAY.equals(datetype)){
			String preDay  = getPastDay(89);
			buildSql(sql,preDay);
		}
		
		return sql.toString();
	}
	
public static String queryPkByOrgAndBillmaker(String statuskey,String sqlQuery,String[] pkOrg,String[] billmaker,String[] moudles) throws BusinessException{
		
		//StringBuffer sql=new StringBuffer("select * from ( select row_.*, rownum rownum_ from ( ");
		StringBuffer sql = new StringBuffer();
		sql.append(sqlQuery);
		if(pkOrg!=null&&pkOrg.length!=0){
			sql.append(" AND pk_org in(");
			for(int i=0;i<pkOrg.length;i++){
				sql.append("'"+pkOrg[i]+"',");
			}
			sql.deleteCharAt(sql.length()-1);
			sql.append(")");
		}
		/**我提交的*/
		if("submit".equals(statuskey)){
			if(billmaker!=null&&billmaker.length!=0){
				sql.append(" AND billmaker in (");
				for(int i=0;i<billmaker.length;i++){
					sql.append("'"+billmaker[i]+"',");
				}
				sql.deleteCharAt(sql.length()-1);
				sql.append(")");
			}
		/**我审批的*/
		}else{
			if(billmaker!=null&&billmaker.length!=0){
				sql.append(" AND pk_WF_TASK in(select pk_WF_TASK from pub_WF_TASK WHERE PK_WF_INSTANCE in(select PK_WF_INSTANCE from pub_WF_INSTANCE WHERE billmaker in (");
				for(int i=0;i<billmaker.length;i++){
					sql.append("'"+billmaker[i]+"',");
				}
				sql.deleteCharAt(sql.length()-1);
				sql.append(")))");
			}
		}
		/*
		 * 去掉模块筛选
		if(moudles!=null&&moudles.length!=0){
			sql.append(" And pk_billtype in(select pk_billtypecode from bd_billtype where nodecode in(select funcode from sm_menuitemreg WHERE ismenutype='N' AND(");
			for(int i=0;i<moudles.length;i++){
				sql.append(" menuitemcode LIKE '"+moudles[i]+"%' OR");
			}
			sql.delete(sql.length()-2,sql.length() );
			sql.append(")))");
		}*/
		
		return sql.toString();
	}
	
	
	
	/**获取前几天数据*/
	private static String getPastDay(int data){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, - data);
		Date day = c.getTime();
		return sdf.format(day);
	}
	
	
	private static void buildSql(StringBuffer sql,String preDay){
		sql.append(" AND ((TO_DATE(ts, 'yyyy-mm-dd hh24:mi:ss')) >= TO_DATE('"+preDay+"', 'yyyy-mm-dd hh24:mi:ss')" +
				"and (TO_DATE(ts, 'yyyy-mm-dd hh24:mi:ss') < trunc(sysdate)+1))");
	}
	

}
