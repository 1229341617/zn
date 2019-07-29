package nc.itf.uap.pf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import nc.vo.pub.BusinessException;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.workflownote.WorkflownoteAttVO;
import nc.vo.pub.workflownote.WorkflownoteVO;
import nc.vo.pub.workflowqry.FlowAdminVO;
import nc.vo.pub.workflowqry.FlowHistoryQryResult;
import nc.vo.pub.workflowqry.WorkflowQueryResult;
import nc.vo.sm.UserVO;
import nc.vo.wfengine.pub.WFTask;
import nc.vo.workflow.admin.FlowInstanceHistoryVO;

/**
 * ��������ѯ�ӿ�
 * 
 * @author �׾� 2005-8-17
 * @modifier leijun 2009-7
 */
public interface IPFWorkflowQry {

	final int WF_INSTANCE_QRY = 1;
	final int WF_NOTE_QRY = 2;
	final int CUSTOM_WF_INSTANCE_QRY = 4;

	/**
	 * �ж�ĳ����Ա�Ƿ��ǵ�ǰ���ݵ������
	 * 
	 * @param billId
	 *            ����ID
	 *            
	 * @param billType
	 *            ��������PK
	 * @param userId
	 *            ����ԱPK
	 * @return boolean �Ƿ��ǵ�ǰ���ݵ������
	 * @throws BusinessException
	 */
	public boolean isCheckman(String billId, String billType, String userId)
			throws BusinessException;

	/**
	 * ��ѯ����ID�����и�ĳ�û���˵�ID���� <li>���뱣֤����ID�����еĵ��ݶ���ͬһ���������͵�
	 * 
	 * @param billIdAry
	 *            ����ID����
	 * @param billType
	 *            ��������PK
	 * @param userId
	 *            ����ԱPK
	 * @return �������ĵ���ID����
	 * @throws BusinessException
	 * 
	 * 
	 */
	public abstract FlowHistoryQryResult queryFlowHistoryQryResult(String paramString1, String paramString2, int paramInt)
		    throws BusinessException;
	public String[] isCheckmanAry(String[] billIdAry, String billType,
			String userId) throws BusinessException;

	/**
	 * @deprecated
	 * �ж�ĳ�û���ĳ�����ݻ��������Ƿ��п����������̶���
	 * 
	 * @param billOrTranstype
	 *            ���ݻ�������
	 * @param pkOrg
	 *            ��֯PK
	 * @param operator
	 *            �û�PK
	 * @param iWorkflowOrApproveflow
	 *            ���������ͣ���<code>WorkflowTypeEnum.Approveflow</code>��
	 *            <code>WorkflowTypeEnum.Workflow</code>
	 * @return boolean
	 * @throws BusinessException
	 */
	public boolean isExistWorkflowDefinition(String billOrTranstype,
			String pkOrg, String operator, int iWorkflowOrApproveflow)
			throws BusinessException;
	
	/**
	 *
	 * �ж�ĳ�û���ĳ�����ݻ��������Ƿ��п����������̶���
	 * 
	 * @param billOrTranstype
	 *            ���ݻ�������
	 * @param pkOrg
	 *            ��֯PK
	 * @param operator
	 *            �û�PK
	 * @param emendEnum 
	 *            �޶�������ö��
	 * @param iWorkflowOrApproveflow
	 *            ���������ͣ���<code>WorkflowTypeEnum.Approveflow</code>��
	 *            <code>WorkflowTypeEnum.Workflow</code>
	 * @return  boolean
	 * @throws BusinessException
	 */
	public boolean isExistWorkflowDefinitionWithEmend(String billOrTranstype,
			String pkOrg, String operator, int emendEnum,int iWorkflowOrApproveflow)
			throws BusinessException;

	/**
	 * ��ѯһ�ŵ��ݵĵ�ǰ������������״̬
	 * 
	 * @param billOrTranstype
	 *            ��������PK
	 * @param billId
	 *            ����ID
	 * @param iWorkflowOrApproveflow
	 *            ���������ͣ���<code>WorkflowTypeEnum.Approveflow</code>��
	 *            <code>WorkflowTypeEnum.Workflow</code>
	 * @return ����ֵΪ<code>IWorkFlowStatus</code>�еĳ���
	 * @throws BusinessException
	 */
	public int queryFlowStatus(String billOrTranstype, String billId,
			int iWorkflowOrApproveflow) throws BusinessException;

	/**
	 * ����SQL����ѯ��Ӧ������,ʹ��Vector���� <li>�������������ѯ(ֻ����ǰ5000����)
	 * 
	 * @param strSQL
	 *            String �����SQL���
	 * @param intCols
	 *            int �����صĲ�ѯ�ֶθ���
	 * @param FieldType
	 *            int[] �����صĲ�ѯ�ֶ�����,����ΪIDapType�еĳ���
	 *            
	 * @deprecated
	 */
	public Vector queryDataBySQL(String strSQL, int intCols, int[] FieldType)
			throws BusinessException;

	/**
	 * ����SQL����ѯ��Ӧ�����ݣ����Ҹ��ݲ�ѯ���ͶԽ������������ ��Ҫ�������̹������ĺ��ҵ����������в�ѯ�����չ��
	 * 
	 * @param strSQL
	 * @param intCols
	 * @param fieldType
	 * @param qryType
	 * @deprecated
	 * @return Vector
	 * @throws BusinessException
	 */
	public Vector queryAndRefactorDataBySql(String strSQL, int intCols,
			int[] fieldType, int qryType) throws BusinessException;

	/**
	 * ����sql���ָ����������������ʵ����pk ����"�ҵ���������"/"���̹�������"�еķ�ҳ����
	 * 
	 * @param sql
	 *            ��ȡָ��where������pub_wf_instance�е�pk_wf_instance��sql���
	 * @return String[]
	 * @deprecated
	 * @throws BusinessException
	 */
	public String[] queryWFPKsBySql(String sql) throws BusinessException;

	/**
	 * ��ѯĳ���ݵ��������������Ĺ�����
	 * 
	 * @param billId
	 *            ����ID
	 * @param billOrTranstype
	 *            ���ݻ�������
	 * @param iWorkflowOrApproveflow
	 *            ���������ͣ���<code>WorkflowTypeEnum.Approveflow</code>��
	 *            <code>WorkflowTypeEnum.Workflow</code>
	 * @param allOrFinished
	 *            0-��ȡ���У�1-��ȡ�Ѵ����
	 * @return WorkflownoteVO[]
	 * @throws BusinessException
	 */
	public WorkflownoteVO[] queryWorkitems(String billId,
			String billOrTranstype, int iWorkflowOrApproveflow,
			int allOrFinished) throws BusinessException;

	/**
	 * ������ѯ������ĸ���(����ָ�û�����ʱ�ϴ��ĸ���)
	 * 
	 * @param pk_checkflows
	 * @return WorkflownoteAttVO[] classified by pk_checkflow
	 * @throws BusinessException
	 */
	public Map<String, List<WorkflownoteAttVO>> queryWorkitemAttBatch(String[] pk_checkflows) throws BusinessException;

	
	/**
	 * ��ѯĳ���ݵ��������������Ĺ��������������Ϣ
	 * @since 6.3 queryWorkitems��queryWorkitemAttBatch�ĺϲ�������Զ�̵��ô���
	 * @param billId
	 *            ����ID
	 * @param billOrTranstype
	 *            ���ݻ�������
	 * @param iWorkflowOrApproveflow
	 *            ���������ͣ���<code>WorkflowTypeEnum.Approveflow</code>��
	 *            <code>WorkflowTypeEnum.Workflow</code>
	 * @param allOrFinished
	 *            0-��ȡ���У�1-��ȡ�Ѵ����
	 * @return WorkflownoteVO[]
	 * @throws BusinessException
	 */
	public WorkflownoteVO[] queryWorkitemsWithAttach(String billId,
			String billOrTranstype, int iWorkflowOrApproveflow,
			int allOrFinished) throws BusinessException;
	/**
	 * ��ѯĳ�˵Ĺ�����
	 * 
	 * @param userid
	 * @param iWorkflowOrApproveflow
	 *            ���������ͣ�0��ʾ�����ˣ���<code>WorkflowTypeEnum.Approveflow</code>��
	 *            <code>WorkflowTypeEnum.Workflow</code>
	 * @param allOrFinished
	 *            0-��ȡ���У�1-��ȡδ����ģ�2-��ȡ�Ѵ����
	 * @return WorkflownoteVO[]
	 * @throws BusinessException
	 */
	public WorkflownoteVO[] queryWorkitemsByUser(String userid,
			int iWorkflowOrApproveflow, int allOrFinished)
			throws BusinessException;

	/**
	 * ���ĳ���ݵ�������Ϣ��ӡ����Դ
	 * 
	 * @param billId
	 *            ����ID
	 * @param billType
	 *            ���ݻ�������
	 * @return Object ��ӡ����Դ
	 * @throws BusinessException
	 */
	public Object getApproveWorkitemPrintDs(String billId, String billType)
			throws BusinessException;
	/**
	 * ���ĳ���ݵ�������Ϣ��ӡ����Դ
	 * 
	 * @param billId
	 *            ����ID
	 * @param billType
	 *            ���ݻ�������
	 * @param isAllorValidity true��ʾ���У�false��ʾ��Ч�Ĺ�����
	 * @return Object ĳ���ݵ�������Ϣ��ӡ����Դ
	 * @throws BusinessException
	 */
	public Object getApproveWorkitemPrintDs(String billId, String billType,boolean isAllorValidity)
	        throws BusinessException;

	/**
	 * �жϸõ����Ƿ����������ʵ�� <li>������Ч״̬�ģ������������״̬
	 * 
	 * @param billId
	 * @param billType
	 * @return boolean  ����ʵ���Ƿ���Ч
	 * @throws BusinessException
	 */
	public boolean isApproveFlowStartup(String billId, String billType)
			throws BusinessException;

	/**
	 * �жϸõ����Ƿ���ڹ�������ʵ�� <li>������Ч״̬�ģ������������״̬ ӦzhangzhijҪ����� 2011.07.26
	 * 
	 * @param billId
	 * @param billType
	 * @return boolean ������ʵ���Ƿ���Ч״̬
	 * @throws BusinessException
	 */
	public boolean isWorkFlowStartup(String billId, String billType)
			throws BusinessException;

	/**
	 * ��ѯĳ���������е����̶����У�ĳ����������Ĳ�����ʵ��ֵ <li>��������Ч������ʵ���������С��ѽ�����
	 * 
	 * @param billId
	 *            ��������
	 * @param billType
	 *            ���ݻ�������
	 * @param varCode
	 *            ��������
	 * @param iWorkflowOrApproveflow
	 *            ���������ͣ���<code>WorkflowTypeEnum.Approveflow</code>��
	 *            <code>WorkflowTypeEnum.Workflow</code>
	 * @return �ò�����ʵ��ֵ����
	 * @throws BusinessException
	 * 
	 *             XXX:leijun+2010-3
	 */
	public String[] queryRealValuesOfGadgetParam(String billId,
			String billType, String varCode, int iWorkflowOrApproveflow)
			throws BusinessException;

	/**
	 * ����������ʱ��ʹ�ù�������
	 * ����㹤��������ҪԶ�̵��ã�����ṩ�˷�������������������ʱʱ��Զ�̵��ô���
	 * <p>
	 * pks[]��beginTimes[]��endTimes[]��lengh��Ҫ��� �������BusinessException�쳣
	 * ��pks[]ĳ��Ϊnull����ô��ʹ����Ȼʱ����������ʱ
	 * ������beginTimes��endTimes����nullֵ����ô������ʱ�������ᱻ��Ϊ""
	 * @param pks
	 * @param beginTimes
	 * @param endTimes
	 * @return String[] ��ʱ
	 * @throws BusinessException
	 */
	public String[] getElapsedTimeInWorkCalendarBatch(String[] pks,
			UFDateTime[] beginTimes, UFDateTime[] endTimes)
			throws BusinessException;
	
	
	/**
	 * �������AbstractWorkflowQuery��ѯ����ʵ����������
	 * 
	 * @param sql
	 * @param refactorClass
	 * @return List<WorkflowQueryResult> 
	 * @throws BusinessException
	 */
	public List<WorkflowQueryResult> queryFieldValues(String sql, String refactorClass) 
			throws BusinessException;
	
	
	/**
	 * ��������ʵ��pk��ѯ�������ʷ
	 * @param pk_wf_instance
	 * @return FlowInstanceHistoryVO[] ��������ʵ��pk��ѯ�������ʷ
	 * @throws BusinessException
	 */
	public FlowInstanceHistoryVO[] queryFlowInstanceHistory(String pk_wf_instance) throws BusinessException;
	
	/**
	 * ����task��pk��ѯtask
	 * @param pk_wf_task
	 * @return WFTask 
	 * @throws BusinessException
	 */
	public WFTask queryWFTaskByPk(String pk_wf_task) throws BusinessException;
	
	/**
	 * 
	 * @param billtype ��������
	 * @param billId   ����Id
	 * @param flowType ��������
	 * @return FlowAdminVO ����������������Ϣ�Լ����ڹ������graphͼ�ν�����Ϣ
	 * @throws BusinessException
	 * */
	public FlowAdminVO queryWorkitemForAdmin(String billtype, String billId, int flowType) throws BusinessException;
	
	/**
	 * 
	 * @param billOrTranstype	�������ͻ�������
	 * @param pkOrg				��֯
	 * @param operator			�û�
	 * @param emendEnum			�޶����������͡�����������Ͳ�֧���޶�����������ô��-1����
	 * @param iWorkflowOrApproveflow	���������������μ�WorkflowTypeEnum
	 * @return String 
	 * @throws BusinessException
	 */
	public String getStartWorkflowDef(String billOrTranstype, String pkOrg, String operator, int emendEnum, int iWorkflowOrApproveflow)  throws BusinessException;
	
	
	/**
	 * @since 6.3
	 * ����ʱ����Ϊ������ȥ��̨ȥworkflownote������޷��ж��Ƿ���ҪCA��
	 * ���Ӹýӿڷ�����������ʱ����ã������ж�����ʱ���Ƿ���ҪCA.
	 * @param userPK �û�PK
	 * @param billorTranstype �������ͻ��߽�����������
	 * @param billIds         �ۺ�VID����
	 * */
	public boolean isNeedCASign4Batch(String userPK,String[] billorTranstype,String[] billIds)throws BusinessException;
	public abstract boolean isApproveFlowInstance(String paramString1, String paramString2)
		    throws BusinessException;
	public abstract List<String> queryFlowApprovers(String paramString1, String paramString2)
		    throws BusinessException;
	 public abstract HashMap<String, ArrayList<UserVO>> queryWfProcessForSupervisors(HashSet<String> paramHashSet)
			    throws BusinessException;
	/**
	 * @since 6.3
	 * @param PfParameterVO ��������ʱ����
	 * ����ʱ��ʹ�ã�������һ���ڵ������Ϣ������������ʱ�䣬��������
	 * 
	 * */
	public String[] findLastPostTacheInfo(PfParameterVO paraVo) throws BusinessException;
	
	/**
	 * @since 6.3
	 * �ṩ��Эͬ
	 * */
	public Map<String,UserVO> findBillMakersByBillid(String[] billids) throws BusinessException;

	public WorkflownoteVO[] queryAllCheckInfo(String billid, String billtype) throws BusinessException;
	public abstract List<UserVO> queryWfProcess(String paramString1, String paramString2)
		    throws BusinessException;
	
	
}
