package nc.itf.zn.dingtalk;

public interface IDingTalk {
	
	/**
	 * 个人额度查询接口
	 * @param psncode 人员编码
	 * @return
	 */
	public String getPersonalAmount(String psncode);
	/**
	 * 我的申请查询接口
	 * @param psncode 人员编码
	 * @return
	 */
	public String getMyApplication(String psncode);
	/**
	 * 我的待办查询接口 
	 * @param psncode 人员编码
	 * @return
	 */
	public String getMyToDoList(String psncode,String condition);
	/**
	 * 我的erp待办查询接口
	 * @param psncode
	 * @param condition
	 * @return
	 */
	public String getMyErpToDoList(String psncode,String condition);
	/**
	 * 我的待办数查询接口
	 * @param psncode
	 * @return
	 */
	public String getMyToDoNum(String psncode);
	
	/**
	 * 查询单据明细
	 * @param 
	 * 
	 * @return
	 */
	public String getBillDetail(String pk_bill,String pk_billtypecode);
	
	/**
	 * 查询erp单据明细
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 */
	public String getErpBillDetail(String pk_bill,String pk_billtypecode);
	
	/**
	 * 审批接口
	 * @param json
	 * @return
	 */
	public String processApprove(String billtype,String billid,String checkResult,String checkman,String checkNote,String pk_flow,String workflow_type);
	
	/**
	 * 影像查看接口
	 * @param detail
	 * @param psncode
	 * @return
	 */
	public String showImage(String pk_bill,String pk_billtypecode,String cuserid);
	
	/**
	 * 附件查看接口
	 * @param pk_bill
	 * @return
	 */
	public String doQueryFile(String pk_bill);

	/**
	 * 查看审批流程信息
	 * @param billno
	 * @return
	 */
	public String getUnApprovalDetails(String billno);
	
	/**
	 * 根据单据类型查询单据类型
	 * @param billtype
	 * @return
	 */
	public String getBillTypeNameByBillType(String billtype);
	
	/**
	 * 查询发送人
	 * @param pk_user
	 * @return
	 */
	public String getSender(String pk_user);
	
	/**
	 * 查询发送人
	 * @param pk_user
	 * @return
	 */
	public String getSenderdate(String pk_taskid);
	
	/**
	 * 判断当前单据模板是否为移动模板
	 * @param billtype
	 * @return
	 */
	public String judgeIsOABill(String billtype);
	
	/**
	 * 获取审批历史信息
	 * @param billid
	 * @param billtype
	 * @return
	 */
	public String getApproveHistoryInfo(String billid, String billtype);
	
	/**
	 * 获取附件列表
	 * @param billid
	 * @param billtype
	 * @return
	 */
	public String getFileList(String billid, String billtype);
	
	
	/**
	 * 获取附件详情
	 * @param pk_attachment
	 * @return
	 */
	public String getFileContent(String pk_attachment);
}