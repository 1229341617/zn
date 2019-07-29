package nc.jzmobile.bill.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.logging.Logger;
import nc.jdbc.framework.page.LimitSQLBuilder;
import nc.jdbc.framework.page.SQLBuilderFactory;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.md.model.IBusinessEntity;
import nc.uap.pf.metadata.PfMetadataTools;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pub.BusinessException;

public class GetDataListHandler implements INCMobileServletHandler {
	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		Logger.info("---GetDataListHandler  start---");
		try{
			String userId = map.get("userid");
			if(userId==null){
				throw new BusinessException("用户信息不能为空！");
			}	
			
			InvocationInfoProxy.getInstance().setGroupId(JZMobileAppUtils.getPkGroupByUserId(userId));
			//获得分页sql
			String sql = getPaginationSql(result,map);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> list = (List<Map<String, Object>>) new BaseDAO()
					.executeQuery(sql, new MapListProcessor());
			
			result.success().setData(list);
			
		}catch(Exception e){
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		return result;
	}
	/**
	 * 根据billType查询对应的主表表名
	 * @param billType
	 * @return
	 * @throws Exception
	 */
	private String getTableName(String billType)throws Exception{
		
		IBusinessEntity be = PfMetadataTools.queryMetaOfBilltype(billType);
		
		Class c = Class.forName(be.getFullClassName());
		
		String tableName = (String) c.getField("TABLENAME").get(c);
		
		if(tableName==null)
			throw new BusinessException("根据billType查询对应的主表表名为空");
		
		return tableName;
	}
	/**
	 * 获得分页sql
	 * @param result
	 * @param map
	 * @return
	 * @throws Exception
	 */
	private String getPaginationSql(Result result,Map<String,String> map) throws Exception{
		String modelCode = map.get("modecode");
		if(modelCode==null){
			throw new BusinessException("模板编码不能为空！");
		}
		String billType = map.get("billtype");
		if(billType==null){
			throw new BusinessException("单据类型不能为空！");
		}
		String pageIndex = map.get("pageindex");
		if(pageIndex==null){
			throw new BusinessException("当前页数不能为空！");
		}
		String pageSize = map.get("pagesize");
		if(pageSize==null){
			throw new BusinessException("每页条数不能为空！");
		}
		//获得查询条件
		String bisinvoiced = map.get("bisinvoiced");
	    //获得表名
		String tableName = getTableName(billType);
		//根据模板编码查询该模板中定义的所有查询字段
		List<String> list = getQueryTemplets(modelCode);
		
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(" select * from "+tableName+" where 1=1");
		//拼接sql条件
		if(bisinvoiced!=null&&list.contains("bisinvoiced"))
			buffer.append(" and bisinvoiced='"+bisinvoiced+"'");
		
		buffer.append(" and dr=0 order by ts desc");
	
		LimitSQLBuilder builder = SQLBuilderFactory.getInstance()
				.createLimitSQLBuilder(new BaseDAO().getDBType());
		//获得分页sql	
		String sql = builder.build(buffer.toString(), Integer.parseInt(pageIndex), Integer.parseInt(pageSize));
		
		return sql;
	}
	/**
	 * 根据模板编码查询该模板中定义的所有查询字段
	 * @param modelName
	 * @return
	 */
	private List<String> getQueryTemplets(String modelCode)throws BusinessException{
		Logger.info("根据模板编码查询该模板中定义的所有查询字段 --start--");
		StringBuffer buffer = new StringBuffer();
		buffer.append(" select id from pub_query_templet where ");
		buffer.append(" model_code='"+modelCode+"'");

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) new BaseDAO()
				.executeQuery(buffer.toString(), new MapListProcessor());
		
		if(list==null||list.size()==0)
			throw new BusinessException("根据模板编码查询不到该模板，请确认模板名称是否正确！");
		
		String id = (String) list.get(0).get("id");
		
		StringBuffer conBuffer = new StringBuffer();
		conBuffer.append(" select field_code from pub_query_condition where ");
		conBuffer.append(" pk_templet='"+id+"'");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> conList = (List<Map<String, Object>>) new BaseDAO()
				.executeQuery(conBuffer.toString(), new MapListProcessor());
		
		if(conList==null||conList.size()==0)
			throw new BusinessException("模板编码为【"+modelCode+"】的模板定义的查询字段为空！");
		List<String> resultList = new ArrayList<String>();
		for(Map<String,Object> map:conList){
			resultList.add((String) map.get("field_code"));
		}
		return resultList;
	}
}
