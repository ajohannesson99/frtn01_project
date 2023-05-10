import se.lth.control.realtime.*;

import java.util.ArrayList;
import java.util.List;

public class Regul extends Thread {

    private PID inner = new PID("PIDInner");
    private PID outer = new PID("PIDOuter");

    PIDParameters innerParam = new PIDParameters();
    PIDParameters outerParam = new PIDParameters();

    protected List<Double> volt = new ArrayList<Double>();

    private ReferenceGenerator refGen;
    private OpCom opCom;

    private AnalogIn analogInAngle;
    private AnalogIn analogInPosition;

    private AnalogIn analogInRef;

    private DigitalIn sensor;
    private AnalogOut analogOut;

    private DigitalOut fire;

    private int priority;
    private boolean shouldRun = true;
    private long startTime;

    double angle, position, ref;

    private ModeMonitor modeMon;

    private SocketServer server;

    public Regul(int pri, ModeMonitor modeMon, SocketServer server) {
        priority = pri;
        setPriority(priority);
        this.modeMon = modeMon;
        this.server = server;

        innerParam.K = 2.4;
        innerParam.Ti = 2.8;
        innerParam.Td = 0.2;
        innerParam.H = 0.01;
        innerParam.Tr = 10.0;
        innerParam.N = 10;
        innerParam.Beta = 1.0;
        innerParam.integratorOn = false;

        setInnerParameters(innerParam);

        outerParam.K = -0.25;
        outerParam.Ti = 0.0;
        outerParam.Td = 0.9;
        outerParam.H = 0.01;
        outerParam.N = 10;
        outerParam.Tr = 10.0;
        outerParam.Beta = 1.0;
        outerParam.integratorOn = false;

        setOuterParameters(outerParam);


        try {
            analogInAngle = new AnalogIn(0);
            analogInPosition = new AnalogIn(1);
            analogInRef = new AnalogIn(2);
            sensor = new DigitalIn(0);

            analogOut = new AnalogOut(0);
            fire = new DigitalOut(0);


       } catch (Exception e) {
            System.out.print("Error: IOChannelException: ");
            System.out.println(e.getMessage());
        }

        try {
            analogOut.set(0.0);
        } catch (Exception e) {
            System.out.println(e);
        }

        try{
            angle = analogInAngle.get();
            position = analogInPosition.get();
            ref = analogInRef.get();
        } catch (Exception e){
            System.out.println(e);
        }




    }

    /** Sets OpCom (called from main) */
    public void setOpCom(OpCom opCom) {
        /** Written by you */
        this.opCom = opCom;
    }

    /** Sets ReferenceGenerator (called from main) */
    public void setRefGen(ReferenceGenerator refGen) {
        /** Written by you */
        this.refGen = refGen;
    }

    // Called in every sample in order to send plot data to OpCom
    private void sendDataToOpCom(double yRef, double y, double u) {
        double x = (double) (System.currentTimeMillis() - startTime) / 1000.0;
        opCom.putControlData(x, u);
        opCom.putMeasurementData(x, yRef, y);
    }

    // Sets the inner controller's parameters
    public void setInnerParameters(PIDParameters p) {
        /** Written by you */
        inner.setParameters(p);
    }

    // Gets the inner controller's parameters
    public PIDParameters getInnerParameters() {
        /** Written by you */
        return inner.getParameters();
    }

    // Sets the outer controller's parameters
    public void setOuterParameters(PIDParameters p) {
        /** Written by you */
        this.outer.setParameters(p);
    }

    // Gets the outer controller's parameters
    public PIDParameters getOuterParameters(){
        /** Written by you */
        return this.outer.getParameters();
    }

    // Called from OpCom when shutting down
    public void shutDown() {
        shouldRun = false;
    }

    // Saturation function
    private double limit(double v) {
        return limit(v, -10, 10);
    }
    // Saturation function
    private double limit(double v, double min, double max) {
        if (v < min) v = min;
        else if (v > max) v = max;
        return v;
    }

    public void run() {

        long duration;
        long t = System.currentTimeMillis();
        startTime = t;

        double angleRef = 0.0;
        double yRef, y, u;


        while (shouldRun) {
            /** Written by you */

            //position = readInput(analogInPosition);
            //angle = readInput(analogInAngle);

            //y = position;
            switch (modeMon.getMode()) {
                case OFF: {
                    /** Written by you */
                    yRef = 0;
                    y = 0;
                    u = 0;
                    sendDataToOpCom(yRef, y, u);
                    writeOutput(u);
                    break;
                }
                case BEAM: {
                    angle = readInput(analogInAngle);
                    angleRef = refGen.getRef();
                	 /** Written by you */

                    synchronized (inner) { 
                        u = limit(inner.calculateOutput(angle, angleRef));
                        writeOutput(u);
                        inner.updateState(u);
                    }
                    sendDataToOpCom(angleRef, angle, u);
                    
                    break;
                }
                case BALL: {
                    /** Written by you */
                    angle = readInput(analogInAngle);
                    angleRef = refGen.getRef();
         
                   
                    y = readInput(analogInPosition);
                    yRef = refGen.getRef();
                    double phiff = refGen.getPhiff();
                    
                    synchronized (outer) {
                        angleRef = limit(outer.calculateOutput(y, yRef));
                        // writeOutput(angleRef);
                        outer.updateState(angleRef);
                    }
                    
                    double uff = refGen.getUff();
                    
                    synchronized (inner) {
                        u = limit(inner.calculateOutput(angle, angleRef));
                        volt.add(u);
                        server.writeMessage("volt", String.valueOf(u));
                        
                        writeOutput(u);
                        inner.updateState(u);
                    }
                


                    sendDataToOpCom(y, yRef, u);
                    break;
                }

                case ALIGN: {
                    angle = readInput(analogInAngle);
                    boolean aligned;

                    try{
                        aligned = sensor.get();
                    }catch (Exception e) {
                        aligned = false;
                    }

                    if(!aligned){
                        angleRef += 0.1;
                    }

                    synchronized (inner) {
                        u = limit(inner.calculateOutput(angle, angleRef));
                        writeOutput(u);
                        inner.updateState(u);
                    }
                    sendDataToOpCom(angleRef, angle, u);

                    break;

                }

                default: {
                    System.out.println("Error: Illegal mode.");
                    break;
                }
            }

            //sendDataToOpCom(yRef, y, u);
    
            // sleep
            t = t + inner.getHMillis();
            duration = t - System.currentTimeMillis();
            if (duration > 0) {
                try {
                    sleep(duration);
                } catch (InterruptedException x) {}
            } else {
                System.out.println("Lagging behind...");
            }
        }
        /** Written by you: Set control signal to zero before exiting run loop */


    }

    // Writes the control signal u to the output channel: analogOut
    // @throws: IOChannelException
    private void writeOutput(double u) {
        try {
            analogOut.set(u);
        } catch (IOChannelException e) {
            e.printStackTrace();
        }
    }

    // Reads the measurement value from the input channel: in
    // @throws: IOChannelException
    private double readInput(AnalogIn in) {
        try {
            return in.get();
        } catch (IOChannelException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}
