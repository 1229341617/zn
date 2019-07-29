package nc.jzmobile.bill.util;

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
public class RefModelUtil {
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
	public static List<BaseTreeGrid> getJsonData(AbstractRefModel refModel)
			throws BusinessException {
		AbstractRefTreeModel refTreeModel = null;
		
		List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
		// ������������ṹ
		if (refModel instanceof AbstractRefTreeModel) {
			refTreeModel = (AbstractRefTreeModel) refModel;
			// �������������ṹ
			if (refTreeModel instanceof AbstractRefGridTreeModel) {
				AbstractRefGridTreeModel refGridTreeModel = (AbstractRefGridTreeModel) refTreeModel;
				return getRefGridTreeModelData(refGridTreeModel);
			}
			Vector datas = refTreeModel.getData();
			Vector cloumns = refTreeModel.getAllColumnNames();
			String fatherField = refTreeModel.getFatherField();
			String pkFieldCode = refTreeModel.getPkFieldCode();
			String refName = refTreeModel.getRefNameField();// �ڵ���
			String refCode = refTreeModel.getRefCodeField();// ����
			String refNodeName = refTreeModel.getRefNodeName();// ��������
			String rootName = refTreeModel.getRootName();// �����ڵ���
			if(datas==null)
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
			String refName = refModel.getRefNameField();
			String refCode = refModel.getRefCodeField();// ����
			String refNodeName = refModel.getRefNodeName();// ��������
			if(datas==null)
				datas = new Vector();
			// ����ѯ�����Ĳ����ֶ������ֶ�ֵһһ��Ӧ��װ��list��
			for (int i = 0; i < datas.size(); i++) {
				Map<Object, Object> map = new HashMap<Object, Object>();
				for (int j = 0; j < cloumns.size(); j++) {
					//����������Ϊpk
					if(pkFieldCode.equals(cloumns.elementAt(j)))
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
	 * ���ݵõ��Ĳ���ʾ����÷��ص�Json����
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String,List<BaseTreeGrid>> getJsonData(AbstractRefModel refModel,String pageIndex,String pageSize,Map<String,Object> filterMap)
			throws BusinessException {
		AbstractRefTreeModel refTreeModel = null;
		
		List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
		Map<String,List<BaseTreeGrid>> resultMap = new HashMap<String,List<BaseTreeGrid>>();
		// ������������ṹ
		if (refModel instanceof AbstractRefTreeModel) {
			refTreeModel = (AbstractRefTreeModel) refModel;
			String refNodeName = refTreeModel.getRefNodeName();// ��������
			
			// �������������ṹ
			if (refTreeModel instanceof AbstractRefGridTreeModel) {
				AbstractRefGridTreeModel refGridTreeModel = (AbstractRefGridTreeModel) refTreeModel;
				resultMap.put("treetable", getRefGridTreeModelData(refGridTreeModel));
				return resultMap;
			}
			Vector datas = refTreeModel.getData();
			Vector cloumns = refTreeModel.getAllColumnNames();
			String fatherField = refTreeModel.getFatherField();
			String pkFieldCode = refTreeModel.getPkFieldCode();
			String refName = refTreeModel.getRefNameField();// �ڵ���
			String refCode = refTreeModel.getRefCodeField();// ����
			refNodeName = refTreeModel.getRefNodeName();// ��������
			String rootName = refTreeModel.getRootName();// �����ڵ���
			if(datas==null)
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
			resultMap.put("tree", resultList);
			return resultMap;
		}
		// ��������Ǳ�ṹ
		else {
			Vector datas = getTableDataByPage(refModel,pageIndex,pageSize,filterMap);//refModel.getData();
			Vector cloumns = refModel.getAllColumnNames();
			String pkFieldCode = refModel.getPkFieldCode();
			String refName = refModel.getRefNameField();
			String refCode = refModel.getRefCodeField();// ����
			String refNodeName = refModel.getRefNodeName();// ��������
			if(datas==null)
				datas = new Vector();
			// ����ѯ�����Ĳ����ֶ������ֶ�ֵһһ��Ӧ��װ��list��
			for (int i = 0; i < datas.size(); i++) {
				Map<Object, Object> map = new HashMap<Object, Object>();
				for (int j = 0; j < cloumns.size(); j++) {
					//����������Ϊpk
					if(pkFieldCode.equals(cloumns.elementAt(j)))
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
			resultMap.put("table", resultList);
			return resultMap;
		}
	}
	
	
	/**��ȡ��ҳ������*/
	@SuppressWarnings({ "rawtypes", "serial" })
	private static Vector getTableDataByPage(AbstractRefModel refModel,String pageIndex,
			String pageSize,Map<String,Object> filterMap){
		
		Vector datas = new Vector();
		if(refModel.getRefSql() == null || "".equals(refModel.getRefSql())){
			return datas;
		}
		
		if(refModel.getAllColumnNames() == null || refModel.getAllColumnNames().size()==0){
			return datas;
		}
		String sql = refModel.getRefSql();
		final String pkFieldCode = refModel.getPkFieldCode();
		final String[] cloumns = refModel.getFieldCode();
		LimitSQLBuilder builder = SQLBuilderFactory.getInstance()
				.createLimitSQLBuilder(new BaseDAO().getDBType());
		String limitSql = builder.build(refModel.getRefSql(),
				Integer.parseInt(pageIndex), Integer.parseInt(pageSize));
		try {
			datas = (Vector)new BaseDAO().executeQuery(limitSql,
					new ResultSetProcessor(){
				
						@SuppressWarnings("unchecked")
						@Override
						public Object handleResultSet(ResultSet rs)
								throws SQLException {
							Vector datas = new Vector();
							while(rs.next()){
								Vector data = new Vector();
								for(int i=0;i<cloumns.length;i++){
									data.add(rs.getString(cloumns[i].toString()));
								}
								data.add(rs.getString(pkFieldCode.toString()));
								datas.add(data);
							}
							return datas;
						}
				
			});
		} catch (DAOException e) {
			e.printStackTrace();
		}
		return datas;
		
	}
	
	

	/**
	 * ������α�������
	 * 
	 * @param refGridTreeModel
	 * @return
	 * @throws BusinessException
	 */
	private static List<BaseTreeGrid> getRefGridTreeModelData(
			AbstractRefGridTreeModel refGridTreeModel) throws BusinessException {
		List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
		
		Vector datas = refGridTreeModel.getClassData();// �����������
		//String sql2 = refGridTreeModel.getRefSql();
		String[] cloumns = refGridTreeModel.getClassFieldCode();// ��������ֶ���
		String pkFieldCode = refGridTreeModel.getPkFieldCode();// �Ҳ�Ĳ���pk
		String refFieldName = refGridTreeModel.getRefNameField();// �Ҳ���ʾ�Ĳ�������
		String childField = refGridTreeModel.getChildField();// ����ӽڵ�
		String fatherField = refGridTreeModel.getFatherField();// ��ุ�ڵ�
		String joinField = refGridTreeModel.getClassJoinField();// ���ҹ������ֶ���
		String refNodeName = refGridTreeModel.getRefNodeName();// ��������
		String rootName = refGridTreeModel.getRootName();// �����ڵ���
		if(datas==null)
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
			//���ù�������
			refGridTreeModel.setClassJoinValue((String)map.get(joinField));
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
			List<Map<Object, Object>> childList = new ArrayList<Map<Object, Object>>();
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
		String[] fieldCode = refModel.getFieldCode();
		String[] fieldName = refModel.getFieldName();
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
			root.setTitle((String) (map.get(getFieldName(refModel, refName))==null?map.get(refName):map.get(getFieldName(refModel, refName))));
			root.setCode((String) (map.get(getFieldName(refModel, refCode))==null?map.get(refCode):map.get(getFieldName(refModel, refCode))));
			// �Ƴ���Ч��Ϣ

			map.remove(pkFieldCode);
			map.remove(fatherField);
			// ֻ�б�ṹʱset map
			String name = refModel.getRefNodeName();
			if (!(refModel instanceof AbstractRefTreeModel))
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
		String GridTreeeName = refGridTreeModel.getClassRefNameField();// ���������ڵ���
		String childField = refGridTreeModel.getChildField();// ����ӽڵ�
		String fatherField = refGridTreeModel.getFatherField();// ��ุ�ڵ�
		String refCode = refGridTreeModel.getRefCodeField();// ����

		List<BaseTreeGrid> rootList = new ArrayList<BaseTreeGrid>();
		for (Map<Object, Object> map : list) {
			BaseTreeGrid root = new BaseTreeGrid();
			root.setId((String) map.get(childField));
			root.setParentId((String) map.get(fatherField));
			root.setTitle((String) (map.get(getFieldName(refGridTreeModel,
					refName))==null?map.get(refName):map.get(getFieldName(refGridTreeModel,
							refName))));
			root.setCode((String) (map.get(getFieldName(refGridTreeModel,
					refCode))==null?map.get(refCode):map.get(getFieldName(refGridTreeModel,
							refCode))));

			rootList.add(root);
		}
		List<BaseTreeGrid> treeList = TreeUtils.formatTree(rootList);

		return treeList;
	}
}
