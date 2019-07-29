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
				throw new BusinessException("�û���Ϣ����Ϊ�գ�");
			}	
			
			InvocationInfoProxy.getInstance().setGroupId(JZMobileAppUtils.getPkGroupByUserId(userId));
			//��÷�ҳsql
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
	 * ����billType��ѯ��Ӧ���������
	 * @param billType
	 * @return
	 * @throws Exception
	 */
	private String getTableName(String billType)throws Exception{
		
		IBusinessEntity be = PfMetadataTools.queryMetaOfBilltype(billType);
		
		Class c = Class.forName(be.getFullClassName());
		
		String tableName = (String) c.getField("TABLENAME").get(c);
		
		if(tableName==null)
			throw new BusinessException("����billType��ѯ��Ӧ���������Ϊ��");
		
		return tableName;
	}
	/**
	 * ��÷�ҳsql
	 * @param result
	 * @param map
	 * @return
	 * @throws Exception
	 */
	private String getPaginationSql(Result result,Map<String,String> map) throws Exception{
		String modelCode = map.get("modecode");
		if(modelCode==null){
			throw new BusinessException("ģ����벻��Ϊ�գ�");
		}
		String billType = map.get("billtype");
		if(billType==null){
			throw new BusinessException("�������Ͳ���Ϊ�գ�");
		}
		String pageIndex = map.get("pageindex");
		if(pageIndex==null){
			throw new BusinessException("��ǰҳ������Ϊ�գ�");
		}
		String pageSize = map.get("pagesize");
		if(pageSize==null){
			throw new BusinessException("ÿҳ��������Ϊ�գ�");
		}
		//��ò�ѯ����
		String bisinvoiced = map.get("bisinvoiced");
	    //��ñ���
		String tableName = getTableName(billType);
		//����ģ������ѯ��ģ���ж�������в�ѯ�ֶ�
		List<String> list = getQueryTemplets(modelCode);
		
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(" select * from "+tableName+" where 1=1");
		//ƴ��sql����
		if(bisinvoiced!=null&&list.contains("bisinvoiced"))
			buffer.append(" and bisinvoiced='"+bisinvoiced+"'");
		
		buffer.append(" and dr=0 order by ts desc");
	
		LimitSQLBuilder builder = SQLBuilderFactory.getInstance()
				.createLimitSQLBuilder(new BaseDAO().getDBType());
		//��÷�ҳsql	
		String sql = builder.build(buffer.toString(), Integer.parseInt(pageIndex), Integer.parseInt(pageSize));
		
		return sql;
	}
	/**
	 * ����ģ������ѯ��ģ���ж�������в�ѯ�ֶ�
	 * @param modelName
	 * @return
	 */
	private List<String> getQueryTemplets(String modelCode)throws BusinessException{
		Logger.info("����ģ������ѯ��ģ���ж�������в�ѯ�ֶ� --start--");
		StringBuffer buffer = new StringBuffer();
		buffer.append(" select id from pub_query_templet where ");
		buffer.append(" model_code='"+modelCode+"'");

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) new BaseDAO()
				.executeQuery(buffer.toString(), new MapListProcessor());
		
		if(list==null||list.size()==0)
			throw new BusinessException("����ģ������ѯ������ģ�壬��ȷ��ģ�������Ƿ���ȷ��");
		
		String id = (String) list.get(0).get("id");
		
		StringBuffer conBuffer = new StringBuffer();
		conBuffer.append(" select field_code from pub_query_condition where ");
		conBuffer.append(" pk_templet='"+id+"'");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> conList = (List<Map<String, Object>>) new BaseDAO()
				.executeQuery(conBuffer.toString(), new MapListProcessor());
		
		if(conList==null||conList.size()==0)
			throw new BusinessException("ģ�����Ϊ��"+modelCode+"����ģ�嶨��Ĳ�ѯ�ֶ�Ϊ�գ�");
		List<String> resultList = new ArrayList<String>();
		for(Map<String,Object> map:conList){
			resultList.add((String) map.get("field_code"));
		}
		return resultList;
	}
}
