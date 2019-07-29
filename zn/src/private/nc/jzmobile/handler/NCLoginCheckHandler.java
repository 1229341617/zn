package nc.jzmobile.handler;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.bs.logging.Logger;
import nc.login.bs.INCUserQueryService;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.sm.UserVO;
import nc.vo.uap.rbac.util.RbacUserPwdUtil;
import sun.misc.BASE64Encoder;

public class NCLoginCheckHandler implements INCMobileServletHandler {

	@SuppressWarnings("rawtypes")
	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Logger.info("==========NCLoginCheckHandler start==========");
		Result result = Result.instance();
		try{
			String usercode = map.get("username");
			String password = map.get("password");
			String dsName = InvocationInfoProxy.getInstance().getUserDataSource();
			INCUserQueryService service = (INCUserQueryService) NCLocator.getInstance().lookup(INCUserQueryService.class);
			UserVO userVO = service.findUserVO(dsName, usercode);
			if (userVO != null) {
				 if (RbacUserPwdUtil.checkUserPassword(userVO, password)) {
					 byte sysid = InvocationInfoProxy.getInstance().getSysid();
//					 InvocationInfoProxy.getInstance().setUserCode(usercode);
					 ISecurityTokenCallback sc = (ISecurityTokenCallback) NCLocator.getInstance().lookup(ISecurityTokenCallback.class);
					 byte[] bytes = new byte[64];
					 new SecureRandom().nextBytes(bytes);
					 byte[] bts = sc.token((sysid + ":" + userVO.getPrimaryKey()).getBytes("UTF-8"),	bytes);
					 String token = new BASE64Encoder().encode(bts);
					 token = token.replaceAll("\r\n", "");
					 Logger.info("token = " + token);
					 LinkedHashMap<String, Object> outmap = new LinkedHashMap<String, Object>();
					 outmap.put("userid", userVO.getCuserid());
					 outmap.put("usercode", userVO.getUser_code());
					 outmap.put("username", userVO.getUser_name());
					 outmap.put("usergroup", userVO.getPk_group());
					 outmap.put("userorg", userVO.getPk_org());
					 outmap.put("nctoken", token);
					 result.setData(outmap);
					 result.setFlag("0");
				 }else{
					 result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
					 result.setErrorMessage("密码错误");
					 result.setFlag("1");
					 result.setDesc("密码错误");
				 }
			} else {
//				return MobileAppUtils.createOutValue("1", , "");
				result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
				result.setErrorMessage("用户不存在");
				result.setFlag("1");
				result.setDesc("用户不存在");
			}
		}catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
			result.setFlag("1");
			result.setDesc(e.getMessage());
		}
		Logger.info("==========NCLoginCheckHandler end==========");
		return result;
	}
	
}
