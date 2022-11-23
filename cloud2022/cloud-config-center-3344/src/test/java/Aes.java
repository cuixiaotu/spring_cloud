//帮忙写个aes解密demon

import cn.hutool.crypto.SecureUtil;
import org.apache.commons.lang.StringUtils;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;

public class Aes {


    private static final String  str = "kCW7Mv0Se8uQFCRMBCth+BXAhnIBVlIT7yVEjG0Wz7Ii0tnHaHhBwUgDvIuwKmDXBNsHW6VsnC14wlOaFvbpLnFJHmcxcaRqf6rp6EYhcsP2DpkK52nrvaqyhRWCoXPGMBr0oAoXRYqtsrUwPJljONpp6NwNrBHnJdcfAoFcvzZk7VXE1kEWYpnwf/h2fM5yoefqDVlP574MyGgZDb5MD9vUsQ/ZxRmP/QEshvhMFG7d1ZTBZ2pIopCGtjz/BlbiVXM9Bf5Qr6bte5J4oLHBDyr+uEmKPskdmiuTuq7wE4IJyNgqhoglvHpS2DH39ZgUHCutVm7vrH/UQWRSmybhZK7eHGQWU01xaRphzIQCfjcwlcWTxFfCEf1bsWaYSTciPqoSafruoWEH+a4+fxC18VO1AB1xQu8lQbx4YL3ck2VP6LzHFtNGenoI9KgqMgqGM3syNFXpW5HWgZk1ueGXvts1pmRGACN7ILlylhYE+npzF+WfIN2Yc1dII2tCiXZLKs6qMXNdOsiQ+0Gspo/eEoycCLiilx/PTwXsfaXBXzCoR4q0kZIG8MwxVHjXH9uCwNilhuvCV/BfdTVjBlOK8Qe0T30Ep/MSd+Sjx83DFix/cqzghdAPMk/1quW4qo0mHkICScY2xuw+0HBAJ/7CVQyGbWQ01ToWWSJ7kYGHWmf0YiMVi1FNzf7D6tTSjCmvih3ktrRXksV+CoJ8gIOMbtx5ZNPD6FcEtynD6Lri/S6spm5NIgc3e2p8nFNwo7d3i2RA8LHeYMhQOvIhvfhUI9OPTKPX1f8NOiDRuGcue8688oG3lIBW3hn3WnLGQxLN5SxGFGXM20zHsUeyTdvRT9vMA4fGKgoYATWA7gLMHPp1El3K+IbGwEGdeEm2SsYbFcRrtMri4bzDAjFJkuTVmEq2o+5rYG5eIuAkUxg0ozqMU6TZkiVASc9oMWbLpQq2WYFdo8ya+dwvuCLWQQu74+21LdQHiw2gEWFrGBKlhWWbyI0QX71u43SueBY8jokbE9BECiJh1/JkGept0MGQbCCws6l2Wyjs5yR+9U55eDi8B8684AEwcC6sTdh8YeXS8gH0XFFTfOD7j/cYnErlKCaEaJ+TPqtXBynzUProksD1+QLXVTngoAmdIRW3iZea+0/bzZ2UQcCZ8PY5F4YkNurTkrG8Czg190KQmJB/KbnBaiA3Pt58TGkDEuJO2Ui2tc2biH8LMvhMhnNwAQhjF4uKSkuqmN8VPEpoB8m7F4T4McedrZwprKUbOnhHQWIZf6/ek0xOfAK1+g1iV4xAr18sLYDvXqF49ENWerHDBU6cDXWu/Ymz+9a3KmwlVSJR8wOVnrMahzMn9RMqdTfwGuZa6pvUQ//65O9KpQk5lutR6W51taA3y1zMFPnD8ZFKv8+8mkJIFEYKDxhubg6ZJV6j7UYHOk5gT6wL+0KC6KmWXf/ULT4yLJDgeCdsI45sjLMF4BpfkVYkrNxbIjiXFLAjHLCBuBw08Tsf/I+n033LmoqHNixZ21mcXpIjcJO1pLEQYoFyVtZo1ljaFKJsHv51vlSES9c8ykxNEIhdHrVbhz5TS8lFrVtL+0ErQvOv5JZ6tKOyyvLhwJwOWpo48A==";
    private static final String  scert = "FnzPoYnMmYLzvfZmn";
    private static final String  ENCODING = "UTF-8";
    private static final String  AES_ALGORITHM = "AES";

    //填充方式
    private static final String CIPHER_PADDING = "AES/ECB/PKCS5Padding";
    //偏移量
    private static final String IV_SEED = "1234567";

    public static void main(String[] args) {

       // SecureUtil.


    }

    public static String encrypt(String content, String scert) {
//        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//        KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
//        SecretKey tmp = factory.generateSecret(spec);
//        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
//
//        String ase = SHA256
//
//
//        if (StringUtils.isNotBlank(aesKey)) {}
        return "123";
    }

}
