package net.eulerform.web.core.security.authentication.service.impl;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.core.security.authentication.dao.IClientDao;
import net.eulerform.web.core.security.authentication.dao.IGrantTypeDao;
import net.eulerform.web.core.security.authentication.dao.IResourceDao;
import net.eulerform.web.core.security.authentication.dao.IScopeDao;
import net.eulerform.web.core.security.authentication.entity.Client;
import net.eulerform.web.core.security.authentication.entity.GrantType;
import net.eulerform.web.core.security.authentication.entity.Resource;
import net.eulerform.web.core.security.authentication.entity.Scope;
import net.eulerform.web.core.security.authentication.service.IClientService;

import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.Signer;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

public class ClientService extends BaseService implements IClientService, ClientDetailsService {
    
    private IClientDao clientDao;
    private IResourceDao resourceDao;
    private IScopeDao scopeDao;
    private IGrantTypeDao grantTypeDao;
    
    private PasswordEncoder passwordEncoder;

    public void setClientDao(IClientDao clientDao) {
        this.clientDao = clientDao;
    }

    public void setResourceDao(IResourceDao resourceDao) {
        this.resourceDao = resourceDao;
    }

    public void setScopeDao(IScopeDao scopeDao) {
        this.scopeDao = scopeDao;
    }
    
	public void setGrantTypeDao(IGrantTypeDao grantTypeDao) {
		this.grantTypeDao = grantTypeDao;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
	    this.passwordEncoder = passwordEncoder;
	}

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        //Client client = this.clientDao.findClientByClientId(clientId);
        Client client = this.clientDao.load(clientId);
        if(client == null)
            throw new ClientRegistrationException("Client \"" + clientId + "\" not found");
        return client;
    }
    
    public static void main(String[] args) throws NoSuchAlgorithmException{
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA"); 
        keyPairGen.initialize(2048); 
        KeyPair keyPair = keyPairGen.generateKeyPair(); 
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic(); 
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        


        
        System.out.println(new String(Base64.encode(publicKey.getEncoded())));
        System.out.println(new String(Base64.encode(privateKey.getEncoded())));
        //Signer  signer = new RsaSigner(privateKey);
        Signer  signer = new RsaSigner("-----BEGIN RSA PRIVATE KEY-----"+
        "MIIEoAIBAAKCAQEAxVVvK5+Z1jjw7vDNEfFi3Vbc0uBmJ2kbGCZtgJ0D2KTGYMTcirS6q6QgJBWv2AWc5JjJGa9aReQameXhYcfpnvALsUZjDhuCaKi8LeAX1y2VQHnZwQHJs3zs2BNpmX6jTca8+9fdlbm2XSpejHLGE2rk+Sk9/o+jWOyyqPsWcyuvQE3/0BjB8c8s/3AggfFwsTjocz1w20clHb5P0Y+4IFaBadi3M3XKKJ7PDsJIx/V4ufUPn7N6kMlF1kDq67lYho7rJdqpA5Q1QZbAE3j5/JKakVaWZd0YLs3oz+3ZXbakK4RPeHulWF54JamtiinHcFat86ExKH2uLUJwA9x62wIBIwKCAQBf2QocgLhvXXxWzLtgfI8bDO7qFTjukiMaW87JcNX7gzvImiH6OoaN4gD7lYFLqvRvCGGtY8zC3IHy+qCr1iEEDjGQnofiR+BBdodJe3lLQgaqSdAqi9b/aJBLsahR3myTfcmBo1z/lLepmDyNXFGUZx7CKfmKRcReZFbHGt8EvJgDIP6Sa7iTvmrBwG2Z9Mr4AZHLFGJscDPlq0F3dCwxsgVqc87yP3L4D4w05XlzEiQ5OjEKm0uJeqMZ2tQS9dVIgVrZ35Ttk89sWBd9L4mw4DELVczS7MQetjBhCfSy7IskIBLtiXhh41C6k086NP1c4YrkuPjRY4sq9HcO+Rf7AoGBAORgyFS/tJBOGrVxeHHjFbEp7dNvBQr6+ptRgasq5if3tpt5vJBQtUrvTaivz/CqZw/I7ag1AAb86TYOCjj/6grRzXM4bJzR6jjZQ89z/CZLEgunCgiGFj2aA0oXpu5cCA0qf6SD/Vk0X8CtG6pX28Nxi6zd3nPPH7r0SfuEYSDjAoGBAN0zbuo9Si4ZLG6Vp73pa0rT/I6AO5s9R2o+ruGhq1NJ1wf2016zxWVYuKtbdD/0aQ9OCBGCClCH45LPN+49PpaX82KHOFIMTH6JQzi73rg2SZNafcoPZuxzreDDUInNi1prWTixF/nxDJjLQJxRDhh7zXm3VPlP61w64wboLzepAoGADQzYP1tpZ1TrlVbw8JCiJ2F7TekWOyRI1azi1pS8sdOkCOJii+da0RT9H5UEkWjSsHHhsdckkq9dyJMWhuoNX7Q3oC8cJjfhf5di9ele3Z3jxibNX5KiL2fi7kp+kUcWZyb/+sW2tKPoNuVSCbvgrBUd7J72xMoB0Cs3bXVHYPcCgYA44VcJCHIpHGqC1gaP65CeNoLFjrA93I6064wOIkH/eWMt7wMm+wbfi9e3CONg5xOzencaceVlKkHVSzpEk2fdlMmOXUj/J7vmBgn4pVaHI+cIoj2aW7thX5Mcif7EWWz5/ljFb13v0EURWNYZkS+Co336uh0qKn5Zi3w0+eBBgwKBgBZXLtvcBjpXqKYwvSQpy6++GlcvcdL0w18HBtueKkb6fnhjnwqBKAbPYa5EPJ74PSZjaw8bkcd759LQ0qm39QeA1Rsnb/BkX47FMnLkmm5vTHfB6OvGJsSUXzHIzR4ce4ewv6Ky5lAmlOrYWLyG7L5jFif1CimTX+csePRBEEff"+
        //"MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIBqObkbit0hNt2ooaWXJjCZAworv0XCIbm1G6FocAK/QXKkXe24aV0B3Rqn5Y7cl9CW9yhygTNpSkIfjUw+2DZDUx6u1qs2+0N3hsYA1NU6foG80WoOWvKpJ+9kb/qLVFOyAIt8t/+FA7SJMrqtqlamR6XhRtao2OU2oML4I7mbAgMBAAECgYB4byejy4j4yuXCDR1NR+yxN0/6gfzpV6B5ork/L8udR/IBqtXM1V87szvCfCR3T7uyDWPPs1Qo+16BlUXXdDkhibX22D7vsrz18OriMbPXPDdrVLXXYbQhc1P1Yl1lsxTVrUaX86X80TGD43yEqDzSbviRre2MXjtGY0zUztrSwQJBAO+GYnwxQDTTxSjB0pC2Zvd2zV5r8XL6LwgatVddTqlOMyKvZFg3h/m57wdH//OUnYZVZQuP2lQVJzfh47b3bgcCQQCJP2NXCoufPXtiZD4Obo/qRTc71pwg2DRSF+LkYJL1EpX4ypVsamgv1LIxtpUfLCt/ZYFqdbwj86u90v9CkvLNAkA7Z7r5TkW8VGFrsFaG6ZYz/dUFfAQQHhiYC/e5yTx/JRPtoE5kyrEAsKqlxaenQ86lyHwdF/pwLCbj5zRQ5qD1AkArqgD+xhoZdUP1z5J2oPIkRBUIcoSFZoNuwq6qnZbjZSBVYI4dSdUPsolmi4HxwumrxC1iI0bR4Un4QFJ5aarZAkEAmzhjTXOQ6Br1JoiYgVZQzaVDvFX2gzscFscjNLlCvff/i8+2Tp9WaQP5f6VofxyeHwRP749I68fIwlwhr7kZoQ=="+
        //"MIIEpgIBAAKCAQEAobqoV5brW/wVskAimNXSB+v+9NHpWF3wq29ZQJhyMEIcMAHzhbfVSZgsDmpGEJTP/eH/tX0k2FoFFkfj3U9cXGIJZVjeNTPDKC7BTtmU/ZfmR1xoJmDexqALa3KRFxvCuUZtAxk75gMmm1+VSDDwDDGsfz7LTsKxUhcXM0A8Oh/w02xghsTuqn2vfKnE3LInZOs785ntoocDlZcexsX4bLXdjrK02YGcqSLavieYI8fjxcfhCD1CGOmUrA6xgADVTs5wBfK+Kg1mYPnYAv6yuWPv3k2WdtsZ7JsT639NksMyOYsxXeQWut9Xj2eeDwGoagEX1s+snhc4CImW6LtWMwIDAQABAoIBAQCE6SRdz06fKr0d321PUzGnhv/hbP0qvREDop+j4WS+WiZWIdRjCSAEukVCl337NID2MZv3J+B22QwjMnOGNik+VudH3c/Hw0FYLYx544B5JDOAY+XH3IZYj8CyzdWFOzA9GS6PhFZggihhOh0x1d4A93W+oPluQbx+LTHI0bptPPIxuXTEpUegi1XlbfU0ZBqW9TtG2XiS9vPKpVQ7KMTJSVdxjXCOaKNSDChEHKfSLw9n0Y6qEh/teNvhckzCtBdDt4rZwZptklWmxFX7zU1TlDU1OqCSlDF5KPIyNGLA/2lYxOqqG3V4m++oATtpp4l0KVw496DV/8kq4+ZG+iQZAoGBAM4sLHwo6PfFd82ys0pn/MdcTJA+GDDkiO94jnaA9CKit+HTXFw8WKjGjlBafr3SfTWwO9ig8fgekGu19VIEtVGc+dzQYBYl6VPdEiM4NKGHvFutlDP69e8/3ZKupow3jwCTzhfgsIjgJA9IWLcp4npA9f1DpmtwyFZZPVwOQ2pPAoGBAMjQyMaWcMZZ/Rw+ig+JbkcPcQGPX5pWNuTm+vurUYdfC6x2x0QpbK+2TWdGx6E5NAwmTVJ7rm4jFqvWgQnJevN3RvdotvIcuucs786I2PVLJKnwTPw6eon7uKyHVw+ddke176giwzhaui2ny32h2v23twEk2JDnQ0JjoJVSGnDdAoGBAIn7bGZIJuq0QOLsxytz/uwZ7K/Yru4B9Vd3srjCwyFvD2vWvgiI5rlF8bb7abl25w+Ie/UWefqZ0gQUSjPzLLqLOXo8ByKrisXyvZHOqwK0Si59NCO5wOC3OH5T3ukWweEcCqFWYi+o+tkzjRRAtu8lDLzMitN7LskDfppefWXnAoGBAIZZ2Nmz8MNjlUl+NdPrOFJmbE6E44tYPuWp+yTBG4yb9C1wUiSyKjrslqCP5CNjKAUw4u5aPPsGkrZojnBD0fRtSpdgAXW97vWXROFDARQrL95aHMdrQGxscsNK0N5rlKSpfitZBo7/dCvzZNsqnF6+uLsVMabQcllKWjdMdNApAoGBAIEyI+iCbKZonaNZ6x8LrVwLYDvio1ExGpzb+Wj5xpXwZNhfKua4uocOs+42BQfqXzKMCmjEVSiNs+n5HRYkkF2yN9DA6SBy/aJvM2Wgv5NF6aWVD1SDAHFRQYwD94InpUMNT0EOp9FBJy7tNSEt2nRC/yN5hkfivpN7BxUporH9"+
                //"MIICWgIBAAKBgGAbzjs7fo9HeoxvZqnjHu9WTOFbNX+RMGtRtrA0PqG0ekOri7MnSkD3K8Zgs3tGjVEwW48YdUfVqNDwGpdwZtlhBgPydbqk1Ki87arAZrExAEly7TQ60+CPj8vGkVEC3K1ZH0/TkRUHlP/6C8V3SOqI56d1/ZjqWerZ8FVFqMy9AgElAoGAIcSUkVoXtc0Bi0m8SYcmi3FZSEKkGBBq9UY5RNQWAXbDLIhhhCKPtfX6n6VvflcPDrAgK1ue1AzMnHAJV82L6xM4S1M06jmq2UvVmpNjpeCN978U9Qpjke+iQDYS1gS6cK3xQYFKM1zGrPCkbqFl4FsWH62pWuH5amxLzu71R60CQQC5EbbwYgTn7BnOQGTV/gDZDwQWiMbfRpBQFGZiWlwGVLb8kCJ7TrkDUzG6yt8ZCSzdkgsJXkGkqS51f+uk/b0BAkEAhPGeAZDMU9rpZZwX0MM6sXDmsrKgPfvElXKHQHeAKiaORhuaSr3xN0dr4R4hppPwWVG4aqmjYBI+ug7N5N1DvQJBAKAPUhwBvw3FRsA3sSfG6/n/JiFTsuqd5JhI/ppAT5bFzrDrXBeeB8uGONjmzsmLZRKn0jGdoI5ozjwbm15DO60CQCPuRmFJuq7hOClMyCqVoSkJwc9ujCxtjxOiab5lfJXFOzWK624lf3a5W21GafWrcWQ/maBJhhn3F99CRXwgICUCQQCcDZ8y99aUOPppd1t4Z1Wemc44Nyl/M+IxGxiEvEAUWv0UdBBTyuQX+9Gh2WImVYfl8ugGTqCI22n6Xt/u+w8n"+
                //"MIICWgIBAAKBgGAbzjs7fo9HeoxvZqnjHu9WTOFbNX+RMGtRtrA0PqG0ekOri7MnSkD3K8Zgs3tGjVEwW48YdUfVqNDwGpdwZtlhBgPydbqk1Ki87arAZrExAEly7TQ60+CPj8vGkVEC3K1ZH0/TkRUHlP/6C8V3SOqI56d1/ZjqWerZ8FVFqMy9AgElAoGAIcSUkVoXtc0Bi0m8SYcmi3FZSEKkGBBq9UY5RNQWAXbDLIhhhCKPtfX6n6VvflcPDrAgK1ue1AzMnHAJV82L6xM4S1M06jmq2UvVmpNjpeCN978U9Qpjke+iQDYS1gS6cK3xQYFKM1zGrPCkbqFl4FsWH62pWuH5amxLzu71R60CQQC5EbbwYgTn7BnOQGTV/gDZDwQWiMbfRpBQFGZiWlwGVLb8kCJ7TrkDUzG6yt8ZCSzdkgsJXkGkqS51f+uk/b0BAkEAhPGeAZDMU9rpZZwX0MM6sXDmsrKgPfvElXKHQHeAKiaORhuaSr3xN0dr4R4hppPwWVG4aqmjYBI+ug7N5N1DvQJBAKAPUhwBvw3FRsA3sSfG6/n/JiFTsuqd5JhI/ppAT5bFzrDrXBeeB8uGONjmzsmLZRKn0jGdoI5ozjwbm15DO60CQCPuRmFJuq7hOClMyCqVoSkJwc9ujCxtjxOiab5lfJXFOzWK624lf3a5W21GafWrcWQ/maBJhhn3F99CRXwgICUCQQCcDZ8y99aUOPppd1t4Z1Wemc44Nyl/M+IxGxiEvEAUWv0UdBBTyuQX+9Gh2WImVYfl8ugGTqCI22n6Xt/u+w8n"+
                "-----END RSA PRIVATE KEY-----");
        System.out.println(signer.toString());
    }

    @Override
    public void createScope(Scope scope) {
        this.scopeDao.save(scope);
    }

    @Override
    public void createResource(Resource resource) {
        this.resourceDao.save(resource);
    }

    @Override
    public List<Client> findAllClient() {
        return this.clientDao.findAll();
    }

	@Override
	public void createClient(String secret, Integer accessTokenValiditySeconds, Integer refreshTokenValiditySeconds, Boolean neverNeedApprove) {
		Client client = new Client();
		client.setClientSecret(this.passwordEncoder.encode(secret));
		client.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
		client.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
		client.setNeverNeedApprove(neverNeedApprove);
		this.clientDao.save(client);
	}

	@Override
	public void createGrantType(GrantType grantType) {
		this.grantTypeDao.save(grantType);
	}
    
}
