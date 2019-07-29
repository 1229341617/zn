//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nc.bs.framework.provision;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.InflaterInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import nc.vo.logging.Debug;

import org.w3c.dom.Document;

public final class ProvisionUtil {
	public static final String ALL_HINT_PARAM = "all";
	public static final String RESOURCE_PARAM = "resource";
	public static final String LOCATION_PARAM = "location";
	public static final String COMMAND_PARAM = "command";
	public static final String DIGEST_PARAM = "digest";
	public static final String DOWNLOAD_PACK_INDEX_CMD = "downloadPackIndex";
	public static final String DOWNLOAD_PACKS_CMD = "downloadPacks";
	public static final String PACK_INDEX_FILE = "pack-index.ucs";
	public static final String PV_CONFIG_FILE = "ierp/bin/provision-config.properties";
	public static final String PV_CONFIG_FILE_PROP = "nc.provision.config";
	public static final String SCAN_CONTROL_PROP = "nc.code.scan";
	public static final String SAVE_PI_PROP = "nc.savePackIndex";
	public static final String DUP_CHECK_PROP = "nc.duplicateCheck";
	public static final String NC_SERVER_LOCATION = "nc.server.location";
	public static final String DUMP_AS_XML = "dumpAsXml";
	public static final String RELOAD_PI = "reloadPackIndex";
	public static final String RESCAN = "rescan";
	public static final String PROVISION_PATH = "provision";
	public static final String OPTIMIZE_MUTIPLE_PROP = "nc.optimizeMultiple";
	public static final String TAMPERED_PROTECT_PROP = "nc.tamperedProtect";
	private static final long FNV_32_INIT = 2166136261L;
	private static final long FNV_32_PRIME = 16777619L;
	public static final int TP_FLAG = 2;
	public static final int PI_CHANGED_FLAG = 1;
	public static FileFilter FF_FILTER = new ProvisionUtil.FFilter();
	public static FileFilter DF_FILTER = new ProvisionUtil.DirectoryFilter();
	public static Comparator<String> CP_COMPARATOR = new ProvisionUtil.PrefixComparator();

	public ProvisionUtil() {
	}

	public static ByteArrayOutputStream marshal(File f) throws IOException {
		FileInputStream fin = null;

		try {
			fin = new FileInputStream(f);
			byte[] buff = new byte[8192];
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			boolean var4 = false;

			int len;
			while ((len = fin.read(buff)) > 0) {
				bout.write(buff, 0, len);
			}

			ByteArrayOutputStream var5 = bout;
			return var5;
		} finally {
			if (fin != null) {
				fin.close();
			}

		}
	}

	public static void unmarshal(File f, byte[] bytes) throws IOException {
		File p = f.getParentFile();
		if (!p.exists()) {
			p.mkdirs();
		}

		FileOutputStream fout = null;

		try {
			fout = new FileOutputStream(f);
			fout.write(bytes);
		} finally {
			if (fout != null) {
				fout.close();
			}

		}

	}

	public static final String getPackageName(String className) {
		int index = className.lastIndexOf(".");
		return index > 0 ? className.substring(0, index) : null;
	}

	public static final String getPackagePath4Resource(String name) {
		if (name.length() > 0 && name.charAt(0) == '/') {
			name = name.substring(1);
		}

		int index = name.lastIndexOf(47);
		return index != -1 ? name.substring(0, index) : null;
	}

	public static final String getPackageName4Resource(String name) {
		if (name.startsWith("/")) {
			name = name.substring(1);
		}

		int index = name.lastIndexOf(47);
		if (index != -1) {
			name = name.substring(0, index);
			return name.replace('/', '.');
		} else {
			return null;
		}
	}

	public static final String getResourceName(String name) {
		int index = name.lastIndexOf(47);
		return index > 0 ? name.substring(index) : name;
	}

	public static PackIndex downloadPackIndex(String digest, URL url) throws IOException {
		return downloadPackIndex(digest, url, (OutHolder) null);
	}

	public static PackIndex downloadPackIndex(String digest, URL url, OutHolder<Integer> out) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("download pack index: [digest=").append(digest).append("] from [url=").append(url).append("]");
		Debug.debug(sb.toString());
		HashMap<String, String> params = new HashMap();
		params.put("command", "downloadPackIndex");
		if (digest != null) {
			params.put("digest", digest);
		}

		HttpURLConnection conn = null;

		PackIndex index;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			OutputStream output = conn.getOutputStream();
			output.write(toRquest(params).getBytes("utf-8"));
			output.flush();
			output.close();
			InputStream input = conn.getInputStream();
			BufferedInputStream buff = new BufferedInputStream(input);
			InflaterInputStream inf = new InflaterInputStream(buff);
			int c = inf.read();
			if (out != null) {
				out.setValue(c);
			}

			Debug.debug("pack index changed flag c=" + c);
			DataInputStream din = new DataInputStream(inf);
			if ((c & 1) != 0) {
				Debug.debug("pack index has chanaged");
				index = new PackIndex();
				index.load(din);
				Debug.debug("downloaded pack index: " + index.getDigest());
				PackIndex var13 = index;
				return var13;
			}

			Debug.debug("pack index not chanaged");
			index = null;
		} catch (IOException var17) {
			Debug.error(sb.toString() + " error!", var17);
			throw var17;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}

		}

		return index;
	}

	public static final void savePackIndex(PackIndex index, File f) throws IOException {
		FileOutputStream fout = null;

		try {
			fout = new FileOutputStream(f);
			BufferedOutputStream bufferred = new BufferedOutputStream(fout);
			index.save(bufferred);
		} finally {
			if (fout != null) {
				fout.close();
			}

		}

	}

	public static final void savePackIndex(PackIndex index, File f, History hs) throws IOException {
		FileOutputStream fout = null;

		try {
			fout = new FileOutputStream(f);
			BufferedOutputStream bufferred = new BufferedOutputStream(fout);
			index.save(bufferred, hs);
		} finally {
			if (fout != null) {
				fout.close();
			}

		}

	}

	public static final void savePackIndexAsXml(PackIndex index, File f) throws Exception {
		FileOutputStream fout = null;

		try {
			fout = new FileOutputStream(f);
			BufferedOutputStream bufferred = new BufferedOutputStream(fout);
			index.dumpAsXml(bufferred);
		} finally {
			if (fout != null) {
				fout.close();
			}

		}

	}

	public static void savePackIndex(File f, PackIndex packIndex) throws IOException {
		File pf = f.getParentFile();
		if (pf != null && !pf.exists()) {
			pf.mkdirs();
		}

		FileOutputStream fout = null;

		try {
			fout = new FileOutputStream(f);
			BufferedOutputStream bufOut = new BufferedOutputStream(fout);
			packIndex.save(bufOut);
			bufOut.flush();
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException var10) {
					;
				}
			}

		}

	}

	public static final PackIndex loadPackIndex(File f) throws IOException {
		if (!f.exists()) {
			return null;
		} else {
			FileInputStream fin = null;

			PackIndex var4;
			try {
				fin = new FileInputStream(f);
				BufferedInputStream buffered = new BufferedInputStream(fin);
				PackIndex packIndex = new PackIndex();
				packIndex.load(buffered);
				var4 = packIndex;
			} finally {
				if (fin != null) {
					fin.close();
				}

			}

			return var4;
		}
	}

	public static final long lastModified(File file) {
		if (file.isDirectory()) {
			File[] fs = file.listFiles(FF_FILTER);
			long l = 0L;
			Arrays.sort(fs, FNComparator.INSTANCE);
			File[] arr$ = fs;
			int len$ = fs.length;

			for (int i$ = 0; i$ < len$; ++i$) {
				File f = arr$[i$];
				long l1 = f.lastModified();
				if (l1 > l) {
					l = l1;
				}
			}

			return l;
		} else {
			return file.lastModified();
		}
	}

	public static final void lastModifiedAndSize(long[] ls, File file) {
		ls[0] = 0L;
		ls[1] = 0L;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		File[] fs = file.listFiles(FF_FILTER);
		Arrays.sort(fs, FNComparator.INSTANCE);
		File[] arr$ = fs;
		int len$ = fs.length;

		for (int i$ = 0; i$ < len$; ++i$) {
			File f = arr$[i$];

			try {
				bout.write(toBytes(f.length()));
				bout.write(toBytes(f.lastModified()));
				bout.write(f.getName().getBytes("utf-8"));
			} catch (IOException var9) {
				;
			}

			ls[1] += f.length();
		}

		ls[0] = getHashCode(bout.toByteArray());
	}

	public static String getRelPath(File f, File base) {
		StringBuilder sb;
		for (sb = new StringBuilder(); !f.equals(base); f = f.getParentFile()) {
			sb.insert(0, f.getName());
			sb.insert(0, '/');
		}

		return sb.length() > 0 ? sb.substring(1) : "";
	}

	public static void computeModuleDirs(File dir, List<File> moduleList) {
		if (dir.exists()) {
			List<File> newModuleList = Arrays.asList(dir.listFiles(new ProvisionUtil.ModuleFilter()));
			moduleList.addAll(newModuleList);
			Collections.sort(moduleList, new ProvisionUtil.ModuleComparator(newModuleList));
		}
	}

	public static void computeStandCP(File stdDir, List<File> files) {
		File file = null;
		file = new File(stdDir, "extension/classes/");
		if (file.exists()) {
			files.add(file);
		}

		file = new File(stdDir, "extension/resources/");
		if (file.exists()) {
			files.add(file);
		}

		files = listFiles(files, new File(stdDir, "extension/lib"), new ProvisionUtil.JarFilter());
		file = new File(stdDir, "hyext/classes/");
		if (file.exists()) {
			files.add(file);
		}

		file = new File(stdDir, "hyext/resources/");
		if (file.exists()) {
			files.add(file);
		}

		files = listFiles(files, new File(stdDir, "hyext/lib"), new ProvisionUtil.JarFilter());
		file = new File(stdDir, "classes/");
		if (file.exists()) {
			files.add(file);
		}

		file = new File(stdDir, "resources/");
		if (file.exists()) {
			files.add(file);
		}

		listFiles(files, new File(stdDir, "lib"), new ProvisionUtil.JarFilter());
	}

	public static void computeJarsInLib(File lib, List<File> list) {
		List<File> files = listFiles(lib, new ProvisionUtil.JarFilter());
		list.addAll(files);
	}

	public static File[] computeJarsInLib(File lib) {
		List<File> files = listFiles(lib, new ProvisionUtil.JarFilter());
		File[] fileArray = new File[files.size()];
		files.toArray(fileArray);
		return fileArray;
	}

	public static List<File> listFiles(File dir, FileFilter filter) {
		List<File> files = new ArrayList();
		if (dir.exists() && !dir.isFile()) {
			listFiles(files, dir, filter);
			return files;
		} else {
			return files;
		}
	}

	private static List<File> listFiles(List<File> filesList, File dir, FileFilter filter) {
		if (dir.exists()) {
			File[] files = dir.listFiles(filter);
			List<File> temp = Arrays.asList(files);
			Collections.sort(temp, FNComparator.INSTANCE);
			filesList.addAll(temp);
			File[] subDirs = dir.listFiles(DF_FILTER);
			if (subDirs.length > 0) {
				Arrays.sort(subDirs, FNComparator.INSTANCE);

				for (int i = 0; i < subDirs.length; ++i) {
					listFiles(filesList, subDirs[i], filter);
				}
			}
		}

		return filesList;
	}

	public static boolean delete(File file) {
		if (null != file && file.exists()) {
			if (!file.isDirectory()) {
				return file.delete();
			} else {
				return removeDirectory(file) && file.delete();
			}
		} else {
			return true;
		}
	}

	public static boolean removeDirectory(File directory) {
		if (null != directory && directory.exists()) {
			if (!directory.isDirectory()) {
				return false;
			} else {
				boolean result = true;
				File[] files = directory.listFiles();

				for (int i = 0; i < files.length; ++i) {
					File file = files[i];
					result = result && delete(file);
				}

				return result;
			}
		} else {
			return true;
		}
	}

	public static void clearEmptyDir(File dir) {
		File[] d = dir.listFiles();
		if (d != null && d.length == 0) {
			File parent = dir.getParentFile();
			delete(dir);
			clearEmptyDir(parent);
		}

	}

	public static String toRquest(Map<String, String> map) {
		StringBuffer sb = new StringBuffer();
		Iterator i$ = map.entrySet().iterator();

		while (i$.hasNext()) {
			Entry<String, String> e = (Entry) i$.next();
			if (e.getValue() != null && e.getKey() != null) {
				sb.append((String) e.getKey()).append('=').append((String) e.getValue()).append("&");
			}
		}

		return sb.toString();
	}

	public static long getHashCode(byte[] bs) {
		long rv = 2166136261L;
		int len = bs.length;

		for (int i = 0; i < len; ++i) {
			rv ^= (long) bs[i];
			rv *= 16777619L;
		}

		return rv & 4294967295L;
	}

	public static byte[] toBytes(long v) {
		byte[] bytes = new byte[] { (byte) ((int) (v >>> 56)), (byte) ((int) (v >>> 48)), (byte) ((int) (v >>> 40)),
				(byte) ((int) (v >>> 32)), (byte) ((int) (v >>> 24)), (byte) ((int) (v >>> 16)),
				(byte) ((int) (v >>> 8)), (byte) ((int) (v >>> 0)) };
		return bytes;
	}

	public static String getI18nName(String name, String langcode) {
		String type = name.substring(name.length() - 3);
		name = name.substring(0, name.length() - 4);
		return name + "_" + langcode + "." + type;
	}

	public static boolean isPicture(String name) {
		String result = name.toLowerCase();
		return result.endsWith("gif") || result.endsWith("jpg") || result.endsWith("png");
	}

	public static boolean isValidLocation(String location) {
		try {
			if (location.startsWith("external/")) {
				return isValidLocation(location.substring("external/".length()));
			} else if (location.startsWith("modules/")) {
				location = location.substring("modules/".length());
				int pos = location.indexOf("/");
				if (pos >= 0) {
					location = location.substring(pos + 1);
					return isValidLocation(location);
				} else {
					return false;
				}
			} else if (location.startsWith("hyext/")) {
				return isValidLocation(location.substring("hyext/".length()));
			} else if (location.startsWith("extension/")) {
				return isValidLocation(location.substring("extension/".length()));
			} else if (location.startsWith("resources/")) {
				return true;
			} else if (location.startsWith("classes/")) {
				return true;
			} else {
				return location.startsWith("lib/");
			}
		} catch (Exception var2) {
			return false;
		}
	}

	public static void marshal(File f, OutputStream out) throws IOException {
		FileInputStream fin = null;

		try {
			fin = new FileInputStream(f);
			byte[] buff = new byte[8192];
			boolean var4 = false;

			int len;
			while ((len = fin.read(buff)) >= 0) {
				out.write(buff, 0, len);
			}
		} finally {
			if (fin != null) {
				fin.close();
			}

		}

	}

	private static class PrefixComparator implements Comparator<String> {
		private String prefix;

		PrefixComparator() {
		}

		PrefixComparator(String prefix) {
			this.prefix = prefix;
		}

		public int compare(String s1, String s2) {
			boolean flag1;
			boolean flag2;
			Comparator next;
			if (this.prefix == null) {
				flag1 = s1.startsWith("resources");
				flag2 = s2.startsWith("resources");
				if (!flag1 && !flag2) {
					next = this.getComparator(s1, s2);
					return next != null ? next.compare(s1, s2) : s1.compareTo(s2);
				} else if (s1.startsWith("external/")) {
					return -1;
				} else if (s2.startsWith("external/")) {
					return 1;
				} else if (!flag1) {
					return 1;
				} else {
					return !flag2 ? -1 : s1.compareTo(s2);
				}
			} else {
				flag1 = s1.startsWith(this.prefix);
				flag2 = s2.startsWith(this.prefix);
				if (!flag1 && !flag2) {
					return s1.compareTo(s2);
				} else if (!flag1) {
					return 1;
				} else if (!flag2) {
					return -1;
				} else {
					s1 = s1.substring(this.prefix.length());
					s2 = s2.substring(this.prefix.length());
					if (this.prefix.equals("modules/")) {
						flag1 = s1.startsWith("uap");
						flag2 = s2.startsWith("uap");
						int v;
						if (!flag1 && !flag2) {
							v = s1.indexOf("/");
							if (v < 0) {
								return s1.compareTo(s2);
							}

							s1.substring(v + 1);
							s1 = s2.substring(v + 1);
						} else {
							if (!flag1) {
								return 1;
							}

							if (!flag2) {
								return -1;
							}

							v = s1.compareTo(s2);
							if (v != 0) {
								return v;
							}

							int pos = s1.indexOf("/");
							if (pos < 0) {
								return v;
							}

							s1.substring(pos + 1);
							s1 = s2.substring(pos + 1);
						}
					}

					next = this.getComparator(s1, s2);
					return next != null ? next.compare(s1, s2) : s1.compareTo(s2);
				}
			}
		}

		private Comparator<String> getComparator(String s1, String s2) {
			boolean flag1 = s1.startsWith("external/");
			boolean flag2 = s2.startsWith("external/");
			if (!flag1 && !flag2) {
				flag1 = s1.startsWith("extension/");
				flag2 = s2.startsWith("extension/");
				if (!flag1 && !flag2) {
					flag1 = s1.startsWith("hyext/");
					flag2 = s2.startsWith("hyext/");
					if (!flag1 && !flag2) {
						flag1 = s1.startsWith("client/");
						flag2 = s2.startsWith("client/");
						if (!flag1 && !flag2) {
							flag1 = s1.startsWith("classes/");
							flag2 = s2.startsWith("classes/");
							if (!flag1 && !flag2) {
								flag1 = s1.startsWith("resources/");
								flag2 = s2.startsWith("resources/");
								if (!flag1 && !flag2) {
									flag1 = s1.startsWith("modules/");
									flag2 = s2.startsWith("modules/");
									if (!flag1 && !flag2) {
										flag1 = s1.startsWith("langlib/");
										flag2 = s2.startsWith("langlib/");
										return !flag1 && !flag2 ? null : new ProvisionUtil.PrefixComparator("langlib/");
									} else {
										return new ProvisionUtil.PrefixComparator("modules/");
									}
								} else {
									return new ProvisionUtil.PrefixComparator("resources/");
								}
							} else {
								return new ProvisionUtil.PrefixComparator("classes/");
							}
						} else {
							return new ProvisionUtil.PrefixComparator("client/");
						}
					} else {
						return new ProvisionUtil.PrefixComparator("hyext/");
					}
				} else {
					return new ProvisionUtil.PrefixComparator("extension/");
				}
			} else {
				return new ProvisionUtil.PrefixComparator("external/");
			}
		}
	}

	private static class FFilter implements FileFilter {
		private FFilter() {
		}

		public boolean accept(File af) {
			return af.isFile() && !af.getName().equalsIgnoreCase("Thumbs.db");
		}
	}

	static class AndFilter implements FileFilter {
		FileFilter[] array;

		public AndFilter(FileFilter[] array) {
			this.array = array;
		}

		public boolean accept(File pathname) {
			if (this.array == null) {
				return true;
			} else {
				boolean ret = true;

				for (int i = 0; i < this.array.length && ret; ++i) {
					ret = ret && this.array[i].accept(pathname);
				}

				return ret;
			}
		}
	}

	static class ExcludeFilter implements FileFilter {
		List excludes;

		public ExcludeFilter(List excludes) {
			this.excludes = excludes;
		}

		public boolean accept(File pathname) {
			if (this.excludes == null) {
				return true;
			} else {
				return !this.excludes.contains(pathname);
			}
		}
	}

	static class DirectoryFilter implements FileFilter {
		DirectoryFilter() {
		}

		public boolean accept(File f) {
			return f.isDirectory();
		}
	}

	static class ModuleFilter implements FileFilter {
		ModuleFilter() {
		}

		public boolean accept(File pathname) {
			if (pathname.isDirectory()) {
				File subFile = new File(pathname, "META-INF/module.xml");
				return subFile.exists();
			} else {
				return false;
			}
		}
	}

	static class JarFilter implements FileFilter {
		JarFilter() {
		}

		public boolean accept(File pathname) {
			String name = pathname.getName().toLowerCase();
			if (name.endsWith(".jar")) {
				return !name.endsWith("_src.jar") && !name.endsWith("_doc.jar");
			} else if (!name.endsWith(".zip")) {
				return false;
			} else {
				return !name.endsWith("_src.zip") && !name.endsWith("_doc.zip");
			}
		}
	}

	static class ModuleComparator implements Comparator<File> {
		private Map<String, Integer> map = new HashMap();

		private List<File> moduleList;

		ModuleComparator() {
		}

		ModuleComparator(List<File> moduleList) {
			this.moduleList = moduleList;
			initPriority();
		}

		public int compare(File o1, File o2) {
			Integer priority1 = this.getPriority(o1);
			Integer priority2 = this.getPriority(o2);
			int retValue = priority1.compareTo(priority2);
			if (retValue != 0) {
				return retValue;
			} else {
				boolean moduleStartsWithUap1 = o1.getName().startsWith("uap");
				boolean moduleStartsWithUap2 = o2.getName().startsWith("uap");
				if (moduleStartsWithUap1 ^ moduleStartsWithUap2) {
					return moduleStartsWithUap1 ? -1 : 1;
				} else {
					return o1.getName().compareTo(o2.getName());
				}
			}
		}

		private Integer getPriority(File module) {
			String moduleName1 = module.getName();
			Integer priority = null;
			if ((priority = (Integer) this.map.get(moduleName1)) == null) {
				File moduleFile = new File(module, "META-INF/module.xml");
				if (moduleFile.exists()) {
					try {
						Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(moduleFile);
						String str = dom.getDocumentElement().getAttribute("priority");
						if (str != null) {
							int value = Integer.parseInt(str);
							priority = value;
						}
					} catch (Exception var8) {
						;
					}
				}
				if (priority == null) {
					priority = 100;
				}

				this.map.put(moduleName1, priority);
			}

			return priority;
		}

		public void initPriority() {
			if (moduleList == null) {
				return;
			}
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime()
					.availableProcessors() * 2);
			Map<String, Future<Integer>> threadMap = new HashMap<>();
			for (File module : moduleList) {
				InitPriorityCallable callable = new InitPriorityCallable(module);
				threadMap.put(module.getName(), executor.submit(callable));
			}
			Iterator<Entry<String, Future<Integer>>> iterator = threadMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Future<Integer>> entry = iterator.next();
				try {
					map.put(entry.getKey(), entry.getValue().get());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			executor.shutdownNow();
		}

		class InitPriorityCallable implements Callable<Integer> {
			private final File module;

			public InitPriorityCallable(File module) {
				this.module = module;
			}

			@Override
			public Integer call() throws Exception {
				Integer priority = null;
				File moduleFile = new File(module, "META-INF/module.xml");
				if (moduleFile.exists()) {
					try {
						Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(moduleFile);
						String str = dom.getDocumentElement().getAttribute("priority");
						if (str != null) {
							int value = Integer.parseInt(str);
							priority = value;
						}
					} catch (Exception var8) {

					}
					if (priority == null) {
						priority = 100;
					}
				}
				return priority;
			}
		}

	}

}
