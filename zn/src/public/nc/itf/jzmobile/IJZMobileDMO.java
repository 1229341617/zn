package nc.itf.jzmobile;

import java.util.Map;

import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;

public interface IJZMobileDMO extends java.io.Serializable{

	/**
	 * 获取aggvo的class
	 * @return
	 */
	public Class<? extends AggregatedValueObject> getAggVOClass();
	
	/**
	 * 获取表头vo的class
	 * @return
	 */
	public Class<? extends SuperVO> getHeadVOClass();
	
	/**
	 * 获取所有的表体 vo class
	 * @return 主键是对应的tablecode，值为对应的vo class
	 */
	public Map<String,Class<? extends SuperVO>> getAllChildVOClass();
	
	/**
	 * 查询aggvo，如果是多子表，则返回IExAggVO
	 * @param billid
	 * @return
	 * @throws BusinessException
	 */
	public AggregatedValueObject getAggVO(String billid) throws BusinessException;
	

	
}
