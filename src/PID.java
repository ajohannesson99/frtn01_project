// PID class to be written by you
public class PID {
    // Current PID parameters
    private PIDParameters p;

    private double I = 0; // Integral part of PID
    private double D = 0; // Derivative part of PID
    private double v = 0; // Computed control signal
    private double e = 0; // Error signal
    private double y = 0; // Measurement signal
    private double yOld = 0; // Old measurement signal
    private double ad; // Help variable for Derivative calculation
    private double bd; // Help variable for Derivative calculation

    // Constructor
    public PID(String name) {
        // TODO C3.E8: Write your code here //
        p = new PIDParameters();
        p.Beta = 1.0;
        p.H = 0.05;
        p.integratorOn = false;
        p.K = 0.9;
        p.Ti = 1.0;
        p.Tr = 10.0;
        p.Td = 0.3;
        p.N = 10;

        ad = p.Td / (p.Td + p.N * p.H);
        bd = p.K * ad * p.N;

        setParameters(p);
    }

    // Calculates the control signal v.
    // Called from BeamRegul.
    public synchronized double calculateOutput(double y, double yref) {
        // TODO C3.E8: Write your code here //
        this.y = y;
        e = yref - y;
        D = ad * D - bd * (y - yOld);
        v = p.K * (p.Beta * yref - y) + I + D;
        //System.out.println(y);
        return v;

    }

    // Updates the controller state.
    // Should use tracking-based anti-windup
    // Called from BeamRegul.
    public synchronized void updateState(double u) {
        // TODO C3.E8: Write your code here //
        if(p.integratorOn){
			I = I + (p.K*p.H/p.Ti)*e + (p.H/p.Tr)*(u - v);
		}else{
			I = 0;
		}	
        yOld = y;
    }

    // Returns the sampling interval expressed as a long.
    // Note: Explicit type casting needed
    public synchronized long getHMillis() {
        // TODO C3.E8: Write your code here //
        return (long) (p.H * 1000.0);
    }

    // Sets the PIDParameters.
    // Called from PIDGUI.
    // Must clone newParameters.
    //justera
    public synchronized void setParameters(PIDParameters newParameters) {
        // TODO C3.E8: Write your code here //
        p = (PIDParameters) newParameters.clone();
        ad = p.Td / (p.Td + p.N * p.H);
        bd = p.K * ad * p.N;
        if (!p.integratorOn) {
            I = 0;
        }
    }

    public synchronized PIDParameters getParameters() {
        p = (PIDParameters) this.p.clone();
        return p;
    }
}
