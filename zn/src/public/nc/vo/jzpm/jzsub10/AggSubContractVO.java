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
	 * �޶�ʱר�ÿ�¡����
	 * 
	 * @author sunywb
	 * @date 2013-9-22����03:17:42
	 * @return
	 */
	public AggSubContractVO cloneNewVO() {
		AggSubContractVO aggVo = new AggSubContractVO();
		dealHeadVO(aggVo);
		dealChildVos(aggVo);
		return aggVo;
	}

	/**
	 * ��¡ʱ�����ͷ����
	 * 
	 * @author sunywb
	 * @date 2013-9-17����02:27:33
	 * @param aggVo
	 */
	public void dealHeadVO(AggSubContractVO aggVo) {
		// ��¡��ͷvo
		SubContractVO headVo = (SubContractVO) SerializationUtils
				.clone((Serializable) this.getParentVO());
		headVo.setPk_subcontract(null);// ��ͬ����
		headVo.setVbillcode(null);// ��ͬ����
		headVo.setIcontstatus(ContStatus.unactive.getIndex());// ��ͬ״̬
		headVo.setFstatusflag((Integer) BillStatusEnum.FREE.value());// ����״̬
		headVo.setTs(null);// ts
		headVo.setDbilldate(AppContext.getInstance().getBusiDate());// ��������
		headVo.setBillmaker(AppContext.getInstance().getPkUser());// �Ƶ���
		headVo.setDmakedate(AppContext.getInstance().getServerTime());// �Ƶ�����
		headVo.setApprover(null);// ������
		headVo.setTaudittime(null);// ����ʱ��
		headVo.setVapprovenote(null);// �������
		headVo.setCreator(null);// ������
		headVo.setCreationtime(null);// ����ʱ��
		headVo.setModifier(null);// ����޸���
		headVo.setModifiedtime(null);

		// ����汾��
		Integer version = headVo.getIversion();
		version = version + 1;
		headVo.setIversion(version);

		// �Ƿ����°汾bisnewversion
		headVo.setBisnewversion(UFBoolean.FALSE);

		aggVo.setParentVO(headVo);
	}

	/**
	 * ��¡ʱ�����������
	 * 
	 * @author sunywb
	 * @date 2013-9-17����02:28:03
	 * @param aggVo
	 */
	public void dealChildVos(AggSubContractVO aggVo) {
		// ��÷��õ���������
		SuperVO[] feeVos = (SuperVO[]) this
				.getChildren(SubContFeeVO.class);
		// ��¡���õ���������
		SuperVO[] feeCloneVos = (SuperVO[]) SerializationUtils
				.clone((Serializable) feeVos);
		// �Ա������ݽ��д���
		List<SubContFeeVO> listFeeVos = new ArrayList<SubContFeeVO>();
		for (SuperVO feeSuperVo : feeCloneVos) {
			SubContFeeVO feeVo = (SubContFeeVO)feeSuperVo;
			feeVo.setPk_contfee(null);
			feeVo.setPk_parent(null);
			feeVo.setPk_subcontract(null);
			feeVo.setTs(null);
			listFeeVos.add(feeVo);
		}
		// ��aggVo��ӱ�������
		aggVo.setChildren(SubContFeeVO.class, listFeeVos.toArray(new SubContFeeVO[0]));

		// ��ú�ͬ�����������
		SubContArticleVO[] articleVos = (SubContArticleVO[]) this
				.getChildren(SubContArticleVO.class);
		// ��¡��ͬ�����������
		SubContArticleVO[] articleCloneVos = (SubContArticleVO[]) SerializationUtils
				.clone((Serializable) articleVos);

		// �Ա������ݽ��д���
		for (SubContArticleVO articleVo : articleCloneVos) {
			articleVo.setPk_contarticle(null);
			articleVo.setPk_subcontract(null);
			articleVo.setTs(null);
			articleVo.setBisself(UFBoolean.FALSE);
		}

		// ��aggVo��ӱ���
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