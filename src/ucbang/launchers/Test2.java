package ucbang.launchers;

import ucbang.network.*;

public class Test2 {
	public static void main(String args[]) {
		new Server(12345);
		new Client("localhost", true, "Host");
		new Client("localhost", true, "Client2");
	}
}