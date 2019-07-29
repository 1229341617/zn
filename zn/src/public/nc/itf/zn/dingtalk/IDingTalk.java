package nc.itf.zn.dingtalk;

public interface IDingTalk {
	
	/**
	 * ���˶�Ȳ�ѯ�ӿ�
	 * @param psncode ��Ա����
	 * @return
	 */
	public String getPersonalAmount(String psncode);
	/**
	 * �ҵ������ѯ�ӿ�
	 * @param psncode ��Ա����
	 * @return
	 */
	public String getMyApplication(String psncode);
	/**
	 * �ҵĴ����ѯ�ӿ� 
	 * @param psncode ��Ա����
	 * @return
	 */
	public String getMyToDoList(String psncode,String condition);
	/**
	 * �ҵ�erp�����ѯ�ӿ�
	 * @param psncode
	 * @param condition
	 * @return
	 */
	public String getMyErpToDoList(String psncode,String condition);
	/**
	 * �ҵĴ�������ѯ�ӿ�
	 * @param psncode
	 * @return
	 */
	public String getMyToDoNum(String psncode);
	
	/**
	 * ��ѯ������ϸ
	 * @param 
	 * 
	 * @return
	 */
	public String getBillDetail(String pk_bill,String pk_billtypecode);
	
	/**
	 * ��ѯerp������ϸ
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 */
	public String getErpBillDetail(String pk_bill,String pk_billtypecode);
	
	/**
	 * �����ӿ�
	 * @param json
	 * @return
	 */
	public String processApprove(String billtype,String billid,String checkResult,String checkman,String checkNote,String pk_flow,String workflow_type);
	
	/**
	 * Ӱ��鿴�ӿ�
	 * @param detail
	 * @param psncode
	 * @return
	 */
	public String showImage(String pk_bill,String pk_billtypecode,String cuserid);
	
	/**
	 * �����鿴�ӿ�
	 * @param pk_bill
	 * @return
	 */
	public String doQueryFile(String pk_bill);

	/**
	 * �鿴����������Ϣ
	 * @param billno
	 * @return
	 */
	public String getUnApprovalDetails(String billno);
	
	/**
	 * ���ݵ������Ͳ�ѯ��������
	 * @param billtype
	 * @return
	 */
	public String getBillTypeNameByBillType(String billtype);
	
	/**
	 * ��ѯ������
	 * @param pk_user
	 * @return
	 */
	public String getSender(String pk_user);
	
	/**
	 * ��ѯ������
	 * @param pk_user
	 * @return
	 */
	public String getSenderdate(String pk_taskid);
	
	/**
	 * �жϵ�ǰ����ģ���Ƿ�Ϊ�ƶ�ģ��
	 * @param billtype
	 * @return
	 */
	public String judgeIsOABill(String billtype);
	
	/**
	 * ��ȡ������ʷ��Ϣ
	 * @param billid
	 * @param billtype
	 * @return
	 */
	public String getApproveHistoryInfo(String billid, String billtype);
	
	/**
	 * ��ȡ�����б�
	 * @param billid
	 * @param billtype
	 * @return
	 */
	public String getFileList(String billid, String billtype);
	
	
	/**
	 * ��ȡ��������
	 * @param pk_attachment
	 * @return
	 */
	public String getFileContent(String pk_attachment);
}