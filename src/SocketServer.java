import java.net.*;
import java.io.*;

public class SocketServer {

	private ModeMonitor modeMonitor;

    private ServerSocket server;
    public Socket comSocket;
    private int port;
    private BufferedReader in;
    private PrintWriter out;
    private String inputLine, outputLine;
    public ReaderThread readerThread;
    public ConnectionThread connThread;

	public double angleRef = 0.0;
	public double pos_ref = 0.0;

	public int regulator = 0;


    // Constructor. Input argument = port number, e.g., 55000
    public SocketServer(int port, ModeMonitor modeMonitor) {
	this.port = port;
	this.modeMonitor = modeMonitor;
	try {
	    server = new ServerSocket(port);
	} catch (IOException e) {
	    System.out.println("ServerTank: Could not listen on port " + port);
	    System.exit(-1);
	}
	System.out.println("Waiting for connection");
        connect();

    }



    // Reader thread that reads from the socket. Currently only writes
    // the message on the x-term. Modify this to instead call user method.
    public class ReaderThread extends Thread {

	public void run() {
	    String tag = "";
	    String value = "";
	    inputLine = null;
	    try {
		in = new BufferedReader(new InputStreamReader(comSocket.getInputStream()));
		out = new PrintWriter(comSocket.getOutputStream(), true);
		while ((inputLine = in.readLine()) != null) {
		    tag = SocketProtocol.getTag(inputLine);
		    value = SocketProtocol.getValue(inputLine);
		    System.out.println("Message received: " + tag + " " + value);
			switch (tag){
				case "ControlMode": {
					modeMonitor.setMode(ModeMonitor.Mode.valueOf(value));
					break;
				}
				case "AngleBeam": {
					angleRef = Double.valueOf(value);
					break;
				}

				case "Regulator": {
					// 0 = Bara inner
					// 1 = Inner och Outer
					regulator = Integer.valueOf(value);
					break;
				}

				case "Ball_Ref": {
					pos_ref = Double.valueOf(value);
					break;
				}

				default:
					break;
			}
		}
	    } catch (Exception e) {
		System.out.println("SocketServer: Error readline");
	    }
	    try {
	    in.close();
	    //		server.close();
	    } catch (Exception x) {
	    }
	System.out.println("Reader thread terminated");
	}

    }

    // Method for writing to the socket
    public void writeMessage(String tag, String value) {
	if (out != null) {
		System.out.println("Message sent: " +tag + value);
	    out.println(SocketProtocol.create(tag,value));
	}
    }

    // Connection Thread. Establishes the connection
    public class ConnectionThread extends Thread {

	public void run() {

	    while (true) {
		System.out.println("Waiting for accept");
		try {
		    comSocket = server.accept();
		} catch (Exception x) {
		}
		createThread();
	    }
	}
    }

    // Creates a connection thread that creates (and recreates) the connection
    public void connect() {
	connThread = new ConnectionThread();
	connThread.start();
    }

    // Creates the ReaderThread and creates a PrintWriter
    // to be used when writing to the socket
    public void createThread() {
	readerThread = new ReaderThread();
	readerThread.start();
	try {
	    out = new PrintWriter(comSocket.getOutputStream(), true);
	} catch (IOException e) {
	}
    }

    // Example Main. Creates a socketServer and shows how to write to it.
    /**public static void main(String[] args) throws Exception {
	SocketServer socketServer = new SocketServer(Integer.parseInt(args[0]));
	int i = 0;
	while (true) {
	    i++;
	    socketServer.writeMessage("I1","" + i);
	    try {
		Thread.sleep(1000);
	    } catch (Exception e) {
	    }
	}
    }*/
}

