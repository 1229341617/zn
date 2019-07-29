package nc.vo.pub.jzpm.budgetctrl;

import nc.vo.pub.SuperVO;

/**
 * 预算控制前后台标志类
 * @author liuhm
 *
 */
public class BudgetCtrlUserObject extends SuperVO {
	private static final long serialVersionUID = 5373439547397182094L;

	private boolean isReDoAction = false;
	
	private Integer ctrlMode = -1;

	public boolean isReDoAction() {
		return isReDoAction;
	}

	public void setReDoAction(boolean isReDoAction) {
		this.isReDoAction = isReDoAction;
	}

	public Integer getCtrlMode() {
		return ctrlMode;
	}

	public void setCtrlMode(Integer ctrlMode) {
		this.ctrlMode = ctrlMode;
	}
}
