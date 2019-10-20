package com.tanyiqu.filesafe.exception;

public class NoSuchFileToOpenException extends Exception {

    public NoSuchFileToOpenException(){
        super();
    }

    public NoSuchFileToOpenException(String msg){
        super(msg);
    }

}
