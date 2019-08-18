package transponderserver;

import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;

import transponderserver.Callback.ReadWriteConnectionCallback;
import transponderserver.Callback.ReadWriteConnectionStatusCallback;
import transponderserver.Callback.SingleTransponderConnnectionCallback;

public class SingleTransponderConnection {
	private static final String TAG = SingleTransponderConnection.class.getSimpleName();
	
	private Socket mRequest;
	private ServerSocket mTransponderServer;
	private Socket mResponse;
	private boolean mIsRunning = true;
	private JSONObject mObjInfo = null;
	private int mTransponderPort = -1;
	
	private TransponderSocketThread mFromToTo;
	private TransponderSocketThread mToToFrom;
	
	private SingleTransponderConnnectionCallback mSingleTransponderConnnectionCallback = new SingleTransponderConnnectionCallback() {
		@Override
		public void onTransponderConnnectionStatusChange(SingleTransponderConnection transponder, String info, String action) {
			// TODO Auto-generated method stub
			LogUtils.LOGD(TAG, "onTransponderConnnectionStatusChange transponder = " + transponder + ", info = " + info + ", action = " + action);
			if (action != null && action.length() > 0) {
				switch (action) {
					case "add":
						
						break;
					case "remove":
						
						break;
					default:
						break;
				}
			}
		}
		
	};
	
	public SingleTransponderConnection(JSONObject jsonInfo) {
		mObjInfo = jsonInfo;
		initParameter(mObjInfo);
	}
	
	private void initParameter(JSONObject jsonInfo) {
		if (jsonInfo != null && jsonInfo.length() > 0) {
			try {
				mTransponderPort = jsonInfo.getInt("request_port");
			} catch (Exception e) {
				LogUtils.TRACE(e);
				LogUtils.LOGE(TAG, "initParameter Exception = " + e.getMessage());
			}
		}
	}
	
	public void startRun() {
		if (mTransponderPort != -1) {
			startRequestServer(mTransponderPort);
		}
	}
	
	public void restartRun(JSONObject objStr) {
		initParameter(objStr);
	}
	
	public void stopRun() {
		
	}
	
	private void startRequestServer(int port){
		LogUtils.LOGD(TAG, "startRequestServer starting " + port + "...");
        try {
        	mTransponderServer = new ServerSocket(port);
		} catch (Exception e) {
			LogUtils.TRACE(e);
			LogUtils.LOGE(TAG, "startRequestServer creat Exception = " + e.getMessage());
			return;
		}
        try {
        	mTransponderServer.setReuseAddress(true);
        	mTransponderServer.setSoTimeout(3000);
		} catch (Exception e) {
			LogUtils.TRACE(e);
			LogUtils.LOGE(TAG, "startRequestServer setReuseAddress Exception = " + e.getMessage());
			return;
		}
        while (mIsRunning) {
        	Socket newSocket = null;
        	try {
        		newSocket = mTransponderServer.accept();
        	} catch (Exception e) {
        		LogUtils.TRACE(e);
        		LogUtils.LOGE(TAG, "mRequestServer.accept() Exception = " + e.getMessage());
        		continue;
        	}
        	try {
        		LogUtils.LOGE(TAG, "startRequestServer dealNewSocket!");
        		
	        } catch (Exception e) {
	        	LogUtils.TRACE(e);
	            LogUtils.LOGE(TAG, "while Exception = " + e.getMessage());
	        }
		}
        try {
        	if (mTransponderServer != null) {
        		mTransponderServer.close();
        	}
        	System.out.println("mRequestServer.close() over!");
        } catch (Exception e) {
        	e.printStackTrace();
    		System.out.println("mRequestServer.close() Exception = " + e.getMessage());
        }
        stopAll();
    }
	
	public void stopAll() {
		
	}
	
	/*public void startConnect() {
		mFromToTo = new TransponderSocketThread(mRequest, mTransponderServer, mResponse);
		mToToFrom = new TransponderSocketThread(mResponse, mTransponderServer, mRequest);
		mFromToTo.start();
		mToToFrom.start();
	}*/
	
	/*public void stopRun() {
		if (mFromToTo != null) {
			mFromToTo.stopRun();
		}
		if (mToToFrom != null) {
			mToToFrom.stopRun();
		}
	}*/
	
	public class TransponderSocketThread extends Thread {

		private Socket mFrom;
		private ServerSocket mServer;
		private Socket mTo;
		
		private ReadWriteConnection mReadWriteConnection = null;
		
		public TransponderSocketThread(Socket from, ServerSocket server, Socket to) {
			mFrom = from;
			mServer = server;
			mTo = to;
			mReadWriteConnection = new ReadWriteConnection(mFrom, mTo, mReadWriteConnectionCallback);
		}
		
		private ReadWriteConnectionCallback mReadWriteConnectionCallback = new ReadWriteConnectionCallback() {
			@Override
			public void onReadWriteConnectionCallbackChange(Socket request, Socket response, String flag, String status) {
				LogUtils.LOGD(TAG, "onReadWriteConnectionCallbackChange flag = " + flag + ", status = " + status);
				if (request != null && response != null) {
					if ("request".equals(flag)) {
						if ("exception".equals(status)) {
							//restart();
						} else if ("exit".equals(status)) {
							//restart();
						}
					} else if ("response".equals(flag)) {
						if ("exception".equals(status)) {
							
						} else if ("exit".equals(status)) {
							
						}
					}
				}
			}
		};
	
		private void startTransponder() {
			
		}
		
		public void run() {
			startTransponder();
		}
		
		public void stopRun() {
			
		}
	}
}
