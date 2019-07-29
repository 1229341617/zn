package nc.jzmobile.bill.handler;

import java.util.Map;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.mobile.app.IMobilebillExecute;
import nc.jzmobile.bill.util.BillMetaUtil;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pf.mobileapp.MobileAppUtils;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;


/**
 * 提交的单据收回
 * @author mxx
 *
 */
public class RecallBillHandler implements INCMobileServletHandler{
	
	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		Logger.info("---RecallBillHandler  start---");
		try {
			String userId = map.get("userid");
			if (userId == null) {
				throw new BusinessException("用户信息不能为空！");
			}
			String billType = map.get("billtype");
			if (billType == null) {
				throw new BusinessException("单据类型不能为空！");
			}
			
			String billid = map.get("billid");
			InvocationInfoProxy.getInstance().setGroupId(
					MobileAppUtils.getPkGroupByUserId(userId));

			/**根据billtype和billid获取aggvo*/
			AggregatedValueObject aggVO = BillMetaUtil.queryAggVO(billType, billid);
			
			
			IMobilebillExecute service = NCLocator.getInstance().lookup(IMobilebillExecute.class);
			
			AggregatedValueObject resultVO = service.recallBill(aggVO, billType);
			
			if(resultVO.getParentVO()==null)
				throw new BusinessException("返回的表头数据为空，删除失败！");
			
			result.success().setData("收回成功!!!");
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}

		return result;
	}

}
