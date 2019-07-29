package nc.jzmobile.bill.handler;

import java.util.Map;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.mobile.app.IMobilebillExecute;
import nc.jzmobile.bill.util.BillMetaUtil;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;
/**
 * 提交操作Handler
 * @author wangruin on 2017/8/21
 *
 */
public class BillSubmitHandler implements INCMobileServletHandler{
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
			String id = map.get("id");
			if(id==null){
				throw new BusinessException("单据主键不能为空！");
			}
			InvocationInfoProxy.getInstance().setGroupId(
					JZMobileAppUtils.getPkGroupByUserId(userId));
			
			
			IMobilebillExecute service = NCLocator.getInstance().lookup(IMobilebillExecute.class);
			
			AggregatedValueObject aggVO = BillMetaUtil.queryAggVO(billType, id);
			
			if(aggVO==null)
				throw new BusinessException("根据单据类型："+billType+"和单据主键："+id+"查询不到单据信息！");
			
			AggregatedValueObject resultVO = service.submitBill(aggVO, billType);
		    
			if(resultVO.getParentVO()==null)
				throw new BusinessException("返回的表头数据为空，提交失败！");
			
			result.success().setData("提交成功!!!");
		} catch (Exception e) {
			e.printStackTrace();
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}

		return result;
	}
}
