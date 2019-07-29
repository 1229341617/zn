package nc.vo.jzpm.jzsub10;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.vo.jzpub.contract.ContStatus;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.pf.BillStatusEnum;
import nc.vo.pubapp.AppContext;
import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;
import nc.vo.pubapp.pattern.model.meta.entity.bill.BillMetaFactory;
import nc.vo.pubapp.pattern.model.meta.entity.bill.IBillMeta;

import org.apache.commons.lang.SerializationUtils;

@nc.vo.annotation.AggVoInfo(parentVO = "nc.vo.jzpm.jzsub10.SubContractVO")
public class AggSubContractVO extends AbstractBill {

	private static final long serialVersionUID = -2907273665207871195L;

	@Override
	public IBillMeta getMetaData() {
		IBillMeta billMeta = BillMetaFactory.getInstance().getBillMeta(
				AggSubContractVOMeta.class);
		return billMeta;
	}

	@Override
	public SubContractVO getParentVO() {
		return (SubContractVO) this.getParent();
	}

	/**
	 * 修订时专用克隆方法
	 * 
	 * @author sunywb
	 * @date 2013-9-22下午03:17:42
	 * @return
	 */
	public AggSubContractVO cloneNewVO() {
		AggSubContractVO aggVo = new AggSubContractVO();
		dealHeadVO(aggVo);
		dealChildVos(aggVo);
		return aggVo;
	}

	/**
	 * 克隆时处理表头数据
	 * 
	 * @author sunywb
	 * @date 2013-9-17下午02:27:33
	 * @param aggVo
	 */
	public void dealHeadVO(AggSubContractVO aggVo) {
		// 克隆表头vo
		SubContractVO headVo = (SubContractVO) SerializationUtils
				.clone((Serializable) this.getParentVO());
		headVo.setPk_subcontract(null);// 合同主键
		headVo.setVbillcode(null);// 合同编码
		headVo.setIcontstatus(ContStatus.unactive.getIndex());// 合同状态
		headVo.setFstatusflag((Integer) BillStatusEnum.FREE.value());// 单据状态
		headVo.setTs(null);// ts
		headVo.setDbilldate(AppContext.getInstance().getBusiDate());// 单据日期
		headVo.setBillmaker(AppContext.getInstance().getPkUser());// 制单人
		headVo.setDmakedate(AppContext.getInstance().getServerTime());// 制单日期
		headVo.setApprover(null);// 审批人
		headVo.setTaudittime(null);// 审批时间
		headVo.setVapprovenote(null);// 审批意见
		headVo.setCreator(null);// 创建人
		headVo.setCreationtime(null);// 创建时间
		headVo.setModifier(null);// 最后修改人
		headVo.setModifiedtime(null);

		// 处理版本号
		Integer version = headVo.getIversion();
		version = version + 1;
		headVo.setIversion(version);

		// 是否最新版本bisnewversion
		headVo.setBisnewversion(UFBoolean.FALSE);

		aggVo.setParentVO(headVo);
	}

	/**
	 * 克隆时处理表体数据
	 * 
	 * @author sunywb
	 * @date 2013-9-17下午02:28:03
	 * @param aggVo
	 */
	public void dealChildVos(AggSubContractVO aggVo) {
		// 获得费用单表体数据
		SuperVO[] feeVos = (SuperVO[]) this
				.getChildren(SubContFeeVO.class);
		// 克隆费用单表体数据
		SuperVO[] feeCloneVos = (SuperVO[]) SerializationUtils
				.clone((Serializable) feeVos);
		// 对表体数据进行处理
		List<SubContFeeVO> listFeeVos = new ArrayList<SubContFeeVO>();
		for (SuperVO feeSuperVo : feeCloneVos) {
			SubContFeeVO feeVo = (SubContFeeVO)feeSuperVo;
			feeVo.setPk_contfee(null);
			feeVo.setPk_parent(null);
			feeVo.setPk_subcontract(null);
			feeVo.setTs(null);
			listFeeVos.add(feeVo);
		}
		// 给aggVo添加表体数据
		aggVo.setChildren(SubContFeeVO.class, listFeeVos.toArray(new SubContFeeVO[0]));

		// 获得合同付款表体数据
		SubContArticleVO[] articleVos = (SubContArticleVO[]) this
				.getChildren(SubContArticleVO.class);
		// 克隆合同付款表体数据
		SubContArticleVO[] articleCloneVos = (SubContArticleVO[]) SerializationUtils
				.clone((Serializable) articleVos);

		// 对表体数据进行处理
		for (SubContArticleVO articleVo : articleCloneVos) {
			articleVo.setPk_contarticle(null);
			articleVo.setPk_subcontract(null);
			articleVo.setTs(null);
			articleVo.setBisself(UFBoolean.FALSE);
		}

		// 给aggVo添加表体
		aggVo.setChildren(SubContArticleVO.class, articleCloneVos);

	}
	
	public static final String BUDGET_TAB_CODE = "pk_contfee";
	
	public static final String CHANGE_TAB_CODE = "pk_contarticle";
	
	public static final String VALSTAT_TAB_CODE = "pk_settle";
	
	public static final String VALREP_TAB_CODE = "pk_subcontsum";
	
	public static final String[] TAB_CODES = new String[] {BUDGET_TAB_CODE, CHANGE_TAB_CODE, VALSTAT_TAB_CODE, VALREP_TAB_CODE};

	
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