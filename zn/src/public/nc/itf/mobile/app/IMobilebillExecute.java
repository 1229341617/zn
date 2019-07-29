package nc.itf.mobile.app;

import java.util.List;
import java.util.Map;

import nc.vo.ic.m45.entity.PurchaseInVO;
import nc.vo.pu.m21.entity.OrderVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;

/**
 * 
 * @Created by wangruin on 2017/8/16
 *
 */
public interface IMobilebillExecute {
	/**
	 * ±£¥Ê
	 * @param aggVO
	 * @param billType
	 * @return
	 * @throws BusinessException
	 */
	AggregatedValueObject saveBill(AggregatedValueObject aggVO,String billType)throws BusinessException;
	
	AggregatedValueObject[] saveBatch(AggregatedValueObject[] aggVOs,String billType) throws BusinessException;
    /**
     * Ã·Ωª
     * @param aggVO
     * @param billType
     * @return
     * @throws BusinessException
     */
	AggregatedValueObject submitBill(AggregatedValueObject aggVO,String billType)throws BusinessException;
	
	AggregatedValueObject deleteBill(AggregatedValueObject aggVO,String billType)throws BusinessException;
	
	AggregatedValueObject recallBill(AggregatedValueObject aggVO,String billType)throws BusinessException;
	
	Object batchSaveBill(AggregatedValueObject[] aggVO,
			String billType) throws BusinessException;

	PurchaseInVO[] slSaveBill(String userid, String pk_group,
			Map<String, String> head, List<Map<String, String>> bodys)
			throws BusinessException;
	
	OrderVO[] xlSaveBill(String userid, String pk_group,
			Map<String, String> head, List<Map<String, String>> bodys)
			throws BusinessException;
	
	
	
}
