package ufmiddle.start.tomcat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLStreamReader;

import nc.bs.framework.aop.AspectManager;
import nc.bs.framework.aop.AspectMeta;
import nc.bs.framework.aop.PatternType;
import nc.bs.framework.core.AbstractMeta;
import nc.bs.framework.core.Container;
import nc.bs.framework.core.Entry;
import nc.bs.framework.core.ExtensionProcessor;
import nc.bs.framework.core.FactoryDesc;
import nc.bs.framework.core.GenericContainer;
import nc.bs.framework.core.Meta;
import nc.bs.framework.core.PrintExtensionProcessor;
import nc.bs.framework.core.common.CreateInfoBag;
import nc.bs.framework.core.common.FactoryParameter;
import nc.bs.framework.core.common.PropertyHolder;
import nc.bs.framework.core.common.RudeBean;
import nc.bs.framework.core.common.RudeList;
import nc.bs.framework.core.common.RudeMap;
import nc.bs.framework.core.common.RudeRef;
import nc.bs.framework.core.common.RudeSet;
import nc.bs.framework.core.common.RudeValue;
import nc.bs.framework.instantiator.CtorInstantiator;
import nc.bs.framework.instantiator.ObjectFactoryInstantiator;
import nc.bs.framework.instantiator.StaticFactoryInstantiator;
import nc.bs.framework.loading.BasicClassLoaderRepository;
import nc.bs.framework.loading.ModulePrivateClassLoader;
import nc.bs.framework.server.ComponentMetaImpl;
import nc.bs.framework.server.Module;
import nc.bs.framework.server.deploy.DeployUtil;
import nc.bs.framework.server.deploy.TempModuleAttribute;
import nc.bs.framework.server.util.MiscUtil;
import nc.bs.framework.server.util.ServerConstants;
import nc.bs.logging.Logger;
import nc.bs.logging.NCSysOutWrapper;
import nc.bs.mw.fm.ServiceManagerDaemon;
import nc.vo.jcom.io.FileFilterFactory;
import nc.vo.jcom.io.IOUtil;
import nc.vo.jcom.lang.StringUtil;

import org.granite.lang.util.PathPattern;
import org.granite.stax.ElementXMLStreamReader;
import org.granite.stax.Staxs;
import org.granite.xv.Arguments;
import org.granite.xv.HandleException;
import org.granite.xv.Option;
import org.granite.xv.SavePoint;
import org.granite.xv.SavePointHandle;
import org.granite.xv.Visitor;
import org.granite.xv.VisitorSupport;
import org.granite.xv.Xvs;
import org.granite.xv.namerule.MapperNameRule;
import org.granite.xv.visitor.AddConstantVisitor;
import org.granite.xv.visitor.CallVisitor;
import org.granite.xv.visitor.CreateWithPreVisitor;
import org.granite.xv.visitor.CreatorVisitor;
import org.granite.xv.visitor.RandomWireVisitor;
import org.granite.xv.visitor.SetterVisitor;
import org.granite.xv.visitor.WireCurrentVisitor;
import org.granite.xv.visitor.WirePreVisitor;

public class StartDirectServer implements Runnable {

	public static void main(String[] args) {
		System.setProperty("nc.run.side", "server");
		if (args.length > 0) {
			if (args[0].endsWith(".xml")) {
				System.setProperty("nc.server.prop", args[0]);
			}
			if (!"start".equals(args[0]) && !"stop".equals(args[0])) {
				args = new String[] { "start" };
			}
		}
		new Thread(new StartDirectServer()).start();
		ServiceManagerDaemon.main(args);
	}

	public static void println(String s) {
		PrintStream pw = System.out;
		if (pw instanceof NCSysOutWrapper) {
			pw = ((NCSysOutWrapper) pw).getSysStream();
		} else {
			System.setOut(new NCSysOutWrapper(pw, true));
			System.setErr(new NCSysOutWrapper(System.err, false));
		}
		pw.println(s);
	}

	@Override
	public void run() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		prepare();
		scanModules();
	}

	private PathPattern<SavePointHandle> sphPP;
	private PathPattern<Visitor> visitorPP;
	private PathPattern<Visitor> mvPP;

	private void scanModules() {
		String ncHome = System.getProperty("nc.server.location", System.getProperty("user.dir"));
		List<File> allModuleList = new ArrayList<>();
		MiscUtil.computeModuleDirs(new File(ncHome + "\\modules"), allModuleList, new HashSet<String>());
		List<URL> allModules = new ArrayList<>(allModuleList.size());
		for (int i = 0; i < allModuleList.size(); i++) {
			try {
				allModules.add(((File) allModuleList.get(i)).toURI().toURL());
			} catch (MalformedURLException e) {
			}
		}
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime()
				.availableProcessors() * 2);
		for (URL url : allModules) {
			executor.execute(new ContainerDeployCallable(url));
		}
		executor.shutdown();
	}

	public class ContainerDeployCallable implements Runnable {
		private final URL url;

		public ContainerDeployCallable(URL url) {
			this.url = url;
		}

		@Override
		public void run() {
			File md = new File(url.getFile());
			BasicClassLoaderRepository basicClassLoaderRepository = new BasicClassLoaderRepository();
			ClassLoader loader = DeployUtil.newModuleClassLoader(basicClassLoaderRepository, this.getClass()
					.getClassLoader(), md);
			Module module = new Module(null, null, loader, md.getName(), url);
			parseModule(md, module);
		}
	}

	private Module parseModule(File md, Module module) {
		File[] cfs = getModuleConfigFiles(md);

		Xvs mxvs = newModuleXvs();
		InputStream in = null;
		try {
			in = getInputStream(cfs[0]);
			mxvs.setId(cfs[0].toString());
			mxvs.parse(module, in);
		} catch (Exception e) {

		} finally {
			IOUtil.close(in);
		}
		for (int i = 0; i < cfs.length; i++) {
			try {
				Xvs cmntXvs = newCmntXvs();
				in = getInputStream(cfs[i]);
				cmntXvs.setId(cfs[i].toString());
				cmntXvs.parse(module, in);
			} catch (Exception e) {

			} finally {
				IOUtil.close(in);
			}
		}

		return module;
	}

	public Xvs newModuleXvs() {
		Xvs xos = new Xvs();
		xos.setVisitorPathPattern(this.mvPP);
		return xos;
	}

	public Xvs newCmntXvs() {
		prepare();
		Xvs xos = new Xvs();
		xos.setSphPathPattern(this.sphPP);
		xos.setVisitorPathPattern(this.visitorPP);

		return xos;
	}

	private InputStream getInputStream(File f) throws Exception {
		FileInputStream fin = new FileInputStream(f);
		BufferedInputStream bin = new BufferedInputStream(fin);
		return bin;
	}

	public File[] getModuleConfigFiles(File md) {
		File metadir = new File(md, "META-INF");
		File cd = new File(metadir, ServerConstants.EXTENSION);
		File[] extcfgs = cd.exists() ? cd.listFiles(this.moduleFileFilter) : new File[0];

		sortByPriority(extcfgs);
		cd = new File(metadir, ServerConstants.HYEXT);
		File[] hyextcfgs = cd.exists() ? cd.listFiles(this.moduleFileFilter) : new File[0];

		sortByPriority(hyextcfgs);

		cd = metadir;
		File[] ocs = cd.exists() ? cd.listFiles(this.moduleFileFilter) : new File[0];
		sortByPriority(ocs);
		File modulexml = new File(cd, "module.xml");
		File[] all = new File[ocs.length + 1 + extcfgs.length + hyextcfgs.length];

		all[0] = modulexml;
		System.arraycopy(extcfgs, 0, all, 1, extcfgs.length);
		System.arraycopy(hyextcfgs, 0, all, 1 + extcfgs.length, hyextcfgs.length);

		System.arraycopy(ocs, 0, all, 1 + extcfgs.length + hyextcfgs.length, ocs.length);

		return all;
	}

	public void sortByPriority(File[] files) {
		FileInputStream fin = null;
		PF[] ps = new PF[files.length];
		for (int i = 0; i < files.length; i++) {
			int priority = 100;
			try {
				fin = new FileInputStream(files[i]);
				XMLStreamReader r = Staxs.getXMLInputFactory().createXMLStreamReader(fin);

				r = Staxs.createFilteredReader(r, new StreamFilter() {
					public boolean accept(XMLStreamReader reader) {
						return reader.isStartElement();
					}
				});
				while (r.hasNext()) {
					if ((r.isStartElement()) && ("module".equals(r.getLocalName()))) {
						for (int j = 0; j < r.getAttributeCount(); j++) {
							if ("priority".equals(r.getAttributeLocalName(j))) {
								priority = Integer.parseInt(r.getAttributeValue(j));
							}
						}
						break;
					}
					r.next();
				}
			} catch (Exception e) {
			} finally {
				if (fin != null) {
					try {
						fin.close();
					} catch (IOException e) {
					}
					fin = null;
				}
			}
			ps[i] = new PF(files[i], priority);
		}
		Arrays.sort(ps);
		for (int i = 0; i < ps.length; i++) {
			files[i] = ps[i].f;
		}
	}

	static class PF implements Comparable<PF> {
		int priority;
		File f;

		public PF(File f, int priority) {
			this.priority = priority;
			this.f = f;
		}

		public int compareTo(PF o) {
			return this.priority - o.priority;
		}
	}

	private PathPattern<Visitor> prepare(boolean v6x) {
		PathPattern<Visitor> visitorPP = new PathPattern();
		if (v6x) {
			visitorPP.add("module", new CreatorVisitor(TempModuleAttribute.class));

			visitorPP.add("module", new SetterVisitor());
		}
		visitorPP.add("module/rest/resource", new VisitorSupport() {
			public void startElement(XMLStreamReader r, Xvs s) throws Exception {
				String resourceClassName = "";
				String exInfo = "";
				for (int i = 0; i < r.getAttributeCount(); i++) {
					if (r.getAttributeLocalName(i).equals("classname")) {
						resourceClassName = r.getAttributeValue(i);
					} else if (r.getAttributeLocalName(i).equals("exinfo")) {
						exInfo = r.getAttributeValue(i);
					}
				}
				GenericContainer<?> c = (GenericContainer) s.peek(-1);

				String moduleName = c.getName();
				try {
					Class deployerClass = Class.forName("uap.ws.rest.deploy.RestExtensionManager");

					Object obj = deployerClass.newInstance();
					Class[] classes = { String.class, String.class };
					Method m = deployerClass.getMethod("processAtDeploy", classes);

					String[] args = { moduleName, resourceClassName };
					m.invoke(obj, args);
				} catch (Exception e) {
					Logger.error("rest deploy error", e);
				}
			}

			public void endElement(XMLStreamReader r, Xvs s) throws Exception {
			}
		});
		visitorPP.add("module/aops/aspect", new VisitorSupport() {
			public void startElement(XMLStreamReader r, Xvs s) throws Exception {
				String ac = null;
				String scope = null;
				PatternType pt = null;
				Map<String, String> attrMap = null;
				for (int i = 0; i < r.getAttributeCount(); i++) {
					if (r.getAttributeLocalName(i).equals("class")) {
						ac = r.getAttributeValue(i);
					} else if (r.getAttributeLocalName(i).equals("component")) {
						scope = r.getAttributeValue(i);
					} else if (r.getAttributeLocalName(i).equals("patternType")) {
						String patternType = r.getAttributeValue(i);
						if ("regex".equals(patternType)) {
							pt = PatternType.regex;
						} else if ("method".equals(patternType)) {
							pt = PatternType.method;
						} else {
							pt = PatternType.ant;
						}
					} else if (r.getAttributeLocalName(i).equals("compAttr")) {
						String compAttr = r.getAttributeValue(i);
						if (!StringUtil.isEmptyWithTrim(compAttr)) {
							String[] compAttrs = compAttr.split(";");
							attrMap = new HashMap();
							for (String attr : compAttrs) {
								String[] tags = attr.split("=");
								attrMap.put(tags[0].trim(), tags[1].trim());
							}
						}
					}
				}
				if (pt == null) {
					pt = PatternType.ant;
				}
				if (ac != null) {
					GenericContainer<?> c = (GenericContainer) s.peek(-1);
					Class<?> clazz = c.getClassLoader().loadClass(ac);
					AspectMeta am = new AspectMeta(clazz);
					if (scope != null) {
						am.addComponent(scope, pt);
					}
					am.setDefaultPatternType(pt);
					am.setCompAttrMap(attrMap);
					s.push(am);
					((AspectManager) c.getExtension(AspectManager.class)).addAspectMeta(am);
				}
			}

			public void endElement(XMLStreamReader r, Xvs s) throws Exception {
				if ((s.peek() instanceof AspectMeta)) {
					s.pop();
				}
			}
		});
		visitorPP.add("module/aops/aspect/component", new VisitorSupport() {
			public void startElement(XMLStreamReader r, Xvs s) {
				PatternType pt = PatternType.ant;
				for (int i = 0; i < r.getAttributeCount(); i++) {
					if (r.getAttributeLocalName(i).equals("patternType")) {
						String patternType = r.getAttributeValue(i);
						if ("regex".equals(patternType)) {
							pt = PatternType.regex;
							break;
						}
						if ("method".equals(patternType)) {
							pt = PatternType.method;
							break;
						}
						pt = PatternType.ant;

						break;
					}
				}
				s.push(pt);
			}

			public void endElement(XMLStreamReader r, Xvs s) throws Exception {
				PatternType pt = (PatternType) s.pop();
				if ((s.peek() instanceof AspectMeta)) {
					AspectMeta am = (AspectMeta) s.peek();
					am.addComponent(s.getText().trim(), pt);
				}
			}
		});
		visitorPP.add("module/public", new AddConstantVisitor(Boolean.valueOf(true)));
		visitorPP.add("module/private", new AddConstantVisitor(Boolean.valueOf(false)));
		visitorPP.add("module/*/component", new CreateWithPreVisitor(ComponentMetaImpl.class, -1));

		visitorPP.add("module/*/component", new WireCurrentVisitor("public"));
		MapperNameRule mnr = new MapperNameRule();
		mnr.alias("tx", "txAttribute");
		visitorPP.add("module/*/component", new SetterVisitor(Option.ATTRIBUTE, mnr));
		if (v6x) {
			visitorPP.add("module/*/component", new VisitorSupport() {
				public void endElement(XMLStreamReader r, Xvs s) throws Exception {
					super.endElement(r, s);
				}

				public void startElement(XMLStreamReader r, Xvs s) throws Exception {
					ComponentMetaImpl metaImpl = (ComponentMetaImpl) s.peek();
					TempModuleAttribute tma = (TempModuleAttribute) s.peek(-2);
					Container c = (Container) s.peek(-1);
					if (tma.getName() != null) {
						metaImpl.setEjbName(tma.getName());
					} else {
						metaImpl.setEjbName(c.getName());
					}
					if (((tma.getFramework() != null) && (tma.getFramework().booleanValue()))
							|| ((tma.getFramework() == null) && (c.isFramework()))) {
						String cluster = metaImpl.getCluster();
						if ((cluster == null) || ("NONE".equals(cluster)) || ("NORMAL".equalsIgnoreCase(cluster))) {
							metaImpl.setCluster("FRAMEWORK");
						} else if ("SP".equals(cluster)) {
							metaImpl.setCluster("MASTER");
						} else if ("NOSP".equals(cluster)) {
							metaImpl.setCluster("NOMASTER");
						}
					}
				}
			});
		}
		visitorPP.add("module/*/component", new RandomWireVisitor("register", true, false, -1, 0));

		visitorPP.add("module/*/component", new VisitorSupport() {
			public void endElement(XMLStreamReader r, Xvs s) throws Exception {
				AbstractMeta meta = (AbstractMeta) s.peek();
				for (ExtensionProcessor ep : meta.getExtensionProcessors()) {
					ep.processAtDeployEnd(meta.getContainer(), meta);
				}
			}
		});
		visitorPP.add("module/*/component", new CallVisitor("validate", false));

		visitorPP.add("module/*/component/description", new SetterVisitor(Option.TEXT));

		visitorPP.add("module/*/component/extension", new VisitorSupport() {
			public void endElement(XMLStreamReader r, Xvs s) throws Exception {
				ExtensionProcessor ep = (ExtensionProcessor) s.pop();
				AbstractMeta meta = (AbstractMeta) s.peek();
				meta.addExtensionProcessor(ep);
			}

			public void startElement(XMLStreamReader r, Xvs s) throws Exception {
				int count = r.getAttributeCount();
				for (int i = 0; i < count; i++) {
					if (r.getAttributeLocalName(i).equals("class")) {
						GenericContainer<?> module = (GenericContainer) s.peek(-1);
						try {
							Class<?> clazz = module.getClassLoader().loadClass(r.getAttributeValue(i).trim());
							if (ExtensionProcessor.class.isAssignableFrom(clazz)) {
								ExtensionProcessor ep = (ExtensionProcessor) clazz.newInstance();

								ep.processAtDeploy((GenericContainer) s.peek(-1), (Meta) s.peek(),
										new ElementXMLStreamReader(r, false));

								s.push(ep);
								return;
							}
						} catch (Throwable thr) {

							break;
						}
					}
				}
				ExtensionProcessor ep = new PrintExtensionProcessor();
				ep.processAtDeploy((GenericContainer) s.peek(-1), (Meta) s.peek(), r);

				s.push(ep);
			}
		});
		visitorPP.add("module/*/component/interface", new VisitorSupport() {
			public void endElement(XMLStreamReader r, Xvs s) throws Exception {
				String txt = s.getText().trim();
				GenericContainer<?> module = (GenericContainer) s.peek(-1);
				int endPos = txt.indexOf('@');
				Class<?> clazz = module.getClassLoader().loadClass(endPos < 0 ? txt : txt.substring(0, endPos));

				AbstractMeta meta = (AbstractMeta) s.peek();

				meta.addInterface(clazz);
				if ((meta.getName() == null) || (meta.isSupportAlias())) {
					meta.addAlia(txt);
				}
			}
		});
		visitorPP.add("module/*/component/implementation", new VisitorSupport() {
			public void endElement(XMLStreamReader r, Xvs s) throws Exception {
				String txt = s.getText().trim();
				GenericContainer<?> module = (GenericContainer) s.peek(-1);

				AbstractMeta meta = (AbstractMeta) s.peek();

				Class<?> clazz = module.getClassLoader().loadClass(txt);
				meta.setImplementation(clazz);
				CtorInstantiator inst = new CtorInstantiator(clazz);

				meta.setRawInstantiator(inst);
			}
		});
		visitorPP.add("module/*/component/**/parameter|property/**/bean", new VisitorSupport() {
			public void endElement(XMLStreamReader r, Xvs s) throws Exception {
				Object v = s.pop();
				Object obj = s.peek();
				if ((v instanceof RudeRef)) {
					((RudeRef) v).setRef(s.getText());
				} else if ((v instanceof RudeValue)) {
					((RudeValue) v).setValue(s.getText());
				}
				if ((obj instanceof FactoryParameter)) {
					((FactoryParameter) obj).setValue(v);
				} else if ((obj instanceof RudeList)) {
					((RudeList) obj).add(v);
				} else if ((obj instanceof RudeSet)) {
					((RudeSet) obj).add(v);
				} else if ((obj instanceof Entry)) {
					((Entry) obj).setObject(v);
				} else if ((obj instanceof PropertyHolder)) {
					((PropertyHolder) obj).setValue(v);
				}
			}

			public void startElement(XMLStreamReader r, Xvs s) throws Exception {
				RudeBean rudeBean = new RudeBean();
				s.push(rudeBean);
				String bcName = r.getAttributeValue(null, "class");
				GenericContainer<?> module = (GenericContainer) s.peek(-1);

				rudeBean.setContainer(module);
				if (bcName != null) {
					Class<?> clazz = module.getClassLoader().loadClass(bcName);

					CtorInstantiator inst = new CtorInstantiator(clazz);
					rudeBean.setRawInstantiator(inst);
				}
			}
		});
		visitorPP.add("module/*/component/**/construct", new CreatorVisitor(Arguments.class));

		visitorPP
				.add("module/*/component/**/construct", new WirePreVisitor("rawInstantiator.parameters", false, false));

		visitorPP.add("module/*/component/**/parameter", new CreatorVisitor(FactoryParameter.class));

		visitorPP.add("module/*/component/**/parameter", new SetterVisitor(Option.ATTRIBUTE));

		visitorPP.add("module/*/component/**/parameter", new WirePreVisitor("add", true, false));

		visitorPP.add("module/*/component/**/parameter|property/**/value", new WireRudeVisitor(RudeValue.class));

		visitorPP.add("module/*/component/**/parameter|property/**/list", new WireRudeVisitor(RudeList.class));

		visitorPP.add("module/*/component/**/parameter|property/**/map", new WireRudeVisitor(RudeMap.class));

		visitorPP.add("module/*/component/**/parameter|property/**/map/entry", new CreatorVisitor(Entry.class));

		visitorPP.add("module/*/component/**/parameter|property/**/map/entry", new SetterVisitor(Option.ATTRIBUTE));

		visitorPP.add("module/*/component/**/parameter|property/**/map/entry", new VisitorSupport() {
			public void endElement(XMLStreamReader r, Xvs s) {
				Entry entry = (Entry) s.peek();
				RudeMap map = (RudeMap) s.peek(1);
				map.put(entry.getKey(), entry.getObject());
			}
		});
		visitorPP.add("module/*/component/**/parameter|property/**/set", new WireRudeVisitor(RudeSet.class));

		visitorPP.add("module/*/component/**/parameter|property/**/ref", new WireRudeVisitor(RudeRef.class));

		visitorPP.add("module/*/component/**/parameter|property/**/props", new WireRudeVisitor(Properties.class));

		visitorPP.add("module/*/component/**/parameter|property/**/props/prop", new VisitorSupport() {
			public void startElement(XMLStreamReader r, Xvs s) {
				Entry entry = new Entry();
				int count = r.getAttributeCount();
				for (int i = 0; i < count; i++) {
					String an = r.getAttributeLocalName(i);
					if ("key".equals(an)) {
						entry.setKey(r.getAttributeValue(i));
					}
				}
				s.push(entry);
			}

			public void endElement(XMLStreamReader r, Xvs s) {
				Entry entry = (Entry) s.pop();
				entry.setObject(s.getText());
				Properties props = (Properties) s.peek();
				props.setProperty(entry.getKey(), (String) entry.getObject());
			}
		});
		visitorPP.add("module/*/component/**/property", new VisitorSupport() {
			public void startElement(XMLStreamReader r, Xvs s) {
				PropertyHolder property = new PropertyHolder();
				int count = r.getAttributeCount();
				for (int i = 0; i < count; i++) {
					String an = r.getAttributeLocalName(i);
					if ("name".equals(an)) {
						property.setName(r.getAttributeValue(i));
					}
				}
				s.push(property);
			}

			public void endElement(XMLStreamReader r, Xvs s) {
				PropertyHolder property = (PropertyHolder) s.pop();
				CreateInfoBag meta = (CreateInfoBag) s.peek();
				meta.addPropertyHolder(property);
			}
		});
		visitorPP.add("module/*/component/factory-method", new VisitorSupport() {
			public void startElement(XMLStreamReader r, Xvs s) {
				int count = r.getAttributeCount();
				String mthdName = null;
				for (int i = 0; i < count; i++) {
					String an = r.getAttributeLocalName(i);
					if ("method".equals(an)) {
						mthdName = r.getAttributeValue(i);
					}
				}
				FactoryDesc fm = new FactoryDesc();
				fm.setMethodName(mthdName);
				s.push(fm);
				s.push(new Arguments());
			}

			public void endElement(XMLStreamReader r, Xvs s) {
				GenericContainer<?> module = (GenericContainer) s.peek(-1);

				Arguments<FactoryParameter> params = (Arguments) s.pop();

				FactoryDesc fd = (FactoryDesc) s.pop();
				fd.setParameters((FactoryParameter[]) params.toArray(new FactoryParameter[params.size()]));
				if ((fd.getObject() instanceof RudeValue)) {
					String cn = ((RudeValue) fd.getObject()).getValue();
					cn = cn.trim();
					try {
						StaticFactoryInstantiator inst = new StaticFactoryInstantiator(module.getClassLoader()
								.loadClass(cn), fd.getMethodName(), fd.getParameters());

						AbstractMeta meta = (AbstractMeta) s.peek();
						meta.setRawInstantiator(inst);
					} catch (ClassNotFoundException e) {
					}
				} else {
					ObjectFactoryInstantiator inst = new ObjectFactoryInstantiator(fd.getObject(), fd.getMethodName(),
							fd.getParameters());

					AbstractMeta meta = (AbstractMeta) s.peek();
					meta.setRawInstantiator(inst);
				}
			}
		});
		visitorPP.add("module/*/component/factory-method/provider/value", new VisitorSupport() {
			public void endElement(XMLStreamReader r, Xvs s) {
				RudeValue v = new RudeValue();

				v.setValue(s.getText());

				FactoryDesc fm = (FactoryDesc) s.peek(1);
				fm.setObject(v);
			}
		});
		visitorPP.add("module/*/component/factory-method/provider/ref", new VisitorSupport() {
			public void endElement(XMLStreamReader r, Xvs s) {
				RudeRef ref = new RudeRef();
				ref.setRef(s.getText());
				FactoryDesc fm = (FactoryDesc) s.peek(1);
				fm.setObject(ref);
			}
		});
		visitorPP.freeze();
		return visitorPP;
	}

	static class WireRudeVisitor extends VisitorSupport {
		private Class<?> clazz;

		public WireRudeVisitor(Class<?> clazz) {
			this.clazz = clazz;
		}

		public void startElement(XMLStreamReader r, Xvs s) throws Exception {
			s.push(this.clazz.newInstance());
		}

		public void endElement(XMLStreamReader r, Xvs s) {
			Object v = s.pop();
			Object obj = s.peek();
			if ((this.clazz != null) && (!this.clazz.isInstance(v))) {
				throw new AssertionError();
			}
			if ((v instanceof RudeRef)) {
				((RudeRef) v).setRef(s.getText());
			} else if ((v instanceof RudeValue)) {
				((RudeValue) v).setValue(s.getText());
			}
			if ((obj instanceof FactoryParameter)) {
				((FactoryParameter) obj).setValue(v);
			} else if ((obj instanceof RudeList)) {
				((RudeList) obj).add(v);
			} else if ((obj instanceof RudeSet)) {
				((RudeSet) obj).add(v);
			} else if ((obj instanceof Entry)) {
				((Entry) obj).setObject(v);
			} else if ((obj instanceof PropertyHolder)) {
				((PropertyHolder) obj).setValue(v);
			}
		}
	}

	private void prepare() {
		if (this.mvPP == null) {
			this.mvPP = new PathPattern();
			this.mvPP.add("module", new SetterVisitor(Option.ATTRIBUTE));
			this.mvPP.add("module/description", new SetterVisitor(Option.TEXT));
			this.mvPP.add("module/classloader/private", new VisitorSupport() {
				public void startElement(XMLStreamReader r, Xvs s) throws Exception {
					int count = r.getAttributeCount();
					for (int i = 0; i < count; i++) {
						if ((r.getAttributeLocalName(i).equals("childFirst"))
								&& (r.getAttributeValue(i).equals("true"))) {
							GenericContainer<?> m = (GenericContainer) s.peek(-1);

							ClassLoader l = m.getClassLoader();
							if ((l instanceof ModulePrivateClassLoader)) {
								((ModulePrivateClassLoader) l).setChildFirst(true);
							} else if ((l.getParent() instanceof ModulePrivateClassLoader)) {
								((ModulePrivateClassLoader) l.getParent()).setChildFirst(true);
							}
						}
					}
				}
			});
			this.mvPP.freeze();
		}
		if (this.sphPP == null) {
			this.sphPP = new PathPattern();
			this.sphPP.add("module/*/component", new SavePointHandle() {
				public void beforeUndo(SavePoint sp, Xvs s) {
					StringBuffer sb = new StringBuffer();
					int size = s.size();
					if ((size > 0) && ((s.peek(-1) instanceof GenericContainer))) {
						GenericContainer<?> m = (GenericContainer) s.peek(-1);

						sb.append("module=").append(m.getName());
						for (int i = 0; i < size; i++) {
							Object o = s.peek();
							if ((o instanceof AbstractMeta)) {
								AbstractMeta meta = (AbstractMeta) o;
								if (meta.getName() == null) {
									break;
								}
								sb.append(" component=").append(meta.getName());
								break;
							}
						}
					}
					Throwable thr = sp.getThrowableSource().getThrowable();
					if ((thr instanceof InvocationTargetException)) {
						thr = thr.getCause();
					}
				}

				public void afterUndo(SavePoint sp, Xvs s) throws HandleException {
				}
			});
			this.sphPP.add("module/aops/aspect", new SavePointHandle() {
				public void beforeUndo(SavePoint sp, Xvs s) {
					StringBuffer sb = new StringBuffer();
					int size = s.size();
					if ((size > 0) && ((s.peek(-1) instanceof GenericContainer))) {
						GenericContainer<?> m = (GenericContainer) s.peek(-1);

						sb.append("module=").append(m.getName());
					}
					Throwable thr = sp.getThrowableSource().getThrowable();
					if ((thr instanceof InvocationTargetException)) {
						thr = thr.getCause();
					}
				}

				public void afterUndo(SavePoint sp, Xvs s) throws HandleException {
				}
			});
			this.sphPP.add("module/rest/resource", new SavePointHandle() {
				public void beforeUndo(SavePoint sp, Xvs s) {
					StringBuffer sb = new StringBuffer();
					int size = s.size();
					if ((size > 0) && ((s.peek(-1) instanceof GenericContainer))) {
						GenericContainer<?> m = (GenericContainer) s.peek(-1);

						sb.append("module=").append(m.getName());
					}
					Throwable thr = sp.getThrowableSource().getThrowable();
					if ((thr instanceof InvocationTargetException)) {
						thr = thr.getCause();
					}
				}

				public void afterUndo(SavePoint sp, Xvs s) throws HandleException {
				}
			});
			this.sphPP.freeze();
		}
		if (this.visitorPP == null) {
			this.visitorPP = prepare(true);
		}

	}

	private FileFilter moduleFileFilter = FileFilterFactory.and(new FileFilter[] {
			FileFilterFactory.or(new FileFilter[] { FileFilterFactory.suffixFileFilter(".module"),
					FileFilterFactory.suffixFileFilter(".upm"), FileFilterFactory.suffixFileFilter(".usm"),
					FileFilterFactory.suffixFileFilter(".bpm"), FileFilterFactory.suffixFileFilter(".bsm"),
					FileFilterFactory.suffixFileFilter(".aop"), FileFilterFactory.suffixFileFilter(".rest") }),
			FileFilterFactory.not(FileFilterFactory.directoryFileFilter()) });

}
