package nc.jzmobile.refmodel;

import nc.ui.bd.ref.AbstractRefModel;
import nc.vo.jzpm.jzin2005.ContractVO;
import nc.vo.jzpub.contract.ContStatus;
import nc.vo.trade.pub.IBillStatus;

/**
 * 总承包合同参照，因为jzpm下的参照放在了client端，无法访问
 * 
 * @author wss
 * 
 */
public class ContDefaultRefModel extends AbstractRefModel {
	public ContDefaultRefModel() {
		super();
		setRefNodeName("施工总承包合同");
	}

	public void setRefNodeName(String refNodeName) {
		m_strRefNodeName = refNodeName;
		setFieldCode(new String[] { ContractVO.VBILLCODE, ContractVO.VNAME });
		setHiddenFieldCode(new String[] { ContractVO.PK_CONTRACT,
				ContractVO.PK_PROJECT });
		this.setPkFieldCode(ContractVO.PK_CONTRACT);
		setTableName("jzin_contract");
		setPkFieldCode(ContractVO.PK_CONTRACT);
		// 显示字段名称
		setFieldName(new String[] { "合同编码", "合同名称" });

		// 设置数据权限
		// setResourceID(IBDResourceIDConst.BANKTYPE);
		this.setWherePart(getWherePart());

		this.setFilterRefNodeName(new String[] { "业务单元"/* -=notranslate=- */});

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
