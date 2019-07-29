/*      */ package nc.bs.pub.taskmanager;
/*      */ 
/*      */ import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
/*      */ import java.util.Collection;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
import java.util.Properties;

/*      */ import nc.bs.dao.BaseDAO;
/*      */ import nc.bs.dao.DAOException;
/*      */ import nc.bs.framework.common.ITimeService;
/*      */ import nc.bs.framework.common.InvocationInfoProxy;
/*      */ import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.BusinessAppServer;
/*      */ import nc.bs.logging.Logger;
/*      */ import nc.bs.pf.pub.PFRequestDataCacheProxy;
/*      */ import nc.bs.pf.pub.PfDataCache;
/*      */ import nc.bs.pf.pub.cache.CondStringKey;
/*      */ import nc.bs.pf.pub.cache.ICacheDataQueryCallback;
/*      */ import nc.bs.pf.pub.cache.IRequestDataCacheKey;
/*      */ import nc.bs.pf.pub.cache.WFTaskCacheKey;
/*      */ import nc.bs.pub.pf.IMessagePriorityCallback;
/*      */ import nc.bs.pub.pf.PfMessageUtil;
/*      */ import nc.bs.pub.pf.PfUtilTools;
/*      */ import nc.bs.pub.taskmanager.workitem.IWorkitemDistributor;
/*      */ import nc.bs.pub.workflownote.WorknoteManager;
/*      */ import nc.bs.pub.workflowpsn.WFAgentTaskTransfer;
/*      */ import nc.bs.pub.workflowpsn.WorkflowPersonDAO;
/*      */ import nc.bs.wfengine.engine.ext.TaskTopicResolver;
/*      */ import nc.itf.uap.ml.DataMultiLangAccessor;
/*      */ import nc.jdbc.framework.JdbcSession;
/*      */ import nc.jdbc.framework.PersistenceManager;
/*      */ import nc.jdbc.framework.SQLParameter;
/*      */ import nc.jdbc.framework.exception.DbException;
/*      */ import nc.jdbc.framework.processor.ColumnProcessor;
/*      */ import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.message.MsgContentCreatorInAppLayer;
/*      */ import nc.message.templet.itf.IMsgtempletquery;
/*      */ import nc.message.templet.vo.MsgtempletVO;
/*      */ import nc.message.templet.vo.MsgtmptypeVO;
/*      */ import nc.message.vo.NCMessage;
/*      */ import nc.uap.bd.util.BDInSqlUtil;
/*      */ import nc.vo.jcom.lang.StringUtil;
/*      */ import nc.vo.pf.pub.util.ArrayUtil;
/*      */ import nc.vo.pf.sql.ParameterizedBatchCaller;
/*      */ import nc.vo.pub.BusinessException;
/*      */ import nc.vo.pub.billtype2.Billtype2VO;
/*      */ import nc.vo.pub.billtype2.ExtendedClassEnum;
/*      */ import nc.vo.pub.lang.UFBoolean;
/*      */ import nc.vo.pub.pf.Pfi18nTools;
/*      */ import nc.vo.pub.workflownote.WorkflownoteVO;
/*      */ import nc.vo.wfengine.pub.WFTask;
/*      */ import nc.vo.wfengine.pub.WFTaskMappingMeta;
/*      */ import nc.vo.wfengine.pub.WfTaskOrInstanceStatus;
/*      */ import nc.vo.wfengine.pub.WfTaskType;
/*      */ import nc.vo.wfengine.pub.WorkitemMsgContext;
/*      */ import uap.apppf.util.SQLTransferMeaningUtil;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public class TaskManagerDMO
/*      */ {
/*   69 */   private static final WFTaskMappingMeta mappingMeta = new WFTaskMappingMeta();
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public WFTask getPrevTask(WFTask curTask)
/*      */     throws DbException
/*      */   {
/*   79 */     if ((curTask == null) || (curTask.getTaskPK() == null)) {
/*   80 */       return null;
/*      */     }
/*   82 */     String whereCond = "pk_wf_actinstance in (  select src_actinstance from pub_wf_actinstancesrc where target_actinstance in ( select pk_wf_actinstance from pub_wf_task  where pk_wf_task ='" + curTask.getTaskPK() + "'))";
/*      */     
/*      */ 
/*      */ 
/*      */ 
/*   87 */     return queryTaskByCondition(whereCond);
/*      */   }
/*      */   
/*      */   public WFTask getNextTask(WFTask curTask) throws DbException {
/*   91 */     if ((curTask == null) || (curTask.getTaskPK() == null)) {
/*   92 */       return null;
/*      */     }
/*   94 */     String whereCond = "pk_wf_actinstance in (  select target_actinstance from pub_wf_actinstancesrc where src_actinstance in ( select pk_wf_actinstance from pub_wf_task  where pk_wf_task ='" + curTask.getTaskPK() + "'))";
/*      */     
/*      */ 
/*      */ 
/*      */ 
/*   99 */     return queryTaskByCondition(whereCond);
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public WFTask getTaskByPK(String taskPK)
/*      */     throws DbException
/*      */   {
/*  109 */     PersistenceManager persist = null;
/*      */     try {
/*  111 */       persist = PersistenceManager.getInstance();
/*  112 */       WFTask task = (WFTask)persist.retrieveByPK(WFTask.class, mappingMeta, taskPK);
/*      */       
/*  114 */       return task;
/*      */     } finally {
/*  116 */       if (persist != null) {
/*  117 */         persist.release();
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public List<WFTask> getTaskByPKs(List<String> taskpks)
/*      */     throws DbException
/*      */   {
/*  130 */     List<WFTask> tasks = new ArrayList();
/*  131 */     PersistenceManager persist = null;
/*      */     
/*  133 */     String condition = " pk_wf_task in " + BDInSqlUtil.getInSql((String[])taskpks.toArray(new String[0]), false);
/*      */     try
/*      */     {
/*  136 */       persist = PersistenceManager.getInstance();
/*  137 */       Collection<WFTask> taskCollection = persist.retrieveByClause(WFTask.class, mappingMeta, condition);
/*  138 */       Iterator<WFTask> iterator; if (taskCollection != null) {
/*  139 */         for (iterator = taskCollection.iterator(); iterator.hasNext();) {
/*  140 */           WFTask task = (WFTask)iterator.next();
/*  141 */           if (task != null) {
/*  142 */             tasks.add(task);
/*      */           }
/*      */         }
/*      */       }
/*      */       
/*  147 */       return tasks;
/*      */     } finally {
/*  149 */       if (persist != null) {
/*  150 */         persist.release();
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void inefficientTasksByActInstPKs(String actInstPKs)
/*      */     throws DbException, BusinessException
/*      */   {
/*  175 */     String sqlWorkflow = " pk_wf_task in (select pk_wf_task from pub_wf_task where pk_wf_actinstance in (" + actInstPKs + "))";
/*      */     
/*      */ 
/*  178 */     BaseDAO dao = new BaseDAO();
/*  179 */     Collection<WorkflownoteVO> colWorknote = dao.retrieveByClause(WorkflownoteVO.class, sqlWorkflow);
/*      */     
/*      */ 
/*  182 */     inefficientWorkitems(colWorknote);
/*      */     
/*  184 */     String sql = "update pub_wf_task set taskstatus =" + WfTaskOrInstanceStatus.Inefficient.getIntValue() + " where pk_wf_actinstance in(" + actInstPKs + ")";
/*      */     
/*      */ 
/*      */ 
/*  188 */     PersistenceManager persist = null;
/*      */     try {
/*  190 */       persist = PersistenceManager.getInstance();
/*  191 */       JdbcSession jdbc = persist.getJdbcSession();
/*  192 */       jdbc.executeUpdate(sql);
/*      */     } finally {
/*  194 */       if (persist != null) {
/*  195 */         persist.release();
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void updateChecknote(String pkCheckflow, String note)
/*      */     throws DbException
/*      */   {
/*  237 */     String sqlWorkflow = "update pub_workflownote set checknote= ? where pk_checkflow=?";
/*  238 */     PersistenceManager persist = null;
/*      */     try {
/*  240 */       persist = PersistenceManager.getInstance();
/*  241 */       JdbcSession jdbc = persist.getJdbcSession();
/*  242 */       SQLParameter para = new SQLParameter();
/*  243 */       para.addParam(note);
/*  244 */       para.addParam(pkCheckflow);
/*  245 */       jdbc.executeUpdate(sqlWorkflow, para);
/*      */     } finally {
/*  247 */       if (persist != null) {
/*  248 */         persist.release();
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */   private WorkflownoteVO createWorkitem(WFTask task, String cuserid) throws DbException {
/*  254 */     int iWfType = task.getWorkflowType();
/*  255 */     WorkitemMsgContext context = task.getContext().clone();
/*  256 */     WorkflownoteVO noteVO = new WorkflownoteVO();
/*  257 */     noteVO.setMsgContext(context);
/*  258 */     noteVO.setFuncode(context.getFuncode());
/*  259 */     noteVO.setPk_billtype(task.getBillType());
/*  260 */     noteVO.setBillno(task.getBillNO());
/*      */     
/*  262 */     noteVO.setSenderman(StringUtil.isEmptyWithTrim(task.getSenderman()) ? InvocationInfoProxy.getInstance().getUserId() : task.getSenderman());
/*  263 */     noteVO.setIscheck("N");
/*  264 */     noteVO.setSenddate(task.getCreateTime());
/*      */     
/*      */ 
/*  267 */     if ((task.getParticipantProcessMode() == 0) || (task.getParticipantProcessMode() == -1))
/*      */     {
/*  269 */       if (task.getParticipantID().equals(context.getAgent()))
/*      */       {
/*  271 */         if (!task.getParticipantID().equals(task.getApproveAgent())) {
/*  272 */           noteVO.setAgencyuser(task.getApproveAgent());
/*      */         }
/*      */       }
/*      */       
/*  276 */       if (task.getParticipantID().equals(task.getApproveAgent())) {
/*  277 */         context.setAgent("");
/*      */       }
/*      */     }
/*      */     
/*      */ 
/*  282 */     String checkman = cuserid;
/*      */     
/*      */ 
/*      */ 
/*  286 */     String[] agentInfo = getAgentInfo(task, checkman);
/*  287 */     if ((agentInfo != null) && (!checkman.equals(agentInfo[0])))
/*      */     {
/*  289 */       noteVO.setAgencyuser(checkman);
/*      */       
/*  291 */       checkman = agentInfo[0];
/*  292 */       String agentName = agentInfo[1];
/*      */       
/*      */ 
/*  295 */       context.setCheckman(checkman);
/*  296 */       context.setAgent(agentName);
/*      */     }
/*      */     
/*  299 */     context.setCheckman(checkman);
/*      */     
/*      */ 
/*  302 */     NCMessage ncmsg = TaskTopicResolver.constructNCMsg(context);
/*      */     
/*  304 */     task.setTopic(ncmsg.getMessage().getSubject());
/*      */     
/*      */ 
/*  307 */     noteVO.setNcMsg(ncmsg);
/*  308 */     noteVO.setCheckman(checkman);
/*  309 */     if ((task.getParticipantProcessMode() == 0) || (task.getParticipantProcessMode() == 0))
/*      */     {
/*  311 */       noteVO.setObserver(task.getApproveAgent());
/*  312 */     } else if (!String.valueOf(checkman).equals(cuserid)) {
/*  313 */       noteVO.setObserver(cuserid);
/*      */     }
/*      */     
/*      */ 
/*  317 */     noteVO.setMessagenote(task.getTopic());
/*      */     
/*  319 */     noteVO.setReceivedeleteflag(UFBoolean.FALSE);
/*  320 */     noteVO.setPk_org(task.getPk_org());
/*      */     
/*  322 */     noteVO.setPk_group(InvocationInfoProxy.getInstance().getGroupId());
/*  323 */     noteVO.setBillid(task.getBillID());
/*  324 */     noteVO.setBillVersionPK(task.getBillversionPK());
/*  325 */     noteVO.setIsmsgbind("N");
/*  326 */     noteVO.setPk_wf_task(task.getTaskPK());
/*  327 */     noteVO.setApprovestatus(Integer.valueOf(task.getStatus()));
/*  328 */     if (task.getTaskType() == WfTaskType.Makebill.getIntValue()) {
/*  329 */       noteVO.setActiontype("MAKEBILL");
/*      */     } else {
/*  331 */       noteVO.setActiontype("Z");
/*      */     }
/*      */     
/*      */ 
/*  335 */     noteVO.setWorkflow_type(Integer.valueOf(iWfType));
/*  336 */     noteVO.setPriority(getMessagePriority(task.getBillType(), task.getBillversionPK()));
/*  337 */     return noteVO;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private WorkflownoteVO createWorkitem(WFTask task, String cuserid, Map<String, UFBoolean> propertyMap, String langcode, MsgtempletVO[] msgTemVOs, HashMap<String, MsgtmptypeVO> msgtmptypeVOHash, HashMap<String, MsgContentCreatorInAppLayer.NcMessageInfo> ncMessageInfoHash)
/*      */     throws DbException
/*      */   {
/*  349 */     int iWfType = task.getWorkflowType();
/*  350 */     WorkitemMsgContext context = task.getContext().clone();
/*  351 */     WorkflownoteVO noteVO = new WorkflownoteVO();
/*  352 */     noteVO.setMsgContext(context);
/*  353 */     noteVO.setFuncode(context.getFuncode());
/*  354 */     noteVO.setPk_billtype(task.getBillType());
/*  355 */     noteVO.setBillno(task.getBillNO());
/*      */     
/*  357 */     noteVO.setSenderman(StringUtil.isEmptyWithTrim(task.getSenderman()) ? InvocationInfoProxy.getInstance().getUserId() : task.getSenderman());
/*  358 */     noteVO.setIscheck("N");
/*  359 */     noteVO.setSenddate(task.getCreateTime());
/*      */     
/*      */ 
/*  362 */     if ((task.getParticipantProcessMode() == 0) || (task.getParticipantProcessMode() == -1))
/*      */     {
/*  364 */       if (task.getParticipantID().equals(context.getAgent()))
/*      */       {
/*  366 */         if (!task.getParticipantID().equals(task.getApproveAgent())) {
/*  367 */           noteVO.setAgencyuser(task.getApproveAgent());
/*      */         }
/*      */       }
/*      */       
/*  371 */       if (task.getParticipantID().equals(task.getApproveAgent())) {
/*  372 */         context.setAgent("");
/*      */       }
/*      */     }
/*      */     
/*      */ 
/*  377 */     String checkman = cuserid;
/*      */     
/*      */ 
/*      */ 
/*  381 */     String[] agentInfo = getAgentInfo(task, checkman, propertyMap);
/*  382 */     if ((agentInfo != null) && (!checkman.equals(agentInfo[0])))
/*      */     {
/*  384 */       noteVO.setAgencyuser(checkman);
/*      */       
/*  386 */       checkman = agentInfo[0];
/*  387 */       String agentName = agentInfo[1];
/*      */       
/*      */ 
/*  390 */       context.setCheckman(checkman);
/*  391 */       context.setAgent(agentName);
/*      */     }
/*      */     
/*  394 */     context.setCheckman(checkman);
/*      */     
/*      */ 
/*  397 */     NCMessage ncmsg = TaskTopicResolver.constructNCMsg(context, langcode, msgTemVOs, msgtmptypeVOHash, ncMessageInfoHash);
/*      */     
/*  399 */     task.setTopic(ncmsg.getMessage().getSubject());
/*      */     
/*      */ 
/*  402 */     noteVO.setNcMsg(ncmsg);
/*  403 */     noteVO.setCheckman(checkman);
/*  404 */     if ((task.getParticipantProcessMode() == 0) || (task.getParticipantProcessMode() == 0))
/*      */     {
/*  406 */       noteVO.setObserver(task.getApproveAgent());
/*  407 */     } else if (!String.valueOf(checkman).equals(cuserid)) {
/*  408 */       noteVO.setObserver(cuserid);
/*      */     }
/*      */     
/*      */ 
/*  412 */     noteVO.setMessagenote(task.getTopic());
/*      */     
/*  414 */     noteVO.setReceivedeleteflag(UFBoolean.FALSE);
/*  415 */     noteVO.setPk_org(task.getPk_org());
/*      */     
/*  417 */     noteVO.setPk_group(InvocationInfoProxy.getInstance().getGroupId());
/*  418 */     noteVO.setBillid(task.getBillID());
/*  419 */     noteVO.setBillVersionPK(task.getBillversionPK());
/*  420 */     noteVO.setIsmsgbind("N");
/*  421 */     noteVO.setPk_wf_task(task.getTaskPK());
/*  422 */     noteVO.setApprovestatus(Integer.valueOf(task.getStatus()));
/*  423 */     if (task.getTaskType() == WfTaskType.Makebill.getIntValue()) {
/*  424 */       noteVO.setActiontype("MAKEBILL");
/*      */     } else {
/*  426 */       noteVO.setActiontype("Z");
/*      */     }
/*      */     
/*      */ 
/*  430 */     noteVO.setWorkflow_type(Integer.valueOf(iWfType));
/*  431 */     noteVO.setPriority(getMessagePriority(task.getBillType(), task.getBillversionPK()));
/*  432 */     return noteVO;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private Integer getMessagePriority(final String billtype, final String billVersionPK)
/*      */   {
/*  444 */     IRequestDataCacheKey key = new CondStringKey("taskmanagerdmo_get_message_priority", new String[] { billtype, billVersionPK });
/*      */     
/*      */ 
/*      */ 
/*      */ 
/*  449 */     ICacheDataQueryCallback<Integer> callback = new ICacheDataQueryCallback()
/*      */     {
/*      */       public Integer queryData() throws BusinessException {
/*  452 */         List<Billtype2VO> b2voList = PfDataCache.getBillType2Info(billtype, ExtendedClassEnum.MESSAGE_PRIORITY_CALLBACK.getIntValue());
/*      */         
/*  454 */         if (ArrayUtil.isNotNull(b2voList))
/*      */         {
/*  456 */           Billtype2VO b2vo = (Billtype2VO)b2voList.get(0);
/*      */           
/*  458 */           String className = b2vo.getClassname();
/*  459 */           String realBillType = b2vo.getPk_billtype();
/*      */           try
/*      */           {
/*  462 */             IMessagePriorityCallback cb = (IMessagePriorityCallback)PfUtilTools.instantizeObject(realBillType, className);
/*  463 */             return cb.getMessagePriority(billVersionPK);
/*      */           }
/*      */           catch (Exception e) {
/*  466 */             Logger.error(e.getMessage(), e);
/*      */           }
/*      */         }
/*      */         
/*  470 */         return null;
/*      */       }
/*      */     };
/*      */     try
/*      */     {
/*  475 */       return (Integer)PFRequestDataCacheProxy.get(key, callback);
/*      */     } catch (Exception e) {}
/*  477 */     return null;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void insertWorkitemsOfTask(String[] userIds, WFTask task, IWorkitemDistributor... dists)
/*      */     throws Exception
/*      */   {
/*  494 */     PFRequestDataCacheProxy.put(new WFTaskCacheKey(task.getTaskPK()), task);
/*      */     
/*  496 */     ArrayList<WorkflownoteVO> al = new ArrayList();
/*      */     
/*      */ 
/*  499 */     WorkflowPersonDAO wpDao = new WorkflowPersonDAO();
/*  500 */     HashMap<String, Map<String, UFBoolean>> userMap = wpDao.isUserOutAndTransWorks(userIds);
/*      */     
/*  502 */     HashMap<String, String> userLangCode = Pfi18nTools.getLangcodesOfUserFromDb(userIds);
/*      */     
/*  504 */     WorkitemMsgContext context = task.getContext();
/*  505 */     String tempcode = context.getMsgtempcode();
/*  506 */     String pk_org = InvocationInfoProxy.getInstance().getGroupId();
/*  507 */     String[] langcodeary = (String[])userLangCode.values().toArray(new String[0]);
/*  508 */     List<String> langcodeList = new ArrayList();
/*  509 */     if ((langcodeary == null) || (langcodeary.length == 0)) {
/*  510 */       String langcode = DataMultiLangAccessor.getInstance().getDefaultLang().getLangcode();
/*  511 */       langcodeary = new String[] { langcode };
/*      */     }
/*      */     else {
/*  514 */       for (String langcode : langcodeary) {
/*  515 */         if (!langcodeList.contains(langcode)) {
/*  516 */           langcodeList.add(langcode);
/*      */         }
/*      */       }
/*      */     }
/*      */     
/*  521 */     MsgtempletVO[] msgTemVOs = ((IMsgtempletquery)NCLocator.getInstance().lookup(IMsgtempletquery.class)).qryTempletsByCodeLangPk_org(tempcode, pk_org, (String[])langcodeList.toArray(new String[0]));
/*  522 */     HashMap<String, List<MsgtempletVO>> msgTemVOHash = new HashMap();
/*  523 */     List<String> typecodes = new ArrayList();
/*      */     
/*  525 */     if ((msgTemVOs != null) && (msgTemVOs.length > 0)) {
/*  526 */       for (MsgtempletVO vo : msgTemVOs) {
/*  527 */         String langcode = vo.getLangcode();
/*  528 */         List<MsgtempletVO> msgTemVOList = (List)msgTemVOHash.get(langcode);
/*  529 */         if (msgTemVOList == null) {
/*  530 */           msgTemVOList = new ArrayList();
/*  531 */           msgTemVOHash.put(langcode, msgTemVOList);
/*      */         }
/*  533 */         msgTemVOList.add(vo);
/*  534 */         typecodes.add(SQLTransferMeaningUtil.tmsql(vo.getTypecode()));
/*      */       }
/*      */     }
/*      */     
/*  538 */     HashMap<String, MsgtmptypeVO> msgtmptypeVOHash = new HashMap();
/*      */     
/*  540 */     if (typecodes.size() > 0) {
/*      */       try
/*      */       {
/*  543 */         IMsgtempletquery query = (IMsgtempletquery)NCLocator.getInstance().lookup(IMsgtempletquery.class);
/*  544 */         MsgtmptypeVO[] msgTemTypeVOs = query.getTemptypeVOByCodes((String[])typecodes.toArray(new String[0]));
/*  545 */         if ((msgTemTypeVOs != null) && (msgTemTypeVOs.length > 0)) {
/*  546 */           for (MsgtmptypeVO vo : msgTemTypeVOs) {
/*  547 */             String votempcode = vo.getTempcode();
/*  548 */             msgtmptypeVOHash.put(votempcode, vo);
/*      */           }
/*      */         }
/*      */       }
/*      */       catch (BusinessException e) {
/*  553 */         Logger.error(e.getMessage(), e);
/*      */       }
/*      */     }
/*  556 */     HashMap<String, MsgContentCreatorInAppLayer.NcMessageInfo> ncMessageInfoHash = new HashMap();
/*  557 */     for (int i = 0; i < userIds.length; i++)
/*      */     {
/*  559 */       Map<String, UFBoolean> propertyMap = (Map)userMap.get(userIds[i]);
/*  560 */       if (propertyMap == null) {
/*  561 */         propertyMap = new HashMap();
/*      */       }
/*  563 */       String langcode = (String)userLangCode.get(userIds[i]);
/*  564 */       if ((langcode == null) || (langcode.equalsIgnoreCase(""))) {
/*  565 */         langcode = DataMultiLangAccessor.getInstance().getDefaultLang().getLangcode();
/*      */       }
/*  567 */       List<MsgtempletVO> msgTemVOList = (List)msgTemVOHash.get(langcode);
/*  568 */       if (msgTemVOList == null) {
/*  569 */         msgTemVOList = new ArrayList();
/*      */       }
/*  571 */       WorkflownoteVO noteVO = createWorkitem(task, userIds[i], propertyMap, langcode, (MsgtempletVO[])msgTemVOList.toArray(new MsgtempletVO[0]), msgtmptypeVOHash, ncMessageInfoHash);
/*      */       
/*  573 */       al.add(noteVO);
/*      */     }
/*  575 */     ncMessageInfoHash.clear();
/*  576 */     ncMessageInfoHash = null;
/*  577 */     WorkflownoteVO[] notes = (WorkflownoteVO[])al.toArray(new WorkflownoteVO[0]);
/*      */     
/*  579 */     new BaseDAO().insertVOArray(notes);
/*  580 */     PfMessageUtil.sendMessageOfWorknoteBatch(notes);
/*      */     
/*  582 */     if (!ArrayUtil.isNull(dists)) {
/*  583 */       for (IWorkitemDistributor d : dists) {
/*  584 */         d.distributeWorkitem(notes);
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */   private String[] getAgentInfo(WFTask task, String checkman) throws DbException
/*      */   {
/*  591 */     if ((task.getParticipantProcessMode() == 0) || (task.getParticipantProcessMode() == -1))
/*      */     {
/*      */ 
/*      */ 
/*  595 */       return null;
/*      */     }
/*      */     
/*      */ 
/*  599 */     String[] agentInfos = WFAgentTaskTransfer.getAgentInfosAndTransOldTask(checkman, task.getBillType());
/*  600 */     return agentInfos;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private String[] getAgentInfo(WFTask task, String checkman, Map<String, UFBoolean> propertyMap)
/*      */     throws DbException
/*      */   {
/*  614 */     if ((task.getParticipantProcessMode() == 0) || (task.getParticipantProcessMode() == -1))
/*      */     {
/*      */ 
/*      */ 
/*  618 */       return null;
/*      */     }
/*      */     
/*      */ 
/*  622 */     String[] agentInfos = WFAgentTaskTransfer.getAgentInfosAndTransOldTask(checkman, task.getBillType(), propertyMap);
/*  623 */     return agentInfos;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void updateWorkitemByTask(WFTask task)
/*      */     throws DAOException
/*      */   {
/*  679 */     BaseDAO dao = new BaseDAO();
/*  680 */     WorkflownoteVO worknote = (WorkflownoteVO)dao.retrieveByPK(WorkflownoteVO.class, task.getWorknoteVO().getPk_checkflow());
/*      */     
/*  682 */     if (worknote != null) {
/*  683 */       worknote.setPk_billtype(task.getBillType());
/*  684 */       worknote.setBillno(task.getBillNO());
/*  685 */       worknote.setBillid(task.getBillID());
/*  686 */       worknote.setBillVersionPK(task.getBillversionPK());
/*  687 */       worknote.setSenderman(task.getWorknoteVO().getSenderman());
/*  688 */       worknote.setCheckman(task.getOperator());
/*  689 */       worknote.setCiphertext(task.getWorknoteVO().getCiphertext());
/*  690 */       String newIscheck = null;
/*  691 */       if ((task.getTaskType() == WfTaskType.Makebill.getIntValue()) || (task.getApproveResult() == null))
/*      */       {
/*  693 */         if (task.getStatus() == WfTaskOrInstanceStatus.Finished.getIntValue())
/*      */         {
/*  695 */           newIscheck = "Y";
/*      */         } else {
/*  697 */           newIscheck = "N";
/*      */         }
/*      */       }
/*  700 */       else if (task.getApproveResult().equals("Y")) {
/*  701 */         newIscheck = "Y";
/*      */       } else {
/*  703 */         newIscheck = "X";
/*      */       }
/*      */       
/*  706 */       worknote.setIscheck(newIscheck);
/*  707 */       worknote.setChecknote(task.getWorknoteVO().getChecknote());
/*      */       
/*  709 */       worknote.setDealdate(task.getModifyTime());
/*  710 */       worknote.setMessagenote(task.getTopic());
/*  711 */       worknote.setPk_org(task.getPk_org());
/*  712 */       worknote.setPriority(Integer.valueOf(0));
/*  713 */       worknote.setPk_wf_task(task.getTaskPK());
/*  714 */       worknote.setApprovestatus(Integer.valueOf(task.getStatus()));
/*  715 */       worknote.setApproveresult(task.getApproveResult());
/*      */     }
/*  717 */     dao.updateVO(worknote);
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void inefficientWorkitemByPK(String pk_checkflow)
/*      */     throws BusinessException
/*      */   {
/*  730 */     BaseDAO dao = new BaseDAO();
/*  731 */     WorkflownoteVO worknote = (WorkflownoteVO)dao.retrieveByPK(WorkflownoteVO.class, pk_checkflow);
/*      */     
/*  733 */     Collection<WorkflownoteVO> col = new ArrayList();
/*  734 */     col.add(worknote);
/*      */     
/*  736 */     inefficientWorkitems(col);
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void inefficientWorkitemsOfTask(WFTask task)
/*      */     throws BusinessException
/*      */   {
/*  752 */     String sqlCond = "pk_wf_task=?";
/*      */     
/*  754 */     SQLParameter param = new SQLParameter();
/*  755 */     param.addParam(task.getTaskPK());
/*      */     
/*  757 */     BaseDAO dao = new BaseDAO();
/*  758 */     Collection<WorkflownoteVO> colWorknote = dao.retrieveByClause(WorkflownoteVO.class, sqlCond, param);
/*      */     
/*  760 */     if (ArrayUtil.isNull(colWorknote)) {
/*  761 */       return;
/*      */     }
/*      */     
/*  764 */     for (Iterator iterator = colWorknote.iterator(); iterator.hasNext();) {
/*  765 */       WorkflownoteVO workflownoteVO = (WorkflownoteVO)iterator.next();
/*  766 */       workflownoteVO.setIscheck("X");
/*  767 */       workflownoteVO.setApprovestatus(Integer.valueOf(WfTaskOrInstanceStatus.Inefficient.getIntValue()));
/*      */       
/*  769 */       if (workflownoteVO.getPk_checkflow().equals(task.getWorknoteVO().getPk_checkflow()))
/*      */       {
/*      */ 
/*  772 */         workflownoteVO.setDealdate(task.getModifyTime());
/*  773 */         workflownoteVO.setDealtimemillis(String.valueOf(((ITimeService)NCLocator.getInstance().lookup(ITimeService.class)).getTime()));
/*  774 */         workflownoteVO.setApproveresult(task.getApproveResult());
/*      */       }
/*      */     }
/*  777 */     WorkflownoteVO[] aryWorknote = (WorkflownoteVO[])colWorknote.toArray(new WorkflownoteVO[0]);
/*      */     
/*  779 */     dao.updateVOArray(aryWorknote);
/*      */     
/*  781 */     PfMessageUtil.deleteMessagesOfWorknote(aryWorknote);
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void saveOrUpdateTask(WFTask task, boolean isInsert)
/*      */     throws DbException
/*      */   {
/*  795 */     PersistenceManager persist = null;
/*      */     try {
/*  797 */       persist = PersistenceManager.getInstance();
/*      */       
/*  799 */       if (isInsert) {
/*  800 */         persist.insertObject(task, mappingMeta);
/*      */       } else {
/*  802 */         persist.updateObject(task, mappingMeta);
/*      */       }
/*      */     } finally {
/*  805 */       if (persist != null) {
/*  806 */         persist.release();
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public boolean isTaskComplete(String taskPK)
/*      */     throws DbException
/*      */   {
/*  823 */     int checked = countCheckedWorkitemsOfTask(taskPK);
/*  824 */     int all = countAllWorkitemsOfTask(taskPK);
/*      */     
/*  826 */     return checked >= all;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public boolean isTaskCompleteByWFTask(WFTask task)
/*      */     throws DbException
/*      */   {
/*  836 */     String pk_wf_task = task.getTaskPK();
/*  837 */     String finishThreshold = task.getParticipantProcessModeValue();
/*      */     
/*  839 */     if (StringUtil.isEmptyWithTrim(finishThreshold)) {
/*  840 */       finishThreshold = "100%";
/*      */     }
/*      */     
/*  843 */     if (finishThreshold.endsWith("%")) {
/*  844 */       return isTaskCompleteWithPercentModal(pk_wf_task, finishThreshold);
/*      */     }
/*  846 */     return isTaskCompleteWithCountModal(pk_wf_task, finishThreshold);
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public boolean isTaskCompleteWithCountModal(String taskPK, String countOrPercentValue)
/*      */     throws DbException
/*      */   {
/*  859 */     int allNote = countAllWorkitemsOfTask(taskPK);
/*      */     
/*      */ 
/*  862 */     int finishedCount = countCheckedWorkitemsOfTask(taskPK);
/*      */     
/*  864 */     int iCountOrPercentValue = Integer.valueOf(countOrPercentValue).intValue();
/*      */     
/*  866 */     if (iCountOrPercentValue > allNote) {
/*  867 */       iCountOrPercentValue = allNote;
/*      */     }
/*  869 */     if (finishedCount >= iCountOrPercentValue) {
/*  870 */       return true;
/*      */     }
/*  872 */     return false;
/*      */   }
/*      */   
/*      */   private int countBySql(String sql, String pk_wf_task) throws DbException {
/*  876 */     PersistenceManager persist = null;
/*      */     try {
/*  878 */       persist = PersistenceManager.getInstance();
/*  879 */       JdbcSession jdbc = persist.getJdbcSession();
/*      */       
/*  881 */       SQLParameter parameter = new SQLParameter();
/*  882 */       parameter.addParam(pk_wf_task);
/*      */       
/*  884 */       ResultSetProcessor processor = new ColumnProcessor();
/*      */       
/*  886 */       Integer count = (Integer)jdbc.executeQuery(sql, parameter, processor);
/*  887 */       return count.intValue();
/*      */     } finally {
/*  889 */       if (persist != null) {
/*  890 */         persist.release();
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */   public int countPassedWorkitemsOfTask(String pk_wf_task) throws DbException {
/*  896 */     String passedSql = "select count(*) from pub_workflownote where pk_wf_task=? and approvestatus=" + WfTaskOrInstanceStatus.Finished.getIntValue() + " and approveresult='Y' and actiontype not like '%" + "_A" + "'";
/*      */     
/*      */ 
/*      */ 
/*  900 */     return countBySql(passedSql, pk_wf_task);
/*      */   }
/*      */   
/*      */   public int countCheckedWorkitemsOfTask(String pk_wf_task) throws DbException
/*      */   {
/*  905 */     String checkedSql = "select count(*) from pub_workflownote where pk_wf_task=? and approvestatus=" + WfTaskOrInstanceStatus.Finished.getIntValue() + " and actiontype not like '%" + "_A" + "'";
/*      */     
/*      */ 
/*      */ 
/*  909 */     return countBySql(checkedSql, pk_wf_task);
/*      */   }
/*      */   
/*      */   public int countAllWorkitemsOfTask(String pk_wf_task) throws DbException
/*      */   {
/*  914 */     String allSql = "select count(*) from pub_workflownote where pk_wf_task=? and approvestatus not in (" + WfTaskOrInstanceStatus.Inefficient.getIntValue() + ") and actiontype not like '%" + "_A" + "'";
/*      */     
/*      */ 
/*      */ 
/*  918 */     return countBySql(allSql, pk_wf_task);
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public boolean isTaskCompleteWithPercentModal(String taskPK, String countOrPercentValue)
/*      */     throws DbException
/*      */   {
/*  937 */     int allNote = countAllWorkitemsOfTask(taskPK);
/*  938 */     int checkedCount = countCheckedWorkitemsOfTask(taskPK);
/*      */     
/*  940 */     double finishingThreshold = Double.parseDouble(countOrPercentValue.substring(0, countOrPercentValue.length() - 1)) * allNote / 100.0D;
/*      */     
/*      */ 
/*  943 */     if (finishingThreshold > allNote) {
/*  944 */       finishingThreshold = allNote;
/*      */     }
/*  946 */     if (checkedCount >= finishingThreshold) {
/*  947 */       return true;
/*      */     }
/*  949 */     return false;
/*      */   }
/*      */   
/*      */   public String calculateTaskResult(WFTask task) throws DbException {
/*  953 */     String pk_wf_task = task.getTaskPK();
/*  954 */     String finishThreshold = task.getParticipantProcessModeValue();
/*  955 */     String passingThreshold = task.getParticipantProcessPassingThreshold();
/*      */     
/*  957 */     if (isTaskPassed(pk_wf_task, finishThreshold, passingThreshold)) {
/*  958 */       return "Y";
/*      */     }
/*  960 */     return "N";
/*      */   }
/*      */   
/*      */   public boolean isTaskPassed(String pk_wf_task, String finishThreshold, String passingThreshold) throws DbException
/*      */   {
/*  965 */     int all = countAllWorkitemsOfTask(pk_wf_task);
/*  966 */     int passed = countPassedWorkitemsOfTask(pk_wf_task);
/*      */     
/*  968 */     if (StringUtil.isEmptyWithTrim(passingThreshold)) {
/*  969 */       if (StringUtil.isEmptyWithTrim(finishThreshold)) {
/*  970 */         passingThreshold = "100%";
/*      */       } else {
/*  972 */         passingThreshold = finishThreshold;
/*      */       }
/*      */     }
/*      */     
/*  976 */     if (passingThreshold.endsWith("%"))
/*      */     {
/*  978 */       double passingCount = Double.valueOf(passingThreshold.substring(0, passingThreshold.length() - 1)).doubleValue() * all / 100.0D;
/*      */       
/*  980 */       if (passingCount > all) {
/*  981 */         passingCount = all;
/*      */       }
/*      */       
/*  984 */       return passed >= passingCount;
/*      */     }
/*      */     
/*      */ 
/*  988 */     int passingCount = Integer.valueOf(passingThreshold).intValue();
/*  989 */     if (passingCount > all) {
/*  990 */       passingCount = all;
/*      */     }
/*      */     
/*  993 */     return passed >= passingCount;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public int queryWorkitemCountOfTask(String taskPK)
/*      */     throws DbException
/*      */   {
/* 1006 */     String sqlPass = "select count(pk_checkflow) count from pub_workflownote where pk_wf_task = ?";
/* 1007 */     int count = 0;
/* 1008 */     PersistenceManager persist = null;
/*      */     try {
/* 1010 */       persist = PersistenceManager.getInstance();
/* 1011 */       JdbcSession jdbc = persist.getJdbcSession();
/* 1012 */       SQLParameter para = new SQLParameter();
/* 1013 */       para.addParam(taskPK);
/* 1014 */       Object objRet = jdbc.executeQuery(sqlPass, para, new ColumnProcessor(1));
/*      */       
/* 1016 */       if (objRet != null) {
/* 1017 */         count = ((Integer)objRet).intValue();
/*      */       }
/* 1019 */       return count;
/*      */     } finally {
/* 1021 */       if (persist != null) {
/* 1022 */         persist.release();
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public String queryCompletedWorkitemOfTask(String taskPK)
/*      */     throws DbException
/*      */   {
/* 1036 */     String sqlPass = "select pk_checkflow from pub_workflownote where pk_wf_task = ? and approvestatus = " + WfTaskOrInstanceStatus.Finished.getIntValue() + " and actiontype not like '%" + "_A" + "'";
/*      */     
/*      */ 
/*      */ 
/* 1040 */     PersistenceManager persist = null;
/*      */     try {
/* 1042 */       persist = PersistenceManager.getInstance();
/* 1043 */       JdbcSession jdbc = persist.getJdbcSession();
/* 1044 */       SQLParameter para = new SQLParameter();
/* 1045 */       para.addParam(taskPK);
/* 1046 */       Object obj = jdbc.executeQuery(sqlPass, para, new ColumnProcessor(1));
/*      */       
/* 1048 */       return obj == null ? null : String.valueOf(obj);
/*      */     } finally {
/* 1050 */       if (persist != null) {
/* 1051 */         persist.release();
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void deleteOtherWorkitemsExclude(String pk_wf_task, String pk_checkflow)
/*      */     throws BusinessException
/*      */   {
/* 1067 */     BaseDAO dao = new BaseDAO();
/* 1068 */     String whereCondition = "pk_checkflow!= ? and pk_wf_task = ? and actiontype<>'BIZ' and approvestatus in(" + WfTaskOrInstanceStatus.getUnfinishedStatusSet() + ")";
/*      */     
/*      */ 
/*      */ 
/*      */ 
/* 1073 */     SQLParameter para = new SQLParameter();
/* 1074 */     para.addParam(pk_checkflow);
/* 1075 */     para.addParam(pk_wf_task);
/* 1076 */     Collection<WorkflownoteVO> colWorknotevo = dao.retrieveByClause(WorkflownoteVO.class, whereCondition, para);
/*      */     
/* 1078 */     if ((colWorknotevo == null) || (colWorknotevo.size() == 0)) {
/* 1079 */       return;
/*      */     }
/* 1081 */     WorkflownoteVO[] aryWorknote = (WorkflownoteVO[])colWorknotevo.toArray(new WorkflownoteVO[0]);

               
/*      */     
/*      */ 
/* 1084 */     WorknoteManager.deleteWorknoteBatch(aryWorknote);
/*      */   }
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
				
				private void clearTask(String pk_flow) {
					Properties prop = getOaUrlProp();
					String uri=getStr(prop, "oaWebserviceUrl2");//oa的erp待办服务地址
					try {
				        URL url = new URL(uri + "?id="+pk_flow+"");
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
				         Logger.error("==========cleartask-网络连接异常==========" + e.getMessage());
				         Logger.info("==========cleartask-网络连接异常==========" + e.getMessage());
				         Logger.debug("==========cleartask-网络连接异常==========" + e.getMessage());
				     }
				}
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void deleteOtherWorkitemsExceptAddAssign(String pk_wf_task, String pk_checkflow)
/*      */     throws BusinessException
/*      */   {
/* 1099 */     BaseDAO dao = new BaseDAO();
int a = 0;
/* 1100 */     String whereCondition = "pk_checkflow!= ? and pk_wf_task = ? and actiontype<>'BIZ' and approvestatus in(" + WfTaskOrInstanceStatus.getUnfinishedStatusSet() + ")";
/*      */     
/*      */ 
/*      */ 
/*      */ 
/* 1105 */     SQLParameter para = new SQLParameter();
/* 1106 */     para.addParam(pk_checkflow);
/* 1107 */     para.addParam(pk_wf_task);
/* 1108 */     Collection<WorkflownoteVO> colWorknotevo = dao.retrieveByClause(WorkflownoteVO.class, whereCondition, para);
/*      */     
/* 1110 */     if ((colWorknotevo == null) || (colWorknotevo.size() == 0))
/* 1111 */       return;
/* 1112 */     WorkflownoteVO[] aryWorknote = (WorkflownoteVO[])colWorknotevo.toArray(new WorkflownoteVO[0]);
/*      */     
/*      */ 
/* 1115 */     List<WorkflownoteVO> filterNoteVOs = new ArrayList();
/* 1116 */     for (WorkflownoteVO vo : aryWorknote) {
/* 1117 */       if ((!vo.getActiontype().endsWith("_A")) || (WfTaskOrInstanceStatus.Finished.getIntValue() != vo.getApprovestatus().intValue()))
/*      */       {
/*      */ 
/* 1120 */         filterNoteVOs.add(vo);
/*      */       }
/*      */     }
/* 1123 */     WorknoteManager.deleteWorknoteBatch((WorkflownoteVO[])filterNoteVOs.toArray(new WorkflownoteVO[0]));
			   for(int i = 0;i<filterNoteVOs.size();i++){
			    	  clearTask(filterNoteVOs.get(i).getPk_checkflow());
			   }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void inefficientUncompletedWorkitemOfTask(String pk_wf_task)
/*      */     throws DbException, BusinessException
/*      */   {
/* 1138 */     String sqlNoPass = "pk_wf_task=? and approvestatus in (" + WfTaskOrInstanceStatus.getUnfinishedStatusSet() + ")";
/*      */     
/*      */ 
/*      */ 
/* 1142 */     SQLParameter param = new SQLParameter();
/* 1143 */     param.addParam(pk_wf_task);
/*      */     
/* 1145 */     BaseDAO dao = new BaseDAO();
/* 1146 */     Collection<WorkflownoteVO> colWorknote = dao.retrieveByClause(WorkflownoteVO.class, sqlNoPass, param);
/*      */     
/*      */ 
/* 1149 */     inefficientWorkitems(colWorknote);
/*      */   }
/*      */   
/*      */   private void inefficientWorkitems(Collection<WorkflownoteVO> colWorknote) throws BusinessException {
/* 1153 */     if ((colWorknote == null) || (colWorknote.size() == 0)) {
/* 1154 */       return;
/*      */     }
/* 1156 */     for (Iterator<WorkflownoteVO> iterator = colWorknote.iterator(); iterator.hasNext();) {
/* 1157 */       WorkflownoteVO workflownoteVO = (WorkflownoteVO)iterator.next();
/* 1158 */       workflownoteVO.setIscheck("X");
/* 1159 */       workflownoteVO.setApprovestatus(Integer.valueOf(WfTaskOrInstanceStatus.Inefficient.getIntValue()));
/*      */     }
/* 1161 */     WorkflownoteVO[] aryWorknote = (WorkflownoteVO[])colWorknote.toArray(new WorkflownoteVO[0]);
/* 1162 */     new BaseDAO().updateVOArray(aryWorknote);
/*      */     
/* 1164 */     PfMessageUtil.deleteMessagesOfWorknote(aryWorknote);
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public void renewWorkitem(WFTask task)
/*      */     throws BusinessException
/*      */   {
/* 1175 */     WorkflownoteVO worknoteVO = task.getWorknoteVO();
/* 1176 */     WorkflownoteVO noteVO = new WorkflownoteVO();
/* 1177 */     noteVO.setPk_billtype(worknoteVO.getPk_billtype());
/* 1178 */     noteVO.setBillno(worknoteVO.getBillno());
/* 1179 */     noteVO.setSenderman(worknoteVO.getSenderman());
/*      */     
/* 1181 */     noteVO.setIscheck("N");
/* 1182 */     noteVO.setSenddate(((ITimeService)NCLocator.getInstance().lookup(ITimeService.class)).getUFDateTime());
/* 1183 */     noteVO.setCheckman(worknoteVO.getCheckman());
/* 1184 */     noteVO.setMessagenote(worknoteVO.getMessagenote());
/* 1185 */     noteVO.setReceivedeleteflag(UFBoolean.FALSE);
/* 1186 */     noteVO.setPk_org(worknoteVO.getPk_org());
/* 1187 */     noteVO.setPk_group(worknoteVO.getPk_group());
/* 1188 */     noteVO.setBillid(worknoteVO.getBillid());
/* 1189 */     noteVO.setBillVersionPK(worknoteVO.getBillVersionPK());
/* 1190 */     noteVO.setPk_wf_task(worknoteVO.getPk_wf_task());
/* 1191 */     noteVO.setApprovestatus(Integer.valueOf(WfTaskOrInstanceStatus.Started.getIntValue()));
/* 1192 */     noteVO.setActiontype(worknoteVO.getActiontype());
/* 1193 */     noteVO.setWorkflow_type(worknoteVO.getWorkflow_type());
/* 1194 */     noteVO.setIsmsgbind(worknoteVO.getIsmsgbind());
/*      */     
/* 1196 */     constructNCMsg(task, noteVO);
/*      */     
/* 1198 */     WorknoteManager.insertWorknote(noteVO);
/*      */   }
/*      */   
/*      */   private void constructNCMsg(WFTask task, WorkflownoteVO noteVO) throws BusinessException {
/* 1202 */     WorkitemMsgContext context = TaskTopicResolver.getMsgContext(task);
/* 1203 */     NCMessage ncMsg = TaskTopicResolver.constructNCMsg(context);
/* 1204 */     noteVO.setNcMsg(ncMsg);
/*      */   }
/*      */   
/*      */ 
/*      */   public WorkflownoteVO[] queryStartedWorkitemsOfTask(String taskPk)
/*      */     throws DAOException
/*      */   {
/* 1211 */     String cond = "pk_wf_task=? and approvestatus=" + WfTaskOrInstanceStatus.Started.getIntValue();
/*      */     
/*      */ 
/* 1214 */     SQLParameter param = new SQLParameter();
/* 1215 */     param.addParam(taskPk);
/*      */     
/* 1217 */     BaseDAO dao = new BaseDAO();
/* 1218 */     Collection<WorkflownoteVO> colWorkflownote = dao.retrieveByClause(WorkflownoteVO.class, cond, param);
/*      */     
/* 1220 */     if ((colWorkflownote == null) || (colWorkflownote.size() == 0)) {
/* 1221 */       return null;
/*      */     }
/* 1223 */     return (WorkflownoteVO[])colWorkflownote.toArray(new WorkflownoteVO[0]);
/*      */   }
/*      */   
/*      */   public void inefficientWorkitemsByTaskPKs(ArrayList<String> taskPKs)
/*      */     throws BusinessException
/*      */   {
/* 1229 */     final BaseDAO dao = new BaseDAO();
/* 1230 */     final List<WorkflownoteVO> noteList = new ArrayList();
/*      */     
/* 1232 */     new ParameterizedBatchCaller(taskPKs).execute(new ParameterizedBatchCaller.Callback()
/*      */     {
/*      */       public void doInParameter(String inSql, SQLParameter param)
/*      */         throws BusinessException
/*      */       {
/* 1237 */         String cond = "pk_wf_task in " + inSql;
/* 1238 */         Collection<WorkflownoteVO> colWorknote = dao.retrieveByClause(WorkflownoteVO.class, cond, param);
/*      */         
/* 1240 */         noteList.addAll(colWorknote);
/*      */       }
/*      */     });
/*      */     
/* 1244 */     if (ArrayUtil.isNull(noteList)) {
/* 1245 */       return;
/*      */     }
/*      */     
/* 1248 */     inefficientWorkitems(noteList);
/*      */   }
/*      */   
/*      */   public WFTask queryTaskByCondition(String condition) throws DbException
/*      */   {
/* 1253 */     PersistenceManager persist = null;
/*      */     try {
/* 1255 */       persist = PersistenceManager.getInstance();
/* 1256 */       Collection<WFTask> col = persist.retrieveByClause(WFTask.class, mappingMeta, condition);
/*      */       WFTask localWFTask;
/* 1258 */       if (ArrayUtil.isNull(col)) {
/* 1259 */         return null;
/*      */       }
/* 1261 */       return (WFTask)col.iterator().next();
/*      */     }
/*      */     finally
/*      */     {
/* 1265 */       if (persist != null) {
/* 1266 */         persist.release();
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */   public Collection<WFTask> queryTaskCollectionByCondition(String condition) throws DbException
/*      */   {
/* 1273 */     PersistenceManager persist = null;
/*      */     try {
/* 1275 */       persist = PersistenceManager.getInstance();
/* 1276 */       Collection<WFTask> col = persist.retrieveByClause(WFTask.class, mappingMeta, condition);
/* 1277 */       return col;
/*      */     } finally {
/* 1279 */       if (persist != null) {
/* 1280 */         persist.release();
/*      */       }
/*      */     }
/*      */   }
/*      */ }

/* Location:           D:\DEV\DEV-NC6.5-YDSP\nchome\modules\riart\META-INF\lib\riart_riartplatformLevel-1.jar
 * Qualified Name:     nc.bs.pub.taskmanager.TaskManagerDMO
 * Java Class Version: 7 (51.0)
 * JD-Core Version:    0.7.1
 */