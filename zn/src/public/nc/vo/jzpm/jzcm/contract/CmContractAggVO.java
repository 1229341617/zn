package nc.vo.jzpm.jzcm.contract;

import java.util.HashMap;
import java.util.Map;

import nc.vo.jzcm.jzct.contract.CmContractAggVOMeta;
import nc.vo.jzcm.jzct.contract.CmContractVO;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;
import nc.vo.pubapp.pattern.model.meta.entity.bill.BillMetaFactory;
import nc.vo.pubapp.pattern.model.meta.entity.bill.IBillMeta;

@nc.vo.annotation.AggVoInfo(parentVO = "nc.vo.jzpm.jzcm.contract.CmContractVO")
public class CmContractAggVO extends AbstractBill {

	@Override
	public IBillMeta getMetaData() {
		IBillMeta billMeta = BillMetaFactory.getInstance().getBillMeta(
				CmContractAggVOMeta.class);
		return billMeta;
	}

	@Override
	public CmContractVO getParentVO() {
		return (CmContractVO) this.getParent();
	}

	/**
	 * 费用项目表体的code
	 */
	public static final String TABLE_CODE_LIST = "cost";
	/**
	 * 收付款协议的表体 code
	 */
	public static final String TABLE_CODE_TREATY = "treaty";
	/**
	 * 执行过程表体的code
	 */
	public static final String TABLE_CODE_EXECUTION = "exec";
	
	
	public static final String[] TAB_CODES = new String[] {TABLE_CODE_LIST, TABLE_CODE_TREATY, TABLE_CODE_EXECUTION};

	
	private Map<String, SuperVO[]> childrenMap = new HashMap<String, SuperVO[]>();
	
	@Override
	public void setTableVO(String tableCode,
			CircularlyAccessibleValueObject[] values) {
		childrenMap.put(tableCode, (SuperVO[])values);
	}

	@Override
	public SuperVO[] getTableVO(String tableCode) {
		return childrenMap.get(tableCode);
	}
	
	@Override
	public String[] getTableCodes() {
		return TAB_CODES;
	}

}