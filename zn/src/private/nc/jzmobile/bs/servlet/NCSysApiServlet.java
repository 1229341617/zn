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
import org.docx4j.model.datastorage.XPathEnhancerParser.main_return;

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

/**
 * NC 系统间数据访问接口
 * @author songlx
 *
 */
@SuppressWarnings("all")
public class NCSysApiServlet extends HttpServlet implements
		IHttpServletAdaptor {
	private final static String SESSION_ID = "JSESSIONID";

	static Map<String, String> map = new HashMap<String, String>();

	/*
	 * 注册handler
	 */
	static {
		// * 批量保存
		map.put("BatchSaveBill", "BatchSaveBillHandler");

	}
	

	// @SuppressWarnings("unchecked")
	@Override
	public void doAction(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Logger.error("==========NCSysApiServlet start==========");
		Logger.error("start time ：" + new UFDate().toString());
		InvocationInfoProxy.getInstance().setUserDataSource(
				new MobilePropertiesLoader().getViewResolverProperties()
						.getDataSource());
		Logger.error("==========NCSysApiServlet InvocationInfoProxy1==========");
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter out = resp.getWriter();
		JSONObject json = getServletJson(req);
		String method = "";
		Map<String, String> map = new HashMap<String, String>();
		// Map<String, Object> returnMap = null;
		Result result = Result.instance();
		try {
			Logger.error("==========NCSysApiServlet InvocationInfoProxy  try==========");
			servletVerify(req, resp);
			method = json.getString("method");
			map = JSON.toJavaObject(json, Map.class);
			InvocationInfoProxy.getInstance().setUserId(map.get("userid"));
			String packageName = "nc.jzmobile.bill.handler.";
			String className = getHandler(method);
			if(StringUtils.isEmpty(className)){
				throw new ServletException("找不到method为" + method
						+ "的对应Handler处理类，请确认后重试！");
			}
			Logger.error("==========NCSysApiServlet InvocationInfoProxy  handler==========");
			INCMobileServletHandler handler = (INCMobileServletHandler) Class
					.forName(packageName + className).newInstance();
			result = handler.handler(map);
		} catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
			// returnMap = createOutValue("1", e.getMessage(), "");
		}

		if (result != null) {
			String resultStr = JSON.toJSONString(result);
			try {
				String str = new String(resultStr.toString().getBytes("utf-8"),
						"ISO-8859-1");
				out.println(str);
				out.flush();
				out.close();
			} catch (JSONException e) {
				Logger.error("调用接口失败");
			}
			out.println(result);
		} else {
			Logger.error("调用接口失败");
		}
		out.flush();
		out.close();
		Logger.error("==========NCSysApiServlet end==========");
		Logger.error("end time : " + new UFDate().toString());
	}
	
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

	private void servletVerify(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, LoginVerifyException, BusinessException {
		String jSessionId = null;
		if (req.getCookies() == null) {
			jSessionId = req.getSession().getId();
		} else {
			for (Cookie cookie : req.getCookies()) {
				if (cookie.getName().equals(SESSION_ID)) {
					jSessionId = cookie.getValue();
					break;
				}
			}
		}

		if (jSessionId == null || jSessionId.length() == 0) {
			Cookie cookie = new Cookie(SESSION_ID, req.getSession().getId());
			resp.addCookie(cookie);
		}

		Object objToken = req.getSession().getAttribute("token");
		byte[] token = null;

		if (objToken == null) {
			token = createSecurityToken(req.getSession().getId());
			req.getSession().setAttribute("token", token);
		} else {
			token = (byte[]) objToken;
			NetStreamContext.setToken(token);
		}

	}

	private byte[] createSecurityToken(String jSessionId)
			throws UnsupportedEncodingException, LoginVerifyException,
			BusinessException {
		LoginVerifyBean verifyBean = new LoginVerifyBean("0");
		verifyBean.setStaticPWDVerify(false);

		// 注册认证结果
		ISecurityTokenCallback sc = NCLocator.getInstance().lookup(
				ISecurityTokenCallback.class);
		return sc.token(verifyBean.getSysID().getBytes("UTF-8"),
				jSessionId.getBytes("UTF-8"));
	}

	public static LinkedHashMap<String, Object> createOutValue(String flag,
			String desc, Object outObj) {
		LinkedHashMap<String, Object> outValue = new LinkedHashMap<String, Object>();
		outValue.put("flag", flag);
		outValue.put("desc", desc);
		outValue.put("data", outObj);
		return outValue;
	}


	public static String getHandler(String methodKey) {
		return map.get(methodKey);
	}
	
}
