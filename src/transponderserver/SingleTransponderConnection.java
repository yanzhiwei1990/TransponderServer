package transponderserver;

import java.net.ServerSocket;
import java.net.Socket;

public class SingleTransponderConnection {

	private Socket mFrom;
	private ServerSocket mServer;
	private Socket mTo;
	
	private TransponderSocketThread mFromToTo;
	private TransponderSocketThread mToToFrom;
	
	public SingleTransponderConnection(Socket from, ServerSocket server, Socket to) {
		mFrom = from;
		mServer = server;
		mTo = to;
	}
	
	public void startConnect() {
		mFromToTo = new TransponderSocketThread(mFrom, mServer, mTo);
		mToToFrom = new TransponderSocketThread(mTo, mServer, mFrom);
		mFromToTo.start();
		mToToFrom.start();
	}
	
	public void start() {
		
	}
	
	public void stop() {
		
	}
}
