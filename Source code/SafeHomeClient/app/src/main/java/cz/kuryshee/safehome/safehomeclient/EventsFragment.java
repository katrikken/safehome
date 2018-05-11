package cz.kuryshee.safehome.safehomeclient;

import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kuryshee.safehome.appcommunicationconsts.AppCommunicationConsts;
import com.kuryshee.safehome.httprequestsender.GetRequestSender;
import com.kuryshee.safehome.requestdataretriever.GetDataRetriever;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * Class for {@link Fragment} containing layout with events information.
 *
 * @author Ekaterina Kurysheva.
 */
public class EventsFragment extends Fragment {

    private Application application;
    private RpiEventItemsAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private String userToken;

    /**
     * Initializes the GUI components.
     * Starts tasks for synchronization of events with the server.
     * @see Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        this.application = this.getActivity().getApplication();
        this.userToken = ((GlobalVariables) application).getToken();

        adapter = new RpiEventItemsAdapter(application);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.events_fragment);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshEvents();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(!recyclerView.canScrollVertically(-1)){ //If you are at the bottom of the list
                    if (userToken != null && userToken.length() != 0) {
                        new LoadMoreEvents().execute(userToken);
                    }
                    else{
                        redirectToAuthorizationActivity();
                    }
                }
            }
        });

        refreshEvents();

        return view;
    }

    /**
     * Starts asynchronous task for loading of latest events from the server.
     */
    private void refreshEvents(){
        if (userToken != null && userToken.length() != 0){
            new GetLatestEvents().execute(userToken);
        }
        else{
            redirectToAuthorizationActivity();
        }
    }

    /**
     * Starts the {@link AuthorizationActivity}.
     */
    private void redirectToAuthorizationActivity(){
        Intent intent = new Intent(application, AuthorizationActivity.class);
        startActivity(intent);
    }

    /**
     * Sends the GET request to the server to obrain the date of latest registered event on Raspberry Pi.
     * @param server is the server address.
     * @param token is the authentication token.
     * @return string with the server answer.
     */
    public static String loadDateOfLatestEvent(String server, String token){
        GetRequestSender sender = new GetRequestSender();
        StringBuilder query = new StringBuilder(server);
        query.append('?').append(AppCommunicationConsts.ACTION).append('=').append( AppCommunicationConsts.GET_LATEST_ACTION_TIME);
        sender.setToken(token);
        byte[] input = sender.connect( query.toString(), null);
        GetDataRetriever retriever = new GetDataRetriever();
        return retriever.getStringData(input);
    }

    /**
     * Loads events from the server.
     * @param server is the server address
     * @param date is the date of the last known event for the application
     * @param token is the authorization token.
     * @return list of {@link RpiEventItem}.
     * @throws ParseException
     */
    public static List<RpiEventItem> loadEventsFromServer(String server, String date, String token) throws ParseException {
        GetRequestSender sender = new GetRequestSender();
        StringBuilder query = new StringBuilder(server);
        query.append('?')
                .append(AppCommunicationConsts.ACTION).append('=').append( AppCommunicationConsts.GET_ACTIONS).append("&")
                .append(AppCommunicationConsts.TIME).append("=").append(date);
        sender.setToken(token);
        byte[] input = sender.connect( query.toString(), null);

        JsonArray array = new GetDataRetriever().getJsonArray(input);
        return EventsFragment.convertToRpiEventItemsList(array);
    }

    /**
     * Loads the most recent events from server.
     */
    private class GetLatestEvents extends AsyncTask<String, Void, List<RpiEventItem>> {

        String token = null;

        /**
         * Loads the latest events registered on Raspberry Pi.
         * @param params expects the identity token on 0 position.
         * @return list of {@link RpiEventItem}.
         */
        @Override
        protected List<RpiEventItem> doInBackground(String... params){

            if(params.length > 0){
                try {
                    this.token = params[0];

                    //Get latest registered date of event
                    String strdate = EventsFragment.loadDateOfLatestEvent(((GlobalVariables) application).getServerPath(), token);

                    Logger.getLogger(EventsFragment.class.getName()).log(Level.INFO, strdate);

                    //Check if the event is cached
                    LruCache<String, RpiEventItem> cache = ((GlobalVariables) application).getEventsMemoryCache();
                    if(cache.get(strdate) == null){ //If not in the cache, download it
                        return EventsFragment.loadEventsFromServer(((GlobalVariables) application).getServerPath(), strdate, token);
                    }
                }
                catch (Exception e){
                    Logger.getLogger(EventsFragment.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                }
            }

            return null;
        }

        /**
         * Passes the result to the view adapter.
         * @param result is a list of {@link RpiEventItem}.
         */
        @Override
        protected void onPostExecute(List<RpiEventItem> result){
            if (result != null){
                adapter.addEventsToCache(result);
                adapter.addEvents(result);
            }
            else{
                Logger.getLogger(EventsFragment.class.getName()).log(Level.INFO, "No events were loaded");
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Converts {@link JsonArray} object to list of {@link RpiEventItem} instances.
     * @param array {@link JsonArray}
     * @return list of {@link RpiEventItem} instances.
     */
    public static List<RpiEventItem> convertToRpiEventItemsList(JsonArray array) throws ParseException{
        List<RpiEventItem> list = new ArrayList<>();
        for( int i = 0; i < array.size(); i++){
            RpiEventItem item = new RpiEventItem();
            JsonObject object = array.getJsonObject(i);
            item.setEventInfo(object.getString(AppConstants.JSON_ACTIOM_PARAM));
            item.setDateFromDateFormatString(object.getString(AppConstants.JSON_TIME_PARAM));
            item.setEventLevel(object.getString(AppConstants.JSON_LEVEL_PARAM));

            list.add(item);
        }

        Logger.getLogger(EventsFragment.class.getName()).log(Level.INFO, list.size() + " items downloaded");

        return list;
    }

    /**
     * Loads older activities from server asynchronously.
     */
    private class LoadMoreEvents extends AsyncTask<String, Void, List<RpiEventItem>> {

        String token = null;

        /**
         * Performs HTTP request.
         * @param params expects identity token on 0 position
         * @return list of {@link RpiEventItem}.
         */
        @Override
        protected List<RpiEventItem> doInBackground(String... params){

            if(params.length > 0){
                try {
                    this.token = params[0];

                    GetRequestSender sender = new GetRequestSender();
                    StringBuilder query = new StringBuilder(((GlobalVariables) application).getServerPath());
                    query.append('?')
                            .append(AppCommunicationConsts.ACTION).append('=').append( AppCommunicationConsts.GET_ACTIONS).append("&")
                            .append(AppCommunicationConsts.TIME).append("=").append(adapter.getOldestTimestamp());
                    sender.setToken(token);
                    byte[] input = sender.connect( query.toString(), null);

                    JsonArray array = new GetDataRetriever().getJsonArray(input);
                    return convertToRpiEventItemsList(array);
                }
                catch (Exception e){
                    Logger.getLogger(EventsFragment.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                }
            }

            return null;
        }

        /**
         * Notifies the view adapter to update data.
         * @param result to pass to the adapter.
         */
        @Override
        protected void onPostExecute(List<RpiEventItem> result){
            if (result != null){
                adapter.addEvents(result);
            }
            else{
                Logger.getLogger(EventsFragment.class.getName()).log(Level.INFO, "No events were loaded");
            }
        }
    }
}
