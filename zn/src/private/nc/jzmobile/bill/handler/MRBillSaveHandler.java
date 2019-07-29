package nc.jzmobile.bill.handler;

import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.mobile.app.IMobilebillExecute;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.pub.fa.common.util.StringUtils;
import nc.vo.ic.m45.entity.PurchaseInVO;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pub.BusinessException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/***
 * 移动收料代码整合
 * @author mxx
 *
 */
public class MRBillSaveHandler implements INCMobileServletHandler {

	@SuppressWarnings("unchecked")
	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		Logger.info("---BillSaveHandler  start---");
		try {
			String userId = map.get("userid");
			if (userId == null) {
				throw new BusinessException("用户信息不能为空！");
			}
			String billType = map.get("billtype");
			if (billType == null) {
				throw new BusinessException("单据类型不能为空！");
			}

			/** 用于查看附件 */
			String taskId =  map.get("taskid");//"MOBILE20190111171349";//
			// String filenames = map.get("filename");

			String pk_group = JZMobileAppUtils.getPkGroupByUserId(userId);

			InvocationInfoProxy.getInstance().setGroupId(pk_group);

			JSONObject headObject = JSON.parseObject(map.get("headdata"));
			Map<String, String> head = (Map<String, String>) headObject
					.get("PurchaseInHeadVO");
			JSONObject bodyObject = JSON.parseObject(map.get("bodydata"));
			List<Map<String, String>> bodys = (List<Map<String, String>>) bodyObject
					.get("cgeneralbid");

			IMobilebillExecute service = NCLocator.getInstance().lookup(
					IMobilebillExecute.class);
			PurchaseInVO[] vos = service.slSaveBill(userId, pk_group, head,
					bodys);

			if (vos == null) {
				throw new BusinessException("收料失败！！");
			}

			if (!StringUtils.isEmpty(taskId)) {
				updateImage(taskId, vos[0].getPrimaryKey());
			}

			result.success().setData("收料成功!!!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}

		return result;

	}

	private void updateImage(String tempid, String pk) {

		BaseDAO dao = new BaseDAO();
		String sql = "UPDATE sm_pub_filesystem SET filepath=REPLACE(filepath, '"
				+ tempid + "', '" + pk + "'); ";

		try {
			dao.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
