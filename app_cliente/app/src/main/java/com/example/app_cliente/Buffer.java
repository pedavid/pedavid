package com.example.app_cliente;

public class Buffer {
    private Number[] Buffer = {0};

    public Number[] getBuffer() { return this.Buffer; }
    public Number getBufferValue(int position) { return this.Buffer[position]; }

    public short[] asShort() {
        short[] bufferShort = new short[this.Buffer.length];

        for (int i = 0; i < this.Buffer.length; i++) { bufferShort[i] = this.Buffer[i].shortValue(); }

        return bufferShort;
    }

    public int[] asInt() {
        int[] bufferInt = new int[this.Buffer.length];
        int i = 0;

        for (i = 0; i < this.Buffer.length; i++){
            bufferInt[i] = this.Buffer[i].intValue();
        }

        return bufferInt;
    }

    public void SetBuffer( int bufferSize ) { this.Buffer = new Number[bufferSize]; }
    public void SetBufferValue( int position, Number value ){ this.Buffer[position] = value; }
}