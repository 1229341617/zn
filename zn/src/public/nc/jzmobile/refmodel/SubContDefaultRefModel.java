package nc.jzmobile.refmodel;

import nc.ui.bd.ref.IRefDocEdit;
import nc.ui.bd.ref.IRefMaintenanceHandler;
import nc.ui.jzpm.ref.JZAbstractRefModel;
import nc.ui.jzpub.ref.IProjectRelation;
import nc.vo.jzpm.jzsub10.SubContractVO;
import nc.vo.jzpub.contract.ContStatus;


@SuppressWarnings("restriction")
public class SubContDefaultRefModel extends JZAbstractRefModel implements IProjectRelation{

	public SubContDefaultRefModel(){
		super();
		setRefTitle("�ְ���ͬ����");
		initRefModel();
		
	}

	protected void initRefModel() {
		setFieldCode(new String[] { SubContractVO.VBILLCODE, SubContractVO.VNAME });
		setHiddenFieldCode(new String[] { SubContractVO.PK_SUBCONTRACT ,SubContractVO.PK_PROJECT ,SubContractVO.TS});
		setTableName("jzsub_contract");
		setPkFieldCode( SubContractVO.PK_SUBCONTRACT );
		// ��ʾ�ֶ�����
		setFieldName(new String[] {"��ͬ����","��ͬ����"});
		
		//���ò��մ�ά�����������
		setRefMaintenanceHandler(new IRefMaintenanceHandler() {
			@Override
			public IRefDocEdit getRefDocEdit() {
				return null;
			}
			
			@Override
			public String[] getFucCodes() {
				return new String[]{"H519010"};	/** ��Ŀ-�ְ���ͬ */
			}
		});
		
		setWherePart(getConditionSql());
		this.setFilterRefNodeName(new String[]{"��Ŀ��֯"/*-=notranslate=-*/});
		
	}
	public void setRefNodeName(String refNodeName) {
		m_strRefNodeName = refNodeName;
		
	}
	/**
	 * ���ù�������
	 * @author sunywb
	 * @date 2014-1-14����03:56:56
	 * @return
	 */
	private String getConditionSql(){
		StringBuilder sql = new StringBuilder();
		sql.append(" iversion=0 and icontstatus= "+ContStatus.active.getIndex()+" ");
		sql.append(" and isnull(dr,0)=0 and not exists(select 1 from jzsub_finish_settle ");
		sql.append(" where jzsub_finish_settle.pk_subcontract=jzsub_contract.pk_subcontract ");
		sql.append(" and isnull(dr,0)=0 )");		
		return sql.toString();
	}
}
