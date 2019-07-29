package nc.mobile.app.impl;

import nc.bs.logging.Logger;
import nc.bs.pf.pub.PfDataCache;
import nc.vo.pmpub.common.utils.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.billtype.BilltypeVO;

/**
 * 移动平台DMO操作服务器
 * @author mazhyb
 *
 */
public class JZMobileDMOService {
	
	private JZMobileDMOService(){}

	/**
	 * 查询单据类型vo
	 * 
	 * @param billtype
	 * @return
	 */
	public static BilltypeVO getBillTypeVO(String billtype){
		return PfDataCache.getBillType(billtype);
	}
	
	public static AbstractJZMobileDMO getReferClass(String billtype)throws BusinessException{
		BilltypeVO vo = getBillTypeVO(billtype);
		if( vo == null ){
			throw new BusinessException("没有查询到对应的单据类型："+ billtype);
		}
		String classname = vo.getReferclassname();
		if( StringUtil.isEmpty(classname) )throw new BusinessException("单据类型："+billtype+"未设置参照查询对应的DMO类");
		try {
			Object dmo = Class.forName(classname).newInstance();
			if(!(dmo instanceof AbstractJZMobileDMO) ){
				throw new BusinessException("DMO类必须继承AbstractJZMobileDMO");
			}
			return (AbstractJZMobileDMO)dmo;
		} catch (Exception e) {
			Logger.error(e);
			throw new BusinessException(e.getMessage(),e);
		}
	}
	
	
	
}
