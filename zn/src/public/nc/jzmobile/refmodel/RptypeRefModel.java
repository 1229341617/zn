package nc.jzmobile.refmodel;

import nc.ui.bd.ref.AbstractRefModel;
import nc.ui.bd.ref.IRefDocEdit;
import nc.ui.bd.ref.IRefMaintenanceHandler;

/**
 * @描述: 奖罚激励类型参照 树形
 * @作者: liuhm
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
            	// 功能号
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
		setFieldName(new String[] {"编码","名称"});
		//setHiddenFieldCode( new String[] { RptypeVO.ICATEGORY } );
		setHiddenFieldCode( new String[] { RptypeVO.PK_RPTYPE } );

		setTableName(RptypeVO.TABLE_NAME );
		setPkFieldCode( RptypeVO.PK_RPTYPE);
		resetFieldName();
		
		this.setRefCodeField(RptypeVO.VCODE);
		this.setRefNameField(RptypeVO.VNAME);;
		
		// 设置大小写敏感
		setCaseSensive(true);

		// 添加“停用”条件
		setAddEnableStateWherePart(true);
//		setResourceID("");
	}*/

	@Override
	public String getWherePart() {
		return " dr=0";
	}
	/**
	 * 管控模式控制
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
		return "奖罚激励类型";
	}
}
