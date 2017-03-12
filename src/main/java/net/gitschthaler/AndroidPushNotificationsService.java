package net.gitschthaler;

import net.gitschthaler.model.FirebaseResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

import java.util.concurrent.Future;

@Service
@EnableAsync
public class AndroidPushNotificationsService {

    @Value("${serverKey}")
    private String serverKey;
    private RestTemplate restTemplate;

    @Async("threadPoolTaskExecutor")
    public Future<FirebaseResponse> send(HttpEntity<String> entity) {
        final FirebaseResponse firebaseResponse =
                restTemplate.postForObject("https://fcm.googleapis.com/fcm/send", entity, FirebaseResponse.class);
        return new AsyncResult<>(firebaseResponse);
    }

    @PostConstruct
    public void initRestTemplate(){
        restTemplate = new RestTemplate();
        ArrayList<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new HeaderRequestInterceptor("Authorization", "key=" + serverKey));
        interceptors.add(new HeaderRequestInterceptor("Content-Type", "application/json"));
        restTemplate.setInterceptors(interceptors);
    }

}
