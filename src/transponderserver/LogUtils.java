package transponderserver;

public class LogUtils {
	public static final String TAG = LogUtils.class.getSimpleName();
	
	public static boolean DEBUG_TRACE = true;
	public static boolean DEBUG_LOGD = true;
	public static boolean DEBUG_LOGE = true;
	
	public static void LOGD(String tag, String log) {
		if (DEBUG_LOGD) {
			System.out.println(tag + "---" + log);
		}
		/*RuntimeException ex = new RuntimeException(tag + "---" + log);
		ex.fillInStackTrace();
		ex.printStackTrace();*/
	}
	
	public static void LOGE(String tag, String log) {
		if (DEBUG_LOGE) {
			System.out.println(tag + "..." + log);
		}
	}
	
	public static void TRACE(Exception e) {
		if (DEBUG_TRACE && e != null) {
			e.printStackTrace();
		}
	}
}
