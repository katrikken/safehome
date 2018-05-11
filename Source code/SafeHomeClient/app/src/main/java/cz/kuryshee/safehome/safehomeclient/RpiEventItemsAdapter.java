package cz.kuryshee.safehome.safehomeclient;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kuryshee.safehome.appcommunicationconsts.AppCommunicationConsts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Class of customized {@link RecyclerView.Adapter}.
 * Prepares data about events on Raspberry Pi for GUI.
 *
 * @author Ekaterina Kurysheva
 */
public class RpiEventItemsAdapter extends RecyclerView.Adapter<RpiEventItemsAdapter.InfoViewHolder> {

    private List<RpiEventItem> eventsList;

    private Application application;

    /**
     * Constructor of the adapter.
     * @param application is an instance of {@link Application}.
     */
    public RpiEventItemsAdapter(Application application){
        this.application = application;
        this.eventsList = new ArrayList<>();

        getEventsFromCache();
    }

    /**
     * Reads cached data to the private variable.
     */
    private void getEventsFromCache(){
        eventsList = new ArrayList<>();

        if(((GlobalVariables) application).getEventsMemoryCache() == null){
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 8;

            ((GlobalVariables) application).setEventsMemoryCache(new LruCache<String, RpiEventItem>(cacheSize));
        }
        else{
            LruCache<String, RpiEventItem> eventCache = ((GlobalVariables) application).getEventsMemoryCache();

            Set<String> keySet = eventCache.snapshot().keySet();

            for(String key : keySet){
                eventsList.add(eventCache.get(key));
            }

            Collections.sort(eventsList, new RpiEventItemComparator());
        }
    }

    /**
     * Adds information about events on RaspberryPi to the application cache.
     * @param events is a list of instances of {@link RpiEventItem}.
     */
    public void addEventsToCache(List<RpiEventItem> events){
        LruCache<String, RpiEventItem> eventCache = ((GlobalVariables) application).getEventsMemoryCache();

        for(RpiEventItem item: events){
            if(eventCache.get(item.getStringUnixEpochDate()) == null){
                eventCache.put(item.getStringUnixEpochDate(), item);
            }
        }
    }

    /**
     * Adds the event to the private list.
     * @param events is a list of instances of {@link RpiEventItem}.
     */
    public void addEvents(List<RpiEventItem> events){
        for(RpiEventItem item: events){

            boolean exists = false;
            for(RpiEventItem existing: eventsList){
                if(item.getDate().equals(existing.getDate())){
                    exists = true;
                    break;
                }
            }
            if (!exists){
                eventsList.add(item);
            }
        }

        Collections.sort(eventsList, new RpiEventItemComparator());

        this.notifyDataSetChanged();
    }

    /**
     * Returns the oldest date of cached event.
     * @return date as a string with milliseconds in Unix Epoch format.
     */
    public String getOldestTimestamp(){
        return eventsList.get(eventsList.size() - 1).getStringUnixEpochDate();
    }

    /**
     * @see RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int).
     */
    @Override
    public InfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rpi_event_item_row, parent, false);

        return new InfoViewHolder(itemView);
    }

    /**
     * @see RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    @Override
    public void onBindViewHolder(@NonNull InfoViewHolder holder, int position) {
        RpiEventItem item = eventsList.get(position);
        holder.date.setText(item.getDateStringGUI());
        holder.eventInfo.setText(item.getEventInfo());
        if(item.getEventLevel().equals(AppCommunicationConsts.DANGEROUS)){
            holder.cardView.setCardBackgroundColor(application.getResources().getColor(R.color.red));
            holder.date.setTextColor(application.getResources().getColor(R.color.clean_white));
            holder.eventInfo.setTextColor(application.getResources().getColor(R.color.clean_white));
        }
        else{
            holder.cardView.setCardBackgroundColor(application.getResources().getColor(R.color.clean_white));
            holder.date.setTextColor(application.getResources().getColor(R.color.dark_grey));
            holder.eventInfo.setTextColor(application.getResources().getColor(R.color.dark_grey));
        }
    }

    /**
     * @see RecyclerView.Adapter#getItemCount()
     */
    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    /**
     * Class holds customized view for the {@link RecyclerView}.
     * @see RecyclerView.ViewHolder
     */
    public class InfoViewHolder extends RecyclerView.ViewHolder {
        public TextView date, eventInfo;
        public CardView cardView;

        public InfoViewHolder(View view) {
            super(view);
            date = (TextView) view.findViewById(R.id.date);
            eventInfo = (TextView) view.findViewById(R.id.eventInfo);
            cardView = (CardView) view.findViewById(R.id.card_view_event);
        }
    }
}
