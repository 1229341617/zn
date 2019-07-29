package nc.jzmobile.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.uap.rbac.userpassword.IUserPasswordManage;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.pub.fa.common.util.StringUtils;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.sm.UserVO;

/**
 * 
 * 用户修改个人密码
 * @author mxx
 *
 */

public class ChangePasswordHandler implements INCMobileServletHandler{

	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Logger.info("==========ChangePasswordHandler start==========");
		Result result = Result.instance();
	
		
		try{
			IUserPasswordManage passwordManage = NCLocator.getInstance().lookup(IUserPasswordManage.class);
			
			String userid = map.get("userid");
			
			String newPassword = map.get("newPassword");
			
			//重置密码
			if(StringUtils.isEmpty(newPassword)){
				String msg = passwordManage.resetUserPassWord(userid);
				result.setData(msg);
			}else{
			//修改密码
				passwordManage.changeUserPassWord(getUserVO(userid), newPassword);
				result.setData("修改密码成功");
			}
	
			passwordManage.changeUserPassWord(getUserVO(userid), newPassword);
			
		}catch(Exception e){
			Logger.error("===========修改密码错误"+e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		
		Logger.info("==========ChangePasswordHandler end==========");
		return result;
	}
	
	/**
	 * 根据用户ID获得用户VO
	 * @param userId
	 * @return
	 */
	private UserVO getUserVO(String userId){
		StringBuffer sql = new StringBuffer();
		sql.append("select * from sm_user where cuserid ='" + userId + "'");
		BaseDAO dao = new BaseDAO();
		List<UserVO> userVO = new ArrayList();
		try {
			userVO = (List<UserVO>) dao.executeQuery(sql.toString(), new BeanListProcessor(UserVO.class));
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userVO.get(0);
	}

	

}
