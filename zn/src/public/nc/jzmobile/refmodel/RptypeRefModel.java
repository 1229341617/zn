package nc.jzmobile.refmodel;

import nc.ui.bd.ref.AbstractRefModel;
import nc.ui.bd.ref.IRefDocEdit;
import nc.ui.bd.ref.IRefMaintenanceHandler;

/**
 * @����: �����������Ͳ��� ����
 * @����: liuhm
 * 
 */
public class RptypeRefModel extends AbstractRefModel {
//	private String beanID = null;

	
	public RptypeRefModel (){
		
		
	}
	public RptypeRefModel(String refNodeName) {
		setRefNodeName(refNodeName);

		setRefMaintenanceHandler(new IRefMaintenanceHandler() {

            @Override
            public String[] getFucCodes() {
            	// ���ܺ�
                return new String[] {"H57303005", };
            }

            @Override
            public IRefDocEdit getRefDocEdit() {
                return null;
            }
        });
	}


	/*@Override
	public void setRefNodeName(String refNodeName) {
		m_strRefNodeName = refNodeName;
		setFieldCode( new String[] { RptypeVO.VCODE, RptypeVO.VNAME} );
		setFieldName(new String[] {"����","����"});
		//setHiddenFieldCode( new String[] { RptypeVO.ICATEGORY } );
		setHiddenFieldCode( new String[] { RptypeVO.PK_RPTYPE } );

		setTableName(RptypeVO.TABLE_NAME );
		setPkFieldCode( RptypeVO.PK_RPTYPE);
		resetFieldName();
		
		this.setRefCodeField(RptypeVO.VCODE);
		this.setRefNameField(RptypeVO.VNAME);;
		
		// ���ô�Сд����
		setCaseSensive(true);

		// ��ӡ�ͣ�á�����
		setAddEnableStateWherePart(true);
//		setResourceID("");
	}*/

	@Override
	public String getWherePart() {
		return " dr=0";
	}
	/**
	 * �ܿ�ģʽ����
	 */
	@Override
	protected String getEnvWherePart() {
		String wherePart = null;
//		try {
//			wherePart= VisibleUtil.getRefVisibleCondition(getPk_group(), getPk_org(), getBeanID());
//		} catch (BusinessException e) {
//			Logger.error(e.getMessage());
//			wherePart= " 1=2 ";
//			return wherePart;
//		}
		return wherePart;
	}

//	private String getBeanID() throws MetaDataException {
//		if (beanID == null) {
//			IBean bean = MDBaseQueryFacade.getInstance().getBeanByFullClassName(BDProjTypeVO.class.getName());
//			beanID = bean.getID();
//		}
//		return beanID;
//	}

	@Override
	public String getRefTitle() {
		return "������������";
	}
}
