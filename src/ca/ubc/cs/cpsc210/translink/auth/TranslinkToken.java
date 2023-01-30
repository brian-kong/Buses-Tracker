package ca.ubc.cs.cpsc210.translink.auth;

/*
You must request the next 6 arrivals on each route through the specified stop in the next 24 hours.
Experiment with different stops and note the form of the URL that is necessary. In phase 1, you loaded Stop's and
Route's from .json files. In phase 2 these files are found in the res/raw folder of the project.
The getUrl method must build a string to represent the URL that requests arrivals at the stop passed to the constructor
of the HttpArrivalDataProvider. It must then return a URL object constructed from that string.
 */
public class TranslinkToken {
    public static final String TRANSLINK_API_KEY = "kl6CNF8M5PqoCipcBdkV";
}
