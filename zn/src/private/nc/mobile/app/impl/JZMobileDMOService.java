package nc.mobile.app.impl;

import nc.bs.logging.Logger;
import nc.bs.pf.pub.PfDataCache;
import nc.vo.pmpub.common.utils.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.billtype.BilltypeVO;

/**
 * �ƶ�ƽ̨DMO����������
 * @author mazhyb
 *
 */
public class JZMobileDMOService {
	
	private JZMobileDMOService(){}

	/**
	 * ��ѯ��������vo
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
			throw new BusinessException("û�в�ѯ����Ӧ�ĵ������ͣ�"+ billtype);
		}
		String classname = vo.getReferclassname();
		if( StringUtil.isEmpty(classname) )throw new BusinessException("�������ͣ�"+billtype+"δ���ò��ղ�ѯ��Ӧ��DMO��");
		try {
			Object dmo = Class.forName(classname).newInstance();
			if(!(dmo instanceof AbstractJZMobileDMO) ){
				throw new BusinessException("DMO�����̳�AbstractJZMobileDMO");
			}
			return (AbstractJZMobileDMO)dmo;
		} catch (Exception e) {
			Logger.error(e);
			throw new BusinessException(e.getMessage(),e);
		}
	}
	
	
	
}
