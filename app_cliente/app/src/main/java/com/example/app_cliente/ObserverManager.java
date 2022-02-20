package com.example.app_cliente;

import java.util.ArrayList;
import java.util.List;

public class ObserverManager {
    private static final List<ObserverManager.OnChanged> Observers = new ArrayList<>();

    public static void addObserver(ObserverManager.OnChanged observer){
        if(observer != null){
            Observers.add(observer);
        }
    }
    public static void removeObserver(ObserverManager.OnChanged observer){
        if(observer != null){
            Observers.remove(observer);
        }
    }
    public static void notifyChange(float k_v, float k_i){
        for(ObserverManager.OnChanged observer : Observers){        //for each notify all observers
            observer.onChanged(k_v, k_i);   // notify observers
        }
    }
    public interface OnChanged{
        void onChanged(float k_v, float k_i);
    }
}
