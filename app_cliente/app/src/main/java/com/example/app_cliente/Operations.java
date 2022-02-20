package com.example.app_cliente;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.android.material.math.MathUtils;

import java.util.ArrayList;

public class Operations {

    private double currentMeanValue = 0.0;
    private double voltageMeanValue = 0.0;
    private double currentRmsValue = 0.0;
    private double voltageRmsValue = 0.0;
    private double apparentPowerValue = 0.0;
    private double activePowerValue = 0.0;
    private double powerFactor = 0.0;
    private ArrayList mafBufferI = new ArrayList<Double>();
    private ArrayList mafBufferV = new ArrayList<Double>();

    public double getCurrentMeanValue(){return currentMeanValue;}
    public double getVoltageMeanValue(){return voltageMeanValue;}
    public double getCurrentRmsValue(){return currentRmsValue;}
    public double getVoltageRmsValue(){return voltageRmsValue;}

    public void setCurrentMeanValue(double value){this.currentMeanValue = value;}
    public void setVoltageMeanValue(double value){this.voltageMeanValue = value;}
    public void setCurrentRmsValue(double value){this.currentRmsValue = value;}
    public void setVoltageRmsValue(double value){this.voltageRmsValue = value;}


    /* Returns mean value of a given array */
    public double meanValue(int[] value){

        double meanValue=0;
        int arrayLength = value.length - 1;

        for(int i = 1; i <= arrayLength; i++){
            meanValue+=value[i];
        }

        meanValue = meanValue/arrayLength;
        return meanValue;
    }

    public double rmsValue(int[] value){

        double rmsValue = 0;
        int arrayLength = value.length -1;

        for(int i = 1; i <= arrayLength; i++){
            rmsValue += Math.abs(( Math.pow(value[i], 2) ));
        }
        rmsValue = Math.sqrt(rmsValue/arrayLength);
        Log.d("RMSMRMRSMMRS", "rmsValue: "+rmsValue);

        return rmsValue;
    }

    public double apparentPowerValue(){

            this.apparentPowerValue = currentRmsValue * voltageRmsValue;

            return this.apparentPowerValue;
    }

    public double activePowerValue(int[] voltage, int[] current){

        double activePower = 0;
        int voltageLength = voltage.length -1;
        int currentLength = current.length -1;

        if(voltageLength == currentLength) {
            for (int i = 1; i <= voltageLength; i++) {

                activePower += voltage[i] * current[i];
            }
            activePower = activePower/voltageLength;

        }else{
            Log.d("Operation activePower", "Vector lengths don't match");
            return -1;
        }

        this.activePowerValue = activePower;

        return this.activePowerValue;
    }

    double powerFactorValue(){
        this.powerFactor = this.activePowerValue / this.apparentPowerValue;
        return this.powerFactor;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public double movingAvergeFilterVol( double vef ) {
        if( mafBufferV.size() < 6 ) {
            if (mafBufferV.size() == 0) mafBufferV.add( vef );

            else mafBufferV.add( mafBufferV.size(), vef );

            return ( mafBufferV.stream().mapToDouble(i -> (double) i).sum() / mafBufferV.size() );
        }

        else {
            mafBufferV.remove(0);
            mafBufferV.add( mafBufferV.size(), vef );

            return ( mafBufferV.stream().mapToDouble(i -> (double) i).sum() / mafBufferV.size() );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public double movingAvergeFilterCur ( double vef ) {
        if( mafBufferI.size() < 6 ) {
            if (mafBufferI.size() == 0) mafBufferI.add( vef );

            else mafBufferI.add( mafBufferI.size(), vef );

            return ( mafBufferI.stream().mapToDouble(i -> (double) i).sum() / mafBufferI.size() );
        }

        else {
            mafBufferI.remove(0);
            mafBufferI.add( mafBufferI.size(), vef );

            return ( mafBufferI.stream().mapToDouble(i -> (double) i).sum() / mafBufferI.size() );
        }
    }
}