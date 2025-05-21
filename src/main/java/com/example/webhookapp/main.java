@SpringBootApplication
public class BFHLApplication implements CommandLineRunner {

    @Autowired
    private WebhookService webhookService;

    public static void main(String[] args) {
        SpringApplication.run(BfhlApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        webhookService.execute();
    }
}
