package transponderserver;

import java.net.ServerSocket;
import java.net.Socket;

import transponderserver.Callback.SingleClientConnnectionCallback;
import transponderserver.Callback.SingleTransponderConnnectionCallback;

public class SingleClientConnection {
	
	private Socket mClient;
	private ServerSocket mSourceServer;
	private SingleClientSocketThread mClientSocketThread;
	private SingleTransponderConnnectionCallback mSingleTransponderConnnectionCallback;
	private SingleClientConnnectionCallback mSingleClientConnnectionCallback;
	
	public SingleClientConnection(ServerSocket server, Socket client,SingleClientConnnectionCallback clientcallback, SingleTransponderConnnectionCallback transpondercallback) {
		mSourceServer = server;
		mClient = client;
		mSingleClientConnnectionCallback = clientcallback;
		mSingleTransponderConnnectionCallback = transpondercallback;
	}
	
	public String getClientInfo() {
		String result = null;
		
		return result;
	}
	
	public void setClientInfo() {
		
	}
	
	public Socket getClientSocket() {
		return mClient;
	}
	
	public void start() {
		mClientSocketThread = new SingleClientSocketThread(mClient, mSingleTransponderConnnectionCallback);
		mClientSocketThread.start();
	}
	
	public void stop() {
		
	}
}
