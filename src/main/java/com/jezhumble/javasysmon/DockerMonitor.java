package com.jezhumble.javasysmon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

class DockerMonitor extends LinuxMonitor {

	private static final File CPU_ACTIVITY_FILE = new File("/sys/fs/cgroup/cpuacct/cpuacct.stat");
	private static final long MILLIS_IN_ONE_TICK = 100;
	private static final Usage BASE_USAGE = getUsage();

	DockerMonitor() {
		if (CPU_ACTIVITY_FILE.exists()) {
			JavaSysMon.setMonitor(this);
		}
	}

	@Override
	public CpuTimes cpuTimes() {
		Usage now = getUsage();

		long totalMillis = System.currentTimeMillis() - BASE_USAGE.getTime();
		long userMillis = (now.getUserTicks() - BASE_USAGE.getUserTicks()) * MILLIS_IN_ONE_TICK;
		long systemMillis = (now.getSystemTicks() - BASE_USAGE.getSystemTicks()) * MILLIS_IN_ONE_TICK;
		long idleMillis = totalMillis - userMillis - systemMillis;

		return new CpuTimes(userMillis, systemMillis, idleMillis);
	}

	/**
	 * Вытаскивает количество "тиков" из текстового представления
	 *
	 * @param usage Строка вида 'user 13123123' или 'system 42424'
	 * @return собственно, число после user или system
	 */
	private static long extractUsage(String usage) {
		String[] parts = usage.split(" ");
		return Long.parseLong(parts[1]);
	}

	private static Usage getUsage() {
		try (FileInputStream inputStream = new FileInputStream(CPU_ACTIVITY_FILE);
			 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			long user = extractUsage(reader.readLine());
			long system = extractUsage(reader.readLine());
			return new Usage(user, system);
		} catch (Exception e) {
			return new Usage(0, 0);
		}
	}
}

final class Usage {

	private final long userTicks;
	private final long systemTicks;
	private final long time = System.currentTimeMillis();

	Usage(long userTicks, long systemTicks) {
		this.userTicks = userTicks;
		this.systemTicks = systemTicks;
	}

	long getUserTicks() {
		return userTicks;
	}

	long getSystemTicks() {
		return systemTicks;
	}

	long getTime() {
		return time;
	}

}