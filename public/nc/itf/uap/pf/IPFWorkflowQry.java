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
 * 工作流查询接口
 * 
 * @author 雷军 2005-8-17
 * @modifier leijun 2009-7
 */
public interface IPFWorkflowQry {

	final int WF_INSTANCE_QRY = 1;
	final int WF_NOTE_QRY = 2;
	final int CUSTOM_WF_INSTANCE_QRY = 4;

	/**
	 * 判断某操作员是否是当前单据的审核人
	 * 
	 * @param billId
	 *            单据ID
	 *            
	 * @param billType
	 *            单据类型PK
	 * @param userId
	 *            操作员PK
	 * @return boolean 是否是当前单据的审核人
	 * @throws BusinessException
	 */
	public boolean isCheckman(String billId, String billType, String userId)
			throws BusinessException;

	/**
	 * 查询单据ID数组中该某用户审核的ID数组 <li>必须保证单据ID数组中的单据都是同一个单据类型的
	 * 
	 * @param billIdAry
	 *            单据ID数组
	 * @param billType
	 *            单据类型PK
	 * @param userId
	 *            操作员PK
	 * @return 待审批的单据ID数组
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
	 * 判断某用户对某个单据或交易类型是否有可启动的流程定义
	 * 
	 * @param billOrTranstype
	 *            单据或交易类型
	 * @param pkOrg
	 *            组织PK
	 * @param operator
	 *            用户PK
	 * @param iWorkflowOrApproveflow
	 *            主流程类型，见<code>WorkflowTypeEnum.Approveflow</code>和
	 *            <code>WorkflowTypeEnum.Workflow</code>
	 * @return boolean
	 * @throws BusinessException
	 */
	public boolean isExistWorkflowDefinition(String billOrTranstype,
			String pkOrg, String operator, int iWorkflowOrApproveflow)
			throws BusinessException;
	
	/**
	 *
	 * 判断某用户对某个单据或交易类型是否有可启动的流程定义
	 * 
	 * @param billOrTranstype
	 *            单据或交易类型
	 * @param pkOrg
	 *            组织PK
	 * @param operator
	 *            用户PK
	 * @param emendEnum 
	 *            修订审批流枚举
	 * @param iWorkflowOrApproveflow
	 *            主流程类型，见<code>WorkflowTypeEnum.Approveflow</code>和
	 *            <code>WorkflowTypeEnum.Workflow</code>
	 * @return  boolean
	 * @throws BusinessException
	 */
	public boolean isExistWorkflowDefinitionWithEmend(String billOrTranstype,
			String pkOrg, String operator, int emendEnum,int iWorkflowOrApproveflow)
			throws BusinessException;

	/**
	 * 查询一张单据的当前审批流或工作流状态
	 * 
	 * @param billOrTranstype
	 *            单据类型PK
	 * @param billId
	 *            单据ID
	 * @param iWorkflowOrApproveflow
	 *            主流程类型，见<code>WorkflowTypeEnum.Approveflow</code>和
	 *            <code>WorkflowTypeEnum.Workflow</code>
	 * @return 返回值为<code>IWorkFlowStatus</code>中的常量
	 * @throws BusinessException
	 */
	public int queryFlowStatus(String billOrTranstype, String billId,
			int iWorkflowOrApproveflow) throws BusinessException;

	/**
	 * 根据SQL语句查询对应的数据,使用Vector返回 <li>处理大数据量查询(只返回前5000行数)
	 * 
	 * @param strSQL
	 *            String 传入的SQL语句
	 * @param intCols
	 *            int 被返回的查询字段个数
	 * @param FieldType
	 *            int[] 被返回的查询字段类型,必须为IDapType中的常量
	 *            
	 * @deprecated
	 */
	public Vector queryDataBySQL(String strSQL, int intCols, int[] FieldType)
			throws BusinessException;

	/**
	 * 根据SQL语句查询对应的数据，并且根据查询类型对结果集进行修正 主要用于流程管理中心和我的流程中心中查询结果的展现
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
	 * 根据sql获得指定条件下所有流程实例的pk 用于"我的流程中心"/"流程管理中心"中的分页功能
	 * 
	 * @param sql
	 *            获取指定where条件下pub_wf_instance中的pk_wf_instance的sql语句
	 * @return String[]
	 * @deprecated
	 * @throws BusinessException
	 */
	public String[] queryWFPKsBySql(String sql) throws BusinessException;

	/**
	 * 查询某单据的审批流或工作流的工作项
	 * 
	 * @param billId
	 *            单据ID
	 * @param billOrTranstype
	 *            单据或交易类型
	 * @param iWorkflowOrApproveflow
	 *            主流程类型，见<code>WorkflowTypeEnum.Approveflow</code>和
	 *            <code>WorkflowTypeEnum.Workflow</code>
	 * @param allOrFinished
	 *            0-获取所有；1-获取已处理的
	 * @return WorkflownoteVO[]
	 * @throws BusinessException
	 */
	public WorkflownoteVO[] queryWorkitems(String billId,
			String billOrTranstype, int iWorkflowOrApproveflow,
			int allOrFinished) throws BusinessException;

	/**
	 * 批量查询工作项的附件(附件指用户审批时上传的附件)
	 * 
	 * @param pk_checkflows
	 * @return WorkflownoteAttVO[] classified by pk_checkflow
	 * @throws BusinessException
	 */
	public Map<String, List<WorkflownoteAttVO>> queryWorkitemAttBatch(String[] pk_checkflows) throws BusinessException;

	
	/**
	 * 查询某单据的审批流或工作流的工作项，包含附件信息
	 * @since 6.3 queryWorkitems和queryWorkitemAttBatch的合并，减少远程调用次数
	 * @param billId
	 *            单据ID
	 * @param billOrTranstype
	 *            单据或交易类型
	 * @param iWorkflowOrApproveflow
	 *            主流程类型，见<code>WorkflowTypeEnum.Approveflow</code>和
	 *            <code>WorkflowTypeEnum.Workflow</code>
	 * @param allOrFinished
	 *            0-获取所有；1-获取已处理的
	 * @return WorkflownoteVO[]
	 * @throws BusinessException
	 */
	public WorkflownoteVO[] queryWorkitemsWithAttach(String billId,
			String billOrTranstype, int iWorkflowOrApproveflow,
			int allOrFinished) throws BusinessException;
	/**
	 * 查询某人的工作项
	 * 
	 * @param userid
	 * @param iWorkflowOrApproveflow
	 *            主流程类型，0表示不过滤，见<code>WorkflowTypeEnum.Approveflow</code>和
	 *            <code>WorkflowTypeEnum.Workflow</code>
	 * @param allOrFinished
	 *            0-获取所有；1-获取未处理的；2-获取已处理的
	 * @return WorkflownoteVO[]
	 * @throws BusinessException
	 */
	public WorkflownoteVO[] queryWorkitemsByUser(String userid,
			int iWorkflowOrApproveflow, int allOrFinished)
			throws BusinessException;

	/**
	 * 获得某单据的审批信息打印数据源
	 * 
	 * @param billId
	 *            单据ID
	 * @param billType
	 *            单据或交易类型
	 * @return Object 打印数据源
	 * @throws BusinessException
	 */
	public Object getApproveWorkitemPrintDs(String billId, String billType)
			throws BusinessException;
	/**
	 * 获得某单据的审批信息打印数据源
	 * 
	 * @param billId
	 *            单据ID
	 * @param billType
	 *            单据或交易类型
	 * @param isAllorValidity true表示所有，false表示有效的工作项
	 * @return Object 某单据的审批信息打印数据源
	 * @throws BusinessException
	 */
	public Object getApproveWorkitemPrintDs(String billId, String billType,boolean isAllorValidity)
	        throws BusinessException;

	/**
	 * 判断该单据是否存在审批流实例 <li>处于有效状态的，即启动或完成状态
	 * 
	 * @param billId
	 * @param billType
	 * @return boolean  流程实例是否有效
	 * @throws BusinessException
	 */
	public boolean isApproveFlowStartup(String billId, String billType)
			throws BusinessException;

	/**
	 * 判断该单据是否存在工作流流实例 <li>处于有效状态的，即启动或完成状态 应zhangzhij要求添加 2011.07.26
	 * 
	 * @param billId
	 * @param billType
	 * @return boolean 工作流实例是否有效状态
	 * @throws BusinessException
	 */
	public boolean isWorkFlowStartup(String billId, String billType)
			throws BusinessException;

	/**
	 * 查询某单据所运行的流程定义中，某个单据组件的参数的实参值 <li>必须是有效的流程实例（运行中、已结束）
	 * 
	 * @param billId
	 *            单据主键
	 * @param billType
	 *            单据或交易类型
	 * @param varCode
	 *            参数编码
	 * @param iWorkflowOrApproveflow
	 *            主流程类型，见<code>WorkflowTypeEnum.Approveflow</code>和
	 *            <code>WorkflowTypeEnum.Workflow</code>
	 * @return 该参数的实参值数组
	 * @throws BusinessException
	 * 
	 *             XXX:leijun+2010-3
	 */
	public String[] queryRealValuesOfGadgetParam(String billId,
			String billType, String varCode, int iWorkflowOrApproveflow)
			throws BusinessException;

	/**
	 * 批量计算历时，使用工作日历
	 * 因计算工作日历需要远程调用，因此提供此方法，减少批量计算历时时的远程调用次数
	 * <p>
	 * pks[]、beginTimes[]、endTimes[]的lengh需要相等 否则会抛BusinessException异常
	 * 若pks[]某行为null，那么会使用自然时间计算该行历时
	 * 若该行beginTimes或endTimes中有null值，那么该行历时计算结果会被设为""
	 * @param pks
	 * @param beginTimes
	 * @param endTimes
	 * @return String[] 历时
	 * @throws BusinessException
	 */
	public String[] getElapsedTimeInWorkCalendarBatch(String[] pks,
			UFDateTime[] beginTimes, UFDateTime[] endTimes)
			throws BusinessException;
	
	
	/**
	 * 用于配合AbstractWorkflowQuery查询流程实例、工作项
	 * 
	 * @param sql
	 * @param refactorClass
	 * @return List<WorkflowQueryResult> 
	 * @throws BusinessException
	 */
	public List<WorkflowQueryResult> queryFieldValues(String sql, String refactorClass) 
			throws BusinessException;
	
	
	/**
	 * 根据流程实例pk查询其操作历史
	 * @param pk_wf_instance
	 * @return FlowInstanceHistoryVO[] 根据流程实例pk查询其操作历史
	 * @throws BusinessException
	 */
	public FlowInstanceHistoryVO[] queryFlowInstanceHistory(String pk_wf_instance) throws BusinessException;
	
	/**
	 * 根据task的pk查询task
	 * @param pk_wf_task
	 * @return WFTask 
	 * @throws BusinessException
	 */
	public WFTask queryWFTaskByPk(String pk_wf_task) throws BusinessException;
	
	/**
	 * 
	 * @param billtype 单据类型
	 * @param billId   单据Id
	 * @param flowType 流程类型
	 * @return FlowAdminVO 包含审批工作项信息以及用于工作项和graph图形交互信息
	 * @throws BusinessException
	 * */
	public FlowAdminVO queryWorkitemForAdmin(String billtype, String billId, int flowType) throws BusinessException;
	
	/**
	 * 
	 * @param billOrTranstype	单据类型或交易类型
	 * @param pkOrg				组织
	 * @param operator			用户
	 * @param emendEnum			修订审批流类型。如果单据类型不支持修订审批流，那么传-1即可
	 * @param iWorkflowOrApproveflow	工作流或审批流参见WorkflowTypeEnum
	 * @return String 
	 * @throws BusinessException
	 */
	public String getStartWorkflowDef(String billOrTranstype, String pkOrg, String operator, int emendEnum, int iWorkflowOrApproveflow)  throws BusinessException;
	
	
	/**
	 * @since 6.3
	 * 批审时候，因为并不会去后台去workflownote，因此无法判断是否需要CA。
	 * 增加该接口方法，在批审时候调用，用于判断批审时候是否需要CA.
	 * @param userPK 用户PK
	 * @param billorTranstype 单据类型或者交易类型数组
	 * @param billIds         聚合VID数组
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
	 * @param PfParameterVO 流程审批时参数
	 * 弃审时候使用，返回上一环节的相关信息。包括：审批时间，审批批语
	 * 
	 * */
	public String[] findLastPostTacheInfo(PfParameterVO paraVo) throws BusinessException;
	
	/**
	 * @since 6.3
	 * 提供给协同
	 * */
	public Map<String,UserVO> findBillMakersByBillid(String[] billids) throws BusinessException;

	public WorkflownoteVO[] queryAllCheckInfo(String billid, String billtype) throws BusinessException;
	public abstract List<UserVO> queryWfProcess(String paramString1, String paramString2)
		    throws BusinessException;
	
	
}
