package nc.vo.jzpm.jzpp05;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.vo.jzpm.jzin2005.ContractVO;
import nc.vo.jzpub.jzpp05.ApproveAggVOMeta;
import nc.vo.jzpub.jzpp05.ApproveVO;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.ISuperVO;
import nc.vo.pub.SuperVO;
import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;
import nc.vo.pubapp.pattern.model.meta.entity.bill.BillMetaFactory;
import nc.vo.pubapp.pattern.model.meta.entity.bill.IBillMeta;

@nc.vo.annotation.AggVoInfo(parentVO = "nc.vo.jzpm.jzpp05.ApproveVO")
public class ApproveAggVO extends AbstractBill {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String CONTRACT_TAB_CODE = "contract";
	
	public static final String[] ALONE_LOAD_TAB_CODES = new String[]{CONTRACT_TAB_CODE};
	
	public static final Set<Class<? extends SuperVO>> ALONE_LOAD_CHILDCLASS_SET = new HashSet<Class<? extends SuperVO>>();
	
	static {
		ALONE_LOAD_CHILDCLASS_SET.add(ContractVO.class);
	}
	
	private Map<String, List<? extends SuperVO>> aloneLoadChildMap = new HashMap<String, List<? extends SuperVO>>();
	
	public void addAloneLoadChild(String tabCode, List<? extends SuperVO> datas) {
		aloneLoadChildMap.put(tabCode, datas);
	}
	
	public boolean isLoad(String tabCode) {
		return aloneLoadChildMap.containsKey(tabCode);
	}
	
	public List<? extends SuperVO> getAloneLoadChild(String tabCode) {
		return aloneLoadChildMap.get(tabCode);
	}

	@Override
	public IBillMeta getMetaData() {
		IBillMeta billMeta = BillMetaFactory.getInstance().getBillMeta(
				ApproveAggVOMeta.class);
		return billMeta;
	}

	@Override
	public ApproveVO getParentVO() {
		return (ApproveVO) this.getParent();
	}
	
	public boolean isAloneChildrenClass(Class<? extends ISuperVO> clazz) {
		return ALONE_LOAD_CHILDCLASS_SET.contains(clazz);
	}
	
	@Override
	public void setChildren(Class<? extends ISuperVO> clazz, ISuperVO[] vos) {
		if(isAloneChildrenClass(clazz)) {
			return;
		}
		super.setChildren(clazz, vos);
	}
	
	@Override
	public ISuperVO[] getChildren(Class<? extends ISuperVO> clazz) {
		if(isAloneChildrenClass(clazz)) {
			return new SuperVO[0];
		}
		return super.getChildren(clazz);
	}
	
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
		return ALONE_LOAD_TAB_CODES;
	}
	
}