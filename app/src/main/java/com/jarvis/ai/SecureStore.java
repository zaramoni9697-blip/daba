package com.jarvis.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import android.util.Base64;

public final class SecureStore {
    private static final String KS="AndroidKeyStore", ALIAS="JARVIS_KEY_V1", PREF="jarvis_secure", SEP="::";
    private static SecretKey key() throws Exception {
        KeyStore ks=KeyStore.getInstance(KS); ks.load(null);
        if(!ks.containsAlias(ALIAS)){
            KeyGenerator kg=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,KS);
            kg.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());
            kg.generateKey();
        }
        return ((KeyStore.SecretKeyEntry)ks.getEntry(ALIAS,null)).getSecretKey();
    }
    public static void put(Context c,String name,String value){
        try{
            Cipher cp=Cipher.getInstance("AES/GCM/NoPadding"); cp.init(Cipher.ENCRYPT_MODE,key());
            String packed=Base64.encodeToString(cp.getIV(),Base64.NO_WRAP)+SEP+
                Base64.encodeToString(cp.doFinal(value.getBytes(StandardCharsets.UTF_8)),Base64.NO_WRAP);
            prefs(c).edit().putString(name,packed).apply();
        }catch(Exception ignored){}
    }
    public static String get(Context c,String name){
        try{
            String packed=prefs(c).getString(name,null); if(packed==null)return null;
            String[] p=packed.split(SEP,2);
            Cipher cp=Cipher.getInstance("AES/GCM/NoPadding");
            cp.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(p[0],Base64.NO_WRAP)));
            return new String(cp.doFinal(Base64.decode(p[1],Base64.NO_WRAP)),StandardCharsets.UTF_8);
        }catch(Exception e){return null;}
    }
    private static SharedPreferences prefs(Context c){return c.getSharedPreferences(PREF,Context.MODE_PRIVATE);}
}
