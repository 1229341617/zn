package nc.jzmobile.bill.util;

/***
 * 移动收料参照
 * @author mxx
 *
 */
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.jdbc.framework.page.LimitSQLBuilder;
import nc.jdbc.framework.page.SQLBuilderFactory;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.jzmobile.refmodel.BaseTreeGrid;
import nc.jzmobile.refmodel.TreeUtils;
import nc.ui.bd.ref.AbstractRefGridTreeModel;
import nc.ui.bd.ref.AbstractRefModel;
import nc.ui.bd.ref.AbstractRefTreeModel;
import nc.ui.bd.ref.RefPubUtil;
import nc.vo.pub.BusinessException;

/**
 * 参照工具类 NC中参照有三种类型：①表结构：参照继承于AbstractRefModel ②树结构：参照继承于AbstractRefTreeModel
 * ③树表结构（即左边是树结构右边是表结构通过一个joinField进行关联）：参照继承于AbstractRefGridTreeModel
 * 
 * @author wangruin on 2017/8/24
 * 
 */
public class MRRefModelUtil {
	/**
	 * 根据参照主键值获得参照实体
	 * 
	 * @return
	 */
	public static AbstractRefModel getRefModel(String pk_refinfo)
			throws BusinessException {
		StringBuffer buffer = new StringBuffer();

		buffer.append(" select name,refclass from bd_refinfo where ");
		buffer.append(" pk_refinfo = '" + pk_refinfo + "'");

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) new BaseDAO()
				.executeQuery(buffer.toString(), new MapListProcessor());
		if (list == null || list.size() == 0)
			throw new BusinessException("根据参照主键值在参照数据库不能查询到数据");

		AbstractRefModel model = RefPubUtil.getRefModel((String) list.get(0)
				.get("name"));

		if (model == null)
			throw new BusinessException("ClassNotFound:引用不到全类名为："
					+ list.get(0).get("refclass") + "的class文件");
		return model;
	}

	/**
	 * 根据得到的参照示例获得返回的Json数据
	 */
	@SuppressWarnings("rawtypes")
	public static List<BaseTreeGrid> getJsonData(AbstractRefModel refModel,
			String pageIndex, String pageSize, Map<String, Object> filterMap)
			throws BusinessException {
		AbstractRefTreeModel refTreeModel = null;

		String refName = refModel.getRefNodeName();
		if (refName.equals("采购组织") || refName.equals("物料")
				|| refName.equals("nc.ui.bd.ref.model.SupplierDefaultRefModel")
				|| refName.equals("项目") || refName.equals("人员")
				|| refName.equals("nc.ui.bd.ref.model.StorDocDefaulteRefModel")
				||refName.equals("部门")
				||refName.equals("nc.ui.pf.pub.TranstypeRefModel")
				||refName.equals("nc.jzmobile.refmodel.JZAddTaxrateRefmodel")
				||refName.equals("库存组织")
				||refName.equals("nc.ui.bd.ref.model.StorDocDefaulteRefModel")
				||refName.equals("财务组织")) {
			return getTable(refModel, pageIndex, pageSize, filterMap);
		}

		List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
		// 如果参照是树结构
		if (refModel instanceof AbstractRefTreeModel) {
			refTreeModel = (AbstractRefTreeModel) refModel;

			// 如果参照是树表结构
			if (refTreeModel instanceof AbstractRefGridTreeModel) {
				AbstractRefGridTreeModel refGridTreeModel = (AbstractRefGridTreeModel) refTreeModel;
				return getRefGridTreeModelData(refGridTreeModel);
			}
			refTreeModel.getRefSql();
			Vector datas = refTreeModel.getData();
			Vector cloumns = refTreeModel.getAllColumnNames();
			String fatherField = refTreeModel.getFatherField();
			String pkFieldCode = refTreeModel.getPkFieldCode();
			refName = refTreeModel.getRefNameField();// 节点名
			String refCode = refTreeModel.getRefCodeField();// 编码
			String refNodeName = refTreeModel.getRefNodeName();// 参照名称
			String rootName = refTreeModel.getRootName();// 树根节点名
			if (datas == null)
				datas = new Vector();
			// 将查询出来的参照字段名和字段值一一对应并装入list中
			for (int i = 0; i < datas.size(); i++) {
				Map<Object, Object> map = new HashMap<Object, Object>();
				for (int j = 0; j < cloumns.size(); j++) {
					map.put(cloumns.elementAt(j),
							((Vector) datas.elementAt(i)).elementAt(j));
				}
				list.add(map);
			}

			List<BaseTreeGrid> resultList = transform(list, fatherField,
					pkFieldCode, refName, refCode, refTreeModel);
			// 设置参照名称和树根节点名
			for (BaseTreeGrid grid : resultList) {
				grid.setRootName(rootName);
				grid.setRefNodeName(refNodeName);
			}
			return resultList;
		}
		// 如果参照是表结构
		else {
			Vector datas = refModel.getData();
			Vector cloumns = refModel.getAllColumnNames();
			String pkFieldCode = refModel.getPkFieldCode();
			refName = refModel.getRefNameField();
			String refCode = refModel.getRefCodeField();// 编码
			String refNodeName = refModel.getRefNodeName();// 参照名称
			if (datas == null)
				datas = new Vector();
			// 将查询出来的参照字段名和字段值一一对应并装入list中
			for (int i = 0; i < datas.size(); i++) {
				Map<Object, Object> map = new HashMap<Object, Object>();
				for (int j = 0; j < cloumns.size(); j++) {
					// 把主键名改为pk
					if (pkFieldCode.equals(cloumns.elementAt(j)))
						map.put("pk",
								((Vector) datas.elementAt(i)).elementAt(j));
					else
						map.put(cloumns.elementAt(j),
								((Vector) datas.elementAt(i)).elementAt(j));
				}
				list.add(map);
			}
			if (list.size() == 0) {
				return null;
			}
			List<BaseTreeGrid> resultList = transform(list, null, null,
					refName, refCode, refModel);
			// 设置参照名称
			for (BaseTreeGrid grid : resultList) {
				grid.setRefNodeName(refNodeName);
			}
			return resultList;
		}
	}

	/**
	 * 将数据转化为表结构
	 * 
	 * @throws BusinessException
	 */
	@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
	public static List<BaseTreeGrid> getTable(AbstractRefModel refModel,
			String pageIndex, String pageSize, Map<String, Object> filterMap)
			throws BusinessException {

		String sql = refModel.getRefSql();
		LimitSQLBuilder builder = SQLBuilderFactory.getInstance()
				.createLimitSQLBuilder(new BaseDAO().getDBType());
		Vector datas = new Vector();
		if(refModel.getRefNodeName().equals("财务组织")){
			String limitSql = builder.build(
					"select * from ("+refModel.getRefSql()+") where name like'%" + filterMap.get("name")
							+ "%' or code like '%" + filterMap.get("code")
							+ "%'", Integer.parseInt(pageIndex),
					Integer.parseInt(pageSize));
			try {
				datas = (Vector) new BaseDAO().executeQuery(limitSql,
						new ResultSetProcessor() {
							@Override
							public Object handleResultSet(ResultSet rs)
									throws SQLException {
								Vector datas = new Vector();
								while (rs.next()) {
									Vector data = new Vector();
									data.add(rs.getString("code"));
									data.add(rs.getString("name"));
									data.add(rs.getString("pk_financeorg"));
									data.add(rs.getString("pk_fatherorg"));
									data.add(rs.getString("pk_corp"));
									data.add(rs.getString("pk_group"));
									data.add(rs.getString("enablestate"));
									datas.add(data);
								}
								return datas;
							}

						});
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(refModel.getRefNodeName().equals("nc.ui.bd.ref.model.StorDocDefaulteRefModel")){
			String limitSql = builder.build(
					"select * from ("+refModel.getRefSql()+") where name like'%" + filterMap.get("name")
							+ "%' or code like '%" + filterMap.get("code")
							+ "%'", Integer.parseInt(pageIndex),
					Integer.parseInt(pageSize));
			try {
				datas = (Vector) new BaseDAO().executeQuery(limitSql,
						new ResultSetProcessor() {
							@Override
							public Object handleResultSet(ResultSet rs)
									throws SQLException {
								Vector datas = new Vector();
								while (rs.next()) {
									Vector data = new Vector();
									data.add(rs.getString("code"));
									data.add(rs.getString("name"));
									data.add(rs.getString("storaddr"));
									data.add(rs.getString("pk_stordoc"));
									datas.add(data);
								}
								return datas;
							}

						});
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(refModel.getRefNodeName().equals("库存组织")){
			String limitSql = builder.build(
					"select * from ("+refModel.getRefSql()+") where name like'%" + filterMap.get("name")
							+ "%' or code like '%" + filterMap.get("code")
							+ "%'", Integer.parseInt(pageIndex),
					Integer.parseInt(pageSize));
			try {
				datas = (Vector) new BaseDAO().executeQuery(limitSql,
						new ResultSetProcessor() {
							@Override
							public Object handleResultSet(ResultSet rs)
									throws SQLException {
								Vector datas = new Vector();
								while (rs.next()) {
									Vector data = new Vector();
									data.add(rs.getString("code"));
									data.add(rs.getString("name"));
									data.add(rs.getString("pk_stockorg"));
									datas.add(data);
								}
								return datas;
							}

						});
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(refModel.getRefNodeName().equals("nc.jzmobile.refmodel.JZAddTaxrateRefmodel")){
			String limitSql = builder.build(
					"select * from ( select vcode code,vname name,pk_taxrate from jzbd_taxrate ORDER BY code ) where name like'%" + filterMap.get("name")
							+ "%' or code like '%" + filterMap.get("code")
							+ "%'", Integer.parseInt(pageIndex),
					Integer.parseInt(pageSize));
			try {
				datas = (Vector) new BaseDAO().executeQuery(limitSql,
						new ResultSetProcessor() {
							@Override
							public Object handleResultSet(ResultSet rs)
									throws SQLException {
								Vector datas = new Vector();
								while (rs.next()) {
									Vector data = new Vector();
									data.add(rs.getString("code"));
									data.add(rs.getString("name"));
									data.add(rs.getString("pk_taxrate"));
									datas.add(data);
								}
								return datas;
							}

						});
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Vector cloumns = new Vector();
			cloumns.add("编码");
			cloumns.add("名称");
			cloumns.add("pk_taxrate");
			String pkFieldCode = "pk_taxrate";
			String refName = "编码";
			String refCode = "code";// 编码
			String refNodeName = refModel.getRefNodeName();// 参照名称
			List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
			for (int i = 0; i < datas.size(); i++) {
				Map<Object, Object> map = new HashMap<Object, Object>();
				for (int j = 0; j < cloumns.size(); j++) {
					// 把主键名改为pk
					if (pkFieldCode.equals(cloumns.elementAt(j)))
						map.put("pk",
								((Vector) datas.elementAt(i)).elementAt(j));
					else
						map.put(cloumns.elementAt(j),
								((Vector) datas.elementAt(i)).elementAt(j));
				}
				list.add(map);
			}
			if (list.size() == 0) {
				return null;
			}
			List<BaseTreeGrid> resultList = transform(list, null, null,
					refName, refCode, refModel);
			// 设置参照名称
			for (BaseTreeGrid grid : resultList) {
				grid.setRefNodeName(refNodeName);
			}
			return resultList;
		}
		
		if(refModel.getRefNodeName().equals("nc.ui.pf.pub.TranstypeRefModel")){
			String limitSql = builder.build(
					"select * from ( select pk_billtypecode code , billtypename name , pk_billtypeid from bd_billtype where ( istransaction = 'Y' and pk_group = '0001A110000000000CFI' and nvl ( islock, 'N' ) = 'N' and ( ( parentbilltype = '21' and 1 = 1 and pk_group = '0001A110000000000CFI' ) ) ) and pk_billtypecode in ( select vtrantypecode from po_potrantype where pk_group = '0001A110000000000CFI' and bdirect = 'N' and dr = 0 ) order by pk_billtypecode ) where name like'%" + filterMap.get("name")
							+ "%' or code like '%" + filterMap.get("code")
							+ "%'", Integer.parseInt(pageIndex),
					Integer.parseInt(pageSize));
			try {
				datas = (Vector) new BaseDAO().executeQuery(limitSql,
						new ResultSetProcessor() {
							@Override
							public Object handleResultSet(ResultSet rs)
									throws SQLException {
								Vector datas = new Vector();
								while (rs.next()) {
									Vector data = new Vector();
									data.add(rs.getString("code"));
									data.add(rs.getString("name"));
									data.add(rs.getString("pk_billtypeid"));
									datas.add(data);
								}
								return datas;
							}

						});
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (refModel.getRefNodeName().equals("采购组织")) {
			String limitSql = builder.build(
					"select * from (" + refModel.getRefSql()
							+ ") where name like'%" + filterMap.get("name")
							+ "%' or code like '%" + filterMap.get("code")
							+ "%'", Integer.parseInt(pageIndex),
					Integer.parseInt(pageSize));
			try {
				datas = (Vector) new BaseDAO().executeQuery(limitSql,
						new ResultSetProcessor() {
							@Override
							public Object handleResultSet(ResultSet rs)
									throws SQLException {
								Vector datas = new Vector();
								while (rs.next()) {
									Vector data = new Vector();
									data.add(rs.getString("code"));
									data.add(rs.getString("name"));
									data.add(rs.getString("pk_purchaseorg"));
									datas.add(data);
								}
								return datas;
							}

						});
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (refModel.getRefNodeName().equals(
				"nc.ui.bd.ref.model.SupplierDefaultRefModel")) {
			String limitSql = builder
					.build("select pk_org,code,name,mnecode, null type4,pk_supplier,pk_supplier_main,pk_supplierclass from bd_supplier where name like'%"
							+ filterMap.get("name")
							+ "%' or code like'%"
							+ filterMap.get("code") + "%'",
							Integer.parseInt(pageIndex),
							Integer.parseInt(pageSize));
			try {
				datas = (Vector) new BaseDAO().executeQuery(limitSql,
						new ResultSetProcessor() {
							@Override
							public Object handleResultSet(ResultSet rs)
									throws SQLException {
								Vector datas = new Vector();
								while (rs.next()) {
									Vector data = new Vector();
									data.add(rs.getString("pk_org"));
									data.add(rs.getString("code"));
									data.add(rs.getString("name"));
									data.add(rs.getString("mnecode"));
									data.add(rs.getString("type4"));
									data.add(rs.getString("pk_supplier"));
									data.add(rs.getString("pk_supplier_main"));
									data.add(rs.getString("pk_supplierclass"));
									datas.add(data);
								}
								return datas;
							}

						});
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Vector cloumns = new Vector();
			cloumns.add("组织");
			cloumns.add("编码");
			cloumns.add("名称");
			cloumns.add("mnecode");
			cloumns.add("type4");
			cloumns.add("pk_supplier");
			cloumns.add("pk_supplier_main");
			cloumns.add("pk_supplierclass");
			String pkFieldCode = "pk_supplier";
			String refName = "编码";
			String refCode = "code";// 编码
			String refNodeName = refModel.getRefNodeName();// 参照名称
			List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
			for (int i = 0; i < datas.size(); i++) {
				Map<Object, Object> map = new HashMap<Object, Object>();
				for (int j = 0; j < cloumns.size(); j++) {
					// 把主键名改为pk
					if (pkFieldCode.equals(cloumns.elementAt(j)))
						map.put("pk",
								((Vector) datas.elementAt(i)).elementAt(j));
					else
						map.put(cloumns.elementAt(j),
								((Vector) datas.elementAt(i)).elementAt(j));
				}
				list.add(map);
			}
			if (list.size() == 0) {
				return null;
			}
			List<BaseTreeGrid> resultList = transform(list, null, null,
					refName, refCode, refModel);
			// 设置参照名称
			for (BaseTreeGrid grid : resultList) {
				grid.setRefNodeName(refNodeName);
			}
			return resultList;

		}

		if (refModel.getRefNodeName().equals("项目")) {
			String limitSql = builder.build(
					"select * from (" + refModel.getRefSql()
							+ ") where project_name like'%"
							+ filterMap.get("name")
							+ "%' or project_code like '%"
							+ filterMap.get("code") + "%'",
					Integer.parseInt(pageIndex), Integer.parseInt(pageSize));
			try {
				datas = (Vector) new BaseDAO().executeQuery(limitSql,
						new ResultSetProcessor() {
							@Override
							public Object handleResultSet(ResultSet rs)
									throws SQLException {
								Vector datas = new Vector();
								while (rs.next()) {
									Vector data = new Vector();
									data.add(rs.getString("project_code"));
									data.add(rs.getString("project_name"));
									data.add(rs.getString("project_sh_name"));
									data.add(rs.getString("pk_project"));
									data.add(rs.getString("ordermethod"));
									data.add(rs.getString("planmodel"));
									data.add(rs.getString("planpriority"));
									data.add(rs.getString("pk_workcalendar"));
									data.add(rs.getString("pk_group"));
									data.add(rs.getString("pk_org"));
									datas.add(data);
								}
								return datas;
							}

						});
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/*if (refModel.getRefNodeName().equals(
				"nc.ui.bd.ref.model.StorDocDefaulteRefModel")) {

			StringBuffer buffer = new StringBuffer(
					"SELECT code,NAME,storaddr,pk_stordoc FROM bd_stordoc ");
			buffer.append("WHERE(enablestate = 2 AND gubflag = 'N' AND isdirectstore = 'N' ");
			buffer.append("AND (profitcentre = '" + filterMap.get("pk_org")
					+ "' OR profitcentre = '~')) ");
			buffer.append("AND (enablestate = 2) ");
			buffer.append("AND ((pk_stordoc IN (SELECT pk_stordoc FROM bd_agentstore  ");
			buffer.append("WHERE pk_stockorg = '" + filterMap.get("pk_org")
					+ "') OR pk_org = '" + filterMap.get("pk_org") + "')) ");
			buffer.append("AND (code like '%" + filterMap.get("name")
					+ "%' or name like '%" + filterMap.get("code") + "%') ");
			buffer.append("ORDER BY code ");

			String limitSql = builder.build(buffer.toString(),
					Integer.parseInt(pageIndex), Integer.parseInt(pageSize));
			try {
				datas = (Vector) new BaseDAO().executeQuery(limitSql,
						new ResultSetProcessor() {
							@Override
							public Object handleResultSet(ResultSet rs)
									throws SQLException {
								Vector datas = new Vector();
								while (rs.next()) {
									Vector data = new Vector();
									data.add(rs.getString("code"));
									data.add(rs.getString("name"));
									data.add(rs.getString("storaddr"));
									data.add(rs.getString("pk_stordoc"));
									datas.add(data);
								}
								return datas;
							}
						});
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/

		if (refModel.getRefNodeName().equals("人员")) {
			String limitSql = builder
					.build("SELECT DISTINCT bd_psndoc.code,bd_psndoc. NAME,bd_psnjob.pk_dept,bd_psndoc.pk_psndoc,bd_psnjob.pk_psnjob,bd_psndoc.idtype,org_dept_v.name ID FROM bd_psndoc LEFT JOIN bd_psnjob ON bd_psndoc.pk_psndoc = bd_psnjob.pk_psndoc LEFT JOIN org_dept_v ON bd_psnjob.pk_dept = org_dept_v.pk_dept where bd_psndoc.code like '%"
							+ filterMap.get("code")
							+ "%' or bd_psndoc. NAME like'%"
							+ filterMap.get("name") + "%'",
							Integer.parseInt(pageIndex),
							Integer.parseInt(pageSize));
			try {
				datas = (Vector) new BaseDAO().executeQuery(limitSql,
						new ResultSetProcessor() {
							@Override
							public Object handleResultSet(ResultSet rs)
									throws SQLException {
								Vector datas = new Vector();
								while (rs.next()) {
									Vector data = new Vector();
									data.add(rs.getString("code"));
									data.add(rs.getString("name"));
									data.add(rs.getString("pk_dept"));
									data.add(rs.getString("pk_psndoc"));
									data.add(rs.getString("pk_psnjob"));
									data.add(rs.getString("idtype"));
									data.add(rs.getString("id"));
									datas.add(data);
								}
								return datas;
							}

						});
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (refModel.getRefNodeName().equals("物料")) {
			String limitSql = builder.build(
					"select * from (" + refModel.getRefSql()
							+ ") where name like'%" + filterMap.get("name")
							+ "%' or code like '%" + filterMap.get("code")
							+ "%'", Integer.parseInt(pageIndex),
					Integer.parseInt(pageSize));
			try {
				datas = (Vector) new BaseDAO().executeQuery(limitSql,
						new ResultSetProcessor() {
							@Override
							public Object handleResultSet(ResultSet rs)
									throws SQLException {
								Vector datas = new Vector();
								while (rs.next()) {
									Vector data = new Vector();
									data.add(rs.getString("pk_org"));
									data.add(rs.getString("code"));
									data.add(rs.getString("name"));
									data.add(rs.getString("materialspec"));
									data.add(rs.getString("materialtype"));
									data.add(rs.getString("materialshortname"));
									data.add(rs.getString("materialmnecode"));
									data.add(rs.getString("type7"));
									data.add(rs.getString("type8"));
									data.add(rs.getString("pk_material"));
									data.add(rs.getString("pk_source"));
									data.add(rs.getString("pk_marbasclass"));
									datas.add(data);
								}
								return datas;
							}

						});
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (refModel.getRefNodeName().equals("部门")) {
			String limitSql = builder.build(
					"select * from (SELECT code,NAME,mnecode,pk_dept,pk_fatherorg,displayorder,innercode,pk_org FROM org_dept WHERE enablestate = 2 AND pk_group = '0001A110000000000CFI' ) where name like'%" + filterMap.get("name")
							+ "%' or code like '%" + filterMap.get("code")
							+ "%'", Integer.parseInt(pageIndex),
					Integer.parseInt(pageSize));
			try {
				datas = (Vector) new BaseDAO().executeQuery(limitSql,
						new ResultSetProcessor() {
							@Override
							public Object handleResultSet(ResultSet rs)
									throws SQLException {
								Vector datas = new Vector();
								while (rs.next()) {
									Vector data = new Vector();
									data.add(rs.getString("code"));
									data.add(rs.getString("name"));
									data.add(rs.getString("mnecode"));
									data.add(rs.getString("pk_dept"));
									data.add(rs.getString("pk_fatherorg"));
									data.add(rs.getString("displayorder"));
									data.add(rs.getString("innercode"));
									data.add(rs.getString("pk_org"));
									datas.add(data);
								}
								return datas;
							}

						});
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Vector cloumns = refModel.getAllColumnNames();
		String pkFieldCode = refModel.getPkFieldCode();
		String refName = refModel.getRefNameField();
		String refCode = refModel.getRefCodeField();// 编码
		String refNodeName = refModel.getRefNodeName();// 参照名称
		List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
		for (int i = 0; i < datas.size(); i++) {
			Map<Object, Object> map = new HashMap<Object, Object>();
			for (int j = 0; j < cloumns.size(); j++) {
				// 把主键名改为pk
				if (pkFieldCode.equals(cloumns.elementAt(j)))
					map.put("pk", ((Vector) datas.elementAt(i)).elementAt(j));
				else
					map.put(cloumns.elementAt(j),
							((Vector) datas.elementAt(i)).elementAt(j));
			}
			list.add(map);
		}
		if (list.size() == 0) {
			return null;
		}
		List<BaseTreeGrid> resultList = transform(list, null, null, refName,
				refCode, refModel);
		// 设置参照名称
		for (BaseTreeGrid grid : resultList) {
			grid.setRefNodeName(refNodeName);
		}
		return resultList;
	}

	/**
	 * 获得树形表格的数据
	 * 
	 * @param refGridTreeModel
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("rawtypes")
	private static List<BaseTreeGrid> getRefGridTreeModelData(
			AbstractRefGridTreeModel refGridTreeModel) throws BusinessException {
		List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();

		Vector datas = refGridTreeModel.getClassData();// 左侧所有数据
		refGridTreeModel.getClassRefSql();
		String[] cloumns = refGridTreeModel.getClassFieldCode();// 左侧所有字段名
		String pkFieldCode = refGridTreeModel.getPkFieldCode();// 右侧的参照pk
		String refFieldName = refGridTreeModel.getRefNameField();// 右侧显示的参照名称
		String childField = refGridTreeModel.getChildField();// 左侧子节点
		String fatherField = refGridTreeModel.getFatherField();// 左侧父节点
		String joinField = refGridTreeModel.getClassJoinField();// 左右关联的字段名
		String refNodeName = refGridTreeModel.getRefNodeName();// 参照名称
		String rootName = refGridTreeModel.getRootName();// 左侧根节点名
		if (datas == null)
			datas = new Vector();
		// 将查询出来的参照字段名和字段值一一对应并装入list中
		for (int i = 0; i < datas.size(); i++) {
			Map<Object, Object> map = new HashMap<Object, Object>();
			for (int j = 0; j < cloumns.length; j++) {
				map.put(cloumns[j], ((Vector) datas.elementAt(i)).elementAt(j));
			}
			list.add(map);
		}
		if (list.size() == 0) {
			throw new BusinessException("树形表格左侧查询的参照数据为空！");
		}
		List<BaseTreeGrid> gridList = transformGridTree(list, refGridTreeModel);
		Map<String, Object> gridMap = new HashMap<String, Object>();
		for (Map<Object, Object> map : list) {
			if (map.get(joinField) == null)
				continue;
			// 设置关联主键
			refGridTreeModel.setClassJoinValue((String) map.get(joinField));
			refGridTreeModel.getRefSql();

			Vector gridDatas = refGridTreeModel.getData();
			Vector griCloumns = refGridTreeModel.getAllColumnNames();
			if (gridDatas == null || gridDatas.size() == 0)
				continue;
			// 将查询出来的参照字段名和字段值一一对应并装入list中
			List<Map<Object, Object>> dataList = new ArrayList<Map<Object, Object>>();
			for (int i = 0; i < gridDatas.size(); i++) {
				Map<Object, Object> m = new HashMap<Object, Object>();
				for (int j = 0; j < griCloumns.size(); j++) {
					// 只需要pk和name，其他信息剔除
					if (griCloumns.elementAt(j).equals(
							getFieldName(refGridTreeModel, pkFieldCode)))
						m.put("pk",
								((Vector) gridDatas.elementAt(i)).elementAt(j));
					else if (griCloumns.elementAt(j).equals(
							getFieldName(refGridTreeModel, refFieldName)))
						m.put(getFieldName(refGridTreeModel, refFieldName),
								((Vector) gridDatas.elementAt(i)).elementAt(j));
					else
						continue;
				}
				dataList.add(m);
			}
			gridMap.put((String) map.get(childField), dataList);
			// 移除无效信息
			map.remove(childField);
			map.remove(fatherField);
		}
		for (BaseTreeGrid grid : gridList) {
			// 递归为BaseTreeGrid中的datas赋值
			recursionGridTree(grid, gridMap);
			grid.setRootName(rootName);
			grid.setRefNodeName(refNodeName);
		}
		return gridList;
	}

	/**
	 * 用递归的方式为BaseTreeGrid中的datas赋值
	 * 
	 * @param grid
	 * @param gridMap
	 */
	@SuppressWarnings("unchecked")
	private static void recursionGridTree(BaseTreeGrid grid,
			Map<String, Object> gridMap) {
		Set<String> set = gridMap.keySet();
		if (grid.getId() != null && set.contains(grid.getId())) {
			for (String s : set) {
				if (grid.getId().equals(s))
					grid.setDatas((List<Map<Object, Object>>) gridMap.get(s));
			}
		}
		if (grid.getChildren() != null && grid.getChildren().size() != 0) {
			List<BaseTreeGrid> list = grid.getChildren();
			for (BaseTreeGrid g : list) {
				// 递归处理
				recursionGridTree(g, gridMap);
			}
		} else
			return;

	}

	/**
	 * 由code查询对于的name
	 * 
	 * @param refModel
	 * @param name
	 * @return
	 * @throws BusinessException
	 */
	public static String getFieldName(AbstractRefModel refModel, String name)
			throws BusinessException {

		String[] fieldCode = null, fieldName = null;
		if (refModel.getRefNodeName().equals(
				"nc.ui.bd.ref.model.SupplierDefaultRefModel")) {
			fieldCode = new String[] { "pk_org", "code", "name", "mnecode",
					"type4", "pk_supplier", "pk_supplier_main",
					"pk_supplierclass" };
			fieldName = new String[] { "组织", "编码", "名称", "mnecode", "type4",
					"pk_supplier", "pk_supplier_main", "pk_supplierclass" };
		} else if(refModel.getRefNodeName().equals("nc.jzmobile.refmodel.JZAddTaxrateRefmodel")){
			fieldCode = new String[] { "code", "name", "pk_taxrate" };
			fieldName = new String[] { "组织", "编码", "pk_taxrate"};
		}else {
			fieldCode = refModel.getFieldCode();
			fieldName = refModel.getFieldName();
		}

		if (fieldCode == null || fieldName == null || fieldCode.length == 0
				|| fieldName.length == 0)
			throw new BusinessException("参照【" + refModel.getRefNodeName()
					+ "】参照类中未设置FieldCode和getFieldName");
		if (fieldCode.length != fieldName.length)
			throw new BusinessException("参照【" + refModel.getRefNodeName()
					+ "】参照类中设置的FieldCode和getFieldName的数量不一致");
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < fieldCode.length; i++) {
			map.put(fieldCode[i], fieldName[i]);
		}
		return map.get(name) == null ? name : map.get(name);
	}

	/**
	 * 将查询到的数据进行对象[树结构]封装以便于转化为Josn串
	 * 
	 * @param list
	 * @param fatherField
	 * @param childField
	 * @return
	 * @throws BusinessException
	 */
	public static List<BaseTreeGrid> transform(List<Map<Object, Object>> list,
			String fatherField, String pkFieldCode, String refName,
			String refCode, AbstractRefModel refModel) throws BusinessException {
		List<BaseTreeGrid> rootList = new ArrayList<BaseTreeGrid>();
		for (Map<Object, Object> map : list) {
			BaseTreeGrid root = new BaseTreeGrid();
			root.setId((String) map.get(pkFieldCode));
			root.setParentId((String) map.get(fatherField));
			root.setTitle((String) (map.get(getFieldName(refModel, refName)) == null ? map
					.get(refName) : map.get(getFieldName(refModel, refName))));
			root.setCode((String) (map.get(getFieldName(refModel, refCode)) == null ? map
					.get(refCode) : map.get(getFieldName(refModel, refCode))));
			// 移除无效信息

			map.remove(pkFieldCode);
			map.remove(fatherField);
			// 只有表结构时set map
			if (!(refModel instanceof AbstractRefTreeModel)
					|| refModel.getRefNodeName().equals("物料")
					|| refModel.getRefNodeName().equals("项目")
					|| refModel.getRefNodeName().equals("人员")
					|| refModel.getRefNodeName().equals("项目")
					||refModel.getRefNodeName().equals("部门")
					|| refModel.getRefNodeName().equals(
							"nc.ui.bd.ref.model.SupplierDefaultRefModel")
							||refModel.getRefNodeName().equals(
									"财务组织"))
				root.setMap(map);
			rootList.add(root);
		}
		List<BaseTreeGrid> refList = TreeUtils.formatTree(rootList);

		return refList;
	}

	/**
	 * 将查询到的数据进行对象[树表结构]封装以便于转化为Josn串
	 * 
	 * @param list
	 * @param fatherField
	 * @param childField
	 * @return
	 * @throws BusinessException
	 */
	public static List<BaseTreeGrid> transformGridTree(
			List<Map<Object, Object>> list,
			AbstractRefGridTreeModel refGridTreeModel) throws BusinessException {
		String refName = refGridTreeModel.getClassRefNameField();// 左侧节点显示名
		/*
		 * String GridTreeeName = refGridTreeModel.getClassRefNameField();//
		 * 获得左边树节点名
		 */
		String childField = refGridTreeModel.getChildField();// 左侧子节点
		String fatherField = refGridTreeModel.getFatherField();// 左侧父节点
		String refCode = refGridTreeModel.getRefCodeField();// 编码

		List<BaseTreeGrid> rootList = new ArrayList<BaseTreeGrid>();
		for (Map<Object, Object> map : list) {
			BaseTreeGrid root = new BaseTreeGrid();
			root.setId((String) map.get(childField));
			root.setParentId((String) map.get(fatherField));
			root.setTitle((String) (map.get(getFieldName(refGridTreeModel,
					refName)) == null ? map.get(refName) : map
					.get(getFieldName(refGridTreeModel, refName))));
			root.setCode((String) (map.get(getFieldName(refGridTreeModel,
					refCode)) == null ? map.get(refCode) : map
					.get(getFieldName(refGridTreeModel, refCode))));

			rootList.add(root);
		}
		List<BaseTreeGrid> treeList = TreeUtils.formatTree(rootList);

		return treeList;
	}
}
