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

public class GetUserHandler implements INCMobileServletHandler {
	/**
	 * 加载pk时默认加载系数。加载的总行数=起始行+请求行数*此系数
	 */
	public Result handler(Map<String, String> map) throws Exception {
		Logger.info("==========GetUserListHandler start==========");
		Result result = Result.instance();
		try{
			
			String condition = map.get("condition");
		
			List<Map<String, String>> userList = findUserList(condition);
			if(null != userList && userList.size() >0 ){
				result.setData(userList.get(0).get("cuserid"));
			}else{
				result.setData(null);
			}
			
		
		}catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		Logger.info("==========GetUserListHandler end==========");
		return result;
	}
	
	private List<Map<String, String>> findUserList(String condition) throws Exception {
//		String pk_group = getPkGroupByUserId(userId);
		StringBuffer sql = new StringBuffer();
		sql.append("select cuserid,user_code,user_name from sm_user where user_code ='" + condition + "'");
		BaseDAO dao = new BaseDAO();
		@SuppressWarnings("unchecked")
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
	

}
