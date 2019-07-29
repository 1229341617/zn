package nc.vo.pub.jzpm.budgetctrl;

import nc.vo.pub.BusinessException;

public class BudgetCtrlException  extends BusinessException{
	private static final long serialVersionUID = 1L;
	private  String  ctrlmessage;
	 
	private  Integer alarmtype ;
	 public Integer getAlarmtype() {
		return alarmtype;
	}

	public void setAlarmtype(Integer alarmtype) {
		this.alarmtype = alarmtype;
	}

	public String getCtrlmessage() {
		return ctrlmessage;
	}

	public  BudgetCtrlException(String ctrlmessage){
		 this.ctrlmessage   = ctrlmessage;
	 }
	
	public BudgetCtrlException(String ctrlmessage,Integer alarmtype){
		this.ctrlmessage = ctrlmessage;
		this.alarmtype = alarmtype;
	}
}
