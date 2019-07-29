package nc.impl.zn.dingtalk;

import nc.bs.logging.Logger;
import nc.itf.zn.dingtalk.IDingTalk;
import nc.jzmobile.utils.MobileMessageUtil;

import org.codehaus.jettison.json.JSONObject;

public class DingTalkImpl implements IDingTalk {

	/**
	 * 个人额度查询
	 */
	@Override
	public String getPersonalAmount(String psncode) {
		
		DingTalkDMO dmo = new DingTalkDMO();
		try {			
			
					
			return returnJson(dmo.getPersonalAmount(psncode),"1") ;
			
		} catch (Exception e) {
			e.printStackTrace();
			return returnJson(e.getMessage(),"0") ;
		}
				
		
	}
	
	@Override
	public String getMyApplication(String psncode) {
		DingTalkDMO dmo = new DingTalkDMO();
		try {			
			return returnJson(dmo.getMyApplication(psncode),"1") ;
			
		} catch (Exception e) {
			e.printStackTrace();
			return returnJson(e.getMessage(),"0") ;
		}
	}
	
	@Override
	public String getMyToDoList(String psncode,String condition) {
		DingTalkDMO dmo = new DingTalkDMO();
		try {
			if(dmo.getMyToDoList(psncode,condition).equals("")) {
				return returnJson("[]", "1");
			}
			return returnJson(dmo.getMyToDoList(psncode,condition),"1") ;
			
		} catch (Exception e) {
			e.printStackTrace();
			return returnJson(e.getMessage(),"0") ;
		}
	}
	
	@Override
	public String getMyErpToDoList(String psncode,String condition) {
		DingTalkDMO dmo = new DingTalkDMO();
		try {
			if(dmo.getMyErpToDoList(psncode,condition).equals("")) {
				return returnJson("[]", "1");
			}
			return returnJson(dmo.getMyErpToDoList(psncode,condition),"1") ;
			
		} catch (Exception e) {
			e.printStackTrace();
			return returnJson(e.getMessage(),"0") ;
		}
	}
	
	@Override
	public String getMyToDoNum(String psncode) {
		DingTalkDMO dmo = new DingTalkDMO();
		try {			
			return returnJson(dmo.getMyToDoNum(psncode),"1") ;
			
		} catch (Exception e) {
			e.printStackTrace();
			return returnJson(e.getMessage(),"0") ;
		}
	}
	
	@Override
	public String getBillDetail(String pk_bill,String pk_billtypecode) {
		
		DingTalkDMO dmo = new DingTalkDMO();
		try {			
			return returnJson(dmo.getBillDetail(pk_bill,pk_billtypecode),"1") ;
			
		} catch (Exception e) {
			e.printStackTrace();
			return returnJson(e.getMessage(),"0") ;
		}
	}
	
	@Override
	public String getErpBillDetail(String pk_bill,String pk_billtypecode) {
		
		DingTalkDMO dmo = new DingTalkDMO();
		try {			
			return returnJson(dmo.getErpBillDetail(pk_bill,pk_billtypecode),"1") ;
			
		} catch (Exception e) {
			e.printStackTrace();
			return returnJson(e.getMessage(),"0") ;
		}
	}
	
	@Override
	public String processApprove(String billtype,String billid,String checkResult,String checkman,String checkNote,String pk_flow,String workflow_type) {
		DingTalkDMO dmo = new DingTalkDMO();
		try {			
			return returnJson(dmo.processApprove(billtype,billid,checkResult,checkman,checkNote,pk_flow,workflow_type),"1") ;
			
		} catch (Exception e) {
			e.printStackTrace();
			return returnJson(e.getMessage(),"0") ;
		}
	}
	
	@Override
	public String showImage(String pk_bill,String pk_billtypecode,String cuserid) {
		DingTalkDMO dmo = new DingTalkDMO();
		try {			
			return returnJson(dmo.showImage(pk_bill,pk_billtypecode, cuserid),"1") ;
			
		} catch (Exception e) {
			e.printStackTrace();
			return returnJson(e.getMessage(),"0") ;
		}
	}
	
	@Override
	public String doQueryFile(String pk_bill) {
		
		DingTalkDMO dmo = new DingTalkDMO();
		try {			
			return returnJson(dmo.doQueryFile(pk_bill),"1") ;
			
		} catch (Exception e) {
			e.printStackTrace();
			return returnJson(e.getMessage(),"0") ;
		}

	}
	
	@Override
	public String getUnApprovalDetails(String billno) {
		try {
			return returnJson(new DingTalkDMO().getUnApprovalDetails(billno), "1");
		} catch (Exception e) {
			e.printStackTrace();
			return returnJson(e.getMessage(), "0");
		}
	}
	
	@Override
	public String getBillTypeNameByBillType(String billtype) {
		try {
			return returnJson(new DingTalkDMO().getBillTypeNameByBillType(billtype), "1");
		} catch (Exception e) {
			e.printStackTrace();
			return returnJson(e.getMessage(), "0");
		}
	}
	
	@Override
	public String getSender(String pk_user) {
		try {
			return returnJson(new DingTalkDMO().getSender(pk_user), "1");
		} catch (Exception e) {
			e.printStackTrace();			return returnJson(e.getMessage(), "0");
		}
	}
	
	@Override
	public String getSenderdate(String pk_taskid) {
		try {
			Object senderdateobj = new DingTalkDMO().getSenderdate(pk_taskid);
			if(senderdateobj == null) {
				return returnJson(senderdateobj, "0");
			}
			return returnJson(senderdateobj, "1");
		} catch (Exception e) {
			e.printStackTrace();
			return returnJson(e.getMessage(), "0");
		}
	}
	
	
	public String judgeIsOABill(String billtype)  {
		try {
			return returnJson(MobileMessageUtil.judgeIsOABill(billtype), "1");
		} catch (Exception e) {
			e.printStackTrace();
			return returnJson(e.getMessage(), "0");
		}
	}
	
	
	@Override
	public String getApproveHistoryInfo(String billid, String billtype) {
		try {
			return returnJson(new DingTalkDMO().getApproveHistoryInfo(billid, billtype), "1");
		} catch (Exception e) {
			e.printStackTrace();
			return returnJson(e.getMessage(), "0");
		}
	}
	
	public String getFileList(String billid, String billtype) {
		try {
			return returnJson(new DingTalkDMO().getFileList(billid, billtype), "1");
		} catch (Exception e) {
			Logger.info("获取附件列表失败！！！");
			return returnJson(e.getMessage(), "0");
		}
	}


	public String getFileContent(String pk_attachment) {
		try {
			return returnJson(new DingTalkDMO().getFileContent(pk_attachment), "1");
		} catch (Exception e) {
			Logger.info("获取附件详情失败！！！");
			return returnJson(e.getMessage(), "0");
		}
	}
	
	
	
	private String returnJson(Object msg,String status){
		JSONObject jsonObj = new JSONObject();	
		try {
			jsonObj.put("status", status);
			jsonObj.put("msg", msg);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jsonObj.toString();
	}
}
