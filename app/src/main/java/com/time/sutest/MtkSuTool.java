package com.time.sutest;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

public class MtkSuTool {
    private static final String TAG = "SuTool";
    private static String RELEASE_DIR_PATH = "/data/local/tmp";
    private static String RELEASE_FILE_PATH = RELEASE_DIR_PATH + "/su";

    private static Context mContext;

    private static MtkSuTool suTool;

    private WeakReference<CommandResultCallback> weakReferenceCommandResultCallback;

    private MtkSuTool(Context context) {
        this.mContext = context;
        RELEASE_DIR_PATH = context.getFilesDir().getAbsolutePath();
        RELEASE_FILE_PATH = RELEASE_DIR_PATH + "/su";
    }

    public static MtkSuTool getInstance(Context context) {
        if (suTool == null) {
            suTool = new MtkSuTool(context);
        }
        return suTool;
    }

    public void regCallBack(CommandResultCallback commandResultCallback) {
        weakReferenceCommandResultCallback = new WeakReference<>(commandResultCallback);
    }


    //释放su文件
    public boolean releaseSu(int res_mtk) {
        try {
            InputStream inputStream = mContext.getResources().openRawResource(res_mtk);
            if (inputStream == null) {
                Log.e(TAG, "releaseSu: inputStream null");
                return false;
            }
            File file = new File(RELEASE_FILE_PATH);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[8 * 1024];
            int length = -1;
            while ((length = inputStream.read(buffer)) != -1) {
                byte[] b = new byte[length];
                System.arraycopy(buffer, 0, b, 0, length);
                outputStream.write(b);
                outputStream.flush();
            }
            outputStream.close();
            inputStream.close();

            Runtime.getRuntime().exec("chmod +x " + RELEASE_FILE_PATH);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }

    public void runExec(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec("./" + RELEASE_FILE_PATH + " -c " + cmd);
            new ReadThread(process.getInputStream(), process.getErrorStream(), "read").start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ReadThread extends Thread {
        InputStream inputStream;
        InputStream errorInputStream;
        String name = "None Name";

        public ReadThread(InputStream inputStream, InputStream errorInputStream, String name) {
            this.inputStream = inputStream;
            this.errorInputStream = errorInputStream;
            this.name = name;
        }

        @Override
        public void run() {
            super.run();
            try {
                byte[] buffer = new byte[1024 * 8];
                int length = 0;

                while ((length = inputStream.read(buffer)) != -1) {
                    byte[] tmp = new byte[length];
                    System.arraycopy(buffer, 0, tmp, 0, length);
                    Log.i(TAG, "run: " + new String((tmp)));
                    if (weakReferenceCommandResultCallback != null && weakReferenceCommandResultCallback.get() != null) {
                        weakReferenceCommandResultCallback.get().onResult(new String(tmp));
                    }
                    buffer = new byte[1024 * 8];
                }

                while ((length = errorInputStream.read(buffer)) != -1) {
                    byte[] tmp = new byte[length];
                    System.arraycopy(buffer, 0, tmp, 0, length);
                    Log.i(TAG, "run: " + new String((tmp)));
                    if (weakReferenceCommandResultCallback != null && weakReferenceCommandResultCallback.get() != null) {
                        weakReferenceCommandResultCallback.get().onResult(new String(tmp));
                    }
                    buffer = new byte[1024 * 8];
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface CommandResultCallback {
        void onResult(String result);
    }


}
