package transponderserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import transponderserver.Callback.SingleTransponderConnnectionCallback;
import transponderserver.Callback.SingleClientConnnectionCallback;

public class TransponderServerManager {

	private ServerSocket mMainServer = null;
	private List<Socket> mClientSocketList = new ArrayList<Socket>();
	private List<SingleTransponderConnection> mTransponderConnecttionList = new ArrayList<SingleTransponderConnection>();
	private List<SingleClientConnection> mClientConnectionList = new ArrayList<SingleClientConnection>();
	private List<SingleTransponderConnnectionCallback> mSingleTransponderConnnectionCallbackList = new ArrayList<SingleTransponderConnnectionCallback>();
	private List<Callback.SingleClientConnnectionCallback> mSingleClientConnnectionCallbackList = new ArrayList<Callback.SingleClientConnnectionCallback>();
	private Object mClientSocketListLock = new Object();
	private Object mTransponderConnecttionListLock = new Object();
	private Object mClientConnectionListLock = new Object();
	private boolean mIsRunning = true;
	
	public TransponderServerManager() {
		// TODO Auto-generated constructor stub
	}
	
	private SingleTransponderConnnectionCallback mSingleTransponderConnnectionCallback = new SingleTransponderConnnectionCallback() {
		@Override
		public void onTransponderConnnectionStatusChange(SingleTransponderConnection transponder, String info, String action) {
			// TODO Auto-generated method stub
			System.out.println("onTransponderConnnectionStatusChange transponder = " + transponder + ", info = " + info + ", action = " + action);
			if (action != null && action.length() > 0) {
				switch (action) {
					case "add":
						addSingleTransponderConnection(transponder);
						break;
					case "remove":
						removeSingleTransponderConnection(transponder);
						break;
					default:
						break;
				}
			}
		}
		
	};
	
	private SingleClientConnnectionCallback mSingleClientConnnectionCallback = new SingleClientConnnectionCallback() {
		@Override
		public void onClientStatusChange(SingleClientConnection client, String info, String action) {
			// TODO Auto-generated method stub
			System.out.println("SingleClientConnnectionCallback client = " + client + ", info = " + info + ", action = " + action);
			if (action != null && action.length() > 0) {
				switch (action) {
					case "add":
						addSingleClientConnection(client);
						break;
					case "remove":
						removeSingleClientConnection(client);
						break;
					default:
						break;
				}
			}
		}
	};
	
	public void moreserver(int port){
    	System.out.println("moreserver starting...");
        try {
        	mMainServer = new ServerSocket(port);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("mMainServer creat Exception = " + e.getMessage());
		}
        try {
        	mMainServer.setReuseAddress(true);
        	mMainServer.setSoTimeout(3000);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("mMainServer setReuseAddress Exception = " + e.getMessage());
		}
        while (mIsRunning) {
        	Socket newSocket = null;
        	try {
        		System.out.println("listen port 19910...");
        		newSocket = mMainServer.accept();
        	} catch (Exception e) {
        		System.out.println("mMainServer.accept() Exception = " + e.getMessage());
        		continue;
        	}
        	try {
        		System.out.println("moreserver dealNewSocket!");
	            dealNewSocket(mMainServer, newSocket, mSingleClientConnnectionCallback, mSingleTransponderConnnectionCallback);
	        } catch (Exception e) {
	            e.printStackTrace();
	            System.out.println("while Exception = " + e.getMessage());
	        }
		}
        try {
        	if (mMainServer != null) {
        		mMainServer.close();
        	}
        	System.out.println("mMainServer.close() over!");
        } catch (Exception e) {
        	e.printStackTrace();
    		System.out.println("mMainServer.close() Exception = " + e.getMessage());
        }
        stopAll();
    }
	
	private void stopAll() {
		synchronized (mClientConnectionListLock) {
			Iterator<SingleClientConnection> iterator = mClientConnectionList.iterator();
			SingleClientConnection singleClientConnection;
			while (iterator.hasNext()) {
				singleClientConnection = (SingleClientConnection)iterator.next();
				singleClientConnection.stop();
			}
			mClientConnectionList.clear();
			System.out.println("stopAll clear mClientConnectionList");
		}
		synchronized (mTransponderConnecttionListLock) {
			Iterator<SingleTransponderConnection> iterator = mTransponderConnecttionList.iterator();
			SingleTransponderConnection singleTransponderConnection;
			while (iterator.hasNext()) {
				singleTransponderConnection = (SingleTransponderConnection)iterator.next();
				singleTransponderConnection.stop();
			}
			mTransponderConnecttionList.clear();
			System.out.println("stopAll clear mTransponderConnecttionList");
		}
	}
	
	private void dealNewSocket(ServerSocket server, Socket client, SingleClientConnnectionCallback clientcallback, SingleTransponderConnnectionCallback transpondercallback) {
		if (containClient(client)) {
			//update info
			System.out.println("dealNewSocket already exist = " + client.getInetAddress());
		} else {
			SingleClientConnection singleclient = new SingleClientConnection(server, client, clientcallback, transpondercallback);
			singleclient.start();
			System.out.println("dealNewSocket start singleclient = " + singleclient);
		}
	}
	
	private boolean containClient(Socket socket) {
		boolean result = false;
		if (mClientSocketList.contains(socket)) {
			result = true;
			System.out.println("containClient find socket = " + socket);
		} else {
			synchronized (mClientConnectionListLock) {
				Iterator<SingleClientConnection> iterator = mClientConnectionList.iterator();
				SingleClientConnection singleClientConnection;
				while (iterator.hasNext()) {
					singleClientConnection = (SingleClientConnection)iterator.next();
					if (singleClientConnection.getClientSocket() == socket) {
						result = true;
						System.out.println("containClient find singleClientConnection = " + singleClientConnection);
						break;
					}
				}
			}
		}
		return result;
	}
	
	private void addClientSocket(Socket add) {
		synchronized (mClientSocketListLock) {
			if (mClientSocketList.contains(add)) {
				System.out.println("addClientSocket already exist");
			} else {
				mClientSocketList.add(add);
				System.out.println("addClientSocket add = " + add);
			}
		}
	}
	
	private void removeClientSocket(Socket remove) {
		if (remove == null) {
			System.out.println("removeClientSocket null");
		}
		synchronized (mClientSocketListLock) {
			mClientSocketList.remove(remove);
			System.out.println("removeClientSocket remove = " + remove);
		}
	}
	
	private void addSingleClientConnection(SingleClientConnection add) {
		synchronized (mClientConnectionListLock) {
			if (mClientConnectionList.contains(add)) {
				System.out.println("addSingleClientConnection already exist");
			} else {
				mClientConnectionList.add(add);
				System.out.println("addSingleClientConnection add = " + add);
			}
		}
	}
	
	private void removeSingleClientConnection(SingleClientConnection remove) {
		if (remove == null) {
			System.out.println("removeSingleClientConnection null");
		}
		synchronized (mClientConnectionListLock) {
			mClientConnectionList.remove(remove);
			System.out.println("removeSingleClientConnection remove = " + remove);
		}
	}
	
	private void addSingleTransponderConnection(SingleTransponderConnection add) {
		synchronized (mTransponderConnecttionListLock) {
			if (mTransponderConnecttionList.contains(add)) {
				System.out.println("addSingleTransponderConnection already exist");
			} else {
				mTransponderConnecttionList.add(add);
				System.out.println("addSingleTransponderConnection add = " + add);
			}
		}
	}
	
	private void removeSingleTransponderConnection(SingleTransponderConnection remove) {
		if (remove == null) {
			System.out.println("removeSingleTransponderConnection null");
		}
		synchronized (mTransponderConnecttionListLock) {
			mTransponderConnecttionList.remove(remove);
			System.out.println("removeSingleTransponderConnection remove = " + remove);
		}
	}
}
