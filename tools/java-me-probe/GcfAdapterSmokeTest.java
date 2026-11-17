import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;

/** Runs the project's GCF adapters on a normal JVM against the loopback probe server. */
public final class GcfAdapterSmokeTest {
    private static final byte[] DATA = new byte[] {
        0, 1, 2, 3, 10, 13, 31, 32, 65, 90, 127, (byte)128, (byte)200, (byte)254, (byte)255
    };

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int http = args.length > 1 ? Integer.parseInt(args[1]) : 18765;
        int tcp = args.length > 2 ? Integer.parseInt(args[2]) : 18766;
        int udp = args.length > 3 ? Integer.parseInt(args[3]) : 18767;
        testHttp(host, http);
        testTcp(host, tcp);
        testUdp(host, udp);
        System.out.println("PASS: GCF HTTP, TCP and UDP adapter smoke tests");
    }

    private static void testHttp(String host, int port) throws Exception {
        HttpConnection get = (HttpConnection) Connector.open(
            "http://" + host + ":" + port + "/health", Connector.READ, true);
        require(get.getResponseCode() == HttpConnection.HTTP_OK, "GCF HTTP GET status");
        drain(get.openInputStream());
        get.close();

        HttpConnection post = (HttpConnection) Connector.open(
            "http://" + host + ":" + port + "/echo", Connector.READ_WRITE, true);
        post.setRequestMethod(HttpConnection.POST);
        post.setRequestProperty("Content-Type", "application/octet-stream");
        OutputStream out = post.openOutputStream();
        out.write(DATA);
        out.close();
        require(post.getResponseCode() == HttpConnection.HTTP_OK, "GCF HTTP POST status");
        require(Arrays.equals(DATA, readAll(post.openInputStream())), "GCF HTTP binary echo");
        post.close();
        System.out.println("PASS: GCF HTTP status and binary body");
    }

    private static void testTcp(String host, int port) throws Exception {
        StreamConnection connection = (StreamConnection) Connector.open(
            "socket://" + host + ":" + port, Connector.READ_WRITE, true);
        OutputStream out = connection.openOutputStream();
        InputStream in = connection.openInputStream();
        out.write(DATA);
        out.flush();
        byte[] received = new byte[DATA.length];
        int offset = 0;
        while (offset < received.length) {
            int count = in.read(received, offset, received.length - offset);
            if (count < 0) throw new AssertionError("GCF TCP peer closed early");
            offset += count;
        }
        require(Arrays.equals(DATA, received), "GCF TCP binary echo");
        connection.close();
        System.out.println("PASS: GCF TCP binary stream");
    }

    private static void testUdp(String host, int port) throws Exception {
        DatagramConnection connection = (DatagramConnection) Connector.open(
            "datagram://" + host + ":" + port, Connector.READ_WRITE, true);
        Datagram request = connection.newDatagram(256);
        request.write(DATA);
        connection.send(request);
        Datagram response = connection.newDatagram(256);
        connection.receive(response);
        byte[] received = new byte[DATA.length];
        response.readFully(received);
        require(Arrays.equals(DATA, received), "GCF UDP DataInput/DataOutput binary datagram");
        connection.close();
        System.out.println("PASS: GCF UDP binary datagram");
    }

    private static void drain(InputStream in) throws Exception {
        try { byte[] buffer = new byte[256]; while (in.read(buffer) != -1) { } }
        finally { in.close(); }
    }

    private static byte[] readAll(InputStream in) throws Exception {
        try {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[256];
            int count;
            while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
            return out.toByteArray();
        } finally { in.close(); }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
