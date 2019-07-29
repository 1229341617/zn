package nc.bs.pub.pf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.pf.pub.PfDataCache;
import nc.bs.pub.taskmanager.TaskManagerDMO;
import nc.itf.uap.pf.IPFConfig;
import nc.itf.uap.pf.IWorkflowMachine;
import nc.itf.uap.pf.IplatFormEntry;
import nc.jdbc.framework.exception.DbException;
import nc.jzmobile.utils.MobileMessageUtil;
import nc.message.vo.MessageReceiver;
import nc.message.vo.NCMessage;
import nc.message.vo.ReceiverSetting;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pf.mobileapp.MobileAppUtils;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.msg.MessageVO;
import nc.vo.pub.msg.MessageinfoVO;
import nc.vo.pub.pf.AssignableInfo;
import nc.vo.pub.workflownote.WorkflownoteVO;
import nc.vo.sm.UserVO;
import nc.vo.uap.pf.PFBusinessException;
import nc.vo.wfengine.core.parser.XPDLParserException;
import nc.vo.wfengine.core.transition.BasicTransitionEx;
import nc.vo.wfengine.core.workflow.WorkflowProcess;
import nc.vo.wfengine.pub.WFTask;
import nc.vo.wfengine.pub.WfTaskType;

public class MobileApproveTools {
	public static String approveSilently(String billType, String billId, String checkResult, String checkNote
			, String checkman, String dispatched_ids[],List<String> mesSenders,String action)
	        throws Exception
    {
        Logger.debug("******\u8FDB\u5165PfUtilTools.approveSilently\u65B9\u6CD5*************************");
        Logger.debug((new StringBuilder()).append("* billType=").append(billType).toString());
        Logger.debug((new StringBuilder()).append("* billId=").append(billId).toString());
        Logger.debug((new StringBuilder()).append("* checkResult=").append(checkResult).toString());
        Logger.debug((new StringBuilder()).append("* checkNote=").append(checkNote).toString());
        Logger.debug((new StringBuilder()).append("* checkman=").append(checkman).toString());
        IPFConfig bsConfig = (IPFConfig)NCLocator.getInstance().lookup(IPFConfig.class);
        AggregatedValueObject billVo = bsConfig.queryBillDataVO(billType, billId);
        if(billVo == null)
            throw new PFBusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("busitype", "busitypehint-000063"));
        IWorkflowMachine bsWorkflow = (IWorkflowMachine)NCLocator.getInstance().lookup(IWorkflowMachine.class);
        HashMap hmPfExParams = new HashMap();
        WorkflownoteVO worknoteVO = bsWorkflow.checkWorkFlow((new StringBuilder()).append(action).append(checkman).toString(), billType, billVo, hmPfExParams);
        if(worknoteVO != null)
        {
            worknoteVO.setChecknote(checkNote);
            if("Y".equalsIgnoreCase(checkResult))
                worknoteVO.setApproveresult("Y");
            else
            if("N".equalsIgnoreCase(checkResult))
                worknoteVO.setApproveresult("N");
            else
            if("R".equalsIgnoreCase(checkResult))
            {
                worknoteVO.getTaskInfo().getTask().setTaskType(WfTaskType.Backward.getIntValue());
                worknoteVO.getTaskInfo().getTask().setBackToFirstActivity(true);
            } else
            {
                return NCLangRes4VoTransl.getNCLangRes().getStrByID("busitype", "busitypehint-000064");
            }
            if(dispatched_ids != null && dispatched_ids.length > 0)
            {
                HashMap hm = new HashMap();
                for(int i = 0; i < dispatched_ids.length; i++)
                {
                    int index = dispatched_ids[i].indexOf("#");
                    if(index < 0 || index > dispatched_ids[i].length() - 2)
                        continue;
                    String userid = dispatched_ids[i].substring(0, index);
                    String actDefid = dispatched_ids[i].substring(index + 1);
                    if(hm.get(actDefid) == null)
                        hm.put(actDefid, new HashSet());
                    ((HashSet)hm.get(actDefid)).add(userid);
                }

                Vector vecDispatch = worknoteVO.getTaskInfo().getAssignableInfos();
                for(int i = 0; i < vecDispatch.size(); i++)
                {
                    AssignableInfo ai = (AssignableInfo)vecDispatch.get(i);
                    if(ai.getAssignedOperatorPKs() != null)
                        ai.getAssignedOperatorPKs().clear();
                    if(ai.getOuAssignedUsers() != null)
                        ai.getOuAssignedUsers().clear();
                    HashSet hs = (HashSet)hm.get(ai.getActivityDefId());
                    if(hs != null)
                    {
                        Iterator iter = hs.iterator();
                        do
                        {
                            if(!iter.hasNext())
                                break;
                            String userId = (String)iter.next();
                            if(!ai.getAssignedOperatorPKs().contains(userId))
                                ai.getAssignedOperatorPKs().add(userId);
                        } while(true);
                    }
                }

            }
            
            worknoteVO.setMsgExtCpySenders(mesSenders);
        } else
        {
            Logger.debug("checkWorkflow\u8FD4\u56DE\u7684\u7ED3\u679C\u4E3Anull");
        }
        IplatFormEntry pff = (IplatFormEntry)NCLocator.getInstance().lookup(IplatFormEntry.class);
        pff.processAction((new StringBuilder()).append(action).append(checkman).toString(), billType, worknoteVO, billVo, null, hmPfExParams);
        return null;
    }
	
	public static String approveSilently(String billType, String billId, String checkResult, String checkNote
			, String checkman, String dispatched_ids[],List<String> mesSenders)
	        throws Exception
    {
        Logger.debug("******\u8FDB\u5165PfUtilTools.approveSilently\u65B9\u6CD5*************************");
        Logger.debug((new StringBuilder()).append("* billType=").append(billType).toString());
        Logger.debug((new StringBuilder()).append("* billId=").append(billId).toString());
        Logger.debug((new StringBuilder()).append("* checkResult=").append(checkResult).toString());
        Logger.debug((new StringBuilder()).append("* checkNote=").append(checkNote).toString());
        Logger.debug((new StringBuilder()).append("* checkman=").append(checkman).toString());
        IPFConfig bsConfig = (IPFConfig)NCLocator.getInstance().lookup(IPFConfig.class);
        AggregatedValueObject billVo = bsConfig.queryBillDataVO(billType, billId);
        if(billVo == null)
            throw new PFBusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("busitype", "busitypehint-000063"));
        IWorkflowMachine bsWorkflow = (IWorkflowMachine)NCLocator.getInstance().lookup(IWorkflowMachine.class);
        HashMap hmPfExParams = new HashMap();
        WorkflownoteVO worknoteVO = bsWorkflow.checkWorkFlow((new StringBuilder()).append("APPROVE").append(checkman).toString(), billType, billVo, hmPfExParams);
        if(worknoteVO != null)
        {
            worknoteVO.setChecknote(checkNote);
            if("Y".equalsIgnoreCase(checkResult))
                worknoteVO.setApproveresult("Y");
            else
            if("N".equalsIgnoreCase(checkResult))
                worknoteVO.setApproveresult("N");
            else
            if("R".equalsIgnoreCase(checkResult))
            {
                worknoteVO.getTaskInfo().getTask().setTaskType(WfTaskType.Backward.getIntValue());
                worknoteVO.getTaskInfo().getTask().setBackToFirstActivity(true);
            } else
            {
                return NCLangRes4VoTransl.getNCLangRes().getStrByID("busitype", "busitypehint-000064");
            }
            if(dispatched_ids != null && dispatched_ids.length > 0)
            {
                HashMap hm = new HashMap();
                for(int i = 0; i < dispatched_ids.length; i++)
                {
                    int index = dispatched_ids[i].indexOf("#");
                    if(index < 0 || index > dispatched_ids[i].length() - 2)
                        continue;
                    String userid = dispatched_ids[i].substring(0, index);
                    String actDefid = dispatched_ids[i].substring(index + 1);
                    if(hm.get(actDefid) == null)
                        hm.put(actDefid, new HashSet());
                    ((HashSet)hm.get(actDefid)).add(userid);
                }

                Vector vecDispatch = worknoteVO.getTaskInfo().getAssignableInfos();
                for(int i = 0; i < vecDispatch.size(); i++)
                {
                    AssignableInfo ai = (AssignableInfo)vecDispatch.get(i);
                    if(ai.getAssignedOperatorPKs() != null)
                        ai.getAssignedOperatorPKs().clear();
                    if(ai.getOuAssignedUsers() != null)
                        ai.getOuAssignedUsers().clear();
                    HashSet hs = (HashSet)hm.get(ai.getActivityDefId());
                    if(hs != null)
                    {
                        Iterator iter = hs.iterator();
                        do
                        {
                            if(!iter.hasNext())
                                break;
                            String userId = (String)iter.next();
                            if(!ai.getAssignedOperatorPKs().contains(userId))
                                ai.getAssignedOperatorPKs().add(userId);
                        } while(true);
                    }
                }

            }
            
            worknoteVO.setMsgExtCpySenders(mesSenders);
        } else
        {
            Logger.debug("checkWorkflow\u8FD4\u56DE\u7684\u7ED3\u679C\u4E3Anull");
        }
        IplatFormEntry pff = (IplatFormEntry)NCLocator.getInstance().lookup(IplatFormEntry.class);
        pff.processAction((new StringBuilder()).append("APPROVE").append(checkman).toString(), billType, worknoteVO, billVo, null, hmPfExParams);
        return null;
    }
	
	/**给移动端发送抄送消息**/
	public static void sendCpyToMobileApp(List<MessageinfoVO> msgInfoVOs){
		for(MessageinfoVO msg : msgInfoVOs){
			// 是否是已经开发完成的移动审批单据
			if (!MobileAppUtils.isMobileAppBillType(msg.getPk_billtype())) {
				continue;
			}
			Logger.error("==========这里开始给移动端发抄送消息==========");
			try {
				String userCode = MobileAppUtils.getUserCodeById(msg.getCheckman());
				UserVO user = new UserVO();
				user.setCuserid(msg.getCheckman());
				user.setUser_code(MobileAppUtils.getUserCodeById(userCode));
				MobileMessageUtil.sendMobileMessage(MessageVO.getMessageNoteAfterI18N(msg.getTitle()), user, "1", "");
				
			} catch (Exception ex) {
				Logger.error("移动端发抄送消息异常:\r\n" + ex.toString());
			}
			Logger.error("==========移动端发抄送消息结束==========");
		}
	}
	/***给移动端发送审批流里配置的消息***/
	public static void sendMsgSyncToMobileApp(List<NCMessage> toBeSent,WFTask task){
		for(NCMessage msg : toBeSent){
			// 是否是已经开发完成的移动审批单据
			if (!MobileAppUtils.isMobileAppBillType(task.getBillType())) {
				continue;
			}
			Logger.error("==========这里开始给移动端发抄送消息==========");
			try {
				
				ReceiverSetting settting = msg.getReceiverSetting();
				MessageReceiver[] receiverArray = settting.getReceiverVOs();
				for(MessageReceiver receiver : receiverArray){
					UserVO user = new UserVO();
					user.setCuserid(receiver.getPk_user());
					user.setUser_code(MobileAppUtils.getUserCodeById(receiver.getPk_user()));
					MobileMessageUtil.sendMobileMessage(msg.getMessage().getContent(), user, "1", "");
				}
			} catch (Exception ex) {
				Logger.error("移动端发抄送消息异常:\r\n" + ex.toString());
			}
			Logger.error("==========移动端发抄送消息结束==========");
		}
	}
	
	
	/**2018/03/17
	 * mxx
	 * 
	 * 获取下一级审批人
	 * @param actionCode
	 * @param billType
	 * @param aggvo
	 * @param args1
	 * @param args2
	 * @param args3
	 * @param args4
	 * @param args5
	 * @param ht
	 * @return
	 * @throws BusinessException
	 * @throws DbException
	 * @throws XPDLParserException
	 */
	
	public static String getNextApprover(String taskid) throws BusinessException, DbException, XPDLParserException{
		 String name=null;
		try{
			//PfParameterVO paraVO = PfUtilBaseTools.getVariableValue(IPFActionName.APPROVE, billType, aggvo, null, null, null, null, null, new Hashtable());
			//WorkflownoteVO[] uncheckedNoteVOs = new EngineService().queryWorkitemsNotCheck(paraVO.m_billType, paraVO.m_billVersionPK, paraVO.m_operator, 2); 
			WFTask currentTask = new TaskManagerDMO().getTaskByPK(taskid);
			WorkflowProcess wp = PfDataCache.getWorkflowProcess(currentTask.getWfProcessDefPK(), currentTask.getWfProcessInstancePK());
			 for(int i=0;i<wp.getTransitions().size();i++){
				 BasicTransitionEx be=(BasicTransitionEx) wp.getTransitions().get(i);
				 if(currentTask.getActivityID().equals(be.getFrom())){
					  name=wp.findActivityByID(be.getTo()).getName();
				 }
				 be=null;
			 }
		}catch(Exception ex){
			Logger.error("获取下一个审批人出错!!!" + ex.toString());
		}
		return name;
	}
}
