package net.gitschthaler;

import net.gitschthaler.model.FirebaseResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
public class FirebaseAWSForwarder {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAWSForwarder.class);

    @Autowired
    private AndroidPushNotificationsService androidPushNotificationsService;
    private Map<String,String> list = new ConcurrentHashMap<>();

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ResponseEntity<String> register(@RequestParam String token, @RequestParam String device) {
        list.put(token,device);
        logger.info("addedd device {} list is now {}", token, list.size());
        return new ResponseEntity<>("added", HttpStatus.OK);
    }

    @RequestMapping(value = "/send", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> send(@RequestParam String title, @RequestParam String body) {
        final int successFullCalls = doSend(title, body);
        return new ResponseEntity<>("sent "+ successFullCalls, HttpStatus.OK);
    }

    private JSONObject assembleJson(String to, String title, String msg){
        JSONObject body = new JSONObject();
        body.put("to", to);
        body.put("priority", "high");

        JSONObject notification = new JSONObject();
        notification.put("body", msg);
        notification.put("title", title);
        body.put("notification", notification);
        return body;
    }

    private int doSend(String title, String msg) {
        List<Future<FirebaseResponse>> calls = new ArrayList<>();
        for(String key : list.keySet()){
            logger.info("scheduling a request for {}", key);
            final String json = assembleJson(key, title, msg).toString();
            HttpEntity < String > request = new HttpEntity<>(json);
            calls.add(androidPushNotificationsService.send(request));
        }

        int successfullCalls = 0;
        for(Future<FirebaseResponse> respons : calls){
            try {
                successfullCalls += respons.get().getSuccess(); // get blocks
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return successfullCalls;
    }
}
