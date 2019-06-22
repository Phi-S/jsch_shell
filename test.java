public class test {

	public static void main(String[] args) {

		jsch_shell c = new jsch_shell("hostname_or_ip", 22, "root", "123");
	
		System.out.println(c.getErg(c.sendCommand("ls /var")));
		System.out.println(c.getErg(c.sendCommand("ls /etc")));
		System.out.println(c.getErg(c.sendCommand("ls /root")));
		System.out.println(c.getErg(c.sendCommand("ls /lib")));
		System.out.println(c.getErg(c.sendCommand("cat /var/log/syslog")));
		
		c.close();
	}

}
