package nc.vo.jzpm.jzcm.settle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.vo.annotation.AggVoInfo;
import nc.vo.jzbase.pub.IFlowBodyTSValidatable;
import nc.vo.jzbase.pub.IFlowHeadTSValidatable;
import nc.vo.jzbase.pub.IJZPKLockable;
import nc.vo.jzbase.pub.JzFlowTsInfo;
import nc.vo.jzbase.pub.JzFlowTsValidateVO;
import nc.vo.jzbase.pub.tool.SafeCompute;
import nc.vo.jzcm.jzct.contract.CmContractVO;
import nc.vo.jzcm.jzct.contract.CmListVO;
import nc.vo.jzcm.jzct.deduct.CmDeductVO;
import nc.vo.jzcm.jzct.settle.AggCmSettleVOMeta;
import nc.vo.jzcm.jzct.settle.CmSettleRegulateVO;
import nc.vo.jzcm.jzct.settle.CmSettleVO;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;
import nc.vo.pubapp.pattern.model.meta.entity.bill.BillMetaFactory;
import nc.vo.pubapp.pattern.model.meta.entity.bill.IBillMeta;

@AggVoInfo(parentVO = "nc.vo.jzpm.jzcm.settle.CmSettleVO")
public class AggCmSettleVO extends AbstractBill implements
		IFlowHeadTSValidatable, IJZPKLockable, IFlowBodyTSValidatable
		{
	private static final long serialVersionUID = -4004702665024477990L;

	public IBillMeta getMetaData() {
		IBillMeta billMeta = BillMetaFactory.getInstance().getBillMeta(
				AggCmSettleVOMeta.class);
		return billMeta;
	}

	public CmSettleVO getParentVO() {
		return (CmSettleVO) getParent();
	}

	public List<String> getHeadFieldList() {
		return Arrays.asList(new String[] { "pk_contract" });
	}

	public Map<Class<? extends SuperVO>, List<String>> getBodyFieldMap() {
		Map<Class<? extends SuperVO>, List<String>> map = new HashMap();
		map.put(CmListVO.class, Arrays.asList(new String[] { "csrcbid" }));
		map.put(CmDeductVO.class, Arrays.asList(new String[] { "csrcid" }));
		return null;
	}

	public List<JzFlowTsValidateVO> getValidateItemList() {
		return null;
	}

	public boolean isSingleFlow() {
		return true;
	}

	public JzFlowTsInfo getSingleFlowTsInfo() {
		return new JzFlowTsInfo(CmContractVO.class, "pk_contract");
	}

	public List<Class<? extends SuperVO>> getValidateBodyClassList() {
		List<Class<? extends SuperVO>> list = new ArrayList();

		list.add(CmSettleRegulateVO.class);
		return list;
	}

	public UFDouble getSourceMny() {
		return SafeCompute.addWithNull(getParentVO().getNorigsettlemny(),
				getParentVO().getNorigoperamny());
	}

	public boolean isSpecialProcess() {
		return false;
	}

	public void processVO4FIP() {
	}

	public static final String TABLE_CODE_TOTAL = "total";
	public static final String TABLE_CODE_DETAIL = "detail";
	public static final String TABLE_CODE_REGULATE = "regulate";

	public static final String[] TAB_CODES = new String[] { TABLE_CODE_TOTAL,
		TABLE_CODE_DETAIL, TABLE_CODE_REGULATE };

	private Map<String, SuperVO[]> childrenMap = new HashMap<String, SuperVO[]>();

	@Override
	public void setTableVO(String tableCode,
			CircularlyAccessibleValueObject[] values) {
		childrenMap.put(tableCode, (SuperVO[]) values);
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
