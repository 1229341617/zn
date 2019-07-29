package nc.jzmobile.bill.data.access;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.framework.common.NCLocator;
import nc.bs.jzmobile.template.strategy.BillItemStrategy;
import nc.bs.logging.Logger;
import nc.itf.uap.billtemplate.IBillTemplateQry;
import nc.jzmobile.consts.IBillDataConst;
import nc.md.data.access.DASFacade;
import nc.md.data.access.NCObject;
import nc.md.model.IBusinessEntity;
import nc.ui.bd.ref.AbstractRefModel;
import nc.ui.pub.bill.IBillItem;
import nc.vo.am.timerule.LeaseTimeRuleVO;
import nc.vo.jzmobile.template.comparator.BillTabVOComparator;
import nc.vo.jzmobile.template.comparator.BillTempletBodyVOComparator;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.bill.BillTabVO;
import nc.vo.pub.bill.BillTempletBodyVO;
import nc.vo.pub.bill.BillTempletVO;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.trade.pub.IExAggVO;

import org.apache.commons.lang.StringUtils;

/**
 * 查询【单据模板】 的详细信息
 * 
 * @author lixyw
 * 
 */

public class NCBillTemplate {

	public static final SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat sdf_ymd_hms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// 排序
	public static final BillTabVOComparator TABVO_COMPARATOR = new BillTabVOComparator();
	public static final BillTempletBodyVOComparator BODYVO_COMPARATOR = new BillTempletBodyVOComparator();

	private BillTempletVO billTempletVO = null;
	private AggregatedValueObject billVO = null;
	private String pk_billTemplet;
	private List<BillTempletBodyVO> headTails = new ArrayList<BillTempletBodyVO>();
	private Map<String, List<BillTempletBodyVO>> bodys = new HashMap<String, List<BillTempletBodyVO>>();
	private NCBillTemplateItem processor = new NCBillTemplateItem();
	private BillItemStrategy processorT = new BillItemStrategy(); //获取模板
	public List<PubBillTempletTModel> bttList = new ArrayList<PubBillTempletTModel>();
	public List<PubBillTempletBModel> btbList = new ArrayList<PubBillTempletBModel>();
	public NCBillTemplate(String pk_billtemplet,AggregatedValueObject aggvo) {
		pk_billTemplet = pk_billtemplet;
		// this.billVO = billVO;
		// init();
		loadTemplate();
		billVO = aggvo;
	}

	public void loadTemplate() {
		billTempletVO = getBillTempletVO(pk_billTemplet);
		if (billTempletVO.getBodyVO() != null && billTempletVO.getBodyVO().length > 0) {
			for (int i = 0; i < billTempletVO.getBodyVO().length; i++) {
				// 只有卡片界面显示的内容才会继续
//				if (!billTempletVO.getBodyVO()[i].getShowflag().booleanValue())
//					continue;
				switch (billTempletVO.getBodyVO()[i].getPos()) {
				case IBillItem.BODY:
					String tablecode = billTempletVO.getBodyVO()[i]
							.getTable_code();
					if (bodys.containsKey(tablecode)) {
						bodys.get(tablecode).add(billTempletVO.getBodyVO()[i]);
					} else {
						List<BillTempletBodyVO> list = new ArrayList<BillTempletBodyVO>();
						list.add(billTempletVO.getBodyVO()[i]);
						bodys.put(tablecode, list);
					}

					break;
				case IBillItem.HEAD:
					headTails.add(billTempletVO.getBodyVO()[i]);
					break;
				// case IBillItem.TAIL:
				// billTempletVO.getBodyVO()[i].setPos(IBillItem.HEAD);
				// headTails.add(billTempletVO.getBodyVO()[i]);
				// break;
				}
			}// end of for loop
			Collections.sort(headTails, BODYVO_COMPARATOR);
		}
	}

	private BillTempletVO getBillTempletVO(String pk_billTemplet) {

		BillTempletVO vo = null;
		IBillTemplateQry qry = (IBillTemplateQry) NCLocator.getInstance()
				.lookup(IBillTemplateQry.class.getName());
		try {
			vo = qry.findTempletData(pk_billTemplet);
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
		}
		return vo;
	}

	public void billVO2Map(String corp,String userid) {
		if (this.billTempletVO == null || billTempletVO.getBodyVO() == null
				|| billTempletVO.getBodyVO().length == 0 || this.billVO == null) {
			return;
		}
		putHeadData( corp, userid);
		// 处理表体
		putBodyData(corp, userid);
	}
	
	public BillTempletModel billVO2MapT(String corp,String userid) {
		BillTempletModel billTempletModel = new BillTempletModel();
		if (this.billTempletVO == null || billTempletVO.getBodyVO() == null
				|| billTempletVO.getBodyVO().length == 0 || this.billVO == null) {
			return billTempletModel;
		}
		
		List<TempletTabModel> heads = putHeadDataT(corp,userid);
		billTempletModel.setHeads(heads);
		// 处理表体
		List<TempletTabModel> bodys = putBodyDataT(corp, userid);
		billTempletModel.setBodys(bodys);
		return billTempletModel;
	}
	
	private List<TempletTabModel> putBodyDataT(String corp, String userid) {
		List<TempletTabModel> tabModels = new ArrayList<TempletTabModel>();
		// 支持多子表
		if (billVO instanceof IExAggVO) {
			IExAggVO aggvo = (IExAggVO) billVO;
			List<BillTabVO> tabvos = getBodyTabVO();
			for (int i =0;i< tabvos.size() ; i++) {
				BillTabVO tabvo = tabvos.get(i);
				String tablecode = tabvo.getBasetab() != null && tabvo.getBasetab().length() > 0 ? tabvo.getBasetab() : tabvo.getTabcode();
				
				//tabvo.getBasetab() != null && tabvo.getBasetab().length() > 0 ? tabvo.getBasetab() : tabvo.getTabcode();
				List<BillTempletBodyVO> tplvos = bodys.get(tablecode);
				if(tplvos==null)
					continue;
				Collections.sort(tplvos, BODYVO_COMPARATOR);// 排序
				
				tablecode = tabvo.getMetadatapath();
				if(tablecode==null)
					continue;
				CircularlyAccessibleValueObject[] bodyVOs = aggvo.getTableVO(tablecode);
				if (bodyVOs == null)
				{
					bodyVOs = new CircularlyAccessibleValueObject[1];
				}else{
					//转换计租方式字段的显示值
					String rent_type = (String) bodyVOs[0].getAttributeValue("rent_type");
					if (StringUtils.isNotBlank(rent_type)){
						LeaseTimeRuleVO rent_type_name = LeaseTimeRuleVO.fromXml(rent_type);
						bodyVOs[0].setAttributeValue("rent_type", rent_type_name);
					}
				}
				//dealFormular(tplvos, bodyVOs);
				TempletTabModel tabModel = getBodyTabDataMapT(bodyVOs,tplvos.toArray(new BillTempletBodyVO[0]), IBillDataConst.BODY, corp, userid, tabvo);
				if (tabModel != null) {
					tabModels.add(tabModel);
				}
			}
		} else {
			CircularlyAccessibleValueObject[] bodyVOs = billVO.getChildrenVO();
			if(bodyVOs==null)
				bodyVOs = new CircularlyAccessibleValueObject[0];
			//dealFormular(billTempletVO.getBodyVO(), bodyVOs);
			
			TempletTabModel tabModel = getTabDataMapT(bodyVOs, billTempletVO.getBodyVO(),IBillDataConst.BODY, corp, userid, new BillTabVO());
			if (tabModel != null) {
				tabModels.add(tabModel);
			}
		}
		
		return tabModels;
	}
	
	public TempletTabModel getBodyTabDataMapT(CircularlyAccessibleValueObject[] bodyVOs,
			BillTempletBodyVO[] itemVOs, int pos, String corp, String userid,
			BillTabVO tabvo) {
	
		TempletTabModel tabModel = new TempletTabModel();
		tabModel.setTabCode(tabvo.getTabcode());
		tabModel.setTabIndex(tabvo.getTabindex().toString());
		tabModel.setTabName(tabvo.getTabname());
	
	    for (int bvo = 0; bvo < bodyVOs.length; bvo++) {
		   List<TempletModel> models = new ArrayList<TempletModel>();
		   IBusinessEntity be = tabvo.getBillMetaDataBusinessEntity();
		   if(be==null)
			  continue;
		   NCObject ncos = DASFacade.newInstanceWithContainedObject(be, bodyVOs);
		
		   for (int i = 0; i < itemVOs.length; i++) {
			   if (itemVOs[i].getPos() != pos || !itemVOs[i].getShowflag().booleanValue()) {
				  continue;
			   }
			   TempletModel  model= processorT.processT(corp, userid,itemVOs[i], bodyVOs[bvo], ncos);
			   if (model != null) {
				  models.add(model);
			   }
		   }
		   tabModel.setModel(models);
	   }
	return tabModel;
}

	
	private List<TempletTabModel> putHeadDataT(String corp,String userid) {
		CircularlyAccessibleValueObject headTailVO = billVO.getParentVO();
		
		// 处理显示公式
		//dealFormular(headTails, new CircularlyAccessibleValueObject[] { headTailVO });

		CircularlyAccessibleValueObject[] headTailVOs = new CircularlyAccessibleValueObject[] { headTailVO };

		List<BillTabVO> tabvos = getHeadTabVO();
		List<TempletTabModel> tabModels = new ArrayList<TempletTabModel>();
		for (BillTabVO tabvo : tabvos) {
			List<BillTempletBodyVO> bvos = getHeadBodyVOs(headTails, tabvo);
			if(bvos==null||bvos.size()==0)
				continue;
			TempletTabModel tabModel = getTabDataMapT(headTailVOs,bvos.toArray(new BillTempletBodyVO[0]),IBillDataConst.HEAD, corp, userid, tabvo,tabvos);
			if (tabModel != null) {
				tabModels.add(tabModel);
			}
		}
		return tabModels;

	}
	
	public TempletTabModel getTabDataMapT(CircularlyAccessibleValueObject[] bodyVOs,
			BillTempletBodyVO[] itemVOs, int pos, String corp, String userid,
			BillTabVO tabvo) {
		
		TempletTabModel tabModel = new TempletTabModel();
		tabModel.setTabCode(tabvo.getTabcode());
		tabModel.setTabName(tabvo.getTabname());
		tabModel.setTabIndex(tabvo.getTabindex().toString());
		for (int bvo = 0; bvo < bodyVOs.length; bvo++) {
			
			IBusinessEntity be = tabvo.getBillMetaDataBusinessEntity();
			if(be==null){
				continue;
			}
			List<TempletModel> models = new ArrayList<TempletModel>();
			NCObject ncos = DASFacade.newInstanceWithContainedObject(be, bodyVOs[bvo]);
			
			for (int i = 0; i < itemVOs.length; i++) {
				if (itemVOs[i].getPos() != pos || 
						!itemVOs[i].getShowflag().booleanValue()) {
					continue;
				}
				
				TempletModel model = processorT.processT(corp, userid,itemVOs[i], bodyVOs[bvo], ncos);
				if (model != null) {
					models.add(model);
				}
			}
			if(models.size()>0)
				tabModel.setModel(models);
		}
		return tabModel;
	}
	
	public TempletTabModel getTabDataMapT(CircularlyAccessibleValueObject[] bodyVOs,
			BillTempletBodyVO[] itemVOs, int pos, String corp, String userid,
			BillTabVO tabvo,List<BillTabVO> tabvos) {
		
		TempletTabModel tabModel = new TempletTabModel();
		tabModel.setTabCode(tabvo.getTabcode());
		tabModel.setTabName(tabvo.getTabname());
		tabModel.setTabIndex(tabvo.getTabindex().toString());
		for (int bvo = 0; bvo < bodyVOs.length; bvo++) {
			
			IBusinessEntity be = tabvo.getBillMetaDataBusinessEntity();
			if(be==null){
				for(BillTabVO temptab : tabvos){
					be = temptab.getBillMetaDataBusinessEntity();
					if(be != null){
						break;
					}
				}
			}
			List<TempletModel> models = new ArrayList<TempletModel>();
			NCObject ncos = DASFacade.newInstanceWithContainedObject(be, bodyVOs[bvo]);
			
			for (int i = 0; i < itemVOs.length; i++) {
				if (itemVOs[i].getPos() != pos || 
						!itemVOs[i].getShowflag().booleanValue()) {
					continue;
				}
				
				TempletModel model = processorT.processT(corp, userid,itemVOs[i], bodyVOs[bvo], ncos);
				if (model != null) {
					models.add(model);
				}
			}
			if(models.size()>0)
				tabModel.setModel(models);
		}
		return tabModel;
	}
	
	public String getItemRefClassName(String tabCode,String itemKey){
		BillTabVO tabvo = getTabVO(tabCode);
		IBusinessEntity be = tabvo.getBillMetaDataBusinessEntity();
		//String refModelName = be.getAttributeByName(itemKey).getRefModelName();
		if(be==null)
			return null;
		NCObject ncos = DASFacade.newInstanceWithContainedObject(be, null);
		return processor.getRefModeName(itemKey,ncos);
	}
	
	private void putHeadData(String corp,String userid) {
		CircularlyAccessibleValueObject headTailVO = billVO.getParentVO();
		
		// 处理显示公式
//		dealFormular(headTails, new CircularlyAccessibleValueObject[] { headTailVO });

		CircularlyAccessibleValueObject[] headTailVOs = new CircularlyAccessibleValueObject[] { headTailVO };

		List<BillTabVO> tabvos = getHeadTabVO();
		for (BillTabVO tabvo : tabvos) {
			List<BillTempletBodyVO> bvos = getHeadBodyVOs(headTails, tabvo);
			if(bvos==null||bvos.size()==0)
				continue;
			getTabDataMap(headTailVOs,bvos.toArray(new BillTempletBodyVO[0]),IBillDataConst.HEAD, corp, userid, tabvo);
			
		}
	}

	private void putBodyData(String corp, String userid) {
		// 支持多子表
		if (billVO instanceof IExAggVO) {
			IExAggVO aggvo = (IExAggVO) billVO;
			List<BillTabVO> tabvos = getBodyTabVO();
			for (BillTabVO tabvo : tabvos) {
				String tablecode = tabvo.getBasetab() != null && tabvo.getBasetab().length() > 0 ? tabvo.getBasetab() : tabvo.getTabcode();
				
				//tabvo.getBasetab() != null && tabvo.getBasetab().length() > 0 ? tabvo.getBasetab() : tabvo.getTabcode();
				List<BillTempletBodyVO> tplvos = bodys.get(tablecode);
				if(tplvos==null)
					continue;
				Collections.sort(tplvos, BODYVO_COMPARATOR);// 排序
				
				tablecode = tabvo.getMetadatapath();
				if(tablecode==null)
					continue;
				CircularlyAccessibleValueObject[] bodyVOs = aggvo.getTableVO(tablecode);
				if (bodyVOs == null)
					bodyVOs = new CircularlyAccessibleValueObject[0];
				
//				dealFormular(tplvos, bodyVOs);
				getBodyTabDataMap(bodyVOs,tplvos.toArray(new BillTempletBodyVO[0]), IBillDataConst.BODY, corp, userid, tabvo);
				
			}
		} else {
			CircularlyAccessibleValueObject[] bodyVOs = billVO.getChildrenVO();
			if(bodyVOs==null)
				bodyVOs = new CircularlyAccessibleValueObject[0];
//			dealFormular(billTempletVO.getBodyVO(), bodyVOs);
			
			getTabDataMap(bodyVOs, billTempletVO.getBodyVO(),IBillDataConst.BODY, corp, userid, new BillTabVO());
			
		}
	}
	
	public void getTabDataMap(CircularlyAccessibleValueObject[] bodyVOs,
			BillTempletBodyVO[] itemVOs, int pos, String corp, String userid,
			BillTabVO tabvo) {
		
		
		PubBillTempletTModel btt = new PubBillTempletTModel();
		btt.setPos(IBillItem.HEAD);
		btt.setBaseTab(tabvo.getBasetab());
		btt.setTabCode(tabvo.getTabcode());
		btt.setTabIndex(tabvo.getTabindex());
		btt.setTabName(tabvo.getTabname());
		
		bttList.add(btt);
		
		for (int bvo = 0; bvo < bodyVOs.length; bvo++) {
			IBusinessEntity be = tabvo.getBillMetaDataBusinessEntity();
			if(be==null)
				continue;
			NCObject ncos = DASFacade.newInstanceWithContainedObject(be, bodyVOs[bvo]);
			
			for (int i = 0; i < itemVOs.length; i++) {
				if (itemVOs[i].getPos() != pos) {
					continue;
				}
				PubBillTempletBModel column = processor.process(corp, userid,itemVOs[i],ncos);
				btbList.add(column);
			}
			
		}
		
	}
	
	
	
	/**
	 * 20160815-zhangwxe
	 * 表体数据拼装和表体不一样
	 * @return
	 */
	public void getBodyTabDataMap(CircularlyAccessibleValueObject[] bodyVOs,
				BillTempletBodyVO[] itemVOs, int pos, String corp, String userid,
				BillTabVO tabvo) {
		
		PubBillTempletTModel btt = new PubBillTempletTModel();
		btt.setPos(IBillItem.HEAD);
		btt.setBaseTab(tabvo.getBasetab());
		btt.setTabCode(tabvo.getTabcode());
		btt.setTabIndex(tabvo.getTabindex());
		btt.setTabName(tabvo.getTabname());
		
		bttList.add(btt);
		
		for (int bvo = 0; bvo < bodyVOs.length; bvo++) {
			IBusinessEntity be = tabvo.getBillMetaDataBusinessEntity();
			if(be==null)
				continue;
			NCObject ncos = DASFacade.newInstanceWithContainedObject(be, bodyVOs);
			
			for (int i = 0; i < itemVOs.length; i++) {
				if (itemVOs[i].getPos() != pos) {
					continue;
				}
				
				PubBillTempletBModel column = processor.process(corp, userid,itemVOs[i],ncos);
				btbList.add(column);
				
			}
		}
		
	}
	

	private AbstractRefModel getModel(String refNodeName) {
		if (null != refNodeName) {
			refNodeName = refNodeName.substring(refNodeName.indexOf("<") + 1,
					refNodeName.indexOf(">"));
		}
		Class refModel = null;
		try {
			refModel = Class.forName(refNodeName);
			return (AbstractRefModel) refModel.newInstance();
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			return null;
		}

	}

	private BillTabVO getTabVO(String tablecode) {
		BillTabVO[] bvos = this.billTempletVO.getHeadVO().getStructvo()
				.getBillTabVOs();
		for (BillTabVO bvo : bvos) {
			if (tablecode != null && tablecode.length() > 0
					&& tablecode.equals(bvo.getTabcode())) {
				return bvo;
			}
		}
		return null;
	}

	private List<BillTabVO> getBodyTabVO() {
		List<BillTabVO> list = new ArrayList<BillTabVO>();
		BillTabVO[] bvos = this.billTempletVO.getHeadVO().getStructvo()
				.getBillTabVOs();
		for (BillTabVO bvo : bvos) {
			if (bvo.getPos() == IBillDataConst.BODY) {
				list.add(bvo);
			}
		}
		Collections.sort(list, TABVO_COMPARATOR);
		return list;
	}

	private List<BillTabVO> getHeadTabVO() {
		List<BillTabVO> list = new ArrayList<BillTabVO>();
		BillTabVO[] bvos = this.billTempletVO.getHeadVO().getStructvo()
				.getBillTabVOs();
		for (BillTabVO bvo : bvos) {
			if (bvo.getPos() == IBillDataConst.HEAD) {
				list.add(bvo);
			}
		}
		Collections.sort(list, TABVO_COMPARATOR);
		return list;
	}
	
	private BillTempletBodyVO getBodyVo(String itemKey,List<BillTempletBodyVO> bodyVos){
		for (BillTempletBodyVO vo : bodyVos) {
			if (vo.getItemkey().equals(itemKey)) {
				return vo;
			}
		}
		return null;
	}

	private List<BillTempletBodyVO> getHeadBodyVOs(
			List<BillTempletBodyVO> head, BillTabVO tabvo) {
		List<BillTempletBodyVO> list = new ArrayList<BillTempletBodyVO>();
		for (BillTempletBodyVO vo : head) {
			if (!vo.getShowflag().booleanValue())
				continue;
			if (vo.getTable_code().equals(tabvo.getTabcode())) {
				list.add(vo);
			}
		}
		Collections.sort(list, BODYVO_COMPARATOR);// 排序
		return list;
	}

	// 格式转换
	private Object parseValue(Object val) {
		if (val == null)
			return null;
		if (val instanceof UFDouble) {
			return ((UFDouble) val).getDouble();
		}
		if (val instanceof UFDate) {
			return sdf_ymd.format(((UFDate) val).toDate());
		}
		if (val instanceof UFDateTime) {
			return ((UFDateTime) val).toString();
		}
		return val;
	}

	public AggregatedValueObject getBillVO() {
		return billVO;
	}

	public void setBillVO(AggregatedValueObject billVO) {
		this.billVO = billVO;
	}

	public BillTempletVO getBillTempletVO() {
		return this.billTempletVO;
	}

}
