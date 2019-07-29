package nc.jzmobile.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import nc.bs.dao.BaseDAO;
import nc.bs.logging.Logger;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.jzmobile.utils.BillTypeModelTrans;
import nc.md.model.IBusinessEntity;
import nc.uap.pf.metadata.PfMetadataTools;
import nc.vo.pf.mobileapp.TaskMetaData;
import nc.vo.pf.mobileapp.query.TaskQuery;
import nc.vo.pub.BusinessException;
import nc.vo.pub.pf.Pfi18nTools;

public class TitleDefUtil {

	/**
	 * 
	 * ���ڴ����Զ���title
	 */

	/**
	 * 
	 * ���ݴ����titleֵ,billtype,billid,��ѯ��Ӧ���ֶ�
	 * 
	 */
	@SuppressWarnings({ "serial", "unchecked" })
	public static List<String> query(List<String> titles, String billtype, String billid) throws BusinessException {
		if(!titles.contains("billmaker")){
			titles.add("billmaker");
		}
		List<String> list = new ArrayList<String>();
		final int length = titles.size();
		try {
			String tableName = getTablename(billtype);
			BaseDAO dao = new BaseDAO();
			StringBuffer sql = new StringBuffer();
			sql.append("select ");
			for (int i = 0; i < titles.size(); i++) {
				sql.append(titles.get(i) + ",");
			}
			sql.deleteCharAt(sql.length() - 1);
			sql.append(" from " + tableName);
			sql.append(" where ");
			sql.append(getPrimKey(billtype));
			sql.append("=");
			sql.append("'" + billid + "'");
			list = (List<String>) dao.executeQuery(sql.toString(), new ResultSetProcessor() {

				@Override
				public Object handleResultSet(ResultSet rs) throws SQLException {
					List<String> pks = new ArrayList<String>();
					while (rs.next()) {
						for (int i = 0; i < length; i++) {
							pks.add(rs.getString(i + 1));
						}
					}
					return pks;
				}

			});
		} catch (Exception e) {
			Logger.error(e);
			e.printStackTrace();
			throw new BusinessException(e);
		}
		return list;
	}

	/**
	 * ���ݴ����ֵ���ж��β�ѯ,�����ֶεı�ʾ ������һ���Ǳ���ֵ,�����ڶ�����Ӧ����Ӧ�ֶ�,�����������Ǳ���,�������Ҫ��ѯֵ
	 * @throws BusinessException 
	 * 
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public static List<String> querySecond(String[] strs, String value) throws BusinessException {
		List<String> list = new ArrayList<String>();
		final int length = strs.length - 3;
		try {
			String tableName = strs[strs.length - 3];
			BaseDAO dao = new BaseDAO();
			StringBuffer sql = new StringBuffer();
			sql.append("select ");
			for (int i = 0; i < strs.length - 3; i++) {
				sql.append(strs[i] + ",");
			}
			sql.deleteCharAt(sql.length() - 1);
			sql.append(" from " + tableName);
			sql.append(" where ");
			sql.append(strs[strs.length - 2]);
			sql.append("=");
			sql.append("'" + value + "'");
			list = (List<String>) dao.executeQuery(sql.toString(), new ResultSetProcessor() {

				@Override
				public Object handleResultSet(ResultSet rs) throws SQLException {
					List<String> pks = new ArrayList<String>();
					while (rs.next()) {
						for (int i = 0; i < length; i++) {
							pks.add(rs.getString(i + 1));
						}
					}
					return pks;
				}

			});
		} catch (Exception e) {
			Logger.error(e);
			e.printStackTrace();
			throw new BusinessException(e);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getList(List<Map<String, Object>> list, TaskQuery query)
			throws BusinessException {
		List<String> titleList = null;//���ԭʼ�ֶ�
		List<String> lists1 = null;//��Ų�ѯ�ֶ�
		List<String> lists2 = null;//��Ŷ��β�ѯ�ֶ�
		for (int i = 0; i < list.size(); i++) {
			titleList = new ArrayList<String>();
			lists1 = new ArrayList<String>();
			lists2 = new ArrayList<String>();
			TaskMetaData tmd = query.queryTaskMetaData(list.get(i).get("taskid").toString());
			String billtype = tmd.getBillType();
			String billid = tmd.getBillId();
			String title = null;
			try {
				title = BillTypeModelTrans.getInstance().getModelByBillType(billtype).getTitle();
				if (StringUtils.isNotEmpty(title)) {
					titleList = StrUtil.getSubUtil(title, "#(.*?)#");
					//title�а�����Ҫ�����ݿ�����ȡ�ı���
					if (titleList != null && titleList.size() > 0) {
						for (String t : titleList) {
							if(!t.contains(",")){
						    	lists1.add(t);
						    }else{
						    	lists2.add(t);
						    	lists1.add(t.split(",")[t.split(",").length-1]);
						    }
						}
						List<String> titlesList=query(lists1, billtype, billid);//��ŵ�һ�β�ѯ��������
						//�����Ƶ�������
						if(lists1.contains("billmaker")){
							int ind = lists1.indexOf("billmaker");
							Map<String, Object> map = list.get(i);
							map.put("makerman", Pfi18nTools.getUserName(titlesList.get(ind)));
							list.set(i, map);
						}
						
						for (int j = 0; j < titleList.size(); j++) {
							//������϶��β�ѯ
							if (titleList.get(j).contains(",")) {
								titlesList.set(j, querySecond(titleList.get(j).split(","), titlesList.get(j)).get(0));
							}
						}
						for (int j = 0; j < titleList.size(); j++) {
							title = title.replaceAll("#" + titleList.get(j) + "#", titlesList.get(j));
						}
						//list.set(i, (Map<String, Object>) list.get(i).put("title", title));
						list.get(i).put("title", title);
					}else{
						//list.set(i,(Map<String, Object>) list.get(i).put("title",title));
						list.get(i).put("title", title);
					}

				}else{
//					List<String> titles = new ArrayList<String>();
//					titles.add("billmaker");
//					List<String> billmakerList = TitleDefUtil.query(titles, tmd.getBillType(), tmd.getBillId());
//					Map<String, Object> map = list.get(i);
//					map.put("makerman", Pfi18nTools.getUserName(billmakerList.get(0)));
//					list.set(i, map);
					continue;
				}

				/*//2.�ж�title�Ƿ�Ϊ��--trueʹ��Ĭ�����ݿ����  falseʹ���Զ������
					if(null==title||"".equals(title)){
						continue;
					}
				//3.ȡ��title����Ӧ����	
					String [] titles=title.split("#");
					for(int j=0;j<titles.length;j++){
						if(!isContainChinese(titles[j].trim())){
							if(!"".equals(titles[j].trim())){
								titleList.add(titles[j].trim());
							}
						}
					}
					//title�а�����Ҫ�����ݿ�����ȡ�ı���
					if(titleList!=null&&titleList.size()!=0){
					
						for(int j=0;j<titleList.size();j++){
						    if(!titleList.get(j).contains(",")){
						    	lists1.add(titleList.get(j));
						    }else{
						    	lists2.add(titleList.get(j));
						    	lists1.add(titleList.get(j).split(",")[titleList.get(j).split(",").length-1]);
						    }
						}
					    List<String> titlesList=query(lists1, billtype, billid);//��ŵ�һ�β�ѯ��������
					    for(int j=0;j<titleList.size();j++){
					    	//������϶��β�ѯ
					    	if(titleList.get(j).contains(",")){
					    		titlesList.set(j,querySecond(titleList.get(j).split(","),titlesList.get(j)).get(0));
					    	}
					    }
						for(int j=0;j<titleList.size();j++){
							title=title.replaceAll("#"+titleList.get(j)+"#", titlesList.get(j));
						}
						list.set(i,(Map<String, Object>) list.get(i).put("title",title));
					//title�в����������ݿ�����ȡ�ı���
					}else{
						list.set(i,(Map<String, Object>) list.get(i).put("title",title));
					}*/
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("�����Զ���������nc.jzmobile.utils.TitleDefUtil.getList(List<Map<String, Object>>, TaskQuery)");
			}
		}

		return list;
	}

	//�ж��и��ַ������Ƿ�������
	private static boolean isContainChinese(String str) {

		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
	}

	public static String getPrimKey(String billType) throws Exception {

		IBusinessEntity be = PfMetadataTools.queryMetaOfBilltype(billType);

		String key = be.getTable().getPrimaryKeyName();
		//BillTempletUtil.templatInfo(billType);
		if (key == null)
			throw new BusinessException("����billType��ѯ��Ӧ����������Ϊ��");

		return key;
	}

	public static String getTablename(String billType) throws Exception {

		IBusinessEntity be = PfMetadataTools.queryMetaOfBilltype(billType);

		String tableName = be.getTable().getName();
		//BillTempletUtil.templatInfo(billType);
		if (tableName == null)
			throw new BusinessException("����billType��ѯ��Ӧ���������Ϊ��");

		return tableName;
	}

}
