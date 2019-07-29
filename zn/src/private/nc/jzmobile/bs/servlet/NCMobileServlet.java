package nc.jzmobile.bs.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.comn.NetStreamContext;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.bs.logging.Logger;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.jzmobile.utils.MobilePropertiesLoader;
import nc.login.bs.LoginVerifyBean;
import nc.login.bs.LoginVerifyException;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

@SuppressWarnings("all")
public class NCMobileServlet extends HttpServlet implements IHttpServletAdaptor {
	private final static String SESSION_ID = "JSESSIONID";
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doAction(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doAction(req, resp);
	}

//	@SuppressWarnings("unchecked")
	@Override
	public void doAction(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Logger.info("==========NCMobileServlet start==========");
		Logger.info("start time ��" + new UFDate().toString());
		InvocationInfoProxy.getInstance().setUserDataSource(new MobilePropertiesLoader().getViewResolverProperties().getDataSource());
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter out = resp.getWriter();
		JSONObject json = getServletJson(req);
		String method = "";
		Map<String, String> map = new HashMap<String, String>();
//		Map<String, Object> returnMap = null;
		Result result = Result.instance();
		try {
			servletVerify(req, resp);
			method = json.getString("method");
			map = JSON.toJavaObject(json, Map.class);
			InvocationInfoProxy.getInstance().setUserId(map.get("userid"));
			String packageName = "nc.jzmobile.handler.";
			String className = "";
			if ("ncLoginCheck".equalsIgnoreCase(method)) {
				className = "NCLoginCheckHandler";
			} else if (method.equalsIgnoreCase("getTask")) // ��ȡ��������	
			{
				className = "GetTaskHandler";
			} else if(method.equalsIgnoreCase("isTaskUnhandle")) //�ж��Ƿ��Ǵ�������
			{
				className = "TaskUnhandleHandler";
			}else if (method.equalsIgnoreCase("getTaskList")) // ��ȡ�����б�
			{
				className = "GetTaskListHandler";
				//sclassName="GetTasksMakerAndOrgHandler";
			} else if (method.equalsIgnoreCase("getTaskListCount")) // ��ȡ�����б�����
			{
				className = "GetTaskListCountHandler";
			} 
			else if (method.equalsIgnoreCase("getBillFileCount")) // ��ȡ��������
			{
				className = "GetBillFileCountHandler";
			} else if (method.equalsIgnoreCase("getBillFileList")) // ��ȡ�����б�
			{
				className = "GetBillFileListHandler";
			} else if (method.equalsIgnoreCase("getWorkflow")) // ��ȡ��������ͼ
			{
				className = "GetWorkflowHandler";
			} else if (method.equalsIgnoreCase("getApproveHistory")) // ��ȡ��������ͼ
			{
				className = "GetApproveHistoryHandler";
			} else if (method.equalsIgnoreCase("getTaskBodyDetail")) // ��ȡ���񵥾ݱ�������
			{
				className = "GetTaskBodyDetailHandler";
			} else if (method.equalsIgnoreCase("dealTask")) // �������� ͬ�⡢��ͬ�⡢����
			{
				className = "DealTaskHandler";
			} else if (method.equalsIgnoreCase("getFileContent")) {
				className = "GetFileContentHandler";
			}
			else if (method.equalsIgnoreCase("getUser")) {
				className = "GetUserHandler";
			}
			else if (method.equalsIgnoreCase("getUserList")) {
				className = "GetUserListHandler";
			}
			else if (method.equalsIgnoreCase("getTaskUserList")) {
				className = "GetTaskUserListHandler";
			}
			else if(method.equalsIgnoreCase("getTasksMakerAndOrg")){//��ȡ��ǰδ�������ݵ�����pk_org �����е����Ƶ���
				className="GetTasksMakerAndOrgHandler";
				
			}else if(method.equalsIgnoreCase("sendMessagewarn")){//������Ϣ���ѹ���
				className="SendMessageWarnHandler";
				
			}else if(method.equalsIgnoreCase("getApproveBillNum")){
				className="GetApproveBillNumHandler";
				
			}else if(method.equalsIgnoreCase("getEditProperties")){//��ȡ���������еĿɱ༭����
				className="GetEditPropertiesHandler";

			}else if(method.equals("getApproveOpt")){
				className = "GetApproveOptHandler"; //��ȡNC���õ�Ĭ����������,���ݵ�ǰ��¼�û�id
				
			}else if(method.equals("getRefData")){
				className = "GetApproveOptHandler"; //�����û�ID ��ȡ��Ӧ�Ļ�����֯Ȩ,�Լ�����ɼ���
				
			} 
			if(StringUtils.isEmpty(className)){
				throw new ServletException("�Ҳ���methodΪ" + method
						+ "�Ķ�ӦHandler�����࣬��ȷ�Ϻ����ԣ�");
			}
			INCMobileServletHandler handler = null;
			handler = (INCMobileServletHandler) Class
					.forName(packageName + className).newInstance();
					
			result = handler.handler(map);
		} catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
//			returnMap = createOutValue("1", e.getMessage(), "");
		}
		String resultStr = "";
		if (result != null) {
			resultStr = JSON.toJSONString(result,SerializerFeature.WriteMapNullValue,SerializerFeature.WriteNullStringAsEmpty,SerializerFeature.WriteNullNumberAsZero);
			try {
				String str = new String(resultStr.toString().getBytes("utf-8"), "ISO-8859-1");
				out.println(str);
				out.flush();
				out.close();
			} catch (JSONException e) {
				Logger.error("���ýӿ�ʧ��");
			}
			out.println(result);
		} else {
			Logger.error("���ýӿ�ʧ��");
		}
		out.flush();
		out.close();
		Logger.info("==========NCMobileServlet end==========");
		Logger.info("end time : " + new UFDate().toString());
	}

	private JSONObject getServletJson(HttpServletRequest req) {
		JSONObject json = null;
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(req.getInputStream(),
					"utf-8"));
			StringBuffer sb = new StringBuffer("");
			String temp;
			while ((temp = br.readLine()) != null) {
				sb.append(temp);
			}
			br.close();
			String acceptjson = sb.toString();
			if (acceptjson != "") {
				json = JSON.parseObject(acceptjson);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json;
	}

	private void servletVerify(HttpServletRequest req, HttpServletResponse resp) throws IOException, LoginVerifyException, BusinessException {
		String jSessionId = null;
		if ( req.getCookies() == null ) {
			jSessionId = req.getSession().getId();
		} else {
			for ( Cookie cookie : req.getCookies() ) {
				if ( cookie.getName().equals(SESSION_ID) ) {
					jSessionId = cookie.getValue();
					break;
				}
			}
		}
		
		if ( jSessionId == null || jSessionId.length() == 0 ) {
			Cookie cookie = new Cookie(SESSION_ID, req.getSession().getId());
			resp.addCookie(cookie);
		}
		
		Object objToken = req.getSession().getAttribute("token");
		byte[] token = null;

		if ( objToken == null ) {
			token = createSecurityToken(req.getSession().getId());
			req.getSession().setAttribute("token", token);
		} else {
			token = (byte[])objToken;
			NetStreamContext.setToken(token);
		}
		
	}
	
	private byte[] createSecurityToken(String jSessionId) throws UnsupportedEncodingException, LoginVerifyException, BusinessException {
		LoginVerifyBean verifyBean = new LoginVerifyBean("0");
		verifyBean.setStaticPWDVerify(false);

		//ע����֤���
		ISecurityTokenCallback sc = NCLocator.getInstance().lookup(ISecurityTokenCallback.class);
		return sc.token(verifyBean.getSysID().getBytes("UTF-8"), jSessionId.getBytes("UTF-8"));
	}

	public static LinkedHashMap<String, Object> createOutValue(String flag,
			String desc, Object outObj) {
		LinkedHashMap<String, Object> outValue = new LinkedHashMap<String, Object>();
		outValue.put("flag", flag);
		outValue.put("desc", desc);
		outValue.put("data", outObj);
		return outValue;
	}
}
