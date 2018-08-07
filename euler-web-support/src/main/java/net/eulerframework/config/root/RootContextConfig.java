packages = {"net.eulerframework.**.web"},
        excludeFilters = {@ComponentScan.Filter(Controller.class),
                          @ComponentScan.Filter(JspController.class),
                          @ComponentScan.Filter(ApiEndpoint.class)}
)
@EnableTransactionManagement
public class RootContextConfig implements TransactionManagementConfigurer {
    
    @Resource(name="transactionManager")
    PlatformTransactionManager transactionManager;
    
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return this.transactionManager;
    }
}
