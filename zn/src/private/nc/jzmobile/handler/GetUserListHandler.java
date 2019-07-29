package nc.jzmobile.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.logging.Logger;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.sm.UserVO;

public class GetUserListHandler implements INCMobileServletHandler {
	/**
	 * 加载pk时默认加载系数。加载的总行数=起始行+请求行数*此系数
	 */
	public Result handler(Map<String, String> map) throws Exception {
		Logger.info("==========GetUserListHandler start==========");
		Result result = Result.instance();
		try{
			
			String condition = map.get("condition");
			int start=1;
			int count=10;
			try{
				start = Integer.parseInt(map.get("start"));
			}catch (Exception e) {}
			try{
				count = Integer.parseInt(map.get("count"));
			}catch (Exception e) {}
			Map<String, Object> resultMap = new HashMap<String, Object>();
			Integer sum = findUserCount(condition);
			List<Map<String, String>> userList = findUserList(condition,start,count);
			resultMap.put("count", sum);
			resultMap.put("userlist", userList);
			result.setData(resultMap);
		}catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		Logger.info("==========GetUserListHandler end==========");
		return result;
	}
	
	private List<Map<String, String>> findUserList(String condition,int start,int count) throws Exception {
//		String pk_group = getPkGroupByUserId(userId);
		StringBuffer sql = new StringBuffer();
		sql.append("select cuserid,user_code,user_name from (");
		sql.append("select user_code,user_name,cuserid,rownum rn from sm_user where pk_usergroupforcreate <> '~'");
		
		if(condition!=null&&!"".equals(condition)){
			sql.append(" and (");
			sql.append(" user_code like '%"+condition+"%' or user_name like '"+condition+"'");
			sql.append(" )");
		}
		sql.append(") where rn>="+start);
		sql.append(" and rn<"+(start+count));
		BaseDAO dao = new BaseDAO();
		List<Map<String, String>> userVoList = (List<Map<String, String>>) dao.executeQuery(sql.toString(), new ResultSetProcessor() {
			public Object handleResultSet(ResultSet rs) throws SQLException {
				List<Map<String, String>> userVoList = new ArrayList<Map<String, String>>();
				UserVO userVo = null;
				while(rs.next()){
					Map<String, String> userMap = new HashMap<String, String>();
					userMap.put("cuserid",rs.getString("cuserid"));
					userMap.put("user_code",rs.getString("user_code"));
					userMap.put("user_name",rs.getString("user_name"));
					
					userVoList.add(userMap);
				}
				return userVoList;
			}
		});
		return userVoList;
	}
	
	private Integer findUserCount(String condition) throws DAOException{
		StringBuffer sql = new StringBuffer();
		sql.append("select count(cuserid) from sm_user where pk_usergroupforcreate <> '~'");
		
		if(condition!=null&&!"".equals(condition)){
			sql.append(" and (");
			sql.append(" user_code like '%"+condition+"%' or user_name like '"+condition+"'");
			sql.append(" )");
		}
		BaseDAO dao = new BaseDAO();
		Integer count = (Integer) dao.executeQuery(sql.toString(), new ResultSetProcessor() {
			public Object handleResultSet(ResultSet rs) throws SQLException {
				Integer c = 0;
				while(rs.next()){
					c = rs.getInt(1);
				}
				return c;
			}
		});
		return count;
	}
}
