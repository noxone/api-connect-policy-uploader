package org.olafneumann.apic.policy;

import java.io.IOException;
import java.util.Optional;

import org.olafneumann.apic.policy.StartOptions.Credentials;

public class PolicyUploader {
	public static void main(String[] args) {
		// parse command line options
		StartOptions options = StartOptions.parse(args);
		// if command line options fail: shut down
		if (options == null) {
			return;
		}

		final Credentials credentials = options.getCredentials();
		final ApiConnectClient client = new ApiConnectClient(options.getServer(), options.getOrganization(),
				options.getCatalog(), options.isIgnoreSsl());

		// Log in to server
		final boolean loggedIn;
		try {
			loggedIn = client.login(credentials.getUsername(), credentials.getPassword());
		} catch (IOException e) {
			System.err.println("Unable to communicate with server while logging in.");
			e.printStackTrace();
			System.exit(1);
			return;
		}
		if (!loggedIn) {
			System.err.println(String.format("Unable to log in to server[%s] using username[%s].", options.getServer(),
					credentials.getUsername()));
			System.exit(2);
			return;
		}

		// Do the upload
		final Optional<String> message;
		try {
			message = client.uploadPolicy(options.getPolicyFile());
		} catch (IOException e) {
			System.err.println("Unable to communicate with server while uploading policy");
			e.printStackTrace();
			System.exit(3);
			return;
		}
		if (message.isPresent()) {
			System.err.println(String.format("Unable to upload policy.\r\n%s", message.get()));
			System.exit(4);
			return;
		}
	}
}
