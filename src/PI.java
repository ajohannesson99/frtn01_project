// PI class to be written by you
public class PI {
	// Current PI parameters
	private PIParameters p;

    private double I = 0; // Integral part of controller
    private double e = 0; // Error signal
    private double v = 0; // Output from controller
	
	// Constructor
	public PI(String name) {
		p = new PIParameters();
		p.Beta = 1.0;
		p.H = 0.05;
		p.integratorOn = false;
		p.K = 0.5;
		p.Ti = 0.0;
		p.Tr = 10.0;
		setParameters(p);
    }
	
	// Calculates the control signal v.
	// Called from BeamRegul.
	public synchronized double calculateOutput(double y, double yref) {
		e = yref - y;
		v = p.K*(p.Beta*yref - y) + I;
        return v;
    }
	
	// Updates the controller state.
	// Should use tracking-based anti-windup
	// Called from BeamRegul.
	public synchronized void updateState(double u) {
		if(p.integratorOn){
			I = I + (p.K*p.H/p.Ti)*e + (p.H/p.Tr)*(u - v);
		}else{
			I = 0;
		}
				
    }
	
	// Returns the sampling interval expressed as a long.
	// Note: Explicit type casting needed
	public synchronized long getHMillis() {
        return (long)(p.H * 1000.0);
    }
	
	// Sets the PIParameters.
	// Called from PIGUI.
	// Must clone newParameters.
	public synchronized void setParameters(PIParameters newParameters) {
        p = (PIParameters)newParameters.clone();
		if(!p.integratorOn){
			I = 0;
		}

    }

	public synchronized PIParameters getParameters() {
		p = (PIParameters) this.p.clone();
		return p;
	}
}
