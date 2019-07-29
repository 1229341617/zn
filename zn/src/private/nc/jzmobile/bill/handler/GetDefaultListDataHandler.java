package nc.jzmobile.bill.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.mobile.app.IMobileBillDetailQuery;
import nc.jdbc.framework.page.LimitSQLBuilder;
import nc.jdbc.framework.page.SQLBuilderFactory;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jzmobile.bill.util.BillMetaUtil;
import nc.jzmobile.bill.util.BillTempletUtil;
import nc.jzmobile.bill.util.OrderQueryUtil;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.vo.am.common.util.StringUtils;
import nc.vo.jzmobile.app.MobileBillData;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;

/**
 * 查询默认列表数据
 * 
 * @author wangruin on 2017/8/7
 * 
 */
public class GetDefaultListDataHandler implements INCMobileServletHandler {

	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		Logger.info("---GetDefaultListDataHandler  start---");
		try {
			String userId = map.get("userid");
			if (userId == null) {
				throw new BusinessException("用户信息不能为空！");
			}
			String billType = map.get("billtype");
			if (billType == null) {
				throw new BusinessException("单据类型不能为空！");
			}

			InvocationInfoProxy.getInstance().setGroupId(
					JZMobileAppUtils.getPkGroupByUserId(userId));
			
			result.success()
			.setData(getPaginationDataList(result, map, userId));

		} catch (Exception e) {
			Logger.error(e);
			e.printStackTrace();
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		return result;
	}


	/**
	 * 获得分页数据list
	 * 
	 * @param result
	 * @param map
	 * @return
	 * @throws Exception
	 */
	private List<MobileBillData> getPaginationDataList(Result result,
			Map<String, String> map, String userId) throws Exception {
		/*String modelCode = map.get("modecode");*/

		String billType = map.get("billtype");
		if (billType == null) {
			throw new BusinessException("单据类型不能为空！");
		}
		String pageIndex = map.get("pageindex");
		if (pageIndex == null) {
			throw new BusinessException("当前页数不能为空！");
		}
		String pageSize = map.get("pagesize");
		if (pageSize == null) {
			throw new BusinessException("每页条数不能为空！");
		}
		
		// 获得表名
		String tableName = BillMetaUtil.getTableName(billType);
		
		StringBuffer buffer = new StringBuffer();

		// 获得该表中的主键字段名
		String pkName = BillTempletUtil.getPkName(tableName);

		buffer.append(" select " + pkName + ",ts from " + tableName + " where 1=1");
		
		

		/*
		// 获得默认查询条件
		String defConditionName = map.get("defconditionname");
		// 获得默认查询条件值
		String defConditionValue = map.get("defconditionvalue");
		*/
		
		
		/*
		// 根据模板编码查询该模板中定义的所有查询字段
		List<String> list = null;
		if (modelCode != null)
			list = getQueryTemplets(modelCode);
	    */

		
		
		/*
		// 拼接sql条件
		if (list != null && list.size() != 0 && defConditionValue != null
				&& list.contains(defConditionName)) {
			buffer.append(" and " + defConditionName + "='" + defConditionValue
					+ "'");
			Iterator<String> it = list.iterator();
			while (it.hasNext()) {
				if (it.next().equals(defConditionName))
					it.remove();
			}
		}
		
		// 拼接高级搜索内容
		if (list != null && list.size() != 0 && searchContext != null) {
			for (int i = 0; i < list.size(); i++) {
				if (list.size() == 1) {
					buffer.append(" and " + list.get(i) + " like '%"
							+ searchContext + "%'");
				} else {
					if (i == 0)
						buffer.append(" and (" + list.get(i) + " like '%"
								+ searchContext + "%'");
					else if (i == list.size() - 1)
						buffer.append(" or " + list.get(i) + " like '%"
								+ searchContext + "%')");
					else
						buffer.append(" or " + list.get(i) + " like '%"
								+ searchContext + "%'");
				}
			}
		}
		*/
		// 搜索
        String searchContext = map.get("searchcontext");
        //进行搜索sql拼接	--根据查询模板进行搜索
        if(StringUtils.isNotEmpty(searchContext)){
			
		}
        
		// 筛选
		String filter = map.get("condition");
		//进行筛选sql拼接
		if(StringUtils.isNotEmpty(filter)){
			@SuppressWarnings("unchecked")
			Map<String,String> conditonMap  = (Map<String,String>)JSON.parse(filter);
			for(String key : conditonMap.keySet()){
				
				if(conditonMap.get(key).contains("#")){
					String[] values = conditonMap.get(key).split("#");
					buffer.append(" and "+key+" in(");
					for(String value:values){
						buffer.append("'"+value+"',");
					}
					buffer.deleteCharAt(buffer.length()-1);
					buffer.append(")");
				}else if(key.equals("start_time")) {
					buffer.append(" and dbilldate >= '"+conditonMap.get(key)+"'");
				}else if(key.equals("end_time")){
					buffer.append(" and dbilldate <= '"+conditonMap.get(key)+"'");
				}else{
					buffer.append(" and "+key+" = '"+conditonMap.get(key)+"'");
				}
			}
		}
		//buffer.append(" and billmaker = '"+userId+"'");
		buffer.append(" and dr=0 order by ts desc");

		LimitSQLBuilder builder = SQLBuilderFactory.getInstance()
				.createLimitSQLBuilder(new BaseDAO().getDBType());
		// 获得分页sql
		String sql = builder.build(buffer.toString(),
				Integer.parseInt(pageIndex), Integer.parseInt(pageSize));

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> dataList = (List<Map<String, Object>>) new BaseDAO()
				.executeQuery(sql, new MapListProcessor());
		String[] strs = new String[dataList.size()];
		for (int i = 0; i < dataList.size(); i++) {
			strs[i] = (String) dataList.get(i).get(pkName);
		}
		// 将数据中的pk值都转化为相对应的中文名
		IMobileBillDetailQuery service = NCLocator.getInstance().lookup(
				IMobileBillDetailQuery.class);
		List<MobileBillData> billDataList = service.getMobileBillDetail(false,
				"", userId, strs, billType);
		return billDataList;
	}

	/**
	 * 根据模板编码查询该模板中定义的所有查询字段
	 * 
	 * @param modelName
	 * @return
	 */
	private List<String> getQueryTemplets(String modelCode)
			throws BusinessException {
		Logger.info("根据模板编码查询该模板中定义的所有查询字段 --start--");

		StringBuffer conBuffer = new StringBuffer();
		conBuffer
				.append(" select field_code from pub_query_condition where pk_templet in ");
		conBuffer.append("(select id from pub_query_templet where model_code='"
				+ modelCode + "')");

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> conList = (List<Map<String, Object>>) new BaseDAO()
				.executeQuery(conBuffer.toString(), new MapListProcessor());

		if (conList == null || conList.size() == 0)
			throw new BusinessException("根据模板编码查询不到模板编码为【" + modelCode
					+ "】的模板或模板未定义查询字段！");

		List<String> resultList = new ArrayList<String>();
		for (Map<String, Object> map : conList) {
			resultList.add((String) map.get("field_code"));
		}
		return resultList;
	}
}
