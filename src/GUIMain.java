import javax.swing.*;

public class GUIMain {

    public static void main(String[] argv) {
        ModeMonitor mon = new ModeMonitor();
        final OpCom opCom = new OpCom(1,mon);

        final int regulPriority     = 8;
        final int refGenPriority    = 7;
        final int plotterPriority   = 6;
        final int serverPriority    = 5;

        // Initialise Control system parts
        ReferenceGenerator refgen = new ReferenceGenerator(refGenPriority);
        SocketServer server = new SocketServer(55000, mon);
        Regul regul = new Regul(regulPriority, mon, server);


        Runnable initializeGUI = new Runnable() {
            @Override
            public void run() {
                opCom.initializeGUI();
                opCom.start();
            }
        };

        regul.setOpCom(opCom);
        regul.setRefGen(refgen);
        opCom.setRegul(regul);

        try {
            SwingUtilities.invokeAndWait(initializeGUI);
        } catch (Exception e) {
            return;
        }

        try {
            opCom.changeActiveSize(1);

            Thread.sleep(1000);

            opCom.changeActiveSize(2);

            Thread.sleep(1000);

            opCom.changeActiveSize(3);

            Thread.sleep(1000);

            opCom.setProgressStatus(1);

            Thread.sleep(1000);

            opCom.setProgressStatus(2);

            Thread.sleep(1000);

            opCom.setProgressStatus(3);
        } catch (Exception e) {
            return;
        }

        refgen.start();
        regul.start();

    }

}
