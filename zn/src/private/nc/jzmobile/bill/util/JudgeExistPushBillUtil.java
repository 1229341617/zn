package nc.jzmobile.bill.util;

import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.jdbc.framework.processor.MapListProcessor;

/**
 * 
 * 
 * 判断是否已经推单
 * @author mxx
 *
 */
public class JudgeExistPushBillUtil {
	
	//质量安全推单
	@SuppressWarnings({ "unchecked", "unused" })
	public static boolean isExistQS(String billtype,String csrcid){
		
		String tableName = null,pkName = null;
		try {
			tableName = BillMetaUtil.getTableName(billtype);
			pkName = BillTempletUtil.getPkName(tableName);
			
			
			StringBuffer buffer = new StringBuffer();
			buffer.append("select pkName from "+tableName+" where csrcid = '"+csrcid+"'");
			
			List<Map<String, Object>> dataList = (List<Map<String, Object>>) new BaseDAO()
			.executeQuery(buffer.toString(), new MapListProcessor());
			
			if(dataList == null || dataList.size() == 0){
				return false;
			}else{
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}
