package hsbc.qs.utils.eventHandler;

import cucumber.api.event.ConcurrentEventListener;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestRunFinished;
import hsbc.qs.utils.selenium.DriverContext;
import hsbc.qs.utils.selenium.DriverFactory;

public class TestEventHandlerPlugin implements ConcurrentEventListener {

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestRunFinished.class, teardown);
    }

    private EventHandler<TestRunFinished> teardown = event -> {
        if(!DriverContext.getInstance().isBrowserInstanceKilledAtHooks()) {
            if (DriverContext.getInstance().getBrowserName() != null) {
//                if (DriverFactory.getInstance().getDriver() != null) {
                    DriverFactory.getInstance().quitAll();
//                }
            }
        }
        DriverContext.getInstance().setKeepBrowserOpen(false);
    };
}
