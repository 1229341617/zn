/*     */ package nc.bs.pub.pf.pfframe;
/*     */ 
/*     */ import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
import java.util.Properties;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
/*     */ import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.BusinessAppServer;
/*     */ import nc.bs.logging.Logger;
/*     */ import nc.bs.pf.pub.PfDataCache;
/*     */ import nc.bs.pub.pf.IPfAfterAction;
/*     */ import nc.bs.pub.pf.IPfBeforeAction;
/*     */ import nc.bs.pub.pf.PfUtilDMO;
/*     */ import nc.bs.pub.pf.PfUtilTools;
/*     */ import nc.bs.pub.pflock.PfBusinessLock;
/*     */ import nc.bs.pub.pflock.VOConsistenceCheck;
/*     */ import nc.bs.pub.pflock.VOLockData;
/*     */ import nc.bs.pub.pflock.VOsConsistenceCheck;
/*     */ import nc.bs.pub.pflock.VOsLockData;
/*     */ import nc.bs.pub.pflock.WFSuperVOLockData;
/*     */ import nc.bs.pub.workflownote.WorknoteManager;
/*     */ import nc.bs.pub.workflowpsn.WorkflowPersonDAO;
/*     */ import nc.bs.uap.pf.workflow.WFAgentMessageHandler;
/*     */ import nc.impl.uap.pf.PFConfigImpl;
/*     */ import nc.itf.uap.pf.IPFBusiAction;
/*     */ import nc.itf.uap.pf.IPFConfig;
/*     */ import nc.itf.uap.pf.IPFResource;
/*     */ import nc.itf.uap.pf.IWorkflowMachine;
/*     */ import nc.itf.uap.pf.IplatFormEntry;
/*     */ import nc.jdbc.framework.exception.DbException;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jzmobile.utils.MobileMessageUtil;
/*     */ import nc.vo.jcom.lang.StringUtil;
/*     */ import nc.vo.ml.NCLangRes4VoTransl;
/*     */ import nc.vo.pf.change.PfUtilBaseTools;
/*     */ import nc.vo.pub.AggregatedValueObject;
/*     */ import nc.vo.pub.BusinessException;
/*     */ import nc.vo.pub.pf.PfUtilActionVO;
/*     */ import nc.vo.pub.workflownote.WorkflownoteVO;
/*     */ import nc.vo.pub.workflowpsn.WFAgentHistoryVO;
/*     */ import nc.vo.pub.workflowpsn.WorkflowagentVO;
/*     */ import nc.vo.pub.workflowpsn.WorkflowpersonVO;
/*     */ import nc.vo.sm.UserVO;
/*     */ import nc.vo.uap.pf.PFBatchExceptionInfo;
/*     */ import nc.vo.uap.pf.PFBusinessException;
/*     */ import nc.vo.uap.pf.PfProcessBatchRetObject;
/*     */ import nc.vo.wfengine.core.activity.Activity;
/*     */ import nc.vo.wfengine.core.parser.XPDLParserException;
/*     */ import nc.vo.wfengine.core.workflow.WorkflowProcess;
/*     */ import nc.vo.wfengine.pub.WFTask;
/*     */ import nc.vo.wfengine.pub.WfTaskType;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class PlatFormEntryImpl
/*     */   implements IplatFormEntry
/*     */ {
			private void takebackoa(String actionName, String[] billTypes,
					String[] billIds) throws BusinessException, DAOException {
				if(billIds != null) {
					for (int i = 0; i < billIds.length; i++) {
						if(actionName != null &&(actionName.contains("UNSAVEBILL") || actionName.contains("UNSAVE"))) {
							//String taskpksql = " select n.pk_wf_task pk_wf_task from pub_workflownote n where n.billid='"+billids[i]+"' ";
							String taskpksql = " select n.pk_checkflow pk_checkflow from pub_workflownote n where n.billid='"+billIds[i]+"' ";
							Object taskpklistobj = new BaseDAO().executeQuery(taskpksql, new MapListProcessor());
							if(taskpklistobj != null) {
								List<Map<String,String>> taskpklist = (List<Map<String,String>>)taskpklistobj;
								for (int j = 0; j < taskpklist.size(); j++) {
									String taskpk = taskpklist.get(j).get("pk_checkflow");
									if(taskpk != null && !"".equals(taskpk)) {
										clearTask(taskpk, billTypes[i]);
									} 
								}
							}
						}
					}
				}
			}
			
			private void takebackoa(String actionName, String billType,
					String billid) throws BusinessException, DAOException {
				takebackoa(actionName, new String[] { billType }, new String[]{ billid });
			}
	
			private void clearTask(String taskpk, String billtype) {
				if (!MobileMessageUtil.judgeIsOABill(billtype)) {
					return;
				}
				Properties prop = getOaUrlProp();
				String uri=getStr(prop, "oaWebserviceUrl2");//oa的erp待办服务地址
				try {
			        URL url = new URL(uri + "?id="+taskpk+"");
			        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			        connection.setDoOutput(true); // 设置该连接是可以输出的
			        connection.setRequestMethod("GET"); // 设置请求方式
			        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			        connection.setConnectTimeout(30000);//30秒
			        connection.setReadTimeout(30000);//30秒
			        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
			        String line = null;
			        StringBuilder result = new StringBuilder();
			        while ((line = br.readLine()) != null) { // 读取数据
			            result.append(line + "\n");
			        }
			        connection.disconnect();
			
			        System.out.println(result.toString());
				 } catch (Exception e) {
			         e.printStackTrace();
			     }
			}
			
			private String getStr(Properties prop, String key) {
		        String value = (String) prop.get(key);
		        
		        try {
		        	if (value != null) {
		            	value = value.trim();
		            	return new String(value.getBytes("ISO-8859-1"), "UTF-8");
		            }
		        	return "";
		            
		        } catch (UnsupportedEncodingException e) {
		            return null;
		        }
		    }
			
			private Properties getOaUrlProp() {
				BusinessAppServer baServer = BusinessAppServer.getInstance();
				String proFilePath = baServer.getServerBase() + "/ierp/jz/mobile.properties";
				if ( !new File(proFilePath).exists() ) {
					return null;
				}
				InputStream inputStream;
				try {
				    inputStream = new BufferedInputStream(new FileInputStream(proFilePath));
				} catch (FileNotFoundException e1) {
				    throw new RuntimeException(e1);
				}  
				Properties prop = new Properties();
				try {
				    prop.load(inputStream);
				} catch (IOException e) {
				    throw new RuntimeException(e);
				}
				return prop;
			}
/*     */   public Object processAction(String actionName, String billType, WorkflownoteVO worknoteVO, AggregatedValueObject billvo, Object userObj, HashMap hmPfExParams)
/*     */     throws BusinessException
/*     */   {
/*  62 */     Logger.debug("******进入PlatFormEntryImpl.processAction方法******************");
/*  63 */     Logger.debug("* actionName=" + actionName);
/*  64 */     Logger.debug("* billType=" + billType);

			  if(billvo != null) {
				  takebackoa(actionName, billType, billvo.getParentVO().getPrimaryKey());
			  }
/*     */     
/*     */ 
/*     */ 
/*  68 */     PfBusinessLock pfLock = null;
/*  69 */     PfBusinessLock pfAgentHistoryLock = null;
/*     */     try
/*     */     {
/*  72 */       if (hmPfExParams == null) {
/*  73 */         hmPfExParams = new HashMap();
/*     */       }
/*     */       
/*     */ 
/*  77 */       pfLock = new PfBusinessLock();
/*  78 */       Object paramNoLock = hmPfExParams == null ? null : hmPfExParams.get("nolockandconsist");
/*     */       
/*  80 */       if (paramNoLock == null) {
/*  81 */         pfLock.lock(new VOLockData(billvo, billType), new VOConsistenceCheck(billvo, billType));
/*     */       }
/*     */       
/*     */ 
/*     */ 
/*  86 */       if ((worknoteVO != null) && (worknoteVO.getTaskInfo() != null) && (worknoteVO.getTaskInfo().getTask() != null))
/*     */       {
/*     */ 
/*  89 */         WFTask task = worknoteVO.getTaskInfo().getTask();
/*     */         
/*  91 */         if (task.getTaskType() == WfTaskType.Backward.getIntValue()) {
/*  92 */           String backTo = task.getJumpToActivity();
/*     */           try
/*     */           {
/*  95 */             WorkflowProcess wp = PfDataCache.getWorkflowProcess(task.getWfProcessDefPK());
/*     */             
/*  97 */             Activity activity = wp.findActivityByID(backTo);
/*  98 */             if (wp.findStartActivity().getId().equals(backTo)) {
/*  99 */               task.setBackToFirstActivity(true);
/*     */             }
/*     */           } catch (XPDLParserException e) {
/* 102 */             throw new BusinessException(e);
/*     */           }
/*     */         }
/*     */       }
/*     */       
/*     */ 
/*     */ 
/*     */ 
/* 110 */       Object paramReloadVO = hmPfExParams == null ? null : hmPfExParams.get("reload_vo");
/*     */       
/* 112 */       AggregatedValueObject reloadvo = billvo;
/* 113 */       if (paramReloadVO != null) {
/* 114 */         String billId = billvo.getParentVO().getPrimaryKey();
/* 115 */         reloadvo = new PFConfigImpl().queryBillDataVO(billType, billId);
/* 116 */         if (reloadvo == null)
/* 117 */           throw new PFBusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("pfworkflow", "PlatFormEntryImpl-0000", null, new String[] { billType, billId }));
/* 118 */         hmPfExParams.remove("reload_vo");
/*     */       }
/*     */       
/*     */ 
/* 122 */       Object paramSilent = hmPfExParams == null ? null : hmPfExParams.get("silently");
/*     */       
/* 124 */       if ((worknoteVO == null) && (paramSilent != null) && ((PfUtilBaseTools.isApproveAction(actionName, billType)) || (PfUtilBaseTools.isSignalAction(actionName, billType))))
/*     */       {
/*     */ 
/*     */ 
/* 128 */         worknoteVO = ((IWorkflowMachine)NCLocator.getInstance().lookup(IWorkflowMachine.class)).checkWorkFlow(actionName, billType, reloadvo, hmPfExParams);
/*     */       }
/*     */       
/*     */ 
/*     */ 
/*     */ 
/* 134 */       Object checkObj = PfUtilTools.getBizRuleImpl(billType);
/* 135 */       AggregatedValueObject completeVO = reloadvo;
/* 136 */       AggregatedValueObject cloneVO = reloadvo;
/* 137 */       if ((checkObj instanceof IPfBeforeAction)) {
/* 138 */         completeVO = ((IPfBeforeAction)checkObj).beforeAction(reloadvo, userObj, hmPfExParams);
/* 139 */         AggregatedValueObject[] tmpAry = ((IPfBeforeAction)checkObj).getCloneVO();
/* 140 */         if ((tmpAry != null) && (tmpAry.length > 0)) {
/* 141 */           cloneVO = tmpAry[0];
/*     */         }
/*     */       }

//				PfParameterVO paraVo = (PfParameterVO)hashBilltypeToParavo.get(null + billType + completeVO.getParentVO().getPrimaryKey());
//				if (paraVo == null) {
//				 paraVo = (PfParameterVO)hashBilltypeToParavo.get(billType + completeVO.getParentVO().getPrimaryKey());
//				}
//				if (paraVo == null)
//				{
//				 paraVo = (PfParameterVO)hashBilltypeToParavo.get(billType);
//				}
//				WFTask startTask = paraVo.m_workFlow.getTaskInfo().getTask();
				
///*     */       String taskPK = startTask.getTaskPK();
/* 145 */       Object retObjAfterAction = ((IPFBusiAction)NCLocator.getInstance().lookup(IPFBusiAction.class)).processAction(actionName, billType, worknoteVO, completeVO, userObj, hmPfExParams);
/*     */       
/*     */ 
/*     */ 
/* 149 */       if ((checkObj instanceof IPfAfterAction)) {
/* 150 */         retObjAfterAction = ((IPfAfterAction)checkObj).afterAction(cloneVO, retObjAfterAction, hmPfExParams);
/*     */       }
/*     */       
/*     */       try
/*     */       {
/* 155 */         if ((worknoteVO != null) && (PfUtilBaseTools.isApproveAction(actionName, billType))) {
/* 156 */           String originalCheckMan = worknoteVO.getAgencyuser();
/* 157 */           if ((!StringUtil.isEmptyWithTrim(originalCheckMan)) && (!worknoteVO.getCheckman().equals(originalCheckMan))) {
/* 158 */             pfAgentHistoryLock = new PfBusinessLock();
/* 159 */             IPFResource agentSrv = (IPFResource)NCLocator.getInstance().lookup(IPFResource.class);
/* 160 */             WorkflowpersonVO outInfo = agentSrv.queryWFPersonOutInfo(originalCheckMan);
/* 161 */             if (outInfo != null) {
/* 162 */               WFAgentHistoryVO agentHistory = new WFAgentHistoryVO();
/* 163 */               agentHistory.setAgentor(worknoteVO.getCheckman());
/*     */               
/*     */ 
/* 166 */               agentHistory.setPk_outinfo(outInfo.getPk_outinfo());
/*     */               
/* 168 */               String pk_billType = "";
/* 169 */               WorkflowPersonDAO dao = new WorkflowPersonDAO();
/* 170 */               List<WorkflowagentVO> agents = dao.queryDynamicAgentVOs(originalCheckMan, worknoteVO.getPk_billtype());
/* 171 */               for (WorkflowagentVO agent : agents) {
/* 172 */                 if (originalCheckMan.equals(agent.getPk_cuserid())) {
/* 173 */                   pk_billType = agent.getBilltypes();
/* 174 */                   break;
/*     */                 }
/*     */               }
/* 177 */               agentHistory.setBilltype(pk_billType);
/* 178 */               pfAgentHistoryLock.lock(new WFSuperVOLockData(new WFAgentHistoryVO[] { agentHistory }), null);
/* 179 */               agentSrv.insertWFAgentHistoryInfo(agentHistory);
/*     */             }
/*     */           }
/*     */         }
/* 183 */         handleAgentMsgSend(worknoteVO, actionName, billType);
/*     */       } catch (DbException ex) {
/* 185 */         throw new BusinessException(ex);
/*     */       }
/*     */       
/* 188 */       Logger.debug("******离开PlatFormEntryImpl.processAction方法******************");
/* 189 */       return retObjAfterAction;
/*     */     }
/*     */     finally {
				}
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   private void handleAgentMsgSend(WorkflownoteVO worknoteVO, String actionName, String billtype)
/*     */   {
/* 206 */     if ((worknoteVO == null) || (!PfUtilBaseTools.isApproveAction(actionName, billtype))) {
/* 207 */       return;
/*     */     }
/* 209 */     WFAgentMessageHandler handler = new WFAgentMessageHandler();
/* 210 */     handler.handleApprove(worknoteVO);
/*     */   }
/*     */   
/*     */   public Object processBatch(String actionName, String billType, WorkflownoteVO worknoteVO, AggregatedValueObject[] billvos, Object[] userObjAry, HashMap hmPfExParams)
/*     */     throws BusinessException
/*     */   {
			  /**批量收回时，删除OA待办链接*/
			  if(billvos != null) {
				  for (int i = 0; i < billvos.length; i++) {
					  takebackoa(actionName, billType, billvos[i].getParentVO().getPrimaryKey());
				  }
			  }
			  
/* 216 */     PfBusinessLock pfLock = null;
/*     */     try
/*     */     {
/* 219 */       pfLock = new PfBusinessLock();
/* 220 */       pfLock.lock(new VOsLockData(billvos, billType), new VOsConsistenceCheck(billvos, billType));
/*     */       
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/* 227 */       if ((worknoteVO != null) && (worknoteVO.getTaskInfo() != null) && (worknoteVO.getTaskInfo().getTask() != null))
/*     */       {
/*     */ 
/* 230 */         WFTask task = worknoteVO.getTaskInfo().getTask();
/*     */         
/* 232 */         if (task.getTaskType() == WfTaskType.Backward.getIntValue()) {
/* 233 */           String backTo = task.getJumpToActivity();
/*     */           try
/*     */           {
/* 236 */             WorkflowProcess wp = PfDataCache.getWorkflowProcess(task.getWfProcessDefPK());
/*     */             
/* 238 */             Activity activity = wp.findActivityByID(backTo);
/* 239 */             if (wp.findStartActivity().getId().equals(backTo)) {
/* 240 */               task.setBackToFirstActivity(true);
/*     */             }
/*     */           } catch (XPDLParserException e) {
/* 243 */             throw new BusinessException(e);
/*     */           }
/*     */         }
/*     */       }
/*     */       
/*     */ 
/*     */ 
/* 250 */       Object paramReloadVO = hmPfExParams == null ? null : hmPfExParams.get("reload_vo");
/*     */       
/* 252 */       AggregatedValueObject[] reloadvos = billvos;
/* 253 */       if (paramReloadVO != null)
/*     */       {
/* 255 */         reloadvos = (AggregatedValueObject[])Array.newInstance(billvos[0].getClass(), billvos.length);
/*     */         
/*     */ 
/* 258 */         for (int i = 0; i < billvos.length; i++)
/*     */         {
/* 260 */           String billId = billvos[i].getParentVO().getPrimaryKey();
/* 261 */           reloadvos[i] = new PFConfigImpl().queryBillDataVO(billType, billId);
/* 262 */           if (reloadvos[i] == null) {
/* 263 */             throw new PFBusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("pfworkflow", "PlatFormEntryImpl-0000", null, new String[] { billType, billId }));
/*     */           }
/*     */         }
/* 266 */         hmPfExParams.remove("reload_vo");
/*     */       }
/*     */       
/*     */ 
/*     */ 
/* 271 */       Object checkObj = PfUtilTools.getBizRuleImpl(billType);
/* 272 */       AggregatedValueObject[] completeVOs = reloadvos;
/* 273 */       AggregatedValueObject[] cloneVOs = reloadvos;
/* 274 */       if ((checkObj instanceof IPfBeforeAction)) {
/* 275 */         completeVOs = ((IPfBeforeAction)checkObj).beforeBatch(billvos, userObjAry, hmPfExParams);
/* 276 */         cloneVOs = ((IPfBeforeAction)checkObj).getCloneVO();
/*     */       }
/*     */       
/*     */ 
/* 280 */       Object paramSilent = hmPfExParams == null ? null : hmPfExParams.get("silently");
/*     */       
/* 282 */       if ((worknoteVO == null) && (paramSilent != null) && (PfUtilBaseTools.isSignalFlowAction(actionName, billType)))
/*     */       {
/*     */ 
/* 285 */         worknoteVO = ((IWorkflowMachine)NCLocator.getInstance().lookup(IWorkflowMachine.class)).checkWorkFlow(actionName, billType, completeVOs[0], hmPfExParams);
/*     */       }
/*     */       
/*     */ 
/*     */ 
/* 290 */       PFBatchExceptionInfo batchExceptionInfo = new PFBatchExceptionInfo();
/* 291 */       Object[] retObjsAfterAction = ((IPFBusiAction)NCLocator.getInstance().lookup(IPFBusiAction.class)).processBatch(actionName, billType, completeVOs, userObjAry, worknoteVO, hmPfExParams, batchExceptionInfo);
/*     */       
/*     */ 
/*     */ 
/*     */ 
/* 296 */       if ((checkObj instanceof IPfAfterAction)) {
/* 297 */         retObjsAfterAction = ((IPfAfterAction)checkObj).afterBatch(cloneVOs, retObjsAfterAction, hmPfExParams);
/*     */       }
/*     */       
/* 300 */       return new PfProcessBatchRetObject(retObjsAfterAction, batchExceptionInfo);
/*     */     } finally {
/* 302 */       if (pfLock != null)
/*     */       {
/* 304 */         pfLock.unLock();
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */   public UserVO[] queryValidCheckers(String billId, String billType) throws BusinessException {
/* 310 */     WorknoteManager noteMgr = new WorknoteManager();
/*     */     try {
/* 312 */       return noteMgr.queryValidCheckers(billId, billType);
/*     */     } catch (DbException e) {
/* 314 */       Logger.error(e.getMessage(), e);
/* 315 */       throw new PFBusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("pfworkflow", "PlatFormEntryImpl-0001", null, new String[] { e.getMessage() }));
/*     */     }
/*     */   }
/*     */   
/*     */   public PfUtilActionVO[] getActionDriveVOs(String billType, String busiType, String pkCorp, String actionName) throws BusinessException
/*     */   {
/* 321 */     PfUtilActionVO[] driveActions = null;
/*     */     try {
/* 323 */       PfUtilDMO dmo = new PfUtilDMO();
/* 324 */       driveActions = dmo.queryDriveAction(billType, busiType, pkCorp, actionName, null);
/*     */     } catch (DbException e) {
/* 326 */       Logger.error(e.getMessage(), e);
/* 327 */       throw new PFBusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("pfworkflow", "PlatFormEntryImpl-0002", null, new String[] { e.getMessage() }));
/*     */     }
/* 329 */     return driveActions;
/*     */   }
/*     */   
/*     */ 
/*     */   public Object processBatch(String actionName, WorkflownoteVO worknoteVO, String[] billTypes, String[] billIds)
/*     */     throws BusinessException
/*     */   {
/* 336 */     List<Object> retList = new ArrayList();
/*     */     
/* 338 */     IPFConfig pfConf = (IPFConfig)NCLocator.getInstance().lookup(IPFConfig.class);
/*     */     
/*     */ 
/* 341 */     Map<String, List<AggregatedValueObject>> billVOMap = new HashMap();
/* 342 */     for (int i = 0; i < billTypes.length; i++) {
/* 343 */       String billType = billTypes[i];
/* 344 */       String billId = billIds[i];
/*     */       
/* 346 */       AggregatedValueObject billvo = pfConf.queryBillDataVO(billType, billId);
/*     */       
/* 348 */       if (!billVOMap.containsKey(billType)) {
/* 349 */         List<AggregatedValueObject> list = new ArrayList();
/* 350 */         billVOMap.put(billType, list);
/*     */       }
/*     */       
/* 353 */       List<AggregatedValueObject> list = (List)billVOMap.get(billType);
/* 354 */       list.add(billvo);
/*     */     }
/*     */     
/* 357 */     HashMap param = new HashMap();
/* 358 */     param.put("batch", "batch");
/*     */     
/* 360 */     for (Iterator<String> it = billVOMap.keySet().iterator(); it.hasNext();)
/*     */     {
/*     */ 
/* 363 */       String billtype = (String)it.next();
/*     */       
/* 365 */       WorkflownoteVO currNote = (WorkflownoteVO)worknoteVO.clone();
/* 366 */       HashMap currParam = (HashMap)param.clone();
/*     */       
/* 368 */       currParam.put("worknote", currNote);
/*     */       
/* 370 */       List<AggregatedValueObject> list = (List)billVOMap.get(billtype);
/* 371 */       Object ret = processBatch(actionName, billtype, currNote, (AggregatedValueObject[])list.toArray(new AggregatedValueObject[0]), null, currParam);
/*     */       
/*     */ 
/* 374 */       retList.add(ret);
/*     */     }
/*     */     
/* 377 */     return retList;
/*     */   }
/*     */ }

/* Location:           D:\DEV\DEV-NC6.5-ZHONGNAN\nchome\modules\riart\META-INF\lib\riart_riartplatformLevel-1.jar
 * Qualified Name:     nc.bs.pub.pf.pfframe.PlatFormEntryImpl
 * Java Class Version: 7 (51.0)
 * JD-Core Version:    0.7.1
 */