package nc.jzmobile.bill.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.mobile.app.IMobileBillDetailQuery;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.jzmobile.bill.util.OrderQueryUtil;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.vo.am.common.util.StringUtils;
import nc.vo.jzmobile.app.MobileBillData;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;

import com.alibaba.fastjson.JSON;

/**
 * 查询默认列表数据
 * 
 * @author wangruin on 2017/8/7
 * 
 */
public class MRGetDefaultListDataHandler implements INCMobileServletHandler {

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
			
			String pk_group = JZMobileAppUtils.getPkGroupByUserId(userId);

			InvocationInfoProxy.getInstance().setGroupId(pk_group);
			
			if("21".equals(billType)){
				/*result.success().setData(
						getPaginationDataListBy21_lb(result, map, userId,pk_group));*/
				result.success().setData(
						getPaginationDataListBy21(result, map, userId,pk_group));
				
			}
			
			if("H5C2".equals(billType)){
				result.success().setData(
						getPaginationDataListByH5C2(result, map, userId,pk_group));
			}
		} catch (Exception e) {
			Logger.error(e);
			e.printStackTrace();
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private List<MobileBillData> getPaginationDataListBy21(Result result,
			Map<String, String> map, String userId,String pk_group) throws Exception {

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

		// 筛选内容
		String condition = map.get("condition");

		Map<String, String> conditonMap = new HashMap<String, String>();
		if (!StringUtils.isEmpty(condition)) {
			conditonMap = (Map<String, String>) JSON.parse(condition);

		}

		String pk_org = conditonMap.get("pk_org");

		String startTime = conditonMap.get("start_time");

		String endTime = conditonMap.get("end_time");

		String pk_project = conditonMap.get("pk_project");

		String vbillcode = conditonMap.get("vbillcode");
		String pk_supplier = conditonMap.get("pk_supplier");
		String pk_material = conditonMap.get("pk_material");
		String cemployeeid = conditonMap.get("cemployeeid");

		StringBuffer buffer = new StringBuffer();

		buffer.append("SELECT po_order.pk_order FROM po_order po_order ");
		//buffer.append(" INNER JOIN po_order_b T1 ON T1.pk_order = po_order.pk_order ");
		//buffer.append(" LEFT OUTER JOIN po_order_bb po_order_bb ON po_order_bb.pk_order_b = T1.pk_order_b");
		//buffer.append(" LEFT OUTER JOIN bd_material bd_material ON bd_material.pk_material = T1.pk_material");
		//buffer.append(" INNER JOIN po_potrantype po_potrantype ON po_order.ctrantypeid = po_potrantype.ctrantypeid");
		//buffer.append(" LEFT OUTER JOIN po_order_bb1 po_order_bb1 ON T1.pk_order_b = po_order_bb1.pk_order_b");
		//buffer.append(" WHERE po_order.dbilldate >= '"
		//		+ startTime + "' AND po_order.dbilldate <= '" + endTime + "'");
		// buffer.append(" AND po_order.pk_busitype IN ('0001C4100000000014AF','1001C410000000005WCP','1001C410000000005WDJ','1001P91000000007FHIW')");
		buffer.append(" where po_order.pk_busitype IN (" + getPkbusinesstype(pk_group)
				+ ")");
		buffer.append(" AND po_order.dr = 0");
		//buffer.append(" and po_order.pk_org = '"+pk_org+"'");
		/*buffer.append(" AND (T1.dbilldate >= '" + startTime
				+ "' AND T1.dbilldate <= '" + endTime + "')");*/
		/*buffer.append(" AND T1.dr = 0 AND po_order.dr = 0 AND T1.dr = 0 AND po_order_bb.dr = 0 AND po_order.bislatest = 'Y' AND po_order.bfrozen = 'N' AND po_order.forderstatus = 3 AND bd_material.fee = 'N' AND bd_material.discountflag = 'N' AND po_order_bb.nonwaynum > 0 AND T1.bstockclose = 'N' AND po_order_bb.fonwaystatus = 8 AND T1.nnum > 0");
		buffer.append(" AND ((po_potrantype.breceiveplan = 'N' AND T1.breceiveplan = 'N' AND T1.nnum > NVL (T1.naccumstorenum, 0) AND T1.pk_arrvstoorg = '"
				+ pk_org + "')");
		buffer.append(" OR (po_potrantype.breceiveplan = 'Y' AND T1.breceiveplan = 'Y' AND po_order_bb1.nnum > NVL (po_order_bb1.naccumstorenum,0) AND po_order_bb1.pk_arrvstoorg = '"
				+ pk_org + "'))");*/
		if (StringUtils.isNotEmpty(pk_project)) {
			buffer.append(" AND po_order. pk_project = '" + pk_project + "'");
		}
		if (StringUtils.isNotEmpty(vbillcode)) {
			buffer.append(" and po_order.vbillcode like '%" + vbillcode + "%'");
		}
		if (StringUtils.isNotEmpty(pk_supplier)) {
			buffer.append(" AND po_order.pk_supplier = '" + pk_supplier + "'");
		}
		/*if (StringUtils.isNotEmpty(pk_material)) {
			buffer.append(" AND bd_material.pk_material = '" + pk_material
					+ "'");
		}*/
		if (StringUtils.isNotEmpty(cemployeeid)) {
			buffer.append(" and po_order.cemployeeid = '" + cemployeeid + "'");
		}
		buffer.append(" order by po_order.ts desc");
		
		List<String> pks = new ArrayList<String>();
		try {
			BaseDAO dao = new BaseDAO();
			pks = (List<String>) dao.executeQuery(buffer.toString(), new ResultSetProcessor() {
						public Object handleResultSet(ResultSet rs) throws SQLException {
							List<String> pks_temp = new ArrayList<String>();
							while (rs.next()) {
								pks_temp.add( rs.getString("pk_order"));
							}
							return pks_temp;
						}
					});
		} catch (DAOException e) {
			Logger.error(e);
			throw new BusinessException(e);
		}
		String[] strs = new String[pks.size()];
		for(int i = 0;i<pks.size();i++){
			strs[i] = pks.get(i);
		}
		List<MobileBillData> billDataList = new ArrayList<MobileBillData>();
		if (strs == null || strs.length == 0) {
			return billDataList;
		}

		/** 对未入库订单进行入库 */
		int start = (Integer.parseInt(pageIndex) - 1)
				* Integer.parseInt(pageSize);
		int end = (Integer.parseInt(pageIndex) - 1)
				* Integer.parseInt(pageSize) + Integer.parseInt(pageSize);
		int length = strs.length - start;
		if (length > Integer.parseInt(pageSize)) {
			length = Integer.parseInt(pageSize);
		}
		String[] strsByPage = new String[length];
		;
		for (int i = start; i < end && i < strs.length; i++) {
			strsByPage[i - start] = strs[i];
		}
		/** 将数据中的pk值都转化为相对应的中文名 */
		IMobileBillDetailQuery service = NCLocator.getInstance().lookup(
				IMobileBillDetailQuery.class);
		billDataList = service.getMobileBillDetail(true, "", userId,
				strsByPage, billType);
		return billDataList;
	}
	
	/**
	 * 
	 * 采购订单特殊处理
	 */
	@SuppressWarnings("unchecked")
	private List<MobileBillData> getPaginationDataListByH5C2(Result result,
			Map<String, String> map, String userId,String pk_group) throws Exception {

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

		// 筛选内容
		String condition = map.get("condition");

		Map<String, String> conditonMap = new HashMap<String, String>();
		if (!StringUtils.isEmpty(condition)) {
			conditonMap = (Map<String, String>) JSON.parse(condition);

		}

		String pk_org = conditonMap.get("pk_org");

		String pk_project = conditonMap.get("pk_project");

		String vbillcode = conditonMap.get("vbillcode");

		String startTime = conditonMap.get("start_time");

		String endTime = conditonMap.get("end_time");
		
		String pk_psndoc = conditonMap.get("pk_psndoc");
		
		String pk_dept = conditonMap.get("pk_dept");

		StringBuffer buffer = new StringBuffer();

		buffer.append(" SELECT pk_cmplan FROM jzmt_cmplan");
		buffer.append(" WHERE pk_purchaseorg IS NOT NULL");
		buffer.append(" AND iplantype = '0'");
		buffer.append(" AND fstatusflag = 1");
		buffer.append(" AND cbilltypecode = 'H5C2'");
		buffer.append(" AND NVL (jzmt_cmplan.dr, 0) = 0");
		buffer.append(" AND pk_purchaseorg = '"+pk_org+"'");
		buffer.append(" AND dbilldate >= '"
				+ startTime + "' AND dbilldate <= '" + endTime + "'");
		if (StringUtils.isNotEmpty(pk_project)) {
			buffer.append(" AND  pk_project = '" + pk_project + "'");
		}
		if (StringUtils.isNotEmpty(vbillcode)) {
			buffer.append(" and vbillcode like '%" + vbillcode + "%'");
		}
		if (StringUtils.isNotEmpty(pk_psndoc)) {
			buffer.append(" AND pk_psndoc = '" + pk_psndoc + "'");
		}
		if (StringUtils.isNotEmpty(pk_dept)) {
			buffer.append(" AND pk_dept = '" + pk_dept
					+ "'");
		}
		
		List<String> pks = new ArrayList<String>();
		try {
			BaseDAO dao = new BaseDAO();
			pks = (List<String>) dao.executeQuery(buffer.toString(), new ResultSetProcessor() {
						public Object handleResultSet(ResultSet rs) throws SQLException {
							List<String> pks_temp = new ArrayList<String>();
							while (rs.next()) {
								pks_temp.add( rs.getString("pk_cmplan"));
							}
							return pks_temp;
						}
					});
		} catch (DAOException e) {
			Logger.error(e);
			throw new BusinessException(e);
		}
		String[] strs = new String[pks.size()];
		for(int i = 0;i<pks.size();i++){
			strs[i] = pks.get(i);
		}
		List<MobileBillData> billDataList = new ArrayList<MobileBillData>();
		if (strs == null || strs.length == 0) {
			return billDataList;
		}

		/** 对未入库订单进行入库 */
		int start = (Integer.parseInt(pageIndex) - 1)
				* Integer.parseInt(pageSize);
		int end = (Integer.parseInt(pageIndex) - 1)
				* Integer.parseInt(pageSize) + Integer.parseInt(pageSize);
		int length = strs.length - start;
		if (length > Integer.parseInt(pageSize)) {
			length = Integer.parseInt(pageSize);
		}
		String[] strsByPage = new String[length];
		;
		for (int i = start; i < end && i < strs.length; i++) {
			strsByPage[i - start] = strs[i];
		}
		/** 将数据中的pk值都转化为相对应的中文名 */
		IMobileBillDetailQuery service = NCLocator.getInstance().lookup(
				IMobileBillDetailQuery.class);
		billDataList = service.getMobileBillDetail(true, "", userId,
				strsByPage, billType);
		return billDataList;
	}

	/**
	 * 
	 * 采购入库特殊处理
	 */
	@SuppressWarnings("unchecked")
	private List<MobileBillData> getPaginationDataListBy21_lb(Result result,
			Map<String, String> map, String userId,String pk_group) throws Exception {

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

		// 筛选内容
		String condition = map.get("condition");

		Map<String, String> conditonMap = new HashMap<String, String>();
		if (!StringUtils.isEmpty(condition)) {
			conditonMap = (Map<String, String>) JSON.parse(condition);

		}

		String pk_org = conditonMap.get("pk_org");

		String startTime = conditonMap.get("start_time");

		String endTime = conditonMap.get("end_time");

		String pk_project = conditonMap.get("pk_project");

		String vbillcode = conditonMap.get("vbillcode");
		String pk_supplier = conditonMap.get("pk_supplier");
		String pk_material = conditonMap.get("pk_material");
		String cemployeeid = conditonMap.get("cemployeeid");

		StringBuffer buffer = new StringBuffer();

		buffer.append("SELECT po_order.pk_order,T1.pk_order_b,po_order_bb1.pk_order_bb1,po_order_bb.pk_order_bb FROM po_order po_order ");
		buffer.append(" INNER JOIN po_order_b T1 ON T1.pk_order = po_order.pk_order ");
		buffer.append(" LEFT OUTER JOIN po_order_bb po_order_bb ON po_order_bb.pk_order_b = T1.pk_order_b");
		buffer.append(" LEFT OUTER JOIN bd_material bd_material ON bd_material.pk_material = T1.pk_material");
		buffer.append(" INNER JOIN po_potrantype po_potrantype ON po_order.ctrantypeid = po_potrantype.ctrantypeid");
		buffer.append(" LEFT OUTER JOIN po_order_bb1 po_order_bb1 ON T1.pk_order_b = po_order_bb1.pk_order_b");
		buffer.append(" WHERE(T1.dr = 0 AND (po_order.dbilldate >= '"
				+ startTime + "' AND po_order.dbilldate <= '" + endTime + "'))");
		// buffer.append(" AND po_order.pk_busitype IN ('0001C4100000000014AF','1001C410000000005WCP','1001C410000000005WDJ','1001P91000000007FHIW')");
		buffer.append(" AND po_order.pk_busitype IN (" + getPkbusinesstype(pk_group)
				+ ")");
		buffer.append(" AND po_order.dr = 0");
		buffer.append(" AND (T1.dbilldate >= '" + startTime
				+ "' AND T1.dbilldate <= '" + endTime + "')");
		buffer.append(" AND T1.dr = 0 AND po_order.dr = 0 AND T1.dr = 0 AND po_order_bb.dr = 0 AND po_order.bislatest = 'Y' AND po_order.bfrozen = 'N' AND po_order.forderstatus = 3 AND bd_material.fee = 'N' AND bd_material.discountflag = 'N' AND po_order_bb.nonwaynum > 0 AND T1.bstockclose = 'N' AND po_order_bb.fonwaystatus = 8 AND T1.nnum > 0");
		buffer.append(" AND ((po_potrantype.breceiveplan = 'N' AND T1.breceiveplan = 'N' AND T1.nnum > NVL (T1.naccumstorenum, 0) AND T1.pk_arrvstoorg = '"
				+ pk_org + "')");
		buffer.append(" OR (po_potrantype.breceiveplan = 'Y' AND T1.breceiveplan = 'Y' AND po_order_bb1.nnum > NVL (po_order_bb1.naccumstorenum,0) AND po_order_bb1.pk_arrvstoorg = '"
				+ pk_org + "'))");
		if (StringUtils.isNotEmpty(pk_project)) {
			buffer.append(" AND po_order. pk_project = '" + pk_project + "'");
		}
		if (StringUtils.isNotEmpty(vbillcode)) {
			buffer.append(" and po_order.vbillcode like '%" + vbillcode + "%'");
		}
		if (StringUtils.isNotEmpty(pk_supplier)) {
			buffer.append(" AND po_order.pk_supplier = '" + pk_supplier + "'");
		}
		if (StringUtils.isNotEmpty(pk_material)) {
			buffer.append(" AND bd_material.pk_material = '" + pk_material
					+ "'");
		}
		if (StringUtils.isNotEmpty(cemployeeid)) {
			buffer.append(" and po_order.cemployeeid = '" + cemployeeid + "'");
		}

		/** 获取所有未入库订单 */
		UFBoolean isLazy = new UFBoolean('0');
		String[] strs = OrderQueryUtil.queryFor45_23(buffer.toString(), isLazy);
		List<MobileBillData> billDataList = new ArrayList<MobileBillData>();
		if (strs == null || strs.length == 0) {
			return billDataList;
		}

		/** 对未入库订单进行入库 */
		int start = (Integer.parseInt(pageIndex) - 1)
				* Integer.parseInt(pageSize);
		int end = (Integer.parseInt(pageIndex) - 1)
				* Integer.parseInt(pageSize) + Integer.parseInt(pageSize);
		int length = strs.length - start;
		if (length > Integer.parseInt(pageSize)) {
			length = Integer.parseInt(pageSize);
		}
		String[] strsByPage = new String[length];
		;
		for (int i = start; i < end && i < strs.length; i++) {
			strsByPage[i - start] = strs[i];
		}
		/** 将数据中的pk值都转化为相对应的中文名 */
		IMobileBillDetailQuery service = NCLocator.getInstance().lookup(
				IMobileBillDetailQuery.class);
		billDataList = service.getMobileBillDetail(true, "", userId,
				strsByPage, billType);
		return billDataList;
	}

	@SuppressWarnings("unchecked")
	private String getPkbusinesstype(String pk_group) throws DAOException {
		
		StringBuffer buffer = new StringBuffer(
				"select pk_businesstype from pub_billbusiness a inner join bd_busitype b on a.pk_businesstype = b.pk_busitype where b.validity != 2 and b.pk_group = '"+pk_group+"' and a.pk_billtype = '45'");
		List<Map<String, String>> dataList = (List<Map<String, String>>) new BaseDAO()
				.executeQuery(buffer.toString(), new MapListProcessor());
		StringBuffer sqlText = new StringBuffer();
		if (dataList != null) {
			for (int i = 0; i < dataList.size(); i++) {
				sqlText.append("'" + dataList.get(i).get("pk_businesstype")
						+ "',");
			}
			sqlText.deleteCharAt(sqlText.length() - 1);
			return sqlText.toString();
		}
		return "";

	}
}
