package nc.bs.framework.core;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import nc.bs.framework.exception.DeployException;
import nc.bs.logging.Log;

public abstract class AbstractDeployer implements Deployer {
	protected static final Log logger = Log.getInstance("nc.bs.framework.server.deploy");
	protected Server server;

	public AbstractDeployer(Server appServer) {
		this.server = appServer;
	}

	public void deploy(URL[] urls) throws DeployException {
		List<Future<Container>> futures = new ArrayList<>();
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime()
				.availableProcessors() * 2);
		for (URL url : urls) {
			if (this.server.getContainer(url) != null) {
				throw new DeployException(String.format("duplicate module: %s", new Object[] { url }));
			}
			futures.add(executor.submit(new ContainerDeployCallable(url)));
			// File md = new File(url.getFile());
			// Container c = newContainer(url, md);
			// if (c != null) {
			// this.server.addContainer(c);
			// }
		}

		while (!futures.isEmpty()) {
			List<Future<Container>> dones = new ArrayList<>();
			try {
				for (Future<Container> future : futures) {
					if (future.isDone()) {
						if (future.get() != null) {
							this.server.addContainer(future.get());
						}
						dones.add(future);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			futures.removeAll(dones);
		}
		executor.shutdown();
	}

	public class ContainerDeployCallable implements Callable<Container> {
		private final URL url;

		public ContainerDeployCallable(URL url) {
			this.url = url;
		}

		@Override
		public Container call() throws Exception {
			File md = new File(url.getFile());
			Container newContainer = newContainer(url, md);
			return newContainer;
		}
	}

	public void undeploy(URL[] urls) throws DeployException {
		for (URL url : urls) {
			this.server.removeContainer(url);
		}
	}

	public void update(URL[] urls) throws DeployException {
		for (URL url : urls) {
			Container c = this.server.getContainer(url);
			if ((c instanceof Updatable)) {
				((Updatable) c).update();
			}
		}
	}

	protected abstract Container newContainer(URL paramURL, File paramFile);

	public void start(URL[] urls) throws DeployException {
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime()
				.availableProcessors() * 2);
		List<Future<Integer>> futures = new ArrayList<>();
		ArrayList<Container> cl = getContainers(urls);
		Collections.sort(cl, new ContainerComparator());
		for (Container c : cl) {
			futures.add(executor.submit(new ContainerStartCallable(c)));
			// try {
			// logger.debug(String.format("before %s module: %s", new Object[] {
			// "start", c.getName() }));
			// c.start();
			// logger.debug(String.format("after %s module: %s", new Object[] {
			// "start", c.getName() }));
			// } catch (Exception exp) {
			// logger.error(String.format("start <%s> error", new Object[] {
			// c.getName() }), exp);
			// } finally {
			// }
		}
		for (Future<Integer> future : futures) {
			try {
				future.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		executor.shutdown();
	}

	public class ContainerStartCallable implements Callable<Integer> {
		private final Container c;

		public ContainerStartCallable(Container c) {
			this.c = c;
		}

		@Override
		public Integer call() throws Exception {
			try {
				logger.debug(String.format("before %s module: %s", new Object[] { "start", c.getName() }));
				c.start();
				logger.debug(String.format("after %s module: %s", new Object[] { "start", c.getName() }));
			} catch (Exception exp) {
				logger.error(String.format("start <%s> error", new Object[] { c.getName() }), exp);
			} finally {
			}
			return 0;

		}
	}

	public void stop(URL[] urls) throws DeployException {
		ArrayList<Container> cl = getContainers(urls);

		Collections.sort(cl, new ContainerComparator(false));
		for (Container c : cl) {
			try {
				logger.debug(String.format("before %s module: %s", new Object[] { "stop", c.getName() }));
				c.stop();

				logger.debug(String.format("after %s module: %s", new Object[] { "stop", c.getName() }));
			} catch (Exception exp) {
				logger.error(String.format("stop <%s> error", new Object[] { c.getName() }), exp);
			}
		}
	}

	private ArrayList<Container> getContainers(URL[] urls) {
		ArrayList<Container> ml = new ArrayList();
		for (int i = 0; i < urls.length; i++) {
			Container c = this.server.getContainer(urls[i]);
			if (c != null) {
				ml.add(c);
			}
		}
		return ml;
	}
}
