package transponderserver;

import java.net.ServerSocket;
import java.net.Socket;

public class TransponderSocketThread extends Thread {

	private Socket mFrom;
	private ServerSocket mServer;
	private Socket mTo;
	
	private ReadWriteRunnable mTransponder;
	
	public TransponderSocketThread(Socket from, ServerSocket server, Socket to) {
		mFrom = from;
		mServer = server;
		mTo = to;
	}
	
	private void startTransponder() {
		mTransponder = new ReadWriteRunnable(mFrom, mTo);
	}
	
	public void run() {
		startTransponder();
	}
}
