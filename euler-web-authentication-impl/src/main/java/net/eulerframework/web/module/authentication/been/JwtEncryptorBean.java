package net.eulerframework.web.module.authentication.been;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.eulerframework.web.module.authentication.util.JwtEncryptor;

@Configuration
public class JwtEncryptorBean {

//	@Bean(name = "jwtVerifierKey")
//	public String jwtVerifierKey() throws IOException {
//		if (this.jwtVerifierKey != null)
//			return this.jwtVerifierKey;
//		String path = this.getClass().getResource("/").getPath();
//		this.jwtVerifierKey = FileReader.readFileByLines(path + jwtVerifierKeyFile);
//		return this.jwtVerifierKey;
//	}
//
//	@Bean(name = "jwtSigningKey")
//	public String jwtSigningKey() throws IOException {
//		if (this.jwtSigningKey != null)
//			return this.jwtSigningKey;
//		String path = this.getClass().getResource("/").getPath();
//		this.jwtSigningKey = FileReader.readFileByLines(path + jwtSigningKeyFile);
//		return this.jwtSigningKey;
//	}
    @Bean(name = "jwtEncryptor")
    public JwtEncryptor jwtEncryptor() {
        JwtEncryptor j = new JwtEncryptor();
        j.setSigningKey(jwtSigningKey());
        j.setVerifierKey(jwtVerifierKey());
        return j;        
    }
    
	public String jwtSigningKey() {
        return "-----BEGIN RSA PRIVATE KEY-----MIIEpgIBAAKCAQEAobqoV5brW/wVskAimNXSB+v+9NHpWF3wq29ZQJhyMEIcMAHzhbfVSZgsDmpGEJTP/eH/tX0k2FoFFkfj3U9cXGIJZVjeNTPDKC7BTtmU/ZfmR1xoJmDexqALa3KRFxvCuUZtAxk75gMmm1+VSDDwDDGsfz7LTsKxUhcXM0A8Oh/w02xghsTuqn2vfKnE3LInZOs785ntoocDlZcexsX4bLXdjrK02YGcqSLavieYI8fjxcfhCD1CGOmUrA6xgADVTs5wBfK+Kg1mYPnYAv6yuWPv3k2WdtsZ7JsT639NksMyOYsxXeQWut9Xj2eeDwGoagEX1s+snhc4CImW6LtWMwIDAQABAoIBAQCE6SRdz06fKr0d321PUzGnhv/hbP0qvREDop+j4WS+WiZWIdRjCSAEukVCl337NID2MZv3J+B22QwjMnOGNik+VudH3c/Hw0FYLYx544B5JDOAY+XH3IZYj8CyzdWFOzA9GS6PhFZggihhOh0x1d4A93W+oPluQbx+LTHI0bptPPIxuXTEpUegi1XlbfU0ZBqW9TtG2XiS9vPKpVQ7KMTJSVdxjXCOaKNSDChEHKfSLw9n0Y6qEh/teNvhckzCtBdDt4rZwZptklWmxFX7zU1TlDU1OqCSlDF5KPIyNGLA/2lYxOqqG3V4m++oATtpp4l0KVw496DV/8kq4+ZG+iQZAoGBAM4sLHwo6PfFd82ys0pn/MdcTJA+GDDkiO94jnaA9CKit+HTXFw8WKjGjlBafr3SfTWwO9ig8fgekGu19VIEtVGc+dzQYBYl6VPdEiM4NKGHvFutlDP69e8/3ZKupow3jwCTzhfgsIjgJA9IWLcp4npA9f1DpmtwyFZZPVwOQ2pPAoGBAMjQyMaWcMZZ/Rw+ig+JbkcPcQGPX5pWNuTm+vurUYdfC6x2x0QpbK+2TWdGx6E5NAwmTVJ7rm4jFqvWgQnJevN3RvdotvIcuucs786I2PVLJKnwTPw6eon7uKyHVw+ddke176giwzhaui2ny32h2v23twEk2JDnQ0JjoJVSGnDdAoGBAIn7bGZIJuq0QOLsxytz/uwZ7K/Yru4B9Vd3srjCwyFvD2vWvgiI5rlF8bb7abl25w+Ie/UWefqZ0gQUSjPzLLqLOXo8ByKrisXyvZHOqwK0Si59NCO5wOC3OH5T3ukWweEcCqFWYi+o+tkzjRRAtu8lDLzMitN7LskDfppefWXnAoGBAIZZ2Nmz8MNjlUl+NdPrOFJmbE6E44tYPuWp+yTBG4yb9C1wUiSyKjrslqCP5CNjKAUw4u5aPPsGkrZojnBD0fRtSpdgAXW97vWXROFDARQrL95aHMdrQGxscsNK0N5rlKSpfitZBo7/dCvzZNsqnF6+uLsVMabQcllKWjdMdNApAoGBAIEyI+iCbKZonaNZ6x8LrVwLYDvio1ExGpzb+Wj5xpXwZNhfKua4uocOs+42BQfqXzKMCmjEVSiNs+n5HRYkkF2yN9DA6SBy/aJvM2Wgv5NF6aWVD1SDAHFRQYwD94InpUMNT0EOp9FBJy7tNSEt2nRC/yN5hkfivpN7BxUporH9-----END RSA PRIVATE KEY-----";
    }

    public String jwtVerifierKey() {
        //return "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7edY0ZALoT+WnhEOmpmSOqQbv2xcChMJwLCtT/LDSXJtOZagjEzodLBI6jUrRDfQ7YU0TLygGWvL+o9ZiWnJQL7UWZO2y66YbAwaUI0FS4uorAebyqbt98mAa41x8PqBqd8pwWSgB6OsYu+bRts1NfYtNOPwVYUyCbT+rL7q1Z1cx8yi3fRRCXp0/bmD4gNmN1S0eHNFOkiCv5/8/CK3nXwzRqUojftbmv52PcPMf3Q6XOeQZBxV4ynoKvri788uV4l9A1iNIf7/lgEmTlU72s+3kx/5fhXerjHSmdZ2/pGDN8Q/+xXKA/2smXmtzx2ZTvFYuzyb64yqEq7CuAVgXwIDAQAB-----END PUBLIC KEY-----";
        return "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAobqoV5brW/wVskAimNXSB+v+9NHpWF3wq29ZQJhyMEIcMAHzhbfVSZgsDmpGEJTP/eH/tX0k2FoFFkfj3U9cXGIJZVjeNTPDKC7BTtmU/ZfmR1xoJmDexqALa3KRFxvCuUZtAxk75gMmm1+VSDDwDDGsfz7LTsKxUhcXM0A8Oh/w02xghsTuqn2vfKnE3LInZOs785ntoocDlZcexsX4bLXdjrK02YGcqSLavieYI8fjxcfhCD1CGOmUrA6xgADVTs5wBfK+Kg1mYPnYAv6yuWPv3k2WdtsZ7JsT639NksMyOYsxXeQWut9Xj2eeDwGoagEX1s+snhc4CImW6LtWMwIDAQAB-----END PUBLIC KEY-----";
    }

}
