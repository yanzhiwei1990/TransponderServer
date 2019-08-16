package transponderserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ReadWriteRunnable implements Runnable {

    private Socket readSocket;
    private Socket writeSocket;
    
    private boolean mIsRunning = true;
    private boolean mFinalStop = false;

    public ReadWriteRunnable(Socket readSocket, Socket writeSocket) {
        this.readSocket = readSocket;
        this.writeSocket = writeSocket;
    }

    public void setRun(boolean status) {
    	mIsRunning = status;
    }
    
    public void setFinalStop(boolean status) {
    	mFinalStop = status;
    }
    
    public boolean getFinalStop() {
    	return mFinalStop;
    }
    
    @Override
    public void run() {
        byte[] b = new byte[1024*10];   
        InputStream is = null;
        OutputStream os = null;
        try {
            is = readSocket.getInputStream();
            os = writeSocket.getOutputStream();
            while(mIsRunning && !readSocket.isClosed() && !writeSocket.isClosed()){
                int size = is.read(b); 
                if (size > -1) {
                    os.write(b, 0, size);
                } else {
                	System.out.println("ReadWriteRunnable size = -1");
                	break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("is.close() Exception = " + e.getMessage());
            }
            try {
            	if (null != os) {
                    os.flush();
                    os.close();
                }
            } catch (Exception e) {
                 e.printStackTrace();
                 System.out.println("os.close() Exception = " + e.getMessage());
            }
            
        }
        mFinalStop = true;
    }

}
