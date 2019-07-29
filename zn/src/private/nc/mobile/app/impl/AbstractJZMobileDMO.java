package nc.mobile.app.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nc.bs.logging.Logger;
import nc.bs.trade.business.HYSuperDMO;
import nc.itf.jzmobile.IJZMobileDMO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.trade.pub.IExAggVO;

/**
 * DMO基类
 */
public abstract class AbstractJZMobileDMO extends HYSuperDMO implements IJZMobileDMO {

	private static final long serialVersionUID = -437249276226822330L;

	protected Map<Class<? extends SuperVO>, String> PARENT_PK_FIELD_MAP = new HashMap<Class<? extends SuperVO>, String>();

	public AggregatedValueObject getAggVO(String billid) throws BusinessException {
		Class<? extends AggregatedValueObject> aggvoclass = this.getAggVOClass();
		Class<? extends SuperVO> headclass = this.getHeadVOClass();
		Map<String, Class<? extends SuperVO>> bodyclasses = this.getAllChildVOClass();
		try {
			AggregatedValueObject aggvo = aggvoclass.newInstance();
			HYSuperDMO dmo = new HYSuperDMO();
			SuperVO headvo = dmo.queryByPrimaryKey(headclass, billid);
			aggvo.setParentVO(headvo);
			for (Entry<String, Class<? extends SuperVO>> entry : bodyclasses.entrySet()) {
				Class<? extends SuperVO> child = entry.getValue();
				String parentPkFld = PARENT_PK_FIELD_MAP.get(child);
				if (null == parentPkFld) {
					SuperVO vo = child.newInstance();
					parentPkFld = vo.getParentPKFieldName();
					PARENT_PK_FIELD_MAP.put(child, parentPkFld);
				}
				SuperVO[] vos = dmo.queryByWhereClause(child, " 1=1 and " + parentPkFld + "='" + billid + "' and isnull(dr,0)=0");
				if (aggvo instanceof IExAggVO) {
					((IExAggVO) aggvo).setTableVO(entry.getKey(), vos);
				} else {
					// 单子表的情况下
					aggvo.setChildrenVO(vos);
				}
			}
			return aggvo;
		} catch (InstantiationException e) {
			Logger.error(e);
			throw new BusinessException(e);
		} catch (IllegalAccessException e) {
			Logger.error(e);
			throw new BusinessException(e);
		} catch(Exception e){
			Logger.error(e);
			throw new BusinessException(e);
		}
	}

}
