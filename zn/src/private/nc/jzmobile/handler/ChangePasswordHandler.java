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
 * �û��޸ĸ�������
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
			
			//��������
			if(StringUtils.isEmpty(newPassword)){
				String msg = passwordManage.resetUserPassWord(userid);
				result.setData(msg);
			}else{
			//�޸�����
				passwordManage.changeUserPassWord(getUserVO(userid), newPassword);
				result.setData("�޸�����ɹ�");
			}
	
			passwordManage.changeUserPassWord(getUserVO(userid), newPassword);
			
		}catch(Exception e){
			Logger.error("===========�޸��������"+e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		
		Logger.info("==========ChangePasswordHandler end==========");
		return result;
	}
	
	/**
	 * �����û�ID����û�VO
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
