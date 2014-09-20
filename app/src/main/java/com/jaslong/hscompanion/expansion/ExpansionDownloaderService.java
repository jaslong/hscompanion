package com.jaslong.hscompanion.expansion;

import com.google.android.vending.expansion.downloader.impl.DownloaderService;

public class ExpansionDownloaderService extends DownloaderService {

    public static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArVJrvgrwUeRn48VN5p3FIPUrUPVKrSTCh3JfuTaja+whJzJnaET0bWv+AotBRY+PK6zB03NOelxV8vod8MAKwho4snpj9wNh25IGC0wweOXfQ0x9eXpV1Vr4hAa/PZxtaWpwkXQ8lm9DQ5no1+FJGu2HQiLvShscyrFsJu1pQRmJZkEu+ByS67Os1fLi0f9lq0fVoywIscSqvkRJ08szO/oBai26wpdDZVEnIuNvMguqoIiRczXr03Qs4+sJr6uoyCdCziwwgJY8y+MIbUwR41Si/Xr8jOPJMp+BEzXnOuLW/0dKS7SPHew5Vlro8xSJfmNXDTNlj20SMG61h/pX/wIDAQAB";
    public static final byte[] SALT = new byte[] {
            90, 24, -123, 17, 74, 55, -51, 12, 95, -100, -4, -92, -38, 92, -2, -108,
            -96, -68, -106, 7, 88, 16, 41, 118, 96, 44, 61, -111, -2, 29, -34, -46,
            41, 3, 65, 87, 60, -34, -36, -35, -109, -10, 66, 28, 71, -101, -70, -34,
            62, 99, 120, 109, -46, -93, 6, 25, 40, -20, 48, -21, -112, -42, -122, -31 };

    @Override
    public String getPublicKey() {
        return BASE64_PUBLIC_KEY;
    }

    @Override
    public byte[] getSALT() {
        return SALT;
    }

    @Override
    public String getAlarmReceiverClassName() {
        return ExpansionAlarmReceiver.class.getName();
    }

}
