package net.eulerform.web.module.oauth2.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerform.common.util.BeanTool;
import net.eulerform.web.core.annotation.WebController;
import net.eulerform.web.core.base.controller.DefaultWebController;
import net.eulerform.web.core.base.exception.ResourceExistException;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.module.oauth2.entity.Client;
import net.eulerform.web.module.oauth2.service.IClientService;

@WebController
@Scope("prototype")
@RequestMapping("/oauth2")
public class Oauth2WebController extends DefaultWebController {
    
    @Resource
    private IClientService clientService;
    
    @RequestMapping(value ="/oauthClient",method=RequestMethod.GET)
    public String oauthClient(){
        return "/oauth2/oauthClient";
    }
    
    @RequestMapping(value ="/oauthResource",method=RequestMethod.GET)
    public String oauthResource(){
        return "/oauth2/oauthResource";
    }
    
    @RequestMapping(value ="/oauthScope",method=RequestMethod.GET)
    public String oauthScope(){
        return "/oauth2/oauthScope";
    }
    
    @ResponseBody
    @RequestMapping(value ="/findOauthClientByPage")
    public PageResponse<Client> findOauthClientByPage(HttpServletRequest request, String page, String rows) {
        QueryRequest queryRequest = new QueryRequest(request);
        
        int pageIndex = Integer.parseInt(page);
        int pageSize = Integer.parseInt(rows);
        return this.clientService.findClientByPage(queryRequest, pageIndex, pageSize);
    }
    
    @ResponseBody
    @RequestMapping(value ="/findOauthResourceByPage")
    public PageResponse<net.eulerform.web.module.oauth2.entity.Resource> findOauthResourceByPage(HttpServletRequest request, String page, String rows) {
        QueryRequest queryRequest = new QueryRequest(request);
        
        int pageIndex = Integer.parseInt(page);
        int pageSize = Integer.parseInt(rows);
        return this.clientService.findResourceByPage(queryRequest, pageIndex, pageSize);
    }
    
    @ResponseBody
    @RequestMapping(value ="/findOauthScopeByPage")
    public PageResponse<net.eulerform.web.module.oauth2.entity.Scope> findOauthScopeByPage(HttpServletRequest request, String page, String rows) {
        QueryRequest queryRequest = new QueryRequest(request);
        
        int pageIndex = Integer.parseInt(page);
        int pageSize = Integer.parseInt(rows);
        return this.clientService.findScopeByPage(queryRequest, pageIndex, pageSize);
    }

    @ResponseBody
    @RequestMapping(value = { "/saveOauthClient" }, method = RequestMethod.POST)
    public void saveOauthClient(@Valid Client client, String[] grantType, String scopesIds, String resourceIds, String redirectUris) {
        BeanTool.clearEmptyProperty(client);
        if(client.getId() == null) {
            try {
                Client tmp = (Client) this.clientService.findClientByClientId(client.getClientId());
                if(tmp != null)
                    throw new ResourceExistException("Client Existed!");
            } catch (ClientRegistrationException e) {
                // DO Nothing
            }
        }
        
        this.clientService.saveClient(client, grantType, scopesIds, resourceIds, redirectUris);
    }

    @ResponseBody
    @RequestMapping(value = { "/saveOauthResource" }, method = RequestMethod.POST)
    public void saveOauthResource(@Valid net.eulerform.web.module.oauth2.entity.Resource resource) {
        this.clientService.saveResource(resource);
    }

    @ResponseBody
    @RequestMapping(value = { "/saveOauthScope" }, method = RequestMethod.POST)
    public void saveOauthScope(@Valid net.eulerform.web.module.oauth2.entity.Scope scope) {
        this.clientService.saveScope(scope);
    }
    
    @ResponseBody
    @RequestMapping(value ="/enableClients", method = RequestMethod.POST)
    public void enableClients(@RequestParam String ids) {
        String[] idArray = ids.trim().replace(" ", "").split(";");
        this.clientService.enableClientsRWT(idArray);
    }
    
    @ResponseBody
    @RequestMapping(value ="/disableClients", method = RequestMethod.POST)
    public void disableClients(@RequestParam String ids) {
        String[] idArray = ids.trim().replace(" ", "").split(";");
        this.clientService.disableClientsRWT(idArray);
    }
    
    @ResponseBody
    @RequestMapping(value ="/deleteOauthResources", method = RequestMethod.POST)
    public void deleteOauthResources(@RequestParam String ids) {
        String[] idArray = ids.trim().replace(" ", "").split(";");
        this.clientService.deleteResources(idArray);
    }
    
    @ResponseBody
    @RequestMapping(value ="/deleteOauthScopes", method = RequestMethod.POST)
    public void deleteOauthScopes(@RequestParam String ids) {
        String[] idArray = ids.trim().replace(" ", "").split(";");
        this.clientService.deleteScopes(idArray);
    }

}
