package nl.vpro.amara;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import nl.vpro.amara.domain.*;
import nl.vpro.amara_poms.Config;

/**
 * @author Michiel Meeuwissen
 * @since 1.2
 */
public class Client {

    private final static Logger LOG = LoggerFactory.getLogger(Client.class);


    private final String amaraUrl;
    private final String username;
    private final String apiKey;
    private final String team;

    public Client(String url, String username, String apiKey, String team) {
        this.amaraUrl = url;
        this.username = username;
        this.apiKey = apiKey;
        this.team = team;

    }

    public void getAllVideos() {

        HttpEntity<?> request = new HttpEntity<>(getGetHeaders());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> response = restTemplate.exchange(getUriForGetAndPostVideos(), HttpMethod.GET, request, String.class);
        LOG.info(String.valueOf(response));

//        RestTemplate restTemplate = new RestTemplate();
//        HttpEntity<String> request = new HttpEntity<String>(headers);
//        ResponseEntity<AmaraVideo[]> response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, request, AmaraVideo[].class);
//        AmaraVideo[] amaraVideos = response.getBody();

//        AmaraVideo amaraVideo = restTemplate.getForObject(url, AmaraVideo.class);
//        logger.info(amaraVideo.toString());
    }


    public Subtitles post(Subtitles amaraSubtitlesIn, String video_id, String language_code) {

        Subtitles amaraSubtitlesOut = null;

        try {
            // construct uri
            String url = amaraUrl + "api/videos/" + video_id + "/languages/" +
                language_code + "/subtitles/";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("team", Config.getRequiredConfig("amara.api.team"));
            URI uri = builder.build().encode().toUri();

            // do request
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Subtitles> request = new HttpEntity<>(amaraSubtitlesIn, getPostHeaders());
            ResponseEntity<Subtitles> response = restTemplate.exchange(uri, HttpMethod.POST, request, Subtitles.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                amaraSubtitlesOut = response.getBody();
            }
        } catch (HttpClientErrorException e) {
            LOG.error("{}:{}", e.getMessage(), e.getResponseBodyAsString());
        }

        return amaraSubtitlesOut;
    }


    public List<Action> getActions(String video_id, String language_code) {
        String url = amaraUrl + "api/videos/" + video_id + "/languages/" + language_code + "/subtitles/actions";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = builder.build().encode().toUri();

        // do request
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Subtitles> request = new HttpEntity<>(getGetHeaders());
        ResponseEntity<Action[]> response = restTemplate.exchange(uri, HttpMethod.GET, request, Action[].class);

        Action[] body = response.getBody();
        return Arrays.asList(body);
    }

    public String getAsVTT(String video_id, String language_code) {

        String amaraSubtitles = null;

        try {
            // construct uri
            String url = amaraUrl + "api/videos/" + video_id + "/languages/" + language_code + "/subtitles/";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("team", Config.getRequiredConfig("amara.api.team"))
                .queryParam("format", Config.getRequiredConfig("amara.subtitles.format"));
            URI uri = builder.build().encode().toUri();

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Subtitles> request = new HttpEntity<>(getGetHeaders());
            HttpEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
            LOG.info(response.getBody());

            // // TODO: 09/04/16 fix
//            if (response.statusCode = HttpStatus.OK) {
            amaraSubtitles = response.getBody();
//            }

        } catch (HttpClientErrorException e) {
            LOG.info(e.toString());
            String responseBody = e.getResponseBodyAsString();
            LOG.info(responseBody);
        }

        return amaraSubtitles;
    }


    // DOESN'T WORK BECAUSE SUBTITLE ARE ALSO IN JSON FORMAT - to get it working a more detailed model is needed
    public Subtitles getSubtitles(String video_id, String language_code) {

        Subtitles amaraSubtitlesOut = null;

        try {
            // construct uri
            String url = amaraUrl + "api/videos/" + video_id + "/languages/" + language_code + "/subtitles/";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
//                    .queryParam("team", Config.getRequiredConfig("amara.api.team"))
                .queryParam("sub_format", Config.getRequiredConfig("amara.subtitles.format"));
            URI uri = builder.build().encode().toUri();

            // do request
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Subtitles> request = new HttpEntity<>(getGetHeaders());
            ResponseEntity<Subtitles> response = restTemplate.exchange(uri, HttpMethod.GET, request, Subtitles.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                amaraSubtitlesOut = response.getBody();
            }
        } catch (HttpClientErrorException e) {
            LOG.info(e.toString());
            String responseBody = e.getResponseBodyAsString();
            LOG.info(responseBody);
        }

        return amaraSubtitlesOut;
    }

    public Activity getActivity(String activity_id) {
        String url = amaraUrl + "api/activity/" + activity_id + "/";
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Activity> request = new HttpEntity<>(getGetHeaders());
        ResponseEntity<Activity> response = restTemplate.exchange(getUriForUri(url), HttpMethod.GET, request, Activity.class);
        Activity amaraActivity = response.getBody();

        //LOG.info(String.valueOf(response));

        return amaraActivity;
    }

    public ActivityCollection getActivities() {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<ActivityCollection> request = new HttpEntity<>(getGetHeaders());
        ResponseEntity<ActivityCollection> response = restTemplate.exchange(getUriForPath("api/activity"), HttpMethod.GET, request, ActivityCollection.class);
        ActivityCollection amaryActivityCollection = response.getBody();

        HttpStatus httpStatus = response.getStatusCode();

        //LOG.info(String.valueOf(response));

        return amaryActivityCollection;
    }

    public ActivityCollection getActivities(int activityType, long afterTimestampInSeconds) {

        // build url
        String url =  amaraUrl + "api/activity/";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("team", team)
            .queryParam("type", activityType)
            .queryParam("after", afterTimestampInSeconds);
        URI uri = builder.build().encode().toUri();

        // do request
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<ActivityCollection> request = new HttpEntity<>(getGetHeaders());
        ResponseEntity<ActivityCollection> response = restTemplate.exchange(uri, HttpMethod.GET, request, ActivityCollection.class);
        ActivityCollection amaryActivityCollection = response.getBody();

        HttpStatus httpStatus = response.getStatusCode();
        //LOG.info(String.valueOf(response));

        return amaryActivityCollection;
    }


    public Task post(Task amaraTaskIn) {

        Task amaraTaskOut = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Task> request = new HttpEntity<>(amaraTaskIn, getPostHeaders());
            ResponseEntity<Task> response = restTemplate.exchange(getPostUri(),
                HttpMethod.POST, request, Task.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                amaraTaskOut = response.getBody();
            }
        } catch (HttpClientErrorException e) {
            LOG.info(e.toString());
            String responseBody = e.getResponseBodyAsString();
            LOG.info(responseBody);
        }

        return amaraTaskOut;
    }

    public TaskCollection getTasks() {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<TaskCollection> request = new HttpEntity<>(getGetHeaders());
        ResponseEntity<TaskCollection> response = restTemplate.exchange(getUriForPath("api/task"), HttpMethod.GET, request, TaskCollection.class);
        TaskCollection tasks = response.getBody();
        HttpStatus httpStatus = response.getStatusCode();

        LOG.debug(String.valueOf(response));

        return tasks;
    }

    public TaskCollection getTasks(String taskType, long afterTimestampInSeconds) {

        // build url
        String url = amaraUrl + "api/teams/" + team + "/tasks";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("type", taskType)
            .queryParam("limit", 300)
            .queryParam("completed")
            .queryParam("completed-after", afterTimestampInSeconds);
        URI uri = builder.build().encode().toUri();

        // do request
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<TaskCollection> request = new HttpEntity<>(getGetHeaders());
        ResponseEntity<TaskCollection> response = restTemplate.exchange(uri, HttpMethod.GET, request, TaskCollection.class);
        TaskCollection tasks = response.getBody();

        HttpStatus httpStatus = response.getStatusCode();
//        logger.info(String.valueOf(response));

        return tasks;
    }

    public Video getVideo(String videoId) {
        String url = amaraUrl + "api/videos/" + videoId;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("team", team);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Video> request = new HttpEntity<>(getGetHeaders());
        ResponseEntity<Video> response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, request, Video.class);
        Video amaraVideo = response.getBody();

//        logger.info(String.valueOf(response));

        return amaraVideo;
    }


    private URI getPostUri() {
        String url = amaraUrl + "api/teams/" + team + "/tasks/";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

        return builder.build().encode().toUri();
    }



    protected HttpHeaders getGetHeaders() {
        HttpHeaders headers = new HttpHeaders();
        authenticate(headers);
        return headers;
    }

    protected HttpHeaders getPostHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        authenticate(headers);
        return headers;
    }

    protected void authenticate(HttpHeaders headers) {
        headers.add("X-api-username", username);
        headers.add("X-api-key", apiKey);
    }

    /**
     * Uri for Get and Post videos
     */
    protected URI getUriForGetAndPostVideos() {
        String u = this.amaraUrl + "api/videos/";

//        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
//                .queryParam("team", Config.getRequiredConfig("amara.api.team"));

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(u);

        return builder.build().encode().toUri();
    }

    /**
     * Uri for Get videos
     */
    protected URI getUriForGetVideoWithId(String videoId) {
        String u = this.amaraUrl + "api/videos/" + videoId;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(u)
            .queryParam("team", team);

        return builder.build().encode().toUri();
    }

    /**
     * Uri for url
     */
    protected URI getUriForUri(String url) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

        return builder.build().encode().toUri();
    }

    /**
     * Uri for url with team
     */
    protected URI getUriForUriWithTeam(String url) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("team", Config.getRequiredConfig("amara.api.team"));

        return builder.build().encode().toUri();
    }

    /**
     * Uri for path
     */
    protected URI getUriForPath(String path) {
        String u = amaraUrl + path;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(u);

        return builder.build().encode().toUri();
    }


}
