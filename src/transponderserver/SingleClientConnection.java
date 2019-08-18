package transponderserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import transponderserver.Callback.SingleClientConnnectionCallback;
import transponderserver.Callback.SingleTransponderConnnectionCallback;

public class SingleClientConnection {
	private static final String TAG = SingleClientConnection.class.getSimpleName();
	
	private Socket mClient;
	private ServerSocket mSourceServer;
	private SingleClientSocketThread mClientSocketThread;
	private SingleClientConnnectionCallback mSingleClientConnnectionCallback;
	private String mClientInfo = null;
	private int fixedServerPort = 19910;
	private SingleTransponderConnection mSingleTransponderConnection = null;
	
	public SingleClientConnection(int serverport, ServerSocket server, Socket client,SingleClientConnnectionCallback clientcallback/*, SingleTransponderConnnectionCallback transpondercallback*/) {
		mSourceServer = server;
		mClient = client;
		mSingleClientConnnectionCallback = clientcallback;
		fixedServerPort = serverport;
	}
	
	public String getClientInfo() {
		return mClientInfo;
	}
	
	public void setClientInfo(String clientinfo) {
		mClientInfo = clientinfo;
	}
	
	public Socket getClientSocket() {
		return mClient;
	}
	
	public void startRun() {
		mClientSocketThread = new SingleClientSocketThread(mClient);
		mClientSocketThread.startRun();
		mClientSocketThread.start();
	}
	
	public void stopRun() {
		if (mClientSocketThread != null) {
			mClientSocketThread.stopRun();
		}
	}
	
	public void restartRun() {
		LogUtils.LOGD(TAG, "restart will not run");
	}
	
	public class SingleClientSocketThread extends Thread {	
		private Socket mClient;
		private ServerSocket mRequestServer;
		private Socket mResponse;
		private Socket mRequest;
		private boolean mIsRunning;
		//private ReadWriteRunnable mTransponder;
		private SingleTransponderConnection mSingleTransponderConnection;
		
		public SingleClientSocketThread(Socket client) {
			mClient = client;
		}
		
		@Override
		public void run() {
			doListening();
		}
		
		private void doListening() {
			byte[] buffer = new byte[1024];   
	        InputStream is = null;
	        OutputStream os = null;
	        JSONObject command = null;
	        String receStr = null;
	        try {
	            is = mClient.getInputStream();
	            os = mClient.getOutputStream();
	            if (mSingleClientConnnectionCallback != null) {
	    			mSingleClientConnnectionCallback.onClientStatusChange(SingleClientConnection.this, "client", "add");
	    		}
	            while(mIsRunning) {
	                int size = is.read(buffer);
	                if (size > -1) {
	                	command = parseRequest(buffer, size);
	                	if (isResponseClient(command)) {
	                		if (mRequestServer == null) {
		                		initResponseConnection(command);
		                	} else {
		                		restartResponseConnection(command);
		                	}
	                	} else {
	                		receStr = parseReceiveString(buffer, size);
	                		LogUtils.LOGD(TAG, "doListening rece = " + receStr);
	                	}
	                } else {
	                	System.out.println("doListening size = -1");
	                	break;
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            System.out.println("doListening read Exception = " + e.getMessage());
	            if (mSingleClientConnnectionCallback != null) {
	    			mSingleClientConnnectionCallback.onClientStatusChange(SingleClientConnection.this, "client", "exception");
	    		}
	        } finally {
	        	
	        }
	        try {
	            if (is != null) {
	                is.close();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            System.out.println("doListening is.close() Exception = " + e.getMessage());
	        }
	        try {
	        	if (os != null) {
	                os.flush();
	                os.close();
	            }
	        } catch (Exception e) {
	             e.printStackTrace();
	             System.out.println("doListening os.close() Exception = " + e.getMessage());
	        }
	        try {
	        	if (mClient != null) {
	                mClient.close();
	            }
	        } catch (Exception e) {
	             e.printStackTrace();
	             System.out.println("doListening mClient.close() Exception = " + e.getMessage());
	        }
	        if (mSingleClientConnnectionCallback != null) {
    			mSingleClientConnnectionCallback.onClientStatusChange(SingleClientConnection.this, "client", "exit");
    		}
		}
		
		private void responseAllowedToClient(Socket socket) {
			if (socket != null && socket.isConnected()) {
				JSONObject responseObj = new JSONObject();
				responseObj.put("request_host", "opendiylib.com");
				responseObj.put("request_port", 19910);
				responseObj.put("response_host", "127.0.0.1");
				responseObj.put("response_port", 3389);
				responseObj.put("request_status", "allowed");
				responseObj.put("request_password", "#qwertyuiop789456123zxcvbnm,.$");
				try {
					socket.getOutputStream().write(responseObj.toString().getBytes());
					socket.getOutputStream().flush();
				} catch (Exception e) {
					LogUtils.TRACE(e);
					LogUtils.LOGE(TAG, "responseAllowedToClient Exception = " + e.getMessage());
				}
			} else {
				LogUtils.LOGD(TAG, "responseAllowedToClient null os");
			}
		}
		
		private JSONObject parseRequest(byte[] buffer, int length) {
			JSONObject object = null;
			String str = null;
			try {
				str = new String(buffer, 0, length);
				object = new JSONObject(str);
			} catch (Exception e) {
				e.printStackTrace();
	            System.out.println("parseRequest Exception = " + e.getMessage() + ", str = " + str);
			}
			return object;
		}
		
		private String parseReceiveString(byte[] buffer, int length) {
			String result = null;
			try {
				result = new String(buffer, 0, length);
			} catch (Exception e) {
				e.printStackTrace();
	            System.out.println("parseReceiveString Exception = " + e.getMessage() + ", result = " + result);
			}
			return result;
		}
		
		private boolean isResponseClient(JSONObject commandobj) {
			boolean result = false;
			if (commandobj != null && commandobj.length() > 0) {
				try {
					String requesthost = commandobj.getString("request_host");
					int requestport= commandobj.getInt("request_port");
					String responsehost = commandobj.getString("response_host");
					int responseport = commandobj.getInt("response_port");
					String requestStatus = commandobj.getString("request_status");
					String password = commandobj.getString("request_password");
					if ("opendiylib".equals(requesthost) &&
							"127.0.0.1".equals(responsehost) &&
							"ready".equals(requestStatus) &&
							"#qwertyuiop789456123zxcvbnm,.$".equals(password)) {
						result = true;
					}
				} catch (Exception e) {
					LogUtils.TRACE(e);
					LogUtils.LOGE(TAG, "isReponseClient Exception = " + e.getMessage());
				}
			}
			return result;
		}
		
		private boolean isRequestClient(JSONObject command) {
			boolean result = false;
			
			return result;
		}
		
		private boolean isResponseConnected() {
			boolean result = false;
			if (mResponse != null && mResponse.isConnected()) {
				result = true;
			}
			return result;
		}
		
		private void initResponseConnection(JSONObject commandobj) {
			if (commandobj != null && commandobj.length() > 0) {
				try {
					String requesthost = commandobj.getString("request_host");
					int requestport = commandobj.getInt("request_port");
					String responsehost = commandobj.getString("response_host");
					int responseport = commandobj.getInt("response_port");
					String requestStatus = commandobj.getString("request_status");
					String password = commandobj.getString("request_password");
					if ("opendiylib".equals(requesthost) &&
							"127.0.0.1".equals(responsehost) &&
							"ready".equals(requestStatus) &&
							"#qwertyuiop789456123zxcvbnm,.$".equals(password)) {
						if (mSingleTransponderConnection == null) {
							mSingleTransponderConnection = new SingleTransponderConnection(commandobj);
							mSingleTransponderConnection.startRun();
						} else {
							mSingleTransponderConnection.restartRun(commandobj);
						}
					}
				} catch (Exception e) {
					LogUtils.TRACE(e);
					LogUtils.LOGE(TAG, "initResponseConnection Exception = " + e.getMessage());
				}
			}
		}
		
		private void restartResponseConnection(JSONObject commandobj) {
			
		}
		
		private TimerTask timerTask;
		private Timer timer;
		
		public void restart() {
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
			if (timerTask != null) {
				timerTask.cancel();
				timerTask = null;
			}
			timerTask = getTimerTask();
			timer = getTimer();
			timer.schedule(timerTask, 3000);
		}
		
		private TimerTask getTimerTask() {
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					
				}
			};
			return timerTask;
		}
		
		private Timer getTimer() {
			Timer timer = new Timer();
			return timer;
		}
		
		public void startRun() {
			mIsRunning = true;
		}
		
		public void stopRun() {
			mIsRunning = false;
		}
		
		private void stopAll() {
			if (mClient != null) {
				try {
					mClient.close();
				} catch (Exception e) {
					LogUtils.TRACE(e);
					LogUtils.LOGE(TAG, "stopAll Exception = " + e.getMessage());
				}
			}
		}
	}
	
	
}
