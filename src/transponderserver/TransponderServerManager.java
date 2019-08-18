package transponderserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import transponderserver.Callback.SingleTransponderConnnectionCallback;
import transponderserver.SingleClientConnection.SingleClientSocketThread;
import transponderserver.Callback.SingleClientConnnectionCallback;

public class TransponderServerManager {
	public static final String TAG = TransponderServerManager.class.getSimpleName();
	
	private ServerSocket mMainServer = null;
	/*private List<Socket> mClientSocketList = new ArrayList<Socket>();*/
	//private List<SingleTransponderConnection> mTransponderConnecttionList = new ArrayList<SingleTransponderConnection>();
	private List<SingleClientConnection> mClientConnectionList = new ArrayList<SingleClientConnection>();
	//private List<SingleTransponderConnnectionCallback> mSingleTransponderConnnectionCallbackList = new ArrayList<SingleTransponderConnnectionCallback>();
	private List<Callback.SingleClientConnnectionCallback> mSingleClientConnnectionCallbackList = new ArrayList<Callback.SingleClientConnnectionCallback>();
	/*private Object mClientSocketListLock = new Object();*/
	//private Object mTransponderConnecttionListLock = new Object();
	private Object mClientConnectionListLock = new Object();
	private boolean mIsRunning = true;
	private int fixed_transponder_port = 19910;
	
	public TransponderServerManager(int serverport) {
		fixed_transponder_port = serverport;
	}
	
	/*private SingleTransponderConnnectionCallback mSingleTransponderConnnectionCallback = new SingleTransponderConnnectionCallback() {
		@Override
		public void onTransponderConnnectionStatusChange(SingleTransponderConnection transponder, String info, String action) {
			// TODO Auto-generated method stub
			LogUtils.LOGD(TAG, "onTransponderConnnectionStatusChange transponder = " + transponder + ", info = " + info + ", action = " + action);
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
		
	};*/
	
	private SingleClientConnnectionCallback mSingleClientConnnectionCallback = new SingleClientConnnectionCallback() {
		@Override
		public void onClientStatusChange(SingleClientConnection client, String info, String action) {
			// TODO Auto-generated method stub
			LogUtils.LOGD(TAG, "SingleClientConnnectionCallback client = " + client + ", info = " + info + ", action = " + action);
			if (action != null && action.length() > 0) {
				switch (action) {
					case "add":
						addSingleClientConnection(client);
						break;
					case "remove":
						removeSingleClientConnection(client);
						break;
					case "exit":
						
						break;
					default:
						break;
				}
			}
		}
	};
	
	public void moreserver(){
		LogUtils.LOGD(TAG, "moreserver starting...");
        try {
        	mMainServer = new ServerSocket(fixed_transponder_port);
		} catch (Exception e) {
			LogUtils.TRACE(e);
			LogUtils.LOGD(TAG, "mMainServer creat Exception = " + e.getMessage());
		}
        try {
        	mMainServer.setReuseAddress(true);
        	//mMainServer.setSoTimeout(3000);
		} catch (Exception e) {
			LogUtils.TRACE(e);
			LogUtils.LOGD(TAG, "mMainServer setReuseAddress Exception = " + e.getMessage());
		}
        while (mIsRunning) {
        	Socket newSocket = null;
        	try {
        		LogUtils.LOGD(TAG, "listen port " + fixed_transponder_port + "...");
        		newSocket = mMainServer.accept();
        	} catch (Exception e) {
        		LogUtils.TRACE(e);
        		LogUtils.LOGD(TAG, "mMainServer.accept() Exception = " + e.getMessage());
        		continue;
        	}
        	try {
        		LogUtils.LOGD(TAG, "moreserver dealNewSocket!");
        		dealNewSocket(mMainServer, newSocket, mSingleClientConnnectionCallback);
	        } catch (Exception e) {
	        	LogUtils.TRACE(e);
	            LogUtils.LOGD(TAG, "while Exception = " + e.getMessage());
	        }
		}
        try {
        	if (mMainServer != null) {
        		mMainServer.close();
        	}
        	LogUtils.LOGD(TAG, "mMainServer.close() over!");
        } catch (Exception e) {
        	LogUtils.TRACE(e);
        	LogUtils.LOGD(TAG, "mMainServer.close() Exception = " + e.getMessage());
        }
        stopAll();
    }
	
	private void stopAll() {
		synchronized (mClientConnectionListLock) {
			Iterator<SingleClientConnection> iterator = mClientConnectionList.iterator();
			SingleClientConnection singleClientConnection;
			while (iterator.hasNext()) {
				singleClientConnection = (SingleClientConnection)iterator.next();
				singleClientConnection.stopRun();
			}
			mClientConnectionList.clear();
			LogUtils.LOGD(TAG, "stopAll clear mClientConnectionList");
		}
		/*synchronized (mTransponderConnecttionListLock) {
			Iterator<SingleTransponderConnection> iterator = mTransponderConnecttionList.iterator();
			SingleTransponderConnection singleTransponderConnection;
			while (iterator.hasNext()) {
				singleTransponderConnection = (SingleTransponderConnection)iterator.next();
				singleTransponderConnection.stopRun();
			}
			mTransponderConnecttionList.clear();
			LogUtils.LOGD(TAG, "stopAll clear mTransponderConnecttionList");
		}*/
	}
	
	private void dealNewSocket(ServerSocket server, Socket client, SingleClientConnnectionCallback clientcallback) {
		LogUtils.LOGD(TAG, "dealNewSocket server = " + server.getInetAddress() + ", client = " + client);
		SingleClientConnection singleClientConnection = getSingleClientConnection(client);
		if (singleClientConnection != null) {
			//update info
			LogUtils.LOGD(TAG, "dealNewSocket already exist singleClientConnection = " + singleClientConnection.toString());
			singleClientConnection.restartRun();
		} else {
			singleClientConnection = new SingleClientConnection(fixed_transponder_port, server, client, clientcallback);
			singleClientConnection.startRun();
			LogUtils.LOGD(TAG, "dealNewSocket start singleClientConnection = " + singleClientConnection.toString());
		}
	}
	
	/*private boolean containClient(Socket socket) {
		boolean result = false;
		if (mClientSocketList.contains(socket)) {
			result = true;
			LogUtils.LOGD(TAG, "containClient find socket = " + socket);
		} else {
			synchronized (mClientConnectionListLock) {
				Iterator<SingleClientConnection> iterator = mClientConnectionList.iterator();
				SingleClientConnection singleClientConnection;
				while (iterator.hasNext()) {
					singleClientConnection = (SingleClientConnection)iterator.next();
					if (singleClientConnection.getClientSocket() == socket) {
						result = true;
						LogUtils.LOGD(TAG, "containClient find singleClientConnection = " + singleClientConnection);
						break;
					}
				}
			}
		}
		return result;
	}*/
	
	private SingleClientConnection getSingleClientConnection(Socket socket) {
		SingleClientConnection result = null;
		synchronized (mClientConnectionListLock) {
			Iterator<SingleClientConnection> iterator = mClientConnectionList.iterator();
			SingleClientConnection singleClientConnection;
			while (iterator.hasNext()) {
				singleClientConnection = (SingleClientConnection)iterator.next();
				if (singleClientConnection.getClientSocket() == socket) {
					result = singleClientConnection;
					LogUtils.LOGD(TAG, "getSingleClientConnection find singleClientConnection = " + singleClientConnection);
					break;
				}
			}
		}
		return result;
	}
	
	/*private void addClientSocket(Socket add) {
		synchronized (mClientSocketListLock) {
			if (mClientSocketList.contains(add)) {
				LogUtils.LOGD(TAG, "addClientSocket already exist");
			} else {
				mClientSocketList.add(add);
				LogUtils.LOGD(TAG, "addClientSocket add = " + add);
			}
		}
	}
	
	private void removeClientSocket(Socket remove) {
		if (remove == null) {
			LogUtils.LOGD(TAG, "removeClientSocket null");
		}
		synchronized (mClientSocketListLock) {
			mClientSocketList.remove(remove);
			LogUtils.LOGD(TAG, "removeClientSocket remove = " + remove);
		}
	}*/
	
	private void addSingleClientConnection(SingleClientConnection add) {
		synchronized (mClientConnectionListLock) {
			if (mClientConnectionList.contains(add)) {
				LogUtils.LOGD(TAG, "addSingleClientConnection already exist");
			} else {
				mClientConnectionList.add(add);
				LogUtils.LOGD(TAG, "addSingleClientConnection add = " + add);
			}
		}
	}
	
	private void removeSingleClientConnection(SingleClientConnection remove) {
		if (remove == null) {
			LogUtils.LOGD(TAG, "removeSingleClientConnection null");
		}
		synchronized (mClientConnectionListLock) {
			mClientConnectionList.remove(remove);
			LogUtils.LOGD(TAG, "removeSingleClientConnection remove = " + remove);
		}
	}
	
	/*private void addSingleTransponderConnection(SingleTransponderConnection add) {
		synchronized (mTransponderConnecttionListLock) {
			if (mTransponderConnecttionList.contains(add)) {
				LogUtils.LOGD(TAG, "addSingleTransponderConnection already exist");
			} else {
				mTransponderConnecttionList.add(add);
				LogUtils.LOGD(TAG, "addSingleTransponderConnection add = " + add);
			}
		}
	}*/
	
	/*private void removeSingleTransponderConnection(SingleTransponderConnection remove) {
		if (remove == null) {
			LogUtils.LOGD(TAG, "removeSingleTransponderConnection null");
		}
		synchronized (mTransponderConnecttionListLock) {
			mTransponderConnecttionList.remove(remove);
			LogUtils.LOGD(TAG, "removeSingleTransponderConnection remove = " + remove);
		}
	}*/	
}
