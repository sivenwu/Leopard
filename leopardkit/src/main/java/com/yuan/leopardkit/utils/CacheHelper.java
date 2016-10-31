package com.yuan.leopardkit.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.yuan.leopardkit.models.CacheModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by Yuan on 2016/10/30.
 * Detail cahe for http
 */

public class CacheHelper {

    private final static String TAG = "CacheHelper";
    private static CacheHelper helper;

    public static  long VALID_TIME = 24 * 60 *60;//默认缓存有效时间为2天

    private Context mC;
    private AESHleper aesHleper = new AESHleper();
    private DiskLruCache cache;

    private File cacheFile;
    private String cachePath = "";
    private String cacheDirName = "LEOPARD_";

    public static final String CHARSET_NAME = "UTF-8";
    private static final String TMP_CHAR = "-leopard-";
    public static final int TYPE_ONLINE = 0;//标记在线缓存
    public static final int TYPE_OFFLINE = 1;//标记离线缓存

    //disk
    private int version = 1;
    private int valueCout = 2;//0 记录头信息 1 记录内容
    private long cacheSize = 1024 * 1024 *1;

    //cache index info 0 记录头信息 1 记录内容
    private final int INDEX_HEADER = 0;
    private final int INDEX_DATA = 1;


    public CacheHelper(Context mC) {
        this.mC = mC;
        init();
    }

    private void init(){
        //init...
//        cachePath = Environment.getExternalStorageDirectory().toString() + "/yuan_cache";
        cachePath = this.mC.getCacheDir().toString();
        cacheFile = getCacheFile(0);
        Log.i(TAG,"[Cache Path] "+cacheFile.getPath().toString());
        try {
            cache = DiskLruCache.open(cacheFile,version,valueCout,cacheSize);
        } catch (IOException e) {
            Log.e(TAG,"[DiskLruCache init] "+e.getMessage().toString());
            e.printStackTrace();
        }
    }

    public long getCacheSize() {
        return cacheSize;
    }

    public File getCacheFile(int type) {
        return new File(cachePath,cacheDirName + "CACHE_"+type);
    }

    public void putCache(String key,CacheModel model, long validTime){

        if (cache == null){
            Log.e(TAG,"[DiskLruCache init] ERROR");
            return;
        }

        // key 进行md5编码
        key = aesHleper.toMD5(key);
        Log.i(TAG,"[ASE putCache KEY] "+key);
        try {
            DiskLruCache.Editor editor = cache.edit(key);
            if (editor== null) return;
            OutputStream osHeader = editor.newOutputStream(INDEX_HEADER);
            OutputStream osData = editor.newOutputStream(INDEX_DATA);

            if (displayOutputStream(signValueByTime(model.getResponseData(),validTime),osData,true)&&displayOutputStream(model.getHeaders(),osHeader,false)){
                editor.commit();
            }else {
                editor.abort();
            }
            // 每次存储必须flush去改变修改记录，不过建议放在视图层的onpause
            cache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void putCache(String key,CacheModel model){

        if (cache == null){
            Log.e(TAG,"[DiskLruCache init] ERROR");
            return;
        }

        // key 进行md5编码
        key = aesHleper.toMD5(key);
        Log.i(TAG,"[ASE putCache KEY] "+key);
        try {
            DiskLruCache.Editor editor = cache.edit(key);
            if (editor== null) return;
            OutputStream osHeader = editor.newOutputStream(INDEX_HEADER);
            OutputStream osData = editor.newOutputStream(INDEX_DATA);

            if (displayOutputStream(signValueByTime(model.getResponseData()),osData,true)&&displayOutputStream(model.getHeaders(),osHeader,false)){
                editor.commit();
            }else {
                editor.abort();
            }
            // 每次存储必须flush去改变修改记录，不过建议放在视图层的onpause
            cache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CacheModel getCache(String key){

        CacheModel model = null;

        byte data[] = null;
        byte header[] = null;

        if (cache == null){
            Log.e(TAG,"[DiskLruCache init] ERROR");
            return null;
        }

        try {
            key = aesHleper.toMD5(key);
            Log.i(TAG,"[ASE getCache KEY] "+key);
            DiskLruCache.Snapshot snapshot = cache.get(key);
            if (snapshot == null) return model;

            InputStream isHeader = snapshot.getInputStream(INDEX_HEADER);
            InputStream isData = snapshot.getInputStream(INDEX_DATA);

            data = displayInputStream(isData,true);
            header = displayInputStream(isHeader,false);
            if ((data = isValidCahe(data))== null){
                Log.e(TAG,"[Cache state]"+" no vaild!!");
                cache.remove(aesHleper.toMD5(key));
            }
            model = new CacheModel(header,data);
            return model;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return model;
    }

    public byte[] isValidCahe(byte[] value) throws UnsupportedEncodingException {
        String data = new String(value,CHARSET_NAME);
        String dataArr[] = data.split(TMP_CHAR);
        String tmpArr[] = dataArr[0].split("-");

        long maxTime = Long.parseLong(tmpArr[1]);
        long nowTime = System.currentTimeMillis()/1000;
        if (nowTime< maxTime){
            return dataArr[1].getBytes();
        }
        return null;
    }

    /**
     * 缓存处理
     * @param value
     * @param os
     * @param isAES 是否内容要进行加密
     * @return
     */
    private boolean displayOutputStream(byte[] value,OutputStream os,boolean isAES){
        if (value == null || os==null) return  false;
        try {
            if (isAES)
            value = aesHleper.encode(new String(value,CHARSET_NAME)).getBytes();

            os.write(value,0,value.length);
            os.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[]  displayInputStream(InputStream is,boolean isAES) throws UnsupportedEncodingException {
        if (is == null) return null;
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        try {
            int len = 0;
            while ( (len = is.read(buffer)) != -1){
                outSteam.write(buffer,0,len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                outSteam.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (isAES) {
            return aesHleper.decode(new String(outSteam.toByteArray(), CHARSET_NAME)).getBytes();
        }

        return outSteam.toByteArray();
    }

    public byte[] signValueByTime(byte[] value){
        String newVaule = null;
        try {
            long secTime = System.currentTimeMillis()/1000;
            newVaule = secTime +"-"+(VALID_TIME+secTime)+ TMP_CHAR + new String(value,CHARSET_NAME);
            return newVaule.getBytes();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] signValueByTime(byte[] value,long time){
        String newVaule = null;
        try {
            long secTime = System.currentTimeMillis()/1000;
            newVaule = secTime +"-"+(time+secTime)+ TMP_CHAR + new String(value,CHARSET_NAME);
            return newVaule.getBytes();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static class AESHleper{

        private final String ALOGOR = "AES/ECB/PKCS5Padding";
        private final String AES_PWD = "LeopardHttpCache";

        public String encode(String value){
            return toHex(encrypt(value, AES_PWD));
        }

        public String decode(String codeValue) throws UnsupportedEncodingException {
            byte[] b = decrypt(toBytes(codeValue), AES_PWD);
            return new String(b,CHARSET_NAME);
        }

        public String encode(String value,String pwd){
            return toHex(encrypt(value, pwd));
        }

        public String decode(String codeValue,String pwd) throws UnsupportedEncodingException {
            byte[] b = decrypt(toBytes(codeValue), pwd);
            return new String(b,CHARSET_NAME);
        }

        private  byte[] encrypt(String content, String password) {
            try {
                byte[] keyStr = getKey(password);
                SecretKeySpec key = new SecretKeySpec(keyStr, "AES");
                Cipher cipher = Cipher.getInstance(ALOGOR);//algorithmStr
                byte[] byteContent = content.getBytes("utf-8");
                cipher.init(Cipher.ENCRYPT_MODE, key);//   ʼ
                byte[] result = cipher.doFinal(byteContent);
                return result; //
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }
            return null;
        }

        private  byte[] decrypt(byte[] content, String password) {
            try {
                byte[] keyStr = getKey(password);
                SecretKeySpec key = new SecretKeySpec(keyStr, "AES");
                Cipher cipher = Cipher.getInstance(ALOGOR);
                cipher.init(Cipher.DECRYPT_MODE, key);
                byte[] result = cipher.doFinal(content);
                return result; //
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }
            return null;
        }

        private byte[] getKey(String password) {
            byte[] rByte = null;
            if (password!=null) {
                rByte = password.getBytes();
            }else{
                rByte = new byte[24];
            }
            return rByte;
        }

        public String toMD5(String key) {
            String cacheKey;
            try {
                final MessageDigest mDigest = MessageDigest.getInstance("MD5");
                mDigest.update(key.getBytes());
                cacheKey = toHex(mDigest.digest());
            } catch (NoSuchAlgorithmException e) {
                cacheKey = String.valueOf(key.hashCode());
            }
            return cacheKey;
        }

        public static String toHex(byte buf[]) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < buf.length; i++) {
                String hex = Integer.toHexString(buf[i] & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                sb.append(hex.toUpperCase());
            }
            return sb.toString();
        }

        public static byte[] toBytes(String hexStr) {
            if (hexStr.length() < 1)
                return null;
            byte[] result = new byte[hexStr.length() / 2];
            for (int i = 0; i < hexStr.length() / 2; i++) {
                int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
                int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
                        16);
                result[i] = (byte) (high * 16 + low);
            }
            return result;
        }

    }

}
