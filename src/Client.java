import java.net.InetAddress;

/**
 * Created by Marthin on 2016-09-21.
 */
public class Client {
    private InetAddress addr;
    private int port;

    private int errors;
    public Client(InetAddress addr, int port) {
        this.errors = 0;
        this.addr = addr;
        this.port = port;
    }

    public InetAddress getAddr() {
        return addr;
    }
    public void setAddr(InetAddress addr) {
        addr = addr;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public int getErrors() { return errors;}
    public void setErrors(int errors) { this.errors = errors; }

}
