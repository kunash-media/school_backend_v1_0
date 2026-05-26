package com.sc.bcrypt;


import org.springframework.security.crypto.bcrypt.BCrypt;

public class BcryptEncoderConfig {

    public String encode(CharSequence rawPassword) {
        return BCrypt.hashpw(rawPassword.toString(), BCrypt.gensalt());
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
    }
}