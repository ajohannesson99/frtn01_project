import se.lth.control.realtime.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Regul extends Thread {

    private PID inner = new PID("PIDInner");
    private PID outer = new PID("PIDOuter");

    private int counter;

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

        outerParam.K = -0.35;
        outerParam.Ti = 0.0;
        outerParam.Td = 0.9;
        outerParam.H = 0.01;
        outerParam.N = 10;
        outerParam.Tr = 10.0;
        outerParam.Beta = 1.0;
        outerParam.integratorOn = false;

        setOuterParameters(outerParam);

	    counter = 0;

        volt = new ArrayList<>();


       try {
           analogInAngle = new AnalogIn(0);
           analogInPosition = new AnalogIn(1);
           analogInRef = new AnalogIn(2);
           sensor = new DigitalIn(0);
           analogOut = new AnalogOut(0);
           fire = new DigitalOut(0);
           analogOut.set(0.0);
           angle = analogInAngle.get();
           position = analogInPosition.get();
           ref = analogInRef.get();


       } catch (Exception e) {
            System.out.print("Error: IOChannelException: ");
            System.out.println(e.getMessage());
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
        double yRef, y;
        double u = 0.0;
	    boolean aligned = false;
        int ballSize = 0;

        while (shouldRun) {

            sendBallPosition();

            switch (modeMon.getMode()) {
                case OFF: {
                    /** Written by you */
                    yRef = 0;
                    y = 0;
                    u = 0;
                    sendDataToOpCom(yRef, y, u);
                    opCom.setProgressStatus(0);
                    writeOutput(u);
                    break;
                }

                case START: {
                    opCom.setProgressStatus(-1);
                    angleRef = 0.0;
                    refGen.setManual(0.0);
                    position = 0.0;
		            aligned = false;
		            counter = 0;
                    if (resetFire()) break;
                    server.writeMessage("BeamAligned", "" + 0);
                    volt = new ArrayList<>();

                    innerParam.K = 2.4;
                    inner.setParameters(innerParam);
                    opCom.updateParams();
                    opCom.changeActiveSize(-1);
                    opCom.setProgressStatus(0);
                    modeMon.setMode(ModeMonitor.Mode.BEAM);
                }

                case BEAM: {
                    angle = readInput(analogInAngle);
                    angleRef = refGen.getRef();
                    innerRegulator(u, angle, angleRef);
                    break;
                }
                case BALL: {
                    /** Written by you */
                    angle = readInput(analogInAngle);
                    angleRef = refGen.getRef();
                    y = readInput(analogInPosition);
                    yRef = refGen.getRef();
                    
                    synchronized (outer) {
                        angleRef = limit(outer.calculateOutput(y, yRef));
                        outer.updateState(angleRef);
                    }
                    innerRegulator(u, angle, angleRef);
                    break;
                }

                case ALIGN: {
                    angle = readInput(analogInAngle);
                    try{
                        aligned = !sensor.get();
                        if(aligned){
                            counter += 1;
                        } else {
                            counter = 0;
                        }
			
                    } catch (Exception e) {
			            System.out.println("error");
                    }

                    if(!aligned){
			            angleRef -= 0.005;
		     
                    } else if (counter > 10) {
			            refGen.setManual(angleRef);
                        server.writeMessage("BeamAligned", "" + 1);
			        }

                    opCom.setProgressStatus(-1);

                    innerRegulator(u, angle, angleRef);
                    break;

                }

                case PUSH_BALL: {

                    opCom.setProgressStatus(-1);

                    angle = readInput(analogInAngle);

                    try{
                        fire.set(false);
                    } catch (Exception e){
                        break;
                    }

                    innerRegulator(u, angle, angleRef);
                    break;
                }

		        case WEIGH_BALL: {
                    y = readInput(analogInPosition);
                    refGen.setManual(2.0);
                    yRef = refGen.getRef();
		            angle = readInput(analogInAngle);

                    synchronized (outer) {
                        angleRef = limit(outer.calculateOutput(y, yRef));
                        // writeOutput(angleRef);
                        outer.updateState(angleRef);
                    }


                    synchronized (inner) {
                        u = limit(inner.calculateOutput(angle, angleRef));
                        volt.add(u);
                        writeOutput(u);
                        inner.updateState(u);
                    }

                    sendDataToOpCom(y, yRef, u);

                    double mean = meanOfVolt(volt);

                    if(mean < 0.33) {
                        ballSize = 1;
                    } else if (mean < 1) {
                        ballSize = 2;
                    } else {
                        ballSize = 3;
                    }


		            server.writeMessage("BallSize", "" + ballSize);

                    opCom.setProgressStatus(ballSize);

		            break;
		        }

                case BIG: {
                    opCom.changeActiveSize(ballSize);
                    angle = readInput(analogInAngle);
                    angleRef = server.angleRef;
                    refGen.setManual(angleRef);

                    innerRegulator(u, angle, angleRef);
                    opCom.setProgressStatus(ballSize);
                    break;

                }

                case MEDIUM: {
                    opCom.changeActiveSize(ballSize);
                    y = readInput(analogInPosition);

                    if(server.regulator == 0){
                        innerParam.K = server.K_inner;
                        inner.setParameters(innerParam);
                        opCom.updateParams();
                        angle = readInput(analogInAngle);
                        angleRef = server.angleRef;
                        refGen.setManual(angleRef);

                        innerRegulator(u, angle, angleRef);
                        opCom.setProgressStatus(ballSize);
                        break;

                    } else if (server.regulator == 1) {
                        yRef = server.pos_ref;
                        synchronized (outer) {
                            angleRef = limit(outer.calculateOutput(y, yRef));
                            outer.updateState(angleRef);
                        }

                        angle = readInput(analogInAngle);

                        innerRegulator(u, angle, angleRef);
                        opCom.setProgressStatus(ballSize);
                        break;

                    } else {
                        break;
                    }
                }

                case FINISH: {
                    opCom.setProgressStatus(-1);
                    yRef = 0;
                    y = 0;
                    u = 0;
                    sendDataToOpCom(yRef, y, u);
                    writeOutput(u);
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
    }

    private boolean resetFire() {
        try {
            fire.set(true);
        }catch (Exception e){
            return true;
        }
        return false;
    }

    private void innerRegulator(double u, double angle, double angleRef){

        synchronized (inner) {
            u = limit(inner.calculateOutput(angle, angleRef));
            writeOutput(u);
            inner.updateState(u);
        }
        sendDataToOpCom(angleRef, angle, u);
    }

    private void sendBallPosition() {
        double y;
        y = readInput(analogInPosition);
        server.writeMessage("BallPosition", "" + ((int) y));
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

    private double meanOfVolt (List<Double> volt) {
        double sum = 0.0;
        if(!volt.isEmpty()){
            for(Double v : volt) {
                sum += v;
            }
            return sum / volt.size();
        }
        return  0.0;

    }
}
