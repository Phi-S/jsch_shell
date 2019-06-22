import com.jcraft.jsch.*;

import java.io.*;
import java.util.ArrayList;

public class jsch_shell_java_api {

	private PipedOutputStream pos;

	Session session;

	Channel channel;

	private boolean stop = false;

	String host;
	String user;

	private int id = 0;

	ArrayList<String> ergs = new ArrayList<String>();

	public jsch_shell_java_api(String host, int port, String user, String pw) {
		JSch jsch = new JSch();
		this.user = user;

		try {
			session = jsch.getSession(user, host, port);

			session.setPassword(pw);

			session.setConfig("StrictHostKeyChecking", "no");

			session.connect();
			System.out.println("sshShell | session connected");

			init();
			System.out.println("sshShell | READY");

			liveOutput lo = new liveOutput();
			Thread th = new Thread(lo);
			th.start();

			sendCommand("ls");

		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void init() {
		try {
			channel = session.openChannel("shell");

			InputStream is = new PipedInputStream();
			pos = new PipedOutputStream((PipedInputStream) is);
			channel.setInputStream(is);

			channel.connect();

			System.out.println("sshShell | channel connected");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int sendCommand(String command) {

		ergs.add("");

		int cid = id++;

		try {
			pos.write(("echo START_COMMAND_ID:" + cid + " && " + command + "\n\n").getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return cid;

	}

	public String getErg(int id) {
		while (true) {

			String erg = ergs.get(id);
			if (!erg.equals("")) {

				int lastIndex = erg.lastIndexOf(user + "@");
				if (erg.indexOf(user + "@") != lastIndex) {
					erg = erg.substring(0, lastIndex);
				}
				return erg;
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void close() {
		session.disconnect();
		channel.disconnect();
		stop = true;
	}

	private class liveOutput implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				InputStream is = channel.getInputStream();

				StringBuilder erg = new StringBuilder();

				StringBuilder line = new StringBuilder();

				int tempCID = -1;

				while (true) {

					int av = is.available();

					if (av != 0) {
						byte[] bytes = new byte[av];

						is.read(bytes);

						for (int i = 0; i < bytes.length; i++) {

							byte b = bytes[i];

							if (b == 0) {
								break;
							}

							line.append((char) b);

							if (b == 10) {
								String temp = line.toString();

								if (temp.startsWith("START_COMMAND_ID:")) {
									tempCID = Integer.parseInt(temp.substring(17).trim());
								} else if (temp.startsWith(user + "@")) {
									if (tempCID != -1) {
										ergs.set(tempCID, ergs.get(tempCID) + erg);
										erg = new StringBuilder();
									}
								}
								erg.append(temp);

								line = new StringBuilder();

							}

						}

					}
					if (stop) {
						break;
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}