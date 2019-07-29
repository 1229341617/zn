package nc.jzmobile.refmodel;

import nc.ui.bd.ref.AbstractRefModel;
import nc.vo.jzpm.jzin2005.ContractVO;
import nc.vo.jzpub.contract.ContStatus;
import nc.vo.trade.pub.IBillStatus;

/**
 * �ܳа���ͬ���գ���Ϊjzpm�µĲ��շ�����client�ˣ��޷�����
 * 
 * @author wss
 * 
 */
public class ContDefaultRefModel extends AbstractRefModel {
	public ContDefaultRefModel() {
		super();
		setRefNodeName("ʩ���ܳа���ͬ");
	}

	public void setRefNodeName(String refNodeName) {
		m_strRefNodeName = refNodeName;
		setFieldCode(new String[] { ContractVO.VBILLCODE, ContractVO.VNAME });
		setHiddenFieldCode(new String[] { ContractVO.PK_CONTRACT,
				ContractVO.PK_PROJECT });
		this.setPkFieldCode(ContractVO.PK_CONTRACT);
		setTableName("jzin_contract");
		setPkFieldCode(ContractVO.PK_CONTRACT);
		// ��ʾ�ֶ�����
		setFieldName(new String[] { "��ͬ����", "��ͬ����" });

		// ��������Ȩ��
		// setResourceID(IBDResourceIDConst.BANKTYPE);
		this.setWherePart(getWherePart());

		this.setFilterRefNodeName(new String[] { "ҵ��Ԫ"/* -=notranslate=- */});

	}

	@Override
	public void reset() {
		super.reset();
	}

	@Override
	public void setWherePart(String newWherePart) {

		if (newWherePart != null && newWherePart.length() > 0) {
			newWherePart += " and isnull(dr,0)=0";
		} else {
			newWherePart = " isnull(dr,0)=0 and fstatusflag = "
					+ IBillStatus.CHECKPASS + " and icontstatus = "
					+ ContStatus.active.getIndex();
		}

		super.setWherePart(newWherePart);
	}

	@Override
	protected String getEnvWherePart() {

		String wherePart = " pk_contract in (select pk_contract from jzin_task where dr = 0 and fstatusflag = "
				+ IBillStatus.CHECKPASS
				+ " and "
				+ " pk_org = '"
				+ getPk_org()
				+ "') ";

		return wherePart;
	}
}
