package ml.kanfa;

import java.io.OutputStream;
import java.util.logging.ConsoleHandler;

public class CustomConsoleHandler extends ConsoleHandler {

    @Override
    public synchronized void setOutputStream(OutputStream out) throws SecurityException {
        super.setOutputStream(System.out);
    }
}
