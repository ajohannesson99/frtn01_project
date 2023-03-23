public class ModeMonitor {
    private Mode mode = Mode.OFF; // Off mode to start with
    // Sets new mode
    public synchronized void setMode(Mode newMode) {
        mode = newMode;
    }
    // Returns the current mode
    public synchronized Mode getMode() {
        return mode;
    }
// Existing modes public
    enum Mode {

    OFF, BEAM, BALL;
    }
}
