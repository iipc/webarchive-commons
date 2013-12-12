package org.archive.util.io;

public class RuntimeIOException extends RuntimeException {
    private static final long serialVersionUID = 4762025404760379497L;
    
    private int status = 503;
    
    public RuntimeIOException()
    {
    	
    }
    
    public RuntimeIOException(String message)
    {
    	super(message);
    }
    
    public RuntimeIOException(int status)
    {
    	this.status = status;
    }
    
    public RuntimeIOException(Throwable cause)
    {
    	super(cause);
    }
   
    public RuntimeIOException(int status, Throwable cause)
    {
    	super(cause);
    	this.status = status;
    } 
    
    public int getStatus()
    {
    	return status;
    }
}
