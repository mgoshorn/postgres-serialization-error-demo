package com.revature.launcher;

public class Launcher {

	ColorDao dao = new ColorDao();
	
	public void launchDemo() {
		dao.concurrentSerializableWrite();
	}
	
	public static void main(String[] args) {
		Launcher launcher = new Launcher();
		launcher.launchDemo();
	}
}
