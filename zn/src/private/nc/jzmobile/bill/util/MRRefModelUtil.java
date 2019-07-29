package nc.jzmobile.bill.util;

/***
 * �ƶ����ϲ���
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
 * ���չ����� NC�в������������ͣ��ٱ�ṹ�����ռ̳���AbstractRefModel �����ṹ�����ռ̳���AbstractRefTreeModel
 * ������ṹ������������ṹ�ұ��Ǳ�ṹͨ��һ��joinField���й����������ռ̳���AbstractRefGridTreeModel
 * 
 * @author wangruin on 2017/8/24
 * 
 */
public class MRRefModelUtil {
	/**
	 * ���ݲ�������ֵ��ò���ʵ��
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
			throw new BusinessException("���ݲ�������ֵ�ڲ������ݿⲻ�ܲ�ѯ������");

		AbstractRefModel model = RefPubUtil.getRefModel((String) list.get(0)
				.get("name"));

		if (model == null)
			throw new BusinessException("ClassNotFound:���ò���ȫ����Ϊ��"
					+ list.get(0).get("refclass") + "��class�ļ�");
		return model;
	}

	/**
	 * ���ݵõ��Ĳ���ʾ����÷��ص�Json����
	 */
	@SuppressWarnings("rawtypes")
	public static List<BaseTreeGrid> getJsonData(AbstractRefModel refModel,
			String pageIndex, String pageSize, Map<String, Object> filterMap)
			throws BusinessException {
		AbstractRefTreeModel refTreeModel = null;

		String refName = refModel.getRefNodeName();
		if (refName.equals("�ɹ���֯") || refName.equals("����")
				|| refName.equals("nc.ui.bd.ref.model.SupplierDefaultRefModel")
				|| refName.equals("��Ŀ") || refName.equals("��Ա")
				|| refName.equals("nc.ui.bd.ref.model.StorDocDefaulteRefModel")
				||refName.equals("����")
				||refName.equals("nc.ui.pf.pub.TranstypeRefModel")
				||refName.equals("nc.jzmobile.refmodel.JZAddTaxrateRefmodel")
				||refName.equals("�����֯")
				||refName.equals("nc.ui.bd.ref.model.StorDocDefaulteRefModel")
				||refName.equals("������֯")) {
			return getTable(refModel, pageIndex, pageSize, filterMap);
		}

		List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
		// ������������ṹ
		if (refModel instanceof AbstractRefTreeModel) {
			refTreeModel = (AbstractRefTreeModel) refModel;

			// �������������ṹ
			if (refTreeModel instanceof AbstractRefGridTreeModel) {
				AbstractRefGridTreeModel refGridTreeModel = (AbstractRefGridTreeModel) refTreeModel;
				return getRefGridTreeModelData(refGridTreeModel);
			}
			refTreeModel.getRefSql();
			Vector datas = refTreeModel.getData();
			Vector cloumns = refTreeModel.getAllColumnNames();
			String fatherField = refTreeModel.getFatherField();
			String pkFieldCode = refTreeModel.getPkFieldCode();
			refName = refTreeModel.getRefNameField();// �ڵ���
			String refCode = refTreeModel.getRefCodeField();// ����
			String refNodeName = refTreeModel.getRefNodeName();// ��������
			String rootName = refTreeModel.getRootName();// �����ڵ���
			if (datas == null)
				datas = new Vector();
			// ����ѯ�����Ĳ����ֶ������ֶ�ֵһһ��Ӧ��װ��list��
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
			// ���ò������ƺ������ڵ���
			for (BaseTreeGrid grid : resultList) {
				grid.setRootName(rootName);
				grid.setRefNodeName(refNodeName);
			}
			return resultList;
		}
		// ��������Ǳ�ṹ
		else {
			Vector datas = refModel.getData();
			Vector cloumns = refModel.getAllColumnNames();
			String pkFieldCode = refModel.getPkFieldCode();
			refName = refModel.getRefNameField();
			String refCode = refModel.getRefCodeField();// ����
			String refNodeName = refModel.getRefNodeName();// ��������
			if (datas == null)
				datas = new Vector();
			// ����ѯ�����Ĳ����ֶ������ֶ�ֵһһ��Ӧ��װ��list��
			for (int i = 0; i < datas.size(); i++) {
				Map<Object, Object> map = new HashMap<Object, Object>();
				for (int j = 0; j < cloumns.size(); j++) {
					// ����������Ϊpk
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
			// ���ò�������
			for (BaseTreeGrid grid : resultList) {
				grid.setRefNodeName(refNodeName);
			}
			return resultList;
		}
	}

	/**
	 * ������ת��Ϊ��ṹ
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
		if(refModel.getRefNodeName().equals("������֯")){
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
		if(refModel.getRefNodeName().equals("�����֯")){
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
			cloumns.add("����");
			cloumns.add("����");
			cloumns.add("pk_taxrate");
			String pkFieldCode = "pk_taxrate";
			String refName = "����";
			String refCode = "code";// ����
			String refNodeName = refModel.getRefNodeName();// ��������
			List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
			for (int i = 0; i < datas.size(); i++) {
				Map<Object, Object> map = new HashMap<Object, Object>();
				for (int j = 0; j < cloumns.size(); j++) {
					// ����������Ϊpk
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
			// ���ò�������
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
		if (refModel.getRefNodeName().equals("�ɹ���֯")) {
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
			cloumns.add("��֯");
			cloumns.add("����");
			cloumns.add("����");
			cloumns.add("mnecode");
			cloumns.add("type4");
			cloumns.add("pk_supplier");
			cloumns.add("pk_supplier_main");
			cloumns.add("pk_supplierclass");
			String pkFieldCode = "pk_supplier";
			String refName = "����";
			String refCode = "code";// ����
			String refNodeName = refModel.getRefNodeName();// ��������
			List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
			for (int i = 0; i < datas.size(); i++) {
				Map<Object, Object> map = new HashMap<Object, Object>();
				for (int j = 0; j < cloumns.size(); j++) {
					// ����������Ϊpk
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
			// ���ò�������
			for (BaseTreeGrid grid : resultList) {
				grid.setRefNodeName(refNodeName);
			}
			return resultList;

		}

		if (refModel.getRefNodeName().equals("��Ŀ")) {
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

		if (refModel.getRefNodeName().equals("��Ա")) {
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

		if (refModel.getRefNodeName().equals("����")) {
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
		
		if (refModel.getRefNodeName().equals("����")) {
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
		String refCode = refModel.getRefCodeField();// ����
		String refNodeName = refModel.getRefNodeName();// ��������
		List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
		for (int i = 0; i < datas.size(); i++) {
			Map<Object, Object> map = new HashMap<Object, Object>();
			for (int j = 0; j < cloumns.size(); j++) {
				// ����������Ϊpk
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
		// ���ò�������
		for (BaseTreeGrid grid : resultList) {
			grid.setRefNodeName(refNodeName);
		}
		return resultList;
	}

	/**
	 * ������α�������
	 * 
	 * @param refGridTreeModel
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("rawtypes")
	private static List<BaseTreeGrid> getRefGridTreeModelData(
			AbstractRefGridTreeModel refGridTreeModel) throws BusinessException {
		List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();

		Vector datas = refGridTreeModel.getClassData();// �����������
		refGridTreeModel.getClassRefSql();
		String[] cloumns = refGridTreeModel.getClassFieldCode();// ��������ֶ���
		String pkFieldCode = refGridTreeModel.getPkFieldCode();// �Ҳ�Ĳ���pk
		String refFieldName = refGridTreeModel.getRefNameField();// �Ҳ���ʾ�Ĳ�������
		String childField = refGridTreeModel.getChildField();// ����ӽڵ�
		String fatherField = refGridTreeModel.getFatherField();// ��ุ�ڵ�
		String joinField = refGridTreeModel.getClassJoinField();// ���ҹ������ֶ���
		String refNodeName = refGridTreeModel.getRefNodeName();// ��������
		String rootName = refGridTreeModel.getRootName();// �����ڵ���
		if (datas == null)
			datas = new Vector();
		// ����ѯ�����Ĳ����ֶ������ֶ�ֵһһ��Ӧ��װ��list��
		for (int i = 0; i < datas.size(); i++) {
			Map<Object, Object> map = new HashMap<Object, Object>();
			for (int j = 0; j < cloumns.length; j++) {
				map.put(cloumns[j], ((Vector) datas.elementAt(i)).elementAt(j));
			}
			list.add(map);
		}
		if (list.size() == 0) {
			throw new BusinessException("���α������ѯ�Ĳ�������Ϊ�գ�");
		}
		List<BaseTreeGrid> gridList = transformGridTree(list, refGridTreeModel);
		Map<String, Object> gridMap = new HashMap<String, Object>();
		for (Map<Object, Object> map : list) {
			if (map.get(joinField) == null)
				continue;
			// ���ù�������
			refGridTreeModel.setClassJoinValue((String) map.get(joinField));
			refGridTreeModel.getRefSql();

			Vector gridDatas = refGridTreeModel.getData();
			Vector griCloumns = refGridTreeModel.getAllColumnNames();
			if (gridDatas == null || gridDatas.size() == 0)
				continue;
			// ����ѯ�����Ĳ����ֶ������ֶ�ֵһһ��Ӧ��װ��list��
			List<Map<Object, Object>> dataList = new ArrayList<Map<Object, Object>>();
			for (int i = 0; i < gridDatas.size(); i++) {
				Map<Object, Object> m = new HashMap<Object, Object>();
				for (int j = 0; j < griCloumns.size(); j++) {
					// ֻ��Ҫpk��name��������Ϣ�޳�
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
			// �Ƴ���Ч��Ϣ
			map.remove(childField);
			map.remove(fatherField);
		}
		for (BaseTreeGrid grid : gridList) {
			// �ݹ�ΪBaseTreeGrid�е�datas��ֵ
			recursionGridTree(grid, gridMap);
			grid.setRootName(rootName);
			grid.setRefNodeName(refNodeName);
		}
		return gridList;
	}

	/**
	 * �õݹ�ķ�ʽΪBaseTreeGrid�е�datas��ֵ
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
				// �ݹ鴦��
				recursionGridTree(g, gridMap);
			}
		} else
			return;

	}

	/**
	 * ��code��ѯ���ڵ�name
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
			fieldName = new String[] { "��֯", "����", "����", "mnecode", "type4",
					"pk_supplier", "pk_supplier_main", "pk_supplierclass" };
		} else if(refModel.getRefNodeName().equals("nc.jzmobile.refmodel.JZAddTaxrateRefmodel")){
			fieldCode = new String[] { "code", "name", "pk_taxrate" };
			fieldName = new String[] { "��֯", "����", "pk_taxrate"};
		}else {
			fieldCode = refModel.getFieldCode();
			fieldName = refModel.getFieldName();
		}

		if (fieldCode == null || fieldName == null || fieldCode.length == 0
				|| fieldName.length == 0)
			throw new BusinessException("���ա�" + refModel.getRefNodeName()
					+ "����������δ����FieldCode��getFieldName");
		if (fieldCode.length != fieldName.length)
			throw new BusinessException("���ա�" + refModel.getRefNodeName()
					+ "�������������õ�FieldCode��getFieldName��������һ��");
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < fieldCode.length; i++) {
			map.put(fieldCode[i], fieldName[i]);
		}
		return map.get(name) == null ? name : map.get(name);
	}

	/**
	 * ����ѯ�������ݽ��ж���[���ṹ]��װ�Ա���ת��ΪJosn��
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
			// �Ƴ���Ч��Ϣ

			map.remove(pkFieldCode);
			map.remove(fatherField);
			// ֻ�б�ṹʱset map
			if (!(refModel instanceof AbstractRefTreeModel)
					|| refModel.getRefNodeName().equals("����")
					|| refModel.getRefNodeName().equals("��Ŀ")
					|| refModel.getRefNodeName().equals("��Ա")
					|| refModel.getRefNodeName().equals("��Ŀ")
					||refModel.getRefNodeName().equals("����")
					|| refModel.getRefNodeName().equals(
							"nc.ui.bd.ref.model.SupplierDefaultRefModel")
							||refModel.getRefNodeName().equals(
									"������֯"))
				root.setMap(map);
			rootList.add(root);
		}
		List<BaseTreeGrid> refList = TreeUtils.formatTree(rootList);

		return refList;
	}

	/**
	 * ����ѯ�������ݽ��ж���[����ṹ]��װ�Ա���ת��ΪJosn��
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
		String refName = refGridTreeModel.getClassRefNameField();// ���ڵ���ʾ��
		/*
		 * String GridTreeeName = refGridTreeModel.getClassRefNameField();//
		 * ���������ڵ���
		 */
		String childField = refGridTreeModel.getChildField();// ����ӽڵ�
		String fatherField = refGridTreeModel.getFatherField();// ��ุ�ڵ�
		String refCode = refGridTreeModel.getRefCodeField();// ����

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
