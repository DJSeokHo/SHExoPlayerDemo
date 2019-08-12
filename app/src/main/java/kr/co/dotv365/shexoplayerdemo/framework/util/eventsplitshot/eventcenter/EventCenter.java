package kr.co.dotv365.shexoplayerdemo.framework.util.eventsplitshot.eventcenter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class EventCenter {

    private static EventCenter instance = new EventCenter();
    private HashMap<String, ArrayList<EventObserver>> map = new HashMap<>();

    private EventCenter() {
    }

    public static EventCenter getInstance() {
        return instance;
    }


    public interface EventRunnable {
        void run(String arrow, Object poster, HashMap<String, Object> data);
    }

    public class EventObserver {
        String arrow;
        WeakReference<Object> objectWeakReference;
        public EventRunnable runnable;

        EventObserver(String arrow, Object obj, EventRunnable runnable) {
            this.arrow = arrow;
            this.objectWeakReference = new WeakReference<Object>(obj);
            this.runnable = runnable;
        }
    }

    public void addEventObserver(String arrow, Object obj, EventRunnable runnable) {
        EventObserver eventObserver = new EventObserver(arrow, obj, runnable);
        getObserverListForArrows(arrow).add(eventObserver);
    }

    public void removeAllObserver(Object obj) {

        ArrayList<EventObserver> deleteList = new ArrayList<EventObserver>();
        for (ArrayList<EventObserver> arrayList : map.values()) {
            deleteList.clear();
            for (EventObserver observer : arrayList) {
                if (observer.objectWeakReference.get() == obj) {
                    deleteList.add(observer);
                }
            }
            arrayList.removeAll(deleteList);
        }
    }

    public void removeObserverForArrows(String arrow, Object obj) {

        ArrayList<EventObserver> result = getObserverListForArrows(arrow);
        ArrayList<EventObserver> deleteList = new ArrayList<EventObserver>();

        Object object;
        for (EventObserver eventObserver : result) {
            object = eventObserver.objectWeakReference.get();
            if (object == obj) {
                deleteList.add(eventObserver);
            }
        }

        for (EventObserver eventObserver : deleteList) {
            result.remove(eventObserver);
        }
    }

    public void sendEvent(String arrow, Object sender, HashMap<String, Object> data) {
        ArrayList<EventObserver> result = getObserverListForArrows(arrow);
        for (EventObserver eventObserver : result) {
            if (eventObserver.runnable != null) {
                eventObserver.runnable.run(arrow, sender, data);
            }
        }
    }

    private ArrayList<EventObserver> getObserverListForArrows(String arrow) {
        ArrayList<EventObserver> result = map.get(arrow);

        if (result == null) {
            result = new ArrayList<>();
            map.put(arrow, result);
        }

        return result;
    }


}

