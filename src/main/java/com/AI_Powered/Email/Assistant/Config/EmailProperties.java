package com.AI_Powered.Email.Assistant.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for email services.
 * This class maps properties from application.properties with the 'email' prefix.
 * 
 * You can also override these values using environment variables:
 * - EMAIL_ACCOUNT_ADDRESS for email.account.address
 * - EMAIL_IMAP_HOST for email.imap.host
 * - EMAIL_SMTP_HOST for email.smtp.host
 * - EMAIL_SMTP_PORT for email.smtp.port
 * - EMAIL_SMTP_SSL_TRUST for email.smtp.ssl.trust
 *
 * Example using environment variables:
 * export EMAIL_ACCOUNT_ADDRESS=myemail@gmail.com
 */
@Configuration
@ConfigurationProperties(prefix = "email")
public class EmailProperties {
    
    private Account account = new Account();
    private Imap imap = new Imap();
    private Smtp smtp = new Smtp();
    
    public Account getAccount() {
        return account;
    }
    
    public void setAccount(Account account) {
        this.account = account;
    }
    
    public Imap getImap() {
        return imap;
    }
    
    public void setImap(Imap imap) {
        this.imap = imap;
    }
    
    public Smtp getSmtp() {
        return smtp;
    }
    
    public void setSmtp(Smtp smtp) {
        this.smtp = smtp;
    }
    
    /**
     * Account settings.
     */
    public static class Account {
        private String address;
        
        public String getAddress() {
            return address;
        }
        
        public void setAddress(String address) {
            this.address = address;
        }
    }
    
    /**
     * IMAP server settings.
     */
    public static class Imap {
        private String host;
        
        public String getHost() {
            return host;
        }
        
        public void setHost(String host) {
            this.host = host;
        }
    }
    
    /**
     * SMTP server settings.
     */
    public static class Smtp {
        private String host;
        private int port;
        private String sslTrust;
        
        public String getHost() {
            return host;
        }
        
        public void setHost(String host) {
            this.host = host;
        }
        
        public int getPort() {
            return port;
        }
        
        public void setPort(int port) {
            this.port = port;
        }
        
        public String getSslTrust() {
            return sslTrust;
        }
        
        public void setSslTrust(String sslTrust) {
            this.sslTrust = sslTrust;
        }
    }
} 