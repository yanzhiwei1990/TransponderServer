package transponderserver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;

import transponderserver.Callback.SingleTransponderConnnectionCallback;

public class SingleClientSocketThread extends Thread {

	private Socket mClient;
	private SingleTransponderConnnectionCallback mSingleTransponderConnnectionCallback;
	private ServerSocket mRequestServer;
	private Socket mResponse;
	private Socket mRequest;
	private boolean mIsRunning;
	private ReadWriteRunnable mTransponder;
	
	public SingleClientSocketThread(Socket client, SingleTransponderConnnectionCallback singleTransponderConnnectionCallback) {
		mClient = client;
		mSingleTransponderConnnectionCallback = singleTransponderConnnectionCallback;
	}
	
	private void startNewRequestTransponder() {
		
	}
	
	public void startRequestServer(int port){
    	System.out.println("startRequestServer starting...");
        try {
        	mRequestServer = new ServerSocket(port);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("startRequestServer creat Exception = " + e.getMessage());
			return;
		}
        try {
        	mRequestServer.setReuseAddress(true);
        	mRequestServer.setSoTimeout(3000);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("startRequestServer setReuseAddress Exception = " + e.getMessage());
			return;
		}
        while (mIsRunning) {
        	Socket newSocket = null;
        	try {
        		System.out.println("listen port " + port + "...");
        		newSocket = mRequestServer.accept();
        	} catch (Exception e) {
        		System.out.println("mRequestServer.accept() Exception = " + e.getMessage());
        		continue;
        	}
        	try {
        		System.out.println("startRequestServer dealNewSocket!");
        		dealRequestSocket(newSocket);
	        } catch (Exception e) {
	            e.printStackTrace();
	            System.out.println("while Exception = " + e.getMessage());
	        }
		}
        try {
        	if (mRequestServer != null) {
        		mRequestServer.close();
        	}
        	System.out.println("mRequestServer.close() over!");
        } catch (Exception e) {
        	e.printStackTrace();
    		System.out.println("mRequestServer.close() Exception = " + e.getMessage());
        }
        stopAll();
    } 
	
	private void dealRequestSocket(Socket socket) {
		byte[] buffer = new byte[1024];   
        InputStream is = null;
        OutputStream os = null;
        PrintWriter printWriter = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        String receive = null;
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
            printWriter = new PrintWriter(os);
            inputStreamReader = new InputStreamReader(is);
            bufferedReader = new BufferedReader(inputStreamReader);
            while ((receive = bufferedReader.readLine()) != null) {
                System.out.println("client:" + receive);
            }

            while(mIsRunning && !socket.isClosed()) {
                int size = is.read(buffer);
                if (size > -1) {
                	receive = bufferedReader.readLine();
                	parseRequest(receive);
                } else {
                	System.out.println("dealRequestSocket size = -1");
                	break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("dealRequestSocket read Exception = " + e.getMessage());
        } finally {
        	try {
                if (bufferedReader != null) {
                	bufferedReader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("dealRequestSocket bufferedReader.close() Exception = " + e.getMessage());
            }
        	try {
                if (inputStreamReader != null) {
                	inputStreamReader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("dealRequestSocket inputStreamReader.close() Exception = " + e.getMessage());
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("dealRequestSocket is.close() Exception = " + e.getMessage());
            }
            try {
                if (printWriter != null) {
                	printWriter.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("dealRequestSocket printWriter.close() Exception = " + e.getMessage());
            }
            try {
            	if (null != os) {
                    os.flush();
                    os.close();
                }
            } catch (Exception e) {
                 e.printStackTrace();
                 System.out.println("dealRequestSocket os.close() Exception = " + e.getMessage());
            }
        }
	}
	
	private void parseRequest(String str) {
		JSONObject object = null;
		try {
			object = new JSONObject(str);
		} catch (Exception e) {
			e.printStackTrace();
            System.out.println("parseRequest Exception = " + e.getMessage() + ", str = " + str);
		}
	}
	
	private void stopAll() {
		
	}
	
	@Override
	public void run() {
		
	}
}
