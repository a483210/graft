# graft-spring-boot-starter

- 类似Guava的Eventbus功能，发送事件后自动订阅
- 任何被@Component（或者类似@Service等）标记的类都可以直接订阅

## 使用

发布事件

    @Service
    public class GraftServiceImpl implements GraftService {

        @PostEvent
        @Override
        public String graftPostEvent(String content) {
            return String.format("%s-%s", content, UUID.randomUUID().toString());
        }
    }

订阅事件

    @Log4j2
    @Component
    public class GraftSubscribe {
    
        @Subscribe
        public void subscribeGraft(String result) {
            log.info("graft " + result);
        }
        
        @Subscribe
        public void subscribeGraftThrowable(String result, Throwable throwable) {
            if (throwable != null) {
                log.info("graft throwable " + throwable.toString());
            } else {
                log.info("graft noError " + result);
            }
        }
    }
    
## 注解说明

- **PostEvent** 发布事件，value用于指定标签
- **Subscribe** 订阅事件，value用于指定标签
