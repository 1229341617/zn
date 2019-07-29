package nc.bs.jzmobile.template.mobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nc.ui.pub.bill.IBillItem;
import nc.vo.jzmobile.template.MobileTemplateBVO;
import nc.vo.jzmobile.template.MobileTemplateVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVOUtil;
import nc.vo.pub.bill.BillTempletBodyVO;

/**
 * 该工具类 可处理 单据模板 或 自定义的移动模板 公式
 * @author wanghui
 *
 */
public class NCMobileFormulaFacade {

	public static void executeFormula(MobileTemplateVO templateVO,
			Object[] datas) {
		if (templateVO.getChildren() == null || datas == null || datas.length==0)
			return;
		if ("主子表".equals(templateVO.getBillpattern())) {
			executeFormula(templateVO.getChildren(),
					(AggregatedValueObject[]) datas);

		} else {
			CircularlyAccessibleValueObject[] parentvo = new CircularlyAccessibleValueObject[datas.length];
			for(int i=0;i<datas.length;i++){
				parentvo[i] = ((AggregatedValueObject)datas[i]).getParentVO();
			}
			executeFormula(templateVO.getChildren(),
					parentvo);
		}

	}

	public static void executeFormula(MobileTemplateBVO[] templateVOs,
			AggregatedValueObject[] datas) {
		if (templateVOs == null || datas == null)
			return;
		List<CircularlyAccessibleValueObject> headVOList = new ArrayList<CircularlyAccessibleValueObject>();
		List<CircularlyAccessibleValueObject> bodyVOList = new ArrayList<CircularlyAccessibleValueObject>();
		List<MobileTemplateBVO> headFormulaItemList = new ArrayList<MobileTemplateBVO>();
		List<MobileTemplateBVO> bodyFormulaItemList = new ArrayList<MobileTemplateBVO>();
		for (MobileTemplateBVO templateVO : templateVOs) {
			if (IBillItem.HEAD == templateVO.getPos()
					&& templateVO.getFormula() != null) {
				headFormulaItemList.add(templateVO);
			} else if (IBillItem.BODY == templateVO.getPos()
					&& templateVO.getFormula() != null) {
				bodyFormulaItemList.add(templateVO);
			}
		}
		for (AggregatedValueObject data : datas) {
			headVOList.add(data.getParentVO());
			if(null!=data.getChildrenVO() && data.getChildrenVO().length>0){
				bodyVOList.addAll(Arrays.asList(data.getChildrenVO()));
			}
			
		}
		if (headFormulaItemList.size() > 0) {
			executeFormula(headFormulaItemList
					.toArray(new MobileTemplateBVO[0]), headVOList
					.toArray(new CircularlyAccessibleValueObject[0]));
		}
		if (bodyFormulaItemList.size() > 0) {
			executeFormula(bodyFormulaItemList
					.toArray(new MobileTemplateBVO[0]), bodyVOList
					.toArray(new CircularlyAccessibleValueObject[0]));
		}

	}

	public static void executeFormula(MobileTemplateBVO[] templateVOs,
			CircularlyAccessibleValueObject[] datas) {
		if (templateVOs == null || datas == null)
			return;
		List<String> formulaList = new ArrayList<String>();
		for (MobileTemplateBVO templateVO : templateVOs) {
			String formula = templateVO.getFormula();
			if (formula!= null) {
				String[] formulas = formula.split(";");
				completeFormulas(templateVO.getItemkey(),formulas);
				for(String formula_single:formulas){
					formulaList.add(formula_single);
				}
			}
		}
		if (formulaList.size() == 0)
			return;
		SuperVOUtil.execFormulaWithVOs(datas, formulaList
				.toArray(new String[0]), null);

	}
	
	public static void executeFormula(BillTempletBodyVO[] templateVOs,
			CircularlyAccessibleValueObject[] datas) {
		if (templateVOs == null || datas == null)
			return;
		List<String> formulaList = new ArrayList<String>();
		for (BillTempletBodyVO templateVO : templateVOs) {
			String formula = templateVO.getLoadformula();
//			if("pk_project".equals(templateVO.getItemkey())){
//				formula = "pk_project->getColValue(bd_project,project_name,pk_project,pk_project)";
//			}
			if (formula!= null) {
				String[] formulas = formula.split(";");
				completeFormulas(templateVO.getItemkey(),formulas);
				for(String formula_single:formulas){
					formulaList.add(formula_single);
				}
			}
		}
		if (formulaList.size() == 0)
			return;
//		SuperVOUtil.execFormulaWithVOs(datas, formulaList.toArray(new String[0]), null);
		for(String formula:formulaList){
			SuperVOUtil.execFormulaWithVOs(datas, new String[]{formula}, null);
		}

	}
	
	private static void completeFormulas(String key, String[] formulas) {
		if (formulas != null) {
			for (int i = 0; i < formulas.length; i++) {
				if (formulas[i].indexOf("->") < 0) {
					formulas[i] = key + "->" + formulas[i];
				}
			}
		}
	}
}
