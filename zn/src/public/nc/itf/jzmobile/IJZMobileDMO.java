package nc.itf.jzmobile;

import java.util.Map;

import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;

public interface IJZMobileDMO extends java.io.Serializable{

	/**
	 * ��ȡaggvo��class
	 * @return
	 */
	public Class<? extends AggregatedValueObject> getAggVOClass();
	
	/**
	 * ��ȡ��ͷvo��class
	 * @return
	 */
	public Class<? extends SuperVO> getHeadVOClass();
	
	/**
	 * ��ȡ���еı��� vo class
	 * @return �����Ƕ�Ӧ��tablecode��ֵΪ��Ӧ��vo class
	 */
	public Map<String,Class<? extends SuperVO>> getAllChildVOClass();
	
	/**
	 * ��ѯaggvo������Ƕ��ӱ��򷵻�IExAggVO
	 * @param billid
	 * @return
	 * @throws BusinessException
	 */
	public AggregatedValueObject getAggVO(String billid) throws BusinessException;
	

	
}
