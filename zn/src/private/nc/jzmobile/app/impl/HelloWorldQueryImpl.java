package nc.jzmobile.app.impl;

import nc.itf.mobile.app.IHelloWorldQuery;

public class HelloWorldQueryImpl implements IHelloWorldQuery {

	@Override
	public String helloworld() {
		return "hello world";
	}

}
