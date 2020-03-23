
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
//hashing imports
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RunBrokers {
    /**
     * Starts brokers locally with different ports
     *              Usage RunBrokers <nBrokers>
     */
    private static String OS = System.getProperty("os.name").toLowerCase();
    private static boolean isWindows() {
        return OS.contains("win");
    }
    private static String classPathDelimiter = isWindows() ? ";" : ":";

    public static void main(String[] args) throws IOException {
        //Usage RunBrokers <nBrokers>
        int nBrokers = Integer.parseInt(args[0]);
        String ip = "127.0.0.1";
        int port = 5000;
        String fileName = "brokers.txt";


        File brokersFile =  new File(fileName);
        PrintWriter pw = new PrintWriter(brokersFile);

        //Array list containing the processes created
        ArrayList<Process> brokers = new ArrayList<>();
        ArrayList<Component> brokerAddr = new ArrayList<> ();

        ArrayList<Process> publishers = new ArrayList<>();



        ArrayList<Process> consumers = new ArrayList<>();


        for (int i = 0 ; i < nBrokers ; i++){
            //Calculating hash of the broker
            int brokerHash = Utilities.getMd5(ip + port).hashCode();
            //Writing to brokers.txt
            pw.printf("%s %d %d%n" , ip , port , brokerHash);
            //Starting the brokers
            String command = String.format("java Broker %s %d %d %s" , ip , port , brokerHash , brokersFile);
            Process p = Runtime.getRuntime().exec(command);
            //Keeping information about the current broker
            brokers.add(p);
            brokerAddr.add(new Component(ip,port));
            System.out.println("[RUNBROKERS] EXEC : " + command);
            //Next node must be started on a new port
            //Running gobblers for the created process' stderr , stdout
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());
            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());
            errorGobbler.start();
            outputGobbler.start();
            port++;
        }
        pw.close();

        Scanner sc = new Scanner(System.in);
        //Program loop watining for commands
        while(true) {
            String line = sc.nextLine();
            //If user typed exit break the loop and terminate created proesses
            if(line.trim().toLowerCase().equals("exit")){
                System.out.println("Bye");
                break;
            }
            else if(line.trim().toLowerCase().equals("start_publisher")) {
                //Starting a publisher
                String command = String.format("java -cp ." + classPathDelimiter + "mp3agic-0.9.0.jar Publisher %s %d a z %s%n" , ip , port , fileName);
                //Adding artists for whom the publisher is responsible to the command
                String[] artists = {"Bob" , "John" , "mpla"};
                for (String artist : artists ){
                    command += " " + artist;
                }
                //Starting the process
                Process p = Runtime.getRuntime().exec(command);
                publishers.add(p);
                System.out.println("[RUNBROKERS] EXEC : " + command);
                //Running gobblers for the created process' stderr , stdout
                StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());
                StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());
                errorGobbler.start();
                outputGobbler.start();
                port++;
                //System.out.println("[RUNBROKERS] EXEC : " + command);
            }
            //Display status of brokers
            else if(line.trim().toLowerCase().equals("broker_status")){
                for(Component c : brokerAddr){
                    System.out.printf("Broker %s %d status : %s%n" , c.getIp() , c.getPort() , brokerStatus(c));
                }
            }
            else{
                System.out.print("exit : exit the program\n" +
                        "start_publisher : start a publisher\n" +
                        "status : show brokers and the artists they are responsible for\n"
                );
            }
        }

        //Destroying the created processes and exiting after user input
        for(Process p : brokers){
            System.out.println("[RUNBROKERS] terminating a broker");
            p.destroy();
        }
        for(Process p : publishers){
            System.out.println("[RUNBROKERS] terminating a publisher");
            p.destroy();
        }
        for(Process p : consumers){
            p.destroy();
        }
        System.out.println("[RUNBROKERS] finished stuff");
        //Exiting
        Runtime.getRuntime().exit(0);


    }
    public static String brokerStatus(Component broker){
        String result = "";
        Request.ReplyFromBroker reply = null;
        Socket s = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            s = new Socket(broker.getIp() , broker.getPort());
            System.out.printf("[RUNBROKERS] Broker(%s,%d) connectionAvailable: %b %n" ,
                    broker.getIp() , broker.getPort() , s.isConnected());
            out =  new ObjectOutputStream(s.getOutputStream());
            //Creating request object
            Request.RequestToBroker request = new Request.RequestToBroker();
            request.method = Request.Methods.STATUS;
            //Sending request
            out.writeObject(request);
            in = new ObjectInputStream(s.getInputStream());
            reply = (Request.ReplyFromBroker) in.readObject();
            result = reply.artists.toString();

        }
        catch(Exception e){
            result = "Unreachable exception : " + e.getMessage();
            e.printStackTrace();
        }
        finally{
            System.out.printf("[RUNBROKERS] closing connection with Broker(%s,%d)%n" ,
                    broker.getIp() , broker.getPort());
            try {
                if(in != null) in.close();
                if(out != null) out.close();
                if(s != null)  s.close();
            }
            catch(Exception e){
                throw new Error();
            }
        }
        return result;
    }

    static class StreamGobbler extends Thread {
        InputStream is;

        // reads everything from is until empty.
        StreamGobbler(InputStream is) {
            this.is = is;
        }

        public void run() {
            Scanner sc = new Scanner(is);
            String line = null;
            try {
                while ((line = sc.nextLine()) != null) {
                    System.out.println(line);
                }
            }
            //Sometimes no such element exception occurs when a process is destroyed
            catch(Exception e){
                System.out.println("Stream gobbler " + this + " terminating");
            }
        }
    }
}
