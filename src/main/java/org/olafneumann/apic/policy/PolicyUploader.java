package org.olafneumann.apic.policy;

public class PolicyUploader {
	public static void main(String[] args) {
		// parse command line options
		StartOptions options = StartOptions.parse(args);
		// if command line options fail: shut down
		if (options == null) {
			return;
		}

	}
}
