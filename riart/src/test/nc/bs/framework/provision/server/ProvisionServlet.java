//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nc.bs.framework.provision.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nc.bs.framework.common.NCLocator;
import nc.bs.framework.provision.PackIndex;
import nc.bs.logging.Log;

public class ProvisionServlet extends HttpServlet {
	private static final Log log = Log.getInstance(ProvisionServlet.class);
	private IProvisionService pvService;

	public ProvisionServlet() {
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		NCLocator locator = NCLocator.getInstance();
		this.pvService = (IProvisionService) locator.lookup(IProvisionService.class);

		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		try {
			pvService.reloadPackIndex();
		} catch (Exception e) {
			log.error("pack index init error(master server not ready?)", e);
			throw new ServletException("pack index init error(master server not ready?)");
		}
		// }
		// }).start();

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doAction(request, response);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doAction(request, response);
	}

	public void doAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		File base = this.pvService.getCodeBase();
		PackIndex packIndex = null;

		try {
			packIndex = this.pvService.getPackIndex();
		} catch (Exception var13) {
			log.error("get pack index error", var13);
			throw new ServletException("get pack index error", var13);
		}

		Command cmd = null;
		String command = request.getParameter("command");
		String location;
		if ("downloadPackIndex".equals(command)) {
			location = request.getParameter("digest");
			cmd = new DPICommand(location, packIndex, this.pvService.isTamperedProtect());
		} else if ("downloadPacks".equals(command)) {
			location = request.getParameter("location");
			String resource = request.getParameter("resource");
			boolean allHint = "true".equals(request.getParameter("all"));
			cmd = new DPCommand(location, resource, allHint, packIndex, base);
		} else if ("dumpAsXml".equals(command)) {
			cmd = new DumpAsXmlCommand(this.pvService);
		} else if ("reloadPackIndex".equals(command)) {
			cmd = new ReloadPICommand(this.pvService);
		} else {
			if (!"rescan".equals(command)) {
				throw new ServletException("not supported command: " + command);
			}

			cmd = new RescanCommand(this.pvService);
		}

		try {
			((Command) cmd).execute(request, response);
		} catch (ServletException var10) {
			log.error("execute command error:" + cmd, var10);
			throw var10;
		} catch (IOException var11) {
			log.error("execute command error:" + cmd, var11);
			throw var11;
		} catch (Exception var12) {
			log.error("execute command error:" + cmd, var12);
			throw new ServletException("execute command error", var12);
		}
	}
}
